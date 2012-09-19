package org.dllearner.algorithm.tbsl.sparql;

public class RatedQuery extends Query implements Comparable<RatedQuery>{

	private float score;
	
	public RatedQuery(Query query, float score){
		super(query);
		this.score = score;
	}
	
	public RatedQuery(float score){
		this.score = score;
	}

	public float getScore() {
		return score;
	}

	public void setScore(float score) {
		this.score = score;
	}
	
//	@Override
//	public String toString() {
//		return super.toString() + "\nSCORE(" + score + ")";
//	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RatedQuery || obj == null){
			return false;
		}
		if(obj == this){
			return true;
		}
		RatedQuery other = (RatedQuery)obj;
		return super.equals(other) && this.score == other.score; 
	}
	
	@Override
	public int hashCode() {
		return super.hashCode() + Float.valueOf(score).hashCode();
	}

	@Override
	public int compareTo(RatedQuery o) {
		if(o.getScore() < this.score){
			return -1;
		} else if(o.getScore() > this.score){
			return 1;
		} else return this.toString().compareTo(o.toString());
	}	
}
