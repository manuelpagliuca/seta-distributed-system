package REST.JSONClass;

import REST.Client.Taxi;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;


public class TaxiInitInfos implements Serializable {
    private long district;
    private int[] position = new int[2];
    private List<Taxi> taxis = Collections.emptyList();

    public TaxiInitInfos(long district, int[] position, List<Taxi> taxis) {
    }

    public long getDistrict() {
        return district;
    }

    public List<Taxi> getListOfTaxis() {
        return taxis;
    }

    public int[] getInitialPosition() {
        return position;
    }

    @Override
    public String toString() {
        String infos = String.format("District [district=%s, position=(%d,%d), taxis=",
                district, position[0], position[1]);
        infos += "[";
        if (!taxis.isEmpty()) {
            for (Taxi taxi : taxis) {
                infos += "id=" + taxi.getID() + " ";
            }
        }
        infos += "]]";

        return infos;
    }
}
