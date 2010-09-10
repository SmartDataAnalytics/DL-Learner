package org.dllearner.core.owl;

import java.util.Map;

public class FunctionalObjectPropertyAxiom extends PropertyAxiom {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2571916949143387591L;
	private ObjectProperty role;
	
	public FunctionalObjectPropertyAxiom(ObjectProperty role) {
		this.role = role;
	}

	public ObjectPropertyExpression getRole() {
		return role;
	}

	public int getLength() {
		return 1 + role.getLength();
	}
		
	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toString(java.lang.String, java.util.Map)
	 */
	public String toString(String baseURI, Map<String,String> prefixes) {
		return "Functional(" + role.toString(baseURI, prefixes) + ")";
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toKBSyntaxString(java.lang.String, java.util.Map)
	 */
	public String toKBSyntaxString(String baseURI, Map<String,String> prefixes) {
		return "Functional(" + role.toKBSyntaxString(baseURI, prefixes) + ")";
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
		return "FUNCTIONALOBJECTPROPERTYAXIOM NOT IMPLEMENTED";
	}	
}
