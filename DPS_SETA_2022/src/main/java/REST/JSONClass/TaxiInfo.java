/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. of Computer Science @UNIMI A.Y. 2021/2022 */
package REST.JSONClass;

import java.io.Serializable;
import java.util.ArrayList;

public class TaxiInfo implements Serializable {
    private int id = -1;
    private int grpcPort = -1;
    private int district = -1;
    private int[] position = new int[2];
    private ArrayList<TaxiInfo> taxis = new ArrayList<>();
    private String administratorServerAddr = null;

    public TaxiInfo(TaxiInfo info) {
        this.id = info.id;
        this.district = info.district;
        this.position = info.position;
        this.taxis = info.taxis;
        this.grpcPort = info.grpcPort;
        this.administratorServerAddr = info.administratorServerAddr;
    }

    public TaxiInfo(int id) {
        this.id = id;
    }

    public TaxiInfo(int id, int grpcPort, String admServer) {
        this.id = id;
        this.grpcPort = grpcPort;
        this.administratorServerAddr = admServer;
    }

    public TaxiInfo(int id, int district, int[] position, ArrayList<TaxiInfo> taxis, int grpcPort,
                    String administratorServerAddr) {
        this.id = id;
        this.district = district;
        this.position = position;
        this.taxis = taxis;
        this.grpcPort = grpcPort;
        this.administratorServerAddr = administratorServerAddr;
    }

    public TaxiInfo() {

    }

    @Override
    public String toString() {
        String infos =
                String.format("[id=%d, grpc-port=%d, district=%s, position=(%d,%d), taxis=",
                        id, grpcPort, district, position[0], position[1]);
        infos += "[";
        if (!taxis.isEmpty()) {
            for (TaxiInfo e : taxis)
                infos += "id=" + e.getId() + ",";
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

    public ArrayList<TaxiInfo> getTaxis() {
        return taxis;
    }

    public void setTaxis(ArrayList<TaxiInfo> taxis) {
        this.taxis = taxis;
    }

    public String getAdministratorServerAddr() {
        return administratorServerAddr;
    }

    public void setAdministratorServerAddr(String administratorServerAddr) {
        this.administratorServerAddr = administratorServerAddr;
    }
}
