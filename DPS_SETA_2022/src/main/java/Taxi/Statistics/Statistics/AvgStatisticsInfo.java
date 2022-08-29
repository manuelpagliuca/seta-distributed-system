/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. of Computer Science @UNIMI A.Y. 2021/2022 */
package Taxi.Statistics.Statistics;

import java.util.List;

import static Misc.Utility.printCalendar;

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
