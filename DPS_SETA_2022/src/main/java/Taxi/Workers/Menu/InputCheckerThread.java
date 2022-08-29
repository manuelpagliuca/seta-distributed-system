/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. of Computer Science @UNIMI A.Y. 2021/2022 */
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
