package edu.eci.arsw.dogsrace.domain;

import java.util.Objects;

/**
 * Thread-safe arrival registry.
 * Critical section is limited to the position assignment and winner selection.
 */
public final class ArrivalRegistry {

    private int nextPosition = 1;
    private String winner = null;

    /**
     * Registers a greyhound's arrival and assigns its position.
     * The first arrival is recorded as the winner.
     * 
     * @param dogName the name of the arriving greyhound.
     * @return snapshot containing the assigned position and current winner.
     * @throws NullPointerException if dogName is null.
     */
    public synchronized ArrivalSnapshot registerArrival(String dogName) {
        Objects.requireNonNull(dogName, "dogName");
        final int position = nextPosition++;
        if (position == 1) {
            winner = dogName;
        }
        return new ArrivalSnapshot(position, winner);
    }

    /**
     * @return the next position to be assigned.
     */
    public synchronized int getNextPosition() {
        return nextPosition;
    }

    /**
     * @return the name of the winning greyhound, or null if no arrivals yet.
     */
    public synchronized String getWinner() {
        return winner;
    }

    /**
     * Immutable snapshot of arrival information.
     * 
     * @param position the assigned position.
     * @param winner the current race winner.
     */
    public record ArrivalSnapshot(int position, String winner) { }
}
