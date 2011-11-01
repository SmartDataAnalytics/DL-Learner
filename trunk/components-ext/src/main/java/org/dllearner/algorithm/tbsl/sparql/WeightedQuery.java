package org.dllearner.algorithm.tbsl.sparql;

public class WeightedQuery implements Comparable<WeightedQuery>{
	
	private double score;
	private Query query;
	
	public WeightedQuery(Query query, double score) {
		super();
		this.score = score;
		this.query = query;
	}
	
	public WeightedQuery(Query query) {
		this(query, 0);
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public Query getQuery() {
		return query;
	}

	@Override
	public int compareTo(WeightedQuery o) {
		if(o.getScore() < this.score){
			return -1;
		} else if(o.getScore() > this.score){
			return 1;
		} else return query.toString().compareTo(o.getQuery().toString());
	}
	
	@Override
	public String toString() {
		return query.toString() + "\n(Score: " + score + ")";
	}
	
	

}
