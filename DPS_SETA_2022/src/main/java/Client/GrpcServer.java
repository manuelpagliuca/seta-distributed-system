package Client;

import io.grpc.ServerBuilder;
import java.io.IOException;

public class GrpcServer implements Runnable {
    private Thread t;
    private int grpcPort = -1;

    GrpcServer(int grpcPort) {
        this.grpcPort = grpcPort;
    }

    /* Start the gRPC server side of the taxi
     * --------------------------------------------------------------
     * Once this method is called it will be possible to send data
     * to this endpoint, the only requirement is to know the gRPC port.
     */
    @Override
    public void run() {
        if (grpcPort != -1) {
            try {
                io.grpc.Server server =
                        ServerBuilder.forPort(grpcPort)
                                .addService(new Taxi()).build();
                server.start();
                System.out.println("gRPC server started on port: " + grpcPort);
                server.awaitTermination();
                System.out.println("The server for gRPC communication has terminated");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }
}
