package Administrator.Server;

class GrpcThread implements Runnable {
    private Thread t;

    public void start() {
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }

    @Override
    public void run() {
        /*try {
            // TODO: 3005 should be get through server
            int grpcPort = 3005;
            Server server = ServerBuilder
                    .forPort(grpcPort)
                    .addService(new Clients.Taxi.BidirectionalServiceImpl())
                    .build();
            server.start();
            System.out.println("GRPC Server started on port: " + grpcPort);
            server.awaitTermination();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }*/
    }
}