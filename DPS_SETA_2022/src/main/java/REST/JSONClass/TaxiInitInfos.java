package REST.JSONClass;

import REST.Client.Taxi;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;


public class TaxiInitInfos implements Serializable {
    private int id = -1;

    private int grpcPort = -1;
    private int district = -1;
    private int[] position = new int[2];
    private List<Taxi> taxis = Collections.emptyList();
    private String administratorServerAddr = null;

    public TaxiInitInfos(int id, int grpcPort, String admServer) {
        this.id = id;
        this.grpcPort = grpcPort;
        this.administratorServerAddr = admServer;
    }

    public TaxiInitInfos(int district, int[] position, List<Taxi> taxis) {
        this.district = id;
        this.position = position;
        this.taxis = taxis;
    }

    @Override
    public String toString() {
        String infos =
                String.format("District [id=%d, grpc-port=%d, district=%s, position=(%d,%d), taxis=",
                id, grpcPort, district, position[0], position[1]);
        infos += "[";
        if (!taxis.isEmpty()) {
            for (Taxi taxi : taxis) {
                infos += "id=" + taxi.getID() + " ";
            }
        }
        infos += "]]";

        return infos;
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

    public List<Taxi> getTaxis() {
        return taxis;
    }

    public void setTaxis(List<Taxi> taxis) {
        this.taxis = taxis;
    }

    public String getAdministratorServerAddr() {
        return administratorServerAddr;
    }

    public void setAdministratorServerAddr(String administratorServerAddr) {
        this.administratorServerAddr = administratorServerAddr;
    }
}
