package Administrator;

import com.google.common.reflect.TypeToken;
import jakarta.ws.rs.client.*;
import Utility.Utility;

import java.util.ArrayList;
import java.util.Scanner;

import static Utility.Utility.GSON;

import Client.TaxiInfo;

public class AdministratorClient {
    private final static String ADMIN_SERVER_ADDRESS = "localhost";
    private final static int ADMIN_SERVER_PORT = 9001;
    private final static String ADMIN_SERVER_URL = "http://" + ADMIN_SERVER_ADDRESS + ":" + ADMIN_SERVER_PORT;
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final Client client = ClientBuilder.newClient();

    public static void main(String[] args) {
        printMenu();
        while (SCANNER.hasNext()) {
            int choice = SCANNER.nextInt();

            if (choice == 1) {
                removeTaxi();
            } else if (choice == 2) {
                getTaxis();
            } else {
                System.out.println("The selected option is not valid.");
            }
            printMenu();
        }

    }

    private static void printMenu() {
        System.out.println("Administrator client menu");
        System.out.println("-------------------------");
        System.out.println("1) Delete a given taxi");
        System.out.println("2) Taxis in the smart city");
        System.out.println("-------------------------");
    }

    private static void removeTaxi() {
        System.out.println("Which taxi do you want to remove? (Insert the ID): ");
        int taxiID = SCANNER.nextInt();

        final String PATH = "/del-taxi/" + taxiID;

        String serverInitInfos = Utility.delRequest(client, ADMIN_SERVER_URL + PATH);
        System.out.println(serverInitInfos);
    }

    private static void getTaxis() {
        final String PATH = "/get-taxis";
        String json = Utility.getRequest(client, ADMIN_SERVER_URL + PATH);

        ArrayList<TaxiInfo> taxis = GSON.fromJson(json, new TypeToken<ArrayList<TaxiInfo>>(){}.getType());

        for (TaxiInfo t : taxis)
            System.out.println(t);
    }
}