package org.dllearner.core.owl;

import java.util.Map;

public class InverseObjectPropertyAxiom extends PropertyAxiom {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6402501966040365366L;
	private ObjectProperty inverseRole;
	private ObjectProperty role;
	
	public InverseObjectPropertyAxiom(ObjectProperty inverseRole, ObjectProperty role) {
		this.inverseRole = inverseRole;
		this.role = role;
	}

	public ObjectProperty getInverseRole() {
		return inverseRole;
	}

	public ObjectProperty getRole() {
		return role;
	}

	public int getLength() {
		return 1 + role.getLength() + inverseRole.getLength();
	}
	
	public String toString(String baseURI, Map<String,String> prefixes) {
		return "Inverse(" + inverseRole + "," + role.toString(baseURI, prefixes) + ")";
	}
	
	public String toKBSyntaxString(String baseURI, Map<String,String> prefixes) {
		return "Inverse(" + inverseRole.toKBSyntaxString(baseURI, prefixes) + "," + role.toKBSyntaxString(baseURI, prefixes) + ")";
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
		return "INVERSEOBJECTPROPERTYAXIOM NOT IMPLEMENTED";
	}	
}
