package org.dllearner.core.owl;

import java.util.Map;

public class EquivalentClassesAxiom extends TerminologicalAxiom {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2893732406014114441L;
	private Description concept1;
	private Description concept2;
	
	public EquivalentClassesAxiom(Description concept1, Description concept2) {
		this.concept1 = concept1;
		this.concept2 = concept2;
	}

	public Description getConcept1() {
		return concept1;
	}

	public Description getConcept2() {
		return concept2;
	}

	public int getLength() {
		return 1 + concept1.getLength() + concept2.getLength();
	}
			
	public String toString(String baseURI, Map<String,String> prefixes) {
		return concept1.toString(baseURI, prefixes) + " = " + concept2.toString(baseURI, prefixes);
	}
	
	public String toKBSyntaxString(String baseURI, Map<String,String> prefixes) {
		return concept1.toKBSyntaxString(baseURI, prefixes) + " = " + concept2.toKBSyntaxString(baseURI, prefixes);
	}

	@Override
	public void accept(AxiomVisitor visitor) {
		visitor.visit(this);
	}
	
	public void accept(KBElementVisitor visitor) {
		visitor.visit(this);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toManchesterSyntaxString(java.lang.String, java.util.Map)
	 */
	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String, String> prefixes) {
		return "EQ_CLASSES_AXIOM NOT IMPLEMENTED";
	}	
}
