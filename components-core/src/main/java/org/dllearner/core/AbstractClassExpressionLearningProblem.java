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

import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * Base class for all class expression learning problems.
 * 
 * @author Lorent Buehmann
 *
 */
public abstract class AbstractClassExpressionLearningProblem<T extends Score>  extends AbstractLearningProblem<T, OWLClassExpression, EvaluatedDescription<T>> implements LearningProblem {
	
	public AbstractClassExpressionLearningProblem(){

    }
	/**
	 * Constructs a learning problem using a reasoning service for
	 * querying the background knowledge. It can be used for 
	 * evaluating solution candidates.
	 * @param reasoner The reasoning service used as 
	 * background knowledge.
	 */
	public AbstractClassExpressionLearningProblem(AbstractReasonerComponent reasoner) {
		this.reasoner = reasoner;
	}
}
