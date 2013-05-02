package org.dllearner.algorithms.qtl.operations.lgg;

import java.util.Collection;

import org.dllearner.algorithms.qtl.datastructures.QueryTree;

public class EvaluatedQueryTree<N> implements Comparable<EvaluatedQueryTree<N>>{
	
	private QueryTree<N> tree;
	private Collection<QueryTree<N>> uncoveredExamples;
	private double score;

	public EvaluatedQueryTree(QueryTree<N> tree, Collection<QueryTree<N>> uncoveredExamples, double score) {
		this.tree = tree;
		this.uncoveredExamples = uncoveredExamples;
		this.score = score;
	}
	
	public QueryTree<N> getTree() {
		return tree;
	}
	
	public Collection<QueryTree<N>> getUncoveredExamples() {
		return uncoveredExamples;
	}
	
	public double getScore() {
		return score;
	}

	@Override
	public int compareTo(EvaluatedQueryTree<N> other) {
		double diff = score - other.getScore();
		if(diff == 0){
			return -1;
		} else if(diff > 0){
			return -1;
		} else {
			return 1;
		}
	}
}