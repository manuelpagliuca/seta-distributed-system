package Clients.Taxi;/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. of Computer Science @UNIMI A.Y. 2021/2022 */

import Clients.SETA.RideInfo;
import Schemes.TaxiSchema;
import com.google.gson.Gson;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import jakarta.ws.rs.client.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.paho.client.mqttv3.*;
import org.example.grpc.BidirectionalMsgServiceGrpc;
import org.example.grpc.BidirectionalMsgServiceOuterClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class Taxi {
    private final static String ADMIN_SERVER_ADDR = "localhost";
    private final static int ADMIN_SERVER_PORT = 9001;
    private final static String ADMIN_SERVER_URL = "http://" + ADMIN_SERVER_ADDR + ":" + ADMIN_SERVER_PORT;
    private final static Gson gson = new Gson();
    private final static String broker = "tcp://localhost:1883";

    public static void main(String[] args) throws MqttException {
        Client client = ClientBuilder.newClient();

        // Port on which the gRPC communication between taxis will be performed
        int grpcPort = 3005;

        // Initialize the taxi on the administrator server through POST, it will return the valid infos
        TaxiSchema taxiSchema = postInit(client, grpcPort);
        TaxiInfo thisTaxi = taxiSchema.getTaxiInfo();
        thisTaxi.setBattery(100.0);
        AtomicReference<ArrayList<TaxiInfo>> taxis = new AtomicReference<>(taxiSchema.getTaxis());
        // Keep updating the taxi list through a thread performing POSTs
        updatesTaxisFromAdminServer(client, thisTaxi, taxis);
        // Print the initial information of the taxi process
        printFormattedInfos(thisTaxi, taxis.get());

        // Creating the client for the MQTT broker
        String clientId = MqttClient.generateClientId();
        MqttClient mqttClient = new MqttClient(broker, clientId, null);
        // Using the MQTT client for seeking rides on the relative assigned district of the taxi
        seekingRides(mqttClient, thisTaxi);

        // closingMqttConnection(mqttClient); // Utility function will be helpful when from console i want to quit the taxi

        // Debug
        /*while (true) {
            try {
                printFormattedInfos(thisTaxi, taxis.get());
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }*/

        // TODO: Inizia l'acquisizione dei dati dal sensore
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
    private static void seekingRides(MqttClient mqttClient, TaxiInfo thisTaxi) throws MqttException {
        String topic = "seta/smartcity/rides/district" + thisTaxi.getDistrict();
        int qos = 2;
        System.out.println("Listening on topic: " + topic);

        final ManagedChannel channel = ManagedChannelBuilder
                .forTarget(ADMIN_SERVER_ADDR + ":" + thisTaxi.getGrpcPort())
                .usePlaintext().build();

        //creating the synchronous blocking stub
        BidirectionalMsgServiceGrpc.BidirectionalMsgServiceBlockingStub stub =
                BidirectionalMsgServiceGrpc.newBlockingStub(channel);

        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(true);
        mqttClient.connect(connectOptions);


        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String ride = new String(message.getPayload());
                System.out.println(ride);
                RideInfo rideInfo = gson.fromJson(ride, RideInfo.class);

                //TODO: Algoritmo decentralizzato da accordare attraverso le gRPC

                // You enter this scope only if this taxi got the priority for taking the ride
                taxiRidesOwnership(mqttClient, channel, stub, thisTaxi, rideInfo);
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

        mqttClient.subscribe(topic, qos);

    }

    /*
     * Coordination of taxis for handling the ownership of the rides in the smart city
     * -------------------------------------------------------------------------------
     * TODO: complete the comment
     *
     */
    private static void taxiRidesOwnership(MqttClient mqttClient, ManagedChannel channel,
                                           BidirectionalMsgServiceGrpc.BidirectionalMsgServiceBlockingStub stub,
                                           TaxiInfo thisTaxi, RideInfo ride) {
        //the stub returns a stream to communicate with the server.
        //the argument is the stream of messages which are transmitted by the server.
        double taxiToRideDistance = euclideanDistance(thisTaxi.getPosition(), ride.getStartPosition());

        RideRequest rideRequest =
                new RideRequest(thisTaxi.getId(), ride.getId(), ride.getStartingDistrict(), taxiToRideDistance, thisTaxi.getBattery());

        String body = gson.toJson(rideRequest);

        BidirectionalMsgServiceOuterClass.ClientRequest clientRequest =
                BidirectionalMsgServiceOuterClass.ClientRequest.newBuilder().setStringRequest(body).build();

        // Ask for taking the ride only if the taxi is not recharging or already in charge of a ride
        boolean notRechargingAndRiding = !thisTaxi.isRecharging() && !thisTaxi.isRiding();
        if (notRechargingAndRiding) {
            // Synchronous call
            BidirectionalMsgServiceOuterClass.ServerResponse serverResponse = stub.rideProposal(clientRequest);
            channel.shutdown();

            RideRequest ans = gson.fromJson(serverResponse.getStringResponse(), RideRequest.class);

            System.out.println("[Server Response]: " + serverResponse.getStringResponse());
            System.out.println("The taxi which will take charge of the ride " + ans.getRideId()
                    + "will be the  " + ans.getTaxiId());

            boolean isThisTaxi = (ans.getTaxiId() == thisTaxi.getId());

            if (isThisTaxi) {
                try {
                    System.out.println("This taxi took the ride " + ride.getId());
                    executeRide(mqttClient, thisTaxi, ride);
                } catch (InterruptedException | MqttException e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println("This taxi didn't took the ride " + ride.getId());
            }

        } else {
            System.out.println("This taxi is recharging or in charge of a ride.");
        }

        try {
            //you need this. otherwise the method will terminate before that answers from the server are received
            channel.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    // TODO: al completamento della corsa aggiornare il distretto dell'oggetto this taxi in maniera che si
    // metta in ascolto su topic corretto.
    private static void executeRide(MqttClient mqttClient, TaxiInfo thisTaxi, RideInfo rideInfo) throws InterruptedException, MqttException {
        // TODO: Ricordarsi il consumo di batteria dei taxi - Requirement from the assignment paper
        final int DELIVERY_TIME = 5000;
        thisTaxi.setRiding(true);

        System.out.println("The taxi " + thisTaxi.getId()
                + " is moving from " + Arrays.toString(thisTaxi.getPosition())
                + " to " + Arrays.toString(rideInfo.getStartPosition()) + ", then to "
                + Arrays.toString(rideInfo.getDestinationPosition()));
        Thread.sleep(DELIVERY_TIME);

        // Set the opportune battery level after the ride
        double fromTaxiToStart = euclideanDistance(thisTaxi.getPosition(), rideInfo.getStartPosition());
        double fromStartToEnd = euclideanDistance(rideInfo.getStartPosition(), rideInfo.getDestinationPosition());
        double totalKm = fromTaxiToStart + fromStartToEnd;
        thisTaxi.setBattery(thisTaxi.getBattery() - totalKm);

        // If the destination is on a different district the taxi should be subscribed to the correct topic
        if (thisTaxi.getDistrict() != rideInfo.getDestinationDistrict()) {
            String base = "seta/smartcity/rides/district";
            mqttClient.unsubscribe(base + thisTaxi.getDistrict());
            mqttClient.subscribe(base + rideInfo.getDestinationDistrict());
            thisTaxi.setDistrict(rideInfo.getDestinationDistrict());
        }

        // Set the new position of the taxi
        thisTaxi.setPosition(rideInfo.getDestinationPosition());
        // Ride has ended
        thisTaxi.setRiding(false);
        printFormattedInfos(thisTaxi);
    }

    /*
     * Continuously update the taxi list of this process through an HTTP POST request
     * -------------------------------------------------------------------------------
     * Generate a thread that will keep asking for the list of other taxis on the
     * administrator server through an HTTP POST request.
     */
    private static void updatesTaxisFromAdminServer(Client client, TaxiInfo thisTaxi, AtomicReference<ArrayList<TaxiInfo>> taxis) {
        Thread updatesFromServer = new Thread(() -> {
            while (true) {
                taxis.set(getTaxisOnServer(client, thisTaxi));
            }
        });
        updatesFromServer.start();
    }

    /*
     * Initialization of the Taxi process through the administrator server
     * ------------------------------------------------------------------------------
     * The taxi sends his generated data to the administrator server, in which
     * there is a proposal ID. This will be checked from server side and in the case
     * it is already taken (not available) the server will return a valid ID.
     *
     * The server answer will contain the initial position of the taxi which is one of the
     * four recharge stations in the smart city, this will depend on the random assignment
     * of the district.
     */
    private static TaxiSchema postInit(Client client, int grpcPort) {
        // Send the taxi initialization request with a tentative random ID
        final String INIT_PATH = "/taxi-init";

        TaxiInfo initInfo = new TaxiInfo(generateRndID(), grpcPort, ADMIN_SERVER_ADDR);
        // Receive the initialization data from the server: valid ID, position, list of other taxis

        String serverInitInfos = postRequest(client, ADMIN_SERVER_URL + INIT_PATH, gson.toJson(initInfo));

        TaxiSchema info = gson.fromJson(serverInitInfos, TaxiSchema.class);
        return info;
    }

    /*
     * Return the view of the other taxis from the point of view of the requester (this process/taxi)
     * ----------------------------------------------------------------------------------------------
     * Essentially will perform an HTTP POST request to the "/get-taxis" exposed REST API, which will
     * return the list of all the other taxis present on the administrator server minus the taxi
     * which is making the request ('thisTaxi' variable).
     */
    private static ArrayList<TaxiInfo> getTaxisOnServer(Client client, TaxiInfo thisTaxi) {
        final String GET_PATH = "/get-taxis";

        String serverResponse = postRequest(client, ADMIN_SERVER_URL + GET_PATH, gson.toJson(thisTaxi));
        TaxiSchema ans = gson.fromJson(serverResponse, TaxiSchema.class);

        ArrayList<TaxiInfo> taxis = ans.getTaxis();

        return taxis;
    }

    /// Utility
    // Calculate the Euclidean distance between two points
    public static double euclideanDistance(int[] start, int[] end) {
        double xOffset = Math.pow((end[0] - start[1]), 2);
        double yOffset = Math.pow((end[1] - start[0]), 2);
        return Math.sqrt(xOffset + yOffset);
    }

    // Perform an HTTP POST request given the url and the body as json
    public static String postRequest(Client client, String url, String jsonBody) {
        WebTarget webTarget = client.target(url);

        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = builder.post(Entity.json(jsonBody));
        response.bufferEntity();

        String responseJson = null;
        try {
            responseJson = response.readEntity(String.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return responseJson;
    }

    // Perform an HTTP GET request given the url and the body as json
    public static String getRequest(Client client, String url) {
        WebTarget webTarget = client.target(url);

        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = builder.get();
        response.bufferEntity();

        String responseJson = null;
        try {
            responseJson = response.readEntity(String.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return responseJson;
    }

    // Close the MQTT connection of this client toward the broker
    private static void closingMqttConnection(MqttClient mqttClient) throws MqttException {
        if (false) {
            mqttClient.disconnect();
        }
    }

    // Generate a random ID for the taxi process
    private static int generateRndID() {
        Random random = new Random();
        return random.nextInt(1, 100 + 1);
    }

    // Print the given information of the taxi
    private static void printFormattedInfos(TaxiInfo initInfo, ArrayList<TaxiInfo> taxis) {
        String infos = "[" + initInfo.toString() + ", taxis=[";

        infos += "[";
        if (!taxis.isEmpty()) {
            for (TaxiInfo e : taxis)
                infos += "id=" + e.getId() + ",";
        }

        if (infos.endsWith(",")) {
            infos = infos.substring(0, infos.length() - 1);
        }
        infos += "]]";

        System.out.println(infos);
    }

    private static void printFormattedInfos(TaxiInfo initInfo) {
        String infos = "[" + initInfo.toString() + "]";
        System.out.println(infos);
    }

}