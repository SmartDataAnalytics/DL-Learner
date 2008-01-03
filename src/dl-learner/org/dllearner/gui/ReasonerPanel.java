package org.dllearner.gui;

/**
 * Copyright (C) 2007, Jens Lehmann
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import org.dllearner.reasoning.DIGReasoner;

//import org.dllearner.core.ReasonerComponent;
//import org.dllearner.reasoning.DIGReasoner;

//import org.dllearner.core.ComponentManager;
//import org.dllearner.core.ReasoningService;

/**
 * ReasonerPanel
 * 
 * @author Tilo Hielscher
 * 
 */

public class ReasonerPanel extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = -7678275020058043937L;

	private JPanel digPanel = new JPanel();
	private JPanel centerPanel = new JPanel();
    private JButton digButton;
    private JList digList = new JList();
    
	ReasonerPanel() {
		super(new BorderLayout());
		
		digButton = new JButton("Use DIG by default");
		digButton.addActionListener(this);
		
		// create a scrollable list of examples
		digList.setLayoutOrientation(JList.VERTICAL);
		digList.setVisibleRowCount(-1);
		JScrollPane listScroller = new JScrollPane(digList);
		listScroller.setPreferredSize(new Dimension(250, 80));
		centerPanel.add(listScroller);

		digPanel.add(digButton);
		add(digPanel, BorderLayout.PAGE_START);
	
		centerPanel.add(digList);
		add(centerPanel, BorderLayout.CENTER);
	
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == digButton) { // DIG
			StartGUI.config.setReasoner(StartGUI.config.getComponentManager().reasoner(DIGReasoner.class, StartGUI.config.getKnowledgeSource()));
			//StartGUI.config.getReasoner().init(); //error
			System.out.println("test");
			
			//config.setReasoningService(config.getComponentManager().reasoningService(config.getReasoner()));

			// set list
			//Set<Individual> individualsSet = config.getReasoningService().getIndividuals();
			//config.setListIndividuals(new LinkedList<Individual>(individualsSet));
			
			// graphic
			//DefaultListModel listModel = new DefaultListModel();
			//for(Individual ind : config.getListIndividuals())
				//listModel.addElement(ind);
			
			// graphic
			//Set<String> exampleSet = new HashSet<String>();
			//int[] selectedIndices = digList.getSelectedIndices();
			//for(int i : selectedIndices)
				//exampleSet.add(config.getListIndividuals().get(i).toString());
		}
	}
}
