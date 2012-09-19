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
 * Active Learning algorithms are those, which can use feedback from an oracle.
 * 
 * @author Jens Lehmann
 *
 */
public interface ActiveLearningAlgorithm extends LearningAlgorithm {

	/**
	 * In order to separate/hide the implementation of an oracle from the active learning algorithm itself,
	 * an oracle object is passed to the algorithm.
	 * @param oracle The oracle to be used by the learning algorithm.
	 */
	public void setOracle(Oracle oracle);
	
}
