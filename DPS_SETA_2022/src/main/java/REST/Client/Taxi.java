package REST.Client;

import REST.JSONClass.TaxiInitInfo;
import com.google.gson.Gson;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

// Taxis are considered as single processes and not as threads
public class Taxi implements Serializable {
    private final static String ADMIN_SERVER_ADDR = "localhost";
    private final static int ADMIN_SERVER_PORT = 9001;
    private final static String ADMIN_SERVER_URL = "http://" + ADMIN_SERVER_ADDR + ":" + ADMIN_SERVER_PORT;
    private static float battery = 100.f;
    private static int grpcPort = 3005;
    private static int id = -1;
    private static int district = -1;
    private static int[] position = new int[2];


    private static Client client = ClientBuilder.newClient();
    private static HashMap<Integer, Taxi> taxis = new HashMap<>();

    private static Gson gson = new Gson();

    public Taxi(int id, HashMap<Integer, Taxi> taxis, int grpcPort) {
        this.id = id;
        this.taxis = taxis;
        this.grpcPort = grpcPort;
    }

    public static void main(String[] args) throws Exception {
        postInit();
        // TODO: Inizia l'acquisizione dei dati dal sensore
        // TODO: Iscrizione al topic MQTT del proprio distretto
    }

    /*
        Initialization of the Taxi through the administrator server.
        The taxi sends his sensible data to the administrator server, in this
        data there is the proposal of an ID. This will be checked from the server side
        if it is available or already taken, in the second case the server will return
        a valid ID.

        The server answer will contain the initial position of the taxi which is one of the
        four recharge stations in the smart city, this will depend from the random assignment
        of the district. */
    private static void postInit() {
        // Send the taxi initialization request with a tentative random ID
        final String INIT_PATH = "/taxi-init";
        id = generateRndID();
        TaxiInitInfo initInfo = new TaxiInitInfo(id, grpcPort, ADMIN_SERVER_ADDR);
        // Receive the initialization data from the server: valid ID, position, list of other taxis
        String serverInitInfos = postRequest(client, ADMIN_SERVER_URL + INIT_PATH, gson.toJson(initInfo));
        initInfo = gson.fromJson(serverInitInfos, TaxiInitInfo.class);
        // Update the information of this taxi
        id = initInfo.getId();
        position = initInfo.getPosition();
        taxis = initInfo.getTaxis();
        district = initInfo.getDistrict();

        printTaxi();
    }

    private static int generateRndID() {
        Random random = new Random();
        return random.nextInt(1, 101);
    }

    public static String postRequest(Client client, String url, String body) {
        WebTarget webTarget = client.target(url);

        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = builder.post(Entity.json(body));
        response.bufferEntity();

        String responseJson = null;
        try {
            responseJson = response.readEntity(String.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return responseJson;

    }

    public static Response getRequest(Client client, String url) {
        WebTarget webTarget = client.target(url);

        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = builder.get();
        response.bufferEntity();

        String responseJson = null;
        try {
            responseJson = response.readEntity(String.class);
            System.out.println(responseJson);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Gson gson = new Gson();
        TaxiInitInfo dis = gson.fromJson(responseJson, TaxiInitInfo.class);
        System.out.println("R" + dis.toString());

        return response;
    }

    // Utility
    public static void printTaxi() {
        System.out.println("ID: " + id + " Position: " + position[0] +
                "," + position[1] + "\nOther Taxis: " + taxis.toString() +
                "\nDistrict: " + district);
    }

    // Getters & Setters
    public int getID() {
        return id;
    }

    public static float getBattery() {
        return battery;
    }

    public static void setBattery(float battery) {
        Taxi.battery = battery;
    }

    public static int getGrpcPort() {
        return grpcPort;
    }

    public static void setGrpcPort(int grpcPort) {
        Taxi.grpcPort = grpcPort;
    }

    public static int getId() {
        return id;
    }

    public static void setId(int id) {
        Taxi.id = id;
    }

    public static HashMap<Integer, Taxi> getTaxis() {
        return taxis;
    }

    public static void setTaxis(HashMap<Integer, Taxi> taxis) {
        Taxi.taxis = taxis;
    }

    @Override
    public String toString() {
        return "Taxi ID: " + id;
    }
}