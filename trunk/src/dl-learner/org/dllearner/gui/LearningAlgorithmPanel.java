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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import org.dllearner.core.LearningAlgorithm;



/**
 * LearningAlgorithmPanel
 * 
 * @author Tilo Hielscher
 * 
 */

public class LearningAlgorithmPanel extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 8721490771860452959L;
    private Config config;
    private List<Class<? extends LearningAlgorithm>> learners;
	private JPanel choosePanel = new JPanel();
	private OptionPanel optionPanel;
	private JPanel initPanel = new JPanel();
    private JButton initButton;
    private String[] cbItems = {};
	private JComboBox cb = new JComboBox(cbItems);
	private int choosenClassIndex;
	
	
	
	LearningAlgorithmPanel(Config config) {
		super(new BorderLayout());

		this.config = config;
		
		initButton = new JButton("Init LearingAlgorithm");
		initButton.addActionListener(this);
				
		initPanel.add(initButton);

		choosePanel.add(cb);
		
		// add into comboBox
		learners = config.getComponentManager().getLearningAlgorithms();
		for (int i=0; i<learners.size(); i++) {
			//cb.addItem(learners.get(i).getSimpleName());
			//System.out.println(learners.get(i).getSimpleName());
			cb.addItem(config.getComponentManager().getComponentName(learners.get(i)));
		}
		
		cb.addActionListener(this);
	
		optionPanel =  new OptionPanel(config, config.getLearningAlgorithm(), learners.get(choosenClassIndex));
		updateOptionPanel();

		
		add(choosePanel, BorderLayout.PAGE_START);
		add(optionPanel, BorderLayout.CENTER);	
		add(initPanel, BorderLayout.PAGE_END);
	}
	

	public void actionPerformed(ActionEvent e) {
		// read selected Class
        choosenClassIndex = cb.getSelectedIndex();

		updateOptionPanel();
		
		// init
		if (e.getSource() == initButton) {
			if (config.getStatus(6)) {
				config.setLearningAlgorithm(config.getComponentManager().learningAlgorithm(learners.get(choosenClassIndex), config.getLearningProblem(), config.getReasoningService()));
				updateOptionPanel();
				config.getLearningAlgorithm().init();
			}
			if (config.getStatus(5)) {  // examples are set
				System.out.println("LearningAlgorithm: " + config.getLearningAlgorithm() + "\n");
			}
		}
	}

	public void updateOptionPanel() {
        // update OptionPanel
        optionPanel.setComponent(config.getLearningAlgorithm());
		optionPanel.setComponentOption(learners.get(choosenClassIndex));
	}
}
