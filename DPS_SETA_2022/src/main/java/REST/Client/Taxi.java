/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. of Computer Science @UNIMI A.Y. 2021/2022 */
package REST.Client;

import REST.JSONClass.TaxiInfo;
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
    private static ArrayList<TaxiInfo> taxis = new ArrayList<>();
    private static int grpcPort = 3005;

    public Taxi(ArrayList<TaxiInfo> taxis, int grpcPort) {
        Taxi.taxis = taxis;
        Taxi.grpcPort = grpcPort;
    }

    public static void main(String[] args) throws Exception {
        float battery = 100.f;
        TaxiInfo info = postInit();
        while (true) {
            Thread.sleep(7000);
            printInfo(info);
        }

        // TODO: Inizia l'acquisizione dei dati dal sensore
        // TODO: Iscrizione al topic MQTT del proprio distretto
        // TODO: Acquire information from the server in order to update taxi list.

    }

    /*  Initialization of the Taxi through the administrator server.
        The taxi sends his sensible data to the administrator server, in this
        data there is the proposal of an ID. This will be checked from the server side
        if it is available or already taken, in the second case the server will return
        a valid ID.

        The server answer will contain the initial position of the taxi which is one of the
        four recharge stations in the smart city, this will depend from the random assignment
        of the district. */

    private static TaxiInfo postInit() {
        // Send the taxi initialization request with a tentative random ID
        final String INIT_PATH = "/taxi-init";

        TaxiInfo initInfo = new TaxiInfo(generateRndID(), grpcPort, ADMIN_SERVER_ADDR);
        // Receive the initialization data from the server: valid ID, position, list of other taxis
        Client client = ClientBuilder.newClient();
        String serverInitInfos = postRequest(client, ADMIN_SERVER_URL + INIT_PATH, gson.toJson(initInfo));

        TaxiInfo info = gson.fromJson(serverInitInfos, TaxiInfo.class);
        return info;
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
        TaxiInfo dis = gson.fromJson(responseJson, TaxiInfo.class);
        System.out.println("R" + dis.toString());

        return response;
    }

    // Getters & Setters

    // Utility
    private static void printInfo(TaxiInfo initInfo) {
        System.out.println(initInfo.toString());
    }
}