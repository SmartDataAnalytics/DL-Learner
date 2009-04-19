/**
 * Copyright (C) 2007-2009, Jens Lehmann
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

import java.awt.BorderLayout;
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
	private final JLabel minAccuracyLabel;
	private final JLabel maxExecutionTimeLabel;
	private final JLabel nrOfConceptsLabel;
	private final JSlider minAccuracy;
	private final JSlider maxExecutionTime;
	private final JSlider nrOfConcepts;
	private JPanel labelPanel;
	private JPanel sliderPanel;
	private double accuracy;
	/**
	 * Constructor for the Option Panel. 
	 */
	public OptionPanel() {
		setLayout(new BorderLayout());
		labelPanel = new JPanel();
		labelPanel.setLayout(new GridLayout(0,1));
		sliderPanel = new JPanel();
		sliderPanel.setLayout(new GridLayout(0,1));
		minAccuracyLabel = new JLabel("noise in %:    ");
		maxExecutionTimeLabel = new JLabel("maximum execution time:    ");
		nrOfConceptsLabel = new JLabel("max. number of results:    ");
		
		minAccuracy = new JSlider(0, 50, 5);
		minAccuracy.setPaintTicks(true);
		minAccuracy.setMajorTickSpacing(10);
		minAccuracy.setMinorTickSpacing(1);
		minAccuracy.setPaintLabels(true);

		
		maxExecutionTime = new JSlider(0, 40, 8);
		maxExecutionTime.setPaintTicks(true);
		maxExecutionTime.setMajorTickSpacing(10);
		maxExecutionTime.setMinorTickSpacing(1);
		maxExecutionTime.setPaintLabels(true);

		
		nrOfConcepts = new JSlider(2, 20, 10);
		nrOfConcepts.setPaintTicks(true);
		nrOfConcepts.setMajorTickSpacing(2);
		nrOfConcepts.setMinorTickSpacing(1);
		nrOfConcepts.setPaintLabels(true);

		labelPanel.add(minAccuracyLabel);
		labelPanel.add(maxExecutionTimeLabel);
		labelPanel.add(nrOfConceptsLabel);
		
		sliderPanel.add(minAccuracy);
		sliderPanel.add(maxExecutionTime);
		sliderPanel.add(nrOfConcepts);
		add(BorderLayout.WEST, labelPanel);
		add(BorderLayout.CENTER, sliderPanel);
	}
	
	/**
	 * This method returns the min accuracy chosen in the slider.
	 * @return double minAccuracy
	 */
	public double getMinAccuracy() {
		double acc = minAccuracy.getValue();
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
