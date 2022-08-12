package Clients.SETA;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.*;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Random;

public class RidesGenerator {
    private static int rideIds = -1;
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        org.eclipse.paho.client.mqttv3.MqttClient client;
        String broker = "tcp://localhost:1883";
        String clientId = org.eclipse.paho.client.mqttv3.MqttClient.generateClientId();
        String[] pubTopics = {
                "seta/smartcity/rides/district1",
                "seta/smartcity/rides/district2",
                "seta/smartcity/rides/district3",
                "seta/smartcity/rides/district4"};

        int pubQos = 2;

        try {
            // New client with no persistence of the messages
            client = new MqttClient(broker, clientId, null);
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(true);

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
                        System.out.println("Client ID: "
                                + clientId
                                + ", Message delivered - Thread PID: "
                                + Thread.currentThread().getId());
                    }
                }
            });
            rideGeneration(client, pubTopics, pubQos);
            // TODO: Insert some thread for handling the reading from console, so that it is possible to interact with the broker
            /*System.out.println("\n ***  Press a random key to exit *** \n");
            Scanner command = new Scanner(System.in);
            command.nextLine();

            client.disconnect();*/

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
    private static void rideGeneration(MqttClient client, String[] pubTopics, int pubQos)
            throws MqttException, InterruptedException {
        // TODO: Possibly remove the true condition there
        while (true) {
            RideInfo ride1 = generateRide();
            RideInfo ride2 = generateRide();

            MqttMessage message = new MqttMessage(gson.toJson(ride1).getBytes());
            message.setQos(pubQos);

            client.publish(pubTopics[ride1.getStartingDistrict() - 1], message);
            System.out.println(pubTopics[ride1.getStartingDistrict() - 1] + ", " + message);

            MqttMessage message2 = new MqttMessage(gson.toJson(ride2).getBytes());
            message2.setQos(pubQos);

            client.publish(pubTopics[ride2.getStartingDistrict() - 1], message2);
            System.out.println(pubTopics[ride2.getStartingDistrict() - 1] + ", " + message2);
            Thread.sleep(5000);
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
        ride.setStatus(Status.FREE);

        return ride;
    }
}
