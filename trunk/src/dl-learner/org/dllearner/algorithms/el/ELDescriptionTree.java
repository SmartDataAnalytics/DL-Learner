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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.core.ReasoningService;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.SubsumptionHierarchy;
import org.dllearner.core.owl.Thing;
import org.dllearner.core.owl.UnsupportedLanguageException;

/**
 * Represents an EL description tree. Unlike {@link ELDescriptionNode}, this is
 * a tree-wide structure, i.e. it does not implement the tree structure itself,
 * but is used to store information about the tree.
 * 
 * @author Jens Lehmann
 * 
 */
public class ELDescriptionTree implements Cloneable {

	// to simplify equivalence checks and minimisation, we
	// attach a simulation relation to the description tree
	// private Simulation simulation;

	// max level = 0 means that there is no tree at all
	// (max level = 1 means the root node exists)
	private int maxLevel = 0;

	protected ELDescriptionNode rootNode;

	private Map<Integer, Set<ELDescriptionNode>> levelNodeMapping = new HashMap<Integer, Set<ELDescriptionNode>>();

	// the background knowledge (we need to have it explicitly here, 
	// since we store simulation information in the tree and simulation
	// updates depend on background knowledge)
	protected ReasoningService rs;
	protected SubsumptionHierarchy subsumptionHierarchy;
	protected ObjectPropertyHierarchy roleHierarchy;
	
	public ELDescriptionTree(ReasoningService rs) {
		this.rs = rs;
		subsumptionHierarchy = rs.getSubsumptionHierarchy();
		roleHierarchy = rs.getRoleHierarchy();
	}

	/**
	 * Constructs an EL description tree from an EL description.
	 * 
	 * @param description
	 *            A description
	 */
	public ELDescriptionTree(ReasoningService rs, Description description) {
		this(rs);
		// construct root node and recursively build the tree
		rootNode = new ELDescriptionNode(this);
		constructTree(description, rootNode);
	}

	private void constructTree(Description description, ELDescriptionNode node) {
		if (description instanceof NamedClass) {
			node.extendLabel((NamedClass) description);
		} else if (description instanceof ObjectSomeRestriction) {
			ObjectProperty op = (ObjectProperty) ((ObjectSomeRestriction) description).getRole();
			ELDescriptionNode newNode = new ELDescriptionNode(node, op, new TreeSet<NamedClass>());
			constructTree(description.getChild(0), newNode);
		} else if (description instanceof Thing) {
			// nothing needs to be done as an empty set is owl:Thing
		} else if (description instanceof Intersection) {
			// loop through all elements of the intersection
			for (Description child : description.getChildren()) {
				if (child instanceof NamedClass) {
					node.extendLabel((NamedClass) child);
				} else if (child instanceof ObjectSomeRestriction) {
					ObjectProperty op = (ObjectProperty) ((ObjectSomeRestriction) child).getRole();
					ELDescriptionNode newNode = new ELDescriptionNode(node, op,
							new TreeSet<NamedClass>());
					constructTree(child.getChild(0), newNode);
				} else {
					throw new UnsupportedLanguageException(description + " specifically " + child,
							"EL");
				}
			}
		} else {
			throw new UnsupportedLanguageException(description.toString(), "EL");
		}
	}

	/**
	 * Gets the nodes on a specific level of the tree. This information is
	 * cached here for performance reasons.
	 * 
	 * @param level
	 *            The level (distance from root node).
	 * @return The set of all nodes on the specified level within this tree.
	 */
	public Set<ELDescriptionNode> getNodesOnLevel(int level) {
		return levelNodeMapping.get(level);
	}

	public Description transformToDescription() {
		return rootNode.transformToDescription();
	}

	// checks whether this tree is minimal wrt. background knowledge
	public boolean isMinimal() {
//		System.out.println(this);
//		System.out.println(levelNodeMapping);
		// loop through all levels starting from root (level 1)
		for(int i=1; i<=maxLevel; i++) {
			// get all nodes of this level
			Set<ELDescriptionNode> nodes = levelNodeMapping.get(i);
//			System.out.println("level " + i + ": " + nodes);
			for(ELDescriptionNode node : nodes) {
				List<ELDescriptionEdge> edges = node.getEdges();
				// we need to compare all combination of edges
				// (in both directions because subsumption is obviously
				// not symmetric)
				for(int j=0; j<edges.size(); j++) {
					for(int k=0; k<edges.size(); k++) {
						if(j != k) {
							// we first check inclusion property on edges
							ObjectProperty op1 = edges.get(j).getLabel();
							ObjectProperty op2 = edges.get(k).getLabel();
							if(rs.getRoleHierarchy().isSubpropertyOf(op1, op2)) {
								ELDescriptionNode node1 = edges.get(j).getTree();
								ELDescriptionNode node2 = edges.get(k).getTree();
								// check simulation condition
								if(node1.in.contains(node2) || node2.in.contains(node1)) {
									// node1 is simulated by node2, i.e. we could remove one
									// of them, so the tree is not minimal
									return false;
								}
							}
						}
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * Internal method for updating the level node mapping. It is called when a
	 * new node is added to the tree.
	 * 
	 * @param node
	 *            The new node.
	 * @param level
	 *            Level of the new node.
	 */
	protected void addNodeToLevel(ELDescriptionNode node, int level) {
		if (level <= maxLevel) {
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

	/**
	 * Gets the node at the given position. The list is processed as follows:
	 * Starting with the root node, the first element i of list is read and the
	 * i-th child of root node is selected. This node is set as current node and
	 * the next element j of the list is read and the j-th child of the i-th
	 * child of the root node selected etc.
	 * 
	 * @return The node at the specified position.
	 */
	public ELDescriptionNode getNode(int[] position) {
		ELDescriptionNode currentNode = rootNode;
		for (int i = 0; i < position.length; i++) {
			currentNode = currentNode.getEdges().get(position[i]).getTree();
		}
		return currentNode;
	}

	protected void updateSimulation(Set<ELDescriptionNode> nUpdate) {
		// create a stack and initialize it with the nodes to be updated
		LinkedList<ELDescriptionNode> list = new LinkedList<ELDescriptionNode>();
		list.addAll(nUpdate);
		
		while(list.size() != 0) {
			// take element from bottom of stack (to ensure that all nodes on the 
			// same level are tested before any node of a lower level is tested)
			ELDescriptionNode v = list.pollFirst();
			// loop through all nodes on same level
			for(ELDescriptionNode w : levelNodeMapping.get(v.getLevel())) {
				if(!v.out.contains(w) && v.outSC1.contains(w) && checkSC2(v,w)) {
					extendSimulation(v,w);
					list.add(v.getParent());
					list.add(w.getParent());
				}
				if(!w.out.contains(v) && w.outSC1.contains(v) && checkSC2(w,v)) {
					extendSimulation(w,v);
					list.add(v.getParent());
					list.add(w.getParent());
				}
			}
		}
	}
	
	// SC satisfied if both SC1 and SC2 satisfied
	public boolean checkSC(ELDescriptionNode node1, ELDescriptionNode node2) {
		return checkSC1(node1, node2) && checkSC2(node1, node2);
	}	
	
	// tests simulation condition 1 (SC1)
	public boolean checkSC1(ELDescriptionNode node1, ELDescriptionNode node2) {
		return isSublabel(node1.getLabel(), node2.getLabel());
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
			if(subsumptionHierarchy.isSubclassOf(nc, superClass)) {
				return true;
			}
		}
		return false;
	}
	
	// tests simulation condition 2 (SC2)
	public boolean checkSC2(ELDescriptionNode node1, ELDescriptionNode node2) {
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
			if(roleHierarchy.isSubpropertyOf(op1, op2)) {
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
	public void extendSimulation(ELDescriptionNode node1, ELDescriptionNode node2) {
		node1.out.add(node2);
		node1.outSC1.add(node2);
		node1.outSC2.add(node2);
		node2.in.add(node1);
		node2.inSC1.add(node1);
		node2.inSC2.add(node1);
	}
	
	// removes (node1,node2) from simulation, takes care of all helper sets
	public void shrinkSimulation(ELDescriptionNode node1, ELDescriptionNode node2) {
		node1.out.remove(node2);
		node1.outSC1.remove(node2);
		node1.outSC2.remove(node2);
		node2.in.remove(node1);
		node2.inSC1.remove(node1);
		node2.inSC2.remove(node1);
	}	
	
	@Override
	public ELDescriptionTree clone() {
		// create a new reference tree
		ELDescriptionTree treeClone = new ELDescriptionTree(rs);
		// create a root node attached to this reference tree
		ELDescriptionNode rootNodeClone = new ELDescriptionNode(treeClone, new TreeSet<NamedClass>(
				rootNode.getLabel()));
		cloneRecursively(rootNode, rootNodeClone);
		return treeClone;
	}

	// we read from the original structure and write to the new structure
	private void cloneRecursively(ELDescriptionNode node, ELDescriptionNode nodeClone) {
		// loop through all edges and clone the subtrees
		for (ELDescriptionEdge edge : node.getEdges()) {
			ELDescriptionNode tmp = new ELDescriptionNode(nodeClone, edge.getLabel(),
					new TreeSet<NamedClass>(edge.getTree().getLabel()));
			cloneRecursively(edge.getTree(), tmp);
		}
	}

	@Override
	public String toString() {
		return rootNode.toString();
	}
}
