package org.dllearner.core.dl;

public class SubRoleAxiom implements RBoxAxiom {

	private AtomicRole role;
	private AtomicRole subRole;
	
	public SubRoleAxiom(AtomicRole subRole, AtomicRole role) {
		this.role = role;
		this.subRole = subRole;
	}
	
	public AtomicRole getRole() {
		return role;
	}

	public AtomicRole getSubRole() {
		return subRole;
	}

	public int getLength() {
		return 1 + role.getLength() + subRole.getLength();
	}
	
	@Override		
	public String toString() {
		return "Subrole(" + subRole + "," + role.toString() + ")";
	}		
}
