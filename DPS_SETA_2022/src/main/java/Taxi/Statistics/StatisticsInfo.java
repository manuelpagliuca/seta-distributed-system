package Taxi.Statistics;

import java.io.Serializable;
import java.util.Calendar;

import static Utility.Utility.printCalendar;

public class StatisticsInfo implements Serializable {
    protected double avgPollutionLevels;
    protected double traveledKms;
    protected int accomplishedRides;
    protected long timestamp;
    protected int taxiID;
    protected double taxiBattery;

    public StatisticsInfo(double avgMeasurements, double traveledKms, int accomplishedRides,
                          int taxiID, double taxiBattery) {
        this.avgPollutionLevels = avgMeasurements;
        this.traveledKms = traveledKms;
        this.accomplishedRides = accomplishedRides;
        this.taxiID = taxiID;
        this.taxiBattery = taxiBattery;
        timestamp = System.currentTimeMillis();
    }

    public StatisticsInfo(StatisticsInfo info) {
        this.avgPollutionLevels = info.avgPollutionLevels;
        this.traveledKms = info.traveledKms;
        this.accomplishedRides = info.accomplishedRides;
        this.taxiID = info.taxiID;
        this.taxiBattery = info.taxiBattery;
        this.timestamp = info.timestamp;
    }

    public double getAvgPollutionLevels() {
        return avgPollutionLevels;
    }

    public void setAvgPollutionLevels(double avgPollutionLevels) {
        this.avgPollutionLevels = avgPollutionLevels;
    }

    public double getTraveledKms() {
        return traveledKms;
    }

    public void setTraveledKms(double traveledKms) {
        this.traveledKms = traveledKms;
    }

    public double getAccomplishedRides() {
        return accomplishedRides;
    }

    public void setAccomplishedRides(int accomplishedRides) {
        this.accomplishedRides = accomplishedRides;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getTaxiID() {
        return taxiID;
    }

    public void setTaxiID(int taxiID) {
        this.taxiID = taxiID;
    }

    public double getTaxiBattery() {
        return taxiBattery;
    }

    public void setTaxiBattery(double taxiBattery) {
        this.taxiBattery = taxiBattery;
    }

    @Override
    public String toString() {
        return String.format("Taxi ID: %d\n" +
                        "Average pollution measurements: %.2f\n" +
                        "Traveled Kilometers: %.2f\n" +
                        "Accomplished rides: %d\n" +
                        "Battery levels: %.2f\n" +
                        "Record took at: " + printCalendar(timestamp),
                taxiID, avgPollutionLevels, traveledKms, accomplishedRides, taxiBattery);
    }
}
