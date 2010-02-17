package org.dllearner.core.owl;

import java.util.Map;

public class SubObjectPropertyAxiom extends PropertyAxiom {

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
	
	@Override
	public void accept(AxiomVisitor visitor) {
		visitor.visit(this);
	}	
	
	public void accept(KBElementVisitor visitor) {
		visitor.visit(this);
	}	
}
