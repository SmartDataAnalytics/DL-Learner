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

/**
 * RunPanel let algorithm start and stop and show informations about.
 * 
 * @author Tilo Hielscher
 * 
 */
public class RunPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1643304576470046636L;

	private JButton runButton, stopButton, getBestSolutionButton, getSolutionScoreButton;
	private JTextArea infoArea;
	private Config config;

	private ThreadRun thread;
	private Boolean runBoolean = new Boolean(false);

	private JPanel showPanel = new JPanel();
	private JPanel infoPanel = new JPanel();
	private JPanel solutionPanel = new JPanel();

	RunPanel(Config config) {
		super(new BorderLayout());

		this.config = config;

		runButton = new JButton("Run");
		runButton.addActionListener(this);
		stopButton = new JButton("Stop");
		stopButton.addActionListener(this);

		getBestSolutionButton = new JButton("GetBestSolution");
		getBestSolutionButton.addActionListener(this);

		getSolutionScoreButton = new JButton("GetSolutionScore");
		getSolutionScoreButton.addActionListener(this);

		infoArea = new JTextArea(20, 50);
		JScrollPane infoScroll = new JScrollPane(infoArea);

		showPanel.add(runButton);
		showPanel.add(stopButton);

		infoPanel.add(infoScroll);

		solutionPanel.add(getBestSolutionButton);
		solutionPanel.add(getSolutionScoreButton);

		add(showPanel, BorderLayout.PAGE_START);
		add(infoPanel, BorderLayout.CENTER);
		add(solutionPanel, BorderLayout.PAGE_END);
	}

	public void actionPerformed(ActionEvent e) {
		// start
		if (e.getSource() == runButton && config.getLearningAlgorithm() != null) {
			thread = new ThreadRun(config);
			thread.start();
			this.runBoolean = true;
		}
		// stop
		if (e.getSource() == stopButton && config.getLearningAlgorithm() != null) {
			thread.exit();
		}
		// getBestSolution
		if (e.getSource() == getBestSolutionButton && runBoolean) {
			infoArea.setText(config.getLearningAlgorithm().getBestSolution().toString());
		}
		// getSolutionScore
		if (e.getSource() == getSolutionScoreButton && runBoolean) {
			infoArea.setText(config.getLearningAlgorithm().getSolutionScore().toString());
		}
	}

}
