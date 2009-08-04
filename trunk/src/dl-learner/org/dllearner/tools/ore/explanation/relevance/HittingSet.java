package org.dllearner.tools.ore.explanation.relevance;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.model.OWLAxiom;

public class HittingSet extends HashSet<OWLAxiom> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7704909550386943944L;

	public HittingSet(Set<OWLAxiom> axioms){
		addAll(axioms);
	}
	
	public HittingSet(OWLAxiom axiom){
		add(axiom);
	}
	
	public HittingSet(){
		
	}
}
