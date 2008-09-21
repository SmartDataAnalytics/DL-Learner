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

package org.dllearner.tools.ore;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionListener;

/**
 * JPanel for repairing action.
 * @author Lorenz Buehmann
 *
 */
public class RepairPanel extends JPanel{

	private static final long serialVersionUID = -7411197973240429632L;

	private JPanel contentPanel;
	
	private DefaultListModel posFailureModel;
	private DefaultListModel negFailureModel;
	
	private JPanel posPanel;
	private JScrollPane posScrollPane;
	private JList posList;
	private JPanel posButtonPanel;
	private JButton pos_repairButton;
	private JButton pos_deleteButton;
	private JButton pos_removeButton;
	
	private JPanel negPanel;
	private JScrollPane negScrollPane;
	private JList negList;
	private JPanel negButtonPanel;
	private JButton neg_repairButton;
	private JButton neg_deleteButton;
	private JButton neg_addButton;
	
	
	public RepairPanel() {
		
		super();
		posFailureModel = new DefaultListModel();
		negFailureModel = new DefaultListModel();
		
		this.setLayout(new java.awt.BorderLayout());
		
		JPanel labelPanel = new JPanel();
				
		contentPanel = getContentPanel();
		
		add(contentPanel,BorderLayout.CENTER);
		add(labelPanel, BorderLayout.SOUTH);
	}

	private JPanel getContentPanel() {
		JPanel contentPanel = new JPanel();
		GridBagLayout thisLayout = new GridBagLayout();
		thisLayout.rowWeights = new double[] {0.1};
		thisLayout.rowHeights = new int[] {7};
		thisLayout.columnWeights = new double[] {0.5, 0.5};
		thisLayout.columnWidths = new int[] {100, 100};
		contentPanel.setLayout(thisLayout);
		setPreferredSize(new Dimension(400, 300));
		{
			posPanel = new JPanel();
			GridBagLayout posPanelLayout = new GridBagLayout();
			contentPanel.add(posPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			posPanelLayout.rowWeights = new double[] {0.1};
			posPanelLayout.rowHeights = new int[] {7};
			posPanelLayout.columnWeights = new double[] {0.0, 0.5};
			posPanelLayout.columnWidths = new int[] {80, 110};
			posPanel.setLayout(posPanelLayout);
			posPanel.setPreferredSize(new java.awt.Dimension(182, 275));
			posPanel.setBorder(BorderFactory.createTitledBorder("positive examples"));
			{
				posScrollPane = new JScrollPane();
				posPanel.add(posScrollPane, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				posScrollPane.setSize(126, 276);
				{
					
					posList = new JList(posFailureModel);
					posScrollPane.setViewportView(posList);
					posList.setPreferredSize(new java.awt.Dimension(85, 93));
					posList.setSize(127, 273);
				}
			}
			{
				posButtonPanel = new JPanel();
				posButtonPanel.setName("positive");
				GroupLayout posButtonPanelLayout = new GroupLayout((JComponent)posButtonPanel);
				posPanel.add(posButtonPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				posButtonPanel.setLayout(posButtonPanelLayout);
				{
					pos_removeButton = new JButton();
					pos_removeButton.setName("posRemove");
					pos_removeButton.setText("remove");
				}
				{
					pos_deleteButton = new JButton();
					pos_deleteButton.setName("posDelete");
					pos_deleteButton.setText("delete");
				}
				{
					pos_repairButton = new JButton();
					pos_repairButton.setName("posRepair");
					pos_repairButton.setText("repair");
				}
					posButtonPanelLayout.setHorizontalGroup(posButtonPanelLayout.createSequentialGroup()
					.addGroup(posButtonPanelLayout.createParallelGroup()
					    .addGroup(GroupLayout.Alignment.LEADING, posButtonPanelLayout.createSequentialGroup()
					        .addComponent(pos_removeButton, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE)
					        .addGap(10))
					    .addComponent(pos_deleteButton, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
					    .addGroup(GroupLayout.Alignment.LEADING, posButtonPanelLayout.createSequentialGroup()
					        .addComponent(pos_repairButton, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE)
					        .addGap(10)))
					.addContainerGap(22, 22));
					posButtonPanelLayout.linkSize(SwingConstants.HORIZONTAL, new Component[] {pos_repairButton, pos_deleteButton, pos_removeButton});
					posButtonPanelLayout.setVerticalGroup(posButtonPanelLayout.createSequentialGroup()
						.addComponent(pos_removeButton, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(pos_deleteButton, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addComponent(pos_repairButton, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(213, 213));
					posButtonPanelLayout.linkSize(SwingConstants.VERTICAL, new Component[] {pos_repairButton, pos_deleteButton, pos_removeButton});
			}
		}
		{
			negPanel = new JPanel();
			GridBagLayout negPanelLayout = new GridBagLayout();
			contentPanel.add(negPanel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			negPanelLayout.rowWeights = new double[] {0.1};
			negPanelLayout.rowHeights = new int[] {7};
			negPanelLayout.columnWeights = new double[] {0.5, 0.0};
			negPanelLayout.columnWidths = new int[] {110, 80};
			negPanel.setLayout(negPanelLayout);
			posPanel.setPreferredSize(new java.awt.Dimension(182, 275));
			negPanel.setBorder(BorderFactory.createTitledBorder(null, "negative examples", TitledBorder.LEADING, TitledBorder.TOP));
			{
				negScrollPane = new JScrollPane();
				negPanel.add(negScrollPane, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				{
					negList = new JList(negFailureModel);
					negScrollPane.setViewportView(negList);
					negList.setPreferredSize(new java.awt.Dimension(85, 93));
					negList.setSize(127, 273);
				}
			}
			{
				negButtonPanel = new JPanel();
				negButtonPanel.setName("negative");
				GroupLayout negButtonPanelLayout = new GroupLayout((JComponent)negButtonPanel);
				negButtonPanel.setLayout(negButtonPanelLayout);
				negPanel.add(negButtonPanel, new GridBagConstraints(1, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				{
					neg_addButton = new JButton();
					neg_addButton.setName("negAdd");
					neg_addButton.setText("add");
				}
				{
					neg_deleteButton = new JButton();
					neg_deleteButton.setName("negDelete");
					neg_deleteButton.setText("delete");
				}
				{
					neg_repairButton = new JButton();
					neg_repairButton.setName("negRepair");
					neg_repairButton.setText("repair");
				}
					negButtonPanelLayout.setHorizontalGroup(negButtonPanelLayout.createSequentialGroup()
					.addGroup(negButtonPanelLayout.createParallelGroup()
					    .addComponent(neg_addButton, GroupLayout.Alignment.LEADING, 0, 79, Short.MAX_VALUE)
					    .addComponent(neg_deleteButton, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE)
					    .addGroup(GroupLayout.Alignment.LEADING, negButtonPanelLayout.createSequentialGroup()
					        .addComponent(neg_repairButton, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
					        .addGap(7)))
					.addContainerGap());
					negButtonPanelLayout.linkSize(SwingConstants.HORIZONTAL, new Component[] {neg_repairButton, neg_deleteButton, neg_addButton});
					negButtonPanelLayout.setVerticalGroup(negButtonPanelLayout.createSequentialGroup()
						.addComponent(neg_addButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(neg_deleteButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(neg_repairButton, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(213, 213));
					negButtonPanelLayout.linkSize(SwingConstants.VERTICAL, new Component[] {neg_repairButton, neg_deleteButton, neg_addButton});
			}
		}
		

		return contentPanel;
	}
	
	/**
	 * Returns the list model for positive examples.
	 * @return list model
	 */
	public DefaultListModel getPosFailureModel() {
		return posFailureModel;
	}
	
	/**
	 * Returns the list model for negative examples.
	 * @return list model
	 */
	public DefaultListModel getNegFailureModel() {
		return negFailureModel;
	}
	
	/**
	 * Returns the list for positive examples.
	 * @return positive list
	 */
	public javax.swing.JList getPosFailureList() {
		return posList;
	}
	
	/**
	 * Returns the list for negative examples.
	 * @return negative list
	 */
	public javax.swing.JList getNegFailureList() {
		return negList;
	}
	
	/**
	 * adds the list selection listener for lists.
	 * @param l list selection listener
	 */
	public void addSelectionListeners(ListSelectionListener l){
		posList.addListSelectionListener(l);
		negList.addListSelectionListener(l);
	}
	
	/**
	 * Adds the action listener to buttons.	
	 * @param aL action listener
	 */
	public void addActionListeners(ActionListener aL){
		pos_removeButton.addActionListener(aL);
		pos_deleteButton.addActionListener(aL);
		pos_repairButton.addActionListener(aL);
		neg_addButton.addActionListener(aL);
		neg_deleteButton.addActionListener(aL);
		neg_repairButton.addActionListener(aL);
	}
	
	/**
	 * Adds mouse listener to lists.
	 * @param mL mouse listener
	 */
	public void addMouseListeners(MouseListener mL){
		posList.addMouseListener(mL);
		negList.addMouseListener(mL);
	}
	
	/**
	 * Sets custom cell renderer to the lists.
	 * @param ore
	 */
	public void setCellRenderers(ORE ore){
		ColorListCellRenderer cell = new ColorListCellRenderer(ore);
		posList.setCellRenderer(cell);
		negList.setCellRenderer(cell);
	}
	
	
	
}  
    
 


	

