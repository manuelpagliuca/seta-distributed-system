package Taxi.Statistics;

import static Utility.Utility.printCalendar;

public class AvgStatisticsInfo extends StatisticsInfo{

    public AvgStatisticsInfo(double avgMeasurements, double traveledKms, int accomplishedRides, int taxiID, double taxiBattery) {
        super(avgMeasurements, traveledKms, accomplishedRides, taxiID, taxiBattery);
    }

    public AvgStatisticsInfo(StatisticsInfo info) {
        super(info);
    }

    @Override
    public String toString() {
        return String.format("Taxi ID: %d\n" +
                        "Average pollution measurements: %.2f\n" +
                        "Average Traveled Kilometers: %.2f\n" +
                        "Average Accomplished rides: %d\n" +
                        "Average Battery levels: %.2f\n" +
                        "Average Record took at: " + printCalendar(timestamp),
                taxiID, avgPollutionLevels, traveledKms, accomplishedRides, taxiBattery);
    }
}
