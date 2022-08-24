package Taxi.MQTT;

import SETA.RideInfo;
import Taxi.Data.TaxiSchema;
import Taxi.gRPC.GrpcModule;
import Taxi.Data.TaxiInfo;
import Taxi.gRPC.GrpcRunnable;
import org.eclipse.paho.client.mqttv3.*;

import java.util.ArrayList;
import java.util.Arrays;

import static Utility.Utility.*;

public class MQTTModule {
    private final static String MQTT_BROKER_URL = "tcp://localhost:1883";
    private static MqttClient mqttClient = null;
    private final static int QOS = 2;
    private static final String clientID = MqttClient.generateClientId();
    private static TaxiInfo thisTaxi;
    private static ArrayList<TaxiInfo> otherTaxis;
    private final GrpcModule grpcModule = GrpcModule.getInstance();
    private final Object checkBattery;
    private final ArrayList<Integer> IDcompletedRides = new ArrayList<>(10);
    private static MqttConnectOptions connectOptions = new MqttConnectOptions();

    private final static String completedRides = "seta/smartcity/rides/completed";


    public MQTTModule(TaxiSchema taxiSchema, Object checkBattery) {
        thisTaxi = taxiSchema.getTaxiInfo();
        otherTaxis = taxiSchema.getTaxis();
        this.checkBattery = checkBattery;
    }

    /*
     * Seeking for rides on the relative district on which the taxi is assigned
     * ------------------------------------------------------------------------
     * Essentially, it seeks for rides by subscribing the MQTT client on the
     * relative topic of the district.
     *
     * Once the message is arrived, it means that there is the presence of a ride
     * on the district, this will trigger the function for electing which taxi
     * should take the ownership of the ride (the coordination will be performed
     * through gRPC).
     */
    public void startMqttClient() {
        try {
            mqttClient = new MqttClient(MQTT_BROKER_URL, clientID);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }

        String initialDistrict = "seta/smartcity/rides/district" + thisTaxi.getDistrict();
        System.out.println("Subscribed on topic: " + initialDistrict);

        connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(false);

        try {
            mqttClient.connect(connectOptions);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }

        setMqttCallback();
        subscribeTopic(initialDistrict);
        subscribeTopic(completedRides);
    }

    private void subscribeTopic(String topic) {
        try {
            mqttClient.subscribe(topic, QOS);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    private void setMqttCallback() {
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String ride = new String(message.getPayload());
                RideInfo rideInfo = GSON.fromJson(ride, RideInfo.class);

                if (topic.equals("seta/smartcity/rides/completed")) {
                    IDcompletedRides.add(rideInfo.getId());
                } else {
                    if (rideInfo.getStartingDistrict() == thisTaxi.getDistrict()) {
                        if (!IDcompletedRides.contains(rideInfo.getId())) {
                            System.out.println("District " + thisTaxi.getDistrict() + ": " + rideInfo);

                            if (!thisTaxi.isRiding() && !thisTaxi.isRecharging()) {
                                coordinateRide(rideInfo, checkBattery);
                            }

                            if (message.isRetained()) {
                                MqttMessage nullPayload = new MqttMessage();
                                nullPayload.setRetained(true);
                                nullPayload.setQos(QOS);
                                mqttClient.publish(topic, nullPayload);
                            }
                        }
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                try {
                    System.out.println(token.getMessage());
                } catch (MqttException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void coordinateRide(RideInfo rideInfo, Object checkBattery) throws InterruptedException, MqttException {
        int[] passengerPosition = rideInfo.getStartPosition();
        final double clientDistance = euclideanDistance(thisTaxi.getPosition(), passengerPosition);

        int totalAck = otherTaxis.size(); // availableTaxisInDistrict.size();
        System.out.println("[Ride " + rideInfo.getId() + "] Waiting for " + totalAck + " ACK votes");

        int receivedAck = grpcModule.coordinateRideGrpcStream(
                clientDistance,
                rideInfo.getStartPosition(),
                false);

        System.out.println("Received a total of " + receivedAck + " ACKs");

        if (receivedAck == totalAck) {
            System.out.println("I got the ownership for the ride " + rideInfo.getId());
            GrpcRunnable.resetACKS();
            performRide(rideInfo, checkBattery);
        } else {
            GrpcRunnable.resetACKS();
            System.out.println("I didn't got the ownership for the ride " + rideInfo.getId());
        }
    }

    private void performRide(RideInfo rideInfo, Object checkBattery) throws InterruptedException, MqttException {
        thisTaxi.setRiding(true);

        final double distToPassenger = euclideanDistance(thisTaxi.getPosition(), rideInfo.getStartPosition());
        final double rideDistance = euclideanDistance(rideInfo.getStartPosition(), rideInfo.getDestinationPosition());
        final double totalKm = distToPassenger + rideDistance;


        thisTaxi.getPosition()[0] = rideInfo.getDestinationPosition()[0];
        thisTaxi.getPosition()[1] = rideInfo.getDestinationPosition()[1];

        Thread.sleep(5000);


        thisTaxi.setBattery(thisTaxi.getBattery() - totalKm);
        System.out.printf("I reached the destination " +
                Arrays.toString(rideInfo.getDestinationPosition()) +
                " at district " +
                rideInfo.getDestinationDistrict() +
                " and after %,.2f Km the battery levels are %,.2f %%", totalKm, thisTaxi.getBattery());
        thisTaxi.setRiding(false);
        thisTaxi.addTotalKm(totalKm);
        thisTaxi.incrementTotalRides();

        // Send the completed ride on the "completed" topic
        MqttMessage rideCompletionMsg = new MqttMessage(GSON.toJson(rideInfo).getBytes());
        rideCompletionMsg.setQos(QOS);
        mqttClient.publish("seta/smartcity/rides/completed", rideCompletionMsg);

        // Change the subscription district if necessary
        if (thisTaxi.getDistrict() != rideInfo.getDestinationDistrict()) {
            String topic = "seta/smartcity/rides/district" + thisTaxi.getDistrict();
            mqttClient.unsubscribe(topic);
            System.out.println("Unsubscribed on topic: " + topic);

            thisTaxi.setDistrict(rideInfo.getDestinationDistrict());
            topic = "seta/smartcity/rides/district" + thisTaxi.getDistrict();

            mqttClient.subscribe(topic, QOS);
            System.out.println("Subscribed on topic: " + topic);
        }
    }

    // Close the MQTT connection of this client toward the broker
    /*private void closingMqttConnection(MqttClient mqttClient) throws MqttException {
        if (false) {
            mqttClient.disconnect();
        }
    }*/
}
