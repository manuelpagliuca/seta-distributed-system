/* Project for the course of "Distributed and Pervasive Systems"
 * Mat. Number 975169
 * Manuel Pagliuca
 * M.Sc. in Computer Science @UNIMI A.Y. 2021/2022 */
package SETA.Structures;

import java.io.Serializable;

/*
 * RideInfo
 * ------------------------------------------------------------------------------
 * This class is the structure of ride message, it will be used
 * for the generation of the rides and for the marshalling/unmarshalling
 * through Gson (JSON) of the MQTT messages to the broker.
 *
 * This will be used both for the sending that for the receiving of the
 * rides messages (from Taxi and SETA).
 */
public class RideInfo implements Serializable {
    private int id;
    private int[] startPosition = new int[2];
    private int[] destinationPosition = new int[2];

    /// Utility

    // Get the starting district of the ride
    public int getStartingDistrict() {
        return getDistrict(startPosition);
    }

    // Get the destination district of the ride
    public int getDestinationDistrict() {
        return getDistrict(destinationPosition);
    }

    // Get the district of the ride given the position, usable only from member functions
    private int getDistrict(int[] position) {
        if (position.length != 2)
            return -1;

        if (position[0] < 5 && position[1] < 5)
            return 1;
        else if (position[0] < 5)
            return 2;
        else if (position[1] < 5)
            return 4;
        else
            return 3;
    }

    // Getters & Setters
    public int[] getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int[] startPosition) {
        this.startPosition = startPosition;
    }

    public int[] getDestinationPosition() {
        return destinationPosition;
    }

    public void setDestinationPosition(int[] destinationPosition) {
        this.destinationPosition = destinationPosition;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Ride " + id + ", from ("
                + startPosition[0] + ", " + startPosition[1] + ") to ("
                + destinationPosition[0] + ", " + destinationPosition[1] + ")";
    }
}
