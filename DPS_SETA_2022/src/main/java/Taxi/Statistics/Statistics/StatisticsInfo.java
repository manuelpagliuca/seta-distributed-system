/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. in Computer Science @UNIMI A.Y. 2021/2022 */
package Taxi.Statistics.Statistics;

import java.io.Serializable;
import java.util.List;

import static Misc.Utility.printCalendar;

@SuppressWarnings("unused")
public class StatisticsInfo implements Serializable {
    protected List<Double> listAvgPollutionLevels;
    protected double traveledKms;
    protected int accomplishedRides;
    protected long timestamp;
    protected int taxiID;
    protected double taxiBattery;

    public StatisticsInfo(List<Double> avgMeasurements, double traveledKms, int accomplishedRides,
                          int taxiID, double taxiBattery) {
        this.listAvgPollutionLevels = avgMeasurements;
        this.traveledKms = traveledKms;
        this.accomplishedRides = accomplishedRides;
        this.taxiID = taxiID;
        this.taxiBattery = taxiBattery;
        timestamp = System.currentTimeMillis();
    }

    public StatisticsInfo(StatisticsInfo info) {
        this.listAvgPollutionLevels = info.listAvgPollutionLevels;
        this.traveledKms = info.traveledKms;
        this.accomplishedRides = info.accomplishedRides;
        this.taxiID = info.taxiID;
        this.taxiBattery = info.taxiBattery;
        this.timestamp = info.timestamp;
    }

    public List<Double> getListAvgPollutionLevels() {
        return listAvgPollutionLevels;
    }

    public void setListAvgPollutionLevels(List<Double> listAvgPollutionLevels) {
        this.listAvgPollutionLevels = listAvgPollutionLevels;
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
                        "Average pollution measurements: " + listAvgPollutionLevels.toString() + "\n" +
                        "Traveled Kilometers: %.2f\n" +
                        "Accomplished rides: %d\n" +
                        "Battery levels: %.2f\n" +
                        "Record took at: " + printCalendar(timestamp),
                taxiID, traveledKms, accomplishedRides, taxiBattery);
    }
}
