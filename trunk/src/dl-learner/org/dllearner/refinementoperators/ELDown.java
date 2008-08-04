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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.dllearner.algorithms.el.ELDescriptionNode;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.core.owl.SubsumptionHierarchy;
import org.dllearner.core.owl.Thing;

/**
 * EL downward refinement operator constructed by Jens Lehmann
 * and Christoph Haase. It takes an EL description tree as input
 * and outputs a set of EL description trees.
 * 
 * <p>Properties:
 * <ul>
 *   <li>complete</li>
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
	
	private ReasoningService rs;
	
	// hierarchies
	private SubsumptionHierarchy subsumptionHierarchy;
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
	
	public ELDown(ReasoningService rs) {
		utility = new Utility(rs);
		subsumptionHierarchy = rs.getSubsumptionHierarchy();
		opHierarchy = rs.getRoleHierarchy();
		
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
		// TODO according to the specification, we need to minimise 
		// the tree (not yet implemented)
		ELDescriptionNode tree = new ELDescriptionNode(concept);
		Set<ELDescriptionNode> refinementTrees = refine(tree);
		Set<Description> refinements = new HashSet<Description>();
		for(ELDescriptionNode refinementTree : refinementTrees) {
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
	public Set<ELDescriptionNode> refine(ELDescriptionNode tree) {
		return refine(tree, new Thing());
	}
	
	private Set<ELDescriptionNode> refine(ELDescriptionNode tree, Description index) {
		Set<ELDescriptionNode> refinements = new HashSet<ELDescriptionNode>(); 
		// option 1: label extension
		
		// option 2: label refinement
		// loop through all classes in label
		for(NamedClass nc : tree.getLabel()) {
			// find all more special classes for the given label
			for(Description moreSpecial : rs.getMoreSpecialConcepts(nc)) {
				if(moreSpecial instanceof NamedClass) {
					// create refinements by replacing class
					ELDescriptionNode tmp = tree.clone();
					tmp.replaceInLabel(nc, (NamedClass) moreSpecial);
					refinements.add(tmp);
				}
			}
		}
		
		// option 3: new edge
		
		// option 4: edge refinement
		
		// option 5: child refinement
		
		return refinements;
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
