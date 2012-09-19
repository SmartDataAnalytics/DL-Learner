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

import org.dllearner.core.owl.Description;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for all learning problems.
 * See also the wiki page for 
 * <a href="http://dl-learner.org/Projects/DLLearner/Architecture">DL-Learner-Architecture</a>.
 * Currently, we assume that all learning problems have the goal
 * of learning class descriptions. However, this may be extended
 * to other scenarios if desired. 
 * 
 * @author Jens Lehmann
 *
 */
public abstract class AbstractLearningProblem extends AbstractComponent implements LearningProblem {
	
	protected AbstractReasonerComponent reasoner;

    public AbstractLearningProblem(){

    }
	/**
	 * Constructs a learning problem using a reasoning service for
	 * querying the background knowledge. It can be used for 
	 * evaluating solution candidates.
	 * @param reasoner The reasoning service used as 
	 * background knowledge.
	 */
	public AbstractLearningProblem(AbstractReasonerComponent reasoner) {
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
	public void changeReasonerComponent(AbstractReasonerComponent reasoner) {
		this.reasoner = reasoner;
	}
		
	/**
	 * Computes the <code>Score</code> of a given class description
	 * with respect to this learning problem.
	 * This can (but does not need to) be used by learning algorithms
	 * to measure how good the description fits the learning problem.
	 * Score objects are used to store e.g. covered examples, accuracy etc.,
	 * so often it is more efficient to only create score objects for
	 * promising class descriptions.
	 * @param description A class description (as solution candidate for this learning problem).
	 * @return A <code>Score</code> object.
	 */
	public abstract Score computeScore(Description description);
	
	/**
	 * Evaluates the description by computing the score and returning an
	 * evaluated description of the correct type (ClassLearningProblem
	 * returns EvaluatedDescriptionClass instead of generic EvaluatedDescription).
	 * @param description Description to evaluate.
	 * @return 
	 */
	public abstract EvaluatedDescription evaluate(Description description);
	
	/**
	 * This method returns a value, which indicates how accurate a
	 * class description solves a learning problem. There can be different
	 * ways to compute accuracy depending on the type of learning problem
	 * and other factors. However, all implementations are required to 
	 * return a value between 0 and 1, where 1 stands for the highest
	 * possible accuracy and 0 for the lowest possible accuracy.
	 * 
	 * @return A value between 0 and 1 indicating the quality (of a class description).
	 */	
	public abstract double getAccuracy(Description description);	
	
	/**
	 * This method computes the accuracy as {@link #getAccuracy(Description)},
	 * but returns -1 instead of the accuracy if 1.) the accuracy of the 
	 * description is below the given threshold and 2.) the accuracy of all
	 * more special w.r.t. subsumption descriptions is below the given threshold.
	 * This is used for efficiency reasons, i.e. -1 can be returned instantly if
	 * it is clear that the description and all its refinements are not 
	 * sufficiently accurate.
	 * 
	 * @return A value between 0 and 1 indicating the quality (of a class description)
	 * or -1 as described above.
	 */	
	public abstract double getAccuracyOrTooWeak(Description description, double noise);

    /**
     * Implementations of learning problems can use this class
     * variable to perform reasoner operations.
     */
    public AbstractReasonerComponent getReasoner() {
        return reasoner;
    }

    @Autowired(required=false)
    public void setReasoner(AbstractReasonerComponent reasoner) {
        this.reasoner = reasoner;
    }
}
