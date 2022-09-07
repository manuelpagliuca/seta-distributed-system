/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. in Computer Science @UNIMI A.Y. 2021/2022 */
package Taxi.Statistics.Statistics;

import static Misc.Utility.printCalendar;

import java.util.List;

/*
 * AvgStatisticsInfo
 * ------------------------------------------------------------------------------
 * Subclass of StatisticsInfo it will just override the 'toString()' method for
 * getting a proper formatting of the data for the averaged statistics.
 */
public class AvgStatisticsInfo extends StatisticsInfo {
    public AvgStatisticsInfo(List<Double> avgMeasurements, double traveledKms,
                             int accomplishedRides, int taxiID, double taxiBattery) {
        super(avgMeasurements, traveledKms, accomplishedRides, taxiID, taxiBattery);
    }

    public AvgStatisticsInfo(StatisticsInfo info) {
        super(info);
    }

    @Override
    public String toString() {
        return String.format("Taxi ID: %d\n" +
                        "Pollution measurements (PM10) average: " + listAvgPollutionLevels.get(0) + "\n" +
                        "Kilometers traversed average: %.2f\n" +
                        "Average rides made: %d\n" +
                        "Average battery levels: %.2f\n" +
                        "Record took at: " + printCalendar(timestamp),
                taxiID, traveledKms, accomplishedRides, taxiBattery);
    }
}
