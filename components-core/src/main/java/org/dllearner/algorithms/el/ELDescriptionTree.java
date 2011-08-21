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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.core.owl.ObjectSomeRestriction;
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

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ELDescriptionTree.class);
	
	// to simplify equivalence checks and minimisation, we
	// attach a simulation relation to the description tree
	// private Simulation simulation;

	// max level = 0 means that there is no tree at all
	// (max level = 1 means the root node exists)
	private int maxLevel = 0;
	
	protected int size = 1;

	protected ELDescriptionNode rootNode;

	// the set of all nodes in the tree
	private Collection<ELDescriptionNode> nodes = new LinkedList<ELDescriptionNode>();
	
	// nodes on a given level of the tree
	private Map<Integer, Set<ELDescriptionNode>> levelNodeMapping = new HashMap<Integer, Set<ELDescriptionNode>>();

	// the background knowledge (we need to have it explicitly here, 
	// since we store simulation information in the tree and simulation
	// updates depend on background knowledge)
	protected AbstractReasonerComponent rs;
	protected ClassHierarchy subsumptionHierarchy;
	protected ObjectPropertyHierarchy roleHierarchy;
	
	public ELDescriptionTree(AbstractReasonerComponent rs) {
		this.rs = rs;
		subsumptionHierarchy = rs.getClassHierarchy();
		roleHierarchy = rs.getObjectPropertyHierarchy();
	}

	/**
	 * Constructs an EL description tree from an EL description.
	 * 
	 * @param description
	 *            A description
	 */
	public ELDescriptionTree(AbstractReasonerComponent rs, Description description) {
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
							if(rs.getObjectPropertyHierarchy().isSubpropertyOf(op1, op2)) {
								ELDescriptionNode node1 = edges.get(j).getNode();
								ELDescriptionNode node2 = edges.get(k).getNode();
								// check simulation condition
								if(node1.in.contains(node2)) { // || node2.in.contains(node1)) {
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
	 * Internal method for updating the node set and the level node mapping. It must be 
	 * called when a new node is added to the tree.
	 * 
	 * @param node
	 *            The new node.
	 * @param level
	 *            Level of the new node.
	 */
	protected void addNodeToLevel(ELDescriptionNode node, int level) {
		nodes.add(node);
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
//		logger.trace(Helper.arrayContent(position));		
//		logger.trace(this);
		ELDescriptionNode currentNode = rootNode;
		for (int i = 0; i < position.length; i++) {
			currentNode = currentNode.getEdges().get(position[i]).getNode();
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
			Set<ELDescriptionNode> sameLevel = levelNodeMapping.get(v.getLevel());
			for(ELDescriptionNode w : sameLevel) {
				if(v != w) {
					
//					System.out.println(v);
//					System.out.println(w);
					
					// we update if SC2 did not hold but does now
					if(!v.inSC2.contains(w) && checkSC2(v,w)) {
//						System.out.println("extend sim. after update");
						
						extendSimulationSC2(v,w);
						if(v.inSC1.contains(w)) {
							extendSimulationSC12(v,w);
						}
						if(!list.contains(v.getParent())) {
							list.add(v.getParent());
						}
						if(!list.contains(w.getParent())) {
							list.add(w.getParent());
						}					
					}
					
					// similar case, but now possibly shrinking the simulation
					if(w.inSC2.contains(v) && !checkSC2(w,v)) {
//						System.out.println("shrink sim. after update");
						
						shrinkSimulationSC2(w,v);
						if(w.inSC1.contains(v)) {
							shrinkSimulationSC12(w,v);
						}
						if(!list.contains(v.getParent())) {
							list.add(v.getParent());
						}
						if(!list.contains(w.getParent())) {
							list.add(w.getParent());
						}							
					}
					/*
					if(!v.out.contains(w) ) {
						System.out.println("test");
						if(checkSC2(v,w) && v.outSC1.contains(w)) {
							extendSimulation(v,w);
							list.add(v.getParent());
							list.add(w.getParent());
						} else {
							System.out.println("test in");
							shrinkSimulationSC2(v,w);
						}
					}
					if(!w.out.contains(v) ) {
						if(checkSC2(w,v) && w.outSC1.contains(v)) {
							extendSimulation(w,v);
							list.add(v.getParent());
							list.add(w.getParent());
						} else {
							shrinkSimulationSC2(w,v);
						}
					}
					*/
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
		
//		System.out.println(node1.transformToDescription());
//		System.out.println(node2.transformToDescription());
		
		for(ELDescriptionEdge superEdge : edges2) {
			// try to find an edge satisfying SC2 in the set,
			// i.e. detect whether superEdge is indeed more general
			if(!checkSC2Edge(superEdge, edges1)) {
//				System.out.println("false");
				return false;
			}
		}
//		System.out.println("true");
		return true;
	}
	
	// check whether edges contains an element satisfying SC2
	private boolean checkSC2Edge(ELDescriptionEdge superEdge, List<ELDescriptionEdge> edges) {
		ObjectProperty superOP = superEdge.getLabel();
		ELDescriptionNode superNode = superEdge.getNode();
		
		for(ELDescriptionEdge edge : edges) {
//			System.out.println("superEdge: " + superEdge);
//			System.out.println("edge: " + edge);
			
			ObjectProperty op = edge.getLabel();		
			// we first check the condition on the properties
			if(roleHierarchy.isSubpropertyOf(op, superOP)) {
				// check condition on simulations of referred nodes
				ELDescriptionNode node = edge.getNode();
//				if(superNode.in.contains(node) || node.in.contains(superNode)) {
				if(node.in.contains(superNode)) {
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
		node1.in.add(node2);
		node1.inSC1.add(node2);
		node1.inSC2.add(node2);
		node2.out.add(node1);
		node2.outSC1.add(node1);
		node2.outSC2.add(node1);
	}
	
	public void extendSimulationSC1(ELDescriptionNode node1, ELDescriptionNode node2) {
		node1.inSC1.add(node2);
		node2.outSC1.add(node1);
	}
	
	public void extendSimulationSC2(ELDescriptionNode node1, ELDescriptionNode node2) {
		node1.inSC2.add(node2);
		node2.outSC2.add(node1);
	}
	
	public void extendSimulationSC12(ELDescriptionNode node1, ELDescriptionNode node2) {
		node1.in.add(node2);
		node2.out.add(node1);
	}
	
	// removes (node1,node2) from simulation, takes care of all helper sets
	public void shrinkSimulation(ELDescriptionNode node1, ELDescriptionNode node2) {
		node1.in.remove(node2);
		node1.inSC1.remove(node2);
		node1.inSC2.remove(node2);
		node2.out.remove(node1);
		node2.outSC1.remove(node1);
		node2.outSC2.remove(node1);
	}	
	
	public void shrinkSimulationSC1(ELDescriptionNode node1, ELDescriptionNode node2) {
		node1.inSC1.remove(node2);
		node2.outSC1.remove(node1);
	}
	
	public void shrinkSimulationSC2(ELDescriptionNode node1, ELDescriptionNode node2) {
//		System.out.println(node2.outSC2);
		node1.inSC2.remove(node2);
		node2.outSC2.remove(node1);
//		System.out.println(node2.outSC2);
	}
	
	public void shrinkSimulationSC12(ELDescriptionNode node1, ELDescriptionNode node2) {
		node1.in.remove(node2);
		node2.out.remove(node1);
	}
	
	public String toSimulationString() {
		String str = "";
		for(ELDescriptionNode node : nodes) {
			str += node.toSimulationString() + "\n";
		}
		return str;
	}		
	
	public String toSimulationString(Map<ELDescriptionNode,String> nodeNames) {
		String str = "";
		for(Entry<ELDescriptionNode,String> entry : nodeNames.entrySet()) {
			String nodeName = entry.getValue();
			ELDescriptionNode node = entry.getKey();
			str += nodeName + ":\n";
			str += node.toSimulationString(nodeNames) + "\n";
		}
		return str;
	}	
	
	@Override
	@SuppressWarnings("unchecked")
	public ELDescriptionTree clone() {
//		Monitor mon = MonitorFactory.start("tree clone");
		// clone "global" tree
		ELDescriptionTree treeClone = new ELDescriptionTree(rs);
		
		// a mapping between "old" and "new" nodes
		// (hash map should be fast here, but one could also
		// experiment with TreeMap)
		Map<ELDescriptionNode, ELDescriptionNode> cloneMap =
			new HashMap<ELDescriptionNode, ELDescriptionNode>();
		
		// create a new (empty) node for each node in the tree
		// (we loop through the level mapping, because it is cheaper
		// than creating a set of all nodes)
		for(int i=1; i<=maxLevel; i++) {
			Set<ELDescriptionNode> tmp = levelNodeMapping.get(i);
			for(ELDescriptionNode node : tmp) {
				ELDescriptionNode nodeNew = new ELDescriptionNode();
				cloneMap.put(node, nodeNew);
			}
		}
		
		ELDescriptionNode newRoot = null;
		
		// loop through all nodes and perform copy operations
		for(Entry<ELDescriptionNode, ELDescriptionNode> entry : cloneMap.entrySet()) {
			ELDescriptionNode oldNode = entry.getKey();
			ELDescriptionNode newNode = entry.getValue();
			
			newNode.tree = treeClone;
			newNode.level = oldNode.level;
			newNode.label = (TreeSet<NamedClass>) oldNode.label.clone();
			if(oldNode.parent != null) {
				newNode.parent = cloneMap.get(oldNode.parent);
			} else {
				newRoot = newNode;
			}
			
			// simulation information
			for(ELDescriptionNode node : oldNode.in) {
				newNode.in.add(cloneMap.get(node));
			}
			for(ELDescriptionNode node : oldNode.inSC1) {
				newNode.inSC1.add(cloneMap.get(node));
			}
			for(ELDescriptionNode node : oldNode.inSC2) {
				newNode.inSC2.add(cloneMap.get(node));
			}
			for(ELDescriptionNode node : oldNode.out) {
				newNode.out.add(cloneMap.get(node));
			}
			for(ELDescriptionNode node : oldNode.outSC1) {
				newNode.outSC1.add(cloneMap.get(node));
			}
			for(ELDescriptionNode node : oldNode.outSC2) {
				newNode.outSC2.add(cloneMap.get(node));
			}			
			
			// edges
			for(ELDescriptionEdge edge : oldNode.edges) {
				// create a new edge with same label and replace the node the edge points to
				newNode.edges.add(new ELDescriptionEdge(edge.getLabel(), cloneMap.get(edge.getNode())));
			}
			
		}
		
		// update global tree
		treeClone.rootNode = newRoot;
		treeClone.maxLevel = maxLevel;
		treeClone.size = size;
		
		// nodes
		treeClone.nodes = new LinkedList<ELDescriptionNode>();
		for(ELDescriptionNode oldNode : nodes) {
			treeClone.nodes.add(cloneMap.get(oldNode));
		}		
		
		// level node mapping
		for(int i=1; i<=maxLevel; i++) {
			Set<ELDescriptionNode> oldNodes = levelNodeMapping.get(i);
			Set<ELDescriptionNode> newNodes = new HashSet<ELDescriptionNode>();
			for(ELDescriptionNode oldNode : oldNodes) {
				newNodes.add(cloneMap.get(oldNode));
			}
			treeClone.levelNodeMapping.put(i, newNodes);
		}
		
//		mon.stop();
		return treeClone;
	}
	
	public ELDescriptionTree cloneOld() {
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
					new TreeSet<NamedClass>(edge.getNode().getLabel()));
			cloneRecursively(edge.getNode(), tmp);
		}
	}

	@Override
	public String toString() {
		return rootNode.toString();
	}
	
	/**
	 * Returns a string of the tree description (without the overhead of converting
	 * the tree into a description).
	 * @return A string for the description the tree stands for.  
	 */
	public String toDescriptionString() {
		return rootNode.toDescriptionString();
	}

	/**
	 * @return the nodes
	 */
	public Collection<ELDescriptionNode> getNodes() {
		return nodes;
	}
	
	public int getDepth() {
		return maxLevel;
	}
	
	/**
	 * size of tree = number of nodes + sum of cardinality of node labels
	 * @return The tree size.
	 */
	public int getSize() {
//		int size = nodes.size();
//		for(ELDescriptionNode node : nodes) {
//			size += node.getLabel().size();
//		}
		return size;
	}
}
