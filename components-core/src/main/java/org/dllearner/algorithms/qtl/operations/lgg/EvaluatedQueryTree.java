/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.algorithms.qtl.operations.lgg;

import com.google.common.collect.ComparisonChain;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.LiteralNodeConversionStrategy;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.learningproblems.QueryTreeScore;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class EvaluatedQueryTree<N> implements Comparable<EvaluatedQueryTree<N>>{
	
	public int cnt = 0;
	
	private TIntSet parentIDs = new TIntHashSet();
	
	// internal identifier
	private final int id;
	
	// the underlying query tree
	private QueryTree<N> tree;
	
	// the positive example trees that are not covered
	private Collection<QueryTree<N>> falseNegatives;
	
	// the negative example trees that are covered
	private Collection<QueryTree<N>> falsePositives;
	
	// the tree score
	private QueryTreeScore score;
//	private ScoreTwoValued score;
	
	// the corresponding description set lazily
	private EvaluatedDescription description;
	
	// the query trees of which the underlying query tree was generated from
	private Set<QueryTree<N>> baseQueryTrees = new HashSet<>();

	public EvaluatedQueryTree(QueryTree<N> tree, Collection<QueryTree<N>> falseNegatives, 
			Collection<QueryTree<N>> falsePositives, QueryTreeScore score) {
		this.tree = tree;
		this.falseNegatives = falseNegatives;
		this.falsePositives = falsePositives;
		this.score = score;
		this.id = cnt++;
	}
	
	public EvaluatedQueryTree(int id, QueryTree<N> tree, Collection<QueryTree<N>> falseNegatives, 
			Collection<QueryTree<N>> falsePositives, QueryTreeScore score) {
		this.id = id;
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
	
	public void setBaseQueryTrees(Set<QueryTree<N>> baseQueryTrees) {
		this.baseQueryTrees = baseQueryTrees;
	}
	
	/**
	 * @return the baseQueryTrees
	 */
	public Set<QueryTree<N>> getBaseQueryTrees() {
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
	public QueryTree<N> getTree() {
		return tree;
	}
	
	/**
	 * @return the positive examples that are not covered by the query tree
	 */
	public Collection<QueryTree<N>> getFalseNegatives() {
		return falseNegatives;
	}
	
	/**
	 * @return the negative examples that are covered by the query tree
	 */
	public Collection<QueryTree<N>> getFalsePositives() {
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
	public int compareTo(EvaluatedQueryTree<N> other) {
		return ComparisonChain.start()
//		         .compare(this.getScore(), other.getScore())
		         .compare(other.getScore(), this.getScore())
		         .compare(this.asEvaluatedDescription(), other.asEvaluatedDescription())
		         .result();
	}
	
	
	/**
	 * @return the query tree as OWL class expression
	 */
	public EvaluatedDescription getEvaluatedDescription() {
		return asEvaluatedDescription();
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(EvaluatedDescription description) {
		this.description = description;
	}
	
	public EvaluatedDescription asEvaluatedDescription(){
		if(description == null){
			description = new EvaluatedDescription(getTree().asOWLClassExpression(LiteralNodeConversionStrategy.MIN_MAX), score);
		}
		return description;
	}
	
	public EvaluatedDescription asEvaluatedDescription(LiteralNodeConversionStrategy strategy){
		return new EvaluatedDescription(getTree().asOWLClassExpression(strategy), score);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "QueryTree(Score:" + score + ")\n" + tree.getStringRepresentation();
	}
}