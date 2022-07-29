package REST;

import REST.Client.Taxi;
import REST.ServerServices.Init;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdministratorServer {
    private static AdministratorServer instance = null;
    private static final String HOST = "localhost";
    private static final int PORT = 9001;
    private List<Taxi> taxis = new ArrayList<>();
    private int[][] smartCity = new int[10][10];

    public AdministratorServer() {
    }

    public static AdministratorServer getInstance() {
        if (instance == null) {
            instance = new AdministratorServer();
        }
        return instance;
    }

    public int addTaxi(Taxi taxi) {
        if (taxi == null) {
            System.out.println("The taxi object is invalid!");
            return -1;
        }

        if (!taxiIDisPresent(taxi.getID())) {
            System.out.println(
                    "The Taxi ID is not present, adding the taxi ID: "
                            + taxi.getID() +
                            " to the system.");
            taxis.add(taxi);
            taxi.getID();
        }

        System.out.println("The Taxi ID is present in the system");
        // TODO: Generare un nuovo ID che sia valido, e restituirlo alle TaxiInfos
        return -1; // NUOVO ID
    }

    private boolean taxiIDisPresent(int id) {
        System.out.println("Checking the presence of the ID in the system...");
        return false;
    }

    public static void main(String[] args) throws IOException {
        ResourceConfig config = new ResourceConfig();
        config.register(Init.class);
        String serverAddress = "http://" + HOST + ":" + PORT;
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(serverAddress), config);

        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Utility
    public void printAllTaxis() {
        System.out.println(taxis);
    }

    // Getters & Setters
    public int getPort() {
        return PORT;
    }

    public static void setInstance(AdministratorServer instance) {
        AdministratorServer.instance = instance;
    }

    public List<Taxi> getTaxis() {
        return taxis;
    }

    public void setTaxis(List<Taxi> taxis) {
        this.taxis = taxis;
    }

    public int[][] getSmartCity() {
        return smartCity;
    }

    public void setSmartCity(int[][] smartCity) {
        this.smartCity = smartCity;
    }
}
