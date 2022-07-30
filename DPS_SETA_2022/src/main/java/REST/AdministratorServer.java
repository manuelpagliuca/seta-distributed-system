package REST;

import REST.Client.Taxi;
import REST.ServerServices.Init;
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
                AdministratorServer.getInstance().printAllTaxis();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}

public class AdministratorServer {
    private static AdministratorServer instance = null;
    private static final String HOST = "localhost";
    private static final int PORT = 9001;
    private static HashMap<Integer, Taxi> taxis = new HashMap<>();
    private static int[][] smartCity = new int[10][10];

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

        System.out.println("Taxis before adding: ");
        printAllTaxis();

        if (!taxiIDisPresent(taxi.getID())) {
            System.out.println("The Taxi ID is not present, adding the taxi ID: "
                    + taxi.getID() +
                    " to the system.");
            taxis.put(taxi.getID(), taxi);
            return taxi.getID();
        }

        int validID = generateValidID();
        taxis.put(validID, taxi);

        System.out.println("The Taxi ID is present in the system");
        return validID;
    }

    private int generateValidID() {
        Random random = new Random();
        int newID = random.nextInt(1, 100);

        while (taxis.containsKey(newID)) {
            newID = random.nextInt(1, 100);
        }
        return newID;
    }

    private boolean taxiIDisPresent(int id) {
        return taxis.containsKey(id);
    }

    public static void main(String[] args) throws IOException {
        ResourceConfig config = new ResourceConfig();
        config.register(Init.class);
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
        System.out.println(taxis.toString());
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

    public static HashMap<Integer, Taxi> getTaxis() {
        return taxis;
    }

    public static void setTaxis(HashMap<Integer, Taxi> taxis) {
        AdministratorServer.taxis = taxis;
    }
}
