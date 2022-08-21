/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. of Computer Science @UNIMI A.Y. 2021/2022 */
package AdminServer;

import Taxi.Data.TaxiInfo;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.sql.Timestamp;
import java.util.*;

/*
 * The administrator server class manages the taxis (clients)
 * ---------------------------------------------------------------------------------
 * The class is defined as a singleton so that it guarantees
 * a easier and global scope access to the same instance inside
 * the project.
 *
 * It allows to:
 * TODO: [WIP] I will add infos at the end of the project
 */
public class AdminServer {
    private static final String HOST = "localhost";
    private static final int PORT = 9001;
    private static AdminServer instance = null;
    private static final ArrayList<TaxiInfo> taxis = new ArrayList<>();
    private static final Object newTaxiArrived = new Object();

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

        Thread taxisUpdater = new Thread(new ServerLoggerRunnable(newTaxiArrived));
        taxisUpdater.start();

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
            int validID = genValidID();
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

    /// Utility.Utility

    // Print all the taxis ID each one with the list of the other taxis on the smart city (debug)
    public void printAllTaxis() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println("---" + timestamp + "---");

        for (TaxiInfo e : taxis)
            System.out.println("id=" + e.getId() + ", gRPC=" + e.getGrpcPort());

        System.out.println("-----------------------");
    }

    // Generate a random district inside the integer range [1,4]
    private int genRandomDistrict() {
        Random rnd = new Random();
        final int lowerBound = 1;
        final int upperBound = 4;

        int district = rnd.nextInt(lowerBound, upperBound + 1);
        // TODO: revert back to original functioning
        return 2; // for testing gRPC
        //return district;
    }

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

    /*
     * Generate the taxi initial position
     * ---------------------------------------------------------------------------------
     * Given the district the function will generate the taxi coordinate of the relative
     * recharge station (assignment requirement).
     */
    private int[] genTaxiInitialPosition(int district) {
        int[] position = new int[2];

        switch (district) {
            case 1:
                break;
            case 2:
                position[1] = 9;
                break;
            case 3:
                position[0] = 9;
                break;
            case 4:
                position[0] = 9;
                position[1] = 9;
                break;
        }
        return position;
    }

    /// Getters & Setters
    public static ArrayList<TaxiInfo> getTaxis() {
        return (ArrayList<TaxiInfo>) taxis.clone();
    }

}