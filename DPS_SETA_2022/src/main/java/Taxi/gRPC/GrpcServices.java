package Taxi.gRPC;

import Misc.Utility;
import Taxi.Structures.LogicalClock;
import Taxi.Structures.TaxiInfo;
import Taxi.Structures.TaxiSchema;

import io.grpc.stub.StreamObserver;
import org.example.grpc.IPC;
import org.example.grpc.IPCServiceGrpc;

import java.util.ArrayList;

import static Taxi.Taxi.CLOCK_OFFSET;

/* GrpcServices
 * ------------------------------------------------------------------------------
 * Contains the implementation of the gRPC services that handles the taxis in
 * the smartcity.
 */
public class GrpcServices extends IPCServiceGrpc.IPCServiceImplBase {
    private final TaxiInfo thisTaxi;
    private final ArrayList<TaxiInfo> otherTaxis;
    private LogicalClock logicalClock;

    public GrpcServices(TaxiSchema taxiSchema) {
        this.thisTaxi = taxiSchema.getTaxiInfo();
        this.otherTaxis = taxiSchema.getTaxis();
    }

    /*
     * Present (the requester) to the already present taxis in the smart city (sync)
     * ------------------------------------------------------------------------------
     * This synchronized method takes care of receiving and handling the new taxi
     * request. It checks for the presence of the taxi within the list of other taxis,
     * once it passes the check it responds with an ACK to the requester.
     */
    @Override
    public void present(IPC.Infos request, StreamObserver<IPC.ACK> responseObserver) {
        TaxiInfo clientTaxi = new TaxiInfo(request);
        final boolean taxiIsNew = otherTaxis.isEmpty() || !otherTaxis.contains(clientTaxi);

        if (taxiIsNew) {
            otherTaxis.add(clientTaxi);
            sendACKAndCompleteStream(responseObserver, IPC.ACK.newBuilder());
            return;
        }
        sendNACKAndCompleteStream(responseObserver, IPC.ACK.newBuilder());
    }

    /*
     * Remove the current taxi (the requester) from the list of the other taxis (sync)
     * ------------------------------------------------------------------------------
     * This synchronized method takes care of receiving and managing the farewell of
     * a taxi already registered in the smart city. The receiver will remove the taxi
     * from its list and respond with a formal ACK.
     */
    @Override
    public void goodbye(IPC.Infos request, StreamObserver<IPC.ACK> responseObserver) {
        TaxiInfo clientTaxi = new TaxiInfo(request);
        otherTaxis.removeIf(t -> clientTaxi.getId() == t.getId());
        sendACKAndCompleteStream(responseObserver, IPC.ACK.newBuilder());
    }

    /*
     * Handle the mutual exclusion for the recharge station (async)
     * ------------------------------------------------------------------------------
     * This method is an implementation of the Ricart & Agrawala distributed mutual
     * exclusion algorithm. The explicit request of the project requires ensuring synchrony
     * through Lamport's algorithm (we are using a stream).
     *
     * Taxis requesting access to the critical section send a broadcast request message,
     * this message contains its own timestamp.
     *
     * In this method each receiver who is interested in the request compares its own
     * timestamp with that of the requestor, in the case where the requestor has the
     * smallest timestamp then we will synchronize with its clock (and increment by its
     * offset) and send an ACK.
     *
     * In other cases where the taxi is not requesting the critical section, is in
     * another district or has a smaller timestamp, a NACK will be sent.
     */
    @Override
    public StreamObserver<IPC.RechargeProposal> coordinateRechargeStream(StreamObserver<IPC.ACK> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(IPC.RechargeProposal request) {
                LogicalClock clientLogicalClock = getLogicalClockFromRequest(request);
                assert thisTaxi != null;
                final boolean sameDistrict = thisTaxi.getDistrict() == request.getTaxi().getDistrict();

                if (!sameDistrict) {
                    sendACKAndCompleteStream(responseObserver, IPC.ACK.newBuilder());
                    return;
                }

                // If the taxi is inside the critical section
                if (thisTaxi.isRecharging()) {
                    sendNACKAndCompleteStream(responseObserver, IPC.ACK.newBuilder());
                    return;
                }

                if (thisTaxi.wantsToRecharge()) {
                    if (!thisTaxi.isRecharging()) {
                        if (clientLogicalClock.getLogicalClock() < logicalClock.getLogicalClock()) {
                            sendACKAndCompleteStream(responseObserver, IPC.ACK.newBuilder());
                            return;
                        } else if (clientLogicalClock.getLogicalClock() > logicalClock.getLogicalClock()) {
                            logicalClock.setLogicalClock(clientLogicalClock.getLogicalClock());
                            logicalClock.increment();
                            sendNACKAndCompleteStream(responseObserver, IPC.ACK.newBuilder());
                            return;
                        } else {
                            // Enforce Total Order
                            if (request.getTaxi().getId() < thisTaxi.getId()) {
                                sendACKAndCompleteStream(responseObserver, IPC.ACK.newBuilder());
                                return;
                            } else {
                                sendNACKAndCompleteStream(responseObserver, IPC.ACK.newBuilder());
                            }
                            return;
                        }
                    }
                }
                // This taxi is not interested in recharging
                sendACKAndCompleteStream(responseObserver, IPC.ACK.newBuilder());
            }

            private LogicalClock getLogicalClockFromRequest(IPC.RechargeProposal request) {
                LogicalClock requestLogicalClock = new LogicalClock(CLOCK_OFFSET);
                requestLogicalClock.setLogicalClock(request.getTaxi().getLogicalClock());
                return requestLogicalClock;
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        };
    }

    /*
     * Handle the ride coordination between multiple taxis (async)
     * ------------------------------------------------------------------------------
     * This service allows for ride management; taxi applicants (can be multiple) for
     * a given ride initiate an election. They will send a broadcast election request
     * to all of them.
     *
     * On the receiving end, the election is resolved through this function. The
     * receiver performs a comparison of its own information with that of the requestor
     * that is encapsulated in the request itself.
     *
     * The receiving taxi will send an ACK to the requester whenever he or she has a
     * Euclidean distance to the sartino point of the lower run. In the case where the
     * distance is equal it will try to discriminate by favoring the taxi with the higher
     * battery levels. In the rare case that in addition to the Euclidean distance also
     * the battery levels coincide, then the taxi with larger ID will be favored.
     *
     * In all other cases, that is, where the receiver turns out to be a better candidate,
     * it will send a NACK to the requester.
     *
     * This is an implementation of the Bully election algorithm, finally the algorithm
     * that receives ACKs from all taxis will win the election and take charge of the
     * ride.
     *
     * Note: The request is sent in broadcast (and in parallel) to all taxis, so even
     * taxis that do not participate in the election will receive the request, they will
     * regardless respond with an ACK.
     */
    @Override
    public StreamObserver<IPC.RideCharge> coordinateRideStream(StreamObserver<IPC.ACK> responseObserver) {
        return new StreamObserver<>() {
            @Override

            public void onNext(IPC.RideCharge request) {
                int[] passengerPos = new int[2];
                passengerPos[0] = request.getDestinationPosition(0);
                passengerPos[1] = request.getDestinationPosition(1);

                final double clientTaxiDistance = request.getDistanceToDestination();
                final double serverTaxiDistance = Utility.euclideanDistance(thisTaxi.getPosition(), passengerPos);

                final boolean differentDistrict = thisTaxi.getDistrict() != request.getTaxi().getDistrict();
                final boolean isRecharging = thisTaxi.isRecharging();
                final boolean isRiding = thisTaxi.isRiding();
                final boolean tripleCondition = differentDistrict || isRecharging || isRiding;

                if (tripleCondition) {
                    sendACKAndCompleteStream(responseObserver, IPC.ACK.newBuilder());
                    return;
                }

                if (serverTaxiDistance > clientTaxiDistance) {
                    sendACKAndCompleteStream(responseObserver, IPC.ACK.newBuilder());
                    return;
                } else if (serverTaxiDistance == clientTaxiDistance) {
                    if (thisTaxi.getBattery() < request.getTaxi().getBattery()) {
                        sendACKAndCompleteStream(responseObserver, IPC.ACK.newBuilder());
                        return;
                    } else if (thisTaxi.getBattery() == request.getTaxi().getBattery()) {
                        if (thisTaxi.getId() < request.getTaxi().getId()) {
                            sendACKAndCompleteStream(responseObserver, IPC.ACK.newBuilder());
                            return;
                        }
                    }
                }
                // This taxi has a better distance, battery level or ID value than the requester
                sendNACKAndCompleteStream(responseObserver, IPC.ACK.newBuilder());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {

            }
        };
    }

    // Sends an ACK to the receiver (+ the ID of the receiver/server taxi)
    private static void sendACKAndCompleteStream(StreamObserver<IPC.ACK> responseObserver, IPC.ACK.Builder thisTaxi) {
        responseObserver.onNext(thisTaxi.setId(thisTaxi.getId()).setVote(true).build());
        responseObserver.onCompleted();
    }

    // Sends a NACK to the receiver (+ the ID of the receiver/server taxi)
    private static void sendNACKAndCompleteStream(StreamObserver<IPC.ACK> responseObserver, IPC.ACK.Builder thisTaxi) {
        responseObserver.onNext(thisTaxi.setId(thisTaxi.getId()).setVote(false).build());
        responseObserver.onCompleted();
    }

    public void setClock(LogicalClock logicalClock) {
        this.logicalClock = logicalClock;
    }
}
