/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. of Computer Science @UNIMI A.Y. 2021/2022 */
package Server;

import Clients.Taxi.TaxiInfo;
import Server.Services.AdministratorServerServices;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.*;

class SomeThread implements Runnable {
    private Thread t;

    public void start() {
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                AdministratorServer.getInstance().updateTaxiLists();
                AdministratorServer.getInstance().printAllTaxis();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

public class AdministratorServer {
    private static final String HOST = "localhost";
    private static final int PORT = 9001;
    private static AdministratorServer instance = null;
    private static ArrayList<TaxiInfo> taxis = new ArrayList<>(10);
    private static int[][] smartCity = new int[10][10];

    public AdministratorServer() {
    }

    public static AdministratorServer getInstance() {
        if (instance == null) {
            instance = new AdministratorServer();
        }
        return instance;
    }

    /* Add taxi process information inside the server and return updated infos.
     *
     * Given the Taxi information parsed from a JSON file it will add the taxi
     * information inside the static taxi list of the server.
     *
     * The ID of the taxi list if already present will be randomly generated again
     * until a valid one will be available, that will be used.
     *
     * Also the district with the relative starting position of the taxi will both
     * be generated here and added to a TaxiInfo object that will be returned to the
     * client. In this way the client will know the valid ID, the district and the
     * position inside the smart city. */
    public TaxiInfo addTaxi(TaxiInfo info) {
        assert (info != null);
        TaxiInfo newTaxi = new TaxiInfo();

        if (taxis.contains(info.getId())) {
            int validID = genValidID();
            newTaxi.setId(validID);
        } else {
            newTaxi.setId(info.getId());
        }

        newTaxi.setDistrict(randomDistrict());
        newTaxi.setPosition(genStartPosition(newTaxi.getDistrict()));
        newTaxi.setTaxis((ArrayList<TaxiInfo>) taxis.clone());
        newTaxi.setGrpcPort(info.getGrpcPort());
        newTaxi.setAdministratorServerAddr(info.getAdministratorServerAddr());

        taxis.add(newTaxi);
        return newTaxi;
    }

    public static void main(String[] args) throws IOException {
        ResourceConfig config = new ResourceConfig();
        config.register(AdministratorServerServices.class);
        String serverAddress = "http://" + HOST + ":" + PORT;
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(serverAddress), config);

        SomeThread t = new SomeThread();
        try {
            t.start();
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    // Utility
    public void printAllTaxis() {
        for (TaxiInfo i : taxis) {
            System.out.println(i.toString());

        }
        System.out.println("---");
    }

    private int randomDistrict() {
        Random rnd = new Random();
        final int lowerBound = 1;
        final int upperBound = 4;

        int district = rnd.nextInt(lowerBound, upperBound + 1);

        return district;
    }

    private int genValidID() {
        Random random = new Random();
        int newID = random.nextInt(1, 100);

        for (int i = 0; i < taxis.size(); ++i) {
            TaxiInfo t = taxis.get(i);
            if (t.getId() == newID) {
                newID = random.nextInt(1, 100);
                i = 0;
            }
        }

        return newID;
    }

    private int[] genStartPosition(int district) {
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

    // Getters & Setters
    public int getPort() {
        return PORT;
    }

    public static void setInstance(AdministratorServer instance) {
        AdministratorServer.instance = instance;
    }


    public int[][] getSmartCity() {
        return smartCity;
    }

    public void setSmartCity(int[][] smartCity) {
        this.smartCity = smartCity;
    }

    public static ArrayList<TaxiInfo> getTaxis() {
        return taxis;
    }

    public static void setTaxis(ArrayList<TaxiInfo> taxis) {
        AdministratorServer.taxis = taxis;
    }

    public void updateTaxiLists() {
        for (TaxiInfo t : taxis) {
            ArrayList<TaxiInfo> tTaxiListView = (ArrayList<TaxiInfo>) taxis.clone();
            tTaxiListView.removeIf(taxi -> taxi == t);
            t.setTaxis(tTaxiListView);
        }
    }
}
