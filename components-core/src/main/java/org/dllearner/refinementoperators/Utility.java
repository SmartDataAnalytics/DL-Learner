/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.ClassHierarchy;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.google.common.collect.Sets;
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
	
	// specifies whether to do real disjoint tests or check that
	// two named classes do not have common instances
	private boolean instanceBasedDisjoints = true;
	
	// cache for reasoner queries
	private Map<OWLClassExpression,Map<OWLClassExpression,Boolean>> cachedDisjoints = new TreeMap<>();
		
	// cache for applicaple object properties
	private Map<OWLClassExpression, SortedSet<OWLObjectProperty>> appOPCache = new TreeMap<>();
	private Map<OWLClassExpression, SortedSet<OWLDataProperty>> appDPCache = new TreeMap<>();
	private Map<OWLObjectProperty,OWLClassExpression> opDomains;
	private Map<OWLDataProperty, OWLClassExpression> dpDomains;
	
	private OWLDataFactory df = new OWLDataFactoryImpl();
	
	public Utility(AbstractReasonerComponent rs) {
		throw new Error("not implemented yet");
	}
	
	public Utility(AbstractReasonerComponent rs, Map<OWLObjectProperty,OWLClassExpression> opDomains, boolean instanceBasedDisjoints) {
		this.reasoner = rs;
		sh = rs.getClassHierarchy();
		// we cache object property domains
		this.opDomains = opDomains;
		this.instanceBasedDisjoints = instanceBasedDisjoints;
	}
	
	public Utility(AbstractReasonerComponent rs, Map<OWLObjectProperty,OWLClassExpression> opDomains, Map<OWLDataProperty,OWLClassExpression> dpDomains, boolean instanceBasedDisjoints) {
		this.reasoner = rs;
		this.dpDomains = dpDomains;
		sh = rs.getClassHierarchy();
		// we cache object property domains
		this.opDomains = opDomains;
		this.instanceBasedDisjoints = instanceBasedDisjoints;
	}
	
	/**
	 * Compute the set of applicable object properties for a
	 * given description.
	 * 
	 * @param index The index is a class expression which determines
	 * which of the properties are applicable. Exactly those which
	 * where the index and property domain are not disjoint are
	 * applicable, where disjoint is defined by {@link #isDisjoint(OWLClassExpression, OWLClassExpression)}.
	 * 
	 */
	public SortedSet<OWLObjectProperty> computeApplicableObjectProperties(OWLClassExpression index) {
		// use a cache, because large ontologies can have many object properties
		SortedSet<OWLObjectProperty> applicableObjectProperties = appOPCache.get(index);
		if(applicableObjectProperties == null) {
			Set<OWLObjectProperty> objectProperties = reasoner.getObjectProperties();
			applicableObjectProperties = new TreeSet<>();
			for(OWLObjectProperty op : objectProperties) {
				OWLClassExpression domain = opDomains.get(op);
				if(!isDisjoint(index,domain)) {
					applicableObjectProperties.add(op);
				}
			}
			appOPCache.put(index, applicableObjectProperties);
		}
		return applicableObjectProperties;
	}
	
	/**
	 * Compute the set of applicable data properties for a
	 * given description.
	 * 
	 * @param index The index is a OWLClassExpression which determines
	 * which of the properties are applicable. Exactly those which
	 * where the index and property domain are not disjoint are
	 * applicable, where disjoint is defined by {@link #isDisjoint(OWLClassExpression, OWLClassExpression)}.
	 * 
	 */
	public SortedSet<OWLDataProperty> computeApplicableDatatypeProperties(OWLClassExpression index) {
		// use a cache, because large ontologies can have many data properties
		SortedSet<OWLDataProperty> applicableDatatypeProperties = appDPCache.get(index);
		if(applicableDatatypeProperties == null) {
			Set<OWLDataProperty> datatypeProperties = reasoner.getDatatypeProperties();
			applicableDatatypeProperties = new TreeSet<>();
			for(OWLDataProperty op : datatypeProperties) {
				OWLClassExpression domain = dpDomains.get(op);
				if(!isDisjoint(index,domain)) {
					applicableDatatypeProperties.add(op);
				}
			}
			appDPCache.put(index, applicableDatatypeProperties);
		}
		return applicableDatatypeProperties;
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
	public Set<OWLObjectProperty> computeMgr(Set<OWLObjectProperty> applicableObjectProperties) {
		return new HashSet<>(Sets.intersection(reasoner.getMostGeneralProperties(), applicableObjectProperties));
	}
	
	/**
	 * Given a set of applicable data properties, this method returns
	 * the most general ones, i.e. those where more general ones do not
	 * exist in the set of applicable properties. Due to the definition
	 * of "applicable", the returned set is just the intersection of the most
	 * general object properties and the applicable properties. (A non-applicable
	 * property cannot have applicable subproperties, because subproperties
	 * can only restrict, but not broaden their domain.)
	 * 
	 * @param applicableDatatypeProperties The set of applicable properties.
	 * @return The most general applicable properties.
	 */
	public Set<OWLDataProperty> computeMgrDP(Set<OWLDataProperty> applicableDatatypeProperties) {
		return new HashSet<>(Sets.intersection(reasoner.getMostGeneralDatatypeProperties(), applicableDatatypeProperties));
	}
	
	public Set<OWLClass> getClassCandidates(OWLClassExpression index, Set<OWLClass> existingClasses) {
		return getClassCandidatesRecursive(index, existingClasses, df.getOWLThing());
	}
	
	private Set<OWLClass> getClassCandidatesRecursive(OWLClassExpression index, Set<OWLClass> existingClasses, OWLClassExpression upperClass) {
		Set<OWLClass> candidates = new TreeSet<>();
		
		// we descend the subsumption hierarchy to ensure that we get
		// the most general concepts satisfying the criteria
		// there are 4 checks a class has to satisfy to get into the set;
		// for 2 of them we can stop further traversal in the subsumption
		// hierarchy
		for(OWLClassExpression d : sh.getSubClasses(upperClass, true)) {
//			System.out.println("d: " + d);
			// owl:Nothing is never a candidate (not in EL)
			if(!d.isOWLNothing()) {
				OWLClass candidate = d.asOWLClass();
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
					if(!isDisjoint(df.getOWLObjectComplementOf(candidate),index) && checkSuperClasses(existingClasses,candidate)) {
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
	private boolean checkSubClasses(Set<OWLClass> existingClasses, OWLClass candidate) {
		for(OWLClass nc : existingClasses) {
//			System.out.println("csc: " + nc + candidate);
			if(sh.isSubclassOf(candidate, nc)) {
				return false;
			}
		}
		return true;
	}
	
	// returns true if the candidate is not superclass of an existing class,
	// false otherwise (check 4)
	private boolean checkSuperClasses(Set<OWLClass> existingClasses, OWLClass candidate) {
		for(OWLClass nc : existingClasses) {
			if(sh.isSubclassOf(nc, candidate))
				return false;
		}
		return true;
	}
	
	// returns false if any of the classes is disjoint with the new one; true otherwise
	private boolean checkDisjoints(Set<OWLClass> existingClasses, OWLClass candidate) {
		for(OWLClass nc : existingClasses) {
			if(isDisjoint(nc, candidate))
				return false;
		}
		return true;
	}
		
	
	public boolean isDisjoint(OWLClassExpression d1, OWLClassExpression d2) {
//		System.out.println("d1: " + d1);
//		System.out.println("d2: " + d2);
//		System.out.println("cache: " + cachedDisjoints);
		
		// check whether we have cached this query
		Map<OWLClassExpression,Boolean> tmp = cachedDisjoints.get(d1);
		Boolean tmp2 = null;
		if(tmp != null)
			tmp2 = tmp.get(d2);
		
		if(tmp2==null) {
			Boolean result;
			if(instanceBasedDisjoints) {
				result = isDisjointInstanceBased(d1,d2);
			} else {
				OWLClassExpression d = df.getOWLObjectIntersectionOf(d1, d2);
				Monitor mon = MonitorFactory.start("disjointness reasoning");
				result = reasoner.isSuperClassOf(df.getOWLNothing(), d);
				mon.stop();
			}
			// add the result to the cache (we add it twice such that
			// the order of access does not matter)
			
			// create new entries if necessary
			if(tmp == null)
				cachedDisjoints.put(d1, new TreeMap<>());
			if(!cachedDisjoints.containsKey(d2))
				cachedDisjoints.put(d2, new TreeMap<>());
			
			// add result symmetrically in the OWLClassExpression matrix
			cachedDisjoints.get(d1).put(d2, result);
			cachedDisjoints.get(d2).put(d1, result);
			return result;
		} else {
			return tmp2;
		}
	}
	
	private boolean isDisjointInstanceBased(OWLClassExpression d1, OWLClassExpression d2) {
		SortedSet<OWLIndividual> d1Instances = reasoner.getIndividuals(d1);
		SortedSet<OWLIndividual> d2Instances = reasoner.getIndividuals(d2);
		for(OWLIndividual d1Instance : d1Instances) {
			if(d2Instances.contains(d1Instance))
				return false;
		}
		return true;
	}

}