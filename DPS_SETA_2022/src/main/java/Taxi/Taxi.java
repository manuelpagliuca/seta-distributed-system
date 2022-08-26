/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. of Computer Science @UNIMI A.Y. 2021/2022 */
package Taxi;

import Taxi.Data.LogicalClock;
import Taxi.Data.TaxiInfo;
import Taxi.MQTT.MQTTModule;
import Taxi.Menu.InputCheckerThread;
import Taxi.Menu.MenuRunnable;
import Taxi.Statistics.LocalStatisticsRunnable;
import Taxi.Statistics.PollutionBuffer;
import Taxi.Statistics.Simulators.PM10Simulator;
import Taxi.gRPC.GrpcModule;

import Utility.Utility;

import io.grpc.stub.StreamObserver;

import jakarta.ws.rs.client.*;
import jakarta.ws.rs.client.Client;

import Taxi.Data.TaxiSchema;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.example.grpc.IPC;
import org.example.grpc.IPCServiceGrpc;

import java.util.ArrayList;

import static Utility.Utility.*;

public class Taxi extends IPCServiceGrpc.IPCServiceImplBase {
    private final static String ADMIN_SERVER_ADDRESS = "localhost";
    private final static int ADMIN_SERVER_PORT = 9001;
    private final static String ADMIN_SERVER_URL = "http://" + ADMIN_SERVER_ADDRESS + ":" + ADMIN_SERVER_PORT;
    private static final GrpcModule grpcModule = GrpcModule.getInstance();

    // Taxi Data
    public static long RAND_CLOCK_OFFSET = Utility.generateRndLong(0, 15L);
    public static LogicalClock logicalClock;
    private static final TaxiInfo thisTaxi = new TaxiInfo();
    private static Client client;
    private static ArrayList<TaxiInfo> otherTaxis = new ArrayList<>();

    public static void main(String[] args) throws MqttException {
        logicalClockInit();

        postInit();

        // Local Stats & Sensor Data
        PollutionBuffer pollutionBuffer = new PollutionBuffer();

        startLocalStatsThread(pollutionBuffer);

        PM10Simulator pm10SimulatorThread = new PM10Simulator(Integer.toString(generateRndInteger(0, 10)), pollutionBuffer);
        pm10SimulatorThread.start();

        // User Input
        Object inputAvailable = startCLIthread();
        startInputChecker(inputAvailable);

        // GRPC
        TaxiSchema taxiSchema = new TaxiSchema();
        taxiSchema.setTaxiInfo(thisTaxi);
        taxiSchema.setTaxis(otherTaxis);

        grpcModule.setTaxiData(taxiSchema);
        grpcModule.setClockAndPort(logicalClock, thisTaxi.getGrpcPort());
        grpcModule.startServer();
        grpcModule.presentToOtherTaxis();

        // Battery Thread
        Object checkBattery = new Object();
        Thread batteryThread = new Thread(new RechargeRunnable(thisTaxi, otherTaxis, checkBattery, grpcModule));
        batteryThread.start();

        // MQTT
        MQTTModule mqttModule = new MQTTModule(taxiSchema, checkBattery);
        mqttModule.startMqttClient();

        // TODO: Function for terminating correctly all the threads.
    }

    private static void logicalClockInit() {
        logicalClock = new LogicalClock(RAND_CLOCK_OFFSET);
        logicalClock.increment();
        System.out.println("Logical clock initial value " + logicalClock.printCalendar());
    }

    private static void startLocalStatsThread(PollutionBuffer pollutionBuffer) {
        Thread localStatistics = new Thread(new LocalStatisticsRunnable(pollutionBuffer,
                thisTaxi, ADMIN_SERVER_URL, client));
        localStatistics.start();
    }

    private static void startInputChecker(Object inputAvailable) {
        InputCheckerThread checkingLines = new InputCheckerThread(inputAvailable);
        checkingLines.start();
    }

    private static Object startCLIthread() {
        Object inputAvailable = new Object();
        MenuRunnable cli = new MenuRunnable(thisTaxi, otherTaxis, inputAvailable);
        cli.start();
        return inputAvailable;
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
    }

    /*
     * Delete this taxi from the administrator server
     * ------------------------------------------------------------------------------
     * Build the specific URL path for performing the DELETE request on the
     * administrator server.
     */
    public static void removeTaxi() throws InterruptedException {
        grpcModule.removeTaxiAsync();

        final String INIT_PATH = "/del-taxi/" + thisTaxi.getId();
        String serverInitInfos = delRequest(client, ADMIN_SERVER_URL + INIT_PATH);
        System.out.println(serverInitInfos);
        System.exit(0);
    }

    /// gRPC Services

    // Ricart & Agrawala
    @Override
    public StreamObserver<IPC.RechargeProposal> coordinateRechargeStream(StreamObserver<IPC.ACK> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(IPC.RechargeProposal request) {
                LogicalClock requestLogicalClock = new LogicalClock(RAND_CLOCK_OFFSET);

                // TODO: Lamport
                requestLogicalClock.setLogicalClock(request.getTaxi().getLogicalClock());
                // System.out.println(request.getTaxi().getId()
                // + " sent the request at "
                // + requestLogicalClock.printCalendar());

                if (logicalClock.getLogicalClock() <= request.getTaxi().getLogicalClock()) {
                    logicalClock.setLogicalClock(request.getTaxi().getLogicalClock());
                }

                logicalClock.increment();

                // il server e' interessato alla corsa?
                if (thisTaxi.getDistrict() == request.getTaxi().getDistrict()) {
                    if (!thisTaxi.wantToRecharge()) {
                        responseObserver.onNext(IPC.ACK.newBuilder()
                                .setVote(true)
                                .setLogicalClock(-9999).build());
                        responseObserver.onCompleted();
                        return;
                    } else {
                        // If also the server taxi wants the recharge, TODO discriminate by logical clock?
                        if (logicalClock.getLogicalClock() < request.getLogicalClock()) {
                            responseObserver.onNext(IPC.ACK.newBuilder()
                                    .setVote(false)
                                    .setLogicalClock(-9999).build());
                            responseObserver.onCompleted();
                        }
                    }
                }
                // The server taxi is from a different district /TODO or bigger logical clock? -> ACK
                responseObserver.onNext(IPC.ACK.newBuilder()
                        .setVote(true)
                        .setLogicalClock(-9999).build());
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

    // TODO Logical Clock
    @Override
    public StreamObserver<IPC.RideCharge> coordinateRideStream(StreamObserver<IPC.ACK> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(IPC.RideCharge request) {
                LogicalClock requestLogicalClock = new LogicalClock(RAND_CLOCK_OFFSET);

                requestLogicalClock.setLogicalClock(request.getTaxi().getLogicalClock());
                // System.out.println(request.getTaxi().getId()
                // + " sent the request at "
                // + requestLogicalClock.printCalendar());

                if (logicalClock.getLogicalClock() <= request.getTaxi().getLogicalClock()) {
                    logicalClock.setLogicalClock(request.getTaxi().getLogicalClock());
                }

                logicalClock.increment();
                //System.out.println(thisTaxi.getId() + " received the request at " + logicalClock.printCalendar());

                // From Client Stream
                // Check the distance between the server taxi and the received distance
                int[] passengerPos = new int[2];
                passengerPos[0] = request.getDestinationPosition(0);
                passengerPos[1] = request.getDestinationPosition(1);

                final double clientTaxiDistance = request.getDistanceToDestination();
                final double serverTaxiDistance = Utility.euclideanDistance(thisTaxi.getPosition(), passengerPos);

                final boolean differentDistrict = thisTaxi.getDistrict() != request.getTaxi().getDistrict();
                final boolean isRecharging = thisTaxi.isRecharging();
                final boolean isRiding = thisTaxi.isRiding();
                final boolean tripleCondition = differentDistrict || isRecharging || isRiding;

                if (tripleCondition) {
                    responseObserver.onNext(IPC.ACK.newBuilder()
                            .setVote(true)
                            .setLogicalClock(logicalClock.getLogicalClock()).build());
                    responseObserver.onCompleted();
                    return;
                }

                if (serverTaxiDistance > clientTaxiDistance) {
                    // The client taxi has a smaller distance to the passenger than this, ACK
                    responseObserver.onNext(IPC.ACK.newBuilder()
                            .setVote(true).setLogicalClock(logicalClock.getLogicalClock()).build());
                    // System.out.println(thisTaxi.getId()
                    //        + " has a smaller distance than "
                    //        + request.getTaxi().getId());
                    responseObserver.onCompleted();
                    return;
                } else if (serverTaxiDistance == clientTaxiDistance) {
                    if (thisTaxi.getBattery() < request.getTaxi().getBattery()) {
                        // Requester has more battery than this, ACK
                        // System.out.println(thisTaxi.getId()
                        //        + " has less battery than "
                        //        + request.getTaxi().getId());
                        responseObserver.onNext(IPC.ACK.newBuilder()
                                .setVote(true)
                                .setLogicalClock(logicalClock.getLogicalClock()).build());
                        responseObserver.onCompleted();
                        return;
                    } else if (thisTaxi.getBattery() == request.getTaxi().getBattery()) {
                        if (thisTaxi.getId() < request.getTaxi().getId()) {
                            // The requesting taxi has a greater ID than this taxi, ACK
                            // System.out.println(thisTaxi.getId()
                            //        + " has a smaller ID than "
                            //        + request.getTaxi().getId());
                            responseObserver.onNext(IPC.ACK.newBuilder()
                                    .setVote(true)
                                    .setLogicalClock(logicalClock.getLogicalClock()).build());
                            responseObserver.onCompleted();
                            return;
                        }
                    }
                }
                // This taxi has a better distance, battery level or ID value than the requester,
                // sends NACK to him
                responseObserver
                        .onNext(IPC.ACK.newBuilder()
                                .setVote(false)
                                .setLogicalClock(logicalClock.getLogicalClock()).build());
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

        LogicalClock clientClock = new LogicalClock(RAND_CLOCK_OFFSET);
        clientClock.setLogicalClock(request.getLogicalClock());

        // System.out.println("Received the presentation from " + request.getId() +
        // " at time " + clientClock.printCalendar());

        if (logicalClock.getLogicalClock() <= request.getLogicalClock()) {
            logicalClock.setLogicalClock(request.getLogicalClock());
        }
        logicalClock.increment();

        if (taxiIsNew) {
            otherTaxis.add(clientTaxi);
            responseObserver.onNext(IPC.ACK.newBuilder()
                    .setVote(true)
                    .setLogicalClock(logicalClock.getLogicalClock()).build());
            responseObserver.onCompleted();
            return;
        }

        responseObserver.onNext(IPC.ACK.newBuilder()
                .setVote(false)
                .setLogicalClock(logicalClock.getLogicalClock()).build());
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<IPC.Infos> goodbye(StreamObserver<IPC.ACK> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(IPC.Infos request) {
                TaxiInfo clientTaxi = new TaxiInfo(request);

                LogicalClock clientClock = new LogicalClock(RAND_CLOCK_OFFSET);
                clientClock.setLogicalClock(request.getLogicalClock());

                // System.out.println("Received the presentation from " + request.getId() +
                // " at time " + clientClock.printCalendar());

                if (logicalClock.getLogicalClock() <= request.getLogicalClock()) {
                    logicalClock.setLogicalClock(request.getLogicalClock());
                }
                logicalClock.increment();

                otherTaxis.removeIf(t -> clientTaxi.getId() == t.getId());

                responseObserver.onNext(IPC.ACK.newBuilder()
                        .setVote(true)
                        .setLogicalClock(logicalClock.getLogicalClock()).build());
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
}