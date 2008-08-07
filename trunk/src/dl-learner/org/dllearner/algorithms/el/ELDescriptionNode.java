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

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.Thing;

/**
 * Represents an EL description tree, which corresponds to a
 * description in the EL description logic. Note that an EL description tree
 * can be a subtree of another EL description tree. In general,
 * an EL description tree is a tree where the node label is a set
 * of named classes and the edges are labelled with a property.
 * 
 * In the documentation below "this node" refers to the root node
 * of this EL description (sub-)tree. One tree cannot be reused,
 * i.e. used as subtree in several description trees, as some of
 * the associated variables (level, simulation) depend on the overall
 * tree. 
 * 
 * @author Jens Lehmann
 *
 */
public class ELDescriptionNode {

	// the reference tree for storing values, must not be null
	private ELDescriptionTree tree;
	
	private SortedSet<NamedClass> label;
	
	private List<ELDescriptionEdge> edges;

	private int level;
	
	// parent node in the tree;
	// null indicates that this node is a root node
	private ELDescriptionNode parent = null;
		
	/**
	 * Constructs an EL description tree with empty root label.
	 */
	public ELDescriptionNode(ELDescriptionTree tree) {
		this(tree, new TreeSet<NamedClass>());
	}	
	
	/**
	 * Constructs an EL description tree given its root label.
	 * @param label Label of the root node.
	 */
	public ELDescriptionNode(ELDescriptionTree tree, SortedSet<NamedClass> label) {
		this.label = label;
		this.edges = new LinkedList<ELDescriptionEdge>();	
		this.tree = tree;
		level = 1;
		parent = null;
	}
	
	public ELDescriptionNode(ELDescriptionNode parentNode, ObjectProperty parentProperty, SortedSet<NamedClass> label) {
		this.label = label;
		this.edges = new LinkedList<ELDescriptionEdge>();
		parent = parentNode;
		// the reference tree is the same as for the parent tree
		tree = parentNode.tree;
		// level increases by 1
		level = parentNode.level + 1;
		// we add an edge from the parent to this node
		ELDescriptionEdge edge = new ELDescriptionEdge(parentProperty, this);
		parent.edges.add(edge);
		// we need to update the set of nodes on a particular level
		tree.addNodeToLevel(this, level);
	}
	
	/**
	 * Constructs an EL description tree given its root label and edges.
	 * @param label Label of the root node.
	 * @param edges Edges connected to the root node.
	 */
//  TODO: probably delete as this constructor is not straightforward to
//  implement within the new structure
//	public ELDescriptionNode(SortedSet<NamedClass> label, List<ELDescriptionEdge> edges) {
//		this.label = label;
//		this.edges = edges;
//	}
	
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
	public ELDescriptionNode getRoot() {
		ELDescriptionNode root = this;
		while(root.parent != null) {
			root = parent;
		}
		return root;
	}
	
	/**
	 * Traverses the tree until the root node and counts how
	 * many edges are traversed. If this node does not have a parent,
	 * zero is returned. This method is used for checking the integrity
	 * of the tree in unit tests. Use {@link #getLevel()} to get the 
	 * level of the tree. 
	 * @return The level of this node (or more specifically the root
	 * node of this subtree) within the overall EL description tree.
	 */
	public int computeLevel() {
		ELDescriptionNode root = this;
		int level = 0;
		while(root.parent != null) {
			root = parent;
			level++;
		}
		return level;		
	}
	
	/**
	 * This method transform the tree to an EL description. The
	 * node labels are transformed to an {@link Intersection}
	 * of {@link NamedClass}. Each edges is transformed to an 
	 * {@link ObjectSomeRestriction}, where the property is the edge
	 * label and the child description the subtree the edge points 
	 * to. Edges are also added to the intersection. If the intersection
	 * is empty, {@link Thing} is returned.
	 * @return The description corresponding to this EL description tree.
	 */
	public Description transformToDescription() {
		if(label.size()==0 && edges.size()==0) {
			return new Thing();
		} else {
			Intersection is = new Intersection();
			for(NamedClass nc : label) {
				is.addChild(nc);
			}
			for(ELDescriptionEdge edge : edges) {
				Description child = edge.getTree().transformToDescription();
				ObjectSomeRestriction osr = new ObjectSomeRestriction(edge.getLabel(),child);
				is.addChild(osr);
			}
			return is;
		}
	}

	/**
	 * Each node is assigned a number within the tree.
	 * TODO add explanation how this is done
	 * @return The position number of this node within the tree as described above.
	 */
	public int getCurrentPositionNumber() {
		return 0;
	}
	
	/**
	 * Replaces an entry in the node label.
	 * @param oldClass Class to remove from label.
	 * @param newClass Class to add to label.
	 */
	public void replaceInLabel(NamedClass oldClass, NamedClass newClass) {
		label.remove(oldClass);
		label.add(newClass);
	}
	
	/**
	 * Gets the label of this node. Do not modify the returned object.
	 * @return The label of root node of this subtree.
	 */
	public SortedSet<NamedClass> getLabel() {
		return label;
	}

	/**
	 * @return The outgoing edges of this subtree. Do not modify the
	 * returned object.
	 */
	public List<ELDescriptionEdge> getEdges() {
		return edges;
	}

	/**
	 * @return The level of the (root node of) this subtree in the overall tree. 
	 */
	public int getLevel() {
		return level;
	}
	
}
