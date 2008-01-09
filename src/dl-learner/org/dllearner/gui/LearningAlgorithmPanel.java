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

import javax.swing.*;

import org.dllearner.algorithms.refinement.ROLearner;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



/**
 * LearningAlgorithmPanel
 * 
 * @author Tilo Hielscher
 * 
 */

public class LearningAlgorithmPanel extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 8721490771860452959L;
    
	private JPanel laPanel = new JPanel();
    private JButton laButton;
    
	LearningAlgorithmPanel() {
		super(new BorderLayout());

		laButton = new JButton("Use ROLearner");
		laButton.addActionListener(this);
		
		laPanel.add(laButton);
		add(laPanel, BorderLayout.PAGE_START);	
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == laButton) {
			if (StartGUI.myconfig.getStatus(6)) {
				StartGUI.myconfig.setLearningAlgorithm(StartGUI.myconfig.getComponentManager().learningAlgorithm(ROLearner.class, StartGUI.myconfig.getLearningProblem(), StartGUI.myconfig.getReasoningService()));
				StartGUI.myconfig.getLearningAlgorithm().init();
			}
		}
	}
}
