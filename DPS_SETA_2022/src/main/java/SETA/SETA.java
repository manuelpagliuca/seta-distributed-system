/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. in Computer Science @UNIMI A.Y. 2021/2022 */
package SETA;

import Misc.Utility;

import SETA.Structures.RideInfo;

import org.eclipse.paho.client.mqttv3.*;

import java.util.*;

/*
 * SETA
 * ------------------------------------------------------------------------------
 * This class is a stand-alone process that produces 2 rides every 5 seconds,
 * these rides will be sent on the relative district (extracted from their starting
 * position).
 *
 * The class is designed in such a way that the rides who haven't got
 * accomplished will be retained, this through 4 priority queues (one for each district).
 * The generated ride will be added to the relative priority queue, and then
 * publish the ride with the highest priority on the topic of the district (except
 * for a corner case, see comments on 'sendQueuedRide()'
 */
public class SETA {
    private static int rideIds = 0;
    private static final int QOS = 2;
    private final static String BROKER_ADDRESS = "tcp://localhost:1883";
    private final static String COMPLETED_RIDES_TOPIC = "seta/smartcity/rides/completed";
    private static Queue<RideInfo> queueDistrict1;
    private static Queue<RideInfo> queueDistrict2;
    private static Queue<RideInfo> queueDistrict3;
    private static Queue<RideInfo> queueDistrict4;

    public static void main(String[] args) {
        org.eclipse.paho.client.mqttv3.MqttClient client;
        String clientId = org.eclipse.paho.client.mqttv3.MqttClient.generateClientId();

        initDistrictQueues();

        try {
            client = new MqttClient(BROKER_ADDRESS, clientId, null);
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(true);

            client.connect(connectOptions);

            client.setCallback(getCallback(clientId));

            client.subscribe(COMPLETED_RIDES_TOPIC);

            rideGeneration(client);
        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        } catch (InterruptedException me) {
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            throw new RuntimeException(me);
        }
    }

    /* Return the callback for handling the reception of the messages
     * ------------------------------------------------------------------------------
     * This process can listen only on the '/seta/smartcity/rides/completed' topic
     * for handling the recycle of the rides in each queue.
     *
     * After a ride will be accomplished the taxi will send a message on this topic
     * for notify the completion, when the message is received from SETA it will remove
     * the ride from the relative queue (to the starting district of the ride).
     */
    private static MqttCallback getCallback(String clientId) {
        return new MqttCallback() {
            public void messageArrived(String topic, MqttMessage message) {
                if (topic.equals(COMPLETED_RIDES_TOPIC)) {
                    RideInfo completedRide = Utility.GSON.fromJson(message.toString(), RideInfo.class);

                    if (completedRide.getStartingDistrict() == 1) {
                        queueDistrict1.remove();
                    } else if (completedRide.getStartingDistrict() == 2) {
                        queueDistrict2.remove();
                    } else if (completedRide.getStartingDistrict() == 3) {
                        queueDistrict3.remove();
                    } else if (completedRide.getStartingDistrict() == 4) {
                        queueDistrict4.remove();
                    }
                    //printDistrictQueues(); //debug
                }
            }

            public void connectionLost(Throwable cause) {
                System.out.println(clientId
                        + " Connectionlost! cause:" + cause.getMessage()
                        + "-  Thread PID: " + Thread.currentThread().getId());
            }

            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        };
    }

    /* Ride generation and publishing on MQTT district topics
     * ------------------------------------------------------------------------------
     * This method generate 2 rides each 5 seconds on the relative topics (this will
     * depend on the district of the generated rides).
     *
     * Then send the rides on the relative district topics, the 'sendQueuedRide()' will
     * buffer the messages in the district queues and send the message with the highest
     * priority to the topic.
     */
    @SuppressWarnings("BusyWait")
    private static void rideGeneration(MqttClient client) throws MqttException, InterruptedException {
        while (!Thread.currentThread().isInterrupted()) {
            RideInfo ride1 = generateRide();
            RideInfo ride2 = generateRide();

            sendQueuedRide(client, ride1, false);
            final boolean sameDistrict = ride1.getStartingDistrict() == ride2.getStartingDistrict();
            sendQueuedRide(client, ride2, sameDistrict);

            Thread.sleep(5000);
        }
    }

    /* Send the ride on the topic of the starting district of the ride
     * ------------------------------------------------------------------------------
     * Since there is a system for buffering the rides which are not accomplished,
     * the first thing to do is add the ride to the queue of his district.
     *
     * If the ride is not in the same district as the previous ride (Note: the first
     * ride will always have this flag set to 'false'), then we must pick
     * the ride with the highest priority from his district queue.
     *
     * Otherwise, if the ride is the same of the previous district we just send that,
     * otherwise it will keep send the same previous ride (previous because it will
     * have a smaller ride, so an higher priority).
     */
    private static void sendQueuedRide(MqttClient client, RideInfo ride, boolean sameDistrictPrevRide)
            throws MqttException {
        addRideDistrictQueue(ride);
        //printDistrictQueues(); //debug
        MqttMessage mqttMessage;
        if (!sameDistrictPrevRide)
            ride = pickRideDistrictQueue(ride.getStartingDistrict());

        mqttMessage = new MqttMessage(Utility.GSON.toJson(ride).getBytes());
        mqttMessage.setQos(QOS);
        String topic1 = "seta/smartcity/rides/district" + ride.getStartingDistrict();
        client.publish(topic1, mqttMessage);
        System.out.println(topic1 + ", " + mqttMessage);
    }

    /* Generate a ride with random positions
     * ------------------------------------------------------------------------------
     * The starting and destination positions of the rides are generated in such
     * a way that they will be in range [0,10]. Also, the destination position will
     * keep getting regenerated in case it will be equal to the start position.
     */
    public static RideInfo generateRide() {
        RideInfo ride = new RideInfo();

        int[] startPos = generateRandomPosition();
        int[] destPos = generateRandomPosition();
        ride.setStartPosition(startPos);

        while (Arrays.equals(destPos, startPos))
            destPos = generateRandomPosition();

        ride.setDestinationPosition(destPos);
        ride.setId(++rideIds);

        return ride;
    }

    /// Utility

    // Generate random position in the range [0,10]
    private static int[] generateRandomPosition() {
        Random random = new Random();
        int[] position = new int[2];
        position[0] = random.nextInt(0, 10);
        position[1] = random.nextInt(0, 10);
        return position;
    }

    // Prints the content of the queues (debug)
    private static void printDistrictQueues() {
        System.out.println("Queue District 1: " + queueDistrict1);
        System.out.println("Queue District 2: " + queueDistrict2);
        System.out.println("Queue District 3: " + queueDistrict3);
        System.out.println("Queue District 4: " + queueDistrict4);
    }

    // Check the first value of a district queue given the district
    private static RideInfo pickRideDistrictQueue(int startingDistrict) {
        if (startingDistrict == 1)
            return queueDistrict1.peek();
        else if (startingDistrict == 2)
            return queueDistrict2.peek();
        else if (startingDistrict == 3)
            return queueDistrict3.peek();
        return queueDistrict4.peek();
    }

    // Add a ride to the relative district queue given the ride
    private static void addRideDistrictQueue(RideInfo ride) {
        if (ride.getStartingDistrict() == 1) {
            queueDistrict1.add(ride);
        } else if (ride.getStartingDistrict() == 2) {
            queueDistrict2.add(ride);
        } else if (ride.getStartingDistrict() == 3) {
            queueDistrict3.add(ride);
        } else {
            queueDistrict4.add(ride);
        }
    }

    // Initializes the queue used for the recycling of rides with the comparators
    private static void initDistrictQueues() {
        queueDistrict1 = new PriorityQueue<>(5, (o1, o2) -> compareRideIDs(o1, o2));
        queueDistrict2 = new PriorityQueue<>(5, (o1, o2) -> compareRideIDs(o1, o2));
        queueDistrict3 = new PriorityQueue<>(5, (o1, o2) -> compareRideIDs(o1, o2));
        queueDistrict4 = new PriorityQueue<>(5, (o1, o2) -> compareRideIDs(o1, o2));
    }

    // Compare the rides by ride ID, which is incremental (the oldest ride/smaller ID is preferred)
    private static int compareRideIDs(RideInfo o1, RideInfo o2) {
        if (o1.getId() > o2.getId())
            return 1;
        else if (o1.getId() < o2.getId())
            return -1;
        return 0;
    }
}