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

	private JButton runButton, stopButton;
	private JTextArea infoArea;
	private Config config;

	private ThreadRun thread;
	// private Boolean runBoolean = new Boolean(false);

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

		infoArea = new JTextArea(20, 50);
		JScrollPane infoScroll = new JScrollPane(infoArea);

		showPanel.add(runButton);
		showPanel.add(stopButton);

		infoPanel.add(infoScroll);

		add(showPanel, BorderLayout.PAGE_START);
		add(infoPanel, BorderLayout.CENTER);
		add(solutionPanel, BorderLayout.PAGE_END);
	}

	public void actionPerformed(ActionEvent e) {
		// start
		if (e.getSource() == runButton && config.getLearningAlgorithm() != null
				&& !config.getThreadIsRunning()) {
			thread = new ThreadRun(config);
			config.getReasoningService().resetStatistics();
			thread.start();
			// this.runBoolean = true;
			ThreadStatistics threadStatistics = new ThreadStatistics(config, this);
			threadStatistics.start();
		}
		// stop
		if (e.getSource() == stopButton && config.getLearningAlgorithm() != null) {
			thread.exit();
		}
	}

	/**
	 * Show Statistics.
	 */
	public void showStats() {
		infoArea.setText("");
		// best solution
		if (config.getLearningAlgorithm().getBestSolution() != null)
			infoArea.append("BestSolution:\n"
					+ config.getLearningAlgorithm().getBestSolution().toString() + "\n\n");
		// solution score
//		if (config.getLearningAlgorithm().getSolutionScore() != null)
//			infoArea.append("SolutionScore:\n"
//					+ config.getLearningAlgorithm().getSolutionScore().toString() + "\n\n");
		// reasoner statistics
		if (config.getAlgorithmRunTime() != null)
			infoArea.append("Algorithm Runtime: " + makeTime(config.getAlgorithmRunTime()) + "\n");
		infoArea.append("OverallReasoningTime: "
				+ makeTime(config.getReasoningService().getOverallReasoningTimeNs()) + "\n");
		infoArea.append("Instances (" + config.getReasoningService().getNrOfInstanceChecks()
				+ "): ");
		if (config.getReasoningService().getNrOfInstanceChecks() > 0)
			infoArea.append(makeTime(config.getReasoningService().getTimePerInstanceCheckNs())
					+ "\n");
		else
			infoArea.append(" - \n");
		infoArea.append("Retrieval (" + config.getReasoningService().getNrOfRetrievals() + "): ");
		if (config.getReasoningService().getNrOfRetrievals() > 0)
			infoArea.append(makeTime(config.getReasoningService().getTimePerRetrievalNs()) + "\n");
		else
			infoArea.append(" - \n");
		if (config.getReasoningService().getNrOfSubsumptionChecks() > 0)
			infoArea.append("Subsumption ("
					+ config.getReasoningService().getNrOfSubsumptionChecks() + "): "
					+ makeTime(config.getReasoningService().getTimePerSubsumptionCheckNs()) + "\n");
	}

	/**
	 * Build a String from nanoSeconds.
	 * 
	 * @param nanoSeconds
	 *            is type of Long and represent a time interval in ns
	 * @return a string like this: 3h 10min 46s 753ms
	 */
	public String makeTime(Long nanoSeconds) {
		if (nanoSeconds == null)
			return null;
		Long hours = 0L, minutes = 0L, seconds = 0L, millis = 0L, mikros = 0L, nanos = 0L;
		String time = "";

		nanos = nanoSeconds % 1000;
		nanoSeconds /= 1000;
		mikros = nanoSeconds % 1000;
		nanoSeconds /= 1000;
		millis = nanoSeconds % 1000;
		nanoSeconds /= 1000;
		seconds = nanoSeconds % 60;
		nanoSeconds /= 60;
		minutes = nanoSeconds % 60;
		nanoSeconds /= 60;
		hours = nanoSeconds;

		if (hours > 0)
			time += hours + "h ";
		if (minutes > 0)
			time += minutes + "min ";
		if (seconds > 0)
			time += seconds + "s ";
		if (millis > 0)
			time += millis + "ms ";
		if (false)
			time += mikros + "µs ";
		if (false)
			time += nanos + "ns ";
		return time;
	}
}
