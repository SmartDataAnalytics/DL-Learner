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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.LearningProblem;

/**
 * LearningProblemPanel tab 3. Choose LearingProblem, change Options and final
 * initiate LearningProblem.
 * 
 * @author Tilo Hielscher
 */
public class LearningProblemPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = -3819627680918930203L;

    private Config config;
    private StartGUI startGUI;
    private List<Class<? extends LearningProblem>> problems;
    private String[] lpBoxItems = {};
    private JComboBox cb = new JComboBox(lpBoxItems);
    private JPanel choosePanel = new JPanel();
    private OptionPanel optionPanel;
    private JPanel initPanel = new JPanel();
    private JButton initButton, setButton;
    private int choosenClassIndex;

    LearningProblemPanel(final Config config, StartGUI startGUI) {
	super(new BorderLayout());

	this.config = config;
	this.startGUI = startGUI;
	problems = config.getComponentManager().getLearningProblems();

	initButton = new JButton("Init LearningProblem");
	initButton.addActionListener(this);
	initPanel.add(initButton);
	initButton.setEnabled(true);
	setButton = new JButton("Set");
	setButton.addActionListener(this);
	choosePanel.add(cb);
	choosePanel.add(setButton);
	cb.addActionListener(this);

	// add into comboBox
	for (int i = 0; i < problems.size(); i++) {
	    cb.addItem(config.getComponentManager().getComponentName(
		    problems.get(i)));
	}

	// read choosen LearningProblem
	choosenClassIndex = cb.getSelectedIndex();

	optionPanel = new OptionPanel(config, config.getLearningProblem(),
		config.getOldLearningProblem(), problems.get(choosenClassIndex));

	add(choosePanel, BorderLayout.PAGE_START);
	add(optionPanel, BorderLayout.CENTER);
	add(initPanel, BorderLayout.PAGE_END);

	choosenClassIndex = cb.getSelectedIndex();
	// setLearningProblem();
	updateInitButtonColor();
    }

    public void actionPerformed(ActionEvent e) {
	// read selected LearningProblemClass
	if (choosenClassIndex != cb.getSelectedIndex()) {
	    this.choosenClassIndex = cb.getSelectedIndex();
	    config.setInitLearningProblem(false);
	    setLearningProblem();
	}

	if (e.getSource() == setButton)
	    setLearningProblem();

	if (e.getSource() == initButton)
	    init();
    }

    /**
     * after this, you can change widgets
     */
    private void setLearningProblem() {
	if (config.isInitReasoner()) {
	    config.setLearningProblem(config.getComponentManager()
		    .learningProblem(problems.get(choosenClassIndex),
			    config.getReasoningService()));
	    startGUI.updateTabColors();
	    updateOptionPanel();
	}
    }

    /**
     * after this, next tab can be used
     */
    private void init() {
	setLearningProblem();
	if (config.getReasoner() != null && config.getLearningProblem() != null) {
	    try {
			config.getLearningProblem().init();
		} catch (ComponentInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    config.setInitLearningProblem(true);
	    System.out.println("init LearningProblem");
	    startGUI.updateTabColors();
	}
    }

    /**
     * update OptionPanel with new selection
     */
    private void updateOptionPanel() {
	// update OptionPanel
	optionPanel.update(config.getLearningProblem(), config
		.getOldLearningProblem(), problems.get(choosenClassIndex));
    }

    /**
     * make init-button red if you have to click
     */
    public void updateInitButtonColor() {
	if (!config.isInitLearningProblem()) {
	    initButton.setForeground(Color.RED);
	} else
	    initButton.setForeground(Color.BLACK);
    }
}
