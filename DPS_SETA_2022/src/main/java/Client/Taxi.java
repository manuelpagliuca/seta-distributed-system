/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. of Computer Science @UNIMI A.Y. 2021/2022 */
package Client;

import Utility.Utility;

import io.grpc.stub.StreamObserver;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.client.Client;

import Schemes.TaxiSchema;
import org.example.grpc.IPC;
import org.example.grpc.IPCServiceGrpc;

import java.util.ArrayList;

import static Utility.Utility.GSON;
import static Utility.Utility.delRequest;

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

    public static void main(String[] args) {
        logicalClock = new LogicalClock(RAND_CLOCK_OFFSET);
        logicalClock.increment();
        System.out.println("Logical clock initial value " + logicalClock.printCalendar());

        postInit();

        CLI cli = new CLI();
        cli.start();

        TaxiSchema taxiSchema = new TaxiSchema();
        taxiSchema.setTaxiInfo(thisTaxi);
        taxiSchema.setTaxis(otherTaxis);

        grpcModule.setTaxiData(taxiSchema);
        grpcModule.setClockAndPort(logicalClock, thisTaxi.getGrpcPort());
        grpcModule.startServer();
        grpcModule.presentToOtherTaxis();

        MQTTModule mqttModule = new MQTTModule(taxiSchema);
        mqttModule.startMqttClient();

    }

    // TODO Logical Clock
    @Override
    public StreamObserver<IPC.RideCharge> coordinateRideStream(StreamObserver<IPC.ACK> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(IPC.RideCharge request) {
                LogicalClock requestLogicalClock = new LogicalClock(RAND_CLOCK_OFFSET);

                requestLogicalClock.setLogicalClock(request.getTaxi().getLogicalClock());
                System.out.println(request.getTaxi().getId()
                        + " sent the request at "
                        + requestLogicalClock.printCalendar());

                if (logicalClock.getLogicalClock() <= request.getTaxi().getLogicalClock()) {
                    logicalClock.setLogicalClock(request.getTaxi().getLogicalClock());
                }

                logicalClock.increment();
                System.out.println(thisTaxi.getId() + " received the request at " + logicalClock.printCalendar());

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
                        responseObserver.onNext(IPC.ACK.newBuilder()
                                .setVote(true).setLogicalClock(logicalClock.getLogicalClock()).build());
                        responseObserver.onCompleted();
                        return;
                    }
                }

                discriminateRequest(request, clientDistance, serverDistance, responseObserver);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        };
    }

    private static void discriminateRequest(IPC.RideCharge request, double clientDistance, double serverDistance, StreamObserver<IPC.ACK> responseObserver) {
        if (serverDistance > clientDistance) {
            // The client taxi has a smaller distance to the passenger than this, ACK
            responseObserver.onNext(IPC.ACK.newBuilder()
                    .setVote(true).setLogicalClock(logicalClock.getLogicalClock()).build());
            responseObserver.onCompleted();
            return;
        } else if (serverDistance == clientDistance) {
            if (thisTaxi.getBattery() < request.getTaxi().getBattery()) {
                // Requester has more battery than this, ACK
                responseObserver.onNext(IPC.ACK.newBuilder()
                        .setVote(true).setLogicalClock(logicalClock.getLogicalClock()).build());
                responseObserver.onCompleted();
                return;
            } else if (thisTaxi.getBattery() == request.getTaxi().getBattery()) {
                if (thisTaxi.getId() < request.getTaxi().getId()) {
                    // The requesting taxi has a greater ID than this taxi, ACK
                    responseObserver.onNext(IPC.ACK.newBuilder()
                            .setVote(true).setLogicalClock(logicalClock.getLogicalClock()).build());
                    responseObserver.onCompleted();
                    return;
                }
            }
        }
        // This taxi has a better distance, battery level or ID value than the requester, sends NACK to him
        responseObserver.onNext(IPC.ACK.newBuilder()
                .setVote(false).setLogicalClock(logicalClock.getLogicalClock()).build());
        responseObserver.onCompleted();
    }

    // todo handle the logical clock
    @Override
    public StreamObserver<IPC.Infos> changedPositionStream(StreamObserver<IPC.ACK> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(IPC.Infos request) {
                if (logicalClock.getLogicalClock() <= request.getLogicalClock()) {
                    logicalClock.setLogicalClock(request.getLogicalClock());
                }
                logicalClock.increment();

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

    @Override
    public void present(IPC.Infos request, StreamObserver<IPC.ACK> responseObserver) {
        TaxiInfo clientTaxi = new TaxiInfo(request);
        final boolean taxiIsNew = otherTaxis.isEmpty() || !otherTaxis.contains(clientTaxi);

        LogicalClock clientClock = new LogicalClock(RAND_CLOCK_OFFSET);
        clientClock.setLogicalClock(request.getLogicalClock());

        System.out.println("Received the presentation from " + request.getId()
                + " at time " + clientClock.printCalendar());

        if (logicalClock.getLogicalClock() <= request.getLogicalClock()) {
            logicalClock.setLogicalClock(request.getLogicalClock());
        }
        logicalClock.increment();

        if (taxiIsNew) {
            otherTaxis.add(clientTaxi);
            responseObserver.onNext(IPC.ACK.newBuilder()
                    .setVote(true)
                    .setLogicalClock(logicalClock.getLogicalClock()).build());
        } else {
            responseObserver.onNext(IPC.ACK.newBuilder()
                    .setVote(false)
                    .setLogicalClock(logicalClock.getLogicalClock()).build());
        }
        responseObserver.onCompleted();
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
    public static void removeTaxi() {
        final String INIT_PATH = "/del-taxi/" + thisTaxi.getId();
        String serverInitInfos = delRequest(client, ADMIN_SERVER_URL + INIT_PATH);
        System.out.println(serverInitInfos);
        // todo goodbye procedure
        System.exit(0);
    }

}