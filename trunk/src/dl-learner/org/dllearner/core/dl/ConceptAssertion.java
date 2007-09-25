package org.dllearner.core.dl;

public class ConceptAssertion implements AssertionalAxiom {
	
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
	
	@Override		
	public String toString() {
		return concept.toString() + "(" + individual + ")";
	}
}
