/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. of Computer Science @UNIMI A.Y. 2021/2022 */
package Clients.Taxi;

import Clients.SETA.RideInfo;
import Clients.SETA.Status;
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
import org.example.grpc.IPC;
import org.example.grpc.IPCServiceGrpc;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Taxi extends IPCServiceGrpc.IPCServiceImplBase {
    // Connection data
    private final static String ADMIN_SERVER_ADDRESS = "localhost";
    private final static int ADMIN_SERVER_PORT = 9001;
    private final static String ADMIN_SERVER_URL = "http://" + ADMIN_SERVER_ADDRESS + ":" + ADMIN_SERVER_PORT;
    private final static String MQTT_BROKER_URL = "tcp://localhost:1883";
    private final static Gson GSON = new Gson();

    // Taxi Data
    private static int grpcPort;
    private static Client client;
    private static double battery = 100.0;
    private static int id = -1;
    private static int district = -1;
    private static int[] position = new int[2];
    private static boolean isRecharging = false;
    private static boolean isRiding = false;
    private static ArrayList<TaxiInfo> taxis = new ArrayList<>();

    // MQTT Subscriber
    private static final String clientID = MqttClient.generateClientId();
    private static MqttClient mqttClient = null;

    public static void main(String[] args) {
        // Initialize the taxi on the administrator server through POST, it will return the valid infos
        postInit();
        printInfos();

        // Standard input for console commands
        Scanner scanner = new Scanner(System.in);
        CLI cli = new CLI(id, client, ADMIN_SERVER_URL, scanner);
        cli.start();

        // Init the gRPC for the P2P communication between taxis
        GrpcServer grpcServer = new GrpcServer(grpcPort);
        grpcServer.start();

        if (taxis.size() > 0) {
            presentToOthers();
        }

        // Creating the client for the MQTT broker with no persistence of the messages
        // Using the MQTT client for seeking rides on the relative assigned district of the taxi
        startMqttClient();

        //closingMqttConnection(mqttClient); // Utility function will be helpful when from console i want to quit the taxi

        // TODO: Inizia l'acquisizione dei dati dal sensore
    }

    private static void printInfos() {
        Thread printInfos = new Thread(() -> {

        });
        printInfos.start();
    }

    @Override
    public void compareDistances(IPC.RideCharge request, StreamObserver<IPC.ACK> responseObserver) {
        // Check the distance between the server taxi and the received distance
        int[] passengerPos = new int[2];
        passengerPos[0] = request.getPassengerPosition(0);
        passengerPos[1] = request.getPassengerPosition(1);

        final double clientDistance = request.getDistanceToTaxi();
        final double serverDistance = euclideanDistance(position, passengerPos);

        if (isRecharging || isRiding) {
            // This taxi is recharging or on a ride
            System.out.println("This taxi is recharging or on a ride, sends ACK to " + request.getTaxi().getId());
            responseObserver.onNext(IPC.ACK.newBuilder().setVote(true).build());
            responseObserver.onCompleted();
            return;
        }

        if (serverDistance > clientDistance) {
            // The client taxi has a smaller distance to the passenger than this, ACK
            responseObserver.onNext(IPC.ACK.newBuilder().setVote(true).build());
            responseObserver.onCompleted();
            return;
        } else if (serverDistance == clientDistance) {
            if (battery < request.getTaxi().getBattery()) {
                // Requester has more battery than this, ACK
                responseObserver.onNext(IPC.ACK.newBuilder().setVote(true).build());
                responseObserver.onCompleted();
                return;
            } else if (battery == request.getTaxi().getBattery()) {
                if (id < request.getTaxi().getId()) {
                    // The requesting taxi has a greater ID than this taxi, ACK
                    responseObserver.onNext(IPC.ACK.newBuilder().setVote(true).build());
                    responseObserver.onCompleted();
                    return;
                }
            }
        }

        // This taxi has a better distance, battery level or ID value than the requester, sends NACK to him
        responseObserver.onNext(IPC.ACK.newBuilder().setVote(false).build());
        responseObserver.onCompleted();
    }

    @Override
    public void changedPosition(IPC.Infos request, StreamObserver<IPC.ACK> responseObserver) {
        for (TaxiInfo t : taxis) {
            if (t.getId() == request.getId()) {
                int[] pos = new int[2];
                pos[0] = request.getPosition(0);
                pos[1] = request.getPosition(1);
                t.setPosition(pos);
                t.setDistrict(request.getDistrict());
                t.setBattery(request.getBattery());
                t.setRecharging(request.getIsRecharging());
                t.setRiding(request.getIsRiding());
            }
        }
        System.out.println("Saved the new position of taxi " + request.getId());
        responseObserver.onNext(IPC.ACK.newBuilder().setVote(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void present(IPC.Infos request, StreamObserver<IPC.Response> responseObserver) {
        int[] pos = new int[2];
        pos[0] = request.getPosition(0);
        pos[1] = request.getPosition(1);

        TaxiInfo info = new TaxiInfo();
        info.setId(request.getId());
        info.setGrpcPort(request.getGrpcPort());
        info.setDistrict(request.getDistrict());
        info.setPosition(pos);
        info.setRecharging(request.getIsRecharging());
        info.setRiding(request.getIsRiding());
        info.setBattery(request.getBattery());

        if (taxis.isEmpty() || !taxis.contains(info)) {
            taxis.add(info);

            responseObserver.onNext(IPC.Response.newBuilder()
                    .setStringResponse("ACK, your taxi data has been received and saved from taxi: "
                            + id
                            + " through gRPC port: "
                            + grpcPort)
                    .build());
            responseObserver.onCompleted();
        } else {
            responseObserver.onNext(IPC.Response.newBuilder()
                    .setStringResponse("The taxi data were already present in the list, discarded.").build());
            responseObserver.onCompleted();
        }
    }

    /*
     * Send a presentation msg to other taxis through gRPC
     * --------------------------------------------------------------
     * This function sends to each previously registered taxi in the
     * system (received from the initialization POST of the
     * administrator server) its own individual data.
     *
     * This will make possible to create future bidirectional
     * communication with the other taxis because now they will know
     * the gRPC port of the new taxi.
     */
    private static void presentToOthers() {
        IPC.Infos presentMsg = IPC.Infos.newBuilder()
                .setId(id)
                .setDistrict(district)
                .setGrpcPort(grpcPort)
                .addPosition(position[0])
                .addPosition(position[1])
                .setIsRecharging(isRecharging)
                .setIsRiding(isRiding)
                .setBattery(battery)
                .build();

        for (TaxiInfo t : taxis) {
            ManagedChannel channel = ManagedChannelBuilder
                    .forTarget(ADMIN_SERVER_ADDRESS + ":" + t.getGrpcPort())
                    .usePlaintext().build();
            IPCServiceGrpc.IPCServiceBlockingStub stub = IPCServiceGrpc.newBlockingStub(channel);
            IPC.Response response = stub.present(presentMsg);
            //todo: debug //System.out.println("Response: " + response.getStringResponse());
            channel.shutdown();
        }
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
    private synchronized static void startMqttClient() {
        try {
            mqttClient = new MqttClient(MQTT_BROKER_URL, clientID, null);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }

        String topic = "seta/smartcity/rides/district" + district;
        int qos = 2;
        System.out.println("Subscribed on topic: " + topic);

        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(true);

        try {
            mqttClient.connect(connectOptions);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }

        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public synchronized void messageArrived(String topic, MqttMessage message) throws Exception {
                String ride = new String(message.getPayload());
                RideInfo rideInfo = GSON.fromJson(ride, RideInfo.class);

                if (district == rideInfo.getStartingDistrict()) {
                    System.out.println("MQTT District " + district + " ride communication: " + rideInfo);

                    if (!isRiding && !isRecharging) {
                        if (rideInfo.getStatus() == Status.FREE) {
                            if (!taxis.isEmpty()) {
                                int[] passengerPosition = rideInfo.getStartPosition();
                                double clientDistance = euclideanDistance(position, passengerPosition);
                                // todo: Should consider only free taxis (no recharging or in a ride)
                                final int taxisInDistrict = getNumberOfTaxisInDistrict();
                                System.out.println("Waiting for " + taxisInDistrict + " ACK votes");
                                final int ackVotes = compareDistancesGrpc(clientDistance, rideInfo.getStartPosition());
                                System.out.println("Received a total of " + ackVotes + " ACKs");

                                if (ackVotes == taxisInDistrict) {
                                    System.out.println("I got the ownership for the ride " + rideInfo.getId());
                                    rideInfo.setStatus(Status.BUSY);
                                    performRide(rideInfo);
                                } else {
                                    // The ride has been taken from someone else
                                    // It should wait of information in case of new position of the taxi
                                    Thread.sleep(1000);
                                    System.out.println("I didn't got the ownership for the ride " + rideInfo.getId());
                                }
                            } else {
                                rideInfo.setStatus(Status.BUSY);
                                System.out.println("I got the ownership for the ride " + rideInfo.getId());
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

        try {
            mqttClient.subscribe(topic, qos);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    private static void performRide(RideInfo rideInfo) throws InterruptedException, MqttException {
        final int DELIVERY_TIME = 5000;

        isRiding = true;
        double distToPassenger = euclideanDistance(position, rideInfo.getStartPosition());
        double rideDistance = euclideanDistance(rideInfo.getStartPosition(), rideInfo.getDestinationPosition());
        double totalKm = distToPassenger + rideDistance;

        if (district != rideInfo.getDestinationDistrict()) {
            String topic = "seta/smartcity/rides/district" + district;
            mqttClient.unsubscribe(topic);
            System.out.println("Unsubscribed on topic: " + topic);

            district = rideInfo.getDestinationDistrict();
            topic = "seta/smartcity/rides/district" + district;

            mqttClient.subscribe(topic);
            System.out.println("Subscribed on topic: " + topic);
        }

        position[0] = rideInfo.getDestinationPosition()[0];
        position[1] = rideInfo.getDestinationPosition()[1];

        communicateNewPosition();

        Thread.sleep(DELIVERY_TIME);
        battery -= totalKm;
        System.out.printf("I reached the destination and after %,.2f Km the battery levels are %,.2f %%\n", totalKm, battery);

        isRiding = false;
    }

    private static void communicateNewPosition() {
        IPC.Infos presentMsg = IPC.Infos.newBuilder()
                .setId(id)
                .setDistrict(district)
                .setGrpcPort(grpcPort)
                .addPosition(position[0])
                .addPosition(position[1])
                .setIsRecharging(isRecharging)
                .setIsRiding(isRiding)
                .setBattery(battery)
                .build();

        for (TaxiInfo t : taxis) {
            ManagedChannel channel = ManagedChannelBuilder
                    .forTarget(ADMIN_SERVER_ADDRESS + ":" + t.getGrpcPort())
                    .usePlaintext().build();
            IPCServiceGrpc.IPCServiceBlockingStub stub = IPCServiceGrpc.newBlockingStub(channel);
            IPC.ACK ack = stub.changedPosition(presentMsg);

            if (ack.getVote())
                System.out.println(t.getId() + " ACKed my new position");

            channel.shutdown();
        }
    }

    private static IPC.Infos getIPCInfos() {
        return IPC.Infos.newBuilder()
                .setId(id)
                .setDistrict(district)
                .setGrpcPort(grpcPort)
                .addPosition(position[0])
                .addPosition(position[1])
                .setIsRecharging(isRecharging)
                .setIsRiding(isRiding)
                .setBattery(battery)
                .build();
    }

    /*
     *
     */
    private static int compareDistancesGrpc(double distToStart, int[] startPosition) throws InterruptedException {
        IPC.RideCharge proposal = IPC.RideCharge.newBuilder()
                .setTaxi(getIPCInfos())
                .addPassengerPosition(startPosition[0])
                .addPassengerPosition(startPosition[1])
                .setDistanceToTaxi(distToStart)
                .build();

        int ackCounter = 0;

        // Send the proposal only to the other taxis in the same district
        for (TaxiInfo t : taxis) {
            if (t.getDistrict() == district) {
                String target = ADMIN_SERVER_ADDRESS + ":" + t.getGrpcPort();
                ManagedChannel channel = ManagedChannelBuilder
                        .forTarget(target)
                        .usePlaintext()
                        .build();
                //todo //System.out.println("Sending my distance to " + target);
                IPCServiceGrpc.IPCServiceBlockingStub stub = IPCServiceGrpc.newBlockingStub(channel);
                IPC.ACK ack = stub.compareDistances(proposal);
                /* If the client taxi has a smaller distance then another server taxi
                 * it will receive its own data back (as ACK vote for consensus) */
                if (ack.getVote()) {
                    ackCounter++;
                    System.out.println("Received an ACK from " + t.getId());
                } else
                    System.out.println("Received a NACK from " + t.getId());

                channel.shutdown();
            }
        }
        Thread.sleep(500);
        return ackCounter;
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
    private static void postInit() {
        Random random = new Random();
        // Port on which the gRPC communication between taxis will be performed
        grpcPort = random.nextInt(0, 65535);
        client = ClientBuilder.newClient();

        // Send the taxi initialization request with a tentative random ID
        final String INIT_PATH = "/taxi-init";

        TaxiInfo initInfo = new TaxiInfo(generateRndID(), grpcPort, ADMIN_SERVER_ADDRESS);
        // Receive the initialization data from the server: valid ID, position, list of other taxis

        String serverInitInfos = postRequest(client, ADMIN_SERVER_URL + INIT_PATH, GSON.toJson(initInfo));

        TaxiSchema taxiSchema = GSON.fromJson(serverInitInfos, TaxiSchema.class);
        TaxiInfo taxiInfo = taxiSchema.getTaxiInfo();
        id = taxiInfo.getId();
        grpcPort = taxiInfo.getGrpcPort();
        position = taxiInfo.getPosition();
        district = taxiInfo.getDistrict();
        taxis = taxiSchema.getTaxis();

        System.out.println(taxiInfo);
    }

    /// Utility
    // Return the number of taxis in the same district of the current taxi process
    private static int getNumberOfTaxisInDistrict() {
        int counter = 0;
        for (TaxiInfo t : taxis)
            if (t.getDistrict() == district)
                counter++;
        return counter;
    }

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
}