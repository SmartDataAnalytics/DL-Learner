package org.dllearner.core.dl;

public class InverseRoleAxiom implements RBoxAxiom {

	private AtomicRole inverseRole;
	private AtomicRole role;
	
	public InverseRoleAxiom(AtomicRole inverseRole, AtomicRole role) {
		this.inverseRole = inverseRole;
		this.role = role;
	}

	public AtomicRole getInverseRole() {
		return inverseRole;
	}

	public AtomicRole getRole() {
		return role;
	}

	public int getLength() {
		return 1 + role.getLength() + inverseRole.getLength();
	}
	
	@Override		
	public String toString() {
		return "Inverse(" + inverseRole + "," + role.toString() + ")";
	}	
}
