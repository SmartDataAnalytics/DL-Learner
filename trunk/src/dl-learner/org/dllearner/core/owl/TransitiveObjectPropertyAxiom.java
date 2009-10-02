package org.dllearner.core.owl;

import java.util.Map;

public class TransitiveObjectPropertyAxiom extends PropertyAxiom {

	private ObjectPropertyExpression role;
	
	public TransitiveObjectPropertyAxiom(ObjectPropertyExpression role) {
		this.role = role;
	}

	public int getLength() {
		return 1 + role.getLength();
	}

	public ObjectPropertyExpression getRole() {
		return role;
	}
	
	public String toString(String baseURI, Map<String,String> prefixes) {
		return "Transitive(" + role.toString(baseURI, prefixes) + ")";
	}
	
	public String toKBSyntaxString(String baseURI, Map<String,String> prefixes) {
		return "Transitive(" + role.toKBSyntaxString(baseURI, prefixes) + ")";
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
		return "Transitive(" + role.toString(baseURI, prefixes) + ")";
	}	
}
