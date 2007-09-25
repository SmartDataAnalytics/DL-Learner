/**
 * Copyright (C) 2007, Jens Lehmann
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
package org.dllearner.algorithms.refinement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.Config;
import org.dllearner.LearningProblem;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.dl.All;
import org.dllearner.core.dl.AtomicConcept;
import org.dllearner.core.dl.AtomicRole;
import org.dllearner.core.dl.Bottom;
import org.dllearner.core.dl.Concept;
import org.dllearner.core.dl.Conjunction;
import org.dllearner.core.dl.Disjunction;
import org.dllearner.core.dl.Exists;
import org.dllearner.core.dl.MultiConjunction;
import org.dllearner.core.dl.MultiDisjunction;
import org.dllearner.core.dl.Negation;
import org.dllearner.core.dl.Quantification;
import org.dllearner.core.dl.Role;
import org.dllearner.core.dl.Top;
import org.dllearner.utilities.ConceptComparator;
import org.dllearner.utilities.ConceptTransformation;

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
public class RhoDown implements RefinementOperator {

	private LearningProblem learningProblem;
	private ReasoningService rs;
	
	// gibt die Gr��e an bis zu der die Refinements des Top-Konzepts
	// bereits berechnet worden => entspricht der max. L�nge der Menge M
	private int topRefinementsLength = 0;
	
	// die Menge M im Refinement-Operator indiziert nach ihrer L�nge
	Map<Integer,Set<Concept>> m = new HashMap<Integer,Set<Concept>>();
	
	// Zerlegungen der Zahl n in Mengen
	// Map<Integer,Set<IntegerCombo>> combos = new HashMap<Integer,Set<IntegerCombo>>();
	Map<Integer, List<List<Integer>>> combos = new HashMap<Integer, List<List<Integer>>>();
	// abspeichern von Kombinationen während diese rekursiv berechnet werden
	// private List<List<Integer>> combosTmp;	
	
	// Refinements des Top-Konzept indiziert nach Länge
	Map<Integer, TreeSet<Concept>> topRefinements = new HashMap<Integer, TreeSet<Concept>>();
	Map<Integer, TreeSet<Concept>> topRefinementsCumulative = new HashMap<Integer, TreeSet<Concept>>();
	
	// comparator für Konzepte
	private ConceptComparator conceptComparator = new ConceptComparator();
	
	// Statistik
	public long mComputationTimeNs = 0;
	public long topComputationTimeNs = 0;
	
	// braucht man wirklich das learningProblem oder reicht der Reasoning-Service?
	// TODO: conceptComparator könnte auch noch Parameter sein
	public RhoDown(LearningProblem learningProblem) {
		this.learningProblem = learningProblem;
		rs = learningProblem.getReasoningService();
	}

	public Set<Concept> refine(Concept concept) {
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
	@SuppressWarnings("unchecked")
	public SortedSet<Concept> refine(Concept concept, int maxLength,
			List<Concept> knownRefinements) {
		
		
		
		// Set<Concept> refinements = new HashSet<Concept>();
		SortedSet<Concept> refinements = new TreeSet<Concept>(conceptComparator);
		Set<Concept> tmp = new HashSet<Concept>();
		// SortedSet<Concept> tmp = new TreeSet<Concept>(conceptComparator);
		
		if (concept instanceof Top) {

			// ggf. Refinements von Top erweitern
			if(maxLength>topRefinementsLength)
				computeTopRefinements(maxLength);
			// System.out.println(topRefinements);
			refinements = (TreeSet<Concept>) topRefinementsCumulative.get(maxLength).clone();
			// refinements = copyTopRefinements(maxLength);
			// refinements = topRefinementsCumulative.get(maxLength);

		} else if (concept instanceof Bottom) {
			// return new HashSet<Concept>();
		} else if (concept instanceof AtomicConcept) {
			// Erkenntnisse aus Benchmarks: dieser Teil wird sehr häufig aufgerufen,
			// allerdings lässt er sich kaum weiter verbessern (selbst ohne klonen
			// der Konzepte im DIG-Reasoner, was durch das entfernen von Bottom notwendig
			// ist und außerdem sicherer vor zukünftigen Bugs, wird es nicht wesentlich
			// schneller)

			// beachte: die Funktion gibt bereits nur nicht-äquivalente Konzepte zurück
			// TODO: der Cast auf SortedSet ist nur ein Hack und muss später geeignet
			// behandelt werden
			refinements = learningProblem.getReasoningService().getMoreSpecialConcepts(concept);
			// refinements.addAll(learningProblem.getReasoningService().getMoreSpecialConcepts(concept));
			
			// Bottom rausschmeißen (nicht im Operator vorgesehen)
			// Iterator<Concept> it = refinements.iterator();
			// while(it.hasNext()) {
			//	Concept c = it.next();
			//	if(c instanceof Bottom)
			//		it.remove();
			// }
			// geht jetzt auch schneller durch conceptComparator
			refinements.remove(new Bottom());		
			
		// negiertes atomares Konzept
		} else if (concept instanceof Negation && concept.getChild(0) instanceof AtomicConcept) {
		
			tmp = rs.getMoreGeneralConcepts(concept.getChild(0));
			
			 //Iterator<Concept> it = tmp.iterator();
			 //while(it.hasNext()) {
			//	Concept c = it.next();
			//	if(c instanceof Top)
			//		it.remove();
			 //}			
			
			// tmp.remove(new Top());
			
			for(Concept c : tmp) {
				if(!(c instanceof Top))
					refinements.add(new Negation(c));
			}
		
		} else if (concept instanceof MultiConjunction) {
				
			// eines der Elemente kann verfeinert werden
			for(Concept child : concept.getChildren()) {
				
				// Refinement für das Kind ausführen
				// System.out.println("child: " + child);
				// wenn man von maximaler Länge die Länge des Konzepts außer dem aktuell
				// betrachteten Element abzieht, dann bekommt man neue maxLength
				tmp = refine(child, maxLength - concept.getLength()+child.getLength(),null);
				
				// neue MultiConjunction konstruieren
				for(Concept c : tmp) {
					// TODO: müssen auch alle Konzepte geklont werden??
					// hier wird nur eine neue Liste erstellt
					// => eigentlich muss nicht geklont werden (d.h. deep copy) da
					// die Konzepte nicht verändert werden während des Algorithmus
					// => es muss geklont werden, da die top refinements nicht verändert
					// werden dürfen
					// List<Concept> newChildren = new LinkedList<Concept>(concept.getChildren());
					// TODO: Class Cast ist nur ein Hack
					List<Concept> newChildren = (List<Concept>)((LinkedList)concept.getChildren()).clone();
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
					MultiConjunction mc = new MultiConjunction(newChildren);
					
					// sicherstellten, dass Konzept in negation normal form ist
					ConceptTransformation.cleanConceptNonRecursive(mc);
					ConceptTransformation.transformToOrderedNegationNormalFormNonRecursive(mc, conceptComparator);
					
					refinements.add(mc);	
				}
				
			}
				
		} else if (concept instanceof MultiDisjunction) {
			// eines der Elemente kann verfeinert werden
			for(Concept child : concept.getChildren()) {
				
				// Refinement für das Kind ausführen
				// tmp = refine(child);
				tmp = refine(child, maxLength - concept.getLength()+child.getLength(),null);
				

				
				// neue MultiConjunction konstruieren
				for(Concept c : tmp) {
					List<Concept> newChildren = new LinkedList<Concept>(concept.getChildren());
					// es muss genau die vorherige Reihenfolge erhalten bleiben
					// (zumindest bis die Normalform definiert ist)
					// int index = newChildren.indexOf(child);
					// newChildren.add(index, c);
					newChildren.remove(child);						
					newChildren.add(c);
					MultiDisjunction md = new MultiDisjunction(newChildren);
						
					// sicherstellten, dass Konzept in negation normal form ist
					// ConceptTransformation.cleanConcept(md); // nicht notwendig, da kein Element einer 
					// Disjunktion auf eine Disjunktion abgebildet wird (nur Top und das ist nie
					// in einer Disjunktion)
					ConceptTransformation.transformToOrderedNegationNormalFormNonRecursive(md, conceptComparator);
										
					
					refinements.add(md);	
				}
				
				
			}
			
		} else if (concept instanceof Exists) {
			Role role = ((Quantification)concept).getRole();
			
			// rule 1: EXISTS r.D => EXISTS r.E
			tmp = refine(concept.getChild(0), maxLength-2, null);

			for(Concept c : tmp) {
				refinements.add(new Exists(((Quantification)concept).getRole(),c));
			}
			
			// rule 2: EXISTS r.D => EXISTS s.D or EXISTS r^-1.D => EXISTS s^-1.D
			// currently inverse roles are not supported
			AtomicRole ar = (AtomicRole) role;
			Set<AtomicRole> moreSpecialRoles = rs.getMoreSpecialRoles(ar);
			for(AtomicRole moreSpecialRole : moreSpecialRoles) {
				refinements.add(new Exists(moreSpecialRole, concept.getChild(0)));
			}

		} else if (concept instanceof All) {
			Role role = ((Quantification)concept).getRole();
			
			// rule 1: ALL r.D => ALL r.E
			tmp = refine(concept.getChild(0), maxLength-2, null);

			for(Concept c : tmp) {
				refinements.add(new All(((Quantification)concept).getRole(),c));
			}		
			
			// rule 2: ALL r.D => ALL r.BOTTOM if D is a most specific atomic concept
			// falls es keine spezielleren atomaren Konzepte gibt, dann wird 
			// bottom angehangen => nur wenn es ein atomares Konzept (insbesondere != bottom)
			// ist
			// if(tmp.size()==0) {
			if(concept.getChild(0) instanceof AtomicConcept && tmp.size()==0) {
				refinements.add(new All(((Quantification)concept).getRole(),new Bottom()));
			}
			
			// rule 3: ALL r.D => ALL s.D or ALL r^-1.D => ALL s^-1.D
			// currently inverse roles are not supported
			AtomicRole ar = (AtomicRole) role;
			Set<AtomicRole> moreSpecialRoles = rs.getMoreSpecialRoles(ar);
			for(AtomicRole moreSpecialRole : moreSpecialRoles) {
				refinements.add(new All(moreSpecialRole, concept.getChild(0)));
			}
			
		} else if(concept instanceof Disjunction || concept instanceof Conjunction)
			throw new RuntimeException("only multi disjunction/conjunction allowed");
		
		// falls Konzept ungleich Bottom oder Top, dann kann ein Refinement von Top
		// angehangen werden
		if(concept instanceof MultiDisjunction || concept instanceof AtomicConcept ||
				concept instanceof Negation || concept instanceof Exists || concept instanceof All) {
			// long someTimeNsStart = System.nanoTime();
			// someCount++;
			// Refinement von Top anhängen
			int topRefLength = maxLength - concept.getLength() - 1; //-1 wegen zusätzlichem UND
			// es könnte passieren, das wir hier neue Refinements von Top berechnen müssen
			if(topRefLength > topRefinementsLength)
				computeTopRefinements(topRefLength);
			if(topRefLength>0) {
				// Set<Concept> topRefs = copyTopRefinements(topRefLength);
				Set<Concept> topRefs = topRefinementsCumulative.get(topRefLength);
				for(Concept c : topRefs) {
					boolean skip = false;
					
					// falls Refinement von der Form ALL r ist, dann prüfen, ob
					// ALL r nicht bereits vorkommt
					if(Config.Refinement.applyAllFilter) {
					if(c instanceof All) {
						for(Concept child : concept.getChildren()) {
							if(child instanceof All) {
								Role r1 = ((All)c).getRole();
								Role r2 = ((All)child).getRole();
								if(r1.toString().equals(r2.toString()))
									skip = true;
							}
						}
					}
					}
					
					if(!skip) {
						// MultiConjunction md = new MultiConjunction(concept.getChildren());
						MultiConjunction mc = new MultiConjunction();
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
	private SortedSet<Concept> copyTopRefinements(int maxLength) {
		// return topRefinementsCumulative.get(maxLength);
		SortedSet<Concept> ret = new TreeSet<Concept>(conceptComparator);
		for(Concept c : topRefinementsCumulative.get(maxLength))
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
			combos.put(i,getCombos(i));
			topRefinements.put(i, new TreeSet<Concept>(conceptComparator));
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
					Set<MultiDisjunction> baseSet = new HashSet<MultiDisjunction>();
					for(Integer j : combo) { // combo.getNumbers()) {
						baseSet = incCrossProduct2(baseSet, m.get(j));
					}
					
					// Umwandlung aller Konzepte in Negationsnormalform
					for(Concept concept : baseSet) {
						ConceptTransformation.transformToOrderedNegationNormalForm(concept, conceptComparator);
					}
					
					if(Config.Refinement.applyExistsFilter) {
					Iterator<MultiDisjunction> it = baseSet.iterator();
					while(it.hasNext()) {
						MultiDisjunction md = it.next();
						boolean remove = false;
						// falls Exists r für gleiche Rolle zweimal vorkommt,
						// dann rausschmeißen
						// Map<AtomicRole,Boolean> roleOccured = new HashMap<AtomicRole,Boolean>();
						Set<String> roles = new TreeSet<String>();
						for(Concept c : md.getChildren()) {
							if(c instanceof Exists) {
								String role = ((Exists)c).getRole().getName();								
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
			TreeSet<Concept> cumulativeRefinements = new TreeSet<Concept>(conceptComparator);
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
			m.put(i, new TreeSet<Concept>(conceptComparator));
		}
		
		// Berechnung der Basiskonzepte in M
		// TODO: Spezialfälle, dass zwischen Top und Bottom nichts liegt behandeln
		if(topRefinementsLength==0 && maxLength>0) {
			// Konzepte der Länge 1 = alle Konzepte, die in der Subsumptionhierarchie unter Top liegen
			Set<Concept> m1 = learningProblem.getReasoningService().getMoreSpecialConcepts(new Top()); 
			m.put(1,m1);
		}
		
		if(topRefinementsLength<2 && maxLength>1) {	
			// Konzepte der Länge 2 = Negation aller Konzepte, die über Bottom liegen
			if(Config.Refinement.useNegation) {
				Set<Concept> m2tmp = learningProblem.getReasoningService().getMoreGeneralConcepts(new Bottom());
				Set<Concept> m2 = new TreeSet<Concept>(conceptComparator);
				for(Concept c : m2tmp) {
					m2.add(new Negation(c));
				}
				m.put(2,m2);
			}
		}
			
		if(topRefinementsLength<3 && maxLength>2) {
			// Konzepte der Länge 3: EXISTS r.TOP
			if(Config.Refinement.useExistsConstructor) {
				Set<Concept> m3 = new TreeSet<Concept>(conceptComparator);
				// previous operator: uses all roles
				// for(AtomicRole r : Config.Refinement.allowedRoles) {
				//	m3.add(new Exists(r, new Top()));
				//}
				// new operator: only uses most general roles
				for(AtomicRole r : rs.getMostGeneralRoles()) {
					m3.add(new Exists(r, new Top()));
				}				
				m.put(3,m3);
			}
		}
		
		if(maxLength>2) {
			if(Config.Refinement.useAllConstructor) {
				// Konzepte, die mit ALL r starten
				// alle existierenden Konzepte durchgehen, die maximal 2 k�rzer als 
				// die maximale L�nge sind
				// topRefinementsLength - 1, damit Konzepte der Länge mindestens
				// topRefinementsLength + 1 erzeugt werden (ALL r)
				for(int i=topRefinementsLength-1; i<=maxLength-2; i++) {
					// i muss natürlich mindestens 1 sein
					if(i>=1) {
						
						// alle Konzepte durchgehen
						for(Concept c : m.get(i)) {
							// Fall wird jetzt weiter oben schon abgehandelt
							// if(!m.containsKey(i+2))
							//	m.put(i+2, new TreeSet<Concept>(conceptComparator));
							
							// previous operator: uses all roles
							// for(AtomicRole r : Config.Refinement.allowedRoles) {
								// Mehrfacheinf�gen ist bei einer Menge kein Problem
							// 	m.get(i+2).add(new All(r,c));
							// }
							
							for(AtomicRole r : rs.getMostGeneralRoles()) {
								m.get(i+2).add(new All(r,c));
							}
						}
					}
				}
			}
		}
		
		mComputationTimeNs += System.nanoTime() - mComputationTimeStartNs;
	}
	
	
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
	
	@SuppressWarnings("unchecked")
	private LinkedList<Integer> cloneList(LinkedList<Integer> list) {
		return (LinkedList<Integer>) list.clone();
	}
	
	/**
	 * 
	 * Dadurch das max das Maximum der vorkommenden Zahl regelt, kommen
	 * keine doppelten Kombinationen vor.
	 * 
	 * TODO: Implementierung mit Speicherung in Datenstruktur statt
	 * direkter Ausgabe; IntegerCombo wird hier gar nicht benötigt, da
	 * alle Elemente bereits in richtiger Reihenfolge vorliegen und
	 * es keine doppelten Nennungen gibt
	 * 
	 * @param zahl Zu zerlegende Zahl.
	 * @param max Maximal in Summenzerlegung vorkommende Zahl.
	 * @param bisher
	 */
	private void zerlege(int zahl, int max, LinkedList<Integer> bisher, List<List<Integer>> combosTmp) {
		
	    for (int i = Math.min(zahl, max); i >= 1; i--)
	    {
	    	
	    	LinkedList<Integer> newBisher = null;
	    	// für i==0 wird aus Effizienzgründen die bisherige Liste genommen
	    	if(i==0) {
	    		newBisher = bisher;
	    		newBisher.add(i);
	    	// für zahl - i == 1 muss gar keine Liste erstellt werden, da dann keine
	    	// Zerlegung mehr möglich ist
	    	} else if(zahl - i != 1) {
	    		newBisher = cloneList(bisher);
	    		newBisher.add(i);
	    	}
	    	
	        
	        if (zahl - i > 1)
	        {
	            // i wird hinzugefügt, d.h.
	            // - es muss nur noch zahl - i - 1 zerlegt werden (-1 wegen OR-Symbol)
	            // - es darf keine größere Zahl als i mehr vorkommen
	            // (dadurch gehen keine Kombinationen verloren)
	            zerlege(zahl - i - 1, i, newBisher,combosTmp);
	        }
	        // Fall zahl == i, d.h. es muss nicht weiter zerlegt werden
	        else if(zahl - i == 0){
	        	combosTmp.add(newBisher);
	        }
	        

	    }	
	    
	    // numbers.add(bisher);
	}
	
	// auf Notebook: Länge 70 in 17 Sekunden, Länge 50 in 800ms, Länge 30 in 15ms
	// http://88.198.173.90/tud/forum/messages?topic=304392
	public List<List<Integer>> getCombos(int length) {
		LinkedList<List<Integer>> combosTmp = new LinkedList<List<Integer>>();
		zerlege(length, length, new LinkedList<Integer>(), combosTmp);
		return combosTmp;
	}
	
	// neue Implementierung, die nicht mehr zur incompleteness führen soll,
	// da die Konzepte in einer MultiDisjunction als Liste gespeichert werden
	private Set<MultiDisjunction> incCrossProduct2(Set<MultiDisjunction> baseSet, Set<Concept> newSet) {
		Set<MultiDisjunction> retSet = new HashSet<MultiDisjunction>();
		
		if(baseSet.isEmpty()) {
			for(Concept c : newSet) {
				MultiDisjunction md = new MultiDisjunction();
				md.addChild(c);
				retSet.add(md);
			}
			return retSet;
		}
		
		for(MultiDisjunction md : baseSet) {
			for(Concept c : newSet) {
				MultiDisjunction mdNew = new MultiDisjunction(md.getChildren());
				mdNew.addChild(c);
				retSet.add(mdNew);
			}
		}
		
		return retSet;
	}
	
	// incremental cross product
	// es müssen Listen statt Sets verwendet werden
	@SuppressWarnings({"unused"})
	private Set<Set<Concept>> incCrossProduct(Set<Set<Concept>> baseSet, Set<Concept> newSet) {
		Set<Set<Concept>> retSet = new HashSet<Set<Concept>>();
		
		// falls erste Menge leer ist, dann wird Menge mit jeweils Singletons aus der
		// zweiten Menge zurückgegeben => das müsste dem Fall entsprechen, dass das
		// baseSet nur die leere Menge enthält
		if(baseSet.isEmpty()) {
			for(Concept c : newSet) {
				Set<Concept> singleton = new HashSet<Concept>();
				singleton.add(c);
				retSet.add(singleton);
			}
			// retSet.add(newSet);
			return retSet;
		}
		
		for(Set<Concept> set : baseSet) {
			for(Concept c : newSet) {
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
				Set<Concept> newConcept = new HashSet<Concept>(set);
				newConcept.add(c);
				retSet.add(newConcept);
			}
		}
		return retSet;
	}

}
