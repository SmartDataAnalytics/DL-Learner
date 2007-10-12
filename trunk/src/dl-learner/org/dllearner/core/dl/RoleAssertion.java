package org.dllearner.core.dl;

import java.util.Map;

public class RoleAssertion extends AssertionalAxiom {

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

	public String toString(String baseURI, Map<String,String> prefixes) {
		return role.toString(baseURI, prefixes) + "(" + individual1.toString(baseURI, prefixes) + "," + individual2.toString(baseURI, prefixes) +")";
	}
}
