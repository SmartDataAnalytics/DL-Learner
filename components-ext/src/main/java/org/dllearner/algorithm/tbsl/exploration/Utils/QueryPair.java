package org.dllearner.algorithm.tbsl.exploration.Utils;

public class QueryPair {
	private String Query;
	private double rank;
	public String getQuery() {
		return Query;
	}
	public void setQuery(String query) {
		Query = query;
	}
	public double getRank() {
		return rank;
	}
	public void setRank(double rank) {
		this.rank = rank;
	}
	
	public void printAll(){
		System.out.println("Query :"+this.getQuery());
		System.out.println("Rank :"+this.getRank());
	}
	
	public QueryPair(String query_new, double rank_new){
		this.setQuery(query_new);
		this.setRank(rank_new);
	}
	
	
}
