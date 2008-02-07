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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.*;

import org.dllearner.core.ReasonerComponent;

/**
 * ReasonerPanel
 * 
 * @author Tilo Hielscher
 * 
 */
public class ReasonerPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = -7678275020058043937L;

    private List<Class<? extends ReasonerComponent>> reasoners;
    private JPanel choosePanel = new JPanel();
    private JPanel initPanel = new JPanel();
    private OptionPanel optionPanel;
    private JButton initButton, autoInitButton;
    private Config config;
    private String[] cbItems = {};
    private JComboBox cb = new JComboBox(cbItems);
    private int choosenClassIndex;

    ReasonerPanel(final Config config) {
	super(new BorderLayout());

	this.config = config;
	reasoners = config.getComponentManager().getReasonerComponents();

	initButton = new JButton("Init Reasoner");
	initButton.addActionListener(this);
	initPanel.add(initButton);
	initButton.setEnabled(false);
	autoInitButton = new JButton("AutoInit");
	autoInitButton.addActionListener(this);

	choosePanel.add(cb);

	// add into comboBox
	for (int i = 0; i < reasoners.size(); i++) {
	    cb.addItem(config.getComponentManager().getComponentName(
		    reasoners.get(i)));
	}

	optionPanel = new OptionPanel(config, config.getReasoner(), reasoners
		.get(choosenClassIndex));

	choosePanel.add(autoInitButton);
	cb.addActionListener(this);

	add(choosePanel, BorderLayout.PAGE_START);
	add(optionPanel, BorderLayout.CENTER);
	add(initPanel, BorderLayout.PAGE_END);

	choosenClassIndex = cb.getSelectedIndex();
	setReasoner();
    }

    public void actionPerformed(ActionEvent e) {
	// read selected Class
	// choosenClassIndex = cb.getSelectedIndex();
	if (choosenClassIndex != cb.getSelectedIndex()) {
	    choosenClassIndex = cb.getSelectedIndex();
	    config.setInitReasoner(false);
	    setReasoner();
	}

	if (e.getSource() == autoInitButton)
	    setReasoner();

	if (e.getSource() == initButton)
	    init();
    }

    /**
     * after this, you can change widgets
     */
    public void setReasoner() {
	config.autoInit();
	if (config.isInitKnowledgeSource()) {
	    config.setReasoner(config.getComponentManager().reasoner(
		    reasoners.get(choosenClassIndex),
		    config.getKnowledgeSource()));
	    updateOptionPanel();
	}
    }

    /**
     * after this, next tab can be used
     */
    public void init() {
	config.autoInit();
    }

    /**
     * update OptionPanel with new selection
     */
    public void updateOptionPanel() {
	optionPanel.update(config.getReasoner(), reasoners
		.get(choosenClassIndex));
    }

}
