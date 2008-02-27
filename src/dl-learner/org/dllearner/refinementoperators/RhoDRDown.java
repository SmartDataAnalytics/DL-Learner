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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.dllearner.algorithms.refinement.RefinementOperator;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.SubsumptionHierarchy;
import org.dllearner.core.owl.Thing;

/**
 * A downward refinement operator, which makes use of domains
 * and ranges of properties. The operator is currently under
 * development. Its aim is to span a much "cleaner" and smaller search
 * tree compared to RhoDown by omitting many class descriptions,
 * which are obviously too weak, because they violate 
 * domain/range restrictions.
 * 
 * @author Jens Lehmann
 *
 */
public class RhoDRDown implements RefinementOperator {

	private ReasoningService rs;
	
	// hierarchies
	private SubsumptionHierarchy subHierarchy;
	
	// domains and ranges
	private Map<ObjectProperty,Description> opDomains = new TreeMap<ObjectProperty,Description>();
	private Map<DatatypeProperty,Description> dpDomains = new TreeMap<DatatypeProperty,Description>();
	private Map<ObjectProperty,Description> opRanges = new TreeMap<ObjectProperty,Description>();
	
	public RhoDRDown(ReasoningService rs) {
		this.rs = rs;
		subHierarchy = rs.getSubsumptionHierarchy();
		
		// query reasoner for domains and ranges
		// (because they are used often in the operator)
		for(ObjectProperty op : rs.getAtomicRoles()) {
			opDomains.put(op, rs.getDomain(op));
			opRanges.put(op, rs.getRange(op));
		}
		for(DatatypeProperty dp : rs.getDatatypeProperties()) {
			dpDomains.put(dp, rs.getDomain(dp));
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.refinement.RefinementOperator#refine(org.dllearner.core.owl.Description)
	 */
	public Set<Description> refine(Description concept) {
		throw new RuntimeException();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.refinement.RefinementOperator#refine(org.dllearner.core.owl.Description, int, java.util.List)
	 */
	public Set<Description> refine(Description description, int maxLength,
			List<Description> knownRefinements) {
		return refine(description, maxLength, knownRefinements, new Thing());
	}

	public Set<Description> refine(Description description, int maxLength,
			List<Description> knownRefinements, Description currDomain) {
		// TODO: check whether using list or set makes more sense 
		// here; and whether HashSet or TreeSet should be used
		Set<Description> refinements = new HashSet<Description>();
		
		// .. do most general rules here ...
		// (easier because it may be possible to add return 
		// statements instead of going through the complete 
		// function)
		
		if(description instanceof Thing) {
			refinements.addAll(subHierarchy.getMoreSpecialConcepts(description));
		} else if(description instanceof Nothing) {
			// cannot be further refined
		} else if(description instanceof NamedClass) {
			refinements.addAll(subHierarchy.getMoreSpecialConcepts(description));
		}
		
		return refinements;		
	}
	
}
