package Clients.Taxi;

import Administrator.Server.AdministratorServer;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Scanner;

/*
 * Command Line Interface for receiving user commands
 * --------------------------------------------------------------
 * It is implemented through a thread and always in listening on the
 * standard input for a command.
 *
 * quit: perform a DELETE request on the administrator server for the
 * removal of this taxi, then quit the process.
 */
public class CLI implements Runnable {
    private Thread t;
    private int taxiID;
    private Client client;
    private String adminServerUrl;
    private Scanner scanner;

    CLI(int taxiID, Client client, String adminServerUrl, Scanner scanner) {
        this.taxiID = taxiID;
        this.client = client;
        this.adminServerUrl = adminServerUrl;
        this.scanner = scanner;
    }

    public void start() {
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        String userInput = null;
        while (scanner.hasNextLine() && !Thread.currentThread().isInterrupted()) {
            userInput = scanner.nextLine();
            if (userInput.equalsIgnoreCase("quit")) {
                System.out.println("Terminating the execution");
                removeTaxi();
                // todo: do the goodbye procedure gRPC
                System.exit(0);
            }
        }
    }

    /*
     * Delete this taxi from the administrator server
     * ------------------------------------------------------------------------------
     * Build the specific URL path for performing the DELETE request on the
     * administrator server.
     */
    private void removeTaxi() {
        final String INIT_PATH = "/del-taxi/" + taxiID;
        String serverInitInfos = delRequest(client, adminServerUrl + INIT_PATH);
        System.out.println(serverInitInfos);
    }

    // Perform an HTTP DELETE request given the specific url and client
    private String delRequest(Client client, String url) {
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