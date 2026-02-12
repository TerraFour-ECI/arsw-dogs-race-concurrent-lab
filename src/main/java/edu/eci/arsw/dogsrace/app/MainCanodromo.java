package edu.eci.arsw.dogsrace.app;

import edu.eci.arsw.dogsrace.control.RaceControl;
import edu.eci.arsw.dogsrace.domain.ArrivalRegistry;
import edu.eci.arsw.dogsrace.threads.Galgo;
import edu.eci.arsw.dogsrace.ui.Canodromo;

import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Entry point (UI + orchestration).
 * <p>
 * Supports an <b>arrival threshold</b> via the system property
 * {@code -Dthreshold=N}.  When set, once N greyhounds arrive all remaining
 * threads terminate early (distributed-search stop condition).
 * Default: no threshold (all greyhounds finish the race).
 *
 * NOTE: the start action runs in a separate thread so the Swing UI thread is not blocked.
 */
public final class MainCanodromo {

    private static Galgo[] galgos;
    private static Canodromo can;

    /** Threshold read from -Dthreshold (0 or absent = no early stop). */
    private static final int THRESHOLD = Integer.getInteger("threshold", 0);

    private static final ArrivalRegistry registry =
            THRESHOLD > 0 ? new ArrivalRegistry(THRESHOLD) : new ArrivalRegistry();
    private static final RaceControl control = new RaceControl();

    public static void main(String[] args) {
        can = new Canodromo(17, 100);
        galgos = new Galgo[can.getNumCarriles()];
        can.setVisible(true);

        can.setStartAction(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                ((JButton) e.getSource()).setEnabled(false);

                new Thread(() -> {
                    // 1) create and start all runners
                    for (int i = 0; i < can.getNumCarriles(); i++) {
                        galgos[i] = new Galgo(can.getCarril(i), String.valueOf(i), registry, control);
                        galgos[i].start();
                    }

                    // 2) wait for all threads (join)
                    for (Galgo g : galgos) {
                        try {
                            g.join();
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }

                    // 3) show results ONLY after all threads finished
                    String winner = registry.getWinner();
                    int totalFinished = registry.getNextPosition() - 1;
                    int totalRunners = galgos.length;

                    // Count how many were stopped early by the threshold
                    int stoppedEarly = 0;
                    for (Galgo g : galgos) {
                        if (g.wasStoppedEarly()) stoppedEarly++;
                    }

                    if (THRESHOLD > 0) {
                        System.out.printf(
                                "=== Early-stop active (threshold=%d) ===%n"
                              + "  Arrivals: %d / %d runners%n"
                              + "  Stopped early: %d greyhounds%n"
                              + "  Winner: %s%n",
                                THRESHOLD, totalFinished, totalRunners,
                                stoppedEarly, winner);
                    }

                    can.winnerDialog(winner, totalFinished);
                    System.out.println("The winner was: " + winner);
                }, "race-orchestrator").start();
            }
        });

        can.setStopAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                control.pause();
                System.out.println("Race paused!");
            }
        });

        can.setContinueAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                control.resume();
                System.out.println("Race resumed!");
            }
        });
    }
}
