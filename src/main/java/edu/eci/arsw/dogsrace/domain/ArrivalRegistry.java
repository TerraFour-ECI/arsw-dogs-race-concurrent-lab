package edu.eci.arsw.dogsrace.domain;

import java.util.Objects;

/**
 * Thread-safe arrival registry.
 * <p>
 * Uses {@code synchronized} methods to guarantee absence of race conditions
 * on the shared position counter and winner assignment.
 * <p>
 * An optional <em>arrival threshold</em> ({@code arrivalAlarmCount}) enables
 * the <b>early-stop</b> pattern: once the number of arrivals reaches the
 * threshold, running threads can query {@link #hasReachedThreshold()} and
 * terminate early instead of traversing remaining steps.
 */
public final class ArrivalRegistry {

    private int nextPosition = 1;
    private String winner = null;
    private final int arrivalAlarmCount;

    /**
     * Creates a registry with no early-stop threshold (all runners finish).
     */
    public ArrivalRegistry() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Creates a registry that signals early stop after {@code arrivalAlarmCount}
     * arrivals.
     *
     * @param arrivalAlarmCount number of arrivals that trigger the threshold.
     * @throws IllegalArgumentException if arrivalAlarmCount &lt; 1.
     */
    public ArrivalRegistry(int arrivalAlarmCount) {
        if (arrivalAlarmCount < 1) {
            throw new IllegalArgumentException("arrivalAlarmCount must be >= 1");
        }
        this.arrivalAlarmCount = arrivalAlarmCount;
    }

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
     * Returns {@code true} when the number of registered arrivals has reached
     * or exceeded the configured threshold.
     * <p>
     * Running threads should call this method each iteration and terminate
     * early when it returns {@code true}, implementing the distributed-search
     * stop condition.
     *
     * @return whether the threshold has been reached.
     */
    public synchronized boolean hasReachedThreshold() {
        // (nextPosition - 1) is the count of arrivals so far
        return (nextPosition - 1) >= arrivalAlarmCount;
    }

    /**
     * Immutable snapshot of arrival information.
     * 
     * @param position the assigned position.
     * @param winner the current race winner.
     */
    public record ArrivalSnapshot(int position, String winner) { }
}
