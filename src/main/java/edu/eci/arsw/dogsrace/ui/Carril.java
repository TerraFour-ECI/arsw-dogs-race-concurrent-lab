package edu.eci.arsw.dogsrace.ui;

import java.awt.Color;

import javax.swing.JButton;

/**
 * A lane of the greyhound track
 * 
 * @author rlopez
 * 
 */
public class Carril {
	private Color on = Color.CYAN;
	private Color off = Color.LIGHT_GRAY;
	private Color stop = Color.red;
	private Color start = Color.GREEN;
	/**
	 * Steps of the lane
	 */
	private JButton[] paso;

	/**
	 * Finish flag of the lane
	 */
	private JButton llegada;

	private String name;

	/**
	 * Builds a lane
	 * 
	 * @param nPasos
	 *            Number of steps in the lane
	 * @param name
	 *            Name of the lane
	 */
	public Carril(int nPasos, String name) {
		paso = new JButton[nPasos];
		JButton bTmp;
		for (int k = 0; k < nPasos; k++) {
			bTmp = new JButton();
			bTmp.setBackground(off);
			paso[k] = bTmp;
		}
		llegada = new JButton(name);
		llegada.setBackground(start);
		this.name = name;
	}

	/**
	 * Lane size in number of steps
	 * 
	 * @return
	 */
	public int size() {
		return paso.length;
	}

	public String getName() {
		return llegada.getText();
	}

	/**
	 * Returns the i-th step of the lane
	 * 
	 * @param i
	 * @return
	 */
	public JButton getPaso(int i) {
		return paso[i];
	}

	/**
	 * Returns the finish flag of the lane
	 * 
	 * @return
	 */
	public JButton getLlegada() {
		return llegada;
	}

	/**
	 * Indicates that step i has been used
	 * 
	 * @param i
	 */
	public void setPasoOn(int i) {
		paso[i].setText("o");
	}

	/**
	 * Indicates that step i has not been used
	 * 
	 * @param i
	 */
	public void setPasoOff(int i) {
		paso[i].setText("");
	}

	/**
	 * Indicates that the end of the lane has been reached
	 */
	public void finish() {
		llegada.setText("!");
	}

	public void displayPasos(int n) {
		llegada.setText("" + n);
	}

	/**
	 * Restarts the lane: no step has been used, flag down.
	 */
	public void reStart() {
		for (int k = 0; k < paso.length; k++) {
			paso[k].setBackground(off);
		}
		llegada.setBackground(start);
		llegada.setText(name);
	}
}
