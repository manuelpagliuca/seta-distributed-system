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

public class RechargeThread extends Thread {
    private final TaxiInfo thisTaxi;
    private final ArrayList<TaxiInfo> otherTaxis;
    private final Object checkBattery;
    private final GrpcModule grpcModule;
    private boolean isRunning = true;

    public RechargeThread(TaxiInfo thisTaxi, ArrayList<TaxiInfo> otherTaxis,
                          Object checkBattery, GrpcModule grpcModule) {
        this.thisTaxi = thisTaxi;
        this.otherTaxis = otherTaxis;
        this.checkBattery = checkBattery;
        this.grpcModule = grpcModule;
    }

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

    public void rechargeProcedure() throws InterruptedException {
        thisTaxi.setWantsToRecharge(true);
        System.out.println("[Critical Battery Level] Moving to the recharge station of district "
                + thisTaxi.getDistrict());

        rideToDistrictRechargeStation();

        int totalACKs = otherTaxis.size();
        int receivedACKs = 0;

        while (receivedACKs < totalACKs) {
            try {
                receivedACKs = grpcModule.coordinateRechargeGrpcStream();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            GrpcRunnable.resetACKS();
            totalACKs = otherTaxis.size();
            System.out.println("The recharge station is busy...");
        }

        System.out.println("It is your turn now!");
        rechargeBattery();
    }

    private void rechargeBattery() throws InterruptedException {
        thisTaxi.setRecharging(true);
        System.out.printf("The taxi is recharging (current battery levels %,.2f%%)\n", thisTaxi.getBattery());
        Thread.sleep(5000);
        thisTaxi.setBattery(100.0D);
        System.out.printf("The taxi has been successfully recharged, battery levels are %,.2f%%\n",
                thisTaxi.getBattery());
        thisTaxi.setRecharging(false);
        thisTaxi.setWantsToRecharge(false);
    }

    private void rideToDistrictRechargeStation() throws InterruptedException {
        thisTaxi.setRiding(true);

        int[] rechargeStationPos = genTaxiInitialPosition(thisTaxi.getDistrict());
        final double totalKm = euclideanDistance(thisTaxi.getPosition(), rechargeStationPos);

        thisTaxi.getPosition()[0] = rechargeStationPos[0];
        thisTaxi.getPosition()[1] = rechargeStationPos[1];

        Thread.sleep(5000);
        thisTaxi.setBattery(thisTaxi.getBattery() - totalKm);
        System.out.printf("I reached the recharge station " +
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
