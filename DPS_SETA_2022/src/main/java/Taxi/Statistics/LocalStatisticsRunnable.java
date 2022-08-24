package Taxi.Statistics;

import Taxi.Data.TaxiInfo;
import Taxi.Statistics.Simulators.Measurement;
import Utility.Utility;
import jakarta.ws.rs.client.Client;

import java.util.List;

import static Utility.Utility.GSON;

public class LocalStatisticsRunnable implements Runnable {
    private final PollutionBuffer pollutionBuffer;
    private TaxiInfo thisTaxi;
    private static final String STAT_PATH = "/stats";
    private String ADMIN_SERVER_URL;
    private Client client;


    public LocalStatisticsRunnable(PollutionBuffer pollutionBuffer, TaxiInfo thisTaxi, String adminServerUrl,
                                   Client client) {
        this.pollutionBuffer = pollutionBuffer;
        this.thisTaxi = thisTaxi;
        this.ADMIN_SERVER_URL = adminServerUrl;
        this.client = client;
    }

    @Override
    public void run() {
        List<Measurement> window;

        while (!Thread.currentThread().isInterrupted()) {

            synchronized (pollutionBuffer) {
                window = pollutionBuffer.readAllAndClean();
            }

            double avgMeasurements = 0;

            for (Measurement m : window) {
                avgMeasurements += m.getValue();
            }

            avgMeasurements /= 4;
            System.out.println("Avg. measurements: " + avgMeasurements);

            StatisticsInfo statisticsInfo = new StatisticsInfo(
                    avgMeasurements,
                    thisTaxi.getKmTraveled(),
                    thisTaxi.getAccomplishedRides(),
                    thisTaxi.getId(),
                    thisTaxi.getBattery());

            postStatistics(statisticsInfo);

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void postStatistics(StatisticsInfo statisticsInfo) {
        String serverInitInfos = Utility.postRequest(client, ADMIN_SERVER_URL + STAT_PATH,
                GSON.toJson(statisticsInfo));

        System.out.println(serverInitInfos);
    }
}
