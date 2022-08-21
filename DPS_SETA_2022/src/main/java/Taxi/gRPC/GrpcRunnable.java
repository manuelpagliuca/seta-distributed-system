package Taxi.gRPC;

import Taxi.Data.TaxiInfo;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.example.grpc.IPC;
import org.example.grpc.IPCServiceGrpc;

public class GrpcRunnable implements Runnable {
    public static class GrpcMessages {
        private IPC.RideCharge rideCharge = null;
        private IPC.Infos infos = null;
    }

    private volatile static Integer ackRides = 0;
    private final GrpcMessages grpcMessages = new GrpcMessages();
    private final IPCServiceGrpc.IPCServiceStub stub;
    private static TaxiInfo t = null;

    public GrpcRunnable(TaxiInfo t, IPC.RideCharge rideCharge, IPCServiceGrpc.IPCServiceStub stub) {
        GrpcRunnable.t = t;
        this.grpcMessages.rideCharge = rideCharge;
        this.stub = stub;
    }

    public GrpcRunnable(TaxiInfo t, IPC.Infos infos, IPCServiceGrpc.IPCServiceStub stub) {
        GrpcRunnable.t = t;
        this.grpcMessages.infos = infos;
        this.stub = stub;
    }

    @Override
    public void run() {
        if (grpcMessages.rideCharge != null) {
            StreamObserver<IPC.RideCharge> clientStream = getRideChargeStreamObserver(stub);
            System.out.println("[Ride][Send] Infos to " + t.getId());
            clientStream.onNext(grpcMessages.rideCharge);
        } else if (grpcMessages.infos != null) {
            // TODO implement infos stream
            StreamObserver<IPC.Infos> clientStream = getInfosStreamObserver(stub);
            System.out.println("[Infos][Send] Infos to " + t.getId());
            clientStream.onNext(grpcMessages.infos);
        }
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

    private static StreamObserver<IPC.Infos> getInfosStreamObserver(IPCServiceGrpc.IPCServiceStub stub) {
        return stub.goodbye(new StreamObserver<IPC.ACK>() {
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
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });
    }

    public static int getACKs() {
        return ackRides;
    }

    public static void resetACKS() {
        ackRides = 0;
    }

}
