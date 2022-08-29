/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. of Computer Science @UNIMI A.Y. 2021/2022 */
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
