package org.dllearner.core.dl;

import java.util.Map;

public class ConceptAssertion extends AssertionalAxiom {
	
	private Concept concept;
	private Individual individual;
	
	public ConceptAssertion(Concept concept, Individual individual) {
		this.concept = concept;
		this.individual = individual;
	}

	public Concept getConcept() {
		return concept;
	}

	public Individual getIndividual() {
		return individual;
	}

	public int getLength() {
		return 1 + concept.getLength();
	}
		
	public String toString(String baseURI, Map<String,String> prefixes) {
		return concept.toString(baseURI, prefixes) + "(" + individual + ")";
	}
}
