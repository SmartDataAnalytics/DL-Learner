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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(score);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Allocation other = (Allocation) obj;
		if (Double.doubleToLongBits(score) != Double.doubleToLongBits(other.score))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}
	
	
	
	

}
