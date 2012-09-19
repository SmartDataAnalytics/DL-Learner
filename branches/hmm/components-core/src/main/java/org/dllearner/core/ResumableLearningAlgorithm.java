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
 * Interface for learning algorithms, which can be paused and later continued.
 * 
 * @author Jens Lehmann
 *
 */
public interface ResumableLearningAlgorithm {

	/**
	 * Pauses the algorithm (not all algorithms need to implement
	 * this operation).
	 */
	public void pause();	
	
	/**
	 * Resumes the algorithm (not all algorithms need to implement
	 * this operation). You can use this method to continue
	 * an algorithm run even after a termination criterion has been
	 * reached. It will run until paused, stopped, or terminated
	 * again.
	 */
	public void resume();
	
}
