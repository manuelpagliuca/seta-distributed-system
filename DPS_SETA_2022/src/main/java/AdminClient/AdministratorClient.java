/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. in Computer Science @UNIMI A.Y. 2021/2022 */
package AdminClient;

import Taxi.Statistics.Statistics.AvgStatisticsInfo;
import Taxi.Statistics.Statistics.TotalStatisticsInfo;
import com.google.common.reflect.TypeToken;
import jakarta.ws.rs.client.*;
import Misc.Utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

import static Misc.Utility.GSON;
import static Misc.Utility.getJsonString;

import Taxi.Structures.TaxiInfo;
import jakarta.ws.rs.core.Response;

/*
 * Administrator Client
 * ------------------------------------------------------------------------------
 * Through this client it is possible to make queries to the administrator server
 * through the REST API.
 *
 * Summarizing the types of queries that can be made are:
 *
 *  - Removal of a cab from the smartcity (DELETE)
 *  - Retrieve the information about the cab registered in the smartcity (GET)
 *  - Compute the last n local statistics of a given cab (GET)
 *  - Calculate the average statistics of all cabs between two timestamps (GET)
 */
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
                removeTaxiRequest();
            } else if (choice == 2) {
                getTaxisRequest();
            } else if (choice == 3) {
                getLocalStatsRequest();
            } else if (choice == 4) {
                getLocalStatsTSRequest();
            } else if (choice == 5) {
                quit();
            } else {
                System.out.println("The selected option is not valid.");
            }
            printMenu();
        }

    }

    // Perform a GET request for the retrieval of the last n measurements of a given taxi ID
    private static void getLocalStatsRequest() {
        System.out.println("Which taxi do you want to receive the statistics ?");
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

    // Perform a DELETE request for the removal of a given taxi ID
    private static void removeTaxiRequest() {
        System.out.println("Which taxi do you want to remove? (Insert the ID): ");
        final int taxiID = SCANNER.nextInt();

        final String PATH = "/del-taxi/" + taxiID;

        String serverInitInfos = Utility.delRequest(client, ADMIN_SERVER_URL + PATH);
        System.out.println(serverInitInfos);
    }

    // Perform a GET request for retrieve a list of taxis information
    private static void getTaxisRequest() {
        final String PATH = "/get-taxis";
        Response response = Utility.getRequest(client, ADMIN_SERVER_URL + PATH);

        String json = getJsonString(response);

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            @SuppressWarnings("UnstableApiUsage") ArrayList<TaxiInfo> taxis = GSON.fromJson(json, new TypeToken<ArrayList<TaxiInfo>>() {
            }.getType());

            assert taxis != null;
            for (TaxiInfo t : taxis)
                System.out.println(t);
        } else {
            System.out.println(json);
        }
    }

    // Get two timestamps and perform a GET request for retrieve the overall avg local stats between t1 and t2
    private static void getLocalStatsTSRequest() {
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
            TotalStatisticsInfo avgTimestampStats =
                    new TotalStatisticsInfo(GSON.fromJson(json, TotalStatisticsInfo.class));
            System.out.println(avgTimestampStats);
        } else {
            System.out.println(json);
        }
    }

    /// Utility

    // Retrieve a timestamp from the CLI input (commented the year, month, day, hour)
    @SuppressWarnings("CommentedOutCode")
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

    // Quit procedure
    private static void quit() {
        System.out.println("Good bye");
        client.close();
        System.exit(0);
    }

    // Just prints the CLI menu
    private static void printMenu() {
        System.out.println("*------------------------------------------------------------*");
        System.out.println("|                 Administrator client menu                  |");
        System.out.println("*------------------------------------------------------------*");
        System.out.println("| 1) Delete a given taxi                                     |");
        System.out.println("| 2) Taxis in the smart city                                 |");
        System.out.println("| 3) Average of the last n local statistics for a given taxi |");
        System.out.println("| 4) Average statistics of all taxis (given two timestamps)  |");
        System.out.println("| 5) Quit                                                    |");
        System.out.println("*------------------------------------------------------------*");
    }
}