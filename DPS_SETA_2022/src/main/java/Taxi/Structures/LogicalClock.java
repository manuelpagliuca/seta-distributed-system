/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. of Computer Science @UNIMI A.Y. 2021/2022 */
package Taxi.Structures;

import java.util.Calendar;

public class LogicalClock {
    private long clock;
    private final long RAND_CLOCK_OFFSET;
    private final Calendar calendar;

    public LogicalClock(long off) {
        calendar = Calendar.getInstance();
        clock = System.currentTimeMillis();
        RAND_CLOCK_OFFSET = off;
    }

    public String printCalendar() {
        calendar.setTimeInMillis(clock);
        return String.format(calendar.get(Calendar.HOUR_OF_DAY) + ":" +
                calendar.get(Calendar.MINUTE) + ":" +
                calendar.get(Calendar.SECOND) + ":" +
                calendar.get(Calendar.MILLISECOND));

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
