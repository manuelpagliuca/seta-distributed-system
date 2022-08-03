/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. of Computer Science @UNIMI A.Y. 2021/2022 */
package Clients.Taxi;


public class TaxiInfo {
    private int id = -1;
    private int grpcPort = -1;
    private int district = -1;
    private int[] position = new int[2];
    private String administratorServerAddr = null;

    public TaxiInfo(TaxiInfo info) {
        this.id = info.id;
        this.district = info.district;
        this.position = info.position;
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

    public TaxiInfo(int id, int district, int[] position, int grpcPort, String administratorServerAddr) {
        this.id = id;
        this.district = district;
        this.position = position;
        this.grpcPort = grpcPort;
        this.administratorServerAddr = administratorServerAddr;
    }

    public TaxiInfo() {

    }

    @Override
    public String toString() {
        String infos =
                String.format("id=%d, grpc-port=%d, district=%s, position=(%d,%d)",
                        id, grpcPort, district, position[0], position[1]);

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

    public String getAdministratorServerAddr() {
        return administratorServerAddr;
    }

    public void setAdministratorServerAddr(String administratorServerAddr) {
        this.administratorServerAddr = administratorServerAddr;
    }
}
