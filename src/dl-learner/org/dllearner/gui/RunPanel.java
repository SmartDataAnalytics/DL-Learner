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
import org.dllearner.utilities.Helper;

/**
 * RunPanel let algorithm start and stop and show informations about.
 * 
 * @author Tilo Hielscher
 * 
 */
public class RunPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1643304576470046636L;

	private JButton runButton, stopButton, getBestSolutionButton, getSolutionScoreButton,
			getReasonerStatsButton;
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

		getReasonerStatsButton = new JButton("GetReasonerStats");
		getReasonerStatsButton.addActionListener(this);

		infoArea = new JTextArea(20, 50);
		JScrollPane infoScroll = new JScrollPane(infoArea);

		showPanel.add(runButton);
		showPanel.add(stopButton);

		infoPanel.add(infoScroll);

		solutionPanel.add(getBestSolutionButton);
		solutionPanel.add(getSolutionScoreButton);
		solutionPanel.add(getReasonerStatsButton);

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
		// ReasonerStats
		if (e.getSource() == getReasonerStatsButton /* && runBoolean*/) {
/*			infoArea.setText("");
			infoArea.append("Algorithm Runtime: "
					+ makeTime(config.getAlgorithmRunTime()) + "\n");
			infoArea.append("OverallReasoningTime: "
					+ makeTime(config.getReasoningService().getOverallReasoningTimeNs()) + "\n");
			infoArea.append("Instances (" + config.getReasoningService().getNrOfInstanceChecks()
					+ "): ");
			if (config.getReasoningService().getNrOfInstanceChecks() > 0)
				infoArea.append(makeTime(config.getReasoningService().getTimePerInstanceCheckNs())
						+ "\n");
			else
				infoArea.append(" - \n");
			infoArea.append("Retrieval (" + config.getReasoningService().getNrOfRetrievals()
					+ "): ");
			if (config.getReasoningService().getNrOfRetrievals() > 0)
				infoArea.append(makeTime(config.getReasoningService().getTimePerRetrievalNs())
						+ "\n");
			else
				infoArea.append(" - \n");
			infoArea.append("Subsumption ("
					+ config.getReasoningService().getNrOfSubsumptionChecks() + "): "
					+ makeTime(config.getReasoningService().getTimePerSubsumptionCheckNs()) + "\n");
	
*/  infoArea.setText(makeTime(9927255727L));
			
		}
	}

	/**
	 * Build a String form nanoSeconds.
	 * 
	 * @param nanoSeconds
	 *            is type of Long and represent a time interval in ns
	 * @return a string like this: 3h 12min 46s 753ms
	 */
	public String makeTime(long nanoSeconds) {
		String time = "";
		long hours, minutes, seconds, millis, mikros, nanos;

		// it cuts last decimals
		nanos = nanoSeconds;
		mikros = nanos / 1000;
		millis = mikros / 1000;
		seconds = millis / 1000;
		minutes = seconds / 60;
		hours = minutes / 60;
		
		// and calculate back
		minutes -= hours * 60; 
		seconds -= minutes * 60; 
		millis -= seconds * 1000;
		mikros -= millis * 1000;
		nanos -= mikros * 1000;
		
		System.out.println("TEST: " + hours + "h " + minutes + "min " + seconds + "s " + millis + "ms " + mikros + "mikro " + nanos + "nano ");
		
		
		
		
		
		System.out.println(Helper.prettyPrintNanoSeconds(nanoSeconds, true, true));
		
		
		
		if (hours > 0)
			time += hours + "h ";
		if (minutes > 0)
			time += minutes + "min ";
		if (seconds > 0)
			time += seconds + "s ";
		if (millis > 0)
			time += millis + "ms ";
		if (mikros > 0)
			time += mikros + "ms ";
		if (nanos > 0)
			time += nanos + "ms ";
		
		// System.out.println("time: " + time);
		return time;
	}
}
