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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.elcopy.ELDescriptionEdge;
import org.dllearner.algorithms.elcopy.ELDescriptionEdgeComparator;
import org.dllearner.algorithms.elcopy.ELDescriptionNode;
import org.dllearner.algorithms.elcopy.ELDescriptionTree;
import org.dllearner.algorithms.elcopy.ELDescriptionTreeComparator;
import org.dllearner.algorithms.elcopy.TreeAndRoleSet;
import org.dllearner.algorithms.elcopy.TreeAndRoleSetComparator;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.DataRange;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypePropertyHierarchy;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.core.owl.Property;
import org.dllearner.core.owl.Thing;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

//import com.jamonapi.Monitor;
//import com.jamonapi.MonitorFactory;

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
public class ELDown3 extends RefinementOperatorAdapter {

	private static Logger logger = Logger.getLogger(ELDown3.class);	
	
	private AbstractReasonerComponent rs;
	
	// hierarchies
	private ClassHierarchy subsumptionHierarchy;
	private ObjectPropertyHierarchy opHierarchy;
	private DatatypePropertyHierarchy dpHierarchy;
	
	// domains and ranges
	private Map<ObjectProperty,Description> opDomains = new TreeMap<ObjectProperty,Description>();
	private Map<ObjectProperty,Description> opRanges = new TreeMap<ObjectProperty,Description>();
	private Map<DatatypeProperty,Description> dpDomains = new TreeMap<DatatypeProperty,Description>();
	private Map<DatatypeProperty,DataRange> dpRanges = new TreeMap<DatatypeProperty,DataRange>();
	
	// app_A set of applicable properties for a given class
	private Map<Description, Set<ObjectProperty>> appOP = new TreeMap<Description, Set<ObjectProperty>>();
	private Map<Description, Set<DatatypeProperty>> appDP = new TreeMap<Description, Set<DatatypeProperty>>();

	// most general applicable properties
	private Map<Description,Set<ObjectProperty>> mgrOP = new TreeMap<Description,Set<ObjectProperty>>();
	private Map<Description,Set<DatatypeProperty>> mgrDP = new TreeMap<Description,Set<DatatypeProperty>>();

	// utility class
	private Utility utility;
	
	// comparators
	private ELDescriptionTreeComparator treeComp = new ELDescriptionTreeComparator();
	private ELDescriptionEdgeComparator edgeComp = new ELDescriptionEdgeComparator();
	private TreeAndRoleSetComparator mComp = new TreeAndRoleSetComparator();
	
	private int maxClassExpressionDepth = 2;

	public ELDown3(AbstractReasonerComponent rs) {
		this(rs, true);
	}
	
	public ELDown3(AbstractReasonerComponent rs, boolean instanceBasedDisjoints) {
		this.rs = rs;
		subsumptionHierarchy = rs.getClassHierarchy();
		opHierarchy = rs.getObjectPropertyHierarchy();
		dpHierarchy = rs.getDatatypePropertyHierarchy();
		
		// query reasoner for domains and ranges
		// (because they are used often in the operator)
		for(ObjectProperty op : rs.getObjectProperties()) {
			opDomains.put(op, rs.getDomain(op));
			opRanges.put(op, rs.getRange(op));
		}	
		for(DatatypeProperty dp : rs.getDatatypeProperties()) {
			dpDomains.put(dp, rs.getDomain(dp));
			dpRanges.put(dp, rs.getRange(dp));
		}
		
		utility = new Utility(rs, opDomains, dpDomains, instanceBasedDisjoints);
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.refinementoperators.RefinementOperator#refine(org.dllearner.core.owl.Description)
	 */
	@Override
	public Set<Description> refine(Description concept) {
		logger.trace("refining " + concept);
		ELDescriptionTree tree = new ELDescriptionTree(rs, concept);
		List<ELDescriptionTree> refinementTrees = refine(tree);
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
	public List<ELDescriptionTree> refine(ELDescriptionTree tree) {
		logger.trace("applying \\rho on " + tree.toDescriptionString());
		List<ELDescriptionTree> refinements = new LinkedList<ELDescriptionTree>();
		// loop over all nodes of the tree and perform one of the 
		// transformations on it (we make a copy of all nodes, because
		// the transformations can, of course, add new nodes)
		List<ELDescriptionNode> nodes = new LinkedList<ELDescriptionNode>(tree.getNodes());
		for(ELDescriptionNode v : nodes) {
			logger.trace("picked node v: " + v);
			
			// the position of the node within the tree (needed for getting
			// the corresponding node in a cloned tree) 
			int[] position = v.getCurrentPosition();	
//			logger.trace("  at position " + Helper.arrayContent(position));
			
			// perform operations
			if(v.isClassNode()){
				refinements.addAll(extendLabel(tree, v, position));
				refinements.addAll(refineLabel(tree, v, position));
			}
			refinements.addAll(refineEdge(tree, v, position));
			if(v.isClassNode() && v.getLevel() <= maxClassExpressionDepth){
				refinements.addAll(attachSubtree2(tree, v, position));
				refinements.addAll(attachSubtreeDatatypeProperties(tree, v, position));
			}
			
		}
		
		return refinements;
	}

	// operation 1: label extension
	private List<ELDescriptionTree> extendLabel(ELDescriptionTree tree, ELDescriptionNode v, int[] position) {
//		Monitor mon = MonitorFactory.start("extend label");
		List<ELDescriptionTree> refinements = new LinkedList<ELDescriptionTree>();
				
		// the index is the range of role in the edge pointing to the parent of this node
		Description index;
		if(v.isRoot()) {
			index = Thing.instance;
		} else {
			index = opRanges.get(v.getParentEdge().getLabel());
		}
		
		// call ncc (see paper)
		Set<NamedClass> candidates = utility.getClassCandidates(index, v.getLabel());
		
//		System.out.println("index: " + index + " label: " + v.getLabel());
//		System.out.println("candidates: " + candidates);
		
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
				
//		mon.stop();
		return refinements;
	}	
	
	// operation 2: label refinement
	private List<ELDescriptionTree> refineLabel(ELDescriptionTree tree, ELDescriptionNode v, int[] position) {
//		Monitor mon = MonitorFactory.start("refine label");
		List<ELDescriptionTree> refinements = new LinkedList<ELDescriptionTree>();
		
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
//		mon.stop();
		return refinements;
	}	
	
	// operation 3: refine edge
	private List<ELDescriptionTree> refineEdge(ELDescriptionTree tree, ELDescriptionNode v, int[] position) {
//		Monitor mon = MonitorFactory.start("refine edge");
		List<ELDescriptionTree> refinements = new LinkedList<ELDescriptionTree>();

		for(int edgeNumber = 0; edgeNumber < v.getEdges().size(); edgeNumber++) {
			ELDescriptionEdge edge = v.getEdges().get(edgeNumber);
			Property op = edge.getLabel();
			// find all more special properties
			if(op instanceof ObjectProperty){
				for(ObjectProperty op2 : rs.getSubProperties((ObjectProperty) op)) {
					// we check whether the range of this property is not disjoint
					// with the existing child node (we do not perform a full disjointness
					// check, but only compare with the flattened concept to keep the number
					// of possible disjointness checks finite)
					if(!utility.isDisjoint(getFlattenedConcept(edge.getNode()), opRanges.get(op2))) {
						// clone operation
						ELDescriptionTree clonedTree = tree.clone();
						// find cloned edge and replace its label
						clonedTree.getNode(position).refineEdge(edgeNumber, op2);
//						ELDescriptionEdge clonedEdge = clonedTree.getNode(position).getEdges().get(edgeNumber);
//						clonedEdge.setLabel(op2);
						if(clonedTree.isMinimal()) {
							refinements.add(clonedTree);	
						}
					}
				}	
			} else {
				for(DatatypeProperty op2 : rs.getSubProperties((DatatypeProperty) op)) {
					// we check whether the range of this property is not disjoint
					// with the existing child node
					if(edge.getNode().getDataRange().equals(dpRanges.get(op2))) {
						// clone operation
						ELDescriptionTree clonedTree = tree.clone();
						// find cloned edge and replace its label
						clonedTree.getNode(position).refineEdge(edgeNumber, op2);
//						ELDescriptionEdge clonedEdge = clonedTree.getNode(position).getEdges().get(edgeNumber);
//						clonedEdge.setLabel(op2);
						if(clonedTree.isMinimal()) {
							refinements.add(clonedTree);	
						}
					}
				}	
			}
			
		}		
//		mon.stop();
		return refinements;
	}
	
	
	
	// new version of as
	private Collection<ELDescriptionTree> attachSubtree2(ELDescriptionTree tree, ELDescriptionNode v, int[] position) {
//		Monitor mon = MonitorFactory.start("attach tree");
		Set<ELDescriptionTree> refinements = new TreeSet<ELDescriptionTree>(treeComp);
		
		// create and initialise M
		TreeSet<TreeAndRoleSet> m = new TreeSet<TreeAndRoleSet>(mComp);
		ELDescriptionTree topTree = new ELDescriptionTree(rs, Thing.instance);
		Description index = getIndex(v);
		SortedSet<? extends Property> appOPs = utility.computeApplicableObjectProperties(index);
		m.add(new TreeAndRoleSet(topTree, (Set<Property>) appOPs));
		
//		logger.trace("M initialised: " + m);
		
		while(!m.isEmpty()) {
			
			// pick first element of M
			TreeAndRoleSet tars = m.pollFirst();
			ELDescriptionTree tp = tars.getTree();
			Set<Property> rSet = tars.getRoles();
//			logger.trace("selected first element of M: " + tars);
			
			
			// init sets R' and R''
			// more efficient
			Set<ObjectProperty> rpSet = utility.computeMgr((Set<ObjectProperty>) appOPs);
			rpSet.retainAll(rSet);
//			SortedSet<ObjectProperty> rpSet = new TreeSet<ObjectProperty>();
//			for(ObjectProperty rEl : rSet) {
//				if(!containsSuperProperty(rEl, rSet)) {
//					rpSet.add(rEl);
//				}
//			}
			
//			logger.trace("R': " + rpSet);
			Set<Property> rppSet = new TreeSet<Property>();
			
			while(!rpSet.isEmpty()) {
				// pick an element r from R'
				Iterator<ObjectProperty> it = rpSet.iterator();
				ObjectProperty r = it.next();
				it.remove();
//				logger.trace("picked role r: " + r);
				ELDescriptionTree tpp = mergeTrees(tree, v, position, r, tp);
//				logger.trace("merged tree:\n" + tpp);
				// the position of w is the position of v + #edges outgoing from v
				int[] wPosition = new int[position.length+1];
				System.arraycopy(position, 0, wPosition, 0, position.length);
				wPosition[position.length] = v.getEdges().size();
				ELDescriptionNode w = tpp.getNode(wPosition);				
				
				boolean minimal = tpp.isMinimal();
//				MonitorFactory.add("as.minimal", "boolean", minimal ? 1 : 0);
				if(minimal) {
					refinements.add(tpp);
//					logger.trace("tree is minimal; added to T");
				} else {
					boolean check = asCheck(w);
//					MonitorFactory.add("as.check", "boolean", check ? 1 : 0);					
//					logger.trace("tree is not minimal; result of complex check: " + check);
					
					if(check) {
						
//						Monitor mon2 = MonitorFactory.start("as.tmp");
						// add role to R' if it is in R (allowed)
						for(ObjectProperty subRole : rs.getSubProperties(r)) {
							if(rSet.contains(subRole)) {
								rpSet.add(subRole);
							}
						}
						rppSet.add(r);
//						logger.trace("updated R' to: " + rpSet);
//						logger.trace("updated R'' to: " + rppSet);
//						mon2.stop();
					}
				}
			}
			
			if(rppSet.size() != 0) {
				// recursive call
//				mon.stop();
//				logger.trace("recursive call start");
				List<ELDescriptionTree> recRefs = refine(tp);
//				logger.trace("recursive call end");
//				mon.start();				
				
				for(ELDescriptionTree tStar : recRefs) {
					m.add(new TreeAndRoleSet(tStar, rppSet));
				}	
//				logger.trace("M after recursion: " + m);
			}
				
		}
//		mon.stop();
		return refinements;		
	}
	
	// new version of as
		private Collection<ELDescriptionTree> attachSubtreeDatatypeProperties(ELDescriptionTree tree, ELDescriptionNode v, int[] position) {
//			Monitor mon = MonitorFactory.start("attach tree");
			Set<ELDescriptionTree> refinements = new TreeSet<ELDescriptionTree>(treeComp);
			// create and initialise M
			TreeSet<TreeAndRoleSet> m = new TreeSet<TreeAndRoleSet>(mComp);
			ELDescriptionTree topTree = new ELDescriptionTree(rs, Thing.instance);
			Description index = getIndex(v);
			SortedSet<? extends Property> appOPs = utility.computeApplicableDatatypeProperties(index);
			m.add(new TreeAndRoleSet(topTree, (Set<Property>) appOPs));
			
//			logger.trace("M initialised: " + m);
			
			while(!m.isEmpty()) {
				
				// pick first element of M
				TreeAndRoleSet tars = m.pollFirst();
				ELDescriptionTree tp = tars.getTree();
				Set<Property> rSet = tars.getRoles();
//				logger.trace("selected first element of M: " + tars);
				
				
				// init sets R' and R''
				// more efficient
				Set<DatatypeProperty> rpSet = utility.computeMgrDP((Set<DatatypeProperty>) appOPs);
				rpSet.retainAll(rSet);
//				SortedSet<ObjectProperty> rpSet = new TreeSet<ObjectProperty>();
//				for(ObjectProperty rEl : rSet) {
//					if(!containsSuperProperty(rEl, rSet)) {
//						rpSet.add(rEl);
//					}
//				}
				
//				logger.trace("R': " + rpSet);
				Set<Property> rppSet = new TreeSet<Property>();
				
				while(!rpSet.isEmpty()) {
					// pick an element r from R'
					Iterator<DatatypeProperty> it = rpSet.iterator();
					DatatypeProperty r = it.next();
					it.remove();
//					logger.trace("picked role r: " + r);
					ELDescriptionTree tpp = mergeTrees(tree, v, position, r, tp);
//					logger.trace("merged tree:\n" + tpp);
					// the position of w is the position of v + #edges outgoing from v
					int[] wPosition = new int[position.length+1];
					System.arraycopy(position, 0, wPosition, 0, position.length);
					wPosition[position.length] = v.getEdges().size();
					ELDescriptionNode w = tpp.getNode(wPosition);				
					
					boolean minimal = tpp.isMinimal();
//					MonitorFactory.add("as.minimal", "boolean", minimal ? 1 : 0);
					if(minimal) {
						refinements.add(tpp);
//						logger.trace("tree is minimal; added to T");
					} else {
						boolean check = asCheck(w);
//						MonitorFactory.add("as.check", "boolean", check ? 1 : 0);					
//						logger.trace("tree is not minimal; result of complex check: " + check);
						
						if(check) {
							
//							Monitor mon2 = MonitorFactory.start("as.tmp");
							// add role to R' if it is in R (allowed)
							for(DatatypeProperty subRole : rs.getSubProperties(r)) {
								if(rSet.contains(subRole)) {
									rpSet.add(subRole);
								}
							}
							rppSet.add(r);
//							logger.trace("updated R' to: " + rpSet);
//							logger.trace("updated R'' to: " + rppSet);
//							mon2.stop();
						}
					}
				}
				
				if(rppSet.size() != 0) {
					// recursive call
//					mon.stop();
//					logger.trace("recursive call start");
					List<ELDescriptionTree> recRefs = refine(tp);
//					logger.trace("recursive call end");
//					mon.start();				
					
					for(ELDescriptionTree tStar : recRefs) {
						m.add(new TreeAndRoleSet(tStar, rppSet));
					}	
//					logger.trace("M after recursion: " + m);
				}
					
			}
//			mon.stop();
			return refinements;		
		}
			
	
	// create a new tree which is obtained by attaching the new tree at the given node in the tree via role r
	private ELDescriptionTree mergeTrees(ELDescriptionTree tree, ELDescriptionNode node, int[] position, Property r, ELDescriptionTree newTree) {
//		Monitor mon = MonitorFactory.start("as.merge trees");
//		System.out.println("merge start");
//		System.out.println(tree);
//		System.out.println(newTree);
		// merged tree = tree + new node with role pointing to a new node
		ELDescriptionTree mergedTree = tree.clone();
		ELDescriptionNode clonedNode = mergedTree.getNode(position);
//		ELDescriptionNode nodeNew = new ELDescriptionNode(clonedNode, r);
//		logger.trace("node: " + node);
//		logger.trace("cloned node: " + clonedNode);
//		logger.trace("node position: " + arrayContent(position));
//		logger.trace("merge start: " + mergedTree);
		
		// create a list of nodes we still need to process
		LinkedList<ELDescriptionNode> toProcess = new LinkedList<ELDescriptionNode>();
		toProcess.add(newTree.getRootNode());
		
		// map from nodes to cloned nodes
		Map<ELDescriptionNode,ELDescriptionNode> cloneMap = new HashMap<ELDescriptionNode,ELDescriptionNode>();
		
//		Monitor mon2 = MonitorFactory.start("as.tmp");
		
		// loop until the process list is empty
		while(!toProcess.isEmpty()) {
			// process a node
			ELDescriptionNode v = toProcess.pollFirst();
			// find parent
			ELDescriptionNode vp;
			if(v.isRoot()) {
				// root is connected to main tree via role r
				if(r instanceof ObjectProperty){
					vp = new ELDescriptionNode(clonedNode, r, newTree.getRootNode().getLabel());
				} else {
					vp = new ELDescriptionNode(clonedNode, r, dpRanges.get(r));
				}
				
			} else if(v.isClassNode()){
				ELDescriptionNode parent = cloneMap.get(v.getParent());
				Property role = v.getParentEdge().getLabel();
				Set<NamedClass> label = v.getLabel();
				// create new node
				vp = new ELDescriptionNode(parent, role, label);				
			} else {
				ELDescriptionNode parent = cloneMap.get(v.getParent());
				Property role = v.getParentEdge().getLabel();
				DataRange label = v.getDataRange();
				// create new node
				vp = new ELDescriptionNode(parent, role, label);
			}
			cloneMap.put(v, vp);
			// attach children of node to process list
			for(ELDescriptionEdge edge : v.getEdges()) {
				toProcess.add(edge.getNode());
			}
		}
		
//		mon2.stop();
		
//		mon.stop();
		return mergedTree;
	}
	
	// TODO: variables have been renamed in article
	public boolean asCheck(ELDescriptionNode v) {
//		Monitor mon = MonitorFactory.start("as.complex check");
//		System.out.println("asCheck: " + v.getTree().toSimulationString());
		
		// find all edges up to the root node
		List<ELDescriptionEdge> piVEdges = new LinkedList<ELDescriptionEdge>();
		ELDescriptionNode tmp = v;
		while(!tmp.isRoot()) {
			piVEdges.add(tmp.getParentEdge());
			tmp = tmp.getParent();
		}
		
//		System.out.println(piVEdges);
		
		// go through all edges
		for(ELDescriptionEdge piVEdge : piVEdges) {
			// collect (w,r',w')
			ELDescriptionNode wp = piVEdge.getNode();
			Property rp = piVEdge.getLabel();
			ELDescriptionNode w = wp.getParent();
			
//			System.out.println("w: " + w);
//			System.out.println("rp: " + rp);
//			System.out.println("wp: " + wp);
			
			// go through all (w,s,w'')
			for(ELDescriptionEdge wEdge : w.getEdges()) {
				Property rpp = wEdge.getLabel();
				ELDescriptionNode wpp = wEdge.getNode();
				if(wp != wpp && rs.isSubPropertyOf(rp, rpp)) {
//					System.out.println("wp: " + wp);
//					System.out.println("wpp: " + wpp);
					if(wp.getIn().contains(wpp)) {
						return false;
					}
				}
			}
		}
		
//		mon.stop();
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
		
		int size = i.getChildren().size();
		// size = 0 means we have the top concept
		if(size == 0) {
			return Thing.instance;
		}
		// if the intersection has just one element, we return
		// the element itself instead
		else if(size == 1) {
			return i.getChild(0);
		}
		
		return i;
	}	
	
	private Description getIndex(ELDescriptionNode v) {
		if(v.isRoot()) {
			return Thing.instance;
		} else {
			return opRanges.get(v.getParentEdge().getLabel());
		}		
	}
	
	private boolean containsSuperProperty(ObjectProperty prop, Set<ObjectProperty> props) {
		for(ObjectProperty p : props) {
			if(!p.equals(prop)) {
				if(opHierarchy.isSubpropertyOf(prop, p)) {
					return true;
				}						
			}
		}
		return false;
	}
	
	/**
	 * @param maxClassExpressionDepth the maxClassExpressionDepth to set
	 */
	public void setMaxClassExpressionDepth(int maxClassExpressionDepth) {
		this.maxClassExpressionDepth = maxClassExpressionDepth;
	}

	@Override
	public void init() throws ComponentInitException {
		// TODO Auto-generated method stub
		
	}
	
}
