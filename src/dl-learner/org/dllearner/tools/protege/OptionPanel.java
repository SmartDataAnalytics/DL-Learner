package org.dllearner.tools.protege;

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
	/**
	 * Construktor for the Option Panel. 
	 */
	public OptionPanel() {


		optionPanel = new JPanel(new GridLayout(0,2));
		minAccuracyLabel = new JLabel("minimum accuracy");
		maxExecutionTimeLabel = new JLabel("maximum execution time");
		nrOfConceptsLabel = new JLabel("maximum number of results");
		
		minAccuracy = new JSlider(50, 100, 80);
		minAccuracy.setPaintTicks(true);
		minAccuracy.setMajorTickSpacing(10);
		minAccuracy.setMinorTickSpacing(1);
		minAccuracy.setPaintLabels(true);

		
		maxExecutionTime = new JSlider(5, 20, 10);
		maxExecutionTime.setPaintTicks(true);
		maxExecutionTime.setMajorTickSpacing(5);
		maxExecutionTime.setMinorTickSpacing(1);
		maxExecutionTime.setPaintLabels(true);

		
		nrOfConcepts = new JSlider(2, 20, 5);
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
		double accuracy = acc/100;
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
