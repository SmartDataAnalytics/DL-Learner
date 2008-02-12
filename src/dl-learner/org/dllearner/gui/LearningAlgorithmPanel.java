package org.dllearner.gui;

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
import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import org.dllearner.core.LearningAlgorithm;

/**
 * LearningAlgorithmPanel, tab 4. Choose LearningAlgorithm, change Options and
 * final initiate LearningAlgorithm.
 * 
 * @author Tilo Hielscher
 */
public class LearningAlgorithmPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 8721490771860452959L;

    private Config config;
    private StartGUI startGUI;
    private List<Class<? extends LearningAlgorithm>> learners;
    private JPanel choosePanel = new JPanel();
    private OptionPanel optionPanel;
    private JPanel initPanel = new JPanel();
    private JButton initButton, autoInitButton;
    private String[] cbItems = {};
    private JComboBox cb = new JComboBox(cbItems);
    private int choosenClassIndex;

    LearningAlgorithmPanel(Config config, StartGUI startGUI) {
	super(new BorderLayout());

	this.config = config;
	this.startGUI = startGUI;
	learners = config.getComponentManager().getLearningAlgorithms();

	initButton = new JButton("Init LearingAlgorithm");
	initButton.addActionListener(this);
	initPanel.add(initButton);
	initButton.setEnabled(true);
	autoInitButton = new JButton("Set");
	autoInitButton.addActionListener(this);

	// add into comboBox
	for (int i = 0; i < learners.size(); i++) {
	    cb.addItem(config.getComponentManager().getComponentName(
		    learners.get(i)));
	}

	choosePanel.add(cb);
	choosePanel.add(autoInitButton);
	cb.addActionListener(this);

	optionPanel = new OptionPanel(config, config.getLearningAlgorithm(),
		learners.get(choosenClassIndex));

	add(choosePanel, BorderLayout.PAGE_START);
	add(optionPanel, BorderLayout.CENTER);
	add(initPanel, BorderLayout.PAGE_END);

    }

    public void actionPerformed(ActionEvent e) {
	// read selected Class
	// choosenClassIndex = cb.getSelectedIndex();
	if (choosenClassIndex != cb.getSelectedIndex()) {
	    choosenClassIndex = cb.getSelectedIndex();
	    config.setInitLearningAlgorithm(false);
	    setLearningAlgorithm();
	}

	if (e.getSource() == autoInitButton)
	    setLearningAlgorithm();

	if (e.getSource() == initButton)
	    init();
    }

    /**
     * after this, you can change widgets
     */
    public void setLearningAlgorithm() {
	// config.autoInit();
	if (/*
	     * config.isInitLearningProblem() &&
	     */config.getLearningProblem() != null
		&& config.getReasoningService() != null) {
	    config.setLearningAlgorithm(config.getComponentManager()
		    .learningAlgorithm(learners.get(choosenClassIndex),
			    config.getLearningProblem(),
			    config.getReasoningService()));
	    updateOptionPanel();
	}
    }

    /**
     * after this, next tab can be used
     */
    public void init() {
	if (config.getLearningProblem() != null) {
	    config.getLearningAlgorithm().init();
	    config.setInitLearningAlgorithm(true);
	    System.out.println("init LearningAlgorithm");
	    startGUI.updateTabColors();
	}
	// config.autoInit();
    }

    /**
     * update OptionPanel with new selection
     */
    public void updateOptionPanel() {
	// update OptionPanel
	optionPanel.update(config.getLearningAlgorithm(), learners
		.get(choosenClassIndex));
    }
}
