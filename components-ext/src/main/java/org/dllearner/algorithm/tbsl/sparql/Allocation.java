package org.dllearner.algorithm.tbsl.sparql;


public class Allocation {
	
	private String uri;
	private int inDegree;
	
	private double similarity;
	private double prominence;
	
	private double score;
	
	public Allocation(String uri, int inDegree, double similarity) {
		this.uri = uri;
		this.inDegree = inDegree;
		this.similarity = similarity;
	}

	public String getUri() {
		return uri;
	}

	public int getInDegree() {
		return inDegree;
	}

	public double getSimilarity() {
		return similarity;
	}
	
	public double getProminence() {
		return prominence;
	}

	public void setProminence(double prominence) {
		this.prominence = prominence;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	@Override
	public String toString() {
		return uri + "(similarity: " + similarity + "; prominence: " + inDegree + ")";
	}
	
	
	
	

}
