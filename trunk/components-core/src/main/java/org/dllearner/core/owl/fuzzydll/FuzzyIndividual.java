package org.dllearner.core.owl.fuzzydll;

import org.dllearner.core.owl.Individual;

public class FuzzyIndividual extends Individual{

	private double beliefDegree;
	
	public FuzzyIndividual(String name, double fuzzyDegree) {
		super(name);
		this.beliefDegree = fuzzyDegree;
	}

	public double getBeliefDegree() {
		return beliefDegree;
	}

	public void setBeliefDegree(double beliefDegree) {
		this.beliefDegree = beliefDegree;
	}
}
