package org.dllearner.algorithm.tbsl.sparql;

import java.util.HashSet;
import java.util.Set;

public class WeightedQuery implements Comparable<WeightedQuery>{
	
	private double score;
	private Query query;
	
	private Set<Allocation> allocations = new HashSet<Allocation>();
	
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
	
	public void addAllocation(Allocation a){
		allocations.add(a);
	}
	
	public void addAllocations(Set<Allocation> allocations){
		this.allocations.addAll(allocations);
	}
	
	public Set<Allocation> getAllocations() {
		return allocations;
	}

	@Override
	public int compareTo(WeightedQuery o) {
		if(o.getScore() < this.score){
			return -1;
		} else if(o.getScore() > this.score){
			return 1;
		} else {
			int filter = Boolean.valueOf(query.getFilters().isEmpty()).compareTo(Boolean.valueOf(o.getQuery().getFilters().isEmpty()));
			if(filter == 0){
				return query.toString().compareTo(o.getQuery().toString());
			} else {
				return filter;
			}
		}
			
			
	}
	
	@Override
	public String toString() {
		return query.toString() + "\n(Score: " + score + ")";
	}
	
	public String explain(){
		String explanation = toString();
		explanation += "\n[";
		for(Allocation a : allocations){
			explanation += a.toString() + "\n";
		}
		explanation += "]";
		return explanation;
	}
	
	

}
