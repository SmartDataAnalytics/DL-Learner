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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionListener;

import org.dllearner.tools.ore.ui.IndividualsTable;

/**
 * JPanel for repairing action.
 * @author Lorenz Buehmann
 *
 */
public class RepairPanel extends JPanel{

	private static final long serialVersionUID = -7411197973240429632L;
	
	private JPanel posPanel;
	private JScrollPane posScrollPane;
	private IndividualsTable posTable;
	private JButton posRepairButton;
	private JButton posDeleteButton;
	private JButton posRemoveButton;
	
	private JPanel negPanel;
	private JScrollPane negScrollPane;
	private IndividualsTable negTable;
	private JButton negRepairButton;
	private JButton negDeleteButton;
	private JButton negAddButton;
	
	
	public RepairPanel() {
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.5;
		c.weighty = 0.5;
		add(getPosPanel(), c);
		c.gridx = 1;
		add(getNegPanel(), c);
		
	}
	
	private JPanel getPosPanel(){
		posPanel = new JPanel();
		posPanel.setName("positive");
		posPanel.setLayout(new GridBagLayout());
		posPanel.setBorder(new TitledBorder("Positive examples"));
		
		GridBagConstraints c = new GridBagConstraints();
		
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
		c.anchor = GridBagConstraints.NORTH;
		posPanel.add(buttonPanel, c);
		
		posTable = new IndividualsTable();
		posScrollPane = new JScrollPane(posTable);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		posPanel.add(posScrollPane, c);
		
		return posPanel;
	}
	
	private JPanel getNegPanel(){
		negPanel = new JPanel();
		negPanel.setName("negative");
		negPanel.setLayout(new GridBagLayout());
		negPanel.setBorder(new TitledBorder("Negative examples"));
		
		GridBagConstraints c = new GridBagConstraints();
			
		negTable = new IndividualsTable();
		negScrollPane = new JScrollPane(negTable);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		negPanel.add(negScrollPane, c);
		
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
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		c.weighty = 0;
		c.anchor = GridBagConstraints.NORTH;
		negPanel.add(buttonPanel, c);
		
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
	}
	
	/**
	 * Adds mouse listener to lists.
	 * @param mL mouse listener
	 */
	public void addMouseListeners(MouseListener mL){
		posTable.addMouseListener(mL);
		negTable.addMouseListener(mL);
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
    
 


	

