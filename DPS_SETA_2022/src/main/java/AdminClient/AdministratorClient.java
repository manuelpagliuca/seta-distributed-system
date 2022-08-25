package AdminClient;

import Taxi.Statistics.AvgStatisticsInfo;
import Taxi.Statistics.StatisticsInfo;
import Taxi.Statistics.TotalStatisticsInfo;
import com.google.common.reflect.TypeToken;
import jakarta.ws.rs.client.*;
import Utility.Utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

import static Utility.Utility.GSON;
import static Utility.Utility.getJsonString;

import Taxi.Data.TaxiInfo;
import jakarta.ws.rs.core.Response;

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
            } else if (choice == 3) {
                getLocalStats();
            } else if (choice == 4) {
                getLocalStatsTimestamps();
            } else if (choice == 5) {
                quit();
            } else {
                System.out.println("The selected option is not valid.");
            }
            printMenu();
        }

    }

    private static void printMenu() {
        System.out.println("*--------------------------------------------*");
        System.out.println("|      Administrator client menu             |");
        System.out.println("*--------------------------------------------*");
        System.out.println("| 1) Delete a given taxi                     |");
        System.out.println("| 2) Taxis in the smart city                 |");
        System.out.println("| 3) Last n local statistics of a taxi       |");
        System.out.println("| 4) Local statistics between two timestamps |");
        System.out.println("| 5) Quit                                    |");
        System.out.println("*--------------------------------------------*");
    }

    private static void getLocalStatsTimestamps() {
        Calendar calendar = Calendar.getInstance();
        System.out.println("Timestamp 1");
        System.out.println("*--------------------------------------------*");
        final long timeStamp1 = getTimestampFromUser(calendar);
        System.out.println("Timestamp 2");
        System.out.println("*--------------------------------------------*");
        final long timeStamp2 = getTimestampFromUser(calendar);

        final String PATH = "/stats/" + timeStamp1 + "+" + timeStamp2;

        Response response = Utility.getRequest(client, ADMIN_SERVER_URL + PATH);
        String json = getJsonString(response);

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            TotalStatisticsInfo avgTimestampStats = new TotalStatisticsInfo(GSON.fromJson(json, TotalStatisticsInfo.class));
            System.out.println(avgTimestampStats);
        } else {
            System.out.println(json);
        }

    }

    private static long getTimestampFromUser(Calendar calendar) {
        //System.out.println("Insert year: ");
        //final int year = SCANNER.nextInt();
        //System.out.println("Insert month: ");
        //final int day = SCANNER.nextInt();
        //System.out.println("Insert day: ");
        //final int month = SCANNER.nextInt();
        //System.out.println("Insert hour: ");
        //final int hour = SCANNER.nextInt();
        System.out.println("Insert minute: ");
        final int minute = SCANNER.nextInt();
        System.out.println("Insert second: ");
        final int second = SCANNER.nextInt();

        //calendar.set(Calendar.YEAR, year);
        //calendar.set(Calendar.MONTH, month);
        //calendar.set(Calendar.HOUR, hour);
        //calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, 0);
        System.out.println(Utility.printCalendar(calendar.getTimeInMillis()));
        return calendar.getTimeInMillis();
    }

    private static void getLocalStats() {
        System.out.println("Which taxi do you want to receive the statistics?");
        final int taxiID = SCANNER.nextInt();
        System.out.println("Starting from the last measurement, how many statistics do you" +
                "want to consider?");
        final int n = SCANNER.nextInt();
        final String PATH = "/stats/" + taxiID + "_" + n;

        Response response = Utility.getRequest(client, ADMIN_SERVER_URL + PATH);

        String json = getJsonString(response);

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            AvgStatisticsInfo avgStats = new AvgStatisticsInfo(GSON.fromJson(json, AvgStatisticsInfo.class));
            System.out.println(avgStats);
        } else {
            System.out.println(json);
        }
    }

    private static void removeTaxi() {
        System.out.println("Which taxi do you want to remove? (Insert the ID): ");
        final int taxiID = SCANNER.nextInt();

        final String PATH = "/del-taxi/" + taxiID;

        String serverInitInfos = Utility.delRequest(client, ADMIN_SERVER_URL + PATH);
        System.out.println(serverInitInfos);
    }

    private static void getTaxis() {
        final String PATH = "/get-taxis";
        Response response = Utility.getRequest(client, ADMIN_SERVER_URL + PATH);

        String json = getJsonString(response);

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            ArrayList<TaxiInfo> taxis = GSON.fromJson(json, new TypeToken<ArrayList<TaxiInfo>>() {
            }.getType());

            assert taxis != null;
            for (TaxiInfo t : taxis)
                System.out.println(t);
        } else {
            System.out.println(json);
        }
    }

    private static void quit() {
        System.out.println("Good Bye");
        client.close();
        System.exit(1);
    }

}