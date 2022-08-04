package Server;

public class ServerTaxisUpdater implements Runnable {
    private Thread t;

    public void start() {
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                AdministratorServer.getInstance().updateTaxiLists();
                AdministratorServer.getInstance().printAllTaxis();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
