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

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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
	private JLabel owlRadioButtonLabel;
	private JLabel elProfileButtonLabel;
	private JLabel allBoxLabel;
	private JLabel someBoxLabel;
	private JLabel notBoxLabel;
	private JLabel valueBoxLabel;
	private JLabel moreBoxLabel;
	private JLabel lessBoxLabel;
	
	private final JSlider minAccuracy;
	private final JSlider maxExecutionTime;
	private final JSlider nrOfConcepts;
	
	private JRadioButton owlRadioButton;
	private JRadioButton elProfileButton;
	
	private JCheckBox allBox;
	private JCheckBox someBox;
	private JCheckBox notBox;
	private JCheckBox valueBox;
	private JCheckBox lessBox;
	private JCheckBox moreBox;
	
	private JComboBox countLessBox;
	private JComboBox countMoreBox;
	
	private JPanel profilePanel;
	private JPanel radioBoxPanel;
	private JPanel checkBoxPanel;
	private JPanel labelPanel;
	private JPanel sliderPanel;
	
	private double accuracy;
	/**
	 * Constructor for the Option Panel. 
	 */
	public OptionPanel() {
		setLayout(new BorderLayout());
		labelPanel = new JPanel();
		labelPanel.setLayout(new GridLayout(0, 1));
		sliderPanel = new JPanel();
		sliderPanel.setLayout(new GridLayout(0, 1));
		profilePanel = new JPanel();
		profilePanel.setLayout(new GridLayout(0, 1));
		radioBoxPanel = new JPanel();
		radioBoxPanel.setLayout(new GridLayout(1, 4));
		checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(new GridLayout(1, 14));
		
		minAccuracyLabel = new JLabel("noise in %:    ");
		maxExecutionTimeLabel = new JLabel("maximum execution time:    ");
		nrOfConceptsLabel = new JLabel("max. number of results:    ");
		owlRadioButtonLabel = new JLabel("OWL 2");
		elProfileButtonLabel = new JLabel("EL Profile");
		allBoxLabel = new JLabel("all");
		someBoxLabel = new JLabel("some");
		notBoxLabel = new JLabel("not");
		valueBoxLabel = new JLabel("value");
		lessBoxLabel = new JLabel("<=x with max.:");
		moreBoxLabel = new JLabel(">=x with max.:");
		
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
		
		owlRadioButton = new JRadioButton();
		elProfileButton = new JRadioButton();
		owlRadioButton.setEnabled(true);
		
		allBox = new JCheckBox();
		someBox = new JCheckBox();
		notBox = new JCheckBox();
		valueBox = new JCheckBox();
		lessBox = new JCheckBox();
		moreBox = new JCheckBox();
		
		countLessBox = new JComboBox();
		countLessBox.setEditable(false);
		countMoreBox = new JComboBox();
		countMoreBox.setEditable(false);
		
		checkBoxPanel.add(allBox);
		checkBoxPanel.add(allBoxLabel);
		checkBoxPanel.add(someBox);
		checkBoxPanel.add(someBoxLabel);
		checkBoxPanel.add(notBox);
		checkBoxPanel.add(notBoxLabel);
		checkBoxPanel.add(valueBox);
		checkBoxPanel.add(valueBoxLabel);
		checkBoxPanel.add(lessBox);
		checkBoxPanel.add(lessBoxLabel);
		checkBoxPanel.add(countLessBox);
		checkBoxPanel.add(moreBox);
		checkBoxPanel.add(moreBoxLabel);
		checkBoxPanel.add(countMoreBox);
		
		
		radioBoxPanel.add(owlRadioButton);
		radioBoxPanel.add(owlRadioButtonLabel);
		radioBoxPanel.add(elProfileButton);
		radioBoxPanel.add(elProfileButtonLabel);
		
		profilePanel.add(radioBoxPanel);
		profilePanel.add(checkBoxPanel);
		
		labelPanel.add(minAccuracyLabel);
		labelPanel.add(maxExecutionTimeLabel);
		labelPanel.add(nrOfConceptsLabel);
		
		sliderPanel.add(minAccuracy);
		sliderPanel.add(maxExecutionTime);
		sliderPanel.add(nrOfConcepts);
		
		add(BorderLayout.SOUTH, profilePanel);
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

	public void setOwlRadioButton(JRadioButton owlRadioButton) {
		this.owlRadioButton = owlRadioButton;
	}

	public JRadioButton getOwlRadioButton() {
		return owlRadioButton;
	}

	public void setElProfileButton(JRadioButton elProfileButton) {
		this.elProfileButton = elProfileButton;
	}

	public JRadioButton getElProfileButton() {
		return elProfileButton;
	}

	public void setAllBox(JCheckBox allBox) {
		this.allBox = allBox;
	}

	public JCheckBox getAllBox() {
		return allBox;
	}

	public void setSomeBox(JCheckBox someBox) {
		this.someBox = someBox;
	}

	public JCheckBox getSomeBox() {
		return someBox;
	}

	public void setNotBox(JCheckBox notBox) {
		this.notBox = notBox;
	}

	public JCheckBox getNotBox() {
		return notBox;
	}

	public void setValueBox(JCheckBox valueBox) {
		this.valueBox = valueBox;
	}

	public JCheckBox getValueBox() {
		return valueBox;
	}

	public void setLessBox(JCheckBox lessBox) {
		this.lessBox = lessBox;
	}

	public JCheckBox getLessBox() {
		return lessBox;
	}

	public JPanel getProfilePanel() {
		return profilePanel;
	}

	public void setCountLessBox(JComboBox countLessBox) {
		this.countLessBox = countLessBox;
	}

	public JComboBox getCountLessBox() {
		return countLessBox;
	}

	public void setCountMoreBox(JComboBox countMoreBox) {
		this.countMoreBox = countMoreBox;
	}

	public JComboBox getCountMoreBox() {
		return countMoreBox;
	}

	public void setMoreBox(JCheckBox moreBox) {
		this.moreBox = moreBox;
	}

	public JCheckBox getMoreBox() {
		return moreBox;
	}
	
}
