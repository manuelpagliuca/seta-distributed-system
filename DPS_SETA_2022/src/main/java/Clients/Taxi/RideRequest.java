package Clients.Taxi;

import java.io.Serializable;

public class RideRequest implements Serializable {
    private int rideId;
    private int taxiId;
    private int district;
    private double euclideanDistance;
    private double battery;

    public RideRequest(int taxiId, int rideId, int district, double euclideanDistance, double battery) {
        this.rideId = rideId;
        this.taxiId = taxiId;
        this.district = district;
        this.euclideanDistance = euclideanDistance;
        this.battery = battery;
    }

    public int getDistrict() {
        return district;
    }

    public void setDistrict(int district) {
        this.district = district;
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

    public double getBattery() {
        return battery;
    }

    public void setBattery(double battery) {
        this.battery = battery;
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
