package grpcTest;

import Clients.Taxi.RideRequest;
import com.google.gson.Gson;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.grpc.IPC;
import org.example.grpc.IPCServiceGrpc;

public class Client {

    public static void main(String[] args) {
        final ManagedChannel channel = ManagedChannelBuilder
                .forTarget("localhost:8080")
                .usePlaintext().build();

        IPCServiceGrpc.IPCServiceBlockingStub stub = IPCServiceGrpc.newBlockingStub(channel);

        RideRequest rideRequest =
                new RideRequest(1, 1, 1, 1, 1);

        Gson gson = new Gson();
        String body = gson.toJson(rideRequest);

        IPC.Proposal proposal = IPC.Proposal.newBuilder().setStringRequest(body).build();
        IPC.Response response = stub.rideProposal(proposal);

        channel.shutdown();


        System.out.println("Response "+ response.getStringResponse());
    }
}
