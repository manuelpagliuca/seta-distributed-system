package REST.Client;

import REST.JSONClass.TaxiInitInfos;
import com.google.gson.Gson;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// Taxis are considered as single processes and not as threads
public class Taxi implements Serializable {
    private final static String ADMIN_SERVER_ADDR = "localhost";
    private final static int ADMIN_SERVER_PORT = 9001;
    private final static String ADMIN_SERVER_URL = "http://" + ADMIN_SERVER_ADDR + ":" + ADMIN_SERVER_PORT;
    private static float battery = 100.f;
    private static int grpcPort = 3005;
    private static int id = -1;
    private static List<Taxi> taxis = Collections.emptyList();

    public Taxi(int id, List<Taxi> taxis, int grpcPort) {
        this.id = id;
        this.taxis = taxis;
        this.grpcPort = grpcPort;
    }

    public static void main(String[] args) throws Exception {
        Client client = ClientBuilder.newClient();
        Response clientResponse = null;

        // GET REQUEST
        //String initPath = "/init";
        //clientResponse = getRequest(client, adminServerUrl + initPath);
        // System.out.println(clientResponse.getEntity().toString());

        // POST REQUEST
        String initPost = "/new_taxi";
        Random random = new Random();
        id = random.nextInt(1, 101);
        TaxiInitInfos taxiInitInfos = new TaxiInitInfos(id, grpcPort, ADMIN_SERVER_ADDR);
        System.out.println(taxiInitInfos.toString());
        Gson gson = new Gson();
        String body = gson.toJson(taxiInitInfos);
        clientResponse = postRequest(client, ADMIN_SERVER_URL + initPost, body);

        while (true) {
        }
    }

    public static Response postRequest(Client client, String url, String body) {
        WebTarget webTarget = client.target(url);

        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = builder.post(Entity.json(body));
        response.bufferEntity();

        String responseJson = null;
        try {
            responseJson = response.readEntity(String.class);
            //     System.out.println(responseJson);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Gson gson = new Gson();
        TaxiInitInfos dis = gson.fromJson(responseJson, TaxiInitInfos.class);
        System.out.println("R" + dis.toString());

        return response;
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
        TaxiInitInfos dis = gson.fromJson(responseJson, TaxiInitInfos.class);
        System.out.println("R" + dis.toString());

        return response;
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

    public static List<Taxi> getTaxis() {
        return taxis;
    }

    public static void setTaxis(List<Taxi> taxis) {
        Taxi.taxis = taxis;
    }

    @Override
    public String toString() {
        return "Taxi ID: " + id + " listening on grpcPort: " + grpcPort;
    }
}