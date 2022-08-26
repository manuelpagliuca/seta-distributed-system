package Taxi.Workers.Menu;

import java.io.IOException;

public class InputCheckerThread implements Runnable {
    final Object availableCLI;
    private Thread t;

    public InputCheckerThread(Object availableCLI) {
        this.availableCLI = availableCLI;
    }

    public void start() {
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            synchronized (availableCLI) {
                try {
                    if (System.in.available() > 0) {
                        availableCLI.notify();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}