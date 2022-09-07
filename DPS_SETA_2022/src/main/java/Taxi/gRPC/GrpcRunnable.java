/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. in Computer Science @UNIMI A.Y. 2021/2022 */
package Taxi.gRPC;

import Taxi.Structures.TaxiInfo;

import io.grpc.stub.StreamObserver;

import org.example.grpc.IPC;
import org.example.grpc.IPCServiceGrpc;

import java.util.concurrent.atomic.AtomicInteger;

/*
 * GrpcRunnable
 * ------------------------------------------------------------------------------
 * This Runnable class allows the execution of a thread that manages the stream
 * services (async). You initiate it with the GrpcMessage that you want to deliver
 * to the gRPC server side, the rest will be done by the class it will extract
 * the actual IPC structure from the GrpcMessage and send it through the stub, then
 * it will wait the answer through the appropriate StreamObserver.
 *
 * This class got a static field for the ACKs (which you can reset) that you receive
 * from the streams.
 */
public class GrpcRunnable implements Runnable {
    private static final AtomicInteger ackRides = new AtomicInteger(0);
    private final GrpcMessage grpcMessages;
    private final IPCServiceGrpc.IPCServiceStub stub;
    private static TaxiInfo t = null;

    public GrpcRunnable(TaxiInfo t, GrpcMessage grpcMessages, IPCServiceGrpc.IPCServiceStub stub) {
        GrpcRunnable.t = t;
        this.grpcMessages = grpcMessages;
        this.stub = stub;
    }

    @Override
    public void run() {
        if (grpcMessages.getRideCharge() != null) {
            StreamObserver<IPC.RideCharge> clientStream = getRideChargeStreamObserver(stub);
            System.out.println("[Ride][Send] Infos to " + t.getId());
            clientStream.onNext(grpcMessages.getRideCharge());
        } else if (grpcMessages.getRechargeProposal() != null) {
            StreamObserver<IPC.RechargeProposal> rechargeStream = getRechargeProposalStreamObserver(stub);
            System.out.println("[Recharge][Send] Proposal to " + t.getId());
            rechargeStream.onNext(grpcMessages.getRechargeProposal());
        }/*else if (grpcMessages.getInfos() != null) {
            // TODO implement infos stream
            StreamObserver<IPC.Infos> clientStream = getInfosStreamObserver(stub);
            System.out.println("[Infos][Send] Infos to " + t.getId());
            clientStream.onNext(grpcMessages.getInfos());
        }*/
    }

    // Returns the StreamObserver for the recharge operation.
    private static StreamObserver<IPC.RechargeProposal> getRechargeProposalStreamObserver(
            IPCServiceGrpc.IPCServiceStub stub) {
        return stub.coordinateRechargeStream(new StreamObserver<>() {
            @Override
            public void onNext(IPC.ACK value) {
                if (value.getVote()) {
                    ackRides.incrementAndGet();
                    System.out.println("[Recharge][Receive] ACK from " + t.getId());
                } else {
                    System.out.println("[Recharge][Receive] NACK from " + t.getId());
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

    // Returns the StreamObserver for the handling of the rides
    private static StreamObserver<IPC.RideCharge> getRideChargeStreamObserver(IPCServiceGrpc.IPCServiceStub stub) {
        return stub.coordinateRideStream(new StreamObserver<>() {
            @Override
            public void onNext(IPC.ACK value) {
                if (value.getVote()) {
                    ackRides.incrementAndGet();
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

    /*
    private static StreamObserver<IPC.Infos> getInfosStreamObserver(IPCServiceGrpc.IPCServiceStub stub) {
        return stub.goodbye(new StreamObserver<>() {
            @Override
            public void onNext(IPC.ACK value) {
                if (value.getVote()) {
                    ackRides.incrementAndGet();
                    System.out.println("[Infos][Receive] ACK from " + t.getId());
                } else {
                    System.out.println("[Infos][Receive] NACK from " + t.getId());
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
*/
    public static int getACKs() {
        return ackRides.get();
    }

    public static void resetACKS() {
        ackRides.set(0);
    }

}

