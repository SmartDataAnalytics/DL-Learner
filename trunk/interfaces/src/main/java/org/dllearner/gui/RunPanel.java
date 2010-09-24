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
package org.dllearner.gui;
/**
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
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.learningproblems.PosNegLPStandard;

/**
 * @author Tilo Hielscher
 * 
 */
public class RunPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1643304576470046636L;
	private DecimalFormat df = new DecimalFormat();

	private JButton runButton, stopButton, treeButton;
	private JTextArea infoArea;
	private Config config;
	private StartGUI startGUI;

//	private long algorithmStartTime = 0;

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

	private class AlgorithmThread extends Thread {
		private long startTime;
		private long endTime;
		
		@Override
		public void run() {
			startTime = System.nanoTime();
//			setPriority(Thread.MIN_PRIORITY);
			config.getLearningAlgorithm().start();
			endTime = System.nanoTime();
		}
		
		public long getRuntimeNanos() {
			if(isAlive()) {
//				System.out.println("ALIVE");
				return System.nanoTime() - startTime;
			} else {
//				System.out.println("NOT ALIVE");
				return endTime - startTime;
			}
		}		
	}
	AlgorithmThread algorithmThread;
	
	// separate thread for learning algorithm
//	Thread algorithmThread = new Thread() {
//		
//		private long startTime;
//		private long endTime;
//		
//		@Override
//		public void run() {
//			startTime = System.nanoTime();
////			setPriority(Thread.MIN_PRIORITY);
//			config.getLearningAlgorithm().start();
//			endTime = System.nanoTime();
//		}
//		
//		public long getRuntimeNanos() {
//			if(isAlive()) {
//				return System.nanoTime() - startTime;
//			} else {
//				return endTime - startTime;
//			}
//		}
//	};	
	
	RunPanel(Config config, StartGUI startGUI) {
		super(new BorderLayout());

		this.config = config;
		this.startGUI = startGUI;

		runButton = new JButton("Run");
		runButton.addActionListener(this);
		showPanel.add(runButton);
		stopButton = new JButton("Stop");
		stopButton.setEnabled(false);
		stopButton.addActionListener(this);
		showPanel.add(stopButton);
		
		treeButton = new JButton("Tree");
		treeButton.addActionListener(this);
		treeButton.setEnabled(false);
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
		if (e.getSource() == runButton) {
			algorithmThread = new AlgorithmThread();
			config.getReasoner().resetStatistics();
			algorithmThread.start();
//			algorithmStartTime = System.nanoTime();
//			algorithmThread.
			StatisticsThread threadStatistics = new StatisticsThread(config, this);
			threadStatistics.start();
			runButton.setEnabled(false);
			stopButton.setEnabled(true);
			// disable other panes (we do not want changes while
			// algorithm is running)
			startGUI.disableTabbedPane();
		}
		// stop
		if (e.getSource() == stopButton) {
			config.getLearningAlgorithm().stop();
			runButton.setEnabled(true);
			stopButton.setEnabled(false);
			// enable panels
			startGUI.enableTabbedPane();
		}
		// tree
		if (e.getSource() == treeButton) {
			TreeWindow a = new TreeWindow(config);
			a.setLocationRelativeTo(startGUI);
		}
	}

	/**
	 * Show Statistics.
	 */
	public void showStats() {
//		System.out.println("stat update " + System.currentTimeMillis());
		long overallReasoningTime = 0; // = null;
		long instanceCheckReasoningTime = 0; // = null;
		long retrievalReasoningTime = 0; // = null;
		long subsumptionReasoningTime = 0; // = null;

		infoArea.setText("");
		// best solutions
		if (config.getLearningAlgorithm().getCurrentlyBestDescription() != null) {
			infoArea.append("Best class descriptions in Manchester OWL Syntax: \n\n"
					+ getSolutionString(config.getLearningAlgorithm().getCurrentlyBestEvaluatedDescriptions(10)) + "\n");
		}
		// solution score
		// if (config.getLearningAlgorithm().getSolutionScore() != null)
		// infoArea.append("SolutionScore:\n"
		// + config.getLearningAlgorithm().getSolutionScore().toString()
		// + "\n\n");

		// update algorithm runtime
//		long algorithmRunTime = System.nanoTime() - algorithmStartTime;
		long algorithmRunTime = algorithmThread.getRuntimeNanos();
		bar[0].update(1.0);
		time[0].setText(makeTime(algorithmRunTime));
		percent[0].setText("100%");
		
		// update overall reasoning time
			overallReasoningTime = config.getReasoner().getOverallReasoningTimeNs();
			bar[1].update((double) overallReasoningTime / (double) algorithmRunTime);
			time[1].setText(makeTime(overallReasoningTime));
			percent[1].setText(Percent(overallReasoningTime, algorithmRunTime));
		
		if (config.getReasoner().getNrOfInstanceChecks() > 0) {
			instanceCheckReasoningTime = config.getReasoner()
					.getInstanceCheckReasoningTimeNs();
			name[2].setText(names[2] + " (" + config.getReasoner().getNrOfInstanceChecks()
					+ ")");
			bar[2].update((double) instanceCheckReasoningTime / (double) algorithmRunTime);
			time[2].setText(makeTime(instanceCheckReasoningTime));
			percent[2].setText(Percent(instanceCheckReasoningTime, algorithmRunTime));
		}
		if (config.getReasoner().getNrOfRetrievals() > 0) {
			retrievalReasoningTime = config.getReasoner().getRetrievalReasoningTimeNs();
			name[3].setText(names[3] + " (" + config.getReasoner().getNrOfRetrievals()
					+ ")");
			bar[3].update((double) retrievalReasoningTime / (double) algorithmRunTime);
			time[3].setText(makeTime(retrievalReasoningTime));
			percent[3].setText(Percent(retrievalReasoningTime, algorithmRunTime));
		}
		if (config.getReasoner().getNrOfSubsumptionChecks() > 0) {
			subsumptionReasoningTime = config.getReasoner().getSubsumptionReasoningTimeNs();
			name[4].setText(names[4] + " ("
					+ config.getReasoner().getNrOfSubsumptionChecks() + ")");
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
	private String makeTime(Long nanoSeconds) {
		if (nanoSeconds == null)
			return null;
		Long hours = 0L, minutes = 0L, seconds = 0L, millis = 0L; //, mikros = 0L, nanos = 0L;
		String timeStr = "";

//		nanos = nanoSeconds % 1000;
		nanoSeconds /= 1000;
//		mikros = nanoSeconds % 1000;
		nanoSeconds /= 1000;
		millis = nanoSeconds % 1000;
		nanoSeconds /= 1000;
		seconds = nanoSeconds % 60;
		nanoSeconds /= 60;
		minutes = nanoSeconds % 60;
		nanoSeconds /= 60;
		hours = nanoSeconds;

		if (hours > 0)
			timeStr += hours + "h ";
		if (minutes > 0)
			timeStr += minutes + "min ";
		if (seconds > 0)
			timeStr += seconds + "s ";
		if (millis > 0)
			timeStr += millis + "ms ";
//		if (false)
//			timeStr += mikros + "�s ";
//		if (false)
//			timeStr += nanos + "ns ";
		return timeStr;
	}

	/**
	 * Get a percent string like this: "10,5%"
	 * 
	 * @param a
	 * @param b
	 * @return string that shows percent
	 */
	private String Percent(Long a, Long b) {
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

	private String getSolutionString(List<? extends EvaluatedDescription> solutions) {
		String baseURI = config.getReasoner().getBaseURI();
		Map<String,String> prefixes = config.getReasoner().getPrefixes();
		String string = "";
		for (EvaluatedDescription d : solutions) {
			string += "accuracy: " + (df.format(d.getAccuracy()*100)) + "%: \t"
					+ d.getDescription().toManchesterSyntaxString(baseURI, prefixes) + "\n";
		}
		return string;
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
	
	/**
	 * Method is called when algorithm has terminated successfully.
	 */
	public void algorithmTerminated() {
		// the methods called are similar to those when the stop button is pressed
		stopButton.setEnabled(false);
		runButton.setEnabled(true);
		startGUI.enableTabbedPane();
		
//		System.out.println("TEST");
		
		// enable tree button
		if(((config.getLearningAlgorithm() instanceof OCEL)
				&& (config.getLearningProblem() instanceof PosNegLPStandard))
			|| (config.getLearningAlgorithm() instanceof CELOE )) {
			treeButton.setEnabled(true);
		}
	}
}
