/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. of Computer Science @UNIMI A.Y. 2021/2022 */
package Client;

import SETA.RideInfo;

import Utility.Utility;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import io.grpc.stub.StreamObserver;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.client.Client;

import Schemes.TaxiSchema;
import org.eclipse.paho.client.mqttv3.*;
import org.example.grpc.IPC;
import org.example.grpc.IPCServiceGrpc;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static Utility.Utility.GSON;

public class Taxi extends IPCServiceGrpc.IPCServiceImplBase {
    private final static String ADMIN_SERVER_ADDRESS = "localhost";
    private final static int ADMIN_SERVER_PORT = 9001;
    private final static String ADMIN_SERVER_URL = "http://" + ADMIN_SERVER_ADDRESS + ":" + ADMIN_SERVER_PORT;
    private final static String MQTT_BROKER_URL = "tcp://localhost:1883";

    // Taxi Data
    private static final TaxiInfo thisTaxi = new TaxiInfo();
    private static Client client;
    private static ArrayList<TaxiInfo> otherTaxis = new ArrayList<>();

    // MQTT Subscriber
    private static final String clientID = MqttClient.generateClientId();
    private static MqttClient mqttClient = null;


    // Logical Clock
    private static long logicalClock = System.currentTimeMillis();
    private static int ackCounter = 0;
    private final static long LOGICAL_OFFSET = Utility.generateRndLong(0, 15);

    @Override
    public StreamObserver<IPC.RideCharge> coordinateRideStream(StreamObserver<IPC.ACK> responseObserver) {

        return new StreamObserver<>() {
            @Override
            public void onNext(IPC.RideCharge request) {
                /*System.out.println(request.getTaxi().getId() +
                        " sent the request at " + request.getLogicalClock());
                if (logicalClock <= request.getLogicalClock()) {
                    logicalClock = request.getLogicalClock();
                    logicalClock++;
                }
                System.out.println(thisTaxi.getId() + " received the request at " + request.getLogicalClock());
*/
                // From Client Stream
                // Check the distance between the server taxi and the received distance
                int[] passengerPos = new int[2];
                passengerPos[0] = request.getDestinationPosition(0);
                passengerPos[1] = request.getDestinationPosition(1);

                final double clientDistance = request.getDistanceToDestination();
                final double serverDistance = Utility.euclideanDistance(thisTaxi.getPosition(), passengerPos);

                if (!request.getRechargingRide()) {
                    if (thisTaxi.isRecharging() || thisTaxi.isRiding()) {
                        // This taxi is recharging or on a ride
                        System.out.println("This taxi is recharging or on a ride, sends ACK to "
                                + request.getTaxi().getId());
                        responseObserver.onNext(IPC.ACK.newBuilder().setVote(true).build());
                        responseObserver.onCompleted();
                        return;
                    }
                }

                if (serverDistance > clientDistance) {
                    // The client taxi has a smaller distance to the passenger than this, ACK
                    responseObserver.onNext(IPC.ACK.newBuilder().setVote(true).build());
                    responseObserver.onCompleted();
                    return;
                } else if (serverDistance == clientDistance) {
                    if (thisTaxi.getBattery() < request.getTaxi().getBattery()) {
                        // Requester has more battery than this, ACK
                        responseObserver.onNext(IPC.ACK.newBuilder().setVote(true).build());
                        responseObserver.onCompleted();
                        return;
                    } else if (thisTaxi.getBattery() == request.getTaxi().getBattery()) {
                        if (thisTaxi.getId() < request.getTaxi().getId()) {
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
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        };
    }

    @Override
    public StreamObserver<IPC.Infos> changedPositionStream(StreamObserver<IPC.ACK> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(IPC.Infos request) {
                for (TaxiInfo t : otherTaxis) {
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
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
            }
        };
    }

    @Override
    public void present(IPC.Infos request, StreamObserver<IPC.ACK> responseObserver) {
        TaxiInfo clientTaxi = new TaxiInfo(request);
        final boolean taxiIsNew = otherTaxis.isEmpty() || !otherTaxis.contains(clientTaxi);

        if (taxiIsNew) {
            otherTaxis.add(clientTaxi);
            responseObserver.onNext(IPC.ACK.newBuilder().setVote(true).build());
        } else {
            responseObserver.onNext(IPC.ACK.newBuilder().setVote(false).build());
        }
        responseObserver.onCompleted();
    }

    /*
     * Send a presentation msg to other taxis through gRPC
     * ------------------------------------------------------------------------
     * This function sends to each previously registered taxi in the
     * system (received from the initialization POST of the
     * administrator server) its own individual data.
     *
     * This will make possible to create future bidirectional
     * communication with the other taxis because now they will know
     * the gRPC port of the new taxi.
     */
    private static void presentToOtherTaxis() {
        if (otherTaxis.size() > 0) {
            presentToOthers();
            int taxisInSmartCity = otherTaxis.size();

            if (ackCounter == taxisInSmartCity)
                System.out.println("Taxi " +
                        thisTaxi.getId() +
                        " has been presented correctly to all the other taxis of the smart city");
            else
                System.out.println("Taxi " +
                        thisTaxi.getId() +
                        " encountered an error during the presentation phase");

            ackCounter = 0;
        }
    }

    private static void presentToOthers() {
        IPC.Infos presentMsg = getIPCInfos();

        for (TaxiInfo t : otherTaxis) {
            ManagedChannel channel = ManagedChannelBuilder
                    .forTarget(ADMIN_SERVER_ADDRESS + ":" + t.getGrpcPort())
                    .usePlaintext().build();
            IPCServiceGrpc.IPCServiceBlockingStub stub = IPCServiceGrpc.newBlockingStub(channel);
            IPC.ACK ans = stub.present(presentMsg);

            if (ans.getVote()) ackCounter++;
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

    private static void connectToMqttBroker(MqttConnectOptions connectOptions) {
        try {
            mqttClient.connect(connectOptions);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    private static void subscribeTopic(String topic, int qos) {
        try {
            mqttClient.subscribe(topic, qos);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setMqttCallback() {
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String ride = new String(message.getPayload());
                RideInfo rideInfo = GSON.fromJson(ride, RideInfo.class);

                final boolean sameDistrictOfRide = thisTaxi.getDistrict() == rideInfo.getStartingDistrict();

                if (sameDistrictOfRide) {
                    System.out.println("MQTT District " + thisTaxi.getDistrict() + " ride communication: " + rideInfo);

                    final boolean thisTaxiIsFree = !thisTaxi.isRiding() && !thisTaxi.isRecharging();

                    if (thisTaxiIsFree) {
                        int[] passengerPosition = rideInfo.getStartPosition();
                        double clientDistance = Utility.euclideanDistance(thisTaxi.getPosition(), passengerPosition);

                        int availableTaxisInDistrict = getAvailableTaxisInDistrict();

                        System.out.println("Waiting for " + availableTaxisInDistrict + " ACK votes");
                        coordinateRideGrpcStream(clientDistance, rideInfo.getStartPosition(), false);
                        System.out.println("Received a total of " + ackCounter + " ACKs");

                        if (ackCounter == availableTaxisInDistrict) {
                            System.out.println("I got the ownership for the ride " + rideInfo.getId());
                            ackCounter = 0;
                            performRide(rideInfo);
                        } else {
                            // The ride has been taken from someone else
                            // It should wait of information in case of new position of the taxi
                            // Thread.sleep(1000);
                            ackCounter = 0;
                            System.out.println("I didn't got the ownership for the ride " + rideInfo.getId());
                        }
                    }
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

    private static void communicateNewPositionAndStatusAsync() {
        IPC.Infos presentMsg = getIPCInfos();

        for (TaxiInfo t : otherTaxis) {
            ManagedChannel channel = ManagedChannelBuilder
                    .forTarget(ADMIN_SERVER_ADDRESS + ":" + t.getGrpcPort())
                    .usePlaintext().build();
            IPCServiceGrpc.IPCServiceStub stub = IPCServiceGrpc.newStub(channel);

            StreamObserver<IPC.Infos> infosStreamObserver = stub.changedPositionStream(new StreamObserver<>() {
                @Override
                public void onNext(IPC.ACK value) {
                    System.out.println(t.getId() + " ACKed his new position");
                }

                @Override
                public void onError(Throwable t) {

                }

                @Override
                public void onCompleted() {

                }
            });

            infosStreamObserver.onNext(presentMsg);
            try {
                channel.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void coordinateRideGrpcStream(double distanceToDestination, int[] destination,
                                                 boolean isRechargeRide)
            throws InterruptedException {
        IPC.RideCharge rideCharge = getRideCharge(distanceToDestination, destination, isRechargeRide);

        for (TaxiInfo t : otherTaxis) {
            final boolean sameDistrict = (t.getDistrict() == thisTaxi.getDistrict());
            final boolean isFree = !t.isRecharging() && !t.isRiding();

            if (sameDistrict && isFree) {
                String target = ADMIN_SERVER_ADDRESS + ":" + t.getGrpcPort();
                ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                IPCServiceGrpc.IPCServiceStub stub = IPCServiceGrpc.newStub(channel);

                StreamObserver<IPC.RideCharge> rideChargeStreamObserver =
                        stub.coordinateRideStream(new StreamObserver<>() {
                            @Override
                            public void onNext(IPC.ACK value) {
                                if (value.getVote()) {
                                    ackCounter++;
                                    System.out.println("Received an ACK from " + t.getId());
                                } else {
                                    System.out.println("Received a NACK from " + t.getId());
                                }
                            }

                            @Override
                            public void onError(Throwable t) {

                            }

                            @Override
                            public void onCompleted() {

                            }
                        });

                rideChargeStreamObserver.onNext(rideCharge);
                //logicalClock += Utility.Utility.generateRndLong(0, LOGICAL_OFFSET);
                channel.awaitTermination(2, TimeUnit.SECONDS);
            }
        }
    }

    private static void performRide(RideInfo rideInfo) throws InterruptedException, MqttException {
        final int DELIVERY_TIME = 5000;

        thisTaxi.setRiding(true);

        double distToPassenger = Utility.euclideanDistance(thisTaxi.getPosition(), rideInfo.getStartPosition());
        double rideDistance = Utility.euclideanDistance(rideInfo.getStartPosition(), rideInfo.getDestinationPosition());
        double totalKm = distToPassenger + rideDistance;

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

        communicateNewPositionAndStatusAsync();

        Thread.sleep(DELIVERY_TIME);
        thisTaxi.setBattery(thisTaxi.getBattery() - totalKm);
        System.out.printf("I reached the destination and after %,.2f Km the battery levels are %,.2f %%\n",
                totalKm, thisTaxi.getBattery());

        thisTaxi.setRiding(false);
    }

    private static IPC.Infos getIPCInfos() {
        return IPC.Infos.newBuilder()
                .setId(thisTaxi.getId())
                .setDistrict(thisTaxi.getDistrict())
                .setGrpcPort(thisTaxi.getGrpcPort())
                .addPosition(thisTaxi.getPosition()[0])
                .addPosition(thisTaxi.getPosition()[1])
                .setIsRecharging(thisTaxi.isRecharging())
                .setIsRiding(thisTaxi.isRiding())
                .setBattery(thisTaxi.getBattery())
                .build();
    }

    private static IPC.RideCharge getRideCharge(double distanceToDestination, int[] destination, boolean isRechargeRide) {
        logicalClock += Utility.generateRndLong(0, LOGICAL_OFFSET);

        return IPC.RideCharge.newBuilder()
                .setTaxi(getIPCInfos())
                .addDestinationPosition(destination[0])
                .addDestinationPosition(destination[1])
                .setDistanceToDestination(distanceToDestination)
                .setRechargingRide(isRechargeRide)
                .setLogicalClock(logicalClock)
                .build();
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
        thisTaxi.setGrpcPort(Utility.generateRndInteger(0, 65535));
        client = ClientBuilder.newClient();

        // Send the taxi initialization request with a tentative random ID
        final String INIT_PATH = "/taxi-init";
        final int taxiID = Utility.generateRndInteger(0, 100 + 1);
        TaxiInfo initInfo = new TaxiInfo(taxiID, thisTaxi.getGrpcPort(), ADMIN_SERVER_ADDRESS);

        // Receive the initialization data from the server: valid ID, position, list of other taxis
        String serverInitInfos = Utility.postRequest(client, ADMIN_SERVER_URL + INIT_PATH, GSON.toJson(initInfo));

        TaxiSchema taxiSchema = GSON.fromJson(serverInitInfos, TaxiSchema.class);

        TaxiInfo serverData = taxiSchema.getTaxiInfo();

        thisTaxi.setId(serverData.getId());
        thisTaxi.setGrpcPort(serverData.getGrpcPort());
        thisTaxi.setPosition(serverData.getPosition());
        thisTaxi.setDistrict(serverData.getDistrict());
        thisTaxi.setBattery(100);
        otherTaxis = taxiSchema.getTaxis();

        System.out.println(serverData);
        logicalClock += LOGICAL_OFFSET;
    }

    private static int getAvailableTaxisInDistrict() {
        int counter = 0;
        for (TaxiInfo t : otherTaxis) {
            final boolean tHasSameDistrict = (t.getDistrict() == thisTaxi.getDistrict());
            final boolean tIsAvailable = !t.isRecharging() && !t.isRiding();
            if (tHasSameDistrict && tIsAvailable) counter++;
        }
        return counter;
    }

    // Close the MQTT connection of this client toward the broker
    /*private static void closingMqttConnection(MqttClient mqttClient) throws MqttException {
        if (false) {
            mqttClient.disconnect();
        }
    }*/

    public static void main(String[] args) {
        postInit();

        Scanner scanner = new Scanner(System.in);
        CLI cli = new CLI(thisTaxi.getId(), client, ADMIN_SERVER_URL, scanner);
        cli.start();

        GrpcServer grpcServer = new GrpcServer(thisTaxi.getGrpcPort());
        grpcServer.start();

        presentToOtherTaxis();

        startMqttClient();

        //closingMqttConnection(mqttClient);
        // Utility.Utility function will be helpful when from console i want to quit the taxi
        // TODO: Inizia l'acquisizione dei dati dal sensore
    }
}