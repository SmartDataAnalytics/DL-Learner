package org.dllearner.algorithms.qtl.operations.lgg;

import java.util.Collection;

import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.LiteralNodeConversionStrategy;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.learningproblems.QueryTreeScore;
import org.dllearner.utilities.owl.DLLearnerDescriptionConvertVisitor;

public class EvaluatedQueryTree<N> implements Comparable<EvaluatedQueryTree<N>>{
	
	private QueryTree<N> tree;
	private Collection<QueryTree<N>> falseNegatives;
	private Collection<QueryTree<N>> falsePositives;
	private QueryTreeScore score;
//	private ScoreTwoValued score;

	public EvaluatedQueryTree(QueryTree<N> tree, Collection<QueryTree<N>> falseNegatives, 
			Collection<QueryTree<N>> falsePositives, QueryTreeScore score) {
		this.tree = tree;
		this.falseNegatives = falseNegatives;
		this.falsePositives = falsePositives;
		this.score = score;
	}
//	
//	public EvaluatedQueryTree(QueryTree<N> tree, ScoreTwoValued score) {
//		this.tree = tree;
//		this.score = score;
//	}
	
	public QueryTree<N> getTree() {
		return tree;
	}
	
	/**
	 * @return the falseNegatives
	 */
	public Collection<QueryTree<N>> getFalseNegatives() {
		return falseNegatives;
	}
	
	/**
	 * @return the falsePositives
	 */
	public Collection<QueryTree<N>> getFalsePositives() {
		return falsePositives;
	}
	
	public double getScore() {
		return score.getScore();
	}
	
	public QueryTreeScore getTreeScore() {
		return score;
	}

	@Override
	public int compareTo(EvaluatedQueryTree<N> other) {
		double diff = getScore() - other.getScore();
		if(diff == 0){
			return -1;
		} else if(diff > 0){
			return -1;
		} else {
			return 1;
		}
	}
	
	public EvaluatedDescription asEvaluatedDescription(){
		return new EvaluatedDescription(DLLearnerDescriptionConvertVisitor.getDLLearnerDescription(
				getTree().asOWLClassExpression(LiteralNodeConversionStrategy.FACET_RESTRICTION)), score);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "QueryTree(Score:" + score + ")\n" + tree.getStringRepresentation();
	}
}