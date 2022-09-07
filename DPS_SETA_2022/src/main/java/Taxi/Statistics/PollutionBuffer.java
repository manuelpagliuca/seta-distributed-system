/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. in Computer Science @UNIMI A.Y. 2021/2022 */
package Taxi.Statistics;

import Taxi.Statistics.Simulators.Buffer;
import Taxi.Statistics.Simulators.Measurement;

import java.util.ArrayList;
import java.util.List;

/*
 * PollutionBuffer
 * ------------------------------------------------------------------------------
 * This buffer is where the PM10 measurements are saved, it keeps saving the
 * single mesurements in a static array of size 8. Then it uses a sliding window
 * of size 4, where the last 4 measurements in the array are recycled by shifting
 * to the left (and adding the new ones starting from position 5).
 */
public class PollutionBuffer implements Buffer {
    private static final Measurement[] measurements = new Measurement[8];
    private static int index = 0;
    private final Object windowIsFull = new Object();

    public PollutionBuffer() {
    }

    /* Add the measurement to the array of measurements
     * ------------------------------------------------------------------------------
     * Since we are using a sliding window of 4, we have to handle the case when the
     * array is empty. In fact after the global index will reach the last element,
     * i.e. is equal to 7 it will go out of bound. The system will recognize this
     * and will reset the position of the array to the 5th element.
     *
     * So after the first fill of the array the new measurements will be saved only
     * in the rightmost part of the array (measurements[4] to measurements[7]).
     */
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

    /* Return the current measurements and then shift to the left
     * ------------------------------------------------------------------------------
     * This method will wait the array to be filled, once the array is filled it
     * will save a copy of the current array and shift the original to the left, then
     * return the copy.
     */
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
        //printSlidingWindow(); //debug
        leftShift();
        return slidingWindow;
    }

    // Perform the shift to the left of the 4 measurements
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

    // Prints the content of the measurements (debug)
    @SuppressWarnings("unused")
    private void printSlidingWindow() {
        System.out.println("input: ");
        for (Measurement m : measurements) {
            System.out.printf("%.2f ", m.getValue());
        }
        System.out.println();
    }
}