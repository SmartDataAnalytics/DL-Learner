package org.dllearner.tools.ore.explanation;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.model.OWLAxiom;

public class HittingSet extends HashSet<OWLAxiom> {

	public HittingSet(Set<OWLAxiom> axioms){
		addAll(axioms);
	}
	
	public HittingSet(OWLAxiom axiom){
		add(axiom);
	}
}
