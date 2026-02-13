package edu.eci.arsw.dogsrace.threads;

import edu.eci.arsw.dogsrace.control.RaceControl;
import edu.eci.arsw.dogsrace.domain.ArrivalRegistry;
import edu.eci.arsw.dogsrace.ui.Carril;
import edu.eci.arsw.dogsrace.util.RandomGenerator;

/**
 * A runner (greyhound) in the race.
 * <p>
 * Implements the <b>distributed search / early-stop</b> pattern: each
 * greyhound checks {@link ArrivalRegistry#hasReachedThreshold()} every
 * iteration and terminates early when the threshold of arrivals has been
 * reached, instead of traversing all remaining steps.
 */
public class Galgo extends Thread {

    private final Carril carril;
    private final ArrivalRegistry registry;
    private final RaceControl control;

    private int paso = 0;
    /** True if this greyhound was stopped early by the threshold. */
    private volatile boolean stoppedEarly = false;

    /** Min delay per step in ms. */
    private static final int MIN_STEP_DELAY = 50;
    /** Max additional delay per step in ms (total = MIN + [0, MAX_EXTRA)). */
    private static final int MAX_EXTRA_DELAY = 100;

    public Galgo(Carril carril, String name, ArrivalRegistry registry, RaceControl control) {
        super(name);
        this.carril = carril;
        this.registry = registry;
        this.control = control;
    }

    private void corra() throws InterruptedException {
        while (paso < carril.size()) {
            control.awaitIfPaused();

            // --- Early-stop condition (distributed search pattern) ---
            // Check the shared AtomicInteger counter each iteration:
            // if enough arrivals have been registered, this thread
            // terminates early without traversing remaining steps.
            if (registry.hasReachedThreshold()) {
                stoppedEarly = true;
                System.out.printf("Greyhound %s stopped early at step %d/%d (threshold reached)%n",
                        getName(), paso, carril.size());
                break;
            }

            // Random delay per step so greyhounds run at different speeds.
            // This makes the early-stop observable: fast dogs finish first,
            // slower dogs are still mid-track when the threshold is reached.
            Thread.sleep((long) MIN_STEP_DELAY + RandomGenerator.nextInt(MAX_EXTRA_DELAY));
            carril.setPasoOn(paso++);
            carril.displayPasos(paso);

            if (paso == carril.size()) {
                carril.finish();
                var snapshot = registry.registerArrival(getName());
                System.out.printf("Greyhound %s arrived in position %d%n", getName(), snapshot.position());
            }
        }
    }

    /**
     * @return true if this greyhound was stopped before finishing the track
     *         because the arrival threshold was reached.
     */
    public boolean wasStoppedEarly() {
        return stoppedEarly;
    }

    @Override
    public void run() {
        try {
            corra();
        } catch (InterruptedException e) {
            // Restore interruption status and exit
            Thread.currentThread().interrupt();
        }
    }
}
