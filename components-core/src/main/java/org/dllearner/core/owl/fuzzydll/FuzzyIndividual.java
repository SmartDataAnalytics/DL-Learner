package org.dllearner.core.owl.fuzzydll;

import org.semanticweb.owlapi.model.IRI;

import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

public class FuzzyIndividual extends OWLNamedIndividualImpl{

	private double truthDegree;
	
	public FuzzyIndividual(String name, double fuzzyDegree) {
		super(IRI.create(name));
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
		if (this == o) return true;
		if (!(o instanceof FuzzyIndividual)) return false;
		if (!super.equals(o)) return false;

		FuzzyIndividual that = (FuzzyIndividual) o;

		return Double.compare(that.truthDegree, truthDegree) == 0;

	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(truthDegree);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
}
