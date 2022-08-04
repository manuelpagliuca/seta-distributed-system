package Clients.Taxi;

import com.google.gson.Gson;
import io.grpc.stub.StreamObserver;
import org.example.grpc.BidirectionalServiceGrpc;
import org.example.grpc.BidirectionalServiceOuterClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class BidirectionalServiceImpl extends BidirectionalServiceGrpc.BidirectionalServiceImplBase {
    private HashMap<Integer, ArrayList<RideRequest>> distancesForRides = new HashMap<>();
    private Gson gson = new Gson();

    @Override
    public StreamObserver<BidirectionalServiceOuterClass.ClientRequest>
    bidirectional(StreamObserver<BidirectionalServiceOuterClass.ServerResponse> responseObserver) {

        //it returns the stream that will be used by the clients to send messages. The client will write on this stream
        return new StreamObserver<>() {
            //receiving a message from the client
            public void onNext(BidirectionalServiceOuterClass.ClientRequest clientRequest) {
                String clientStringRequest = clientRequest.getStringRequest();
                System.out.println("[FROM CLIENT] " + clientStringRequest);

                RideRequest rideRequest = gson.fromJson(clientStringRequest, RideRequest.class);
                int rideID = rideRequest.getRideId();
                if (distancesForRides.get(rideID) == null)
                    distancesForRides.put(rideID, new ArrayList<>());

                distancesForRides.get(rideID).add(rideRequest);

                try {
                    System.out.println("Assigning the ride " + rideID + " to a taxi");
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                // Take the first euclidean distance, then iterate over it
                double minDistance = distancesForRides.get(rideID).get(0).getEuclideanDistance();

                // Discriminate by distance (min)
                for (RideRequest r : distancesForRides.get(rideID)) {
                    if (r.getEuclideanDistance() < minDistance) {
                        minDistance = r.getEuclideanDistance();
                    }
                }

                // Discriminate by ID (min)
                ArrayList<Integer> IDsWithSameDist = new ArrayList<>();
                for (RideRequest r : distancesForRides.get(rideID)) {
                    if (r.getEuclideanDistance() == minDistance)
                        IDsWithSameDist.add(r.getTaxiId());
                }

                int minTaxiID = -1;

                if (IDsWithSameDist.size() != 1) {
                    minTaxiID = Collections.min(IDsWithSameDist);
                } else {
                    minTaxiID = IDsWithSameDist.get(0);
                }

                // The ride that actually will take ownership
                RideRequest rideAnswer = new RideRequest(minTaxiID, rideID, minDistance);
                String serverResponse = gson.toJson(rideAnswer);

                // sending the response to the client
                System.out.println("The taxi that will take ownership: " + serverResponse);

                responseObserver
                        .onNext(BidirectionalServiceOuterClass.ServerResponse.newBuilder()
                                .setStringResponse(serverResponse).build());
            }

            public void onError(Throwable throwable) {
            }

            public void onCompleted() {
            }
        };
    }
}



