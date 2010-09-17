package org.dllearner.core.owl;

import java.util.Map;

public class SubObjectPropertyAxiom extends PropertyAxiom {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1085651734702155330L;
	private ObjectProperty role;
	private ObjectProperty subRole;
	
	public SubObjectPropertyAxiom(ObjectProperty subRole, ObjectProperty role) {
		this.role = role;
		this.subRole = subRole;
	}
	
	public ObjectProperty getRole() {
		return role;
	}

	public ObjectProperty getSubRole() {
		return subRole;
	}

	public int getLength() {
		return 1 + role.getLength() + subRole.getLength();
	}
		
	public String toString(String baseURI, Map<String,String> prefixes) {
		return "Subrole(" + subRole.toString(baseURI, prefixes) + "," + role.toString(baseURI, prefixes) + ")";
	}
	
	public String toKBSyntaxString(String baseURI, Map<String,String> prefixes) {
		return "Subrole(" + subRole.toKBSyntaxString(baseURI, prefixes) + "," + role.toKBSyntaxString(baseURI, prefixes) + ")";
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
		return "Subrole(" + subRole.toString(baseURI, prefixes) + "," + role.toString(baseURI, prefixes) + ")";
	}	
}
