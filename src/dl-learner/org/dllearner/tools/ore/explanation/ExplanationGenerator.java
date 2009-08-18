package org.dllearner.tools.ore.explanation;

import java.util.Set;

import org.semanticweb.owl.model.OWLAxiom;

public interface ExplanationGenerator {
	public abstract Set<OWLAxiom> getExplanation(OWLAxiom enatilment);
	public abstract Set<Set<OWLAxiom>> getExplanations(OWLAxiom entailment);
	public abstract Set<Set<OWLAxiom>> getExplanations(OWLAxiom entailment, int limit);
	
}
