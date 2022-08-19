package Schemes;

import Client.TaxiInfo;

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
