package REST.Client;

import Utility.LocalData;
import REST.beans.User;
import REST.beans.Users;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class Taxi {
    public LocalData localData;
    public float battery = 100.f;
    static String adminServerAddr;
    static int adminServerPort;
    static int grpcPort;
    static long id;

    public static void main(String[] args) throws Exception {
        Client client = Client.create();
        String serverAddress = "http://localhost:1337";
        ClientResponse clientResponse = null;

        //GET REQUEST #1
        String getPath = "/users";
        clientResponse = getRequest(client, serverAddress + getPath);
        System.out.println(clientResponse.toString());
        Users users = clientResponse.getEntity(Users.class);
        System.out.println("Users List");
        for (User u : users.getUserslist()) {
            System.out.println("Name: " + u.getName() + " Surname: " + u.getSurname());
        }


        //System.out.println("Insert the Administrator Server address: ");
        //BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        //adminServerAddr = inFromUser.readLine();

        //System.out.println("Insert the Administrator Server port: ");
        //inFromUser = new BufferedReader(new InputStreamReader(System.in));
        //adminServerPort = Integer.parseInt(inFromUser.readLine());

        //System.out.println("Insert the Administrator Server port: ");
        //inFromUser = new BufferedReader(new InputStreamReader(System.in));
        //grpcPort = Integer.parseInt(inFromUser.readLine());

        //System.out.println("Insert the Taxi ID: ");
        //inFromUser = new BufferedReader(new InputStreamReader(System.in));
        //id = Integer.parseInt(inFromUser.readLine());
        //id = Thread.currentThread().getId();

        //Socket clientSocket = new Socket(adminServerAddr, adminServerPort);

        // output stream towards socket initialization
        //DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

        // input stream from socket initialization
        //BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        // send the Taxi ID to the server
        //outToServer.writeBytes(id + "\n");

        // read the response from the server
        //String modifiedSentence = inFromServer.readLine();
        //System.out.println("Smart city district: " + modifiedSentence);

        //clientSocket.close();
    }

    public static ClientResponse getRequest(Client client, String url) {
        WebResource webResource = client.resource(url);
        try {
            return webResource.type("application/json").get(ClientResponse.class);
        } catch (ClientHandlerException e) {
            System.out.println("Server non disponibile");
            return null;
        }
    }

}
