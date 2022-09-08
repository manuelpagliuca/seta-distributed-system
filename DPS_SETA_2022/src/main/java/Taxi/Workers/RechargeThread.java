/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. in Computer Science @UNIMI A.Y. 2021/2022 */
package Taxi.Workers;

import Taxi.Structures.TaxiInfo;
import Taxi.gRPC.GrpcModule;
import Taxi.gRPC.GrpcRunnable;

import java.util.ArrayList;
import java.util.Arrays;

import static Misc.Utility.euclideanDistance;
import static Misc.Utility.genTaxiInitialPosition;

/*
 * RechargeThread
 * ------------------------------------------------------------------------------
 * This thread monitors the battery levels of the taxi at the end of each ride,
 * if the battery levels drop below the 30% it will start a procedure that will
 * move the taxi to the recharge station of the current district for recharging
 * the batteries (this will use Ricart & Agrawala mutual exclusion algorithm
 * for handling the access to the critical section).
 */
public class RechargeThread extends Thread {
    private final TaxiInfo thisTaxi;
    private final ArrayList<TaxiInfo> otherTaxis;
    private final Object checkBattery;
    private final GrpcModule grpcModule;
    private boolean isRunning = true;
    private static final Object lock = new Object();
    private static int receivedACKs = 0;
    private static final ArrayList<Integer> waitingList = new ArrayList<>();

    public RechargeThread(TaxiInfo thisTaxi, ArrayList<TaxiInfo> otherTaxis,
                          Object checkBattery, GrpcModule grpcModule) {
        this.thisTaxi = thisTaxi;
        this.otherTaxis = otherTaxis;
        this.checkBattery = checkBattery;
        this.grpcModule = grpcModule;
    }

    /*
     * Check the battery levels and if necessary start a recharge operation
     * ------------------------------------------------------------------------------
     * It checks the battery levels every time the tixe has completed a ride, in the
     * case in which the battery levels are below 30% it starts a recharge procedure.
     */
    @Override
    public void run() {
        while (isRunning) {
            // Check the battery condition only after the execution of a ride.
            synchronized (checkBattery) {
                try {
                    checkBattery.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            if (thisTaxi.getBattery() < 30.0D) {
                try {
                    rechargeProcedure();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /*
     * It drives the taxi to the recharge station and starts the mutual exclusion
     * ------------------------------------------------------------------------------
     * First of all it sets that the taxi wants to recharge (in this way it can't
     * accept any ride) and then moves the taxi to the recharge station of the
     * current district.
     *
     * After that it starts the procedure for acessing to the critical section,
     * so it has to use the gRPC service which implements the Ricart & Agrawala
     * algorithm.
     *
     * If he passes that barrier, it recharges the batteries.
     */
    public void rechargeProcedure() throws InterruptedException {
        thisTaxi.setWantsToRecharge(true);
        System.out.println(
                "[Critical Battery Level] Moving to the recharge station of district "
                        + thisTaxi.getDistrict());

        rideToDistrictRechargeStation();

        final int totalACKs = otherTaxis.size();
        receivedACKs = grpcModule.coordinateRechargeGrpcStream(waitingList);

        while (receivedACKs < totalACKs) {
            synchronized (lock) {
                lock.wait();
            }
            System.out.println("The recharge station is busy...");
        }

        System.out.println("It is your turn now!");
        rechargeBattery();
    }

    /*
     * Recharges the battery
     * -----------------------------------------------------------------------------
     * It enables the flag for telling to the other taxis that he is recharging
     * (this means that he still can't join any ride election), and then it wait 5
     * seconds for charge is battery levels to 100%.
     */
    private void rechargeBattery() throws InterruptedException {
        thisTaxi.setRecharging(true);
        System.out.printf(
                "The taxi is recharging (current battery levels %,.2f%%)\n",
                thisTaxi.getBattery());
        Thread.sleep(5000);

        thisTaxi.setBattery(100.0D);

        System.out.printf(
                "The taxi has been successfully recharged, battery levels are %,.2f%%\n",
                thisTaxi.getBattery());
        thisTaxi.setRecharging(false);
        thisTaxi.setWantsToRecharge(false);

        if (!waitingList.isEmpty()) {
            grpcModule.sendAcksToWaitingTaxis(waitingList);
            receivedACKs = 0;
            waitingList.clear();
        }
    }

    public static void addWaitingTaxi(Integer id) {
        waitingList.add(id);
    }

    public static void incrementAcks() {
        receivedACKs++;
        synchronized (lock) {
            lock.notify();
        }
    }

    /*
     * Move the taxi from the current position to the recharge station
     * -----------------------------------------------------------------------------
     * It drives the taxi to the recharge station of is district (it took 5 seconds
     * like for a normal ride), even in this case he will consume batteries for
     * reaching the station. It behaves like a normal ride to another node of the
     * smartcity.
     */
    private void rideToDistrictRechargeStation() throws InterruptedException {
        thisTaxi.setRiding(true);

        final int[] rechargeStationPos = genTaxiInitialPosition(thisTaxi.getDistrict());
        final double totalKm = euclideanDistance(thisTaxi.getPosition(), rechargeStationPos);

        thisTaxi.getPosition()[0] = rechargeStationPos[0];
        thisTaxi.getPosition()[1] = rechargeStationPos[1];

        Thread.sleep(5000);
        thisTaxi.setBattery(thisTaxi.getBattery() - totalKm);
        System.out.printf(
                "I reached the recharge station " +
                        Arrays.toString(rechargeStationPos) +
                        " at district " +
                        thisTaxi.getDistrict() +
                        "and after %,.2f Km the battery levels are %,.2f%%\n", totalKm, thisTaxi.getBattery());
        thisTaxi.setRiding(false);
        thisTaxi.incrementTotalRides();
    }

    // Used for the general software termination procedure (through CLI)
    public void terminate() {
        isRunning = false;
    }
}