package AdminServer.Workers;

import AdminServer.AdminServer;

public class ServerLoggerThread extends Thread {
    private final Object newTaxiArrived;

    public ServerLoggerThread(Object dummy) {
        this.newTaxiArrived = dummy;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (newTaxiArrived) {
                try {
                   newTaxiArrived.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                AdminServer.getInstance().printAllTaxis();
            }
        }
    }
}
