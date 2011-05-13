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
	
	public int compareTo(FuzzyIndividual o) {
		int d = Double.compare(beliefDegree, o.getBeliefDegree());
		if (d == 0)
			return super.compareTo(o);
		else
			return d;
	}
    
	@Override
	public boolean equals(Object o) {
		if(o==null) {
			return false;
		}
		return (compareTo((FuzzyIndividual)o)==0);
	}
}
