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
	
	private final JSlider minAccuracy;
	private final JSlider maxExecutionTime;
	private final JSlider nrOfConcepts;
	
	private JRadioButton owlRadioButton;
	private JRadioButton elProfileButton;
	
	private JCheckBox allBox;
	private JCheckBox someBox;
	private JCheckBox notBox;
	private JCheckBox valueBox;
	private JCheckBox moreBox;
	
	private JComboBox countMoreBox;
	
	private JPanel profilePanel;
	private JPanel radioBoxPanel;
	private JPanel checkBoxPanel;
	private JPanel labelPanel;
	private JPanel sliderPanel;
	
	private OptionPanelHandler optionHandler;
	private double accuracy;
	
	/**
	 * Constructor for the Option Panel. 
	 */
	public OptionPanel() {
		setLayout(new BorderLayout());
		optionHandler = new OptionPanelHandler(this);
		labelPanel = new JPanel();
		labelPanel.setLayout(new GridLayout(0, 1));
		sliderPanel = new JPanel();
		sliderPanel.setLayout(new GridLayout(0, 1));
		profilePanel = new JPanel();
		profilePanel.setLayout(new GridLayout(0, 1));
		radioBoxPanel = new JPanel();
		radioBoxPanel.setLayout(new GridLayout(1, 3));
		checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(new GridLayout(1, 6));
		
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
		
		owlRadioButton = new JRadioButton("OWL 2", true);
		elProfileButton = new JRadioButton("EL Profile", false);
		owlRadioButton.setEnabled(true);
		owlRadioButton.addItemListener(optionHandler);
		elProfileButton.addItemListener(optionHandler);
		
		allBox = new JCheckBox("all", true);
		allBox.addItemListener(optionHandler);
		someBox = new JCheckBox("some", true);
		someBox.addItemListener(optionHandler);
		notBox = new JCheckBox("not", true);
		notBox.addItemListener(optionHandler);
		valueBox = new JCheckBox("value", true);
		valueBox.addItemListener(optionHandler);
		moreBox = new JCheckBox("<=x, >=x with max.:", true);
		moreBox.addItemListener(optionHandler);
		
		countMoreBox = new JComboBox();
		countMoreBox.addItem(1);
		countMoreBox.addItem(2);
		countMoreBox.addItem(3);
		countMoreBox.addItem(4);
		countMoreBox.addItem(5);
		countMoreBox.addItem(6);
		countMoreBox.addItem(7);
		countMoreBox.addItem(8);
		countMoreBox.addItem(9);
		countMoreBox.addItem(10);
		countMoreBox.setSelectedItem(5);
		countMoreBox.setEditable(false);
		
		checkBoxPanel.add(allBox);
		checkBoxPanel.add(someBox);
		checkBoxPanel.add(notBox);
		checkBoxPanel.add(valueBox);
		checkBoxPanel.add(moreBox);
		checkBoxPanel.add(countMoreBox);
		
		
		radioBoxPanel.add(owlRadioButton);
		radioBoxPanel.add(elProfileButton);
		
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

	/**
	 * This methode returns the OWLRadioButton.
	 * @return OWLRAdioButton
	 */
	public JRadioButton getOwlRadioButton() {
		return owlRadioButton;
	}

	/**
	 * This methode returns the ELProfileButton.
	 * @return ELProfileButton
	 */
	public JRadioButton getElProfileButton() {
		return elProfileButton;
	}

	/**
	 * This methode returns if the allquantor box is selected.
	 * @return boolean if allquantor box is selected
	 */
	public boolean getAllBox() {
		return allBox.isSelected();
	}

	/**
	 * This methode returns if the some box is selected.
	 * @return boolean if some box is selected
	 */
	public boolean getSomeBox() {
		return someBox.isSelected();
	}

	/**
	 * This methode returns if the not box is selected.
	 * @return boolean if not box is selected
	 */
	public boolean getNotBox() {
		return notBox.isSelected();
	}

	/**
	 * This methode returns if the value box is selected.
	 * @return boolean if value box is selected
	 */
	public boolean getValueBox() {
		return valueBox.isSelected();
	}

	/**
	 * This methode returns the ProfilePanel.
	 * @return Profile Panel
	 */
	public JPanel getProfilePanel() {
		return profilePanel;
	}

	/**
	 * This methode returns the int of the cardinality restriction. 
	 * @return cardinality restriction int
	 */
	public int getCountMoreBox() {
		return Integer.parseInt(countMoreBox.getSelectedItem().toString());
	}

	/**
	 * This methode returns if the cardinality restiction box is selected.
	 * @return boolean if cardinality restiction box is selected
	 */
	public boolean getMoreBox() {
		return moreBox.isSelected();
	}
	
	/**
	 * This methode sets the the checkboxes enable that are needed for
	 * the OWL 2 Profile.
	 */
	public void setToOWLProfile() {
		allBox.setSelected(true);
		someBox.setSelected(true);
		notBox.setSelected(true);
		valueBox.setSelected(true);
		moreBox.setSelected(true);
		this.setCountMoreBoxEnabled(true);
	}
	
	/**
	 * This methode sets the the checkboxes enable that are needed for
	 * the EL Profile.
	 */
	public void setToELProfile() {
		allBox.setSelected(false);
		someBox.setSelected(true);
		notBox.setSelected(false);
		valueBox.setSelected(false);
		moreBox.setSelected(false);
		this.setCountMoreBoxEnabled(false);
	}
	
	/**
	 * This methode sets the combo box for the cardinality restriction
	 * enabled/disabled. 
	 * @param isEnabled
	 */
	public void setCountMoreBoxEnabled(boolean isEnabled) {
		countMoreBox.setEnabled(isEnabled);
	}
	
}
