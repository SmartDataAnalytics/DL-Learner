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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.ReasoningService;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.Thing;
import org.dllearner.utilities.Helper;

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
@SuppressWarnings("unused")
public class ELDescriptionNode {

	// the reference tree for storing values, must not be null
	private ELDescriptionTree tree;
	
	private NavigableSet<NamedClass> label;
	
	private List<ELDescriptionEdge> edges;

	private int level;
	
	// parent node in the tree;
	// null indicates that this node is a root node
	private ELDescriptionNode parent = null;
		
	// simulation information (list or set?)
	protected Set<ELDescriptionNode> in = new HashSet<ELDescriptionNode>();
	private Set<ELDescriptionNode> inSC1 = new HashSet<ELDescriptionNode>();
	private Set<ELDescriptionNode> inSC2 = new HashSet<ELDescriptionNode>();
	protected Set<ELDescriptionNode> out = new HashSet<ELDescriptionNode>();
	private Set<ELDescriptionNode> outSC1 = new HashSet<ELDescriptionNode>();
	private Set<ELDescriptionNode> outSC2 = new HashSet<ELDescriptionNode>();
	
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
	public ELDescriptionNode(ELDescriptionTree tree, NavigableSet<NamedClass> label) {
		this.label = label;
		this.edges = new LinkedList<ELDescriptionEdge>();	
		this.tree = tree;
		level = 1;
		parent = null;
		// this is the root node of the overall tree
		tree.rootNode = this;
		tree.addNodeToLevel(this, level);
		
		// TODO simulation update
	}
	
	public ELDescriptionNode(ELDescriptionNode parentNode, ObjectProperty parentProperty, NavigableSet<NamedClass> label) {
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
		
		// simulation update
		// the nodes, which need to be updated
		Set<ELDescriptionNode> update = new TreeSet<ELDescriptionNode>();
		
		// loop over all nodes on the same level, which are not in the in set
		Set<ELDescriptionNode> nodes = tree.getNodesOnLevel(level);
		for(ELDescriptionNode w : nodes) {
			if(w.label.size() == 0) {
				
			}
			
			if(inSC1.contains(w) && checkSC2(this, w)) {
				extendSimulation(this, w);
				update.add(w.parent);
			}
		}
		
		// loop over all nodes in out set
		for(ELDescriptionNode w : out) {
			if(!checkSC1(this, w)) {
				shrinkSimulation(this, w);
				update.add(w.parent);
			}
		}
		
		// apply updates recursively top-down
		tree.updateSimulation(update);		
		
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
	 * of {@link NamedClass}. Each edge is transformed to an 
	 * {@link ObjectSomeRestriction}, where the property is the edge
	 * label and the child description the subtree the edge points 
	 * to. Edges are also added to the intersection. If the intersection
	 * is empty, {@link Thing} is returned.
	 * @return The description corresponding to this EL description tree.
	 */
	public Description transformToDescription() {
		int nrOfElements = label.size() + edges.size();
		// leaf labeled with \emptyset stands for owl:Thing
		if(nrOfElements == 0) {
			return new Thing();
		// we want to avoid intersections with only 1 element, so in this
		// case we return either the NamedClass or ObjectSomeRestriction directly
		} else if(nrOfElements == 1) {
			if(label.size()==1) {
				return label.first();
			} else {
				ELDescriptionEdge edge = edges.get(0);
				Description child = edge.getTree().transformToDescription();
				return new ObjectSomeRestriction(edge.getLabel(),child);
			}
		// return an intersection of labels and edges
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
	 * Gets a list describing the position of this node within the 
	 * tree. If the list is e.g. [2,5,1], then the node can be reached
	 * by picking the second child of the root node, then picking the
	 * 5th child of this node and finally selecting the first child of
	 * the previous node.
	 * @return The position number of this node within the tree as described above.
	 */
	public int[] getCurrentPosition() {
		int[] position = new int[level-1];
		ELDescriptionNode root = this;
		while(root.parent != null) {
			position[root.level-2] = getChildNumber();
			root = root.parent;	
		}
		return position;
	}
	
	// returns the child number of this node, i.e. whether it is 
	// the first, second, third etc. child
	private int getChildNumber() {
		int count = 0;
		for(ELDescriptionEdge edge : parent.edges) {
			if(edge.getTree() == this) {
				return count;
			}
		}
		throw new RuntimeException("Inconsistent tree. Child tree not reachable from parent.");
	}
	
	/**
	 * Replaces an entry in the node label.
	 * @param oldClass Class to remove from label.
	 * @param newClass Class to add to label.
	 */
	public void replaceInLabel(NamedClass oldClass, NamedClass newClass) {
		label.remove(oldClass);
		label.add(newClass);
		labelSimulationUpdate();
	}
	
	/**
	 * Adds an entry to the node label.
	 * @param newClass Class to add to label.
	 */
	public void extendLabel(NamedClass newClass) {
		label.add(newClass);
		labelSimulationUpdate();
	}	
	
	// simulation update when extending or refining label 
	// (same in both cases)
	private void labelSimulationUpdate() {
		// compute the nodes, which need to be updated
		Set<ELDescriptionNode> update = new TreeSet<ELDescriptionNode>();
		
		// loop over all nodes on the same level, which are not in the in set
		Set<ELDescriptionNode> nodes = tree.getNodesOnLevel(level);
		Set<ELDescriptionNode> tmp = Helper.difference(nodes, in);
		for(ELDescriptionNode w : tmp) {
			// we only need to recompute SC2
			if(inSC1.contains(w) && checkSC2(this, w)) {
				extendSimulation(this, w);
				update.add(w.parent);
			}
		}
		
		// loop over all nodes in out set
		for(ELDescriptionNode w : out) {
			if(!checkSC1(this, w)) {
				shrinkSimulation(this, w);
				update.add(w.parent);
			}
		}
		
		// apply updates recursively top-down
		tree.updateSimulation(update);		
	}
	
	// SC satisfied if both SC1 and SC2 satisfied
	private boolean checkSC(ELDescriptionNode node1, ELDescriptionNode node2) {
		return checkSC1(node1, node2) && checkSC2(node1, node2);
	}	
	
	// tests simulation condition 1 (SC1)
	private boolean checkSC1(ELDescriptionNode node1, ELDescriptionNode node2) {
		return isSublabel(node1.label, node2.label);
	}
	
	private boolean isSublabel(NavigableSet<NamedClass> subLabel, NavigableSet<NamedClass> superLabel) {
		// implemented according to definition in article
		// (TODO can probably be done more efficiently)
		for(NamedClass nc : superLabel) {
			if(!containsSubclass(nc, subLabel)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean containsSubclass(NamedClass superClass, NavigableSet<NamedClass> label) {
		for(NamedClass nc : label) {
			if(tree.subsumptionHierarchy.isSubclassOf(nc, superClass)) {
				return true;
			}
		}
		return false;
	}
	
	// tests simulation condition 2 (SC2)
	private boolean checkSC2(ELDescriptionNode node1, ELDescriptionNode node2) {
		List<ELDescriptionEdge> edges1 = node1.getEdges();
		List<ELDescriptionEdge> edges2 = node2.getEdges();
		
		for(ELDescriptionEdge edge : edges1) {
			// try to find an edge satisfying SC2 in the set
			if(!checkSC2Edge(edge, edges2)) {
				return false;
			}
		}
		
		return true;
	}
	
	// check whether edges contains an element satisfying SC2
	private boolean checkSC2Edge(ELDescriptionEdge edge, List<ELDescriptionEdge> edges) {
		ObjectProperty op1 = edge.getLabel();
		ELDescriptionNode node1 = edge.getTree();
		
		for(ELDescriptionEdge edge2 : edges) {
			ObjectProperty op2 = edge2.getLabel();
			// we first check the condition on the properties
			if(tree.roleHierarchy.isSubpropertyOf(op1, op2)) {
				// check condition on simulations of referred nodes
				ELDescriptionNode node2 = edge2.getTree();
				if(node1.in.contains(node2) || node2.in.contains(node1)) {
					// we found a node satisfying the condition, so we can return
					return true;
				}				
			}
		}
		
		// none of the edges in the set satisfies the 2nd simulation criterion
		// wrt. the first edge
		return false;
	}

	// adds (node1,node2) to simulation, takes care of all helper sets
	private void extendSimulation(ELDescriptionNode node1, ELDescriptionNode node2) {
		node1.out.add(node2);
		node2.in.add(node1);
		// TODO: SC1, SC2 sets ?
	}
	
	// removes (node1,node2) from simulation, takes care of all helper sets
	private void shrinkSimulation(ELDescriptionNode node1, ELDescriptionNode node2) {
		node1.out.remove(node2);
		node2.in.remove(node1);
		// TODO: SC1, SC2 sets ?
	}
	
	/**
	 * Gets the label of this node. Do not modify the returned object,
	 * but use the provided methods instead!
	 * @return The label of root node of this subtree.
	 */
	public NavigableSet<NamedClass> getLabel() {
		return label;
	}

	/**
	 * Gets the edges of this node. Do not modify the
	 * returned object, but use the provided methods instead!
	 * @return The outgoing edges of this subtree. 
	 */
	public List<ELDescriptionEdge> getEdges() {
		return edges;
	}

	/**
	 * Gets the level (distance from root) of this node. The root node
	 * has level 1.
	 * @return The level of the (root node of) this subtree in the overall tree. 
	 */
	public int getLevel() {
		return level;
	}
	
	@Override
	public String toString() {
		return toString(0);
	}
	
	private String toString(int indent) {
		String indentString = "";
		for(int i=0; i<indent; i++)
			indentString += "  ";
		
		String str = indentString + label.toString() + "\n";
		for(ELDescriptionEdge edge : edges) {
			str += indentString + "-- " + edge.getLabel() + " -->\n";
			str += edge.getTree().toString(indent + 2);
		}
		return str;
	}
}
