/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. of Computer Science @UNIMI A.Y. 2021/2022 */
package Clients.Taxi;

import com.google.gson.Gson;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.Random;

// Taxis are considered as single processes and not as threads
public class Taxi {
    private final static String ADMIN_SERVER_ADDR = "localhost";
    private final static int ADMIN_SERVER_PORT = 9001;
    private final static String ADMIN_SERVER_URL = "http://" + ADMIN_SERVER_ADDR + ":" + ADMIN_SERVER_PORT;
    private final static Gson gson = new Gson();

    public static void main(String[] args) throws Exception {
        Client client = ClientBuilder.newClient();
        int grpcPort = 3005;

        TaxiInfo thisTaxi = postInit(client, grpcPort);
        thisTaxi.getAdministratorServerAddr();

        while (true) {
            Thread.sleep(1000);
            //updateTaxiList(client, thisTaxi);
            formatInfos(thisTaxi);
        }
        // TODO: Iscrizione al topic MQTT del proprio distretto
        // TODO: Inizia l'acquisizione dei dati dal sensore
    }

    /*  Initialization of the Taxi through the administrator server.
        The taxi sends his sensible data to the administrator server, in this
        data there is the proposal of an ID. This will be checked from the server side
        if it is available or already taken, in the second case the server will return
        a valid ID.

        The server answer will contain the initial position of the taxi which is one of the
        four recharge stations in the smart city, this will depend from the random assignment
        of the district. */
    private static TaxiInfo postInit(Client client, int grpcPort) {
        // Send the taxi initialization request with a tentative random ID
        final String INIT_PATH = "/taxi-init";

        TaxiInfo initInfo = new TaxiInfo(generateRndID(), grpcPort, ADMIN_SERVER_ADDR);
        // Receive the initialization data from the server: valid ID, position, list of other taxis
        String serverInitInfos = postRequest(client, ADMIN_SERVER_URL + INIT_PATH, gson.toJson(initInfo));

        TaxiInfo info = gson.fromJson(serverInitInfos, TaxiInfo.class);
        return info;
    }

    private static ArrayList<TaxiInfo> getTaxisOnServer(Client client, TaxiInfo thisTaxi) {
        final String GET_PATH = "/get-taxis";

        String serverResponse = postRequest(client, ADMIN_SERVER_URL + GET_PATH, gson.toJson(thisTaxi));
        TaxiInfo ans = gson.fromJson(serverResponse, TaxiInfo.class);

        ArrayList<TaxiInfo> taxis = ans.getTaxis();

        return taxis;
    }

    private static void updateTaxiList(Client client, TaxiInfo thisTaxi) {
        thisTaxi.setTaxis(getTaxisOnServer(client, thisTaxi));
    }

    // Utility
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

    public static String getRequest(Client client, String url) {
        WebTarget webTarget = client.target(url);

        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
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

    private static int generateRndID() {
        Random random = new Random();
        return random.nextInt(1, 101);
    }

    private static String formatInfos(int id, int district, int[] position,
                                      float battery, ArrayList<TaxiInfo> taxis) {
        String infos = String.format("ID: " + id + ", District: " + district +
                ", Position: " + position[0] + ", " + position[1] +
                "Battery: " + battery + ", Other taxis: [");

        for (TaxiInfo t : taxis) {
            infos += t.getId() + ", ";
        }

        if (infos.endsWith(",")) {
            infos = infos.substring(0, infos.length() - 1);
        }
        return infos;
    }

    private static void formatInfos(TaxiInfo initInfo) {
        System.out.println(initInfo.toString());
    }

    // Getters & Setters
}