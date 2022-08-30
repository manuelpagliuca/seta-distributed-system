/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. in Computer Science @UNIMI A.Y. 2021/2022 */
package Taxi.Statistics.Statistics;

import java.util.List;

import static Misc.Utility.printCalendar;

public class TotalStatisticsInfo extends StatisticsInfo {
    public TotalStatisticsInfo(List<Double> avgMeasurements, double traveledKms, int accomplishedRides, int taxiID, double taxiBattery) {
        super(avgMeasurements, traveledKms, accomplishedRides, taxiID, taxiBattery);
    }

    public TotalStatisticsInfo(StatisticsInfo info) {
        super(info);
    }

    @Override
    public String toString() {
        return String.format(
                "Pollution measurements (PM10) total average: " + listAvgPollutionLevels.get(0) + "\n" +
                        "Total kilometers traversed average: %.2f\n" +
                        "Total average rides made: %d\n" +
                        "Total average battery levels: %.2f\n" +
                        "Record took at: " + printCalendar(timestamp),
                traveledKms, accomplishedRides, taxiBattery);
    }
}
