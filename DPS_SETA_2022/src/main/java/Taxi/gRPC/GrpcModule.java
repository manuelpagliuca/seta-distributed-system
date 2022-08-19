package Taxi.gRPC;

import Taxi.Data.TaxiSchema;
import Taxi.Data.LogicalClock;
import Taxi.Taxi;
import Taxi.Data.TaxiInfo;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.example.grpc.IPC;
import org.example.grpc.IPCServiceGrpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static Taxi.Taxi.*;

public class GrpcModule implements Runnable {
    private final static String ADMIN_SERVER_ADDRESS = "localhost";
    private Thread server;
    private TaxiInfo thisTaxi;
    private ArrayList<TaxiInfo> otherTaxis;
    private int grpcPort = -1;
    private static GrpcModule instance;
    private static int ackRides = 0;
    private static int ackNewPosition = 0;
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

    private int sendHelloInBroadcast() {
        IPC.Infos presentMsg = getIPCInfos();
        int ackS = 0;
        for (TaxiInfo t : otherTaxis) {
            ManagedChannel channel = ManagedChannelBuilder
                    .forTarget(ADMIN_SERVER_ADDRESS + ":" + t.getGrpcPort()).usePlaintext().build();
            IPCServiceGrpc.IPCServiceBlockingStub stub = IPCServiceGrpc.newBlockingStub(channel);
            System.out.println("Sending to " + t.getId() + " the presentation at time " + logicalClock.printCalendar());
            IPC.ACK ans = stub.present(presentMsg);

            if (ans.getVote())
                ackS++;

            // Synchronize and increment the logical clock
            syncLogicalClock(ans, t);
            channel.shutdown();
        }

        return ackS;
    }

    public void communicateNewPositionAndStatusAsync() {
        IPC.Infos presentMsg = getIPCInfos();

        for (TaxiInfo t : otherTaxis) {
            ManagedChannel channel = ManagedChannelBuilder.forTarget(ADMIN_SERVER_ADDRESS + ":" + t.getGrpcPort())
                    .usePlaintext().build();
            IPCServiceGrpc.IPCServiceStub stub = IPCServiceGrpc.newStub(channel);
            StreamObserver<IPC.Infos> infosStreamObserver = countAckNewPosition(stub, t);
            infosStreamObserver.onNext(presentMsg);
            awaitChannelTermination(channel);
        }

        if (ackNewPosition == otherTaxis.size()) {
            System.out.println("The remaining " + otherTaxis.size() + " taxi/s know your new position.");
        } else {
            System.out.println("Not all the other taxis received correctly your position!");
            removeTaxi();
        }

        ackNewPosition = 0;
    }

    private static StreamObserver<IPC.Infos> countAckNewPosition(
            IPCServiceGrpc.IPCServiceStub stub, TaxiInfo t) {
        return stub.changedPositionStream(new StreamObserver<>() {
            @Override
            public void onNext(IPC.ACK value) {
                if (value.getVote())
                    ackNewPosition++;
                syncLogicalClock(value, t);
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });
    }

    private static void awaitChannelTermination(ManagedChannel channel) {
        try {
            channel.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void coordinateRideGrpcStream(double distanceToDestination, int[] destination, boolean isRechargeRide)
            throws InterruptedException {
        IPC.RideCharge rideCharge = getRideCharge(distanceToDestination, destination, isRechargeRide);

        for (TaxiInfo t : otherTaxis) {
            final boolean sameDistrict = (t.getDistrict() == thisTaxi.getDistrict());
            final boolean isFree = (!t.isRecharging() && !t.isRiding());

            if (sameDistrict && isFree) {
                String target = ADMIN_SERVER_ADDRESS + ":" + t.getGrpcPort();
                ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
                IPCServiceGrpc.IPCServiceStub stub = IPCServiceGrpc.newStub(channel);
                StreamObserver<IPC.RideCharge> rideChargeStreamObserver = getRideChargeStreamObserver(t, stub);
                rideChargeStreamObserver.onNext(rideCharge);
                channel.awaitTermination(2, TimeUnit.SECONDS);
            }
        }
    }

    private static StreamObserver<IPC.RideCharge> getRideChargeStreamObserver(TaxiInfo t,
                                                                              IPCServiceGrpc.IPCServiceStub stub) {
        return stub.coordinateRideStream(new StreamObserver<>() {
            @Override
            public void onNext(IPC.ACK value) {
                if (value.getVote()) {
                    ackRides++;
                    System.out.println("[Ride] Received an ACK from " + t.getId());
                } else {
                    System.out.println("[Ride] Received a NACK from " + t.getId());
                }

                syncLogicalClock(value, t);
            }

            @Override
            public void onError(Throwable t1) {

            }

            @Override
            public void onCompleted() {

            }
        });
    }

    public static void syncLogicalClock(IPC.ACK value, TaxiInfo t) {
        LogicalClock serverClock = new LogicalClock(RAND_CLOCK_OFFSET);
        serverClock.setLogicalClock(value.getLogicalClock());

        if (logicalClock.getLogicalClock() <= value.getLogicalClock())
            logicalClock.setLogicalClock(value.getLogicalClock());

        logicalClock.increment();

        System.out.println("ACK/NACK sent from " + t.getId()
                + " at " + serverClock.printCalendar()
                + ", received at " + logicalClock.printCalendar());
    }

    public int getAckRides() {
        return ackRides;
    }

    public void resetAckRides() {
        ackRides = 0;
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