package Taxi;

import Taxi.Data.TaxiInfo;
import Taxi.gRPC.GrpcModule;

import java.util.ArrayList;
import java.util.Arrays;

import static Utility.Utility.euclideanDistance;
import static Utility.Utility.genTaxiInitialPosition;

public class RechargeThread extends Thread {
    private final TaxiInfo thisTaxi;
    private final ArrayList<TaxiInfo> otherTaxis;
    private final Object checkBattery;
    private final GrpcModule grpcModule;
    private boolean isRunning = true;

    public RechargeThread(TaxiInfo thisTaxi, ArrayList<TaxiInfo> otherTaxis, Object checkBattery, GrpcModule grpcModule) {
        this.thisTaxi = thisTaxi;
        this.otherTaxis = otherTaxis;
        this.checkBattery = checkBattery;
        this.grpcModule = grpcModule;
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

            System.out.println("Notified!");
            if (thisTaxi.getBattery() < 30.0) {
                try {
                    moveToRechargeStation();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void moveToRechargeStation() throws InterruptedException {
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
                "and after %,.2f Km the battery levels are %,.2f%%\n", totalKm, thisTaxi.getBattery());
        thisTaxi.setRiding(false);

        int totalAck = otherTaxis.size();
        int receivedAck = grpcModule.coordinateRechargeGrpcStream();

        // Keep asking until he doesn't get the ACK from everybody
        // TODO: al momento quello che sto utilizzando e' il Bully Algorithm
        // TODO: Ricart and Agrawala algorithm uses a stack, doesn't keep asking
        while (totalAck != receivedAck) {
            receivedAck = grpcModule.coordinateRechargeGrpcStream();
        }
        thisTaxi.setRecharging(true);
        // do the recharging
        System.out.printf("The taxi is recharging... (current battery levels %,.2f%%)\n", thisTaxi.getBattery());
        Thread.sleep(8000);
        thisTaxi.setBattery(100.0);
        System.out.printf("The taxi has been successfully recharged, now the battery levels are at the %,.2f%%\n",
                thisTaxi.getBattery());
        thisTaxi.setRecharging(false);
    }


}
