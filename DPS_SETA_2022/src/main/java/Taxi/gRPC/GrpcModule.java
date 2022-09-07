/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. in Computer Science @UNIMI A.Y. 2021/2022 */
package Taxi.gRPC;

import Taxi.Structures.TaxiSchema;
import Taxi.Structures.LogicalClock;

import Taxi.Structures.TaxiInfo;


import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerBuilder;

import org.example.grpc.IPC;
import org.example.grpc.IPCServiceGrpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/* GrpcModule
 * ------------------------------------------------------------------------
 * This modules handle the various communication (broadcast) between the
 * taxis. Essentially it prepares the settings for use the gRPC services
 * and collect the ACKs.
 */
public class GrpcModule implements Runnable {
    private final static String ADMIN_SERVER_ADDRESS = "localhost";
    private GrpcServices grpcServices;
    private Thread server;
    private TaxiInfo thisTaxi;
    private ArrayList<TaxiInfo> otherTaxis;
    private int grpcPort = -1;
    private static GrpcModule instance;
    private LogicalClock taxiLogicalClock;

    GrpcModule() {
    }

    public static GrpcModule getInstance() {
        if (instance == null)
            instance = new GrpcModule();
        return instance;
    }

    public void startServer() {
        if (server == null) {
            server = new Thread(this);
            server.start();
        }
    }

    /* Start the gRPC server side of the taxi
     * -------------------------------------------------------------------------------------
     * Once this method is called it will be possible to send data to this
     * endpoint, the only requirement is to know the gRPC port.
     */
    @Override
    public void run() {
        if (grpcPort != -1) {
            try {
                io.grpc.Server server = ServerBuilder.forPort(grpcPort)
                        .addService(new GrpcServices(new TaxiSchema(thisTaxi, otherTaxis)))
                        .build();
                server.start();
                System.out.println("gRPC server started on port: " + grpcPort);
                server.awaitTermination();
                System.out.println("The server for gRPC communication has terminated");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * Send a broadcast presentation message through gRPC to all taxis in the smartcity
     * -------------------------------------------------------------------------------------
     * This function sends to each previously registered taxi in the system (received from
     * the initialization POST of the administrator server) its own individual data.
     *
     * This will make possible to create future bidirectional communication with the other
     * taxis because now they will know the gRPC port of the new taxi.
     */
    public void broadcastPresentationSync() {
        if (otherTaxis.size() > 0) {
            int ackS = broadcastPresentationSyncImpl();
            int taxisInSmartCity = otherTaxis.size();

            if (ackS == taxisInSmartCity)
                System.out.println("The taxi " + thisTaxi.getId() +
                        " has been presented correctly to all the taxis of the smart city");
            else
                System.out.println("The taxi " + thisTaxi.getId() +
                        " encountered an error during the presentation phase");
        }
    }

    // Sends in broadcast (parallel) the presentation messages and return the collect ACKs
    private int broadcastPresentationSyncImpl() {
        IPC.Infos presentMsg = getIPCInfos();
        int ackS = 0;

        for (TaxiInfo t : otherTaxis) {
            ManagedChannel channel = getManagedChannel(t.getGrpcPort());
            IPCServiceGrpc.IPCServiceBlockingStub stub = IPCServiceGrpc.newBlockingStub(channel);

            IPC.ACK answer = stub.present(presentMsg);

            if (answer.getVote())
                ackS++;

            channel.shutdown();
        }

        return ackS;
    }

    /*
     * Send a broadcast goodbye message through gRPC to all taxis in the smartcity
     * -------------------------------------------------------------------------------------
     * This function sends to each previously registered taxi in the system its own
     * individual data.
     *
     * This will trigger the elimination of the taxi from the list of the other taxis.
     */
    public void broadcastGoodbyeSync() {
        if (otherTaxis.size() > 0) {
            int ackS = broadcastGoodbyeSyncImpl();
            int taxisInSmartCity = otherTaxis.size();

            if (ackS == taxisInSmartCity)
                System.out.println("The taxi " + thisTaxi.getId() +
                        " has been removed correctly from all the taxis of the smart city");
            else
                System.out.println("The taxi " + thisTaxi.getId() +
                        " encountered an error during the goodbye phase");
        }
    }

    // Sends in broadcast (parallel) the goodbye messages and return the collected ACKs
    private int broadcastGoodbyeSyncImpl() {
        IPC.Infos goodByeMsg = getIPCInfos();
        int ackS = 0;

        for (TaxiInfo t : otherTaxis) {
            ManagedChannel channel = getManagedChannel(t.getGrpcPort());
            IPCServiceGrpc.IPCServiceBlockingStub stub = IPCServiceGrpc.newBlockingStub(channel);
            IPC.ACK answer = stub.goodbye(goodByeMsg);

            if (answer.getVote())
                ackS++;

            channel.shutdown();
        }

        return ackS;
    }

    // Uses the 'broadcastGrpcStreams' for sending broadcast recharging requests
    public int coordinateRechargeGrpcStream() throws InterruptedException {
        IPC.RechargeProposal rechargeProposal = getIPCRechargeProposal();

        GrpcMessage grpcMessages = new GrpcMessage();
        grpcMessages.setRechargeProposal(rechargeProposal);

        final int receivedACKs = broadcastGrpcStreams(grpcMessages);
        GrpcRunnable.resetACKS();
        return receivedACKs;
    }

    // Uses the 'broadcastGrpcStreams' for sending broadcast ride requests
    public int coordinateRideGrpcStream(double distanceToDestination, int[] destination,
                                        boolean isRechargeRide) throws InterruptedException {
        IPC.RideCharge rideCharge = getRideCharge(distanceToDestination, destination, isRechargeRide);

        GrpcMessage grpcMessages = new GrpcMessage();
        grpcMessages.setRideCharge(rideCharge);

        final int receivedACKs = broadcastGrpcStreams(grpcMessages);
        GrpcRunnable.resetACKS();
        return receivedACKs;
    }

    /* Sends in broadcasts the requests for streams
     * -------------------------------------------------------------------------------------
     * Generalizes the gRPC services that uses streams. i.e. recharge and ride handling.
     * It sends the requests in broadcast (parallel), and returns the collected ACKs.
     */
    private int broadcastGrpcStreams(GrpcMessage grpcMessages) throws InterruptedException {
        Thread[] threads = new Thread[otherTaxis.size()];
        int i = 0;

        for (TaxiInfo t : otherTaxis) {
            ManagedChannel channel = getManagedChannel(t.getGrpcPort());
            IPCServiceGrpc.IPCServiceStub stub = IPCServiceGrpc.newStub(channel);
            threads[i] = new Thread(new GrpcRunnable(t, grpcMessages, stub));
            threads[i].start();
            channel.awaitTermination(2, TimeUnit.SECONDS);
            i++;
        }

        for (Thread t : threads) {
            t.join();
        }
        return GrpcRunnable.getACKs();
    }

    /// Getters & Setters

    // Return the managed channel given the gRPC port
    private ManagedChannel getManagedChannel(int grpcPort) {
        return ManagedChannelBuilder.forTarget(ADMIN_SERVER_ADDRESS + ":" + grpcPort).usePlaintext().build();
    }

    // Get IPC proposal information for the recharge service
    private IPC.RechargeProposal getIPCRechargeProposal() {
        return IPC.RechargeProposal.newBuilder()
                .setTaxi(IPC.Infos.newBuilder()
                        .setId(thisTaxi.getId())
                        .setDistrict(thisTaxi.getDistrict())
                        .setGrpcPort(thisTaxi.getGrpcPort())
                        .addPosition(thisTaxi.getPosition()[0])
                        .addPosition(thisTaxi.getPosition()[1])
                        .setIsRecharging(thisTaxi.isRecharging())
                        .setIsRiding(thisTaxi.isRiding())
                        .setBattery(thisTaxi.getBattery())
                        .setLogicalClock(taxiLogicalClock.getLogicalClock())
                        .build())
                .setLogicalClock(taxiLogicalClock.getLogicalClock())
                .build();
    }

    // Get IPC ride charge information for the handling of the ride
    private IPC.RideCharge getRideCharge(double distanceToDestination, int[] destination, boolean isRechargeRide) {
        return IPC.RideCharge.newBuilder()
                .setTaxi(getIPCInfos())
                .addDestinationPosition(destination[0])
                .addDestinationPosition(destination[1])
                .setDistanceToDestination(distanceToDestination)
                .setRechargingRide(isRechargeRide)
                .build();
    }

    // Get IPC infos information for using the presentation and goodbye services
    private IPC.Infos getIPCInfos() {
        return IPC.Infos.newBuilder()
                .setId(thisTaxi.getId())
                .setDistrict(thisTaxi.getDistrict())
                .setGrpcPort(thisTaxi.getGrpcPort())
                .addPosition(thisTaxi.getPosition()[0])
                .addPosition(thisTaxi.getPosition()[1])
                .setIsRecharging(thisTaxi.isRecharging())
                .setIsRiding(thisTaxi.isRiding())
                .setBattery(thisTaxi.getBattery())
                .setLogicalClock(taxiLogicalClock.getLogicalClock())
                .build();
    }

    // Set the taxi info and taxi list
    public void setTaxiData(TaxiSchema taxiSchema) {
        this.thisTaxi = taxiSchema.getTaxiInfo();
        this.otherTaxis = taxiSchema.getTaxis();
        grpcServices = new GrpcServices(taxiSchema);
    }

    // Set the logical clock and the gRPC port of the taxi
    public void setClockAndPort(LogicalClock logicalClock, int grpcPort) {
        this.grpcPort = grpcPort;
        this.taxiLogicalClock = logicalClock;
        grpcServices.setClock(logicalClock);
    }

    /*
    private int sendGoodbyeBroadcastAsync() throws InterruptedException {
        IPC.Infos goodByeMsg = getIPCInfos();
        GrpcMessages grpcMessages = new GrpcMessages();
        grpcMessages.setInfos(goodByeMsg);

        final int receivedACKs = broadcastGrpcStreams(grpcMessages);
        GrpcRunnable.resetACKS();
        return receivedACKs;
    }

    // TODO : Should be an async communication
    public void removeTaxiAsync() throws InterruptedException {
        int totalAck = otherTaxis.size();
        int receivedAck = sendGoodbyeBroadcastAsync();
        GrpcRunnable.resetACKS();

        if (totalAck == receivedAck) {
            System.out.println("The other taxis removed me correctly from their list");
        } else {
            System.out.println("An error occurred during the removal of this taxi from the" +
                    "entries of the other taxis");
        }
    }
    */
}