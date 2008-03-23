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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.*;

/**
 * @author Tilo Hielscher
 * 
 */
public class RunPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1643304576470046636L;

	private JButton runButton, stopButton, treeButton;
	private JTextArea infoArea;
	private Config config;

	private ThreadRun thread;

	private GridBagLayout gridbag = new GridBagLayout();
	private GridBagConstraints constraints = new GridBagConstraints();
	private GridBagLayout gridbag2 = new GridBagLayout();
	private GridBagConstraints constraints2 = new GridBagConstraints();

	private JPanel showPanel = new JPanel();
	private JPanel centerPanel = new JPanel();
	private JPanel solutionPanel = new JPanel();
	private JPanel infoPanel = new JPanel();

	private String[] names = { "Algorithm Runtime", "OverallReasoningTime", "Instances",
			"Retrieval", "Subsumption" };

	private JLabel[] name = new JLabel[5];
	private Bar[] bar = new Bar[5];
	private JLabel[] time = new JLabel[5];
	private JLabel[] percent = new JLabel[5];

	RunPanel(Config config) {
		super(new BorderLayout());

		this.config = config;

		runButton = new JButton("Run");
		runButton.addActionListener(this);
		showPanel.add(runButton);
		stopButton = new JButton("Stop");
		stopButton.addActionListener(this);
		showPanel.add(stopButton);
		treeButton = new JButton("Tree");
		treeButton.addActionListener(this);
		showPanel.add(treeButton);

		infoPanel.setLayout(gridbag);
		constraints.anchor = GridBagConstraints.WEST;
		constraints.ipadx = 20;

		// make names
		for (int i = 0; i < 5; i++) {
			name[i] = new JLabel(names[i]);
		}

		// make bars
		for (int i = 0; i < 5; i++) {
			bar[i] = new Bar(100, 10, 0.0);
		}

		// make time
		for (int i = 0; i < 5; i++) {
			time[i] = new JLabel("-");
		}

		// make percent
		for (int i = 0; i < 5; i++) {
			percent[i] = new JLabel("-");
		}

		// layout for name, bar, time
		for (int i = 0; i < 5; i++) {
			buildConstraints(constraints, 0, i, 1, 1, 1, 1);
			gridbag.setConstraints(name[i], constraints);
			infoPanel.add(name[i], constraints);
			buildConstraints(constraints, 1, i, 1, 1, 1, 1);
			gridbag.setConstraints(bar[i], constraints);
			infoPanel.add(bar[i], constraints);
			buildConstraints(constraints, 2, i, 1, 1, 1, 1);
			gridbag.setConstraints(time[i], constraints);
			infoPanel.add(time[i], constraints);
			buildConstraints(constraints, 3, i, 1, 1, 1, 1);
			gridbag.setConstraints(percent[i], constraints);
			infoPanel.add(percent[i], constraints);
		}

		// text area
		infoArea = new JTextArea(20, 50);
		JScrollPane infoScroll = new JScrollPane(infoArea);

		// layout for centerPanel
		centerPanel.setLayout(gridbag2);
		constraints2.anchor = GridBagConstraints.CENTER;
		constraints2.fill = GridBagConstraints.BOTH;
		constraints2.ipadx = 10;
		buildConstraints(constraints2, 0, 0, 1, 1, 1, 1);
		gridbag2.setConstraints(infoPanel, constraints2);
		centerPanel.add(infoPanel, constraints2);
		buildConstraints(constraints2, 0, 1, 1, 1, 1, 1);
		gridbag2.setConstraints(infoScroll, constraints2);
		centerPanel.add(infoScroll, constraints2);

		// layout for this panel
		add(showPanel, BorderLayout.PAGE_START);
		add(centerPanel, BorderLayout.CENTER);
		add(solutionPanel, BorderLayout.PAGE_END);
	}

	public void actionPerformed(ActionEvent e) {
		// start
		if (e.getSource() == runButton && config.getLearningAlgorithm() != null
				&& !config.getThreadIsRunning()) {
			thread = new ThreadRun(config);
			config.getReasoningService().resetStatistics();
			thread.start();
			ThreadStatistics threadStatistics = new ThreadStatistics(config, this);
			threadStatistics.start();
		}
		// stop
		if (e.getSource() == stopButton && config.getLearningAlgorithm() != null) {
			thread.exit();
		}
		// tree
		if (e.getSource() == treeButton) {
			@SuppressWarnings("unused")
			TreeWindow a = new TreeWindow(config);
		}
	}

	/**
	 * Show Statistics.
	 */
	public void showStats() {
		Long algorithmRunTime = null;
		Long overallReasoningTime = null;
		Long instanceCheckReasoningTime = null;
		Long retrievalReasoningTime = null;
		Long subsumptionReasoningTime = null;

		infoArea.setText("");
		// best solutions
		if (config.getLearningAlgorithm().getBestSolutions(5) != null) {
			infoArea.append("Best solutions: \n\n"
					+ listToString(config.getLearningAlgorithm().getBestSolutions(10)) + "\n");
		}
		// solution score
		// if (config.getLearningAlgorithm().getSolutionScore() != null)
		// infoArea.append("SolutionScore:\n"
		// + config.getLearningAlgorithm().getSolutionScore().toString()
		// + "\n\n");

		// reasoner statistics
		if (config.getAlgorithmRunTime() != null) {
			algorithmRunTime = config.getAlgorithmRunTime();
			bar[0].update(1.0);
			time[0].setText(makeTime(algorithmRunTime));
			percent[0].setText("100%");
		}
		if (config.getReasoningService() != null) {
			overallReasoningTime = config.getReasoningService().getOverallReasoningTimeNs();
			bar[1].update((double) overallReasoningTime / (double) algorithmRunTime);
			time[1].setText(makeTime(overallReasoningTime));
			percent[1].setText(Percent(overallReasoningTime, algorithmRunTime));
		}
		if (config.getReasoningService().getNrOfInstanceChecks() > 0) {
			instanceCheckReasoningTime = config.getReasoningService()
					.getInstanceCheckReasoningTimeNs();
			name[2].setText(names[2] + " (" + config.getReasoningService().getNrOfInstanceChecks()
					+ ")");
			bar[2].update((double) instanceCheckReasoningTime / (double) algorithmRunTime);
			time[2].setText(makeTime(instanceCheckReasoningTime));
			percent[2].setText(Percent(instanceCheckReasoningTime, algorithmRunTime));
		}
		if (config.getReasoningService().getNrOfRetrievals() > 0) {
			retrievalReasoningTime = config.getReasoningService().getRetrievalReasoningTimeNs();
			name[3].setText(names[3] + " (" + config.getReasoningService().getNrOfRetrievals()
					+ ")");
			bar[3].update((double) retrievalReasoningTime / (double) algorithmRunTime);
			time[3].setText(makeTime(retrievalReasoningTime));
			percent[3].setText(Percent(retrievalReasoningTime, algorithmRunTime));
		}
		if (config.getReasoningService().getNrOfSubsumptionChecks() > 0) {
			subsumptionReasoningTime = config.getReasoningService().getSubsumptionReasoningTimeNs();
			name[4].setText(names[4] + " ("
					+ config.getReasoningService().getNrOfSubsumptionChecks() + ")");
			bar[4].update((double) subsumptionReasoningTime / (double) algorithmRunTime);
			time[4].setText(makeTime(subsumptionReasoningTime));
			percent[4].setText(Percent(subsumptionReasoningTime, algorithmRunTime));
		}
		repaint();
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

	/**
	 * Get a percent string like this: "10,5%"
	 * 
	 * @param a
	 * @param b
	 * @return string that shows percent
	 */
	public String Percent(Long a, Long b) {
		if (a != null && b != null) {
			Double c = (double) a / (double) b * (double) 100;
			c = Math.ceil(c * 10) / 10;
			return c.toString() + "% ";
		}
		return null;
	}

	/**
	 * Define GridBagConstraints
	 */
	private void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, int wx,
			int wy) {
		gbc.gridx = gx;
		gbc.gridy = gy;
		gbc.gridwidth = gw;
		gbc.gridheight = gh;
		gbc.weightx = wx;
		gbc.weighty = wy;
	}

	/**
	 * Make a string from list, every entry in new line.
	 * 
	 * @param list
	 *            it is the list.
	 * @return the string.
	 */
	public String listToString(List<?> list) {
		String string = "";
		for (int i = 0; i < list.size(); i++) {
			string += list.get(i) + "\n";
		}
		return string;
	}
}
