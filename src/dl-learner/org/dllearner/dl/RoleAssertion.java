package org.dllearner.dl;

public class RoleAssertion implements AssertionalAxiom {

	private AtomicRole role;
	private Individual individual1;
	private Individual individual2;
	
	public RoleAssertion(AtomicRole role, Individual individual1, Individual individual2) {
		this.role = role;
		this.individual1 = individual1;
		this.individual2 = individual2;
	}
	
	public Individual getIndividual1() {
		return individual1;
	}

	public Individual getIndividual2() {
		return individual2;
	}

	public AtomicRole getRole() {
		return role;
	}

	public int getLength() {
		return 2 + role.getLength();
	}

	public String toString() {
		return role.toString() + "(" + individual1 + "," + individual2 +")";
	}
}
