package Clients.SETA;

import java.io.Serializable;

public class RideInfo implements Serializable {
    public int id;
    public int[] startPosition = new int[2];
    public int[] destinationPosition = new int[2];

    // Utility
    public int getDistrict(int[] position) {
        if (position.length != 2)
            return -1;

        if (position[0] < 5 && position[1] < 5)
            return 1;
        else if (position[0] > 5 && position[1] < 5)
            return 2;
        else if (position[0] < 5 && position[1] > 5)
            return 3;
        return 4;
    }

    public int getStartingDistrict() {
        return getDistrict(startPosition);
    }

    public int getDestinationDistrict() {
        return getDistrict(destinationPosition);
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
