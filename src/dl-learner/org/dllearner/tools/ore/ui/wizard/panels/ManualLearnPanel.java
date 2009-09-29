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

package org.dllearner.tools.ore.ui.wizard.panels;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionListener;

import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.ui.GraphicalCoveragePanel;
import org.dllearner.tools.ore.ui.HelpablePanel;
import org.dllearner.tools.ore.ui.LearningOptionsPanel;
import org.dllearner.tools.ore.ui.ResultTable;


/**
 * The wizard panel where result table and buttons for learning step are shown.
 * @author Lorenz Buehmann
 *
 */
public class ManualLearnPanel extends JPanel{	

	private static final long serialVersionUID = -7411197973240429632L;

	private ResultTable resultTable;
	private JScrollPane tableScrollPane;
	private JPanel resultPanel;

	private JButton stopButton;
	private JButton startButton;
	private JPanel buttonPanel;
	private JPanel buttonSliderPanel;
	
	private GraphicalCoveragePanel graphicPanel;
	private LearningOptionsPanel optionsPanel;
	
	private JRadioButton equivalentClassButton;
	private JRadioButton superClassButton;
	
	private GridBagConstraints c;


	public ManualLearnPanel() {
		createUI();
	}
	
	private void createUI(){
		setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		createResultPanel();
		createControlPanel();
		createCoveragePanel();
	}
	
	private void createResultPanel(){
		c.gridx = 0;
		c.gridy = 0;		
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.BOTH;
		
		resultTable = new ResultTable();
		tableScrollPane = new JScrollPane(resultTable);
		resultPanel = new JPanel();
		resultPanel.setLayout(new BorderLayout());
		resultPanel.add(tableScrollPane);
		resultPanel.setBorder(new TitledBorder("Learned class expressions"));

		add(resultPanel, c);
	}
	
	private void createControlPanel(){
		c.gridx = GridBagConstraints.RELATIVE;
		c.gridy = 0;
		c.anchor = GridBagConstraints.NORTH;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		
		buttonSliderPanel = new JPanel();
	
		GridBagLayout buttonSliderPanelLayout = new GridBagLayout();
		buttonSliderPanelLayout.rowWeights = new double[] { 0.0, 0.0 };
		buttonSliderPanelLayout.rowHeights = new int[] { 126, 7 };
		buttonSliderPanelLayout.columnWeights = new double[] { 0.1 };
		buttonSliderPanelLayout.columnWidths = new int[] { 7 };
		buttonSliderPanel.setLayout(buttonSliderPanelLayout);

		buttonPanel = new JPanel();
		buttonSliderPanel.add(buttonPanel, new GridBagConstraints(0, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		startButton = new JButton();
		buttonPanel.add(startButton);
		startButton.setText("Start");

		stopButton = new JButton();
		buttonPanel.add(stopButton);
		stopButton.setText("Stop");
		stopButton.setEnabled(false);

		JPanel learnTypePanel = new JPanel();
		learnTypePanel.setLayout(new GridLayout(0, 1));
		equivalentClassButton = new JRadioButton("Learn equivalent class expressions", true);
		equivalentClassButton.setActionCommand("equivalent");
		equivalentClassButton.setSelected(true);
		superClassButton = new JRadioButton("Learn super class expressions");
		superClassButton.setActionCommand("super");
			
		ButtonGroup learningType = new ButtonGroup();
		learningType.add(equivalentClassButton);
		learningType.add(superClassButton);
		
		learnTypePanel.add(equivalentClassButton);
		learnTypePanel.add(superClassButton);
		HelpablePanel learnTypeHelpPanel = new HelpablePanel(learnTypePanel);
		learnTypeHelpPanel.setBorder(new TitledBorder("Learning type"));
		buttonSliderPanel.add(learnTypeHelpPanel, new GridBagConstraints(0, 1,
				1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		
		optionsPanel = new LearningOptionsPanel();
		HelpablePanel optionsHelpPanel = new HelpablePanel(optionsPanel);
		optionsHelpPanel.setBorder(new TitledBorder("Options"));
		buttonSliderPanel.add(optionsHelpPanel, new GridBagConstraints(0, 2,
				1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		
		add(buttonSliderPanel, c);
	}
	
	
	
	private void createCoveragePanel(){
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		graphicPanel = new GraphicalCoveragePanel("");
		HelpablePanel coverageHelpPanel = new HelpablePanel(graphicPanel);
		
		JPanel graphicHolderPanel = new JPanel(new BorderLayout());
		graphicHolderPanel.add(coverageHelpPanel);
		graphicHolderPanel.setBorder(new TitledBorder("Coverage"));

		add(graphicHolderPanel, c);
	}
	
	public void addStartButtonListener(ActionListener a){
		startButton.addActionListener(a);
	}
	
	public void addStopButtonListener(ActionListener a){
		stopButton.addActionListener(a);
	}

	public JButton getStartButton() {
		return startButton;
	}

	public JButton getStopButton() {
		return stopButton;
	}
	
	public ResultTable getResultTable(){
		return resultTable;
	}
	
	public void addSelectionListener(ListSelectionListener l){
		resultTable.getSelectionModel().addListSelectionListener(l);
	}
	
	public void updateCurrentGraphicalCoveragePanel(EvaluatedDescription desc){
		this.graphicPanel.setNewClassDescription(desc);	
	}
	
	public LearningOptionsPanel getOptionsPanel(){
		return optionsPanel;
	}
	
	public void reset(){
		graphicPanel.clear();
		resultTable.clear();
	}
	
	public static void main(String[] args){
		OREManager.getInstance().setCurrentClass2Learn(new NamedClass("dummy"));
		JFrame frame = new JFrame();
		JPanel panel = new ManualLearnPanel();
		frame.add(panel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}	
}  
 
