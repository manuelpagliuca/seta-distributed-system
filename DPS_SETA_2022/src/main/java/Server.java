import Clients.Taxi.BidirectionalServiceImpl;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class Server {
    public static void main(String[] args) {
        try {

            BidirectionalServiceImpl service =
                    new BidirectionalServiceImpl();

            io.grpc.Server server =
                    ServerBuilder.forPort(3005)
                            .addService(service)
                            .build();
            server.start();
            System.out.println("Server started!\n");
            server.awaitTermination();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
