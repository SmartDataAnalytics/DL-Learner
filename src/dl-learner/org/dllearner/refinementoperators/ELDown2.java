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
package org.dllearner.refinementoperators;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.el.ELDescriptionEdge;
import org.dllearner.algorithms.el.ELDescriptionNode;
import org.dllearner.algorithms.el.ELDescriptionTree;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.Thing;

/**
 * EL downward refinement operator constructed by Jens Lehmann
 * and Christoph Haase. It takes an EL description tree as input
 * and outputs a set of EL description trees.
 * 
 * <p>Properties:
 * <ul>
 *   <li>weakly complete (can be extended to guarantee completeness if desired)</li>
 *   <li>proper</li>
 *   <li>finite</li>
 *   <li>uses class/property hierarchy</li>
 *   <li>takes domain/range into account</li>
 *   <li>uses disjoint classes/classes without common instances</li>
 *   <li>all refinements are minimal (i.e. cannot be shortened without changing semantics)</li>
 * </ul>
 * 
 * @author Jens Lehmann
 *
 */
@SuppressWarnings("unused")
public class ELDown2 extends RefinementOperatorAdapter {

	private static Logger logger = Logger.getLogger(ELDown2.class);	
	
	private ReasonerComponent rs;
	
	// hierarchies
	private ClassHierarchy subsumptionHierarchy;
	private ObjectPropertyHierarchy opHierarchy;
	
	// domains and ranges
	private Map<ObjectProperty,Description> opDomains = new TreeMap<ObjectProperty,Description>();
	private Map<ObjectProperty,Description> opRanges = new TreeMap<ObjectProperty,Description>();
	
	// app_A set of applicable properties for a given class
	private Map<Description, Set<ObjectProperty>> app = new TreeMap<Description, Set<ObjectProperty>>();

	// most general applicable properties
	private Map<Description,Set<ObjectProperty>> mgr = new TreeMap<Description,Set<ObjectProperty>>();

	// utility class
	private Utility utility;
	
	public ELDown2(ReasonerComponent rs) {
		this.rs = rs;
		utility = new Utility(rs);
		subsumptionHierarchy = rs.getClassHierarchy();
		opHierarchy = rs.getObjectPropertyHierarchy();
		
		// query reasoner for domains and ranges
		// (because they are used often in the operator)
		for(ObjectProperty op : rs.getObjectProperties()) {
			opDomains.put(op, rs.getDomain(op));
			opRanges.put(op, rs.getRange(op));
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.refinementoperators.RefinementOperator#refine(org.dllearner.core.owl.Description)
	 */
	@Override
	public Set<Description> refine(Description concept) {
		ELDescriptionTree tree = new ELDescriptionTree(rs, concept);
		Set<ELDescriptionTree> refinementTrees = refine(tree);
		Set<Description> refinements = new HashSet<Description>();
		for(ELDescriptionTree refinementTree : refinementTrees) {
			refinements.add(refinementTree.transformToDescription());
		}
		return refinements;
	}
	
	/**
	 * Performs downward refinement for the given tree. The operator
	 * works directly on EL description trees (which differ from the
	 * the tree structures build by descriptions).
	 * 
	 * @param tree Input EL description tree.
	 * @return Set of refined EL description trees.
	 */
	public Set<ELDescriptionTree> refine(ELDescriptionTree tree) {
		Set<ELDescriptionTree> refinements = new HashSet<ELDescriptionTree>();
		// loop over all nodes of the tree and perform one of the 
		// transformations on it (we make a copy of all nodes, because
		// the transformations can, of course, add new nodes)
		Set<ELDescriptionNode> nodes = new HashSet<ELDescriptionNode>(tree.getNodes());
		for(ELDescriptionNode v : nodes) {
			// the position of the node within the tree (needed for getting
			// the corresponding node in a cloned tree) 
			int[] position = v.getCurrentPosition();	
			
			// perform operations
			refinements.addAll(extendLabel(tree, v, position));
			refinements.addAll(refineLabel(tree, v, position));
			refinements.addAll(refineEdge(tree, v, position));
			refinements.addAll(attachSubtree(tree, v, position));
		}
		
//		return refine(tree, tree.getRootNode(), new Thing(), true);
		return refinements;
	}

	// operation 1: label extension
	private Set<ELDescriptionTree> extendLabel(ELDescriptionTree tree, ELDescriptionNode v, int[] position) {
		Set<ELDescriptionTree> refinements = new HashSet<ELDescriptionTree>();
				
		// the index is the range of role in the edge pointing to the parent of this node
		Description index;
		if(v.isRoot()) {
			index = Thing.instance;
		} else {
			index = opRanges.get(v.getParentEdge().getLabel());
		}
		
		// call ncc (see paper)
		Set<NamedClass> candidates = utility.getClassCandidates(index, v.getLabel());
		
		for(NamedClass nc : candidates) {
			// clone operation
			ELDescriptionTree clonedTree = tree.clone();
			ELDescriptionNode clonedNode = clonedTree.getNode(position);
			// extend label
			clonedNode.extendLabel(nc);
			if(clonedTree.isMinimal()) {
				refinements.add(clonedTree);	
			}
		}
				
		return refinements;
	}	
	
	// operation 2: label refinement
	private Set<ELDescriptionTree> refineLabel(ELDescriptionTree tree, ELDescriptionNode v, int[] position) {
		Set<ELDescriptionTree> refinements = new HashSet<ELDescriptionTree>();
		
		// loop through all classes in label
		for(NamedClass nc : v.getLabel()) {
			// find all more special classes for the given label
			for(Description moreSpecial : rs.getSubClasses(nc)) {
				if(moreSpecial instanceof NamedClass) {
					// clone operation
					ELDescriptionTree clonedTree = tree.clone();
					ELDescriptionNode clonedNode = clonedTree.getNode(position);
					
					// create refinements by replacing class					
					clonedNode.replaceInLabel(nc, (NamedClass) moreSpecial);
					
					if(clonedTree.isMinimal()) {
						refinements.add(clonedTree);	
					}
				}
			}
		}
				
		return refinements;
	}	
	
	// operation 3: refine edge
	private Set<ELDescriptionTree> refineEdge(ELDescriptionTree tree, ELDescriptionNode v, int[] position) {
		Set<ELDescriptionTree> refinements = new HashSet<ELDescriptionTree>();

		for(int edgeNumber = 0; edgeNumber < v.getEdges().size(); edgeNumber++) {
			ELDescriptionEdge edge = v.getEdges().get(edgeNumber);
			ObjectProperty op = edge.getLabel();
			// find all more special properties
			for(ObjectProperty op2 : rs.getSubProperties(op)) {
				// we check whether the range of this property is not disjoint
				// with the existing child node (we do not perform a full disjointness
				// check, but only compare with the flattened concept to keep the number
				// of possible disjointness checks finite)
				if(!utility.isDisjoint(getFlattenedConcept(edge.getNode()), opRanges.get(op2))) {
					// clone operation
					ELDescriptionTree clonedTree = tree.clone();
					// find cloned edge and replace its label
					clonedTree.getNode(position).refineEdge(edgeNumber, op2);
//					ELDescriptionEdge clonedEdge = clonedTree.getNode(position).getEdges().get(edgeNumber);
//					clonedEdge.setLabel(op2);
					if(clonedTree.isMinimal()) {
						refinements.add(clonedTree);	
					}
				}
			}	
		}		
		
		return refinements;
	}
	
	// operation 4: attach tree
	private Set<ELDescriptionTree> attachSubtree(ELDescriptionTree tree, ELDescriptionNode v, int[] position) {
		Set<ELDescriptionTree> refinements = new HashSet<ELDescriptionTree>();
		
		// compute the set of most general roles such that the domain of each role is not disjoint
		// with the range of the role pointing to this node
		Description index;
		if(v.isRoot()) {
			index = Thing.instance;
		} else {
			index = opRanges.get(v.getParentEdge().getLabel());
		}
		SortedSet<ObjectProperty> appOPs = utility.computeApplicableObjectProperties(index);
		Set<ObjectProperty> mgr = utility.computeMgr(appOPs);
		
		// TODO: in as ist ein baum t nicht definiert; ersetzen durch t_{C'}
		// TODO: Einrückung in as nach pick element nicht notwendig
		
		// loop through most general roles
		for(ObjectProperty op : mgr) {
			
			// a list of subtrees (stored as edges i.e. role + root node which points to tree)
			// TODO: Do we need to store m at all?
			LinkedList<ELDescriptionEdge> m = new LinkedList<ELDescriptionEdge>();
			
			// create tree corresponding to top node
			ELDescriptionTree topTree = new ELDescriptionTree(rs, Thing.instance);
			
			// init list with picked role and top node i.e. its root
			m.add(new ELDescriptionEdge(op, topTree.getRootNode()));
			
			// iterate until m is empty
			while(!m.isEmpty()) {
				// pick and remove first element
				ELDescriptionEdge edge = m.pollFirst();
				ObjectProperty r = edge.getLabel();
				// tp = t' in algorithm description (p stands for prime)
				ELDescriptionTree tp = edge.getNode().getTree();
				
				// merge tree into main tree
				ELDescriptionTree mergedTree = mergeTrees(tree, v, position, r, tp);
				
				// we check equivalence by a minimality test (TODO: can we still do this?)
				if(mergedTree.isMinimal()) {
					// it is not equivalent, i.e. we found a refinement
					refinements.add(mergedTree);
				} else {					
					// perform complex check
					boolean check = asCheck(v);
					
					if(check) {
						// refine property
						for(ObjectProperty subRole : rs.getSubProperties(r)) {
							m.add(new ELDescriptionEdge(subRole, tp.getRootNode()));
						}
						// refine tree using recursive operator call
						Set<ELDescriptionTree> recRefs = refine(tp);
						for(ELDescriptionTree tpp : recRefs) {
							m.add(new ELDescriptionEdge(r, tpp.getRootNode()));
						}
					}
				}		
			}
		}
				
		return refinements;
	}	
	
	// create a new tree which is obtained by attaching the new tree at the given node in the tree via role r
	private ELDescriptionTree mergeTrees(ELDescriptionTree tree, ELDescriptionNode node, int[] position, ObjectProperty r, ELDescriptionTree newTree) {
		// merged tree = tree + new node with role pointing to a new node
		ELDescriptionTree mergedTree = tree.clone();
		ELDescriptionNode clonedNode = mergedTree.getNode(position);
//		ELDescriptionNode nodeNew = new ELDescriptionNode(clonedNode, r);
		
		// create a list of nodes we still need to process
		LinkedList<ELDescriptionNode> toProcess = new LinkedList<ELDescriptionNode>();
		toProcess.add(newTree.getRootNode());
		
		// map from nodes to cloned nodes
		Map<ELDescriptionNode,ELDescriptionNode> cloneMap = new HashMap<ELDescriptionNode,ELDescriptionNode>();
//		cloneMap.put(newTree.getRootNode(), nodeNew);
		
		// loop until the process list is empty
		while(!toProcess.isEmpty()) {
			// process a node
			ELDescriptionNode v = toProcess.pollFirst();
			// find parent
			ELDescriptionNode vp;
			if(v.isRoot()) {
				// root is connected to main tree via role r
				vp = new ELDescriptionNode(clonedNode, r);
			} else {
				ELDescriptionNode parent = cloneMap.get(v.getParent());
				ObjectProperty role = v.getParentEdge().getLabel();
				Set<NamedClass> label = v.getLabel();
				// create new node
				vp = new ELDescriptionNode(parent, role, label);				
			}
			cloneMap.put(v, vp);
			// attach children of node to process list
			for(ELDescriptionEdge edge : v.getEdges()) {
				toProcess.add(edge.getNode());
			}
		}
		
		return mergedTree;
	}
	
	private boolean asCheck(ELDescriptionNode v) {
		// find all edges up to the root node
		List<ELDescriptionEdge> piVEdges = new LinkedList<ELDescriptionEdge>();
		ELDescriptionNode tmp = v;
		while(!tmp.isRoot()) {
			piVEdges.add(tmp.getParentEdge());
			tmp = tmp.getParent();
		}
		
		// go through all edges
		for(ELDescriptionEdge piVEdge : piVEdges) {
			// collect (w,s,w')
			ELDescriptionNode wp = piVEdge.getNode();
			ObjectProperty s = piVEdge.getLabel();
			ELDescriptionNode w = wp.getParent();
			
			// go through all (w,s,w'') - TODO: s or a new s' ?
			for(ELDescriptionEdge wEdge : w.getEdges()) {
				ObjectProperty sp = wEdge.getLabel();
				ELDescriptionNode wpp = wEdge.getNode();
				if(s.equals(sp) && wp != wpp) {
					if(wp.getIn().contains(wpp)) {
						return false;
					}
				}
			}
		}
		
		return true;
	}
	
	// simplifies a potentially nested tree in a flat conjunction by taking
	// the domain of involved roles, e.g. for
	// C = Professor \sqcap \exists hasChild.Student
	// the result would be Professor \sqcap Human (assuming Human is the domain
	// of hasChild)
	// TODO: used in both EL operators => move to utility class
	private Description getFlattenedConcept(ELDescriptionNode node) {
		Intersection i = new Intersection();
		
		// add all named classes to intersection
		for(NamedClass nc : node.getLabel()) {
			i.addChild(nc);
		}
		// add domain of all roles to intersection
		for(ELDescriptionEdge edge : node.getEdges()) {
			i.addChild(opDomains.get(edge.getLabel()));
		}
		
		// if the intersection has just one element, we return
		// the element itself instead
		if(i.getChildren().size() == 1) {
			return i.getChild(0);
		}
		
		return i;
	}	
	
}
