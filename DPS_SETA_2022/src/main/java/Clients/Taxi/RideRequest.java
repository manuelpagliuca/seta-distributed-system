package Clients.Taxi;

import java.io.Serializable;

public class RideRequest implements Serializable {
    private int taxiId;
    private int rideId;
    private double euclideanDistance;

    public RideRequest(int taxiId, int rideId, double euclideanDistance) {
        this.taxiId = taxiId;
        this.rideId = rideId;
        this.euclideanDistance = euclideanDistance;
    }

    public int getTaxiId() {
        return taxiId;
    }

    public void setTaxiId(int taxiId) {
        this.taxiId = taxiId;
    }

    public int getRideId() {
        return rideId;
    }

    public void setRideId(int rideId) {
        this.rideId = rideId;
    }

    public double getEuclideanDistance() {
        return euclideanDistance;
    }

    public void setEuclideanDistance(double euclideanDistance) {
        this.euclideanDistance = euclideanDistance;
    }

    @Override
    public String toString() {
        return "taxiID: " + taxiId + " rideID: " + rideId + " distance: " + euclideanDistance;
    }
}
