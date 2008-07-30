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

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.owl.NamedClass;

/**
 * Represents an EL description tree, which corresponds to a
 * description in the EL description logic. Note that an EL description tree
 * can be a subtree of another EL description tree. In general,
 * an EL description tree is a tree where the node label is a set
 * of named classes and the edges are labelled with a property.
 * 
 * In the documentation below "this node" refers to the root node
 * of this EL description (sub-)tree.
 * 
 * @author Jens Lehmann
 *
 */
public class ELDescriptionTree {

	private SortedSet<NamedClass> label;
	
	private List<Edge> edges;

	// parent node in the tree;
	// null indicates that this node is a root node
	private ELDescriptionTree parent = null;
	
	/**
	 * Constructs an empty EL description tree with the empty set
	 * as root label and an empty set of outgoing edges.
	 */
	public ELDescriptionTree() {
		this(new TreeSet<NamedClass>(), new LinkedList<Edge>());
	}
	
	/**
	 * Constructs an EL description tree given its root label.
	 * @param label Label of the root node.
	 */
	public ELDescriptionTree(SortedSet<NamedClass> label) {
		this(label, new LinkedList<Edge>());
	}
	
	/**
	 * Constructs an EL description tree given its root label and edges.
	 * @param label Label of the root node.
	 * @param edges Edges connected to the root node.
	 */
	public ELDescriptionTree(SortedSet<NamedClass> label, List<Edge> edges) {
		this.label = label;
		this.edges = edges;
	}
	
	/**
	 * Checks whether this node has a parent. If the parent link
	 * is null, the node is considered to be a root node.
	 * @return True of this is the root node and false otherwise.
	 */
	public boolean isRoot() {
		return parent == null;
	}
	
	/**
	 * Traverses the EL description tree upwards until it finds 
	 * the root and returns it.
	 * @return The root node of this EL description tree.
	 */
	public ELDescriptionTree getRoot() {
		ELDescriptionTree root = this;
		while(root.parent != null) {
			root = parent;
		}
		return root;
	}
	
	/**
	 * Traverses the tree until the root node and counts how
	 * many edges are traversed. If this node does not have a parent,
	 * zero is returned.
	 * @return The level of this node (or more specifically the root
	 * node of this subtree) within the overall EL description tree.
	 */
	public int getLevel() {
		ELDescriptionTree root = this;
		int level = 0;
		while(root.parent != null) {
			root = parent;
			level++;
		}
		return level;		
	}
	
	/**
	 * @return The label of root node of this subtree.
	 */
	public SortedSet<NamedClass> getLabel() {
		return label;
	}

	/**
	 * @return The outgoing edges of this subtree.
	 */
	public List<Edge> getEdges() {
		return edges;
	}
	
}
