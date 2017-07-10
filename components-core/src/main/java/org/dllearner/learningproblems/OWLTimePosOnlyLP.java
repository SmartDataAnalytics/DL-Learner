package org.dllearner.learningproblems;

import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.ComponentInitException;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 * Learning problem for positive instant based learning with negative instant sampling(?)
 */
public class OWLTimePosOnlyLP extends AbstractClassExpressionLearningProblem<ScorePosOnly<OWLNamedIndividual>> {
	@Override
	public void init() throws ComponentInitException {

	}

	@Override
	public ScorePosOnly<OWLNamedIndividual> computeScore(OWLClassExpression hypothesis, double noise) {
		return null;
	}

	@Override
	public double getAccuracyOrTooWeak(OWLClassExpression hypothesis, double noise) {
		return 0;
	}
}
