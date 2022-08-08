package Administrator;

import Clients.Taxi.TaxiInfo;
import Schemes.TaxiSchema;
import com.google.gson.Gson;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Scanner;

public class AdministratorClient {
    private final static String ADMIN_SERVER_ADDR = "localhost";
    private final static int ADMIN_SERVER_PORT = 9001;
    private final static String ADMIN_SERVER_URL = "http://" + ADMIN_SERVER_ADDR + ":" + ADMIN_SERVER_PORT;
    private static Scanner scanner = new Scanner(System.in);
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        System.out.println("Administrator client menu");
        System.out.println("-------------------------");
        System.out.println("1) Delete a given taxi");
        System.out.println("-------------------------");
        Client client = ClientBuilder.newClient();

        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                removeTaxi(client);
                break;
            default:
                System.out.println("The selected option is not valid.");
                break;
        }
    }

    private static void removeTaxi(Client client) {
        System.out.println("Which taxi do you want to remove? (Insert the ID): ");
        int taxiID = scanner.nextInt();

        final String INIT_PATH = "/del-taxi/" + taxiID;

        // Receive the initialization data from the server: valid ID, position, list of other taxis

        String serverInitInfos = delRequest(client, ADMIN_SERVER_URL + INIT_PATH);
        System.out.println(serverInitInfos);
    }

    public static String delRequest(Client client, String url) {
        WebTarget webTarget = client.target(url);

        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
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
}