/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
 *
 */
package org.dllearner.algorithms.el;

import java.util.List;

/**
 * A node in the search tree of an EL algorithm.
 * 
 * @author Jens Lehmann
 *
 */
public class SearchTreeNode {

	private ELDescriptionTree descriptionTree;
	
	private List<SearchTreeNode> children;
	
	private int coveredNegatives;
	private boolean tooWeak = false;
	
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
	
}
