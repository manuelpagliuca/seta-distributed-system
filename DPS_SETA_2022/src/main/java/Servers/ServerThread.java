package Servers;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Random;

public class ServerThread extends Thread {
    private Socket connectionSocket = null;
    private BufferedReader inFromClient;
    private DataOutputStream outToClient;
    private int numberOfDistricts = 4;
    private int lowerBound = 1;

    public ServerThread(Socket s) {
        connectionSocket = s;
        try {
            inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        String taxiId;
        try {
            taxiId = inFromClient.readLine();
            if (!taxiId.isEmpty()) {
                int number = Integer.parseInt(taxiId);
                if (number > 0 && number < Integer.MAX_VALUE) {
                    System.out.println(taxiId);
                    Random rand = new Random();
                    int district = rand.nextInt(numberOfDistricts - lowerBound) + lowerBound;
                    outToClient.writeBytes(district + "\n");
                }
            }

            connectionSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
