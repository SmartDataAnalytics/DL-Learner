package org.dllearner.core.dl;

import java.util.Map;

public class Inclusion extends TerminologicalAxiom {
	
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
	
	public String toString(String baseURI, Map<String,String> prefixes) {
		return subConcept.toString(baseURI, prefixes) + " SUBCONCEPTOF " + superConcept.toString(baseURI, prefixes);
	}
}
