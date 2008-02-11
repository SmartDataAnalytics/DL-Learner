package org.dllearner.core.dl;

import java.util.Map;

public class SubRoleAxiom extends RBoxAxiom {

	private ObjectProperty role;
	private ObjectProperty subRole;
	
	public SubRoleAxiom(ObjectProperty subRole, ObjectProperty role) {
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
}
