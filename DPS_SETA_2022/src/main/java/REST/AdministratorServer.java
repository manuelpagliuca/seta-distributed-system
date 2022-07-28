package REST;

import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;

public class AdministratorServer {

    private static final String HOST = "localhost";
    private static final int PORT = 1337;


    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServerFactory.create("http://" + HOST + ":" + PORT + "/");
        server.start();

        System.out.println("Server running!");
        System.out.println("Server started on: http://" + HOST + ":" + PORT);

        System.out.println("Hit return to stop...");
        System.in.read();
        System.out.println("Stopping server");
        server.stop(0);
        System.out.println("Server stopped");
    }
}

/*
        ServerSocket welcomeSocket = new ServerSocket(6789);

        System.out.println("Listening...");
        while (true) {
            Socket connectionSocket = welcomeSocket.accept();

            ServerThread theThread = new ServerThread(connectionSocket);

            theThread.start();
        }
*/
