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
package org.dllearner.core;

import org.dllearner.core.owl.Description;

/**
 * Base class for all learning problems.
 * See also the wiki page for 
 * <a href="http://dl-learner.org/Projects/DLLearner/Architecture">DL-Learner-Architecture</a>.
 * Currently, we assume that all learning problems have the goal
 * of learning class descriptions. However, this may be extended
 * to other scenarios if desired. 
 * 
 * TODO: The current learning problem implementations assume that 
 * we learn a description for a class, which does not exist
 * in the knowledge base so far (if it exists, it needs to be ignored
 * explicitly). However, often we want to learn a complex definition 
 * for a concept which is already integrated in a subsumption hierarchy
 * or may already have an associated description. It may make sense
 * to use this knowledge for (re-)learning descriptions.
 * 
 * @author Jens Lehmann
 *
 */
public abstract class LearningProblem extends Component {
	
	/**
	 * Implementations of learning problems can use this class
	 * variable to perform reasoner operations.
	 */
	protected ReasonerComponent reasoner;
	
	/**
	 * Constructs a learning problem using a reasoning service for
	 * querying the background knowledge. It can be used for 
	 * evaluating solution candidates.
	 * @param reasoner The reasoning service used as 
	 * background knowledge.
	 */
	public LearningProblem(ReasonerComponent reasoner) {
		this.reasoner = reasoner;
	}
	
	/**
	 * Method to exchange the reasoner underlying the learning
	 * problem.
	 * Implementations, which do not only use the provided reasoning
	 * service class variable, must make sure that a call to this method
	 * indeed changes the reasoning service. 
	 * @param reasoner New reasoning service.
	 */
	public void changeReasoningService(ReasonerComponent reasoner) {
		this.reasoner = reasoner;
	}
	
	/**
	 * Computes the <code>Score</code> of a given class description
	 * with respect to this learning problem.
	 * This can (but does not need to) be used by learning algorithms
	 * to measure how good the description fits the learning problem.
	 * @param description A class description (as solution candidate for this learning problem).
	 * @return A <code>Score</code> object.
	 */
	public abstract Score computeScore(Description description);
	
}
