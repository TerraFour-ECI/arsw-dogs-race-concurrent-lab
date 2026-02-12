package edu.eci.arsw.dogsrace.domain;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe arrival registry.
 * <p>
 * Uses {@link AtomicInteger} for the position counter to guarantee absence of
 * race conditions without heavy synchronization.  Minimal {@code synchronized}
 * is kept only for the compound winner-assignment (read-then-write on two
 * fields), following the lab requirement of using {@code AtomicInteger} or
 * minimal synchronization over the critical section of the counter.
 * <p>
 * An optional <em>arrival threshold</em> ({@code arrivalAlarmCount}) enables
 * the <b>early-stop</b> pattern: once the number of arrivals reaches the
 * threshold, running threads can query {@link #hasReachedThreshold()} and
 * terminate early instead of traversing remaining steps.
 */
public final class ArrivalRegistry {

    private final AtomicInteger nextPosition = new AtomicInteger(1);
    private final int arrivalAlarmCount;

    /** Lock object used only for the compound winner assignment. */
    private final Object winnerLock = new Object();
    private String winner = null;

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
     * <p>
     * The position counter is incremented atomically via
     * {@link AtomicInteger#getAndIncrement()}, so no lock is needed for the
     * counter itself.  The winner assignment is a compound check-then-act and
     * uses minimal synchronization.
     *
     * @param dogName the name of the arriving greyhound.
     * @return snapshot containing the assigned position and current winner.
     * @throws NullPointerException if dogName is null.
     */
    public ArrivalSnapshot registerArrival(String dogName) {
        Objects.requireNonNull(dogName, "dogName");

        // Atomic increment — no race condition on the counter
        final int position = nextPosition.getAndIncrement();

        // Minimal synchronization only for the compound winner assignment
        synchronized (winnerLock) {
            if (winner == null) {
                winner = dogName;
            }
            return new ArrivalSnapshot(position, winner);
        }
    }

    /**
     * @return the next position to be assigned (read is atomic).
     */
    public int getNextPosition() {
        return nextPosition.get();
    }

    /**
     * @return the name of the winning greyhound, or null if no arrivals yet.
     */
    public String getWinner() {
        synchronized (winnerLock) {
            return winner;
        }
    }

    /**
     * Returns the configured arrival alarm count (threshold).
     *
     * @return the threshold value.
     */
    public int getArrivalAlarmCount() {
        return arrivalAlarmCount;
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
    public boolean hasReachedThreshold() {
        // (nextPosition - 1) is the count of arrivals so far
        return (nextPosition.get() - 1) >= arrivalAlarmCount;
    }

    /**
     * Immutable snapshot of arrival information.
     *
     * @param position the assigned position.
     * @param winner   the current race winner.
     */
    public record ArrivalSnapshot(int position, String winner) { }
}
