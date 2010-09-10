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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
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
	
	private final JSlider noiseInPercentage;
	private final JSlider maxExecutionTimeInSeconds;
	private final JSlider maxNumberOfResults;
	
	private JRadioButton owlRadioButton;
	private JRadioButton elProfileButton;
	private JRadioButton defaultProfileButton;
	
	private JCheckBox allBox;
	private JCheckBox someBox;
	private JCheckBox notBox;
	private JCheckBox valueBox;
	private JCheckBox cardinalityBox;
	
	private JComboBox cardinalityLimitBox;
	
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
		
		setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), 
				BorderFactory.createTitledBorder("Options")));
		setLayout(new BorderLayout());
		optionHandler = new OptionPanelHandler(this);
		labelPanel = new JPanel();
		labelPanel.setLayout(new GridLayout(0, 1));
		sliderPanel = new JPanel();
		sliderPanel.setLayout(new GridLayout(0, 1));
		profilePanel = new JPanel();
		profilePanel.setLayout(new GridLayout(0, 1));
		radioBoxPanel = new JPanel();
		radioBoxPanel.setLayout(new FlowLayout());
		checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(new GridBagLayout());
		
		JLabel noiseInPercentageLabel = new JLabel("<html>noise in %:    </html>");
		JLabel maxExecutionTimeLabel = new JLabel("<html>maximum execution time:    </html>");
		JLabel nrOfConceptsLabel = new JLabel("<html>max. number of results:    </html>");
		
		noiseInPercentage = new JSlider(0, 50, 5);
		noiseInPercentage.setPaintTicks(true);
		noiseInPercentage.setMajorTickSpacing(10);
		noiseInPercentage.setMinorTickSpacing(1);
		noiseInPercentage.setPaintLabels(true);

		
		maxExecutionTimeInSeconds = new JSlider(0, 40, 8);
		maxExecutionTimeInSeconds.setPaintTicks(true);
		maxExecutionTimeInSeconds.setMajorTickSpacing(10);
		maxExecutionTimeInSeconds.setMinorTickSpacing(1);
		maxExecutionTimeInSeconds.setPaintLabels(true);

		
		maxNumberOfResults = new JSlider(2, 20, 10);
		maxNumberOfResults.setPaintTicks(true);
		maxNumberOfResults.setMajorTickSpacing(2);
		maxNumberOfResults.setMinorTickSpacing(1);
		maxNumberOfResults.setPaintLabels(true);
		
		owlRadioButton = new JRadioButton("<html>OWL 2</html>", false);
		elProfileButton = new JRadioButton("<html>EL Profile</html>", false);
		defaultProfileButton = new JRadioButton("<html>Default</html>", true);
		owlRadioButton.setEnabled(true);
		owlRadioButton.addActionListener(optionHandler);
		elProfileButton.addActionListener(optionHandler);
		defaultProfileButton.addActionListener(optionHandler);
		
		
		allBox = new JCheckBox("<html>all</html>", true);
		//allBox.addItemListener(optionHandler);
		someBox = new JCheckBox("<html>some</html>", true);
		//someBox.addItemListener(optionHandler);
		notBox = new JCheckBox("<html>not</html>", false);
		//notBox.addItemListener(optionHandler);
		valueBox = new JCheckBox("<html>value</html>", false);
		//valueBox.addItemListener(optionHandler);
		cardinalityBox = new JCheckBox("<html> &#8249;=x, &#8250;=x with max.:</html>", true);
		cardinalityBox.setActionCommand("Cardinality");
		cardinalityBox.addActionListener(optionHandler);
		//moreBox.addItemListener(optionHandler);
		
		cardinalityLimitBox = new JComboBox();
		for(int i = 1; i <= 10; i++){
			cardinalityLimitBox.addItem(i);
		}
		cardinalityLimitBox.setSelectedItem(5);
		cardinalityLimitBox.setEditable(false);
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		checkBoxPanel.add(allBox, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 1;
		checkBoxPanel.add(someBox, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.gridx = 4;
		c.gridy = 0;
		c.gridwidth = 1;
		checkBoxPanel.add(notBox, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.gridx = 6;
		c.gridy = 0;
		c.gridwidth = 1;
		checkBoxPanel.add(valueBox, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridx = 8;
		c.gridy = 0;
		c.gridwidth = 1;
		checkBoxPanel.add(cardinalityBox, c);
		
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridx = 9;
		c.gridy = 0;
		c.gridwidth = 1;
		checkBoxPanel.add(cardinalityLimitBox, c);
		
		
		radioBoxPanel.add(owlRadioButton);
		radioBoxPanel.add(elProfileButton);
		radioBoxPanel.add(defaultProfileButton);
		
		profilePanel.setBorder(BorderFactory.createTitledBorder("OWL Profile"));
		profilePanel.add(radioBoxPanel);
		profilePanel.add(checkBoxPanel);
		
		labelPanel.add(noiseInPercentageLabel);
		labelPanel.add(maxExecutionTimeLabel);
		labelPanel.add(nrOfConceptsLabel);
		
		sliderPanel.add(noiseInPercentage);
		sliderPanel.add(maxExecutionTimeInSeconds);
		sliderPanel.add(maxNumberOfResults);
		
		add(BorderLayout.SOUTH, profilePanel);
		add(BorderLayout.WEST, labelPanel);
		add(BorderLayout.CENTER, sliderPanel);
	}
	
	/**
	 * This method returns the min accuracy chosen in the slider.
	 * @return double minAccuracy
	 */
	public double getNoise() {
		double acc = noiseInPercentage.getValue();
		accuracy = (acc/100.0);
		return accuracy;
	}
	
	/**
	 * This method returns the max execution time chosen in the slider.
	 * @return int maxExecutionTime
	 */
	public int getMaxExecutionTimeInSeconds() {
		return maxExecutionTimeInSeconds.getValue();
	}
	
	/**
	 * This method returns the number of concepts chosen in the slider.
	 * @return int nrOfConcepts
	 */
	public int getMaxNumberOfResults() {
		return maxNumberOfResults.getValue();
	}

	/**
	 * This method returns the OWLRadioButton.
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
	
	public JRadioButton getDefaultProfileButton() {
		return defaultProfileButton;
	}

	/**
	 * This methode returns if the allquantor box is selected.
	 * @return boolean if allquantor box is selected
	 */
	public boolean isUseAllQuantor() {
		return allBox.isSelected();
	}

	/**
	 * This methode returns if the some box is selected.
	 * @return boolean if some box is selected
	 */
	public boolean isUseExistsQuantor() {
		return someBox.isSelected();
	}

	/**
	 * This methode returns if the not box is selected.
	 * @return boolean if not box is selected
	 */
	public boolean isUseNegation() {
		return notBox.isSelected();
	}

	/**
	 * This methode returns if the value box is selected.
	 * @return boolean if value box is selected
	 */
	public boolean isUseHasValue() {
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
	public int getCardinalityLimit() {
		return Integer.parseInt(cardinalityLimitBox.getSelectedItem().toString());
	}

	/**
	 * This methode returns if the cardinality restiction box is selected.
	 * @return boolean if cardinality restiction box is selected
	 */
	public boolean isUseCardinalityRestrictions() {
		return cardinalityBox.isSelected();
	}
	
	/**
	 * This methode sets the the checkboxes enable that are needed for
	 * the OWL 2 Profile.
	 */
	public void setToOWLProfile() {
		allBox.setSelected(true);
		owlRadioButton.setSelected(true);
		elProfileButton.setSelected(false);
		defaultProfileButton.setSelected(false);
		someBox.setSelected(true);
		notBox.setSelected(true);
		valueBox.setSelected(true);
		cardinalityBox.setSelected(true);
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
		cardinalityBox.setSelected(false);
		owlRadioButton.setSelected(false);
		elProfileButton.setSelected(true);
		defaultProfileButton.setSelected(false);
		this.setCountMoreBoxEnabled(false);
	}
	
	public void setToDefaultProfile() {
		allBox.setSelected(true);
		someBox.setSelected(true);
		notBox.setSelected(false);
		valueBox.setSelected(false);
		cardinalityBox.setSelected(true);
		owlRadioButton.setSelected(false);
		elProfileButton.setSelected(false);
		defaultProfileButton.setSelected(true);
		this.setCountMoreBoxEnabled(true);
	}
	
	/**
	 * This methode sets the combo box for the cardinality restriction
	 * enabled/disabled. 
	 * @param isEnabled
	 */
	public void setCountMoreBoxEnabled(boolean isEnabled) {
		cardinalityLimitBox.setEnabled(isEnabled);
	}
	
}
