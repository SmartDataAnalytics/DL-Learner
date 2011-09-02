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

package org.dllearner.refinementoperators;

import java.util.Collection;
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

import org.dllearner.algorithms.el.ELDescriptionEdge;
import org.dllearner.algorithms.el.ELDescriptionNode;
import org.dllearner.algorithms.el.ELDescriptionTree;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
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
 *   <li>complete? (still open)</li>
 *   <li>proper</li>
 *   <li>finite</li>
 *   <li>uses class/property hierarchy</li>
 *   <li>takes domain/range into account</li>
 *   <li>uses disjoint classes/classes without common instances</li>
 * </ul>
 * 
 * @author Jens Lehmann
 *
 */
@SuppressWarnings("unused")
public class ELDown extends RefinementOperatorAdapter {

//	private static Logger logger = Logger.getLogger(ELDown.class);	
	
	private AbstractReasonerComponent rs;
	
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
	
	public ELDown(AbstractReasonerComponent rs) {
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
//		System.out.println("Refinements finished.");
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
		return refine(tree, tree.getRootNode(), new Thing(), true);
	}
	
	private Set<ELDescriptionTree> refine(ELDescriptionTree tree, ELDescriptionNode node, Description index, boolean minimize) {
		// the set of all refinements, which we will return
		Set<ELDescriptionTree> refinements = new HashSet<ELDescriptionTree>();
		// the position of the node within the tree (needed for getting
		// the corresponding node in a cloned tree)
		int[] position = node.getCurrentPosition();	
		
		// option 1: label extension
		Set<NamedClass> candidates = utility.getClassCandidates(index, node.getLabel());
		for(NamedClass nc : candidates) {
			// clone operation
			ELDescriptionTree clonedTree = tree.clone();
			ELDescriptionNode clonedNode = clonedTree.getNode(position);
			// extend label
			clonedNode.extendLabel(nc);
			refinements.add(clonedTree);
		}
		
		
		// option 2: label refinement
		// loop through all classes in label
		for(NamedClass nc : node.getLabel()) {
			// find all more special classes for the given label
			for(Description moreSpecial : rs.getSubClasses(nc)) {
				if(moreSpecial instanceof NamedClass) {
					// clone operation
					ELDescriptionTree clonedTree = tree.clone();
					ELDescriptionNode clonedNode = clonedTree.getNode(position);
					
//					System.out.println("tree: " + tree);
//					System.out.println("cloned tree: " + clonedTree);
//					System.out.println("node: " + node);
//					System.out.println("cloned unmodified: " + clonedNode);
					
					// create refinements by replacing class					
					clonedNode.replaceInLabel(nc, (NamedClass) moreSpecial);
					
//					System.out.println("cloned modified: " + clonedNode);
					refinements.add(clonedTree);
				}
			}
		}
		
		// option 3: new edge
		SortedSet<ObjectProperty> appOPs = utility.computeApplicableObjectProperties(index);
		Set<ObjectProperty> mgr = utility.computeMgr(appOPs);
		// temporary set of all concepts, which still have to pass the equivalence check
		Stack<ELDescriptionTree> stack = new Stack<ELDescriptionTree>();
		for(ObjectProperty op : mgr) {
			// clone operation
			ELDescriptionTree clonedTree = tree.clone();
			ELDescriptionNode clonedNode = clonedTree.getNode(position);
			// add a new node and edge
			ELDescriptionNode newNode = new ELDescriptionNode(clonedNode, op, new TreeSet<NamedClass>());
			stack.add(clonedTree);
			
			// recurse if concept is equivalent
			while(stack.size() != 0) {
				// we pick an arbitrary tree and remove it from the stack
				ELDescriptionTree testTree = stack.pop();
				// test equivalence (we found out that we can use the
				// minimality test for equivalence in this case)
				boolean equivalent = !testTree.isMinimal();
				// if the tree is equivalent, we need to populate the
				// stack with refinements (which are later tested for
				// equivalence)
				if(equivalent) {
					// edge refinement
					// we know that the edge we added is the last one for this node
					int edgeNr = node.getEdges().size() - 1;
					ELDescriptionEdge edge = node.getEdges().get(edgeNr);
					// all refinements of this edge are added to the stack
					// (set 1 in article)
					refineEdge(stack, tree, node, position, edgeNr);
					// perform node refinements in non-minimize-mode
					// (set 2 in article)
//					commented out, because didn't make sense to me
//					refinements.addAll(refineEdges(tree, newNode, position));
					stack.addAll(refine(tree, newNode, opRanges.get(edge), false));
				} else {
					// tree is not equivalent, i.e. a proper refinement
					refinements.add(testTree);
				}
			}			
		}
		
		// option 4: edge refinement
		refinements.addAll(refineEdges(tree, node, position));
		
		// option 5: child refinement
		for(ELDescriptionEdge edge : node.getEdges()) {
			// recursive call on child node and property range as index
			Description range = rs.getRange(edge.getLabel());
//			System.out.println(tree + "\nrecurse to:\n"  + edge.getTree());
			refinements.addAll(refine(tree, edge.getNode(), range, minimize));
		}
		
		// we found out that, in case we start from the TOP concept
		// (which is assumed in the current implementation), we can
		// simply throw away all non-minimal concepts
		if(minimize) {
			Iterator<ELDescriptionTree> it = refinements.iterator();
			while(it.hasNext()) {
				if(!it.next().isMinimal()) {
					it.remove();
				}
			}
		}
		
		return refinements;
	}

	private Set<ELDescriptionTree> refineEdges(ELDescriptionTree tree, ELDescriptionNode node, int[] position) {
		Set<ELDescriptionTree> refinements = new HashSet<ELDescriptionTree>();
		for(int edgeNumber = 0; edgeNumber < node.getEdges().size(); edgeNumber++) {
			refineEdge(refinements, tree, node, position, edgeNumber);
		}
		return refinements;
	}
	
	private void refineEdge(Collection<ELDescriptionTree> refinements, ELDescriptionTree tree, ELDescriptionNode node, int[] position, int edgeNumber) {
		ELDescriptionEdge edge = node.getEdges().get(edgeNumber);
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
//				ELDescriptionEdge clonedEdge = clonedTree.getNode(position).getEdges().get(edgeNumber);
//				clonedEdge.setLabel(op2);
				refinements.add(clonedTree);				
			}

		}
	}
	
	// simplifies a potentially nested tree in a flat conjunction by taking
	// the domain of involved roles, e.g. for
	// C = Professor \sqcap \exists hasChild.Student
	// the result would be Professor \sqcap Human (assuming Human is the domain
	// of hasChild)
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

	@Override
	public void init() throws ComponentInitException {
		// TODO Auto-generated method stub
		
	}
	
//	private void computeMg(Description index) {
//		// compute the applicable properties if this has not been done yet
//		if(app.get(index) == null)
//			app.put(index, utility.computeApplicableObjectProperties(index));	
//		
//		mgr.put(index, new TreeSet<ObjectProperty>());
//		
//		
//	}	
	
}
