/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. in Computer Science @UNIMI A.Y. 2021/2022 */
package Taxi;

import Taxi.Structures.LogicalClock;
import Taxi.Structures.TaxiInfo;
import Taxi.MQTT.MQTTModule;
import Taxi.Workers.Menu.InputCheckerThread;
import Taxi.Workers.Menu.CLIThread;
import Taxi.Workers.LocalStatsThread;
import Taxi.Statistics.PollutionBuffer;
import Taxi.Statistics.Simulators.PM10Simulator;
import Taxi.Workers.RechargeThread;
import Taxi.gRPC.GrpcModule;

import Misc.Utility;

import io.grpc.stub.StreamObserver;

import jakarta.ws.rs.client.*;
import jakarta.ws.rs.client.Client;

import Taxi.Structures.TaxiSchema;

import org.example.grpc.IPC;
import org.example.grpc.IPCServiceGrpc;

import java.util.ArrayList;

import static Misc.Utility.*;

public class Taxi extends IPCServiceGrpc.IPCServiceImplBase {
    private final static String ADMIN_SERVER_ADDRESS = "localhost";
    private final static int ADMIN_SERVER_PORT = 9001;
    private final static String ADMIN_SERVER_URL =
            "http://" + ADMIN_SERVER_ADDRESS + ":" + ADMIN_SERVER_PORT;
    private static final GrpcModule grpcModule = GrpcModule.getInstance();

    // Taxi Data
    public static long CLOCK_OFFSET = Utility.generateRndLong(0, 15L);

    public static LogicalClock logicalClock;
    private static final TaxiInfo thisTaxi = new TaxiInfo();
    private static Client client;
    private static ArrayList<TaxiInfo> otherTaxis = new ArrayList<>();

    public static void main(String[] args) {
        logicalClockInit();

        postInit();

        // Create Local Stats & PM10 threads
        PollutionBuffer pollutionBuffer = new PollutionBuffer();
        PM10Simulator pm10SimulatorThread =
                new PM10Simulator(Integer.toString(generateRndInteger(0, 10)), pollutionBuffer);

        LocalStatsThread localStatsThread =
                new LocalStatsThread(thisTaxi, ADMIN_SERVER_URL, client, pollutionBuffer);

        // Create recharge thread
        Object checkBattery = new Object();
        RechargeThread rechargeThread = new RechargeThread(thisTaxi, otherTaxis, checkBattery, grpcModule);

        // Create the thread for CLI and for checking the input
        Object inputAvailable = new Object();
        CLIThread cliThread = new CLIThread(thisTaxi, otherTaxis, inputAvailable, rechargeThread);
        InputCheckerThread inputCheckerThread = new InputCheckerThread(inputAvailable);

        // Start the threads
        cliThread.start();
        inputCheckerThread.start();
        localStatsThread.start();
        pm10SimulatorThread.start();
        rechargeThread.start();

        // GRPC
        TaxiSchema taxiSchema = new TaxiSchema();
        taxiSchema.setTaxiInfo(thisTaxi);
        taxiSchema.setTaxis(otherTaxis);

        grpcModule.setTaxiData(taxiSchema);
        grpcModule.setClockAndPort(logicalClock, thisTaxi.getGrpcPort());
        grpcModule.startServer();
        grpcModule.presentToOtherTaxis();

        // MQTT
        MQTTModule mqttModule = new MQTTModule(taxiSchema, checkBattery);
        mqttModule.startMqttClient();

        // TODO: Function for terminating correctly all the threads.
    }

    private static void logicalClockInit() {
        logicalClock = new LogicalClock(CLOCK_OFFSET);
        logicalClock.increment();
        //System.out.println("Logical clock initial value " + logicalClock.printCalendar());
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
        thisTaxi.setBattery(100.0);
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
                logicalClock.increment();

                LogicalClock requestLogicalClock = new LogicalClock(CLOCK_OFFSET);
                requestLogicalClock.setLogicalClock(request.getTaxi().getLogicalClock());

                if (logicalClock.getLogicalClock() < requestLogicalClock.getLogicalClock()) {
                    logicalClock.setLogicalClock(requestLogicalClock.getLogicalClock());
                }

                if (thisTaxi.getDistrict() == request.getTaxi().getDistrict()) {
                    if (thisTaxi.wantToRecharge()) {
                        if (logicalClock.getLogicalClock() > request.getTaxi().getLogicalClock()) {
                            sendAck(responseObserver, IPC.ACK.newBuilder()
                                            .setVote(true)
                                            .setId(thisTaxi.getId()),
                                    logicalClock.getLogicalClock());
                        } else if (logicalClock.getLogicalClock() == request.getTaxi().getLogicalClock()) {
                            // Enforce Lamport Total Order
                            if (request.getTaxi().getId() < thisTaxi.getId()) {
                                sendAck(responseObserver, IPC.ACK.newBuilder()
                                                .setVote(true)
                                                .setId(thisTaxi.getId()),
                                        logicalClock.getLogicalClock());
                            }
                        }
                    }
                } else {
                    // REPLY ACK, the taxi is not interested in recharging
                    sendAck(responseObserver, IPC.ACK.newBuilder().setVote(true), -9999L);
                }

            }

            private void sendAck(StreamObserver<IPC.ACK> responseObserver, IPC.ACK.Builder thisTaxi, long logicalClock) {
                responseObserver.onNext(thisTaxi
                        .setLogicalClock(logicalClock).build());
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
                logicalClock.increment();
                LogicalClock requestLogicalClock = new LogicalClock(CLOCK_OFFSET);

                requestLogicalClock.setLogicalClock(request.getTaxi().getLogicalClock());
                // System.out.println(request.getTaxi().getId()
                // + " sent the request at "
                // + requestLogicalClock.printCalendar());

                if (logicalClock.getLogicalClock() <= request.getTaxi().getLogicalClock()) {
                    logicalClock.setLogicalClock(request.getTaxi().getLogicalClock());
                }

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

        LogicalClock clientClock = new LogicalClock(CLOCK_OFFSET);
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

                LogicalClock clientClock = new LogicalClock(CLOCK_OFFSET);
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

    public static LogicalClock getLogicalClock() {
        return logicalClock;
    }

    public static void setLogicalClock(LogicalClock logicalClock) {
        Taxi.logicalClock = logicalClock;
    }
}