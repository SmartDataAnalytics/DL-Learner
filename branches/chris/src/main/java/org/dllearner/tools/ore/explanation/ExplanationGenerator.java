package org.dllearner.tools.ore.explanation;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;

public interface ExplanationGenerator {
	public abstract Explanation getExplanation(OWLAxiom enatilment);
	public abstract Set<Explanation> getExplanations(OWLAxiom entailment);
	public abstract Set<Explanation> getExplanations(OWLAxiom entailment, int limit);
	
}
