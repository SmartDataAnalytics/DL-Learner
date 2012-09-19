/**
 * Copyright (C) 2007-2011, Jens Lehmann
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
 */

package org.dllearner.core;

/**
 * 
 * Interface for algorithms, which can be stopped and checked whether they are
 * running. This allows learning algorithms to be run in separate threads or
 * be terminated by the user.
 * 
 * @author Jens Lehmann
 *
 */
public interface StoppableLearningAlgorithm extends LearningAlgorithm {

	/**
	 * Stops the algorithm gracefully. A stopped algorithm cannot be resumed anymore.
	 * Use this method for cleanup and freeing memory.
	 */
	public abstract void stop();
	
	/**
	 * Returns whether the learning algorithm is running. Implementation
	 * should use a boolean status variable in their implementations of
	 * the start and resume methods.
	 * @return True if the algorithm is running, false otherwise.
	 */
	public abstract boolean isRunning();
	
}
