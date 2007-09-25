package org.dllearner.core.dl;

public class Inclusion implements TerminologicalAxiom {
	
	private Concept subConcept;
	private Concept superConcept;
	
	public Inclusion(Concept subConcept, Concept superConcept) {
		this.subConcept = subConcept;
		this.superConcept = superConcept;
	}

	public Concept getSubConcept() {
		return subConcept;
	}

	public Concept getSuperConcept() {
		return superConcept;
	}

	public int getLength() {
		return 1 + subConcept.getLength() + superConcept.getLength();
	}
	
	@Override		
	public String toString() {
		return subConcept.toString() + " SUBCONCEPTOF " + superConcept.toString();
	}
}
