package AdminServer;

import AdminServer.AdminServer;

public class ServerLoggerRunnable implements Runnable {
    private final Object newTaxiArrived;

    ServerLoggerRunnable(Object dummy) {
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
