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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.owl.BooleanValueRestriction;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.Union;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyExpression;
import org.dllearner.core.owl.ObjectQuantorRestriction;
import org.dllearner.core.owl.Thing;
import org.dllearner.core.owl.ValueRestriction;
import org.dllearner.utilities.owl.ConceptComparator;
import org.dllearner.utilities.owl.ConceptTransformation;

/**
 * Implementation of the downward refinement operator in the DL-Learner refinement 
 * based algorithm.
 * 
 * See <a href="http://jens-lehmann.org/files/2007_alc_learning_algorithm.pdf"
 * >http://jens-lehmann.org/files/2007_alc_learning_algorithm.pdf</a> for
 * details.
 * 
 * @author Jens Lehmann
 *
 */
public class RhoDown extends RefinementOperatorAdapter {

//	private PosNegLP learningProblem;
	private AbstractReasonerComponent rs;
	
	// gibt die Gr��e an bis zu der die Refinements des Top-Konzepts
	// bereits berechnet worden => entspricht der max. L�nge der Menge M
	private int topRefinementsLength = 0;
	
	// die Menge M im Refinement-Operator indiziert nach ihrer L�nge
	Map<Integer,Set<Description>> m = new HashMap<Integer,Set<Description>>();
	
	// Zerlegungen der Zahl n in Mengen
	// Map<Integer,Set<IntegerCombo>> combos = new HashMap<Integer,Set<IntegerCombo>>();
	Map<Integer, List<List<Integer>>> combos = new HashMap<Integer, List<List<Integer>>>();
	// abspeichern von Kombinationen während diese rekursiv berechnet werden
	// private List<List<Integer>> combosTmp;	
	
	// Refinements des Top-Konzept indiziert nach Länge
	Map<Integer, TreeSet<Description>> topRefinements = new HashMap<Integer, TreeSet<Description>>();
	Map<Integer, TreeSet<Description>> topRefinementsCumulative = new HashMap<Integer, TreeSet<Description>>();
	
	// comparator für Konzepte
	private ConceptComparator conceptComparator = new ConceptComparator();
	
	// Statistik
	public long mComputationTimeNs = 0;
	public long topComputationTimeNs = 0;
	
	private boolean applyAllFilter = true;
	private boolean applyExistsFilter = true;
	private boolean useAllConstructor = true;
	private boolean useExistsConstructor = true;
	private boolean useNegation = true;
	private boolean useBooleanDatatypes = true;
	
	// braucht man wirklich das learningProblem oder reicht der Reasoning-Service?
	// TODO: conceptComparator könnte auch noch Parameter sein
	public RhoDown(AbstractReasonerComponent reasoningService, boolean applyAllFilter, boolean applyExistsFilter, boolean useAllConstructor,
	boolean useExistsConstructor, boolean useNegation, boolean useBooleanDatatypes) {
		this.rs = reasoningService;
		this.applyAllFilter = applyAllFilter;
		this.applyExistsFilter = applyExistsFilter;
		this.useAllConstructor = useAllConstructor;
		this.useExistsConstructor = useExistsConstructor;
		this.useNegation = useNegation;
		this.useBooleanDatatypes = useBooleanDatatypes;
		
//		this.learningProblem = learningProblem;
//		rs = learningProblem.getReasonerComponent();
	}

	@Override
	public Set<Description> refine(Description concept) {
		throw new RuntimeException();
		// TODO Auto-generated method stub
		// return null;
	}

	// TODO: Methode muss effizienter werden
	// Hauptproblem ist nicht die Berechnung von M und Top (siehe Benchmarks)
	// => zuerst muss Objekterzeugung minimiert werden
	// => als zweites wäre bei nicht ausreichendem Performancegewinn die Implementierung
	// von Minimallänge eine Möglichkeit (alle Refinements, auch improper, müssten
	// dann im Algorithmus gespeichert werden)
	@Override
	@SuppressWarnings("unchecked")
	public SortedSet<Description> refine(Description concept, int maxLength,
			List<Description> knownRefinements) {
		
		
		
		// Set<Concept> refinements = new HashSet<Concept>();
		SortedSet<Description> refinements = new TreeSet<Description>(conceptComparator);
		Set<Description> tmp = new HashSet<Description>();
		// SortedSet<Concept> tmp = new TreeSet<Concept>(conceptComparator);
		
		if (concept instanceof Thing) {

			// ggf. Refinements von Top erweitern
			if(maxLength>topRefinementsLength)
				computeTopRefinements(maxLength);
			// System.out.println(topRefinements);
			refinements = (TreeSet<Description>) topRefinementsCumulative.get(maxLength).clone();
			// refinements = copyTopRefinements(maxLength);
			// refinements = topRefinementsCumulative.get(maxLength);

		} else if (concept instanceof Nothing) {
			// return new HashSet<Concept>();
		} else if (concept instanceof ValueRestriction) {
			// value restrictions cannot be further refined	
		} else if (concept instanceof NamedClass) {
			// Erkenntnisse aus Benchmarks: dieser Teil wird sehr häufig aufgerufen,
			// allerdings lässt er sich kaum weiter verbessern (selbst ohne klonen
			// der Konzepte im DIG-Reasoner, was durch das entfernen von Bottom notwendig
			// ist und außerdem sicherer vor zukünftigen Bugs, wird es nicht wesentlich
			// schneller)

			// beachte: die Funktion gibt bereits nur nicht-äquivalente Konzepte zurück
			// TODO: der Cast auf SortedSet ist nur ein Hack und muss später geeignet
			// behandelt werden
			refinements = rs.getSubClasses(concept);
			// refinements.addAll(learningProblem.getReasonerComponent().getMoreSpecialConcepts(concept));
			
			// Bottom rausschmeißen (nicht im Operator vorgesehen)
			// Iterator<Concept> it = refinements.iterator();
			// while(it.hasNext()) {
			//	Concept c = it.next();
			//	if(c instanceof Bottom)
			//		it.remove();
			// }
			// geht jetzt auch schneller durch conceptComparator
			refinements.remove(new Nothing());		
			
		// negiertes atomares Konzept
		} else if (concept instanceof Negation && concept.getChild(0) instanceof NamedClass) {
		
			tmp = rs.getSuperClasses(concept.getChild(0));
			
			 //Iterator<Concept> it = tmp.iterator();
			 //while(it.hasNext()) {
			//	Concept c = it.next();
			//	if(c instanceof Top)
			//		it.remove();
			 //}			
			
			// tmp.remove(new Top());
			
			for(Description c : tmp) {
				if(!(c instanceof Thing))
					refinements.add(new Negation(c));
			}
		
		} else if (concept instanceof Intersection) {
				
			// eines der Elemente kann verfeinert werden
			for(Description child : concept.getChildren()) {
				
				// Refinement für das Kind ausführen
				// System.out.println("child: " + child);
				// wenn man von maximaler Länge die Länge des Konzepts außer dem aktuell
				// betrachteten Element abzieht, dann bekommt man neue maxLength
				tmp = refine(child, maxLength - concept.getLength()+child.getLength(),null);
				
				// neue MultiConjunction konstruieren
				for(Description c : tmp) {
					// TODO: müssen auch alle Konzepte geklont werden??
					// hier wird nur eine neue Liste erstellt
					// => eigentlich muss nicht geklont werden (d.h. deep copy) da
					// die Konzepte nicht verändert werden während des Algorithmus
					// => es muss geklont werden, da die top refinements nicht verändert
					// werden dürfen
					// List<Concept> newChildren = new LinkedList<Concept>(concept.getChildren());
					// TODO: Class Cast ist nur ein Hack
					List<Description> newChildren = (List<Description>)((LinkedList<Description>)concept.getChildren()).clone();
					// es muss genau die vorherige Reihenfolge erhalten bleiben
					// (zumindest bis die Normalform definiert ist)
					// int index = newChildren.indexOf(child);
					// newChildren.add(index, c);					
					// newChildren.remove(child);
					// MultiConjunction mc = new MultiConjunction(newChildren);
					
					// Index muss jetzt nich mehr erhalten bleiben, da ohnehin
					// neu sortiert wird
					newChildren.add(c);
					newChildren.remove(child);
					Intersection mc = new Intersection(newChildren);
					
					// sicherstellten, dass Konzept in negation normal form ist
					ConceptTransformation.cleanConceptNonRecursive(mc);
					ConceptTransformation.transformToOrderedNegationNormalFormNonRecursive(mc, conceptComparator);
					
					refinements.add(mc);	
				}
				
			}
				
		} else if (concept instanceof Union) {
			// eines der Elemente kann verfeinert werden
			for(Description child : concept.getChildren()) {
				
				// Refinement für das Kind ausführen
				// tmp = refine(child);
				tmp = refine(child, maxLength - concept.getLength()+child.getLength(),null);
				

				
				// neue MultiConjunction konstruieren
				for(Description c : tmp) {
					List<Description> newChildren = new LinkedList<Description>(concept.getChildren());
					// es muss genau die vorherige Reihenfolge erhalten bleiben
					// (zumindest bis die Normalform definiert ist)
					// int index = newChildren.indexOf(child);
					// newChildren.add(index, c);
					newChildren.remove(child);						
					newChildren.add(c);
					Union md = new Union(newChildren);
						
					// sicherstellten, dass Konzept in negation normal form ist
					// ConceptTransformation.cleanConcept(md); // nicht notwendig, da kein Element einer 
					// Disjunktion auf eine Disjunktion abgebildet wird (nur Top und das ist nie
					// in einer Disjunktion)
					ConceptTransformation.transformToOrderedNegationNormalFormNonRecursive(md, conceptComparator);
										
					
					refinements.add(md);	
				}
				
				
			}
			
		} else if (concept instanceof ObjectSomeRestriction) {
			ObjectPropertyExpression role = ((ObjectQuantorRestriction)concept).getRole();
			
			// rule 1: EXISTS r.D => EXISTS r.E
			tmp = refine(concept.getChild(0), maxLength-2, null);

			for(Description c : tmp) {
				refinements.add(new ObjectSomeRestriction(((ObjectQuantorRestriction)concept).getRole(),c));
			}
			
			// rule 2: EXISTS r.D => EXISTS s.D or EXISTS r^-1.D => EXISTS s^-1.D
			// currently inverse roles are not supported
			ObjectProperty ar = (ObjectProperty) role;
			Set<ObjectProperty> moreSpecialRoles = rs.getSubProperties(ar);
			for(ObjectProperty moreSpecialRole : moreSpecialRoles) {
				refinements.add(new ObjectSomeRestriction(moreSpecialRole, concept.getChild(0)));
			}

		} else if (concept instanceof ObjectAllRestriction) {
			ObjectPropertyExpression role = ((ObjectQuantorRestriction)concept).getRole();
			
			// rule 1: ALL r.D => ALL r.E
			tmp = refine(concept.getChild(0), maxLength-2, null);

			for(Description c : tmp) {
				refinements.add(new ObjectAllRestriction(((ObjectQuantorRestriction)concept).getRole(),c));
			}		
			
			// rule 2: ALL r.D => ALL r.BOTTOM if D is a most specific atomic concept
			// falls es keine spezielleren atomaren Konzepte gibt, dann wird 
			// bottom angehangen => nur wenn es ein atomares Konzept (insbesondere != bottom)
			// ist
			// if(tmp.size()==0) {
			if(concept.getChild(0) instanceof NamedClass && tmp.size()==0) {
				refinements.add(new ObjectAllRestriction(((ObjectQuantorRestriction)concept).getRole(),new Nothing()));
			}
			
			// rule 3: ALL r.D => ALL s.D or ALL r^-1.D => ALL s^-1.D
			// currently inverse roles are not supported
			ObjectProperty ar = (ObjectProperty) role;
			Set<ObjectProperty> moreSpecialRoles = rs.getSubProperties(ar);
			for(ObjectProperty moreSpecialRole : moreSpecialRoles) {
				refinements.add(new ObjectAllRestriction(moreSpecialRole, concept.getChild(0)));
			}
			
		}
		
		// falls Konzept ungleich Bottom oder Top, dann kann ein Refinement von Top
		// angehangen werden
		if(concept instanceof Union || concept instanceof NamedClass ||
				concept instanceof Negation || concept instanceof ObjectQuantorRestriction
				|| concept instanceof ValueRestriction) {
			// long someTimeNsStart = System.nanoTime();
			// someCount++;
			// Refinement von Top anhängen
			int topRefLength = maxLength - concept.getLength() - 1; //-1 wegen zusätzlichem UND
			// es könnte passieren, das wir hier neue Refinements von Top berechnen müssen
			if(topRefLength > topRefinementsLength)
				computeTopRefinements(topRefLength);
			if(topRefLength>0) {
				// Set<Concept> topRefs = copyTopRefinements(topRefLength);
				Set<Description> topRefs = topRefinementsCumulative.get(topRefLength);
				for(Description c : topRefs) {
					boolean skip = false;
					
					// falls Refinement von der Form ALL r ist, dann prüfen, ob
					// ALL r nicht bereits vorkommt
					if(applyAllFilter) {
					if(c instanceof ObjectAllRestriction) {
						for(Description child : concept.getChildren()) {
							if(child instanceof ObjectAllRestriction) {
								ObjectPropertyExpression r1 = ((ObjectAllRestriction)c).getRole();
								ObjectPropertyExpression r2 = ((ObjectAllRestriction)child).getRole();
								if(r1.toString().equals(r2.toString()))
									skip = true;
							}
						}
					}
					}
					
					if(!skip) {
						// MultiConjunction md = new MultiConjunction(concept.getChildren());
						Intersection mc = new Intersection();
						mc.addChild(concept);
						mc.addChild(c);				
						
						// Negationsnormalform herstellen
						ConceptTransformation.cleanConceptNonRecursive(mc);
						ConceptTransformation.transformToOrderedNegationNormalFormNonRecursive(mc, conceptComparator);
											
						refinements.add(mc);
					}
				}
			}
			// someTimeNs += System.nanoTime() - someTimeNsStart;
		}

		
		
		// Refinements werden jetzt noch bereinigt, d.h. Verschachtelungen von Konjunktionen
		// werden entfernt; es wird eine neue Menge erzeugt, da die Transformationen die
		// Ordnung des Konzepts ändern könnten
		// TODO: eventuell geht das noch effizienter, da die meisten Refinement-Regeln Refinements
		// von Child-Konzepten sind, die bereits geordnet sind, d.h. man könnte dort eventuell
		// gleich absichern, dass alle neu hinzugefügten Refinements in geordneter Negationsnormalform
		// sind
		/*
		SortedSet<Concept> returnSet = new TreeSet<Concept>(conceptComparator);
		for(Concept c : refinements) {
			ConceptTransformation.cleanConcept(c);
			ConceptTransformation.transformToOrderedNegationNormalForm(c, conceptComparator);
			returnSet.add(c);
		}
		
		return returnSet;
		*/
		// TODO: obiger Code kann noch nicht gelöscht werden (anderes Ergebnis); warum?
		// => erstmal so implementieren, dass obiger Code nicht mehr gebraucht wird und
		// im zweiten Schritt dann auf die nicht-rekursiven Methoden umsteigen
		return refinements;
	}
	
	
	// TODO: Methode kann später entfernt werden, es muss nur
	// sichergestellt werden, dass die refine-Methode an den
	// kumulativen Top-Refinements nichts ändert
	@SuppressWarnings("unused")
	private SortedSet<Description> copyTopRefinements(int maxLength) {
		// return topRefinementsCumulative.get(maxLength);
		SortedSet<Description> ret = new TreeSet<Description>(conceptComparator);
		for(Description c : topRefinementsCumulative.get(maxLength))
			ret.add(c);
		return ret;
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
				/*
				// für eine Kombo alle Konzeptkombinationen berechnen
				Set<Set<Concept>> baseSet = new HashSet<Set<Concept>>();
				// boolean firstNonEmptyNumber = true;
				for(Integer j : combo.getNumbers()) {
					// initialisiert wird mit der passenden Menge m
					//if(firstNonEmptyNumber || )
					//	baseSet.add(m.get(j));
					// else {
						baseSet = incCrossProduct(baseSet,m.get(j));
					//}
				}
				
				// Disjunktionen erzeugen und hinzufügen
				for(Set<Concept> children : baseSet) {
					if(children.size() == 1) {
						Iterator<Concept> it = children.iterator();
						Concept c = it.next();
						topRefinements.get(i).add(c);
					} else {
						topRefinements.get(i).add(new MultiDisjunction(children));
					}
				}
				*/
				
				/* neue Implementierung */
				
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
						ConceptTransformation.transformToOrderedForm(concept, conceptComparator);
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
			Set<Description> m1 = rs.getSubClasses(new Thing()); 
			m.put(1,m1);
		}
		
		if(topRefinementsLength<2 && maxLength>1) {	
			// Konzepte der Länge 2 = Negation aller Konzepte, die über Bottom liegen
			if(useNegation) {
				Set<Description> m2tmp = rs.getSuperClasses(new Nothing());
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
				for(ObjectProperty r : rs.getMostGeneralProperties()) {
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
							
							for(ObjectProperty r : rs.getMostGeneralProperties()) {
								m.get(i+2).add(new ObjectAllRestriction(r,c));
							}
						}
					}
				}
			}
		}
		
		mComputationTimeNs += System.nanoTime() - mComputationTimeStartNs;
	}
	
	
	// wird nicht mehr verwendet
	public static void summen(int zahl, int max, String bisher, int recDepth)
	{
		for(int j=0; j<recDepth; j++)
    		System.out.print("  ");
		System.out.println("f("+zahl+","+max+",\""+bisher+"\")");
		
	    for (int i = Math.min(zahl, max); i >= 1; i--)
	    {

	        String jetzt = bisher + i;

	        if (zahl - i > 1)
	        {
	            jetzt += " + ";
	            // i wird hinzugefügt, d.h.
	            // - es muss nur noch zahl - i zerlegt werden
	            // - es darf keine größere Zahl als i mehr vorkommen
	            // (dadurch gehen keine Kombinationen verloren)
	            summen(zahl - i -1 , i, jetzt, recDepth+1);
	        }
	        // Fall zahl == i, d.h. es muss nicht weiter zerlegt werden
	        else if(zahl - i == 0){
	        	for(int j=0; j<recDepth; j++)
	        		System.out.print("  ");
	            System.out.println(jetzt);
	        }
	        // durch die -1 Abzug für jedes Zwischenzeichen gibt es auch Fälle, in denen
	        // keine Lösung entsteht (zahl-i==1)

	    }
	}	
	

	

	
	// incremental cross product
	// es müssen Listen statt Sets verwendet werden
	@SuppressWarnings({"unused"})
	private Set<Set<Description>> incCrossProductOld(Set<Set<Description>> baseSet, Set<Description> newSet) {
		Set<Set<Description>> retSet = new HashSet<Set<Description>>();
		
		// falls erste Menge leer ist, dann wird Menge mit jeweils Singletons aus der
		// zweiten Menge zurückgegeben => das müsste dem Fall entsprechen, dass das
		// baseSet nur die leere Menge enthält
		if(baseSet.isEmpty()) {
			for(Description c : newSet) {
				Set<Description> singleton = new HashSet<Description>();
				singleton.add(c);
				retSet.add(singleton);
			}
			// retSet.add(newSet);
			return retSet;
		}
		
		for(Set<Description> set : baseSet) {
			for(Description c : newSet) {
				// neues Konzept zu alter Konzeptmenge hinzufügen, indem altes
				// Konzept kopiert und ergänzt wird
				// beachte: dadurch, dass die Konzepte nach ihrem Hash eingefügt werden,
				// ist schon eine Ordnung vorgegeben und es entfallen viele Mengen
				// z.B. ist {male,female} = {female,male}
				// TODO: das ist allerdings auch gefährlich, denn es gilt auch
				// {male,male,female} = {male,female} d.h. es entfallen auch gewünschte
				// Lösungen! (Es könnte z.B. sein, dass die Lösung eine Disjunktion von
				// 3 atomaren Konzepten ist, die nur über male erreichbar sind.) D.h. diese
				// Implementierung führt zur incompleteness des Operators.
				Set<Description> newConcept = new HashSet<Description>(set);
				newConcept.add(c);
				retSet.add(newConcept);
			}
		}
		return retSet;
	}

	@Override
	public void init() throws ComponentInitException {
		// TODO Auto-generated method stub
		
	}

}
