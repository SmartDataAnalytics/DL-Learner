package org.dllearner.core.ref;

import org.dllearner.core.LearningProblem;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * @author Lorenz Buehmann
 *
 */
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
		SearchTreeNode<OWLClassExpression> bestNode = searchTree.getNodes().last();
		return bestNode;
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
