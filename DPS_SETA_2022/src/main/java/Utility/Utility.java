package Utility;

import com.google.gson.Gson;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import Taxi.Data.TaxiInfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

public class Utility {
    private static Utility instance;
    private static final Random random = new Random();
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

    public static String getJsonString(Response response) {
        String json = "";
        try {
            json = response.readEntity(String.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return json;
    }

    public static Response getRequest(Client client, String url) {
        Invocation.Builder builder = getBuilder(client, url);
        Response response = builder.get();
        response.bufferEntity();

        return response;
    }

    public static String printCalendar(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return String.format(calendar.get(Calendar.HOUR_OF_DAY) + ":" +
                calendar.get(Calendar.MINUTE) + ":" +
                calendar.get(Calendar.SECOND) + ":" +
                calendar.get(Calendar.MILLISECOND));
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
        return webTarget.request(MediaType.APPLICATION_JSON_TYPE);
    }

    public static double euclideanDistance(int[] start, int[] end) {
        double xOffset = Math.pow((end[0] - start[1]), 2);
        double yOffset = Math.pow((end[1] - start[0]), 2);
        return Math.sqrt(xOffset + yOffset);
    }

    public static ArrayList<TaxiInfo> getAvailableTaxisInDistrict(ArrayList<TaxiInfo> otherTaxis, TaxiInfo thisTaxi) {
        ArrayList<TaxiInfo> availableTaxisInDistrict = new ArrayList<>();

        for (TaxiInfo t : otherTaxis) {
            final boolean tHasSameDistrict = (t.getDistrict() == thisTaxi.getDistrict());
            final boolean tIsAvailable = !t.isRecharging() && !t.isRiding();
            if (tHasSameDistrict && tIsAvailable)
                availableTaxisInDistrict.add(t);
        }
        return availableTaxisInDistrict;
    }

    /*
     * Generate the taxi initial position
     * ---------------------------------------------------------------------------------
     * Given the district the function will generate the taxi coordinate of the relative
     * recharge station (assignment requirement).
     */
    public static int[] genTaxiInitialPosition(int district) {
        int[] position = new int[2];

        switch (district) {
            case 1:
                break;
            case 2:
                position[1] = 9;
                break;
            case 3:
                position[0] = 9;
                position[1] = 9;
                break;
            case 4:
                position[0] = 9;
                break;
        }
        return position;
    }

    public static int generateRndInteger(int origin, int bound) {
        return random.nextInt(origin, bound);
    }

    public static long generateRndLong(long origin, long bound) {
        return random.nextLong(origin, bound);
    }
}
