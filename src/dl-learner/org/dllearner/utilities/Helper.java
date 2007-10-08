package org.dllearner.utilities;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dllearner.Config;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.dl.AssertionalAxiom;
import org.dllearner.core.dl.AtomicConcept;
import org.dllearner.core.dl.AtomicRole;
import org.dllearner.core.dl.Concept;
import org.dllearner.core.dl.ConceptAssertion;
import org.dllearner.core.dl.FlatABox;
import org.dllearner.core.dl.Individual;
import org.dllearner.core.dl.KB;
import org.dllearner.core.dl.Negation;
import org.dllearner.core.dl.NumberRestriction;
import org.dllearner.core.dl.Quantification;
import org.dllearner.core.dl.RoleAssertion;

/**
 * Die Hilfsmethoden benutzen alle SortedSet, da die Operationen damit schneller sind.
 * @author jl
 *
 */
public class Helper {
     
	// findet alle atomaren Konzepte in einem Konzept
	public static List<AtomicConcept> getAtomicConcepts(Concept concept) {
		List<AtomicConcept> ret = new LinkedList<AtomicConcept>();
		if(concept instanceof AtomicConcept) {
			ret.add((AtomicConcept)concept);
			return ret;
		} else {
			for(Concept child : concept.getChildren()) {
				ret.addAll(getAtomicConcepts(child));
			}
			return ret;
		}
	}
	
	// findet alle atomaren Rollen in einem Konzept
	public static List<AtomicRole> getAtomicRoles(Concept concept) {
		List<AtomicRole> ret = new LinkedList<AtomicRole>();
		
		if(concept instanceof Quantification) {
			ret.add(new AtomicRole(((Quantification)concept).getRole().getName()));
		} else if(concept instanceof NumberRestriction) {
			ret.add(new AtomicRole(((NumberRestriction)concept).getRole().getName()));
		}
		
		// auch NumberRestrictions und Quantifications können weitere Rollen enthalten,
		// deshalb hier kein else-Zweig
		for(Concept child : concept.getChildren()) {
			ret.addAll(getAtomicRoles(child));
		}
		return ret;
		
	}	
	
	// sucht, ob der übergebene String mit einem Prefix beginnt der
	// versteckt werden soll und gibt diesen zurück, ansonsten wird
	// null zurück gegeben
	public static String findPrefixToHide(String name) {
    	for(String prefix : Config.hidePrefixes) {
    		if(name.startsWith(prefix))
    			return prefix;
    	}		
    	return null;
	}
	
	public static String prettyPrintNanoSeconds(long nanoSeconds) {
		return prettyPrintNanoSeconds(nanoSeconds, false, false);
	}
	
	// formatiert Nano-Sekunden in einen leserlichen String
	public static String prettyPrintNanoSeconds(long nanoSeconds, boolean printMicros, boolean printNanos) {
		// String str = "";
		// long seconds = 0;
		// long milliSeconds = 0;
		// long microseconds = 0;
		
		long seconds = nanoSeconds/1000000000;
		nanoSeconds = nanoSeconds % 1000000000;
		
		long milliSeconds = nanoSeconds/1000000;
		nanoSeconds = nanoSeconds % 1000000;

		// Mikrosekunden werden immer angezeigt, Sekunden nur falls größer 0
		String str = "";
		if(seconds > 0)
			str = seconds + "s ";
		str += milliSeconds + "ms";
		
		if(printMicros) {
			long microSeconds = nanoSeconds/1000;
			nanoSeconds = nanoSeconds % 1000;			
			str += " " + microSeconds + "usec";
		}
		if(printNanos) {
			str += " " + nanoSeconds + "ns";
		}
		
		return str;
	}
	
	public static<T1,T2> void addMapEntry(Map<T1, SortedSet<T2>> map,
			T1 keyEntry, T2 setEntry) {
		if (map.containsKey(keyEntry)) {
			map.get(keyEntry).add(setEntry);
		} else {
			SortedSet<T2> newSet = new TreeSet<T2>();
			newSet.add(setEntry);
			map.put(keyEntry, newSet);
		}
	}	
	
    /**
     * Das ist eine "generic method", d.h. die Methode hat einen bestimmten Typ.
     * Ich habe das benutzt um allen beteiligten Mengen den gleichen Typ zu geben,
     * denn ansonsten ist es nicht möglich der neu zu erzeugenden Menge (union) den
     * gleichen Typ wie den Argumenten zu geben. 
     * 
     * Die Methode hat gegenüber addAll den Vorteil, dass sie ein neues Objekt
     * erzeugt.
     * 
     * @param <T>
     * @param set1
     * @param set2
     * @return
     */
    public static<T> Set<T> unionAlt(Set<T> set1, Set<T> set2) {
        // TODO: effizientere Implementierung (längere Liste klonen und Elemente
        // anhängen)
        Set<T> union = new TreeSet<T>();
        union.addAll(set1);
        union.addAll(set2);
        return union;
        /*
        Set union;
        if(set1.size()>set2.size()) {
            union = set1.clone();
        } else {
            
        }
        return union;
        */
    }
    
    public static<T> SortedSet<T> union(SortedSet<T> set1, SortedSet<T> set2) {
    	//Set<T> union = set1.clone();
    	//((Cloneable) set1).clone();
    	
        // TODO: effizientere Implementierung (längere Liste klonen und Elemente
        // anhängen)
    	
    	// f�r TreeSet gibt es einen Konstruktor, der eine Collection entgegennimmt
    	// und einen weiteren, der ein SortedSet entgegennimmt; vermutlich ist
    	// letzterer schneller
    	
    	SortedSet<T> union;
    	if(set1.size()>set2.size()) {
    		union = new TreeSet<T>(set1);
    		union.addAll(set2);
    	} else {
    		union = new TreeSet<T>(set2);
    		union.addAll(set1);    		
    	}
        // SortedSet<T> union = new TreeSet<T>(set1);
        // union.addAll(set1);
        // union.addAll(set2);
        return union;

    }
    
    public static<T> SortedSet<T> intersection(SortedSet<T> set1, SortedSet<T> set2) {
        // TreeSet<T> intersection = (TreeSet<T>) set1.clone();
        // TODO: effizienter implementieren d.h. lange Liste klonen und dann
        // retainAll
        SortedSet<T> intersection = new TreeSet<T>(set1);
        // intersection.addAll(set1);
        intersection.retainAll(set2);
        return intersection;
    }
    
    public static<T> SortedSet<T> intersectionTuple(SortedSet<T> set, SortedSetTuple<T> tuple) {
    	SortedSet<T> ret = intersection(set,tuple.getPosSet());
    	ret.retainAll(tuple.getNegSet());
    	return ret;
    }
    
    public static<T> SortedSet<T> difference(SortedSet<T> set1, SortedSet<T> set2) {
        // TODO: effizienter implementieren 
        SortedSet<T> difference = new TreeSet<T>(set1);
        // difference.addAll(set1);
        difference.removeAll(set2);
        return difference;
    }

	// Umwandlung von Menge von Individuals auf Menge von Strings
	public static SortedSet<Individual> getIndividualSet(Set<String> individuals) {
		SortedSet<Individual> ret = new TreeSet<Individual>();
		for(String s : individuals) {
			ret.add(new Individual(s));
		}
		return ret;
	}	
	
	public static SortedSetTuple<Individual> getIndividualTuple(SortedSetTuple<String> tuple) {
		return new SortedSetTuple<Individual>(getIndividualSet(tuple.getPosSet()),getIndividualSet(tuple.getNegSet()));
	}
	
	public static SortedSetTuple<String> getStringTuple(SortedSetTuple<Individual> tuple) {
		return new SortedSetTuple<String>(getStringSet(tuple.getPosSet()),getStringSet(tuple.getNegSet()));
	}	
	
	// Umwandlung von Menge von Individuals auf Menge von Strings
	public static SortedSet<String> getStringSet(Set<Individual> individuals) {
		SortedSet<String> ret = new TreeSet<String>();
		for(Individual i : individuals) {
			ret.add(i.getName());
		}
		return ret;
	}
	
	public static Map<String,SortedSet<String>> getStringMap(Map<Individual, SortedSet<Individual>> roleMembers) {
		Map<String,SortedSet<String>> ret = new TreeMap<String,SortedSet<String>>();
		for(Individual i : roleMembers.keySet()) {
			ret.put(i.getName(), getStringSet(roleMembers.get(i)));
		}
		return ret;
	}

	/**
	 * TODO: 
	 * split in two methods (one for concepts, one for roles),
	 * document what exactly the method is doing, 
	 * remove dependencies from old Config class, 
	 * incorporate the new methods in the learning algorithms when appropriate 
	 * (common conf options for allowed concepts/roles and forbidden 
	 * concepts/roles need to be created)
	 * 
	 * Computes the set of allowed concepts based on configuration settings (also
	 * ignores anonymous and standard RDF, RDFS, OWL concept produces by Jena).
	 *
	 */
	public static void autoDetectConceptsAndRoles(ReasoningService rs) {
	// einige Sachen, die momentan nur vom Refinement-Algorithmus
	// unterstützt werden (später ev. auch von anderen Algorithmen)
	//if (Config.algorithm == Algorithm.REFINEMENT) {
		
		// berechnen der verwendbaren Konzepte
		if (Config.Refinement.allowedConceptsAutoDetect) {
			// TODO: Code aus DIG-Reasoner-Klasse einfügen
	
			Set<AtomicConcept> allowedConceptsTmp = new TreeSet<AtomicConcept>(
					new ConceptComparator());
			allowedConceptsTmp.addAll(rs.getAtomicConcepts());
			Iterator<AtomicConcept> it = allowedConceptsTmp.iterator();
			while (it.hasNext()) {
				String conceptName = it.next().getName();
				// System.out.println(conceptName);
				// seltsame anon-Konzepte, die von Jena erzeugt werden
				// löschen
				if (conceptName.startsWith("anon")) {
					System.out
							.println("  Ignoring concept "
									+ conceptName
									+ " (probably an anonymous concept produced by Jena when reading in OWL file).");
					it.remove();
				} else if (conceptName
						.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#")) {
					System.out
							.println("  Ignoring concept "
									+ conceptName
									+ " (RDF construct produced by Jena when reading in OWL file).");
					it.remove();
				} else if (conceptName
						.startsWith("http://www.w3.org/2000/01/rdf-schema#")) {
					System.out
							.println("  Ignoring concept "
									+ conceptName
									+ " (RDF Schema construct produced by Jena when reading in OWL file).");
					it.remove();
				} else if (conceptName.startsWith("http://www.w3.org/2002/07/owl#")) {
					System.out
							.println("  Ignoring concept "
									+ conceptName
									+ " (OWL construct produced by Jena when reading in OWL file).");
					it.remove();
				}
			}
			
			// hier werden jetzt noch die zu ignorierenden Konzepte entfernt
			if(Config.Refinement.ignoredConcepts != null) {
				
				
				for(AtomicConcept ac : Config.Refinement.ignoredConcepts) {
					boolean success = allowedConceptsTmp.remove(ac);
					if(!success) {
						System.out.println("Ignored concept " + ac + " does not exist in knowledge base.");
						System.exit(0);
					}
						
				}
			}
				
			
			Config.Refinement.allowedConcepts = allowedConceptsTmp;
		} else {
			// prüfen, ob nur verfügbare Konzepte vom Nutzer gewählt worden
			Set<AtomicConcept> allowed = new HashSet<AtomicConcept>();
			allowed.addAll(Config.Refinement.allowedConcepts);
			allowed.removeAll(rs.getAtomicConcepts());
			if (allowed.size() > 0) {
				System.out
						.println("Some of the concepts you told the learner to use in the definition, "
								+ "do not exist in the background knowledge: "
								+ allowed);
				System.out.println("Please correct this problem and restart.");
				System.exit(0);
			}
		}
	
		if (Config.Refinement.allowedRolesAutoDetect) {
			Set<AtomicRole> allowedRolesTmp = rs.getAtomicRoles();
			
			// hier werden jetzt noch die zu ignorierenden Rollen entfernt
			if(Config.Refinement.ignoredRoles != null) {
				
				
				for(AtomicRole ar : Config.Refinement.ignoredRoles) {
					boolean success = allowedRolesTmp.remove(ar);
					if(!success) {
						System.out.println("Ignored role " + ar + " does not exist in knowledge base.");
						System.exit(0);
					}
						
				}
			}
			
			Config.Refinement.allowedRoles = allowedRolesTmp;
			
		} else {
			Set<AtomicRole> allowedR = new HashSet<AtomicRole>();
			allowedR.addAll(Config.Refinement.allowedRoles);
	
			Set<AtomicRole> existingR = new TreeSet<AtomicRole>(new RoleComparator());
			existingR.addAll(rs.getAtomicRoles());
	
			// allowedR.removeAll(rs.getAtomicRoles());
			allowedR.removeAll(existingR);
	
			if (allowedR.size() > 0) {
				System.out
						.println("Some of the roles you told the learner to use in the definition, "
								+ "do not exist in the background knowledge: "
								+ allowedR);
				System.out.println("Please correct this problem and restart.");
				System.out.println(rs.getAtomicRoles());
				System.out.println(Config.Refinement.allowedRoles);
				System.exit(0);
			}
	
		}
	}

	// creates a flat ABox by querying a reasoner
	public static FlatABox createFlatABox(ReasoningService rs)
			throws ReasoningMethodUnsupportedException {
		long dematStartTime = System.currentTimeMillis();
		
		FlatABox aBox = new FlatABox(); // FlatABox.getInstance();
		for (AtomicConcept atomicConcept : rs.getAtomicConcepts()) {
			aBox.atomicConceptsPos.put(atomicConcept.getName(), getStringSet(rs.retrieval(atomicConcept)));
			Negation negatedAtomicConcept = new Negation(atomicConcept);
			aBox.atomicConceptsNeg.put(atomicConcept.getName(), getStringSet(rs.retrieval(negatedAtomicConcept)));
			aBox.concepts.add(atomicConcept.getName());
		}
	
		for (AtomicRole atomicRole : rs.getAtomicRoles()) {
			aBox.rolesPos.put(atomicRole.getName(), getStringMap(rs
					.getRoleMembers(atomicRole)));
			aBox.roles.add(atomicRole.getName());
		}
	
		aBox.domain = getStringSet(rs.getIndividuals());
		aBox.top = aBox.domain;
		// ab hier keine �nderungen mehr an FlatABox
		aBox.prepare();
	
		// System.out.println(aBox);
	
		long dematDuration = System.currentTimeMillis() - dematStartTime;
		System.out.println("OK (" + dematDuration + " ms)");
		return aBox;
	}

	// die Methode soll alle Konzeptzusicherungen und Rollenzusicherungen von
	// Individuen entfernen, die mit diesem Individuum verbunden sind
	@SuppressWarnings("unused")
	private static void removeIndividualSubtree(KB kb, Individual individual) {
		System.out.println();
		// erster Schritt: alle verbundenen Individuen finden
		Set<Individual> connectedIndividuals = kb.findRelatedIndividuals(individual);
		System.out.println("connected individuals: " + connectedIndividuals);
		// Individual selbst auch entfernen
		connectedIndividuals.add(individual);
	
		// zweiter Schritt: entfernen von Rollen- und Konzeptzusicherungen
		Set<AssertionalAxiom> abox = kb.getAbox();
		Iterator<AssertionalAxiom> it = abox.iterator();
		while (it.hasNext()) {
			AssertionalAxiom a = it.next();
			if (a instanceof RoleAssertion) {
				RoleAssertion ra = (RoleAssertion) a;
				if (connectedIndividuals.contains(ra.getIndividual1())
						|| connectedIndividuals.contains(ra.getIndividual2())) {
					System.out.println("remove " + ra);
					it.remove();
				}
			} else if (a instanceof ConceptAssertion) {
				if (connectedIndividuals.contains(((ConceptAssertion) a).getIndividual())) {
					System.out.println("remove " + a);
					it.remove();
				}
			} else
				throw new RuntimeException();
		}
	
		Set<Individual> inds = kb.findAllIndividuals();
		System.out.println("remaining individuals: " + inds);
		System.out.println();
	}	
}
