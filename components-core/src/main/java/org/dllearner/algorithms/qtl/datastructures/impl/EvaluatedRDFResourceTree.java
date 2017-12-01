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
package org.dllearner.algorithms.qtl.datastructures.impl;

import com.google.common.collect.ComparisonChain;

import java.util.TreeSet;

import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.Score;
import org.dllearner.learningproblems.QueryTreeScore;

import java.util.Collection;
import java.util.Set;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

public class EvaluatedRDFResourceTree implements Comparable<EvaluatedRDFResourceTree>{
	
	public int cnt = 0;
	
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
	
	// the query trees of which this query tree was generated from
	private Set<RDFResourceTree> baseQueryTrees = new TreeSet<>();

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

	/**
	 * @return the query tree as OWL class expression
	 */
	public EvaluatedDescription<? extends Score> getEvaluatedDescription() {
		return asEvaluatedDescription();
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(EvaluatedDescription<? extends Score> description) {
		this.description = description;
	}
	
	/**
	 * @return the query tree as OWL class expression with score
	 */
	public EvaluatedDescription<? extends Score> asEvaluatedDescription(){
		// lazy generation
		if(description == null){
			description = new EvaluatedDescription(QueryTreeUtils.toOWLClassExpression(getTree()), score);
		}
		return description;
	}
	
	@Override
	public int compareTo(EvaluatedRDFResourceTree other) {
		return ComparisonChain.start()
		         .compare(other.getScore(), this.getScore()) // score
		         .compare(this.baseQueryTrees.toString(), other.baseQueryTrees.toString()) // base query trees
		         .compare(this.asEvaluatedDescription(), other.asEvaluatedDescription()) // class expression representation
		         .result();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "QueryTree(Score:" + score + ")\n" + tree.getStringRepresentation();
	}
}