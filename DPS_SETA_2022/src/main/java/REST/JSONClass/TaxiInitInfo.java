package REST.JSONClass;

import REST.Client.Taxi;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class TaxiInitInfo implements Serializable {
    private int id = -1;

    private int grpcPort = -1;
    private int district = -1;
    private int[] position = new int[2];
    private HashMap<Integer, Taxi> taxis = new HashMap<>();
    private String administratorServerAddr = null;

    public TaxiInitInfo(int id, int grpcPort, String admServer) {
        this.id = id;
        this.grpcPort = grpcPort;
        this.administratorServerAddr = admServer;
    }

    public TaxiInitInfo(int district, int[] position, HashMap<Integer, Taxi> taxis) {
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
            for (Map.Entry<Integer, Taxi> e : taxis.entrySet())
                infos += "id=" + e.getKey() + ",";
        }

        if (infos.endsWith(",")) {
            infos = infos.substring(0, infos.length() - 1);
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

    public HashMap<Integer, Taxi> getTaxis() {
        return taxis;
    }

    public void setTaxis(HashMap<Integer, Taxi> taxis) {
        this.taxis = taxis;
    }

    public String getAdministratorServerAddr() {
        return administratorServerAddr;
    }

    public void setAdministratorServerAddr(String administratorServerAddr) {
        this.administratorServerAddr = administratorServerAddr;
    }
}
