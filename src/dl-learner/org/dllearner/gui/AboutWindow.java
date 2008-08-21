package org.dllearner.gui;

import javax.swing.JFrame;

/**
 * Window displaying some information about DL-Learner and DL-Learner GUI.
 * 
 * @author Jens Lehmann
 *
 */
public class AboutWindow extends JFrame {

	private static final long serialVersionUID = -5448814141333659068L;

	public AboutWindow() {
		setTitle("About");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationByPlatform(true);
		setSize(300, 300);
		
		// display icon
		
		// display build version
		
		// display authors of GUI
		
		// display DL-Learner contributors
		
		setVisible(true);
	}
	
}
