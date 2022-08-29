/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. of Computer Science @UNIMI A.Y. 2021/2022 */
package SETA;

import Misc.Utility;
import SETA.Structures.RideInfo;
import jdk.jshell.execution.Util;
import org.eclipse.paho.client.mqttv3.*;

import java.util.*;

public class SETA {
    private static int rideIds = 0;
    private static final int QOS = 2;
    private final static String COMPLETED_RIDES_TOPIC = "seta/smartcity/rides/completed";
    private static Queue<RideInfo> queueDistrict1;
    private static Queue<RideInfo> queueDistrict2;
    private static Queue<RideInfo> queueDistrict3;
    private static Queue<RideInfo> queueDistrict4;

    public static void main(String[] args) {
        org.eclipse.paho.client.mqttv3.MqttClient client;
        String broker = "tcp://localhost:1883";
        String clientId = org.eclipse.paho.client.mqttv3.MqttClient.generateClientId();

        initDistrictQueues();

        try {
            // New client with no persistence of the messages
            client = new MqttClient(broker, clientId);
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(true);

            // Connect to client
            client.connect(connectOptions);

            client.setCallback(new MqttCallback() {
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
                        //printDistrictQueues();
                    }
                }

                public void connectionLost(Throwable cause) {
                    System.out.println(clientId
                            + " Connectionlost! cause:"
                            + cause.getMessage()
                            + "-  Thread PID: "
                            + Thread.currentThread().getId());
                }

                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });

            client.subscribe(COMPLETED_RIDES_TOPIC);
            rideGeneration(client);
            // TODO: Insert some thread for handling the reading from console,
            // todo: so that it is possible to interact with the broker
            /*
               System.out.println("\n ***  Press a random key to exit *** \n");
               Scanner command = new Scanner(System.in);
               command.nextLine();
               client.disconnect();
             */
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

    private static void initDistrictQueues() {
        queueDistrict1 = new PriorityQueue<>(5, (o1, o2) -> compareRideIDs(o1, o2));
        queueDistrict2 = new PriorityQueue<>(5, (o1, o2) -> compareRideIDs(o1, o2));
        queueDistrict3 = new PriorityQueue<>(5, (o1, o2) -> compareRideIDs(o1, o2));
        queueDistrict4 = new PriorityQueue<>(5, (o1, o2) -> compareRideIDs(o1, o2));
    }

    private static int compareRideIDs(RideInfo o1, RideInfo o2) {
        if (o1.getId() > o2.getId())
            return 1;
        else if (o1.getId() < o2.getId())
            return -1;
        return 0;
    }

    /* Ride generation and publishing on the respective topics
     * ------------------------------------------------------------------------------
     * This method generate 2 rides each 5 seconds on the relative topics (this will
     * depend on the district of the generated rides).
     */
    @SuppressWarnings("BusyWait")
    private static void rideGeneration(MqttClient client) throws MqttException, InterruptedException {
        while (!Thread.currentThread().isInterrupted()) {
            RideInfo ride1 = generateRide();
            RideInfo ride2 = generateRide();

            // Todo: Trattare il caso in cui entrambe le ride pubblichino sullo stesso distretto
            boolean sameDistrict = false;
            sendQueuedRide(client, ride1, false);

            if (ride1.getStartingDistrict() == ride2.getStartingDistrict())
                sameDistrict = true;

            sendQueuedRide(client, ride2, sameDistrict);

            Thread.sleep(5000);
        }
    }

    private static void printDistrictQueues() {
        System.out.println("Queue District 1: " + queueDistrict1);
        System.out.println("Queue District 2: " + queueDistrict2);
        System.out.println("Queue District 3: " + queueDistrict3);
        System.out.println("Queue District 4: " + queueDistrict4);
    }

    private static void sendQueuedRide(MqttClient client, RideInfo ride, boolean sameDistrict) throws MqttException {
        addRideDistrictQueue(ride);
        //printDistrictQueues();
        MqttMessage mqttMessage;
        if (!sameDistrict) {
            // Overwrite with higher priority rides
            ride = pickRideDistrictQueue(ride.getStartingDistrict());
        }
        mqttMessage = new MqttMessage(Utility.GSON.toJson(ride).getBytes());

        //System.out.println("Prioritized " + prioritizedRide.toString() + " from " + ride.getStartingDistrict());
        mqttMessage.setQos(QOS);

        String topic1 = "seta/smartcity/rides/district" + ride.getStartingDistrict();
        client.publish(topic1, mqttMessage);
        System.out.println(topic1 + ", " + mqttMessage);
    }

    private static RideInfo pickRideDistrictQueue(int startingDistrict) {
        if (startingDistrict == 1)
            return queueDistrict1.peek();
        else if (startingDistrict == 2)
            return queueDistrict2.peek();
        else if (startingDistrict == 3)
            return queueDistrict3.peek();
        return queueDistrict4.peek();
    }

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

    private static boolean isDistrictQueueEmpty(RideInfo ride) {
        if (ride.getStartingDistrict() == 1) {
            return queueDistrict1.isEmpty();
        } else if (ride.getStartingDistrict() == 2) {
            return queueDistrict2.isEmpty();
        } else if (ride.getStartingDistrict() == 3) {
            return queueDistrict3.isEmpty();
        }
        return queueDistrict4.isEmpty();
    }

    private static void addToDistrictQueue(RideInfo ride) {
        if (ride.getStartingDistrict() == 1) {
            if (!queueDistrict1.contains(ride)) {
                if (queueDistrict1.size() >= 5) {
                    queueDistrict1.add(ride);
                }
            }
        } else if (ride.getStartingDistrict() == 2) {
            if (!queueDistrict2.contains(ride)) {
                if (queueDistrict2.size() >= 5) {
                    queueDistrict2.add(ride);
                } else {

                }
            }
        } else if (ride.getStartingDistrict() == 3) {
            if (!queueDistrict3.contains(ride)) {
                if (queueDistrict3.size() >= 5) {
                    queueDistrict3.add(ride);
                }
            }
        } else if (ride.getStartingDistrict() == 4) {
            if (!queueDistrict4.contains(ride)) {
                if (queueDistrict4.size() >= 5) {
                    queueDistrict4.add(ride);
                }
            }
        }
    }

    public static RideInfo generateRide() {
        RideInfo ride = new RideInfo();

        Random random = new Random();
        int[] startPos = new int[2];
        startPos[0] = random.nextInt(0, 10);
        startPos[1] = random.nextInt(0, 10);
        ride.setStartPosition(startPos);

        int[] destPos = new int[2];
        destPos[0] = random.nextInt(0, 10);
        destPos[1] = random.nextInt(0, 10);

        while (Arrays.equals(destPos, startPos)) {
            destPos[0] = random.nextInt(0, 10);
            destPos[1] = random.nextInt(0, 10);
        }

        ride.setDestinationPosition(destPos);
        ride.setId(++rideIds);

        return ride;
    }
}
