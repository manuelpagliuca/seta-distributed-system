/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. in Computer Science @UNIMI A.Y. 2021/2022 */
package Taxi.MQTT;

import static Misc.Utility.*;

import SETA.Structures.RideInfo;

import Taxi.Structures.TaxiSchema;
import Taxi.gRPC.GrpcModule;
import Taxi.Structures.TaxiInfo;
import Taxi.gRPC.GrpcRunnable;

import org.eclipse.paho.client.mqttv3.*;

import java.util.ArrayList;
import java.util.Arrays;

/*
 * MQTTModule
 * ------------------------------------------------------------------------------
 * This class handles the connections to the MQTT broker from the taxi side. It
 * allows the subscription to district topics for receiving the rides. This class
 * allows also the taxi to publish on a specific a topic
 * "seta/smartcity/rides/completed" the completion of a ride, but it is also
 * subscribed so that he can know when a ride got taken.
 */
public class MQTTModule {
    private final static String MQTT_BROKER_URL = "tcp://localhost:1883";
    private final static int QUALITY_OF_SERVICE = 2;
    private final static String DISTRICT_TOPIC_PREFIX = "seta/smartcity/rides/district";
    private final static String COMPLETED_RIDES_TOPIC = "seta/smartcity/rides/completed";
    private final ArrayList<Integer> IDCompletedRides = new ArrayList<>(10);
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
     * ------------------------------------------------------------------------------
     * Essentially, it seeks for rides by subscribing the MQTT client on the
     * relative topic of the district.
     *
     * Once the message is arrived, it means that there is the presence of a ride
     * on the district, this will trigger the function for electing which taxi
     * should take the ownership of the ride (the coordination will be performed
     * through gRPC).
     */
    public void initMQTTConnection() {
        MqttClient mqttClient = createNewClient();

        // Connect to the broker
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(true);
        connectToBroker(mqttClient, connectOptions);

        // Set the callbacks (ride handling)
        setMqttCallback(mqttClient);

        // Subscribe to the initial district topic + topics for rides
        String initDistrict = DISTRICT_TOPIC_PREFIX + thisTaxi.getDistrict();
        subscribeTopic(mqttClient, initDistrict);
        subscribeTopic(mqttClient, COMPLETED_RIDES_TOPIC);
    }

    // Set the callbacks for handling the received messages of a given MQTT client
    private void setMqttCallback(MqttClient mqttClient) {
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                parseMessage(topic, message, mqttClient);
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

    /* Parse the received messages for infer which operations the taxi should do
     * ------------------------------------------------------------------------------
     * In the case the message is received on the "seta/smartcity/rides/completed"
     * topic it will add the ride on the list for completed rides (so that other
     * taxis can ignore it).
     *
     * In the case the message is received on the topic district of this taxi,
     * then if the ride isn't completed and the taxis isn't busy it will handle
     * the ride.
     */
    private void parseMessage(String topic, MqttMessage message, MqttClient mqttClient)
            throws InterruptedException, MqttException {
        String ride = new String(message.getPayload());
        RideInfo rideInfo = GSON.fromJson(ride, RideInfo.class);

        if (topic.equals(COMPLETED_RIDES_TOPIC)) {
            IDCompletedRides.add(rideInfo.getId());
        } else {
            if (rideInfo.getStartingDistrict() == thisTaxi.getDistrict()) {
                if (!IDCompletedRides.contains(rideInfo.getId())) {
                    System.out.println("District " + thisTaxi.getDistrict() + ": " + rideInfo);

                    if (!thisTaxi.isRiding() && !thisTaxi.isRecharging()) {
                        coordinateRide(mqttClient, rideInfo, checkBattery);
                    }
                }
            }
        }
    }

    /* Coordinate the ride with the other taxis for see who should take the charge
     * ------------------------------------------------------------------------------
     * It sends in broadcast a request for taking this ride to the other taxis, the
     * other taxis will check if the parameters are optimal for this taxi (consider
     * that this function will be executed by n taxi whenre n is the number of
     * process taxi started).
     *
     * If the number of ACKs received is equal to the number of requests done (so
     * the number of taxis since it is in broadcast) this taxi can handle the ride.
     *
     * In the while it checks if the ride didn't get completed by any other taxi.
     * Before starting the procedure for executing the ride it sends the message
     * of this ride is completed on "seta/smartcity/rides/completed" topic (now
     * no-one else can take the ownership of this ride).
     */
    private void coordinateRide(MqttClient mqttClient, RideInfo rideInfo, Object checkBattery) throws
            InterruptedException, MqttException {
        int[] passengerPosition = rideInfo.getStartPosition();
        final double clientDistance = euclideanDistance(thisTaxi.getPosition(), passengerPosition);

        final int totalAck = otherTaxis.size();
        //System.out.println("[Ride " + rideInfo.getId() + "] Waiting for " + totalAck + " ACK votes"); //debug

        final int receivedAck = grpcModule.coordinateRideGrpcStream(
                clientDistance,
                rideInfo.getStartPosition(),
                false);

        if (receivedAck == totalAck) {
            // Check that any other taxi didn't complete this ride
            if (!IDCompletedRides.contains(rideInfo.getId())) {
                // Send the completed ride on the "completed" topic
                MqttMessage rideCompletionMsg = new MqttMessage(GSON.toJson(rideInfo).getBytes());
                rideCompletionMsg.setQos(QUALITY_OF_SERVICE);
                mqttClient.publish(COMPLETED_RIDES_TOPIC, rideCompletionMsg);

                // Reset the acks
                System.out.println("I got the ownership for the ride " + rideInfo.getId());
                GrpcRunnable.resetACKS();

                // Perform ride
                performRide(mqttClient, rideInfo, checkBattery);
            }
        } else {
            GrpcRunnable.resetACKS();
            System.out.println("I didn't got the ownership for the ride " + rideInfo.getId());
        }
    }

    /* Perform the actual assigned ride
     * ------------------------------------------------------------------------------
     * Just compute the distances from the taxi to the starting position of the ride,
     * then from the start to the end position of the ride. Use this information for
     * decrement the battery levels.
     *
     * Before updating the taxi information like number of rides, battery levels and
     * global km traveled it make wait the taxi 5 seconds.
     *
     * At the end make the taxi available again.
     */
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
        System.out.printf("I reached the destination " + Arrays.toString(rideInfo.getDestinationPosition()) +
                " at district " + rideInfo.getDestinationDistrict() +
                " and after %,.2f Km the battery levels are %,.2f %%\n", totalKm, thisTaxi.getBattery());
        thisTaxi.setRiding(false);
        thisTaxi.addTotalKm(totalKm);
        thisTaxi.incrementTotalRides();

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

    /// Utility

    // Creates and return a new MQTT client
    private static MqttClient createNewClient() {
        MqttClient mqttClient;
        String clientID = MqttClient.generateClientId();

        try {
            mqttClient = new MqttClient(MQTT_BROKER_URL, clientID);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
        return mqttClient;
    }

    // Connect the given MQTT client to the broker
    private static void connectToBroker(MqttClient mqttClient, MqttConnectOptions connectOptions) {
        try {
            mqttClient.connect(connectOptions);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    // Subscribe the given MQTT client to the topic
    private void subscribeTopic(MqttClient mqttClient, String topic) {
        try {
            mqttClient.subscribe(topic, QUALITY_OF_SERVICE);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }
}