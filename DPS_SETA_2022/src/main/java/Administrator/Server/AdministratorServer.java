/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. of Computer Science @UNIMI A.Y. 2021/2022 */
package Administrator.Server;

import Clients.Taxi.RideRequest;
import Clients.Taxi.Taxi;
import Clients.Taxi.TaxiInfo;
import Administrator.Server.Services.AdministratorServerServices;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.*;

/*
 * The administrator server class manages the taxis (clients)
 * ----------------------------------------------------------
 * The class is defined as a singleton so that it guarantees
 * a easier and global scope access to the same instance inside
 * the project.
 *
 * It allows to:
 * TODO: [WIP] I will add infos at the end of the project
 */
public class AdministratorServer {
    private static final String HOST = "localhost";
    private static final int PORT = 9001;
    private static AdministratorServer instance = null;
    private static HashMap<TaxiInfo, ArrayList<TaxiInfo>> taxis = new HashMap<>();

    public AdministratorServer() {
    }

    public static AdministratorServer getInstance() {
        if (instance == null) {
            instance = new AdministratorServer();
        }
        return instance;
    }

    public static void main(String[] args) throws IOException {
        ResourceConfig config = new ResourceConfig();
        config.register(AdministratorServerServices.class);
        String serverAddress = "http://" + HOST + ":" + PORT;
        HttpServer restServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(serverAddress), config);

        ServerTaxisUpdater taxiListsUpdater = new ServerTaxisUpdater();
        GrpcThread grpcServerThread = new GrpcThread();

        try {
            taxiListsUpdater.start();
            restServer.start();
            grpcServerThread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Add taxi process information inside the server and return updated infos
     * ------------------------------------------------------------------------
     * Given the Clients.Taxi.Taxi information parsed from a JSON file it will add the taxi
     * information inside the static taxi list of the server.
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

        synchronized (taxis) {
            if (!taxis.containsValue(info.getId())) {
                newTaxi.setId(info.getId());
            } else {
                int validID = genValidID();
                newTaxi.setId(validID);
            }
        }
        newTaxi.setDistrict(genRandomDistrict());
        newTaxi.setPosition(genTaxiInitialPosition(newTaxi.getDistrict()));
        newTaxi.setGrpcPort(info.getGrpcPort());
        newTaxi.setAdministratorServerAddr(info.getAdministratorServerAddr());

        // TODO: Aggiungere nuovo taxi all'hashmap
        ArrayList<TaxiInfo> otherTaxis = (ArrayList<TaxiInfo>) getTaxis().clone();
        otherTaxis.removeIf(taxi -> taxi == newTaxi);

        synchronized (taxis) {
            taxis.put(newTaxi, otherTaxis);
        }

        return newTaxi;
    }

    /* Remove Taxi
     * ----------------------------------------------------------------------------
     *
     */
    public synchronized boolean removeTaxi(int taxiID) {
        HashMap<TaxiInfo, ArrayList<TaxiInfo>> newTaxiList = new HashMap<>();

        for (Map.Entry<TaxiInfo, ArrayList<TaxiInfo>> e : taxis.entrySet()) {
            if (e.getKey().getId() != taxiID) {
                newTaxiList.put(e.getKey(), e.getValue());
            }
        }

        if (taxis.size() > newTaxiList.size()) {
            taxis = newTaxiList;
            return true;
        }

        return false;
    }
    /// Utility

    // Single thread used from gRPC side
    public synchronized RideRequest assignRide(HashMap<Integer, ArrayList<RideRequest>> distancesForRides,
                                               int rideID, int rideDistrict) {
        ArrayList<RideRequest> pool = distancesForRides.get(rideID);
        int minID = -1;
        double minDistance = -1.0;
        double minBatteryLevel = -1.0;

        // Discriminate by distance (min)
        minDistance = pool.get(0).getEuclideanDistance();
        for (RideRequest r : pool) {
            if (r.getEuclideanDistance() < minDistance) {
                minDistance = r.getEuclideanDistance();
            }
        }

        double finalMinDistance = minDistance;
        pool.removeIf(r -> r.getEuclideanDistance() != finalMinDistance);

        if (pool.size() > 1) {
            // Discriminate by battery level
            minBatteryLevel = pool.get(0).getBattery();
            for (RideRequest r : pool) {
                if (r.getBattery() > minBatteryLevel)
                    minBatteryLevel = r.getBattery();
            }

            double finalMinBatteryLevel = minBatteryLevel;
            pool.removeIf(r -> r.getBattery() != finalMinBatteryLevel);

            if (pool.size() > 1) {
                // Discriminate by taxi ID
                minID = pool.get(0).getTaxiId();

                for (RideRequest r : pool) {
                    if (r.getTaxiId() < minID)
                        minID = r.getTaxiId();
                }

                int finalMinID = minID;
                pool.removeIf(r -> r.getTaxiId() != finalMinID);
            }
        }

        pool.trimToSize();
        minID = pool.get(0).getTaxiId();
        minBatteryLevel = pool.get(0).getBattery();
        minDistance = pool.get(0).getEuclideanDistance();

        return new RideRequest(minID, rideID, rideDistrict, minDistance, minBatteryLevel);
    }

    // Return the number of taxis in a given district
    public int getNumberOfTaxisInDistrict(int district) {
        int numberOfTaxis = 0;
        for (Map.Entry<TaxiInfo, ArrayList<TaxiInfo>> e : taxis.entrySet()) {
            if (e.getKey().getDistrict() == district) {
                numberOfTaxis++;
            }
        }
        return numberOfTaxis;
    }

    // Print all the taxis ID each one with the list of the other taxis on the smart city (debug)
    public void printAllTaxis() {
        for (Map.Entry<TaxiInfo, ArrayList<TaxiInfo>> e : taxis.entrySet()) {
            System.out.println("id= " + e.getKey().getId() + ", taxis = " + e.getValue());
        }
        System.out.println("---");
    }

    // Generate a random district inside the integer range [1,4]
    private int genRandomDistrict() {
        Random rnd = new Random();
        final int lowerBound = 1;
        final int upperBound = 4;

        int district = rnd.nextInt(lowerBound, upperBound + 1);

        return 2; // for testing gRPC
        //return district;
    }

    /*
     * Generate a valid ID (not taken/available) for a taxi in the smart city
     * ----------------------------------------------------------------------
     * Generate randomly a taxi ID from the integer range [1,100], then it checks inside the
     * hashmap of the registered taxis if the generated ID is already present, and it will
     * continuously generate a new id until the new ID is not taken.
     */
    private int genValidID() {
        Random random = new Random();
        int newID = random.nextInt(1, 100 + 1);

        ArrayList<Integer> ids = new ArrayList<>();
        for (Map.Entry<TaxiInfo, ArrayList<TaxiInfo>> e : taxis.entrySet()) {
            ids.add(e.getKey().getId());
        }

        while (ids.contains(newID)) {
            newID = random.nextInt(1, 100 + 1);
        }

        return newID;
    }

    /*
     * Generate the taxi initial position
     * ----------------------------------------------------------------------
     * Given the district the function will generate the taxi coordinate of
     * the relative recharge station (assignment requirement).
     */
    private int[] genTaxiInitialPosition(int district) {
        int[] position = new int[2];

        switch (district) {
            case 1:
                position[0] = 0;
                position[1] = 0;
                break;
            case 2:
                position[0] = 0;
                position[1] = 9;
                break;
            case 3:
                position[0] = 9;
                position[1] = 0;
                break;
            case 4:
                position[0] = 9;
                position[1] = 9;
                break;
        }
        return position;
    }

    /*
     * Update the list of taxis of each taxi on the administrator server
     * ----------------------------------------------------------------------
     * This function makes each taxi (in the administrator server) aware of
     * the presence of other taxis in the smart city.
     */
    public void updateTaxiLists() {
        for (Map.Entry<TaxiInfo, ArrayList<TaxiInfo>> e : taxis.entrySet()) {
            ArrayList<TaxiInfo> allTaxis = (ArrayList<TaxiInfo>) getTaxis().clone();
            allTaxis.removeIf(taxi -> taxi == e.getKey());
            e.setValue(allTaxis);
        }
    }

    /// Getters & Setters
    public int getPort() {
        return PORT;
    }

    public static void setInstance(AdministratorServer instance) {
        AdministratorServer.instance = instance;
    }

    public static ArrayList<TaxiInfo> getTaxis() {
        Set<TaxiInfo> taxiInfos = taxis.keySet();
        return new ArrayList<>(taxiInfos);
    }

    public static void setTaxis(HashMap<TaxiInfo, ArrayList<TaxiInfo>> taxis) {
        AdministratorServer.taxis = taxis;
    }

}
