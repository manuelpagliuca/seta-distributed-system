package grpcTest;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.example.grpc.IPC;
import org.example.grpc.IPCServiceGrpc;

import java.util.concurrent.TimeUnit;

public class Client {

    public static void main(String[] args) {
        final ManagedChannel channel = ManagedChannelBuilder
                .forTarget("localhost:53170")
                .usePlaintext().build();

        IPCServiceGrpc.IPCServiceStub stub = IPCServiceGrpc.newStub(channel);

        StreamObserver<IPC.Proposal> serverStream = stub.coordinate(new StreamObserver<>() {
            @Override
            public void onNext(IPC.Response msg) {


                System.out.println(msg.getStringResponse());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        });
        String msg = "First request from the client";
        System.out.println("Sending the message '" + msg + "' to the server...");
        serverStream.onNext(IPC.Proposal.newBuilder().setStringRequest(msg).build());

        msg = "Second request from the client";
        System.out.println("Sending the message '" + msg + "' to the server...");
        serverStream.onNext(IPC.Proposal.newBuilder().setStringRequest(msg).build());

        msg = "Third request from the client";
        System.out.println("Sending the message '" + msg + "' to the server...");
        serverStream.onNext(IPC.Proposal.newBuilder().setStringRequest(msg).build());

        try {
            //you need this. otherwise the method will terminate before that answers from the server are received
            channel.awaitTermination(10, TimeUnit.SECONDS);
            System.out.println("Terminate");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
