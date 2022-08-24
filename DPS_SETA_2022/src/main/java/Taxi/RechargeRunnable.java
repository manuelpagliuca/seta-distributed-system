package Taxi;

import Taxi.Data.TaxiInfo;

import java.util.Arrays;

import static Utility.Utility.euclideanDistance;
import static Utility.Utility.genTaxiInitialPosition;

public class RechargeRunnable implements Runnable {
    private final TaxiInfo thisTaxi;
    private final Object checkBattery;

    private boolean isRunning = true;

    public RechargeRunnable(TaxiInfo thisTaxi, Object checkBattery) {
        this.thisTaxi = thisTaxi;
        this.checkBattery = checkBattery;
    }

    @Override
    public void run() {
        while (isRunning) {
            synchronized (checkBattery) {
                try {
                    checkBattery.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            if (thisTaxi.getBattery() < 30.0) {
                try {
                    moveToRechargeStation();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        }
    }

    private void moveToRechargeStation() throws InterruptedException {
        System.out.println("[Critical Battery Level] Moving to the recharge station of district "
                + thisTaxi.getDistrict());

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
                "and after %,.2f Km the battery levels are %,.2f %%\n", totalKm, thisTaxi.getBattery());
        thisTaxi.setRiding(false);
    }

    public void stop() {
        isRunning = false;
    }
}
