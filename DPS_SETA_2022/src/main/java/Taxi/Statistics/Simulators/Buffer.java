package Taxi.Statistics.Simulators;

import java.util.List;

public interface Buffer {
    void addMeasurement(Measurement m);

    List<Measurement> readAllAndClean();

}
