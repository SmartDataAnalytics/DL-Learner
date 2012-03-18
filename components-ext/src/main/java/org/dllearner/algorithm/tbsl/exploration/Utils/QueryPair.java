package org.dllearner.algorithm.tbsl.exploration.Utils;

public class QueryPair {
	private String Query;
	private float rank;
	public String getQuery() {
		return Query;
	}
	public void setQuery(String query) {
		Query = query;
	}
	public float getRank() {
		return rank;
	}
	public void setRank(float rank) {
		this.rank = rank;
	}
	
	public void printAll(){
		System.out.println("Query :"+this.getQuery());
		System.out.println("Rank :"+this.getRank());
	}
	
	public QueryPair(String query_new, float rank_new){
		this.setQuery(query_new);
		this.setRank(rank_new);
	}
	
	
}
