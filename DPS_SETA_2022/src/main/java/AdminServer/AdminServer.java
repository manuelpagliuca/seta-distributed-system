/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. in Computer Science @UNIMI A.Y. 2021/2022 */
package AdminServer;

import AdminServer.Workers.ServerLoggerThread;
import Taxi.Structures.TaxiInfo;
import Taxi.Statistics.Statistics.AvgStatisticsInfo;
import Taxi.Statistics.Statistics.StatisticsInfo;
import Taxi.Statistics.Statistics.TotalStatisticsInfo;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.sql.Timestamp;
import java.util.*;

import static Misc.Utility.genTaxiInitialPosition;

/*
 * The administrator server class manages the taxis (clients)
 * ---------------------------------------------------------------------------------
 * The class is defined as a singleton so that it guarantees an easier and global
 * scope access to the same instance inside the project.
 *
 * It manages the taxis of the smartcity, implement the HTTP request from the
 * services and do the analysis for the received measurements.
 */
public class AdminServer {
    private static final String HOST = "localhost";
    private static final int PORT = 9001;
    private static AdminServer instance = null;
    private static final ArrayList<TaxiInfo> taxis = new ArrayList<>();
    private static final Object newTaxiArrived = new Object();
    private static final Map<Integer, ArrayList<StatisticsInfo>> taxiLocalStatistics = new HashMap<>();

    public AdminServer() {
    }

    public static AdminServer getInstance() {
        if (instance == null) {
            instance = new AdminServer();
        }
        return instance;
    }

    public static void main(String[] args) {
        ResourceConfig config = new ResourceConfig();
        config.register(AdminServerServices.class);
        String serverAddress = "http://" + HOST + ":" + PORT;
        HttpServer restServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(serverAddress), config);
        ServerLoggerThread serverLoggerThread = new ServerLoggerThread(newTaxiArrived);
        serverLoggerThread.start();

        try {
            restServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Add taxi process information inside the server and return updated infos
     * ---------------------------------------------------------------------------------
     * Given the Clients.Taxi.Taxi information parsed from a JSON file it will add
     * the taxi information inside the static taxi list of the server.
     *
     * The ID of the taxi list if already present will be randomly generated again
     * until a valid one will be available, that will be used.
     *
     * Also, the district with the relative starting position of the taxi will both
     * be generated here and added to a TaxiInfo object that will be returned to the
     * client. In this way the client will know the valid ID, the district and the
     * position inside the smart city.
     */
    public TaxiInfo addTaxi(TaxiInfo info) {
        assert (info != null);
        TaxiInfo newTaxi = new TaxiInfo();

        int newID = info.getId();
        boolean genNewID = false;

        synchronized (taxis) {
            for (TaxiInfo e : taxis) {
                if (e.getId() == newID) {
                    genNewID = true;
                    break;
                }
            }
        }

        if (genNewID) {
            final int validID = genValidID();
            newTaxi.setId(validID);
        } else {
            newTaxi.setId(newID);
        }

        newTaxi.setDistrict(genRandomDistrict());
        newTaxi.setPosition(genTaxiInitialPosition(newTaxi.getDistrict()));
        newTaxi.setGrpcPort(info.getGrpcPort());
        newTaxi.setAdministratorServerAddress(info.getAdministratorServerAddress());

        synchronized (taxis) {
            taxis.add(newTaxi);
        }

        synchronized (newTaxiArrived) {
            newTaxiArrived.notify();
        }

        return newTaxi;
    }

    /* Remove Taxi by a given ID
     * ---------------------------------------------------------------------------------
     * This function perform a DELETE operation through a REST endpoint which is
     * exposes in the services class.
     *
     * It could also signal the relative taxi process through gRPC to end is
     * execution.
     */
    public synchronized boolean removeTaxi(int taxiID) {
        for (TaxiInfo e : taxis) {
            if (e.getId() == taxiID) {
                // Could be used a gRPC to the taxi for signaling him to quit the process
                taxis.remove(e);
                synchronized (newTaxiArrived) {
                    newTaxiArrived.notify();
                }
                return true;
            }
        }
        return false;
    }

    /// Utility
    // Print all the data entries of taxis in the smart city (debug)
    public void printAllTaxis() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println("*--" + timestamp + "--*");

        if (taxis.isEmpty()) {
            System.out.println("| There are no taxi.\t\t|");
        }

        for (TaxiInfo e : taxis)
            System.out.println("| id=" + e.getId() + ", gRPC=" + e.getGrpcPort() + "\t\t\t|");

        System.out.println("*---------------------------*");
    }

    /// Utility

    /*
     * Generate a valid ID (not taken/available) for a taxi in the smart city
     * ---------------------------------------------------------------------------------
     * Generate randomly a taxi ID from the integer range [1,100], then it checks
     * inside the hashmap of the registered taxis if the generated ID is already present,
     * and it will continuously generate a new id until the new ID is not taken.
     */
    private int genValidID() {
        Random random = new Random();
        int newID = random.nextInt(1, 100 + 1);

        ArrayList<Integer> ids = new ArrayList<>();

        for (TaxiInfo e : taxis)
            ids.add(e.getId());

        while (ids.contains(newID))
            newID = random.nextInt(1, 100 + 1);

        return newID;
    }

    // Generate a random district inside the integer range [1,4]
    private int genRandomDistrict() {
        Random rnd = new Random();
        final int lowerBound = 1;
        final int upperBound = 4;

        // TODO: revert back to original functioning
        return 2; // for testing gRPC
        //return rnd.nextInt(lowerBound, upperBound + 1);
    }

    // Check the presence of a taxi in the smartcity
    public synchronized boolean taxiIsPresent(int taxiID) {
        for (TaxiInfo t : taxis)
            if (t.getId() == taxiID)
                return true;
        return false;
    }

    /* Add the local statistics
     * ---------------------------------------------------------------------------------
     * If necessary (there is no measurement for a given taxi, i.e. it is the first
     * measurement) creates new element in the hashmap of the statistics.
    */
    public synchronized void addLocalStatistics(StatisticsInfo info) {
        if (taxiLocalStatistics.get(info.getTaxiID()) == null) {
            taxiLocalStatistics.put(info.getTaxiID(), new ArrayList<>());
        }
        taxiLocalStatistics.get(info.getTaxiID()).add(info);
    }

    /* Get the average of the latest N stats
     * ---------------------------------------------------------------------------------
     * Get the stats list of a given taxi ID, then sort it by timestamp non-decremental
     * order. Create the list from the most recent timestamp (which is the long with
     * the highest value), the list will stop adding element when the iterator
     * reaches the value of N.
     *
     * For each list of measurements (one list for each taxi) we get the average
     * of these measurements for each stat measurement. Then computes the averages of the
     * averages of these lists (for all stat measurements).
     */
    public AvgStatisticsInfo getAveragesNStats(int taxiID, int lastNstats) {
        // Get the list of statistics for a given taxi ID
        ArrayList<StatisticsInfo> taxiStats = getLocalTaxiStats(taxiID);
        // Sort the list by timestamp
        taxiStats.sort(Comparator.comparingLong(StatisticsInfo::getTimestamp));

        // Create the list from the most recent timestamp (the highest value)
        ArrayList<StatisticsInfo> lastNstatsList = new ArrayList<>();
        for (int i = 0; i < lastNstats; i++)
            lastNstatsList.add(taxiStats.get(i));

        // Get the average of each list of measurement averages
        // then take the average of the averages
        double avgPollutionForAllStats = 0.0D;
        for (StatisticsInfo statisticsInfo : lastNstatsList) {
            List<Double> listAvgPollutionLevels = statisticsInfo.getListAvgPollutionLevels();
            double avgPollutionLevelsForSingleStat = 0.0D;

            for (Double avg : listAvgPollutionLevels)
                avgPollutionLevelsForSingleStat += avg;

            avgPollutionLevelsForSingleStat /= listAvgPollutionLevels.size();
            avgPollutionForAllStats += avgPollutionLevelsForSingleStat;
        }

        // Overall average of
        avgPollutionForAllStats /= lastNstatsList.size();

        // Take the averages of the other local statistics
        double avgTraveledKms = 0.0D;
        int avgAccomplishedRides = 0;
        double avgBatteryLevel = 0.0D;

        for (StatisticsInfo statisticsInfo : lastNstatsList) {
            avgTraveledKms += statisticsInfo.getTraveledKms();
            avgAccomplishedRides += statisticsInfo.getAccomplishedRides();
            avgBatteryLevel += statisticsInfo.getTaxiBattery();
        }

        avgTraveledKms /= lastNstatsList.size();
        avgAccomplishedRides /= lastNstatsList.size();
        avgBatteryLevel /= lastNstatsList.size();

        // Return the average of the pollution as a single element list
        ArrayList<Double> singlePollutionAvg = new ArrayList<>();
        singlePollutionAvg.add(avgPollutionForAllStats);

        //System.out.println(avgLastNstats); // debug

        return new AvgStatisticsInfo(
                singlePollutionAvg,
                avgTraveledKms, avgAccomplishedRides,
                taxiID, avgBatteryLevel);
    }

    /* Get the average of all the taxis between two given timestamps
     * ---------------------------------------------------------------------------------
     * It computes the average of all the statistics between two timestamps,
     * the method is the same as the 'getAveragesNStats', but considers the list of all
     * taxis clamped between the two given timestamps.
     *
     * It iterates over all taxis, then over all stats of each taxis, and then on
     * the list of pollution for each state, so the performance of this algorithm are
     * pretty bad (this is a design problem of how the data for measurements
     * are structured).
     */
    public TotalStatisticsInfo getAllTaxisAvgStats(long timestamp1, long timestamp2) {
        TotalStatisticsInfo noMeasurements = checkMeasurementsPresence();
        if (noMeasurements != null) return noMeasurements;

        double totalAvgPollution = 0.0D;
        double totalAvgTraveledKms = 0.0D;
        int totalAvgAccomplishedRides = 0;
        double totalBatteryLevels = 0.0D;

        int numberOfTaxis;
        synchronized (taxiLocalStatistics) {
            numberOfTaxis = taxiLocalStatistics.size();
            for (Map.Entry<Integer, ArrayList<StatisticsInfo>> e : taxiLocalStatistics.entrySet()) {
                double avgPollutionTaxi = 0.0D;
                double avgTraveledKmsTaxi = 0.0D;
                int avgAccomplishedRidesTaxi = 0;
                double avgBatteryLevelsTaxi = 0.0D;

                ArrayList<StatisticsInfo> listOfAllStats = e.getValue();
                int consideredMeasurements = 0;

                for (StatisticsInfo statsTaxi : listOfAllStats) {
                    double avgPollutionMeasurement = 0.0D;
                    if (statsTaxi.getTimestamp() >= timestamp1 && statsTaxi.getTimestamp() <= timestamp2) {
                        // Avg. Pollution level for a single measurement list
                        for (Double m : statsTaxi.getListAvgPollutionLevels()) {
                            avgPollutionMeasurement += m;
                        }
                        avgPollutionMeasurement /= statsTaxi.getListAvgPollutionLevels().size();
                        avgPollutionTaxi += avgPollutionMeasurement;
                        avgTraveledKmsTaxi += statsTaxi.getTraveledKms();
                        avgAccomplishedRidesTaxi += statsTaxi.getAccomplishedRides();
                        avgBatteryLevelsTaxi += statsTaxi.getTaxiBattery();
                        consideredMeasurements++;
                        //System.out.println("Avg. Pollution measurement " + avgPollutionMeasurement);
                    }
                }

                // Compute the average for each taxi
                avgPollutionTaxi /= consideredMeasurements;
                avgTraveledKmsTaxi /= consideredMeasurements;
                avgAccomplishedRidesTaxi /= consideredMeasurements;
                avgBatteryLevelsTaxi /= consideredMeasurements;

                //System.out.println("Avg. Pollution Taxi " + e.getKey() + avgPollutionTaxi);
                //System.out.println("Number of considered measurements (timestamp valid) " + consideredMeasurements);
                // Add each taxi average to the total
                totalAvgPollution += avgPollutionTaxi;
                totalAvgTraveledKms += avgTraveledKmsTaxi;
                totalAvgAccomplishedRides += avgAccomplishedRidesTaxi;
                totalBatteryLevels += avgBatteryLevelsTaxi;
            }

        }

        /// Compute the total averages
        // Single valued list for total pollutions
        totalAvgPollution /= numberOfTaxis;
        //System.out.println("Total Avg. Pollution " + totalAvgPollution + ",over all the " + taxiLocalStatistics.size() + " taxis");
        ArrayList<Double> singleValuedAvg = new ArrayList<>();
        singleValuedAvg.add(totalAvgPollution);
        totalAvgAccomplishedRides /= numberOfTaxis;
        totalAvgTraveledKms /= numberOfTaxis;
        totalBatteryLevels /= numberOfTaxis;

        // Re-use the taxiID as error code (-1, means fine)
        int errorCode = -1;
        if (totalAvgPollution == 0.0) {
            errorCode = -9999;
        }

        return new TotalStatisticsInfo(singleValuedAvg,
                totalAvgTraveledKms,
                totalAvgAccomplishedRides,
                errorCode, totalBatteryLevels);
    }

    // Return the list of statistics given a taxi ID
    public ArrayList<StatisticsInfo> getLocalTaxiStats(int taxiID) {
        ArrayList<StatisticsInfo> taxiStats = new ArrayList<>();
        synchronized (taxiLocalStatistics) {
            for (Map.Entry<Integer, ArrayList<StatisticsInfo>> e : taxiLocalStatistics.entrySet())
                if (e.getKey() == taxiID)
                    taxiStats = e.getValue();
        }
        return taxiStats;
    }

    // Check if there are measurements register in the system
    private synchronized static TotalStatisticsInfo checkMeasurementsPresence() {
        if (taxiLocalStatistics.isEmpty()) {
            int errorCode = -7777;
            return new TotalStatisticsInfo(null, 0, 0,
                    errorCode, 0);
        }
        return null;
    }

    /// Getters & Setters

    // Get a clone of the taxi list
    public static ArrayList<TaxiInfo> getTaxis() {
        return (ArrayList<TaxiInfo>) taxis.clone();
    }
}