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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.core.owl.NamedClass;

/**
 * Represents an EL description tree. Unlike {@link ELDescriptionNode},
 * this is a tree-wide structure, i.e. it does not implement the tree
 * structure itself, but is used to store information about the tree.
 * 
 * @author Jens Lehmann
 *
 */
public class ELDescriptionTree implements Cloneable {

	// to simplify equivalence checks and minimisation, we
	// attach a simulation relation to the description tree
//	private Simulation simulation;	
	
	private int maxLevel = 1;
	
	private ELDescriptionNode rootNode;	
	
	private Map<Integer,Set<ELDescriptionNode>> levelNodeMapping = new HashMap<Integer,Set<ELDescriptionNode>>();
	
	public ELDescriptionTree() {
		
	}

	/**
	 * Gets the nodes on a specific level of the tree. 
	 * This information is cached here for performance
	 * reasons.
	 * @param level The level (distance from root node).
	 * @return The set of all nodes on the specified level within
	 * this tree.
	 */
	public Set<ELDescriptionNode> getNodesOnLevel(int level) {
		return levelNodeMapping.get(level);
	}

	/**
	 * Internal method for updating the level node mapping.
	 * It is called when a new node is added to the tree.
	 * @param node The new node.
	 * @param level Level of the new node.
	 */
	protected void addNodeToLevel(ELDescriptionNode node, int level) {
		if(level <= maxLevel) {
			 levelNodeMapping.get(level).add(node);
		} else if (level == maxLevel + 1) {
			Set<ELDescriptionNode> set = new HashSet<ELDescriptionNode>();
			set.add(node);
			levelNodeMapping.put(level, set);
			maxLevel++;
		} else {
			throw new RuntimeException("Inconsistent EL description tree structure.");
		}
	}
	
	/**
	 * @return the maxLevel
	 */
	public int getMaxLevel() {
		return maxLevel;
	}

	/**
	 * @return the rootNode
	 */
	public ELDescriptionNode getRootNode() {
		return rootNode;
	}
	
	@Override
	public ELDescriptionTree clone() {
		// create a new reference tree
		ELDescriptionTree treeClone = new ELDescriptionTree();
		// create a root node attached to this reference tree
		ELDescriptionNode rootNodeClone = new ELDescriptionNode(treeClone, new TreeSet<NamedClass>(rootNode.getLabel())); 
		cloneRecursively(rootNode, rootNodeClone);
		return treeClone;
	}
	
	// we read from the original structure and write to the new structure
	private void cloneRecursively(ELDescriptionNode node, ELDescriptionNode nodeClone) {
		// loop through all edges and clone the subtrees
		for(ELDescriptionEdge edge : node.getEdges()) {
			ELDescriptionNode tmp = new ELDescriptionNode(nodeClone, edge.getLabel(), new TreeSet<NamedClass>(edge.getTree().getLabel()));
			cloneRecursively(edge.getTree(), tmp);
		}		
	}
	
}
