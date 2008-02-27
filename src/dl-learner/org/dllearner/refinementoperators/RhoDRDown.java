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
	
	// gibt die Gr��e an bis zu der die Refinements des Top-Konzepts
	// bereits berechnet worden => entspricht der max. L�nge der Menge M
	private int topRefinementsLength = 0;
	
	// die Menge M im Refinement-Operator indiziert nach ihrer L�nge
	private Map<Integer,Set<Description>> m = new HashMap<Integer,Set<Description>>();
	
	// Zerlegungen der Zahl n in Mengen
	// Map<Integer,Set<IntegerCombo>> combos = new HashMap<Integer,Set<IntegerCombo>>();
	private Map<Integer, List<List<Integer>>> combos = new HashMap<Integer, List<List<Integer>>>();
	// abspeichern von Kombinationen während diese rekursiv berechnet werden
	// private List<List<Integer>> combosTmp;	
	
	// Refinements des Top-Konzept indiziert nach Länge
	private Map<Integer, TreeSet<Description>> topRefinements = new HashMap<Integer, TreeSet<Description>>();
	private Map<Integer, TreeSet<Description>> topRefinementsCumulative = new HashMap<Integer, TreeSet<Description>>();
	
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
	
	// TODO: später private
	public void computeTopRefinements(int maxLength) {
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
	
	// computation of the set M 
	private void computeM(int maxLength) {
		long mComputationTimeStartNs = System.nanoTime();
		// System.out.println("compute M from " + (topRefinementsLength+1) + " up to " + maxLength);
		
		// initialise all not yet initialised lengths
		// (avoids null pointers in some cases)
		for(int i=topRefinementsLength+1; i<=maxLength; i++) {
			m.put(i, new TreeSet<Description>(conceptComparator));
		}
		
		// Berechnung der Basiskonzepte in M
		// TODO: Spezialfälle, dass zwischen Top und Bottom nichts liegt behandeln
		if(topRefinementsLength==0 && maxLength>0) {
			// Konzepte der Länge 1 = alle Konzepte, die in der Subsumptionhierarchie unter Top liegen
			Set<Description> m1 = rs.getMoreSpecialConcepts(new Thing()); 
			m.put(1,m1);
		}
		
		if(topRefinementsLength<2 && maxLength>1) {	
			// Konzepte der Länge 2 = Negation aller Konzepte, die über Bottom liegen
			if(useNegation) {
				Set<Description> m2tmp = rs.getMoreGeneralConcepts(new Nothing());
				Set<Description> m2 = new TreeSet<Description>(conceptComparator);
				for(Description c : m2tmp) {
					m2.add(new Negation(c));
				}
				m.put(2,m2);
			}
		}
			
		if(topRefinementsLength<3 && maxLength>2) {
			// Konzepte der Länge 3: EXISTS r.TOP
			Set<Description> m3 = new TreeSet<Description>(conceptComparator);
			if(useExistsConstructor) {
				// previous operator: uses all roles
				// for(AtomicRole r : Config.Refinement.allowedRoles) {
				//	m3.add(new Exists(r, new Top()));
				//}
				// new operator: only uses most general roles
				for(ObjectProperty r : rs.getMostGeneralRoles()) {
					m3.add(new ObjectSomeRestriction(r, new Thing()));
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
		}
		
		if(maxLength>2) {
			if(useAllConstructor) {
				// Konzepte, die mit ALL r starten
				// alle existierenden Konzepte durchgehen, die maximal 2 k�rzer als 
				// die maximale L�nge sind
				// topRefinementsLength - 1, damit Konzepte der Länge mindestens
				// topRefinementsLength + 1 erzeugt werden (ALL r)
				for(int i=topRefinementsLength-1; i<=maxLength-2; i++) {
					// i muss natürlich mindestens 1 sein
					if(i>=1) {
						
						// alle Konzepte durchgehen
						for(Description c : m.get(i)) {
							// Fall wird jetzt weiter oben schon abgehandelt
							// if(!m.containsKey(i+2))
							//	m.put(i+2, new TreeSet<Concept>(conceptComparator));
							
							// previous operator: uses all roles
							// for(AtomicRole r : Config.Refinement.allowedRoles) {
								// Mehrfacheinf�gen ist bei einer Menge kein Problem
							// 	m.get(i+2).add(new All(r,c));
							// }
							
							for(ObjectProperty r : rs.getMostGeneralRoles()) {
								m.get(i+2).add(new ObjectAllRestriction(r,c));
							}
						}
					}
				}
			}
		}
		
		mComputationTimeNs += System.nanoTime() - mComputationTimeStartNs;
	}
		
	
}
