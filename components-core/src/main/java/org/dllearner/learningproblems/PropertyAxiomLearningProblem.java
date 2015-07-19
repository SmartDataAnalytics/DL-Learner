package org.dllearner.learningproblems;

import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedAxiom;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLPropertyAxiom;

@ComponentAnn(name = "PropertyAxiomLearningProblem", shortName = "palp", version = 0.6)
public class PropertyAxiomLearningProblem<T extends OWLPropertyAxiom> extends AbstractLearningProblem<AxiomScore, T, EvaluatedAxiom<T>>{

	private OWLProperty propertyToDescribe;

	@Override
	public void init() throws ComponentInitException {
		// TODO Auto-generated method stub
	}

	@Override
	public AxiomScore computeScore(T hypothesis, double noise) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getAccuracy(T hypothesis, double noise) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getAccuracyOrTooWeak(T hypothesis, double noise) {
		// TODO Auto-generated method stub
		return 0;
	}
}
