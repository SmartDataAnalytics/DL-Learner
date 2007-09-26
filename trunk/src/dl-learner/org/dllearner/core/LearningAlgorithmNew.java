/**
 * Copyright (C) 2007, Jens Lehmann
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
package org.dllearner.core;

import java.util.Collection;
import java.util.LinkedList;

import org.dllearner.Score;
import org.dllearner.core.dl.Concept;

/**
 * @author Jens Lehmann
 *
 */
public abstract class LearningAlgorithmNew extends Component {

	/**
	 * Starts the algorithm.
	 *
	 */
	public abstract void start();
	

	/**
	 * Stops the algorithm gracefully.
	 *
	 */
	public abstract void stop();

	/**
	 * Every algorithm must be able to return the score of the
	 * best solution found.
	 * @return Best score.
	 */
	public abstract Score getSolutionScore();
	
	/**
	 * Returns the best solutions obtained so far.
	 * @return Best solution.
	 */
	public abstract Concept getBestSolution();
	
	/**
	 * Returns all learning problems supported by this component.
	 */
	public static Collection<Class<? extends LearningProblemNew>> supportedLearningProblems() {
		return new LinkedList<Class<? extends LearningProblemNew>>();
	}
	
}
