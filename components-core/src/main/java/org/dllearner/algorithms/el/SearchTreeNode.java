/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

/**
 * A node in the search tree of an EL algorithm.
 * 
 * @author Jens Lehmann
 *
 */
public class SearchTreeNode {

	private ELDescriptionTree descriptionTree;
	
	private List<SearchTreeNode> children = new LinkedList<SearchTreeNode>();
	
	private int coveredNegatives;
	private boolean tooWeak = false;
	
	private double score;
	
	public SearchTreeNode(ELDescriptionTree descriptionTree) {
		this.descriptionTree = descriptionTree;
	}

	/**
	 * @return the tooWeak
	 */
	public boolean isTooWeak() {
		return tooWeak;
	}

	/**
	 * @param tooWeak the tooWeak to set
	 */
	public void setTooWeak() {
		tooWeak = true;
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
		String ret = descriptionTree.toDescriptionString() + " [q:";
		if(tooWeak)
			ret += "tw";
		else
			ret += coveredNegatives;
		ret += ", children:" + children.size() + "]";
		ret += " score: " + score;
		return ret;
	}
	
	public String getTreeString() {
		return getTreeString(0).toString();
	}
	
	private StringBuilder getTreeString(int depth) {
		StringBuilder treeString = new StringBuilder();
		for(int i=0; i<depth-1; i++)
			treeString.append("  ");
		if(depth!=0)
			treeString.append("|--> ");
		treeString.append(toString()+"\n");
		for(SearchTreeNode child : children) {
			treeString.append(child.getTreeString(depth+1));
		}
		return treeString;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}	
	
}
