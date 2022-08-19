package Utility;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import Client.TaxiInfo;

import java.util.ArrayList;
import java.util.Random;

public class Utility {
    private static Utility instance;
    private static Random random = new Random();
    public static Gson GSON = new Gson();

    Utility() {
    }

    public static Utility getInstance() {
        if (instance == null) {
            instance = new Utility();
        }
        return instance;
    }

    public static String postRequest(Client client, String url, String jsonBody) {
        Invocation.Builder builder = getBuilder(client, url);
        Response response = builder.post(Entity.json(jsonBody));
        response.bufferEntity();

        String responseJson = null;
        try {
            responseJson = response.readEntity(String.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return responseJson;
    }

    public static String getRequest(Client client, String url) {
        Invocation.Builder builder = getBuilder(client, url);
        Response response = builder.get();
        response.bufferEntity();

        String responseJson = null;
        try {
            responseJson = response.readEntity(String.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return responseJson;
    }

    public static String delRequest(Client client, String url) {
        Invocation.Builder builder = getBuilder(client, url);
        Response response = builder.delete();
        response.bufferEntity();

        String responseJson = null;
        try {
            responseJson = response.readEntity(String.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return responseJson;
    }

    private static Invocation.Builder getBuilder(Client client, String url) {
        WebTarget webTarget = client.target(url);
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        return builder;
    }

    public static double euclideanDistance(int[] start, int[] end) {
        double xOffset = Math.pow((end[0] - start[1]), 2);
        double yOffset = Math.pow((end[1] - start[0]), 2);
        return Math.sqrt(xOffset + yOffset);
    }

    public static int generateRndInteger(int origin, int bound) {
        return random.nextInt(origin, bound);
    }

    public static long generateRndLong(long origin, long bound) {
        return random.nextLong(origin, bound);
    }
}
