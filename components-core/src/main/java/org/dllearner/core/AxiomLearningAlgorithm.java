/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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

import java.util.List;

import org.semanticweb.owlapi.model.OWLAxiom;

public interface AxiomLearningAlgorithm<T extends OWLAxiom> extends LearningAlgorithm {
	
	
	
	/**
	 * @return The best axioms found by the learning algorithm so far.
	 */
	List<T> getCurrentlyBestAxioms();
	
	/**
	 * @param nrOfAxioms Limit for the number or returned axioms.
	 * @return The best axioms found by the learning algorithm so far.
	 */
	List<T> getCurrentlyBestAxioms(int nrOfAxioms);
	
	/**
	 * @return The best evaluated axioms found by the learning algorithm so far.
	 */
	List<EvaluatedAxiom<T>> getCurrentlyBestEvaluatedAxioms();
	
	/**
	 * @param nrOfAxioms Limit for the number or returned evaluated axioms.
	 * @return The best evaluated axioms found by the learning algorithm so far.
	 */
	List<EvaluatedAxiom<T>> getCurrentlyBestEvaluatedAxioms(int nrOfAxioms);
	
	List<EvaluatedAxiom<T>> getCurrentlyBestEvaluatedAxioms(int nrOfAxioms,
															double accuracyThreshold);

}
