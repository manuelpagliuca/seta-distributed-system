package Administrator.Server;

public class ServerTaxisUpdater implements Runnable {
    private final Object newTaxiArrived;

    ServerTaxisUpdater(Object dummy) {
        this.newTaxiArrived = dummy;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (newTaxiArrived) {
                // System.out.println("ServerTaxisUpdater is waiting");
                try {
                   newTaxiArrived.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                // System.out.println("Notified!");
                AdministratorServer.getInstance().printAllTaxis();
            }
        }
    }
}
