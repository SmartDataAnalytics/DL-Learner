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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.MatteBorder;
import javax.swing.event.ListSelectionListener;

import org.dllearner.tools.ore.ui.IndividualsTable;
import org.dllearner.tools.ore.ui.MarkableClassExpressionsTable;
import org.jdesktop.swingx.JXTitledPanel;

/**
 * JPanel for repairing action.
 * @author Lorenz Buehmann
 *
 */
public class RepairPanel extends JPanel{

	private static final long serialVersionUID = -7411197973240429632L;
	
	private IndividualsTable posTable;
	private JButton posRepairButton;
	private JButton posDeleteButton;
	private JButton posRemoveButton;
	
	private IndividualsTable negTable;
	private JButton negRepairButton;
	private JButton negDeleteButton;
	private JButton negAddButton;
	
	private JButton nextButton;
	private JLabel classToDescribeLabel;
	
	
	private MarkableClassExpressionsTable descriptionsTable;
	
	public RepairPanel() {
		createUI();
	}
	
	private void createUI(){
		setLayout(new BorderLayout());
		JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		
		JPanel descriptionPanel = new JPanel(new BorderLayout());
		descriptionPanel.add(createDescriptionsPanel(), BorderLayout.CENTER);
		nextButton = new JButton("Next suggestion");
		nextButton.setActionCommand("next");
		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.add(nextButton, BorderLayout.NORTH);
		descriptionPanel.add(buttonPanel, BorderLayout.EAST);
		
		
		JSplitPane examplesSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		examplesSplitPane.setLeftComponent(createPosPanel());
		examplesSplitPane.setRightComponent(createNegPanel());
		examplesSplitPane.setDividerLocation(0.5);
		examplesSplitPane.setResizeWeight(0.5);
		examplesSplitPane.setOneTouchExpandable(true);
		
		mainSplitPane.setTopComponent(descriptionPanel);
		mainSplitPane.setBottomComponent(examplesSplitPane);
		mainSplitPane.setDividerLocation(0.3);
		mainSplitPane.setOneTouchExpandable(true);
		
		add(mainSplitPane);
	}
	
	
	private JComponent createDescriptionsPanel(){
		JPanel panel = new JPanel(new BorderLayout());
		JPanel classToDescribePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		classToDescribePanel.add(new JLabel("Class to describe:"));
		classToDescribeLabel = new JLabel();
		classToDescribePanel.add(classToDescribeLabel);
		panel.add(classToDescribePanel, BorderLayout.NORTH);
		
		descriptionsTable = new MarkableClassExpressionsTable();
		JScrollPane scroll = new JScrollPane(descriptionsTable);
		scroll.setBorder(new MatteBorder(null));
		panel.add(scroll);
		return panel;
	}
	
	private JComponent createPosPanel(){
		JXTitledPanel posPanel = new JXTitledPanel("Positive examples");
		posPanel.getContentContainer().setLayout(new BorderLayout());
		posPanel.setName("positive");
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setName("positive");
		buttonPanel.setLayout(new GridLayout(0, 1));
		posRemoveButton = new JButton("Remove");
		posRemoveButton.setActionCommand("posRemove");
		buttonPanel.add(posRemoveButton);
		posDeleteButton = new JButton("Delete");
		posDeleteButton.setActionCommand("posDelete");
		buttonPanel.add(posDeleteButton);
		posRepairButton = new JButton("Repair");
		posRepairButton.setActionCommand("posRepair");
		buttonPanel.add(posRepairButton);
		JPanel buttonPanelHolder = new JPanel(new BorderLayout());
		buttonPanelHolder.add(buttonPanel, BorderLayout.NORTH);
		posPanel.getContentContainer().add(buttonPanelHolder, BorderLayout.EAST);
		
		posTable = new IndividualsTable();
		posPanel.getContentContainer().add(new JScrollPane(posTable), BorderLayout.CENTER);
		
		return posPanel;
	}
	
	private JComponent createNegPanel(){
		JXTitledPanel negPanel = new JXTitledPanel("Negative examples");
		negPanel.getContentContainer().setLayout(new BorderLayout());
		negPanel.setName("negative");
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setName("negative");
		buttonPanel.setLayout(new GridLayout(0, 1));
		negAddButton = new JButton("Add");
		negAddButton.setActionCommand("negAdd");
		buttonPanel.add(negAddButton);
		negDeleteButton = new JButton("Delete");
		negDeleteButton.setActionCommand("negDelete");
		buttonPanel.add(negDeleteButton);
		negRepairButton = new JButton("Repair");
		negRepairButton.setActionCommand("negRepair");
		buttonPanel.add(negRepairButton);
		JPanel buttonPanelHolder = new JPanel(new BorderLayout());
		buttonPanelHolder.add(buttonPanel, BorderLayout.NORTH);
		negPanel.getContentContainer().add(buttonPanelHolder, BorderLayout.EAST);
		
		negTable = new IndividualsTable();
		negPanel.getContentContainer().add(new JScrollPane(negTable), BorderLayout.CENTER);
		
		return negPanel;
	}
	
	/**
	 * Returns the list for positive examples.
	 * @return positive list
	 */
	public IndividualsTable getPosFailureTable() {
		return posTable;
	}
	
	/**
	 * Returns the list for negative examples.
	 * @return negative list
	 */
	public IndividualsTable getNegFailureTable() {
		return negTable;
	}
	
	public void setNextButtonEnabled(boolean enabled){
		nextButton.setEnabled(enabled);
	}
	
	public void setManualStyle(boolean value){
		if(value){
			nextButton.setVisible(false);
		} else {
			nextButton.setVisible(true);
		}
		repaint();
	}
	
	/**
	 * adds the list selection listener for lists.
	 * @param l list selection listener
	 */
	public void addSelectionListeners(ListSelectionListener l){
		posTable.getSelectionModel().addListSelectionListener(l);
		negTable.getSelectionModel().addListSelectionListener(l);
	}
	
	/**
	 * Adds the action listener to buttons.	
	 * @param aL action listener
	 */
	public void addActionListeners(ActionListener aL){
		posRemoveButton.addActionListener(aL);
		posDeleteButton.addActionListener(aL);
		posRepairButton.addActionListener(aL);
		
		negAddButton.addActionListener(aL);
		negDeleteButton.addActionListener(aL);
		negRepairButton.addActionListener(aL);
		
		nextButton.addActionListener(aL);
	}
	
	/**
	 * Adds mouse listener to lists.
	 * @param mL mouse listener
	 */
	public void addMouseListeners(MouseListener mL){
		posTable.addMouseListener(mL);
		negTable.addMouseListener(mL);
	}
	
	public void setClassToDescribe(String classToDescribeString){
		classToDescribeLabel.setText(classToDescribeString);
	}
	
	public static void main(String[] args){
		
		JFrame frame = new JFrame();
		JPanel panel = new RepairPanel();
		frame.add(panel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	
}  
    
 


	

