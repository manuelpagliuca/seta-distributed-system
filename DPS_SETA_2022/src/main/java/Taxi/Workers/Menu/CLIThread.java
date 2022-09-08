/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. in Computer Science @UNIMI A.Y. 2021/2022 */
package Taxi.Workers.Menu;

import Taxi.Structures.TaxiInfo;
import Taxi.Workers.RechargeThread;

import java.util.ArrayList;
import java.util.Scanner;

import static Taxi.Taxi.removeTaxi;

/*
 * Command Line Interface for receiving user commands
 * ------------------------------------------------------------------------------
 * It is implemented through a thread and always in listening on the standard
 * input for a command.
 *
 * quit: perform a DELETE request on the administrator server for the removal of
 * this taxi, then quit the process.
 *
 * info: print the taxi information.
 *
 * recharge: trigger the recharge operation (even if battery levels are > 30%)
 */
public class CLIThread extends Thread {
    final Object availableCLI;
    final Object checkRechargeCLI;
    private Thread t;
    private final TaxiInfo taxi;
    private final ArrayList<TaxiInfo> otherTaxis;
    private final RechargeThread rechargeThreadRef;

    public CLIThread(TaxiInfo taxi, ArrayList<TaxiInfo> otherTaxis,
                     Object availableCLI, Object checkRechargeCLI, RechargeThread rechargeThread) {
        this.availableCLI = availableCLI;
        this.checkRechargeCLI = checkRechargeCLI;
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
                    rechargeThreadRef.terminate();
                    removeTaxi();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else if (userInput.equalsIgnoreCase("info")) {
                System.out.println(taxi.toString());
            } else if (userInput.equalsIgnoreCase("recharge")) {
                if (taxi.isRecharging() || taxi.wantsToRecharge()) {
                    System.out.println("The taxi is already recharging!");
                } else if (taxi.isRiding() && !taxi.isRecharging()) {
                    System.out.println("The taxi is executing a ride, it will go to recharge station after.");
                    synchronized (checkRechargeCLI) {
                        try {
                            checkRechargeCLI.wait();
                            rechargeThreadRef.rechargeProcedure();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else {
                    try {
                        rechargeThreadRef.rechargeProcedure();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                System.out.println("This command is not available.");
            }
        }
    }
}