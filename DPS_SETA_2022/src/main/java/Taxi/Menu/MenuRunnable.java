package Taxi.Menu;

import Taxi.Data.TaxiInfo;

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

    public MenuRunnable(TaxiInfo taxi, Object availableCLI) {
        this.availableCLI = availableCLI;
        this.taxi = taxi;
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
                removeTaxi();
            } else if (userInput.equalsIgnoreCase("info")) {
                System.out.println(taxi.toString());
            } else {
                System.out.println("This command is not available.");
            }
        }
    }
}