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

import org.dllearner.learningproblems.PosOnlyDefinitionLP;

/**
 * LearningProblemPanel
 * 
 * @author Tilo Hielscher
 * 
 */

public class LearningProblemPanel extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = -3819627680918930203L;

	private JPanel lpPanel = new JPanel();
    private JButton lpButton;
    
	LearningProblemPanel() {
		super(new BorderLayout());

		lpButton = new JButton("Use PosOnlyDefinitionLP");
		lpButton.addActionListener(this);
		
		lpPanel.add(lpButton);
		add(lpPanel, BorderLayout.PAGE_START);	
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == lpButton) {
			if (StartGUI.myconfig.getStatus(5)) {
				System.out.println(StartGUI.myconfig.getExampleSet());
				StartGUI.myconfig.setLearningProblem(StartGUI.myconfig.getComponentManager().learningProblem(PosOnlyDefinitionLP.class, StartGUI.myconfig.getReasoningService()));
				StartGUI.myconfig.getComponentManager().applyConfigEntry(StartGUI.myconfig.getLearningProblem(), "positiveExamples", StartGUI.myconfig.getExampleSet());
				StartGUI.myconfig.getLearningProblem().init();
			}
		}
	}
	
}
