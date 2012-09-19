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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.Thing;
import org.dllearner.utilities.Helper;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

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
	protected ELDescriptionTree tree;
	
	protected TreeSet<NamedClass> label = new TreeSet<NamedClass>();
	
	protected List<ELDescriptionEdge> edges = new LinkedList<ELDescriptionEdge>();

	protected int level;
	
	// parent node in the tree;
	// null indicates that this node is a root node
	protected ELDescriptionNode parent = null;
		
	// simulation information (list or set?)
	protected Set<ELDescriptionNode> in = new HashSet<ELDescriptionNode>();
	protected Set<ELDescriptionNode> inSC1 = new HashSet<ELDescriptionNode>();
	protected Set<ELDescriptionNode> inSC2 = new HashSet<ELDescriptionNode>();
	protected Set<ELDescriptionNode> out = new HashSet<ELDescriptionNode>();
	protected Set<ELDescriptionNode> outSC1 = new HashSet<ELDescriptionNode>();
	protected Set<ELDescriptionNode> outSC2 = new HashSet<ELDescriptionNode>();
	
	/**
	 * Internal constructor used for cloning nodes.
	 */
	protected ELDescriptionNode() {
		
	}
	
	/**
	 * Constructs an EL description tree with empty root label.
	 */
	public ELDescriptionNode(ELDescriptionTree tree) {
		this(tree, new TreeSet<NamedClass>());
	}	
	
	// convenience constructor
	public ELDescriptionNode(ELDescriptionTree tree, NamedClass... label) {
		this(tree, new TreeSet<NamedClass>(Arrays.asList(label)));
	}	
	
	/**
	 * Constructs an EL description tree given its root label.
	 * @param label Label of the root node.
	 */
	public ELDescriptionNode(ELDescriptionTree tree, TreeSet<NamedClass> label) {
		this.label = label;
		this.edges = new LinkedList<ELDescriptionEdge>();	
		this.tree = tree;
		level = 1;
		parent = null;
		// this is the root node of the overall tree
		tree.rootNode = this;
		tree.addNodeToLevel(this, level);
		tree.size += label.size();
	}
	
	// convenience constructor
	public ELDescriptionNode(ELDescriptionNode parentNode, ObjectProperty parentProperty, NamedClass... label) {
		this(parentNode, parentProperty, new TreeSet<NamedClass>(Arrays.asList(label)));
	}
	
	public ELDescriptionNode(ELDescriptionNode parentNode, ObjectProperty parentProperty, Set<NamedClass> label) {
//		this.label = label;
		// we first need to add the edge and update the simulation and then add
		// all classes iteratively to the label (each time updating the simulation again)
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
//		Monitor mon = MonitorFactory.start("simulation update");
		// the nodes, which need to be updated
		Set<ELDescriptionNode> update = new HashSet<ELDescriptionNode>();
		
		// loop over all nodes on the same level, which are not in the in set
		Set<ELDescriptionNode> nodes = tree.getNodesOnLevel(level);
		for(ELDescriptionNode w : nodes) {
			// to save space, we do not add reflexive relations
			if(w != this) {
				// (w,v') is automatically added
				tree.extendSimulation(w, this);
				
				// check conditions for (v',w)
				boolean sc1 = false, sc2 = false;
				
				if(w.label.size() == 0) {
					tree.extendSimulationSC1(this, w);
					sc1 = true;
				}
				
				if(w.edges.size() == 0) {
					tree.extendSimulationSC2(this, w);
					sc2 = true;
				}
				
				if(sc1 && sc2) {
					tree.extendSimulationSC12(this, w);
				}	
				
				update.add(w.parent);
			}
		}
		update.add(this.parent);
		
//		if(inSC1.contains(w) && tree.checkSC2(this, w)) {
//			tree.extendSimulation(this, w);
//			update.add(w.parent);
//		}		
		
		// loop over all nodes in out set
//		for(ELDescriptionNode w : out) {
//			if(!tree.checkSC1(this, w)) {
//				tree.shrinkSimulation(this, w);
//				update.add(w.parent);
//			}
//		}
		
//		System.out.println(update);
		
		// apply updates recursively top-down
		tree.updateSimulation(update);
//		mon.stop();
		
		// add all classes in label
		for(NamedClass nc : label) {
			extendLabel(nc);
		}
		
		// 1 for the edge (labels are already taken care of by extendLabel)
		tree.size += 1;
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
				Description child = edge.getNode().transformToDescription();
				return new ObjectSomeRestriction(edge.getLabel(),child);
			}
		// return an intersection of labels and edges
		} else {
			Intersection is = new Intersection();
			for(NamedClass nc : label) {
				is.addChild(nc);
			}
			for(ELDescriptionEdge edge : edges) {
				Description child = edge.getNode().transformToDescription();
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
			position[root.level-2] = root.getChildNumber();
			root = root.parent;	
		}
		return position;
	}
	
	// returns the child number of this node, i.e. whether it is 
	// the first, second, third etc. child;
	// TODO: might be a bit faster to store this explicitly
	private int getChildNumber() {
		int count = 0;
		for(ELDescriptionEdge edge : parent.edges) {
			if(edge.getNode() == this) {
				return count;
			}
			count++;
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
		tree.size += 1;
//		System.out.println(tree);
//		System.out.println(tree.size);
	}	
	
	// simulation update when extending or refining label 
	// (same in both cases)
	private void labelSimulationUpdate() {
//		Monitor mon = MonitorFactory.start("simulation update");
		// compute the nodes, which need to be updated
		Set<ELDescriptionNode> update = new HashSet<ELDescriptionNode>();
		
		Set<ELDescriptionNode> tmp = tree.getNodesOnLevel(level);
		for(ELDescriptionNode w : tmp) {
			if(w != this) {
				// SC1(v,w) can only change from false to true
				if(!inSC1.contains(w) && tree.checkSC1(this, w)) {
					tree.extendSimulationSC1(this, w);
					if(inSC2.contains(w)) {
						tree.extendSimulationSC12(this, w);		
					}
					update.add(w.getParent());
				}
				// SC1(w,v) can only change from true to false
				if(outSC1.contains(w) && !tree.checkSC1(w, this)) {
					tree.shrinkSimulationSC1(w, this);
					if(outSC2.contains(w)) {
						tree.shrinkSimulationSC12(w, this);		
					}
					update.add(w.getParent());
				}
			}
		}
		if(parent != null) {
			update.add(parent);
		}
		
		/*
		// loop over all nodes on the same level, which are not in the in set
		Set<ELDescriptionNode> tmp = new HashSet<ELDescriptionNode>(tree.getNodesOnLevel(level));
		tmp.removeAll(in);
		for(ELDescriptionNode w : tmp) {
			if(w != this) {
				// we only need to recompute SC1
				if(inSC1.contains(w) && tree.checkSC2(this, w)) {
					System.out.println("satisfied");
					tree.extendSimulation(this, w);
					update.add(w.parent);
				}
			}
		}
		
		// loop over all nodes in out set (we make a copy, because out
		// is potentially modified, so we cannot safely iterate over it)
		tmp = new HashSet<ELDescriptionNode>(out);
		for(ELDescriptionNode w : tmp) {
			if(w != this) {
				if(!tree.checkSC1(w, this)) {
//					tree.shrinkSimulation(w, this);
					tree.shrinkSimulationSC1(w, this);
					tree.shrinkSimulationSC12(w, this);
					update.add(w.parent);
				}
			}
		}
		*/
		
		// apply updates recursively top-down
		tree.updateSimulation(update);	
//		mon.stop();
	}

	public void refineEdge(int edgeNumber, ObjectProperty op) {
		edges.get(edgeNumber).setLabel(op);
		
//		Monitor mon = MonitorFactory.start("simulation update");
		// compute the nodes, which need to be updated
		Set<ELDescriptionNode> update = new HashSet<ELDescriptionNode>();
		update.add(this);
		
		/*
		// loop over all nodes on the same level, which are not in the in set
		Set<ELDescriptionNode> tmp = new HashSet<ELDescriptionNode>(tree.getNodesOnLevel(level));
		tmp.removeAll(in);
		for(ELDescriptionNode w : tmp) {
			if(w != this) {
				// we only need to recompute SC1
				if(inSC2.contains(w) && tree.checkSC1(this, w)) {
					tree.extendSimulation(this, w);
					update.add(w.parent);
				}
			}
		}
		
		// loop over all nodes in out set
		for(ELDescriptionNode w : out) {
			if(w != this) {
				if(!tree.checkSC2(this, w)) {
					tree.shrinkSimulation(this, w);
					update.add(w.parent);
				}
			}
		}
		*/
		
//		update.add(this.parent);
		
		// apply updates recursively top-down
		tree.updateSimulation(update);	
//		mon.stop();
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
			str += edge.getNode().toString(indent + 2);
		}
		return str;
	}

	public String toDescriptionString() {
		String str = "";
		if(label.isEmpty()) {
			str = "TOP";
		} else {
			Iterator<NamedClass> it = label.iterator();
			while(it.hasNext()) {
				NamedClass nc = it.next();
				if(it.hasNext()) {
					str += nc.toString() + " AND ";
				} else {
					str += nc.toString();
				}
			}
		}
		for(ELDescriptionEdge edge : edges) {
			str += " AND EXISTS " + edge.getLabel().toString() + ".(";
			str += edge.getNode().toDescriptionString() + ")";
		}
		return str;		
	}
	
	private String toDescriptionString(Set<ELDescriptionNode> nodes) {
		String str = "";
		// comma separated list of descriptions
		for(ELDescriptionNode node : nodes) {
			str += node.toDescriptionString() + ",";
		}
		// remove last comma
		if(str.length() > 0) {
			str = str.substring(0, str.length()-1);
		}
		return str;
	}
	
	public String toSimulationString() {
		String str = "";
		str += "in: " + toDescriptionString(in) + "\n";
		str += "inSC1: " + toDescriptionString(inSC1) + "\n";
		str += "inSC2: " + toDescriptionString(inSC2) + "\n";
		str += "out: " + toDescriptionString(out) + "\n";
		str += "outSC1: " + toDescriptionString(outSC1) + "\n";
		str += "outSC2: " + toDescriptionString(outSC2) + "\n";		
		return str;
	}
	
	/**
	 * A convenience method (for debugging purposes) to get a comma separated list of nodes, where the
	 * nodes are given names (to make them readable).
	 * @param nodes The node objects.
	 * @param nodeNames A mapping to node names.
	 * @return A comma separated list of the node names.
	 */
	public static String toString(Set<ELDescriptionNode> nodes, Map<ELDescriptionNode,String> nodeNames) {
		String str = "";
		// comma separated list of descriptions
		for(ELDescriptionNode node : nodes) {
			str += nodeNames.get(node) + ",";
		}
		// remove last comma
		if(str.length() > 0) {
			str = str.substring(0, str.length()-1);
		}
		return str;
	}
	
	public String toSimulationString(Map<ELDescriptionNode,String> nodeNames) {
		String str = "";
		str += "  in: " + toString(in, nodeNames) + "\n";
		str += "  inSC1: " + toString(inSC1, nodeNames) + "\n";
		str += "  inSC2: " + toString(inSC2, nodeNames) + "\n";
		str += "  out: " + toString(out, nodeNames) + "\n";
		str += "  outSC1: " + toString(outSC1, nodeNames) + "\n";
		str += "  outSC2: " + toString(outSC2, nodeNames) + "\n";		
		return str;
	}
	
	public ELDescriptionNode getParent() {
		return parent;
	}
	
	public ELDescriptionEdge getParentEdge() {
		int childNr = getChildNumber();
		return parent.edges.get(childNr);
	}

	/**
	 * @return the in
	 */
	public Set<ELDescriptionNode> getIn() {
		return in;
	}

	/**
	 * @return the inSC1
	 */
	public Set<ELDescriptionNode> getInSC1() {
		return inSC1;
	}

	/**
	 * @return the inSC2
	 */
	public Set<ELDescriptionNode> getInSC2() {
		return inSC2;
	}

	/**
	 * @return the out
	 */
	public Set<ELDescriptionNode> getOut() {
		return out;
	}

	/**
	 * @return the outSC1
	 */
	public Set<ELDescriptionNode> getOutSC1() {
		return outSC1;
	}

	/**
	 * @return the outSC2
	 */
	public Set<ELDescriptionNode> getOutSC2() {
		return outSC2;
	}

	public ELDescriptionTree getTree() {
		return tree;
	}
}
