package org.dllearner.learningproblems;

import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.Score;
import org.semanticweb.owlapi.model.OWLPropertyAxiom;

@ComponentAnn(name = "PropertyAxiomLearningProblem", shortName = "palp", version = 0.6)
public class PropertyAxiomLearningProblem extends AbstractLearningProblem<Score, OWLPropertyAxiom>{
	
	

	@Override
	public void init() throws ComponentInitException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Score computeScore(OWLPropertyAxiom hypothesis, double noise) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getAccuracy(OWLPropertyAxiom hypothesis, double noise) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getAccuracyOrTooWeak(OWLPropertyAxiom hypothesis, double noise) {
		// TODO Auto-generated method stub
		return 0;
	}


}
