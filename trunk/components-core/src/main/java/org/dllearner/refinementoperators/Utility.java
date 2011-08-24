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

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.Thing;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.owl.ConceptComparator;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * Utility methods for constructing refinement operators.
 * 
 * @author Jens Lehmann
 *
 */
public final class Utility {
		
	private AbstractReasonerComponent reasoner;
	ClassHierarchy sh; 
	private Map<ObjectProperty,Description> opDomains;
	
	// concept comparator
	private ConceptComparator conceptComparator = new ConceptComparator();	
	
	// specifies whether to do real disjoint tests or check that
	// two named classes do not have common instances
	private boolean instanceBasedDisjoints = true;	
	
	// cache for reasoner queries
	private Map<Description,Map<Description,Boolean>> cachedDisjoints = new TreeMap<Description,Map<Description,Boolean>>(conceptComparator);
		
	// cache for applicaple object properties
	private Map<Description, SortedSet<ObjectProperty>> appOPCache = new TreeMap<Description, SortedSet<ObjectProperty>>(conceptComparator);
	
	public Utility(AbstractReasonerComponent rs) {
		throw new Error("not implemented yet");
	}
	
	public Utility(AbstractReasonerComponent rs, Map<ObjectProperty,Description> opDomains, boolean instanceBasedDisjoints) {
		this.reasoner = rs;
		sh = rs.getClassHierarchy();
		// we cache object property domains
		this.opDomains = opDomains;
		this.instanceBasedDisjoints = instanceBasedDisjoints;
	}
	
	/**
	 * Compute the set of applicable object properties for a 
	 * given description. 
	 * 
	 * @param index The index is a description which determines
	 * which of the properties are applicable. Exactly those which
	 * where the index and property domain are not disjoint are 
	 * applicable, where disjoint is defined by {@link #isDisjoint(Description, Description)}.
	 * 
	 */
	public SortedSet<ObjectProperty> computeApplicableObjectProperties(Description index) {
		// use a cache, because large ontologies can have many object properties
		SortedSet<ObjectProperty> applicableObjectProperties = appOPCache.get(index);
		if(applicableObjectProperties == null) {
			Set<ObjectProperty> objectProperties = reasoner.getObjectProperties();
			applicableObjectProperties = new TreeSet<ObjectProperty>();
			for(ObjectProperty op : objectProperties) {
				Description domain = opDomains.get(op);
				if(!isDisjoint(index,domain)) {
					applicableObjectProperties.add(op);
				}
			}
			appOPCache.put(index, applicableObjectProperties);
		}
		return applicableObjectProperties;		
	}
	
	/**
	 * Given a set of applicable object properties, this method returns
	 * the most general ones, i.e. those where more general ones do not
	 * exist in the set of applicable properties. Due to the definition
	 * of "applicable", the returned set is just the intersection of the most
	 * general object properties and the applicable properties. (A non-applicable
	 * property cannot have applicable subproperties, because subproperties
	 * can only restrict, but not broaden their domain.)
	 * 
	 * @param applicableObjectProperties The set of applicable properties.
	 * @return The most general applicable properties.
	 */
	public Set<ObjectProperty> computeMgr(Set<ObjectProperty> applicableObjectProperties) {
		return Helper.intersection(reasoner.getMostGeneralProperties(), applicableObjectProperties);
	}
	
	public Set<NamedClass> getClassCandidates(Description index, Set<NamedClass> existingClasses) {
		return getClassCandidatesRecursive(index, existingClasses, Thing.instance);
	}
	
	private Set<NamedClass> getClassCandidatesRecursive(Description index, Set<NamedClass> existingClasses, Description upperClass) {
		Set<NamedClass> candidates = new TreeSet<NamedClass>();
		
		// we descend the subsumption hierarchy to ensure that we get
		// the most general concepts satisfying the criteria
		// there are 4 checks a class has to satisfy to get into the set;
		// for 2 of them we can stop further traversal in the subsumption
		// hierarchy
		for(Description d : sh.getSubClasses(upperClass)) {
//			System.out.println("d: " + d);
			// owl:Nothing is never a candidate (not in EL)
			if(!(d instanceof Nothing)) {
				NamedClass candidate = (NamedClass) d;
				// we first do those checks where we know that we do not
				// need to traverse the subsumption hierarchy if they are
				// not satisfied
				// check1: disjointness with index
				// check3: no superclass exists already
				// check5: disjointness
				if(!isDisjoint(candidate,index) && checkSubClasses(existingClasses,candidate) && checkDisjoints(existingClasses,candidate)) {
					// check whether the class is meaningful, i.e. adds something to the index
					// to do this, we need to make sure that the class is not a superclass of the
					// index (otherwise we get nothing new)
					if(!isDisjoint(new Negation(candidate),index) && checkSuperClasses(existingClasses,candidate)) {
						// candidate went successfully through all checks
						candidates.add(candidate);
					} else {
//						System.out.println("k32: " + candidate + " index " + index + " cond1 " + isDisjoint(new Negation(candidate),index) + " cond2 " + checkSuperClasses(existingClasses,candidate));
						// descend subsumption hierarchy to find candidates
						candidates.addAll(getClassCandidatesRecursive(index, existingClasses, candidate));
					}
				}
			}
		}
		return candidates;
	}
	
	// returns true if the candidate is not subclass of an existing class,
	// false otherwise (check 3)
	private boolean checkSubClasses(Set<NamedClass> existingClasses, NamedClass candidate) {
		for(NamedClass nc : existingClasses) {
//			System.out.println("csc: " + nc + candidate);
			if(sh.isSubclassOf(candidate, nc)) {
				return false;
			}
		}
		return true;
	}
	
	// returns true if the candidate is not superclass of an existing class,
	// false otherwise (check 4)
	private boolean checkSuperClasses(Set<NamedClass> existingClasses, NamedClass candidate) {
		for(NamedClass nc : existingClasses) {
			if(sh.isSubclassOf(nc, candidate))
				return false;
		}
		return true;
	}	
	
	// returns false if any of the classes is disjoint with the new one; true otherwise
	private boolean checkDisjoints(Set<NamedClass> existingClasses, NamedClass candidate) {
		for(NamedClass nc : existingClasses) {
			if(isDisjoint(nc, candidate))
				return false;
		}
		return true;
	}	
		
	
	public boolean isDisjoint(Description d1, Description d2) {
//		System.out.println("d1: " + d1);
//		System.out.println("d2: " + d2);
//		System.out.println("cache: " + cachedDisjoints);
		
		// check whether we have cached this query
		Map<Description,Boolean> tmp = cachedDisjoints.get(d1);
		Boolean tmp2 = null;
		if(tmp != null)
			tmp2 = tmp.get(d2);
		
		if(tmp2==null) {
			Boolean result;
			if(instanceBasedDisjoints) {
				result = isDisjointInstanceBased(d1,d2);
			} else {
				Description d = new Intersection(d1, d2);
				Monitor mon = MonitorFactory.start("disjointness reasoning");
				result = reasoner.isSuperClassOf(new Nothing(), d);	
				mon.stop();
			}
			// add the result to the cache (we add it twice such that
			// the order of access does not matter)
			
			// create new entries if necessary
			Map<Description,Boolean> map1 = new TreeMap<Description,Boolean>(conceptComparator);
			Map<Description,Boolean> map2 = new TreeMap<Description,Boolean>(conceptComparator);
			if(tmp == null)
				cachedDisjoints.put(d1, map1);
			if(!cachedDisjoints.containsKey(d2))
				cachedDisjoints.put(d2, map2);
			
			// add result symmetrically in the description matrix
			cachedDisjoints.get(d1).put(d2, result);
			cachedDisjoints.get(d2).put(d1, result);
			return result;
		} else {
			return tmp2;
		}
	}	
	
	private boolean isDisjointInstanceBased(Description d1, Description d2) {
		SortedSet<Individual> d1Instances = reasoner.getIndividuals(d1);
		SortedSet<Individual> d2Instances = reasoner.getIndividuals(d2);
		for(Individual d1Instance : d1Instances) {
			if(d2Instances.contains(d1Instance))
				return false;
		}
		return true;
	}

}
