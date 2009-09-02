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
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionListener;

import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.ui.GraphicalCoveragePanel;
import org.dllearner.tools.ore.ui.ResultTable;
import org.dllearner.tools.protege.OptionPanel;


/**
 * The wizard panel where result table and buttons for learning step are shown.
 * @author Lorenz Buehmann
 *
 */
public class LearningPanel extends JPanel{

	

	private static final long serialVersionUID = -7411197973240429632L;

	private JPanel contentPanel;

	private ResultTable resultTable;
	private JScrollPane tableScrollPane;
	private JPanel resultPanel;

	private JButton stopButton;
	private JButton startButton;
	private JPanel buttonPanel;
	private JPanel buttonSliderPanel;
	
	private GraphicalCoveragePanel graphicPanel;
	private OptionPanel optionsPanel;


	public LearningPanel() {

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.BOTH;
		contentPanel = getResultPanel();
		add(contentPanel, c);


		c.gridx = GridBagConstraints.RELATIVE;
		c.gridy = 0;
		c.anchor = GridBagConstraints.NORTH;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		buttonSliderPanel = new JPanel();
		add(buttonSliderPanel, c);
		GridBagLayout buttonSliderPanelLayout = new GridBagLayout();
		buttonSliderPanelLayout.rowWeights = new double[] { 0.0, 0.0 };
		buttonSliderPanelLayout.rowHeights = new int[] { 126, 7 };
		buttonSliderPanelLayout.columnWeights = new double[] { 0.1 };
		buttonSliderPanelLayout.columnWidths = new int[] { 7 };
		buttonSliderPanel.setLayout(buttonSliderPanelLayout);

		buttonPanel = new JPanel();
		BoxLayout buttonPanelLayout = new BoxLayout(buttonPanel,
				javax.swing.BoxLayout.X_AXIS);
//		buttonPanel.setLayout(buttonPanelLayout);
		buttonSliderPanel.add(buttonPanel, new GridBagConstraints(0, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		startButton = new JButton();
		buttonPanel.add(startButton);
		startButton.setText("Start");

		stopButton = new JButton();
		buttonPanel.add(stopButton);
		stopButton.setText("Stop");

//		add(buttonPanel, c);
		
		optionsPanel = new OptionPanel();
		optionsPanel.setBorder(new TitledBorder("Options"));
		buttonSliderPanel.add(optionsPanel, new GridBagConstraints(0, 1,
				1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		
		
		graphicPanel = new GraphicalCoveragePanel("test");
		JPanel graphicHolderPanel = new JPanel(new BorderLayout());
		graphicHolderPanel.add(graphicPanel);
		graphicHolderPanel.setBorder(new TitledBorder("Graphical coverage"));
//		graphicHolderPanel.setPreferredSize(new Dimension(300,300));
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.fill = GridBagConstraints.HORIZONTAL;
//		c.gridwidth = GridBagConstraints.REMAINDER;
		
		add(graphicHolderPanel, c);

	}

	private JPanel getResultPanel() {

		resultPanel = new JPanel();
		resultPanel.setLayout(new BorderLayout());
		resultTable = new ResultTable();
		
		
		tableScrollPane = new JScrollPane(resultTable);
//		int height = resultTable.getRowHeight() * 10 + resultTable.getTableHeader().getHeight();
//		tableScrollPane.getViewport().setPreferredSize(new Dimension(800, height));
		resultPanel.add(tableScrollPane);
		resultPanel.setBorder(new TitledBorder("Learned class expressions"));

		return resultPanel;
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
	
	public OptionPanel getOptionsPanel(){
		return optionsPanel;
	}
	
	public static void main(String[] args){
		OREManager.getInstance().setCurrentClass2Learn(new NamedClass("dummy"));
		JFrame frame = new JFrame();
		JPanel panel = new LearningPanel();
		frame.add(panel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
}  
    
 


	

