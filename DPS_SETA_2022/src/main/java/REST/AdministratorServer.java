package REST;

import REST.ServerServices.Init;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public class AdministratorServer {
    private static final String HOST = "localhost";
    private static final int PORT = 9001;

    public static void main(String[] args) throws IOException {
        ResourceConfig config = new ResourceConfig();
        config.register(Init.class);
        String serverAddress = "http://" + HOST + ":" + PORT;
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(serverAddress), config);

        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
