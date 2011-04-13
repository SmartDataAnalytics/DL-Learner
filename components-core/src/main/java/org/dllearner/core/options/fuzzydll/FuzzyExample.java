package org.dllearner.core.options.fuzzydll;

public class FuzzyExample implements Comparable<FuzzyExample> {
	private String exampleName;
	private double fuzzyDegree;
	
	public FuzzyExample(String i, double d) {
		this.exampleName = i;
		this.fuzzyDegree = d;
	}

	@Override
	public int compareTo(FuzzyExample fe) {
		return this.getExampleName().compareTo(fe.getExampleName());
	}

	public String getExampleName() {
		return exampleName;
	}

	public void setExampleName(String individual) {
		this.exampleName = individual;
	}

	public double getFuzzyDegree() {
		return fuzzyDegree;
	}

	public void setFuzzyDegree(double fuzzyDegree) {
		this.fuzzyDegree = fuzzyDegree;
	}
}
