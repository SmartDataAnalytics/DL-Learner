package org.dllearner.core.owl;

import java.util.Map;

public class Equality extends TerminologicalAxiom {
	
	private Concept concept1;
	private Concept concept2;
	
	public Equality(Concept concept1, Concept concept2) {
		this.concept1 = concept1;
		this.concept2 = concept2;
	}

	public Concept getConcept1() {
		return concept1;
	}

	public Concept getConcept2() {
		return concept2;
	}

	public int getLength() {
		return 1 + concept1.getLength() + concept2.getLength();
	}
			
	public String toString(String baseURI, Map<String,String> prefixes) {
		return concept1.toString(baseURI, prefixes) + " = " + concept2.toString(baseURI, prefixes);
	}
}
