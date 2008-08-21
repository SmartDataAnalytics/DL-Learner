package org.dllearner.gui;

import javax.swing.JFrame;

/**
 * Window displaying a tutorial for the DL-Learner GUI.
 * 
 * @author Jens Lehmann
 *
 */
public class TutorialWindow extends JFrame {

	private static final long serialVersionUID = 9152567539729126842L;

	public TutorialWindow() {
		setTitle("Quick Tutorial");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationByPlatform(true);
		setSize(300, 500);
		
		// display tutorial text
		
		setVisible(true);
	}	
	
}
