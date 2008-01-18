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
    private JButton initButton;
    private Config config;
    
	ReasonerPanel(final Config config) {
		super(new BorderLayout());
		
		this.config = config;
		
		initButton = new JButton("Use DIG by default");
		initButton.addActionListener(this);
		
		digPanel.add(initButton);
		add(digPanel, BorderLayout.PAGE_START);

	}
  
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == initButton) {
			if (config.getStatus(2)) { // no check if button init was used
				// set reasoner
				config.setReasoner(config.getComponentManager().reasoner(DIGReasoner.class, config.getKnowledgeSource()));
				config.getReasoner().init();
			
				// set ReasoningService
				config.setReasoningService(config.getComponentManager().reasoningService(config.getReasoner()));
			}
			if (config.getStatus(3)) {  // Reasoner is set
				System.out.println("Reasoner: " + config.getReasoner());
			}
			if (config.getStatus(4)) {  // ReasoningServic is set
				System.out.println("ReasoningService: " + config.getReasoningService());
			}
		}
	}
}
