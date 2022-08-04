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
import org.example.grpc.BidirectionalServiceGrpc;
import org.example.grpc.BidirectionalServiceOuterClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

// Taxis are considered as single processes and not as threads
public class Taxi {
    private final static String ADMIN_SERVER_ADDR = "localhost";
    private final static int ADMIN_SERVER_PORT = 9001;
    private final static String ADMIN_SERVER_URL = "http://" + ADMIN_SERVER_ADDR + ":" + ADMIN_SERVER_PORT;
    private final static Gson gson = new Gson();
    private final static String broker = "tcp://localhost:1883";

    public static void main(String[] args) throws MqttException {
        Client client = ClientBuilder.newClient();

        int grpcPort = 3005;

        TaxiSchema taxiSchema = postInit(client, grpcPort);

        TaxiInfo thisTaxi = taxiSchema.getTaxiInfo();
        thisTaxi.setBattery(100.0);
        AtomicReference<ArrayList<TaxiInfo>> taxis = new AtomicReference<>(taxiSchema.getTaxis());

        updatesTaxisFromAdminServer(client, thisTaxi, taxis);

        printFormattedInfos(thisTaxi, taxis.get());

        // TODO: Iscrizione al topic MQTT del proprio distretto
        String clientId = MqttClient.generateClientId();
        MqttClient mqttClient = new MqttClient(broker, clientId, null);


        seekingRides(mqttClient, thisTaxi);

        // closingMqttConnection(mqttClient); // Utility function will be helpful when from console i want to quit the taxi


        // Debug
        while (true) {
            try {
                printFormattedInfos(thisTaxi, taxis.get());
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // TODO: Inizia l'acquisizione dei dati dal sensore
    }


    /*  Seeks for rides by connecting it to the MQTT broker and subscribing on the
        topic of the district. */
    private static MqttClient seekingRides(MqttClient mqttClient, TaxiInfo thisTaxi) throws MqttException {
        String topic = "seta/smartcity/rides/district" + thisTaxi.getDistrict();
        int qos = 2;
        System.out.println("Listening on topic: " + topic);

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
                //System.out.println(ride);
                RideInfo rideInfo = gson.fromJson(ride, RideInfo.class);

                //TODO: Algoritmo decentralizzato da accordare attraverso le gRPC

                // You enter this scope only if this taxi got the priority for taking the ride
                grpcRidesHandling(thisTaxi, rideInfo);
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

        return mqttClient;
    }

    private static void grpcRidesHandling(TaxiInfo thisTaxi, RideInfo ride) {
        final ManagedChannel channel = ManagedChannelBuilder
                .forTarget(ADMIN_SERVER_ADDR + ":" + thisTaxi.getGrpcPort())
                .usePlaintext().build();

        //creating the asynchronous stub
        BidirectionalServiceGrpc.BidirectionalServiceStub stub = BidirectionalServiceGrpc.newStub(channel);

        //the stub returns a stream to communicate with the server.
        //the argument is the stream of messages which are transmitted by the server.
        StreamObserver<BidirectionalServiceOuterClass.ClientRequest> serverStream =
                stub.bidirectional(new StreamObserver<>() {
                    //remember: all the methods here are CALLBACKS which are handled in an asynchronous manner.
                    //we define what to do when a message from the server arrives (just print the message)
                    public void onNext(BidirectionalServiceOuterClass.ServerResponse serverResponse) {
                        System.out.println("[FROM SERVER] " + serverResponse.getStringResponse());
                    }

                    public void onError(Throwable throwable) {
                    }

                    public void onCompleted() {
                    }
                });

        int[] taxiPosition = thisTaxi.getPosition();
        int[] passengerPosition = ride.getStartPosition();
        double xOffset = Math.pow((passengerPosition[0] - taxiPosition[1]), 2);
        double yOffset = Math.pow((passengerPosition[1] - taxiPosition[0]), 2);
        double euclideanDistance = Math.sqrt(xOffset + yOffset);

        RideRequest rideRequest = new RideRequest(thisTaxi.getId(), ride.getId(), euclideanDistance);

        String body = gson.toJson(rideRequest);

        System.out.println("Sending the message '" + body + "' to the server...");
        BidirectionalServiceOuterClass.ClientRequest.Builder build = BidirectionalServiceOuterClass.ClientRequest.newBuilder();
        serverStream.onNext(build.setStringRequest(body).build());

        RideRequest ans = gson.fromJson(build.getStringRequest(), RideRequest.class);

        System.out.println("Taxi che si prenderà la corsa: " + ans.toString());

        if (ans.getTaxiId() == thisTaxi.getId()) {
            if (!thisTaxi.isRecharging() && !thisTaxi.isRiding()) {
                try {
                    executeRide(thisTaxi, ride);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
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
    private static void executeRide(TaxiInfo thisTaxi, RideInfo rideInfo) throws InterruptedException {
        // TODO: Ricordarsi il consumo di batteria dei taxi - Requirement from the assignment paper
        final int DELIVERY_TIME = 5000;
        thisTaxi.setRiding(true);

        System.out.println("The taxi " + thisTaxi.getId()
                + " is moving from " + Arrays.toString(thisTaxi.getPosition())
                + " to " + Arrays.toString(rideInfo.getStartPosition()) + ", then to "
                + Arrays.toString(rideInfo.getDestinationPosition()));
        Thread.sleep(DELIVERY_TIME);

        double fromTaxiToStart = euclideanDistance(thisTaxi.getPosition(), rideInfo.getStartPosition());
        double fromStartToEnd = euclideanDistance(rideInfo.getStartPosition(), rideInfo.getDestinationPosition());
        double totalKm = fromTaxiToStart + fromStartToEnd;
        thisTaxi.setBattery(thisTaxi.getBattery() - totalKm);

        thisTaxi.setDistrict(rideInfo.getDestinationDistrict());
        thisTaxi.setPosition(rideInfo.getDestinationPosition());

        thisTaxi.setRiding(false);
    }

    private static void updatesTaxisFromAdminServer(Client client, TaxiInfo thisTaxi, AtomicReference<ArrayList<TaxiInfo>> taxis) {
        Thread updatesFromServer = new Thread(() -> {
            while (true) {
                taxis.set(getTaxisOnServer(client, thisTaxi));
            }
        });
        updatesFromServer.start();
    }

    /*  Initialization of the Clients.Taxi.Taxi through the administrator server.
        The taxi sends his sensible data to the administrator server, in this
        data there is the proposal of an ID. This will be checked from the server side
        if it is available or already taken, in the second case the server will return
        a valid ID.

        The server answer will contain the initial position of the taxi which is one of the
        four recharge stations in the smart city, this will depend from the random assignment
        of the district. */
    private static TaxiSchema postInit(Client client, int grpcPort) {
        // Send the taxi initialization request with a tentative random ID
        final String INIT_PATH = "/taxi-init";

        TaxiInfo initInfo = new TaxiInfo(generateRndID(), grpcPort, ADMIN_SERVER_ADDR);
        // Receive the initialization data from the server: valid ID, position, list of other taxis

        String serverInitInfos = postRequest(client, ADMIN_SERVER_URL + INIT_PATH, gson.toJson(initInfo));

        TaxiSchema info = gson.fromJson(serverInitInfos, TaxiSchema.class);
        return info;
    }

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

}