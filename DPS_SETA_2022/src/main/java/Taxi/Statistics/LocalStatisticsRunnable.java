package Taxi.Statistics;

import Taxi.Data.TaxiInfo;
import Taxi.Statistics.Simulators.Measurement;
import Utility.Utility;
import jakarta.ws.rs.client.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static Utility.Utility.GSON;

public class LocalStatisticsRunnable implements Runnable {
    private final PollutionBuffer pollutionBuffer;
    private final ArrayList<Double> listAvgPollution = new ArrayList<>();
    private final TaxiInfo thisTaxi;
    private static final String STAT_PATH = "/stats";
    private final String ADMIN_SERVER_URL;
    private final Client client;
    AtomicBoolean sendData = new AtomicBoolean(false);


    public LocalStatisticsRunnable(PollutionBuffer pollutionBuffer, TaxiInfo thisTaxi, String adminServerUrl,
                                   Client client) {
        this.pollutionBuffer = pollutionBuffer;
        this.thisTaxi = thisTaxi;
        this.ADMIN_SERVER_URL = adminServerUrl;
        this.client = client;
    }

    @Override
    public void run() {
        List<Measurement> slidingWindow;

        Thread postRequestClock = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                sendData.set(true);
            }
        });
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
            System.out.println("Added measurement. measurements: " + avgMeasurements);

            if (sendData.get()) {
                StatisticsInfo statisticsInfo = new StatisticsInfo(
                        listAvgPollution,
                        thisTaxi.getKmTraveled(),
                        thisTaxi.getAccomplishedRides(),
                        thisTaxi.getId(),
                        thisTaxi.getBattery());
                System.out.println("Send the info to the server");
                postStatistics(statisticsInfo);
                sendData.set(false);
                listAvgPollution.clear();
            }
        }
    }

    private void postStatistics(StatisticsInfo statisticsInfo) {
        String serverInitInfos = Utility.postRequest(client,
                ADMIN_SERVER_URL + STAT_PATH, GSON.toJson(statisticsInfo));
        System.out.println(serverInitInfos);
    }
}
