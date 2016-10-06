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
package org.dllearner.algorithms.el;

import java.util.LinkedList;
import java.util.List;

import org.dllearner.core.Score;
import org.semanticweb.owlapi.io.OWLObjectRenderer;

/**
 * A node in the search tree of an EL algorithm.
 * 
 * @author Jens Lehmann
 *
 */
public class SearchTreeNode {

	private ELDescriptionTree descriptionTree;
	
	private List<SearchTreeNode> children = new LinkedList<>();
	
	private int coveredPositives;
	private int coveredNegatives;
	private boolean tooWeak = false;
	
	private Score score;
	protected double accuracy;
	
	public SearchTreeNode(ELDescriptionTree descriptionTree) {
		this.descriptionTree = descriptionTree;
	}

	/**
	 * @return whether the node is too weak
	 */
	public boolean isTooWeak() {
		return tooWeak;
	}

	/**
	 * set if the node is too weak
	 */
	public void setTooWeak() {
		tooWeak = true;
	}
	
	/**
	 * @param coveredPositives the coveredPositives to set
	 */
	public void setCoveredPositives(int coveredPositives) {
		this.coveredPositives = coveredPositives;
	}
	
	/**
	 * @return the coveredPositives
	 */
	public int getCoveredPositives() {
		return coveredPositives;
	}

	/**
	 * @param coveredNegatives the coveredNegatives to set
	 */
	public void setCoveredNegatives(int coveredNegatives) {
		this.coveredNegatives = coveredNegatives;
		tooWeak = false;
	}

	/**
	 * @return the coveredNegatives
	 */
	public int getCoveredNegatives() {
		return coveredNegatives;
	}

	public void addChild(SearchTreeNode node) {
		children.add(node);
	}
	
	/**
	 * @return the descriptionTree
	 */
	public ELDescriptionTree getDescriptionTree() {
		return descriptionTree;
	}

	/**
	 * @return the children
	 */
	public List<SearchTreeNode> getChildren() {
		return children;
	}
	
	@Override		
	public String toString() {
		String ret = descriptionTree.transformToClassExpression().toString() + " [q:";
		if(tooWeak)
			ret += "tw";
		else
			ret += coveredNegatives;
		ret += ", children:" + children.size() + "]";
		ret += " score: " + score;
		return ret;
	}
	
	public String toString(OWLObjectRenderer renderer) {
		String ret = renderer.render(descriptionTree.transformToClassExpression()) + " [q:";
		if(tooWeak)
			ret += "tw";
		else
			ret += coveredNegatives;
		ret += ", children:" + children.size() + "]";
		ret += " score: " + score;
		return ret;
	}
	
	public String getTreeString(OWLObjectRenderer renderer) {
		return getTreeString(0, renderer).toString();
	}
	
	private StringBuilder getTreeString(int depth, OWLObjectRenderer renderer) {
		StringBuilder treeString = new StringBuilder();
		for(int i=0; i<depth-1; i++)
			treeString.append("  ");
		if(depth!=0)
			treeString.append("|--> ");
		treeString.append(toString(renderer)).append("\n");
		for(SearchTreeNode child : children) {
			treeString.append(child.getTreeString(depth+1, renderer));
		}
		return treeString;
	}

	/**
	 * @return the score of the node
	 */
	public Score getScore() {
		return score;
	}

	/**
	 * @param score the score of the node to set
	 */
	public void setScore(Score score) {
		this.score = score;
	}	
	
	/**
	 * @return the accuracy
	 */
	public double getAccuracy() {
		return accuracy;
	}
	
	/**
	 * @param accuracy the accuracy to set
	 */
	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}
	
}
