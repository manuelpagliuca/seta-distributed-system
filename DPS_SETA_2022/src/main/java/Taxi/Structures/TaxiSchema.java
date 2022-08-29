/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. of Computer Science @UNIMI A.Y. 2021/2022 */
package Taxi.Structures;

import java.io.Serializable;
import java.util.ArrayList;

public class TaxiSchema implements Serializable {
    private TaxiInfo taxiInfo;
    private ArrayList<TaxiInfo> taxis = new ArrayList<>();
    public TaxiInfo getTaxiInfo() {
        return taxiInfo;
    }
    public void setTaxiInfo(TaxiInfo taxiInfo) {
        this.taxiInfo = taxiInfo;
    }
    public ArrayList<TaxiInfo> getTaxis() {
        return taxis;
    }
    public void setTaxis(ArrayList<TaxiInfo> taxis) {
        this.taxis = taxis;
    }
}
