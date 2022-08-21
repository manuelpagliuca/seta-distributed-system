package Taxi.MQTT;

import SETA.RideInfo;
import Taxi.Data.TaxiSchema;
import Taxi.gRPC.GrpcModule;
import Taxi.Data.TaxiInfo;
import Taxi.gRPC.GrpcRunnable;
import org.eclipse.paho.client.mqttv3.*;

import java.util.ArrayList;

import static Utility.Utility.*;

public class MQTTModule {
    private final static String MQTT_BROKER_URL = "tcp://localhost:1883";
    private static MqttClient mqttClient = null;
    private static final String clientID = MqttClient.generateClientId();
    private static TaxiInfo thisTaxi;
    private static ArrayList<TaxiInfo> otherTaxis;
    private final GrpcModule grpcModule = GrpcModule.getInstance();

    public MQTTModule(TaxiSchema taxiSchema) {
        thisTaxi = taxiSchema.getTaxiInfo();
        otherTaxis = taxiSchema.getTaxis();
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

        String topic = "seta/smartcity/rides/district" + thisTaxi.getDistrict();
        int qos = 2;
        System.out.println("Subscribed on topic: " + topic);

        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(true);

        connectToMqttBroker(connectOptions);
        setMqttCallback();
        subscribeTopic(topic, qos);
    }

    private void connectToMqttBroker(MqttConnectOptions connectOptions) {
        try {
            mqttClient.connect(connectOptions);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    private void subscribeTopic(String topic, int qos) {
        try {
            mqttClient.subscribe(topic, qos);
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

                System.out.println("District " + thisTaxi.getDistrict() + ": " + rideInfo);

                if (!thisTaxi.isRiding() && !thisTaxi.isRecharging()) {
                    coordinateRide(rideInfo);
                }

                if (message.isRetained()) {
                    // Delete the retained message on the topic
                    System.out.println("This message was retained, I will delete it.");
                    mqttClient.publish(topic, null);
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

    private void coordinateRide(RideInfo rideInfo) throws InterruptedException, MqttException {
        int[] passengerPosition = rideInfo.getStartPosition();
        final double clientDistance = euclideanDistance(thisTaxi.getPosition(), passengerPosition);

        //ArrayList<TaxiInfo> availableTaxisInDistrict = getAvailableTaxisInDistrict(otherTaxis, thisTaxi);

        int totalAck = otherTaxis.size(); // availableTaxisInDistrict.size();
        System.out.println("[Ride " + rideInfo.getId() + "] Waiting for " + totalAck + " ACK votes");

        int receivedAck = grpcModule.coordinateRideGrpcStream(
                clientDistance,
                rideInfo.getStartPosition(),
                false);

        System.out.println("Received a total of " + receivedAck + " ACKs");

        //Object waitingNewStatus = new Object();

        if (receivedAck == totalAck) {
            System.out.println("I got the ownership for the ride " + rideInfo.getId());
            GrpcRunnable.resetACKS();
            performRide(rideInfo/*, waitingNewStatus*/);
        } else {
            // The ride has been taken from someone else
            // It should wait of information in case of new position of the taxi
            // Thread.sleep(1000);
            //waitingNewStatus.wait();
            GrpcRunnable.resetACKS();
            System.out.println("I didn't got the ownership for the ride " + rideInfo.getId());
        }
    }

    private void performRide(RideInfo rideInfo/*, Object waitingNewStatus*/) throws InterruptedException, MqttException {
        thisTaxi.setRiding(true);

        final double distToPassenger = euclideanDistance(thisTaxi.getPosition(), rideInfo.getStartPosition());
        final double rideDistance = euclideanDistance(rideInfo.getStartPosition(), rideInfo.getDestinationPosition());
        final double totalKm = distToPassenger + rideDistance;

        if (thisTaxi.getDistrict() != rideInfo.getDestinationDistrict()) {
            String topic = "seta/smartcity/rides/district" + thisTaxi.getDistrict();
            mqttClient.unsubscribe(topic);
            System.out.println("Unsubscribed on topic: " + topic);

            thisTaxi.setDistrict(rideInfo.getDestinationDistrict());
            topic = "seta/smartcity/rides/district" + thisTaxi.getDistrict();

            mqttClient.subscribe(topic);
            System.out.println("Subscribed on topic: " + topic);
        }

        thisTaxi.getPosition()[0] = rideInfo.getDestinationPosition()[0];
        thisTaxi.getPosition()[1] = rideInfo.getDestinationPosition()[1];

        // Communicate to other that he started the ride
        //communicateNewInfos();
        //waitingNewStatus.notify();

        Thread.sleep(5000);
        thisTaxi.setBattery(thisTaxi.getBattery() - totalKm);
        System.out.printf("I reached the destination and after %,.2f Km the battery levels are %,.2f %%\n",
                totalKm, thisTaxi.getBattery());

        thisTaxi.setRiding(false);

        // Communicate to other that he finished the ride
        //communicateNewInfos();
    }

    /*private void communicateNewInfos() throws InterruptedException {
        int receivedAck = grpcModule.grpcNewInfosAsync();

        int totalAck = otherTaxis.size();
        System.out.println("totalAck to wait : " + totalAck + ", receivedAck: " + receivedAck);

        if (receivedAck == totalAck) {
            System.out.println("The remaining " + totalAck + " taxi/s know your new position.");
        } else {
            System.out.println("Not all the other taxis received correctly your position!");
            removeTaxi();
        }

        StatusRunnable.resetAckNewPosition();
    }*/

    // Close the MQTT connection of this client toward the broker
    /*private void closingMqttConnection(MqttClient mqttClient) throws MqttException {
        if (false) {
            mqttClient.disconnect();
        }
    }*/
}
