/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. in Computer Science @UNIMI A.Y. 2021/2022 */
package Taxi.Structures;

/*
 * LogicalClock
 * ------------------------------------------------------------------------------
 * A class for handlingl the logical clock of the taxi, the real utility is
 * to return the timestamp value of the class and to increment the value by a
 * randomly initialized fixed value.
 */
public class LogicalClock {
    private long clock;
    private final long RAND_CLOCK_OFFSET;

    public LogicalClock(long off) {
        clock = System.currentTimeMillis();
        RAND_CLOCK_OFFSET = off;
    }

    public void increment() {
        clock += RAND_CLOCK_OFFSET;
    }

    public long getLogicalClock() {
        return clock;
    }

    public void setLogicalClock(long val) {
        clock = val;
    }
}