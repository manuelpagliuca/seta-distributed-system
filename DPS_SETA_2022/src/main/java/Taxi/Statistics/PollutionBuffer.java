package Taxi.Statistics;

import Taxi.Statistics.Simulators.Buffer;
import Taxi.Statistics.Simulators.Measurement;

import java.util.ArrayList;
import java.util.List;

public class PollutionBuffer implements Buffer {
    private static final Measurement[] measurements = new Measurement[8];
    private static int index = 0;
    private final Object windowIsFull = new Object();

    public PollutionBuffer() {
    }

    @Override
    public void addMeasurement(Measurement m) {
        if (index >= 8) {
            index = 4;
        }

        measurements[index] = m;
        index++;

        if (index > 7) {
            synchronized (windowIsFull) {
                windowIsFull.notify();
            }
        }
    }

    @Override
    public List<Measurement> readAllAndClean() {
        synchronized (windowIsFull) {
            try {
                windowIsFull.wait();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        Measurement[] window = new Measurement[4];

        System.arraycopy(measurements, 0, window, 0, window.length);
        printInputData();
        printArray(window);
        leftShift();

        return new ArrayList<>(List.of(window));
    }

    private void printInputData() {
        System.out.println("input: ");
        for (Measurement m : measurements) {
            System.out.printf("%.2f ", m.getValue());
        }
        System.out.println();
    }

    private void printArray(Measurement[] array) {
        for (Measurement measurement : array) {
            System.out.printf("%.2f ", measurement.getValue());
        }
        System.out.println();
    }

    private void leftShift() {
        measurements[0] = measurements[2];
        measurements[1] = measurements[3];
        measurements[2] = measurements[4];
        measurements[3] = measurements[5];
        measurements[4] = measurements[6];
        measurements[5] = measurements[7];
        index = 4;
    }
}
