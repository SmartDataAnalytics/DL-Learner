package org.dllearner.core;

import org.dllearner.learningproblems.AxiomScore;
import org.semanticweb.owlapi.model.OWLAxiom;

public interface AxiomScoreCalculator {

	AxiomScore calculateScore(OWLAxiom axiom);
}
