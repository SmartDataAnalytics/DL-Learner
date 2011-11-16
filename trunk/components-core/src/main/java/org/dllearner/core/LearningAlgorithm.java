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

import org.springframework.beans.factory.annotation.Autowired;


/**
 * Basic interface for all DL-Learner learning algorithms.
 * 
 * @author Jens Lehmann
 *
 */
public interface LearningAlgorithm extends Component {

	/**
	 * Starts the algorithm. It runs until paused, stopped, or
	 * a termination criterion has been reached.
	 */
	public abstract void start();	
	
	/**
	 * Get underlying learning problem.
	 * @return Underlying learning problem.
	 */
    public LearningProblem getLearningProblem();

    /**
     * Set the learning problem, which the algorithm should solve.
     * @param learningProblem The learning problem to solve.
     */
    @Autowired
    public void setLearningProblem(LearningProblem learningProblem);
	
}
