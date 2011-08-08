package org.dllearner.core.owl;

import java.util.Map;

public class DisjointObjectPropertyAxiom extends PropertyAxiom {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1085651734702155330L;
	private ObjectProperty role;
	private ObjectProperty disjointRole;
	
	public DisjointObjectPropertyAxiom(ObjectProperty role, ObjectProperty disjointRole) {
		this.role = role;
		this.disjointRole = disjointRole;
	}
	
	public ObjectProperty getRole() {
		return role;
	}

	public ObjectProperty getDisjointRole() {
		return disjointRole;
	}

	public int getLength() {
		return 1 + role.getLength() + disjointRole.getLength();
	}
		
	public String toString(String baseURI, Map<String,String> prefixes) {
		return "DisjointObjectProperties(" + role.toString(baseURI, prefixes) + "," + disjointRole.toString(baseURI, prefixes) + ")";
	}
	
	public String toKBSyntaxString(String baseURI, Map<String,String> prefixes) {
		return "DisjointObjectProperties(" + role.toKBSyntaxString(baseURI, prefixes) + "," + disjointRole.toKBSyntaxString(baseURI, prefixes) + ")";
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
		return role.toString(baseURI, prefixes) + " DisjointWith: " + disjointRole.toString(baseURI, prefixes);
	}	
}
