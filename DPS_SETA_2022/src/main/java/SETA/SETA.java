package SETA;

import Utility.Utility;
import org.eclipse.paho.client.mqttv3.*;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Random;

public class SETA {
    private static int rideIds = 0;
    private static final int QOS = 2;

    public static void main(String[] args) {
        org.eclipse.paho.client.mqttv3.MqttClient client;
        String broker = "tcp://localhost:1883";
        String clientId = org.eclipse.paho.client.mqttv3.MqttClient.generateClientId();

        try {
            // New client with no persistence of the messages
            client = new MqttClient(broker, clientId);
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(false);

            // Connect to client
            client.connect(connectOptions);

            client.setCallback(new MqttCallback() {
                public void messageArrived(String topic, MqttMessage message) {
                    // Called when a message arrives from the server that matches any subscription made by the client
                    String time = new Timestamp(System.currentTimeMillis()).toString();

                    String receivedMessage = new String(message.getPayload());
                    System.out.println(clientId + " Received a Message! - Callback - Thread PID: "
                            + Thread.currentThread().getId()
                            + "\n\tTime:    " + time
                            + "\n\tTopic:   " + topic
                            + "\n\tMessage: " + receivedMessage
                            + "\n\tQoS:     " + message.getQos() + "\n");

                    System.out.println("\n ***  Press a random key to exit *** \n");
                }

                public void connectionLost(Throwable cause) {
                    System.out.println(clientId
                            + " Connectionlost! cause:"
                            + cause.getMessage()
                            + "-  Thread PID: "
                            + Thread.currentThread().getId());
                }

                public void deliveryComplete(IMqttDeliveryToken token) {
                    if (token.isComplete()) {
                        /*System.out.println("Client ID: "
                                + clientId
                                + ", Message delivered - Thread PID: "
                                + Thread.currentThread().getId());*/
                    }
                }
            });
            rideGeneration(client, QOS);
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

    /* Ride generation and publishing on the respective topics
     * ------------------------------------------------------------------------------
     * This method generate 2 rides each 5 seconds on the relative topics (this will
     * depend on the district of the generated rides).
     */
    @SuppressWarnings("BusyWait")
    private static void rideGeneration(MqttClient client, int pubQos) throws
            MqttException, InterruptedException {
        while (!Thread.currentThread().isInterrupted()) {
            RideInfo ride1 = generateRide();
            RideInfo ride2 = generateRide();

            MqttMessage message = new MqttMessage(Utility.GSON.toJson(ride1).getBytes());
            message.setQos(pubQos);
            message.setRetained(true);

            String topic1 = "seta/smartcity/rides/district" + ride1.getStartingDistrict();
            client.publish(topic1, message);
            System.out.println(topic1 + ", " + message);
            client.isConnected();

            MqttMessage message2 = new MqttMessage(Utility.GSON.toJson(ride2).getBytes());
            message2.setQos(pubQos);
            message2.setRetained(true);

            String topic2 = "seta/smartcity/rides/district" + ride2.getStartingDistrict();
            client.publish(topic2, message2);
            System.out.println(topic2 + ", " + message2);

            // TODO revert to 5000 !!
            Thread.sleep(10000);
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
