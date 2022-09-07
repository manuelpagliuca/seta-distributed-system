/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. in Computer Science @UNIMI A.Y. 2021/2022 */
package Taxi.Structures;

import java.io.Serializable;
import java.util.ArrayList;

/*
 * TaxiSchema
 * ------------------------------------------------------------------------------
 * This class act as a wrapper for the taxi information and his list of other
 * taxis.
 */
public class TaxiSchema implements Serializable {
    private TaxiInfo taxiInfo;
    private ArrayList<TaxiInfo> taxis = new ArrayList<>();

    public TaxiSchema(TaxiInfo thisTaxi, ArrayList<TaxiInfo> otherTaxis) {
        taxiInfo = thisTaxi;
        taxis = otherTaxis;
    }

    public TaxiSchema(){
    }

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