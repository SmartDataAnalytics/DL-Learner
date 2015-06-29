package org.dllearner.learningproblems;

import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.EvaluatedAxiom;
import org.semanticweb.owlapi.model.OWLPropertyAxiom;

@ComponentAnn(name = "PropertyAxiomLearningProblem", shortName = "palp", version = 0.6)
public abstract class PropertyAxiomLearningProblem<T extends OWLPropertyAxiom> extends AbstractLearningProblem<AxiomScore, T, EvaluatedAxiom<T>>{
	

}
