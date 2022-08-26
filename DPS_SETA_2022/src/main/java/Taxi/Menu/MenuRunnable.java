package Taxi.Menu;

import Taxi.Data.TaxiInfo;
import Taxi.RechargeThread;

import java.util.ArrayList;
import java.util.Scanner;

import static Taxi.Taxi.removeTaxi;

/*
 * Command Line Interface for receiving user commands
 * --------------------------------------------------------------
 * It is implemented through a thread and always in listening on the
 * standard input for a command.
 *
 * quit: perform a DELETE request on the administrator server for the
 * removal of this taxi, then quit the process.
 */
public class MenuRunnable implements Runnable {
    final Object availableCLI;
    private Thread t;
    private final TaxiInfo taxi;
    private final ArrayList<TaxiInfo> otherTaxis;
    private RechargeThread rechargeThreadRef;


    public MenuRunnable(TaxiInfo taxi, ArrayList<TaxiInfo> otherTaxis, Object availableCLI, RechargeThread rechargeThread) {
        this.availableCLI = availableCLI;
        this.taxi = taxi;
        this.otherTaxis = otherTaxis;
        this.rechargeThreadRef = rechargeThread;
    }

    public void start() {
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        String userInput;

        while (!Thread.currentThread().isInterrupted()) {
            synchronized (availableCLI) {
                try {
                    availableCLI.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("quit") || userInput.equalsIgnoreCase("exit")) {
                System.out.println("Terminating the execution");
                try {
                    removeTaxi();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else if (userInput.equalsIgnoreCase("info")) {
                System.out.println(taxi.toString());
                System.out.println(otherTaxis);
            } else if (userInput.equalsIgnoreCase("recharge")) {
                // TODO: Dovrebbe terminare la corsa che sta eseguendo?
                try {
                    rechargeThreadRef.moveToRechargeStation();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            } else {
                System.out.println("This command is not available.");
            }
        }
    }
}