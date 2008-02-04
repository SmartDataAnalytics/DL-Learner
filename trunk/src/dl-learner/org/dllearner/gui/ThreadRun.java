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

/**
 * threadRun
 * 
 * @author Tilo Hielscher
 */
public class ThreadRun extends Thread {

    Config config;

    public ThreadRun(Config config) {
	this.config = config;
    }

    // method to start thread
    @Override
    public void run() {
	if (config.getLearningAlgorithm() != null)
	    config.getLearningAlgorithm().start();
    }

    public void exit() {
	if (config.getLearningAlgorithm() != null)
	    config.getLearningAlgorithm().stop();
    }
}
