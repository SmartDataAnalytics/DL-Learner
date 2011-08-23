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

import java.util.List;

import org.dllearner.core.owl.Axiom;

public interface AxiomLearningAlgorithm extends LearningAlgorithm {
	
	
	
	/**
	 * @return The best axioms found by the learning algorithm so far.
	 */
	public List<Axiom> getCurrentlyBestAxioms();
	
	/**
	 * @param nrOfAxioms Limit for the number or returned axioms.
	 * @return The best axioms found by the learning algorithm so far.
	 */
	public List<Axiom> getCurrentlyBestAxioms(int nrOfAxioms);
	
	/**
	 * @return The best evaluated axioms found by the learning algorithm so far.
	 */
	public List<EvaluatedAxiom> getCurrentlyBestEvaluatedAxioms();
	
	/**
	 * @param nrOfAxioms Limit for the number or returned evaluated axioms.
	 * @return The best evaluated axioms found by the learning algorithm so far.
	 */
	public List<EvaluatedAxiom> getCurrentlyBestEvaluatedAxioms(int nrOfAxioms);
	
	public List<EvaluatedAxiom> getCurrentlyBestEvaluatedAxioms(int nrOfAxioms,
			double accuracyThreshold);

}
