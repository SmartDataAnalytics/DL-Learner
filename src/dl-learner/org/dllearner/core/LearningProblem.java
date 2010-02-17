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

import org.dllearner.core.owl.Description;

/**
 * Base class for all learning problems.
 * 
 * @todo The current learning problem implementations 
 * assume that we learn a concept, which does not exist
 * in the knowledge base so far. However, often we want
 * to learn a complex definition for a concept which
 * is already integrated in a subsumption hierarchy. This
 * means it would make sense to specifiy the list of these
 * superclasses as an additional argument of the learning
 * problem. The learning algorithms could then make use of
 * this to optimise their search for a solution. (More
 * generally, one could specify the name of the concept, which
 * should be improved.)
 * 
 * @author Jens Lehmann
 *
 */
public abstract class LearningProblem extends Component {
	
	protected ReasoningService reasoningService;
	
	public LearningProblem(ReasoningService reasoningService) {
		this.reasoningService = reasoningService;
	}
	
	public abstract Score computeScore(Description concept);
	
	// TODO: remove? reasoning service should probably not be accessed via
	// learning problem
	public ReasoningService getReasoningService() {
		return reasoningService;
	}
	
}
