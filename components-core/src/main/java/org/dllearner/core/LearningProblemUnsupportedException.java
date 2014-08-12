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

import java.util.Collection;

/**
 * Exception, which is thrown when an application tries to run 
 * a learning algorithm with a learning problem it does not
 * support.
 * 
 * @author Jens Lehmann
 *
 */
public class LearningProblemUnsupportedException extends Exception {

	private static final long serialVersionUID = 177919265073997460L;

	public LearningProblemUnsupportedException(Class<? extends AbstractLearningProblem> problemClass, Class<? extends LearningAlgorithm> algorithmClass) {
		super("Warning: No suitable constructor registered for algorithm "
				+ algorithmClass.getName() + " and problem " + problemClass.getClass().getName() + ".");		
	}
	
	public LearningProblemUnsupportedException(Class<? extends AbstractLearningProblem> problemClass, Class<? extends LearningAlgorithm> algorithmClass, Collection<Class<? extends AbstractLearningProblem>> supportedProblems) {
		super("Warning: No suitable constructor registered for algorithm "
				+ algorithmClass.getName() + " and problem " + problemClass.getClass().getName()
				+ ". Registered constructors for " + algorithmClass.getName() + ": "
				+ supportedProblems + ".");		
	}
	
}
