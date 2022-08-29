/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. of Computer Science @UNIMI A.Y. 2021/2022 */
package Taxi.gRPC;

import Taxi.Structures.TaxiInfo;
import io.grpc.stub.StreamObserver;
import org.example.grpc.IPC;
import org.example.grpc.IPCServiceGrpc;

import java.util.concurrent.atomic.AtomicInteger;

public class GrpcRunnable implements Runnable {
    public static class GrpcMessages {
        private IPC.RideCharge rideCharge = null;
        private IPC.Infos infos = null;
        private IPC.RechargeProposal rechargeProposal = null;
    }

    private static final AtomicInteger ackRides = new AtomicInteger(0);
    //private volatile static Integer ackRides = 0;
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

    public GrpcRunnable(TaxiInfo t, IPC.RechargeProposal rechargeProposal, IPCServiceGrpc.IPCServiceStub stub) {
        GrpcRunnable.t = t;
        this.grpcMessages.rechargeProposal = rechargeProposal;
        this.stub = stub;
    }

    @Override
    public void run() {
        if (grpcMessages.rideCharge != null) {
            StreamObserver<IPC.RideCharge> clientStream = getRideChargeStreamObserver(stub);
            //System.out.println("[Ride][Send] Infos to " + t.getId());
            clientStream.onNext(grpcMessages.rideCharge);
        } else if (grpcMessages.infos != null) {
            // TODO implement infos stream
            StreamObserver<IPC.Infos> clientStream = getInfosStreamObserver(stub);
            //System.out.println("[Infos][Send] Infos to " + t.getId());
            clientStream.onNext(grpcMessages.infos);
        } else if (grpcMessages.rechargeProposal != null) {
            StreamObserver<IPC.RechargeProposal> rechargeStream = getRechargeProposalStreamObserver(stub);
            //System.out.println("[Recharge][Send] Proposal to " + t.getId());
            rechargeStream.onNext(grpcMessages.rechargeProposal);
        }
    }

    private static StreamObserver<IPC.RechargeProposal> getRechargeProposalStreamObserver(IPCServiceGrpc.IPCServiceStub stub) {
        return stub.coordinateRechargeStream(new StreamObserver<>() {
            @Override
            public void onNext(IPC.ACK value) {
                if (value.getVote()) {
                    ackRides.incrementAndGet();
                    //System.out.println("[Ride][Receive] ACK from " + t.getId());
                } else {
                    //System.out.println("[Ride][Receive] NACK from " + t.getId());
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });
    }

    private static StreamObserver<IPC.RideCharge> getRideChargeStreamObserver(IPCServiceGrpc.IPCServiceStub stub) {
        return stub.coordinateRideStream(new StreamObserver<>() {
            @Override
            public void onNext(IPC.ACK value) {
                if (value.getVote()) {
                    ackRides.incrementAndGet();
                    //System.out.println("[Ride][Receive] ACK from " + t.getId());
                } else {
                    //System.out.println("[Ride][Receive] NACK from " + t.getId());
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
        return stub.goodbye(new StreamObserver<>() {
            @Override
            public void onNext(IPC.ACK value) {
                if (value.getVote()) {
                    //synchronized (ackRides) {
                    ackRides.incrementAndGet();
                    //}
                    //System.out.println("[Ride][Receive] ACK from " + t.getId());
                } else {
                    //System.out.println("[Ride][Receive] NACK from " + t.getId());
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
        return ackRides.get();
    }

    public static void resetACKS() {
        ackRides.set(0);
    }

}
