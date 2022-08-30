/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. in Computer Science @UNIMI A.Y. 2021/2022 */
package Taxi.gRPC;

import Taxi.Structures.TaxiSchema;
import Taxi.Structures.LogicalClock;
import Taxi.Taxi;
import Taxi.Structures.TaxiInfo;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerBuilder;

import org.example.grpc.IPC;
import org.example.grpc.IPCServiceGrpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class GrpcModule implements Runnable {
    private final static String ADMIN_SERVER_ADDRESS = "localhost";
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

    public void setTaxiData(TaxiSchema taxiSchema) {
        this.thisTaxi = taxiSchema.getTaxiInfo();
        this.otherTaxis = taxiSchema.getTaxis();
    }

    public void setClockAndPort(LogicalClock logicalClock, int grpcPort) {
        this.grpcPort = grpcPort;
        this.taxiLogicalClock = logicalClock;
    }

    /* Start the gRPC server side of the taxi
     * ------------------------------------------------------------------------
     * Once this method is called it will be possible to send data to this
     * endpoint, the only requirement is to know the gRPC port.
     */
    @Override
    public void run() {
        if (grpcPort != -1) {
            try {
                io.grpc.Server server =
                        ServerBuilder.forPort(grpcPort)
                                .addService(new Taxi()).build();
                server.start();
                System.out.println("gRPC server started on port: " + grpcPort);
                server.awaitTermination();
                System.out.println("The server for gRPC communication has terminated");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void startServer() {
        if (server == null) {
            server = new Thread(this);
            server.start();
        }
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
    public void presentToOtherTaxis() {
        if (otherTaxis.size() > 0) {
            int ackS = sendHelloInBroadcast();
            int taxisInSmartCity = otherTaxis.size();

            if (ackS == taxisInSmartCity)
                System.out.println("Taxi " +
                        thisTaxi.getId() +
                        " has been presented correctly to all the previous taxis of the smart city");
            else
                System.out.println("Taxi " +
                        thisTaxi.getId() + " encountered an error during the presentation phase");
        }
    }

    // TODO: should be an async communication
    private int sendHelloInBroadcast() {
        IPC.Infos presentMsg = getIPCInfos();
        int ackS = 0;

        for (TaxiInfo t : otherTaxis) {
            ManagedChannel channel = getManagedChannel(t.getGrpcPort());
            IPCServiceGrpc.IPCServiceBlockingStub stub = IPCServiceGrpc.newBlockingStub(channel);
            //System.out.println("Sending to " + t.getId() + " the presentation at time " + logicalClock.printCalendar());
            IPC.ACK answer = stub.present(presentMsg);

            if (answer.getVote())
                ackS++;

            // Synchronize and increment the logical clock
            //syncLogicalClock(answer, t);
            channel.shutdown();
        }

        return ackS;
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

    private int sendGoodbyeBroadcastAsync() throws InterruptedException {
        IPC.Infos goodByeMsg = getIPCInfos();

        int i = 0;
        Thread[] threads = new Thread[otherTaxis.size()];

        for (TaxiInfo t : otherTaxis) {
            ManagedChannel channel = getManagedChannel(t.getGrpcPort());
            IPCServiceGrpc.IPCServiceStub stub = IPCServiceGrpc.newStub(channel);

            threads[i] = new Thread(new GrpcRunnable(t, goodByeMsg, stub));
            threads[i].start();
            channel.awaitTermination(2, TimeUnit.SECONDS);
            // Synchronize and increment the logical clock
            //syncLogicalClock(answer, t);
            channel.shutdown();
            i++;
        }

        for (Thread thread : threads) {
            thread.join();
        }

        return GrpcRunnable.getACKs();
    }

    private ManagedChannel getManagedChannel(int grpcPort) {
        return ManagedChannelBuilder.forTarget(ADMIN_SERVER_ADDRESS + ":" + grpcPort).usePlaintext().build();
    }

    public int coordinateRechargeGrpcStream() throws InterruptedException {
        IPC.RechargeProposal rechargeProposal = getIPCRechargProposal();

        Thread[] msg = new Thread[otherTaxis.size()];
        int i = 0;

        for (TaxiInfo t : otherTaxis) {
            ManagedChannel channel = getManagedChannel(t.getGrpcPort());
            IPCServiceGrpc.IPCServiceStub stub = IPCServiceGrpc.newStub(channel);
            msg[i] = new Thread(new GrpcRunnable(t, rechargeProposal, stub));
            msg[i].start();
            channel.awaitTermination(2, TimeUnit.SECONDS);
            i++;
        }

        return GrpcRunnable.getACKs();
    }


    public int coordinateRideGrpcStream(double distanceToDestination, int[] destination,
                                        boolean isRechargeRide) throws InterruptedException {
        IPC.RideCharge rideCharge = getRideCharge(distanceToDestination, destination, isRechargeRide);

        Thread[] msg = new Thread[otherTaxis.size()];
        int i = 0;

        for (TaxiInfo t : otherTaxis) {
            ManagedChannel channel = getManagedChannel(t.getGrpcPort());
            IPCServiceGrpc.IPCServiceStub stub = IPCServiceGrpc.newStub(channel);
            msg[i] = new Thread(new GrpcRunnable(t, rideCharge, stub));
            msg[i].start();
            channel.awaitTermination(2, TimeUnit.SECONDS);
            i++;
        }

        for (Thread thread : msg) {
            thread.join();
        }

        return GrpcRunnable.getACKs();
    }

    /*
    public static void syncLogicalClock(IPC.ACK value, TaxiInfo t) {
        LogicalClock serverClock = new LogicalClock(CLOCK_OFFSET);
        serverClock.setLogicalClock(value.getLogicalClock());

        if (logicalClock.getLogicalClock() <= value.getLogicalClock())
            logicalClock.setLogicalClock(value.getLogicalClock());

        logicalClock.increment();

        //System.out.println("ACK/NACK sent from " + t.getId()
        //      + " at " + serverClock.printCalendar()
        //    + ", received at " + logicalClock.printCalendar());
    }*/

    private IPC.RechargeProposal getIPCRechargProposal() {
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

    private IPC.RideCharge getRideCharge(double distanceToDestination, int[] destination, boolean isRechargeRide) {
        return IPC.RideCharge.newBuilder()
                .setTaxi(getIPCInfos())
                .addDestinationPosition(destination[0])
                .addDestinationPosition(destination[1])
                .setDistanceToDestination(distanceToDestination)
                .setRechargingRide(isRechargeRide)
                .build();
    }

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
}