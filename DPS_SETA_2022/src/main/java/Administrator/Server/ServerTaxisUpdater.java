package Administrator.Server;

public class ServerTaxisUpdater implements Runnable {
    private Thread t;
    private boolean updated = false;

    public void start() {
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            if (updated) {
                clearScreen();
                AdministratorServer.getInstance().printAllTaxis();
                updated = false;
            }
        }
    }

    public void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void update() {
        updated = true;
    }
}
