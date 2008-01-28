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

import javax.swing.*;

import org.dllearner.core.dl.Concept;

/**
 * OutputPanel
 * 
 * @author Tilo Hielscher
 * 
 */
public class RunPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1643304576470046636L;

    private JButton runButton;
    private JTextArea infoArea;
    private Config config;

    RunPanel(Config config) {
	super(new BorderLayout());

	this.config = config;

	runButton = new JButton("Run");
	runButton.addActionListener(this);

	infoArea = new JTextArea(20, 50);
	JScrollPane infoScroll = new JScrollPane(infoArea);

	JPanel showPanel = new JPanel();
	showPanel.add(runButton);
	JPanel infoPanel = new JPanel();
	infoPanel.add(infoScroll);

	add(showPanel, BorderLayout.PAGE_START);
	add(infoPanel, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == runButton && config.getLearningAlgorithm() != null) {
	    config.getLearningAlgorithm().start();
	    Concept solution = config.getLearningAlgorithm().getBestSolution();
	    infoArea.setText(solution.toString());
	}
    }
}
