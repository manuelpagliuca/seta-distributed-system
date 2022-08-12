package Administrator;

import com.google.gson.Gson;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Scanner;

public class AdministratorClient {
    private final static String ADMIN_SERVER_ADDRESS = "localhost";
    private final static int ADMIN_SERVER_PORT = 9001;
    private final static String ADMIN_SERVER_URL = "http://" + ADMIN_SERVER_ADDRESS + ":" + ADMIN_SERVER_PORT;
    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Administrator client menu");
        System.out.println("-------------------------");
        System.out.println("1) Delete a given taxi");
        System.out.println("-------------------------");
        Client client = ClientBuilder.newClient();

        int choice = SCANNER.nextInt();

        if (choice == 1) {
            removeTaxi(client);
        } else {
            System.out.println("The selected option is not valid.");
        }
    }

    private static void removeTaxi(Client client) {
        System.out.println("Which taxi do you want to remove? (Insert the ID): ");
        int taxiID = SCANNER.nextInt();

        final String INIT_PATH = "/del-taxi/" + taxiID;

        // Receive the initialization data from the server: valid ID, position, list of other taxis

        String serverInitInfos = delRequest(client, ADMIN_SERVER_URL + INIT_PATH);
        System.out.println(serverInitInfos);
    }

    private static String delRequest(Client client, String url) {
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