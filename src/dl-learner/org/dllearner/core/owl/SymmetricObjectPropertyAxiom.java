package org.dllearner.core.owl;

import java.util.Map;

public class SymmetricObjectPropertyAxiom extends PropertyAxiom {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8698680348695324368L;
	private ObjectProperty role;
	
	public SymmetricObjectPropertyAxiom(ObjectProperty role) {
		this.role = role;
	}

	public ObjectProperty getRole() {
		return role;
	}

	public int getLength() {
		return 1 + role.getLength();
	}
	
	public String toString(String baseURI, Map<String,String> prefixes) {
		return "Symmetric(" + role.toString(baseURI, prefixes) + ")";
	}
	
	public String toKBSyntaxString(String baseURI, Map<String,String> prefixes) {
		return "Symmetric(" + role.toKBSyntaxString(baseURI, prefixes) + ")";
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
		return "Symmetric(" + role.toString(baseURI, prefixes) + ")";
	}	
}
