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
    // MQTT
    private final static String MQTT_BROKER_URL = "tcp://localhost:1883";
    private final static int QUALITY_OF_SERVICE = 2;
    private final static String DISTRICT_TOPIC_PREFIX = "seta/smartcity/rides/district";
    private final static String COMPLETED_RIDES_TOPIC = "seta/smartcity/rides/completed";
    private final ArrayList<Integer> IDCompletedRides = new ArrayList<>(10);

    // TAXI
    private static TaxiInfo thisTaxi;
    private static ArrayList<TaxiInfo> otherTaxis;
    private final GrpcModule grpcModule = GrpcModule.getInstance();
    private final Object checkBattery;


    public MQTTModule(TaxiSchema taxiSchema, Object checkBatteryRef) {
        thisTaxi = taxiSchema.getTaxiInfo();
        otherTaxis = taxiSchema.getTaxis();
        checkBattery = checkBatteryRef;
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
    public void startMqttClient() throws MqttException {
        MqttClient mqttClient = createNewClient();

        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(false);
        connectToBroker(mqttClient, connectOptions);

        setMqttCallback(mqttClient);

        String initDistrict = DISTRICT_TOPIC_PREFIX + thisTaxi.getDistrict();
        subscribeTopic(mqttClient, initDistrict);
        subscribeTopic(mqttClient, COMPLETED_RIDES_TOPIC);
    }

    private static MqttClient createNewClient() {
        MqttClient mqttClient = null;
        String clientID = MqttClient.generateClientId();

        try {
            mqttClient = new MqttClient(MQTT_BROKER_URL, clientID);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
        return mqttClient;
    }

    private static void connectToBroker(MqttClient mqttClient, MqttConnectOptions connectOptions) {
        try {
            mqttClient.connect(connectOptions);
            mqttClient.disconnect();
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    private void subscribeTopic(MqttClient mqttClient, String topic) throws MqttException {
        try {
            mqttClient.subscribe(topic, QUALITY_OF_SERVICE);
        } catch (MqttException e) {
            mqttClient.disconnect();
            throw new RuntimeException(e);
        }
        System.out.println("Subscribed on topic: " + topic);
    }

    private void setMqttCallback(MqttClient mqttClient) {
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String ride = new String(message.getPayload());
                RideInfo rideInfo = GSON.fromJson(ride, RideInfo.class);

                if (topic.equals("seta/smartcity/rides/completed")) {
                    IDCompletedRides.add(rideInfo.getId());
                } else {
                    if (rideInfo.getStartingDistrict() == thisTaxi.getDistrict()) {
                        if (!IDCompletedRides.contains(rideInfo.getId())) {
                            System.out.println("District " + thisTaxi.getDistrict() + ": " + rideInfo);

                            if (!thisTaxi.isRiding() && !thisTaxi.isRecharging()) {
                                coordinateRide(mqttClient, rideInfo, checkBattery);
                            }

                            /*if (message.isRetained()) {
                                MqttMessage nullPayload = new MqttMessage(new byte[0]);
                                nullPayload.setRetained(true);
                                nullPayload.setQos(QOS);
                                mqttClient.publish(topic, nullPayload);
                            }*/
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

    private void coordinateRide(MqttClient mqttClient, RideInfo rideInfo, Object checkBattery) throws
            InterruptedException, MqttException {
        int[] passengerPosition = rideInfo.getStartPosition();
        final double clientDistance = euclideanDistance(thisTaxi.getPosition(), passengerPosition);

        int totalAck = otherTaxis.size();
        System.out.println("[Ride " + rideInfo.getId() + "] Waiting for " + totalAck + " ACK votes");

        int receivedAck = grpcModule.coordinateRideGrpcStream(
                clientDistance,
                rideInfo.getStartPosition(),
                false);

        System.out.println("Received a total of " + receivedAck + " ACKs");

        if (receivedAck == totalAck) {
            System.out.println("I got the ownership for the ride " + rideInfo.getId());
            GrpcRunnable.resetACKS();
            performRide(mqttClient, rideInfo, checkBattery);
        } else {
            GrpcRunnable.resetACKS();
            System.out.println("I didn't got the ownership for the ride " + rideInfo.getId());
        }
    }

    private void performRide(MqttClient mqttClient, RideInfo rideInfo, Object checkBattery)
            throws InterruptedException, MqttException {
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
                " and after %,.2f Km the battery levels are %,.2f %%\n", totalKm, thisTaxi.getBattery());
        thisTaxi.setRiding(false);
        thisTaxi.addTotalKm(totalKm);
        thisTaxi.incrementTotalRides();

        // Send the completed ride on the "completed" topic
        MqttMessage rideCompletionMsg = new MqttMessage(GSON.toJson(rideInfo).getBytes());
        rideCompletionMsg.setQos(QUALITY_OF_SERVICE);
        mqttClient.publish("seta/smartcity/rides/completed", rideCompletionMsg);

        // Change the subscription district if necessary
        if (thisTaxi.getDistrict() != rideInfo.getDestinationDistrict()) {
            String topic = "seta/smartcity/rides/district" + thisTaxi.getDistrict();
            mqttClient.unsubscribe(topic);
            System.out.println("Unsubscribed on topic: " + topic);

            thisTaxi.setDistrict(rideInfo.getDestinationDistrict());
            topic = "seta/smartcity/rides/district" + thisTaxi.getDistrict();

            mqttClient.subscribe(topic, QUALITY_OF_SERVICE);
            System.out.println("Subscribed on topic: " + topic);
        }

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (checkBattery) {
            checkBattery.notify();
        }
    }
}
