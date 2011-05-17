package org.dllearner.core.owl.fuzzydll;

import org.dllearner.core.owl.Individual;

public class FuzzyIndividual extends Individual{

	private double truthDegree;
	
	public FuzzyIndividual(String name, double fuzzyDegree) {
		super(name);
		this.truthDegree = fuzzyDegree;
	}

	public double getTruthDegree() {
		return truthDegree;
	}

	public void setTruthDegree(double beliefDegree) {
		this.truthDegree = beliefDegree;
	}
	
	public int compareTo(FuzzyIndividual o) {
		int d = Double.compare(truthDegree, o.getTruthDegree());
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
