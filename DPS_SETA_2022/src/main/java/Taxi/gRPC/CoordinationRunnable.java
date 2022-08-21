package Taxi.gRPC;

import Taxi.Data.TaxiInfo;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.example.grpc.IPC;
import org.example.grpc.IPCServiceGrpc;

public class CoordinationRunnable implements Runnable {
    private final ManagedChannel channel;
    private final IPC.RideCharge rideCharge;
    private volatile static Integer ackRides = 0;
    private static TaxiInfo t;

    CoordinationRunnable(ManagedChannel channel, IPC.RideCharge presentMsg, TaxiInfo t) {
        this.channel = channel;
        this.rideCharge = presentMsg;
        CoordinationRunnable.t = t;
    }

    @Override
    public void run() {
        IPCServiceGrpc.IPCServiceStub stub = IPCServiceGrpc.newStub(channel);
        StreamObserver<IPC.RideCharge> clientStream = getRideChargeStreamObserver(stub);
        System.out.println("[Ride][Send] Infos to " + t.getId());
        clientStream.onNext(rideCharge);
    }

    public static int getACKs() {
        return ackRides;
    }

    public static void resetACKS() {
        ackRides = 0;
    }

    private static StreamObserver<IPC.RideCharge> getRideChargeStreamObserver(IPCServiceGrpc.IPCServiceStub stub) {
        return stub.coordinateRideStream(new StreamObserver<>() {
            @Override
            public void onNext(IPC.ACK value) {
                if (value.getVote()) {
                    synchronized (ackRides) {
                        ++ackRides;
                    }
                    System.out.println("[Ride][Receive] ACK from " + t.getId());
                } else {
                    System.out.println("[Ride][Receive] NACK from " + t.getId());
                }
                //syncLogicalClock(value, t);
            }

            @Override
            public void onError(Throwable t1) {

            }

            @Override
            public void onCompleted() {

            }
        });
    }
}
