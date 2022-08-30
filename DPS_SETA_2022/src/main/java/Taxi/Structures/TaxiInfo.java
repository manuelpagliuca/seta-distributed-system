/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. in Computer Science @UNIMI A.Y. 2021/2022 */
package Taxi.Structures;

import org.example.grpc.IPC;

import java.io.Serializable;

// Class for buffering data regarding Taxi processes
public class TaxiInfo implements Serializable {
    private int id = -1;
    private int grpcPort = -1;
    private int district = -1;
    private int[] position = new int[2];
    private String administratorServerAddress = null;
    private boolean isRecharging = false;
    private boolean wantToRecharge;
    private boolean isRiding = false;
    private double battery = -1;
    private int accomplishedRides = 0;
    private double kmTraveled = 0.0;

    public TaxiInfo(TaxiInfo info) {
        this.id = info.id;
        this.district = info.district;
        this.position = info.position;
        this.grpcPort = info.grpcPort;
        this.isRecharging = info.isRecharging;
        this.isRiding = info.isRiding;
        this.battery = info.battery;
        this.administratorServerAddress = info.administratorServerAddress;
        this.wantToRecharge = info.wantToRecharge;
    }

    public TaxiInfo(int id, int grpcPort, String admServer) {
        this.id = id;
        this.grpcPort = grpcPort;
        this.administratorServerAddress = admServer;
        wantToRecharge = false;
    }

    public TaxiInfo(IPC.Infos infos) {
        id = infos.getId();
        grpcPort = infos.getGrpcPort();
        district = infos.getDistrict();

        int[] pos = new int[2];
        pos[0] = infos.getPosition(0);
        pos[1] = infos.getPosition(1);
        position = pos;

        isRecharging = infos.getIsRecharging();
        isRiding = infos.getIsRiding();
        battery = infos.getBattery();
        wantToRecharge = false;
    }

    public TaxiInfo() {
        wantToRecharge = false;
    }

    @Override
    public String toString() {
        return String.format("id=%d, grpc-port=%d, district=%s, position=(%d,%d)",
                id, grpcPort, district, position[0], position[1]);
    }

    public double getBattery() {
        return battery;
    }

    public void setBattery(double battery) {
        this.battery = battery;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGrpcPort() {
        return grpcPort;
    }

    public void setGrpcPort(int grpcPort) {
        this.grpcPort = grpcPort;
    }

    public int getDistrict() {
        return district;
    }

    public void setDistrict(int district) {
        this.district = district;
    }

    public int[] getPosition() {
        return position;
    }

    public void setPosition(int[] position) {
        this.position = position;
    }

    public String getAdministratorServerAddress() {
        return administratorServerAddress;
    }

    public void setAdministratorServerAddress(String administratorServerAddress) {
        this.administratorServerAddress = administratorServerAddress;
    }

    public boolean isRecharging() {
        return isRecharging;
    }

    public void setRecharging(boolean recharging) {
        isRecharging = recharging;
    }

    public boolean isRiding() {
        return isRiding;
    }

    public void setRiding(boolean riding) {
        isRiding = riding;
    }

    public int getAccomplishedRides() {
        return accomplishedRides;
    }

    public void incrementTotalRides() {
        this.accomplishedRides += 1;
    }

    public double getKmTraveled() {
        return kmTraveled;
    }

    public void addTotalKm(double kmTraveled) {
        this.kmTraveled += kmTraveled;
    }

    public boolean wantToRecharge() {
        return wantToRecharge;
    }

    public void setWantToRecharge(boolean wantToRecharge) {
        this.wantToRecharge = wantToRecharge;
    }
}
