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
package org.dllearner.learningproblems;

import java.util.SortedSet;

import org.dllearner.Score;
import org.dllearner.core.LearningProblemNew;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.dl.Concept;
import org.dllearner.core.dl.Individual;

/**
 * The definition learning problem.
 * 
 * @author Jens Lehmann
 *
 */
public abstract class DefinitionLP extends LearningProblemNew {

	protected boolean useRetrievalForClassification = false;
	protected UseMultiInstanceChecks useDIGMultiInstanceChecks = UseMultiInstanceChecks.TWOCHECKS;

	/**
	 * If instance checks are used for testing concepts (e.g. no retrieval), then
	 * there are several options to do this. The enumeration lists the supported
	 * options. These options are only important if the reasoning mechanism 
	 * supports sending several reasoning requests at once as it is the case for
	 * DIG reasoners.
	 * 
	 * @author Jens Lehmann
	 *
	 */
	public enum UseMultiInstanceChecks {
		/**
		 * Perform a separate instance check for each example.
		 */
		NEVER,
		/**
		 * Perform one instance check for all positive and one instance check
		 * for all negative examples.
		 */
		TWOCHECKS,
		/**
		 * Perform all instance checks at once.
		 */
		ONECHECK
	};
	
	public DefinitionLP(ReasoningService reasoningService) {
		super(reasoningService);
	}
	
	public abstract int coveredNegativeExamplesOrTooWeak(Concept concept);
	
	public abstract Score computeScore(Concept concept);
	
	/**
	 * @todo Method not implemented yet.
	 * @param concept
	 * @param adc
	 * @return
	 */
	public Score computeScore(Concept concept, Concept adc) {
		throw new UnsupportedOperationException();
	}
	
	public abstract SortedSet<Individual> getNegativeExamples();

	public abstract  SortedSet<Individual> getPositiveExamples();

	// TODO: remove? reasoning service should probably not be accessed via
	// learning problem
	public abstract ReasoningService getReasoningService();
	
}
