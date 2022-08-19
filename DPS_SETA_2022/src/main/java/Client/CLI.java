package Client;

import java.util.Scanner;

import static Client.Taxi.removeTaxi;

/*
 * Command Line Interface for receiving user commands
 * --------------------------------------------------------------
 * It is implemented through a thread and always in listening on the
 * standard input for a command.
 *
 * quit: perform a DELETE request on the administrator server for the
 * removal of this taxi, then quit the process.
 */
public class CLI implements Runnable {
    private Thread t;

    CLI() {
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

        while (scanner.hasNextLine() && !Thread.currentThread().isInterrupted()) {
            userInput = scanner.nextLine();
            if (userInput.equalsIgnoreCase("quit")) {
                System.out.println("Terminating the execution");
                removeTaxi();
            } else {
                System.out.println("This command is not available.");
            }
        }
    }
}