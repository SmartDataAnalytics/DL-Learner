/**
 * Copyright (C) 2007-2008, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.dllearner.tools.protege;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
/**
 * This Class is responsible for the Options of the DL-Learner.
 * @author Christian Koetteritzsch
 *
 */
public class OptionPanel extends JPanel {

	
	private static final long serialVersionUID = 2190682281812478244L;
	private JLabel minAccuracyLabel;
	private JLabel maxExecutionTimeLabel;
	private JLabel nrOfConceptsLabel;
	private JSlider minAccuracy;
	private JSlider maxExecutionTime;
	private JSlider nrOfConcepts;
	private JPanel optionPanel;
	private double accuracy;
	/**
	 * Construktor for the Option Panel. 
	 */
	public OptionPanel() {

		setPreferredSize(new Dimension(490, 120));
		setLayout(new GridLayout(0, 1));
		optionPanel = new JPanel(new GridLayout(0, 2));
		minAccuracyLabel = new JLabel("minimum accuracy");
		maxExecutionTimeLabel = new JLabel("maximum execution time");
		nrOfConceptsLabel = new JLabel("maximum number of results");
		
		minAccuracy = new JSlider(50, 100, 90);
		minAccuracy.setPaintTicks(true);
		minAccuracy.setMajorTickSpacing(10);
		minAccuracy.setMinorTickSpacing(1);
		minAccuracy.setPaintLabels(true);

		
		maxExecutionTime = new JSlider(2, 20, 3);
		maxExecutionTime.setPaintTicks(true);
		maxExecutionTime.setMajorTickSpacing(5);
		maxExecutionTime.setMinorTickSpacing(1);
		maxExecutionTime.setPaintLabels(true);

		
		nrOfConcepts = new JSlider(2, 20, 10);
		nrOfConcepts.setPaintTicks(true);
		nrOfConcepts.setMajorTickSpacing(2);
		nrOfConcepts.setMinorTickSpacing(1);
		nrOfConcepts.setPaintLabels(true);

		optionPanel.add(minAccuracyLabel);
		optionPanel.add(minAccuracy);
		optionPanel.add(maxExecutionTimeLabel);
		optionPanel.add(maxExecutionTime);
		optionPanel.add(nrOfConceptsLabel);
		optionPanel.add(nrOfConcepts);
		add(optionPanel);
		
	}
	
	/**
	 * This method returns the min accuracy chosen in the slider.
	 * @return double minAccuracy
	 */
	public double getMinAccuracy() {
		int acc = minAccuracy.getValue();
		accuracy = (acc/100.0);
		return accuracy;
	}
	
	/**
	 * This method returns the max executiontime chosen in the slider.
	 * @return int maxExecutionTime
	 */
	public int getMaxExecutionTime() {
		return maxExecutionTime.getValue();
	}
	
	/**
	 * This method returns the nr. of concepts chosen in the slider.
	 * @return int nrOfConcepts
	 */
	public int getNrOfConcepts() {
		return nrOfConcepts.getValue();
	}
}
