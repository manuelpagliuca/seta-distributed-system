/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. in Computer Science @UNIMI A.Y. 2021/2022 */
package Taxi.Workers;

import Taxi.Structures.TaxiInfo;
import Taxi.Statistics.PollutionBuffer;
import Taxi.Statistics.Statistics.StatisticsInfo;
import Taxi.Statistics.Simulators.Measurement;
import Misc.Utility;
import jakarta.ws.rs.client.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static Misc.Utility.GSON;

public class LocalStatsThread extends Thread {
    private final PollutionBuffer pollutionBuffer;
    private final ArrayList<Double> listAvgPollution = new ArrayList<>();
    private final TaxiInfo thisTaxi;
    private static final String STAT_PATH = "/stats";
    private final String ADMIN_SERVER_URL;
    private final Client client;
    private final AtomicBoolean sendData = new AtomicBoolean(false);
    private final Thread postRequestClock = new Thread(this::sendEach15sec);

    public LocalStatsThread(TaxiInfo thisTaxi, String adminServerUrl,
                            Client client, PollutionBuffer pollutionBuffer) {
        this.pollutionBuffer = pollutionBuffer;
        this.thisTaxi = thisTaxi;
        this.ADMIN_SERVER_URL = adminServerUrl;
        this.client = client;
    }

    @Override
    public void run() {
        List<Measurement> slidingWindow;

        postRequestClock.start();

        while (!Thread.currentThread().isInterrupted()) {
            synchronized (pollutionBuffer) {
                slidingWindow = pollutionBuffer.readAllAndClean();
            }

            double avgMeasurements = 0;

            for (Measurement m : slidingWindow) {
                avgMeasurements += m.getValue();
            }

            avgMeasurements /= 8;
            listAvgPollution.add(avgMeasurements);

            //System.out.println("Added measurement. measurements: " + avgMeasurements); // debug
            if (sendData.get()) {
                StatisticsInfo statisticsInfo = new StatisticsInfo(
                        listAvgPollution,
                        thisTaxi.getKmTraveled(),
                        thisTaxi.getAccomplishedRides(),
                        thisTaxi.getId(),
                        thisTaxi.getBattery());
                //System.out.println("Send the info to the server"); // debug
                postStatistics(statisticsInfo);
                sendData.set(false);
                listAvgPollution.clear();
            }
        }
    }

    @SuppressWarnings("BusyWait")
    private void sendEach15sec() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            sendData.set(true);
        }
    }

    private void postStatistics(StatisticsInfo statisticsInfo) {
        String serverInitInfos = Utility.postRequest(client,
                ADMIN_SERVER_URL + STAT_PATH, GSON.toJson(statisticsInfo));
        assert (serverInitInfos != null);
        //System.out.println(serverInitInfos); // debug
    }
}
