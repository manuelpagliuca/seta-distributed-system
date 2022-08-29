/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. of Computer Science @UNIMI A.Y. 2021/2022 */
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


        List<Measurement> slidingWindow = new ArrayList<>(List.of(measurements));
        //printSlidingWindow();
        leftShift();
        return slidingWindow;
    }

    private void printSlidingWindow() {
        System.out.println("input: ");
        for (Measurement m : measurements) {
            System.out.printf("%.2f ", m.getValue());
        }
        System.out.println();
    }

    private void leftShift() {
        measurements[0] = measurements[4];
        measurements[1] = measurements[5];
        measurements[2] = measurements[6];
        measurements[3] = measurements[7];
        measurements[4] = null;
        measurements[5] = null;
        measurements[6] = null;
        measurements[7] = null;
        index = 4;
    }
}
