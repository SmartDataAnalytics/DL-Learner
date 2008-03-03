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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dllearner.algorithms.refinement.RefinementOperator;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.owl.BooleanValueRestriction;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.SubsumptionHierarchy;
import org.dllearner.core.owl.Thing;
import org.dllearner.core.owl.Union;
import org.dllearner.utilities.ConceptComparator;
import org.dllearner.utilities.ConceptTransformation;

/**
 * A downward refinement operator, which makes use of domains
 * and ranges of properties. The operator is currently under
 * development. Its aim is to span a much "cleaner" and smaller search
 * tree compared to RhoDown by omitting many class descriptions,
 * which are obviously too weak, because they violate 
 * domain/range restrictions. Furthermore, it makes use of disjoint
 * classes in the knowledge base.
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
	
	// start concept (can be used to start from an arbitrary concept, needs
	// to be Thing or NamedClass), note that when you use e.g. Compound as 
	// start class, then the algorithm should start the search with class
	// Compound (and not with Thing), because otherwise concepts like
	// NOT Carbon-87 will be returned which itself is not a subclass of Compound
	private Description startClass = new Thing();
	
	// the length of concepts of top refinements, the first values is
	// for refinements of \rho_\top(\top), the second one for \rho_A(\top)
	private int topRefinementsLength = 0;
	private Map<NamedClass, Integer> topARefinementsLength = new TreeMap<NamedClass, Integer>();
	
	// the sets M_\top and M_A
	private Map<Integer,Set<Description>> m = new TreeMap<Integer,Set<Description>>();
	private Map<NamedClass,Map<Integer,Set<Description>>> mA = new TreeMap<NamedClass,Map<Integer,Set<Description>>>();
	
	// @see MathOperations.getCombos
	private Map<Integer, List<List<Integer>>> combos = new HashMap<Integer, List<List<Integer>>>();

	// refinements of the top concept ordered by length
	private Map<Integer, TreeSet<Description>> topRefinements = new TreeMap<Integer, TreeSet<Description>>();
	private Map<NamedClass,Map<Integer, TreeSet<Description>>> topARefinements = new TreeMap<NamedClass,Map<Integer, TreeSet<Description>>>();
	
	// cumulated refinements of top (all from length one to the specified length)
	private Map<Integer, TreeSet<Description>> topRefinementsCumulative = new HashMap<Integer, TreeSet<Description>>();
	private Map<NamedClass,Map<Integer, TreeSet<Description>>> topARefinementsCumulative = new TreeMap<NamedClass,Map<Integer, TreeSet<Description>>>();
	
	// app_A set of applicable properties for a given class (separte for
	// object properties, boolean datatypes, and double data types)
	private Map<NamedClass, Set<ObjectProperty>> appOP = new TreeMap<NamedClass, Set<ObjectProperty>>();
	private Map<NamedClass, Set<DatatypeProperty>> appBD = new TreeMap<NamedClass, Set<DatatypeProperty>>();
	private Map<NamedClass, Set<DatatypeProperty>> appDD = new TreeMap<NamedClass, Set<DatatypeProperty>>();
	
	// most general applicable properties
	private Map<NamedClass,Set<ObjectProperty>> mgr = new TreeMap<NamedClass,Set<ObjectProperty>>();
	
	// comparator für Konzepte
	private ConceptComparator conceptComparator = new ConceptComparator();
	
	// Statistik
	private long mComputationTimeNs = 0;
	private long topComputationTimeNs = 0;
	
//	private boolean applyAllFilter = true;
	private boolean applyExistsFilter = true;
	private boolean useAllConstructor = true;
	private boolean useExistsConstructor = true;
	private boolean useNegation = true;
	private boolean useBooleanDatatypes = true;	
	
	public RhoDRDown(ReasoningService rs) {
		this(rs, null);
	}
	
	public RhoDRDown(ReasoningService rs, NamedClass startClass) {
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
		
		if(startClass != null)
			this.startClass = startClass;
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

	@SuppressWarnings({"unchecked"})
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
			// extends top refinements if necessary
			if(currDomain instanceof Thing) {
				if(maxLength>topRefinementsLength)
					computeTopRefinements(maxLength);
				refinements = (TreeSet<Description>) topRefinementsCumulative.get(maxLength).clone();
			} else {
				if(maxLength>topARefinementsLength.get(currDomain))
					computeTopRefinements(maxLength);
				refinements = (TreeSet<Description>) topRefinementsCumulative.get(maxLength).clone();					
			}
			
//			refinements.addAll(subHierarchy.getMoreSpecialConcepts(description));
		} else if(description instanceof Nothing) {
			// cannot be further refined
		} else if(description instanceof NamedClass) {
			refinements.addAll(subHierarchy.getMoreSpecialConcepts(description));
		}
		
		return refinements;		
	}
	
	private void computeTopRefinements(int maxLength) {
		computeTopRefinements(maxLength, null);
	}
	
	private void computeTopRefinements(int maxLength, NamedClass domain) {
		long topComputationTimeStartNs = System.nanoTime();
		
		// M erweiteren
		computeM(maxLength);
		
		// berechnen aller möglichen Kombinationen für Disjunktion,
		for(int i = topRefinementsLength+1; i <= maxLength; i++) {
			combos.put(i,MathOperations.getCombos(i));
			topRefinements.put(i, new TreeSet<Description>(conceptComparator));
			// topRefinements.put(i, new HashSet<Concept>());
			
			for(List<Integer> combo : combos.get(i)) {
				
				// Kombination besteht aus nur einer Zahl => einfach M benutzen
				// if(combo.getNumbers().size()==1) {
				if(combo.size()==1) {
					topRefinements.get(i).addAll(m.get(i));
				// Kombination besteht aus mehreren Zahlen => Disjunktion erzeugen
				} else {
					Set<Union> baseSet = new HashSet<Union>();
					for(Integer j : combo) { // combo.getNumbers()) {
						baseSet = MathOperations.incCrossProduct(baseSet, m.get(j));
					}
					
					// Umwandlung aller Konzepte in Negationsnormalform
					for(Description concept : baseSet) {
						ConceptTransformation.transformToOrderedNegationNormalForm(concept, conceptComparator);
					}
					
					if(applyExistsFilter) {
					Iterator<Union> it = baseSet.iterator();
					while(it.hasNext()) {
						Union md = it.next();
						boolean remove = false;
						// falls Exists r für gleiche Rolle zweimal vorkommt,
						// dann rausschmeißen
						// Map<AtomicRole,Boolean> roleOccured = new HashMap<AtomicRole,Boolean>();
						Set<String> roles = new TreeSet<String>();
						for(Description c : md.getChildren()) {
							if(c instanceof ObjectSomeRestriction) {
								String role = ((ObjectSomeRestriction)c).getRole().getName();								
								boolean roleExists = !roles.add(role);
								// falls Rolle schon vorkommt, dann kann ganzes
								// Refinement ignoriert werden (man könnte dann auch
								// gleich abbrechen, aber das hat nur minimalste
								// Auswirkungen auf Effizienz)
								if(roleExists)
									remove = true;
							}
						}
						if(remove)
							it.remove();
						
					}
					}
					
					topRefinements.get(i).addAll(baseSet);
				}
			}
			
			// neu berechnete Refinements kumulieren, damit sie schneller abgefragt werden können
			// computeCumulativeTopRefinements(i);
			TreeSet<Description> cumulativeRefinements = new TreeSet<Description>(conceptComparator);
			// Set<Concept> cumulativeRefinements = new HashSet<Concept>();
			for(int j=1; j<=i; j++) {
				cumulativeRefinements.addAll(topRefinements.get(j));
			}			
			topRefinementsCumulative.put(i, cumulativeRefinements);		
		}
		
		// neue Maximallänge eintragen
		topRefinementsLength = maxLength;
		
		topComputationTimeNs += System.nanoTime() - topComputationTimeStartNs;
	}
	
	// compute M_\top
	private void computeM(int maxLength) {
		computeM(maxLength, null);
	}
	
	// computation of the set M_A
	// a major difference compared to the ILP 2007 \rho operator is that
	// M is finite and contains elements of length (currently) at most 3
	private void computeM(int maxLength, NamedClass domain) {
		long mComputationTimeStartNs = System.nanoTime();

		// initialise all possible lengths (1 to 3)
		for(int i=1; i<=3; i++) {
			m.put(i, new TreeSet<Description>(conceptComparator));
		}
		
		Set<Description> m1 = rs.getMoreSpecialConcepts(new Thing()); 
		m.put(1,m1);		
		
		if(useNegation) {
			Set<Description> m2tmp = rs.getMoreGeneralConcepts(new Nothing());
			Set<Description> m2 = new TreeSet<Description>(conceptComparator);
			for(Description c : m2tmp) {
				m2.add(new Negation(c));
			}
			m.put(2,m2);
		}
			
		Set<Description> m3 = new TreeSet<Description>(conceptComparator);
		if(useExistsConstructor) {
			// only uses most general roles
			for(ObjectProperty r : rs.getMostGeneralRoles()) {
				m3.add(new ObjectSomeRestriction(r, new Thing()));
			}				
		}
		
		if(useAllConstructor) {
			// we allow \forall r.\top here because otherwise the operator
			// becomes too difficult to manage due to dependencies between
			// M_A and M_A' where A'=ran(r)
			for(ObjectProperty r : rs.getMostGeneralRoles()) {
				m3.add(new ObjectAllRestriction(r, new Thing()));
			}				
		}		
		
		// boolean datatypes, e.g. testPositive = true
		if(useBooleanDatatypes) {
			Set<DatatypeProperty> booleanDPs = rs.getBooleanDatatypeProperties();
			for(DatatypeProperty dp : booleanDPs) {
				m3.add(new BooleanValueRestriction(dp,true));
				m3.add(new BooleanValueRestriction(dp,false));
			}
		}
		
		m.put(3,m3);
		
		mComputationTimeNs += System.nanoTime() - mComputationTimeStartNs;
	}
		
	private void computeMgr(NamedClass domain) {
		// compute the applicable properties if this has not been done yet
		if(appOP.get(domain) == null)
			computeApp(domain);
		Set<ObjectProperty> mostGeneral = rs.getMostGeneralRoles();
		computeMgrRecursive(domain, mostGeneral, mgr.get(domain));
	}
	
	private void computeMgrRecursive(NamedClass domain, Set<ObjectProperty> currProperties, Set<ObjectProperty> mgrTmp) {
		for(ObjectProperty prop : currProperties) {
			if(appOP.get(domain).contains(prop))
				mgrTmp.add(prop);
			else
				computeMgrRecursive(domain, rs.getMoreSpecialRoles(prop), mgrTmp);
		}
	}
	
	// computes the set of applicable properties for a given class
	private void computeApp(NamedClass domain) {
		// TODO: also implement this for boolean/double datatype properties
		Set<ObjectProperty> mostGeneral = rs.getAtomicRoles();
		Set<ObjectProperty> applicableRoles = new TreeSet<ObjectProperty>();
		for(ObjectProperty role : mostGeneral) {
			// TODO: currently we just rely on named classes as roles,
			// instead of computing dom(r) and ran(r)
			NamedClass nc = (NamedClass) rs.getDomain(role);
			if(!isDisjoint(domain,nc))
				applicableRoles.add(role);
		}
		appOP.put(domain, applicableRoles);
	}
	
	// computes whether two classes are disjoint; this should be computed
	// by the reasoner only ones and otherwise taken from a matrix
	private boolean isDisjoint(NamedClass class1, NamedClass class2) {
		// we need to test whether A AND B is equivalent to BOTTOM
		Description d = new Intersection(class1, class2);
		return rs.subsumes(new Nothing(), d);
	}
	
}