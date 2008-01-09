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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.*;

import org.dllearner.core.dl.Individual;
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
    private List<Individual> individuals;
    
	ReasonerPanel() {
		super(new BorderLayout());
		
		digButton = new JButton("Use DIG by default");
		digButton.addActionListener(this);
		
		// create a scrollable list of examples
		digList = new JList();
		digList.setLayoutOrientation(JList.VERTICAL);
		digList.setVisibleRowCount(-1);
		JScrollPane listScroller = new JScrollPane(digList);
		listScroller.setPreferredSize(new Dimension(550, 350));
		
		digPanel.add(digButton);
		add(digPanel, BorderLayout.PAGE_START);
	
		centerPanel.add(listScroller);
		add(centerPanel, BorderLayout.CENTER);
	
		digList.addListSelectionListener(new ListSelectionListener() {
		      public void valueChanged(ListSelectionEvent evt) {
		    	  if (evt.getValueIsAdjusting())
		    		  return;
		    	  //System.out.println("Selected from " + evt.getFirstIndex() + " to " + evt.getLastIndex());
		    	  // detect which examples have been selected			
		    	  Set<String> exampleSet = new HashSet<String>();
		    	  int[] selectedIndices = digList.getSelectedIndices();
		    	  for(int i : selectedIndices)
		    		  exampleSet.add(individuals.get(i).toString());
		    	  StartGUI.myconfig.setExampleSet(exampleSet);
		    	  System.out.println("digList: " + StartGUI.myconfig.getExampleSet() );
		      }
		});
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == digButton) {
			// set reasoner
			StartGUI.myconfig.setReasoner(StartGUI.myconfig.getComponentManager().reasoner(DIGReasoner.class, StartGUI.myconfig.getKnowledgeSource()));
			//System.out.println(StartGUI.myconfig.getKnowledgeSource());
			StartGUI.myconfig.getReasoner().init();
			//System.out.println(StartGUI.myconfig.getReasoner());
			
			// set ReasoningService
			StartGUI.myconfig.setReasoningService(StartGUI.myconfig.getComponentManager().reasoningService(StartGUI.myconfig.getReasoner()));

			// get list from ReasoningService
			Set<Individual> individualsSet = StartGUI.myconfig.getReasoningService().getIndividuals();
			//System.out.println("IndividualsSet: " + individualsSet);
			individuals = new LinkedList<Individual>(individualsSet);
			//System.out.println("individuals: " + individuals);
			
			// make list
			DefaultListModel listModel = new DefaultListModel();
			for(Individual ind : individuals)
				listModel.addElement(ind);
			
			digList.setModel(listModel);
			
			// graphic
			digList.setModel(listModel);
			StartGUI.myrun.renew();
			
			//return;
		}
	}
}
