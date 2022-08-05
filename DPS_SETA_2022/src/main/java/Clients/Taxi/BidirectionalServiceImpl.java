package Clients.Taxi;

import Server.AdministratorServer;
import com.google.gson.Gson;
import io.grpc.stub.StreamObserver;
import org.example.grpc.BidirectionalMsgServiceGrpc;
import org.example.grpc.BidirectionalMsgServiceOuterClass;

import java.util.ArrayList;
import java.util.HashMap;

public class BidirectionalServiceImpl extends BidirectionalMsgServiceGrpc.BidirectionalMsgServiceImplBase {
    // Key: Ride ID, Value: A list of RideRequest from the various taxis
    private HashMap<Integer, ArrayList<RideRequest>> distancesForRides = new HashMap<>();
    private Gson gson = new Gson();
    private static int counter = 0;

    /* TODO: complete the comment
     * --------------------------
     * TODO: complete the comment
     */
    @Override
    public void rideProposal(BidirectionalMsgServiceOuterClass.ClientRequest request,
                             StreamObserver<BidirectionalMsgServiceOuterClass.ServerResponse> responseObserver) {
        System.out.println(request);
        counter++;

        RideRequest rideRequest = gson.fromJson(request.getStringRequest(), RideRequest.class);
        int rideID = rideRequest.getRideId();

        // If the ID for that ride is not present, create a new entry in the hashmap
        if (distancesForRides.get(rideID) == null)
            distancesForRides.put(rideID, new ArrayList<>());

        distancesForRides.get(rideID).add(rideRequest);

        // TODO: "Synchronization barrier"
        int numberOfTotalRequests =
                AdministratorServer.getInstance()
                        .getNumberOfTaxisInDistrict(rideRequest.getDistrict());

        System.out.println("Number of total request: " + numberOfTotalRequests);
        if (counter == numberOfTotalRequests) {
            RideRequest rideAns = AdministratorServer.getInstance()
                    .assignRide(distancesForRides, rideID, rideRequest.getDistrict());
            // The ride that actually will take ownership
            String serverResponse = gson.toJson(rideAns);

            // sending the response to the client
            System.out.println("The taxi that will take ownership: " + serverResponse);

            BidirectionalMsgServiceOuterClass.ServerResponse response =
                    BidirectionalMsgServiceOuterClass.ServerResponse.newBuilder()
                            .setStringResponse(serverResponse).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

}




