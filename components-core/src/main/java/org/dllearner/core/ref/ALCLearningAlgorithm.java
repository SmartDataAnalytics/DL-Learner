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
package org.dllearner.core.ref;

import org.dllearner.core.LearningProblem;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * @author Lorenz Buehmann
 *
 */
// not for conf
public class ALCLearningAlgorithm extends RefinementOperatorBasedLearningAlgorithmBase<OWLClassExpression>{
	
	public ALCLearningAlgorithm(OWLOntology ontology, OWLReasoner reasoner) {
	
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningAlgorithm#getLearningProblem()
	 */
	@Override
	public LearningProblem getLearningProblem() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningAlgorithm#setLearningProblem(org.dllearner.core.LearningProblem)
	 */
	@Override
	public void setLearningProblem(LearningProblem learningProblem) {
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.ref.RefinementOperatorBasedLearningAlgorithmBase#computeStartNode()
	 */
	@Override
	protected SearchTreeNode<OWLClassExpression> computeStartNode() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.ref.RefinementOperatorBasedLearningAlgorithmBase#getNextNodeToExpand()
	 */
	@Override
	protected SearchTreeNode<OWLClassExpression> getNextNodeToExpand() {
		return searchTree.getNodes().last();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.ref.RefinementOperatorBasedLearningAlgorithmBase#isValid(java.lang.Object)
	 */
	@Override
	protected boolean isValid(OWLClassExpression refinement) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.ref.RefinementOperatorBasedLearningAlgorithmBase#terminationCriteriaSatisfied()
	 */
	@Override
	protected boolean terminationCriteriaSatisfied() {
		return false;
	}

}
