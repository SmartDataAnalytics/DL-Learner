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
 * This thread is responsible for sending update events to the GUI.
 * In regular intervals it tests whether the learning algorithm is
 * running and calls methods to update the statistics in the run
 * panel.
 * 
 * @author Tilo Hielscher
 * @author Jens Lehmann
 */
public class StatisticsThread extends Thread {

	private Config config;
	private RunPanel runPanel;

	public StatisticsThread(Config config, RunPanel runPanel) {
		this.config = config;
		this.runPanel = runPanel;
	}

	/**
	 * Calls {@link RunPanel#showStats()} in regular intervals.
	 */
	@Override
	public void run() {	
		try {
			// initial delay of one second
			sleep(1000);
			while (config.getLearningAlgorithm().isRunning()) {
				// update statistics every 3 seconds
				runPanel.showStats();
				sleep(2000);
			}
			// show final stats
//			System.out.println("terminated");
//			System.exit(0);
			runPanel.showStats();
			runPanel.algorithmTerminated();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
}
