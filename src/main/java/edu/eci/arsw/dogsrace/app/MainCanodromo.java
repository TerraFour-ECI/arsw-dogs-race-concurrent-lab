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
 *
 * NOTE: the start action runs in a separate thread so the Swing UI thread is not blocked.
 */
public final class MainCanodromo {

    private static Galgo[] galgos;
    private static Canodromo can;

    private static final ArrivalRegistry registry = new ArrivalRegistry();
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
                    int total = registry.getNextPosition() - 1;

                    can.winnerDialog(winner, total);
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
