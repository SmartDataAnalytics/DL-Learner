package org.dllearner.algorithms.qtl.datastructures.impl;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.Score;
import org.dllearner.learningproblems.QueryTreeScore;

import com.google.common.collect.ComparisonChain;

public class EvaluatedRDFResourceTree implements Comparable<EvaluatedRDFResourceTree>{
	
	public static int cnt = 0;
	
	private TIntSet parentIDs = new TIntHashSet();
	
	// internal identifier
	private final int id;
	
	// the underlying query tree
	private RDFResourceTree tree;
	
	// the positive example trees that are not covered
	private Collection<RDFResourceTree> falseNegatives;
	
	// the negative example trees that are covered
	private Collection<RDFResourceTree> falsePositives;
	
	// the tree score
	private QueryTreeScore score;
//	private ScoreTwoValued score;
	
	// the corresponding description set lazily
	private EvaluatedDescription<? extends Score> description;
	
	// the query trees of which the underlying query tree was generated from
	private Set<RDFResourceTree> baseQueryTrees = new HashSet<>();

	public EvaluatedRDFResourceTree(RDFResourceTree tree, Collection<RDFResourceTree> falseNegatives, 
			Collection<RDFResourceTree> falsePositives, QueryTreeScore score) {
		this.tree = tree;
		this.falseNegatives = falseNegatives;
		this.falsePositives = falsePositives;
		this.score = score;
		this.id = cnt++;
	}
	
	public EvaluatedRDFResourceTree(int id, RDFResourceTree tree, Collection<RDFResourceTree> falseNegatives, 
			Collection<RDFResourceTree> falsePositives, QueryTreeScore score) {
		this.id = id;
		this.tree = tree;
		this.falseNegatives = falseNegatives;
		this.falsePositives = falsePositives;
		this.score = score;
	}
	
//	
//	public EvaluatedQueryTree(RDFResourceTree tree, ScoreTwoValued score) {
//		this.tree = tree;
//		this.score = score;
//	}
	
	public void setBaseQueryTrees(Set<RDFResourceTree> baseQueryTrees) {
		this.baseQueryTrees = baseQueryTrees;
	}
	
	/**
	 * @return the baseQueryTrees
	 */
	public Set<RDFResourceTree> getBaseQueryTrees() {
		return baseQueryTrees;
	}
	
	/**
	 * @return an internal identifier which is assumed to be unique during 
	 * the complete algorithm run
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return the parentIDs
	 */
	public TIntSet getParentIDs() {
		return parentIDs;
	}
	
	/**
	 * @return the underlying query tree
	 */
	public RDFResourceTree getTree() {
		return tree;
	}
	
	/**
	 * @return the positive examples that are not covered by the query tree
	 */
	public Collection<RDFResourceTree> getFalseNegatives() {
		return falseNegatives;
	}
	
	/**
	 * @return the negative examples that are covered by the query tree
	 */
	public Collection<RDFResourceTree> getFalsePositives() {
		return falsePositives;
	}
	
	/**
	 * @return the score of the query tree
	 */
	public double getScore() {
		return score.getScore();
	}
	
	/**
	 * @return the score of the query tree
	 */
	public QueryTreeScore getTreeScore() {
		return score;
	}

	@Override
	public int compareTo(EvaluatedRDFResourceTree other) {
		return ComparisonChain.start()
//		         .compare(this.getScore(), other.getScore())
		         .compare(other.getScore(), this.getScore())
		         .compare(this.asEvaluatedDescription(), other.asEvaluatedDescription())
		         .result();
	}
	
	
	/**
	 * @return the query tree as OWL class expression
	 */
	public EvaluatedDescription<? extends Score> getEvaluatedDescription() {
		return asEvaluatedDescription();
	}
	/**
	 * @param OWLClassExpression the OWLClassExpression to set
	 */
	public void setDescription(EvaluatedDescription<? extends Score> description) {
		this.description = description;
	}
	
	public EvaluatedDescription<? extends Score> asEvaluatedDescription(){
		if(description == null){
			description = new EvaluatedDescription(QueryTreeUtils.toOWLClassExpression(getTree()), score);
		}
		return description;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "QueryTree(Score:" + score + ")\n" + tree.getStringRepresentation();
	}
}