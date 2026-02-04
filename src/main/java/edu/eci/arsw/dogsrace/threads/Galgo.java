package edu.eci.arsw.dogsrace.threads;

import edu.eci.arsw.dogsrace.control.RaceControl;
import edu.eci.arsw.dogsrace.domain.ArrivalRegistry;
import edu.eci.arsw.dogsrace.ui.Carril;

/**
 * A runner (greyhound) in the race.
 */
public class Galgo extends Thread {

    private final Carril carril;
    private final ArrivalRegistry registry;
    private final RaceControl control;

    private int paso = 0;

    public Galgo(Carril carril, String name, ArrivalRegistry registry, RaceControl control) {
        super(name);
        this.carril = carril;
        this.registry = registry;
        this.control = control;
    }

    private void corra() throws InterruptedException {
        while (paso < carril.size()) {
            control.awaitIfPaused();

            Thread.sleep(100);
            carril.setPasoOn(paso++);
            carril.displayPasos(paso);

            if (paso == carril.size()) {
                carril.finish();
                var snapshot = registry.registerArrival(getName());
                System.out.printf("Greyhound %s arrived in position %d%n", getName(), snapshot.position());
            }
        }
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
