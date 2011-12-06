package org.dllearner.utilities;

import java.util.Comparator;
import java.util.Set;

import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.Union;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.ObjectQuantorRestriction;
import org.dllearner.core.owl.Thing;

// Comparator ist momentan inkonsistent mit equals für Konzepte, d.h. es kann sein, dass
// zwei Konzepte nicht als gleich deklariert werden (momentan gelten Konzepte immer als
// unterschiedlich, wenn sie nicht das gleiche Objekt im Speicher sind), aber in der
// compare-Funktion trotzdem 0 zurückgegeben wird
public class ConceptComparator implements Comparator<Description> {

	RoleComparator rc = new RoleComparator();
	
	// private List<AtomicConcept> atomicConcepts = new LinkedList<AtomicConcept>();
	
	public ConceptComparator() {
		
	}
	
	// Liste von atomaren Konzepten wird übergeben, damit eine Ordnung
	// auf atomaren Konzepten festgelegt werden kann; also keine Stringvergleiche
	// auf diesen Konzepten notwendig sind
	// TODO: erstmal nur mit Stringvergleichen, da diese bei atomaren Konzepten
	// schnell sein könnten, und dann testen, ob vorgegebene Ordnung Geschwindigkeitsvorteile
	// bringt
	public ConceptComparator(Set<NamedClass> atomicConcepts) {
		
	}
	
	// es werden Annahmen über Konzepte gemacht:
	//    1. bestehen aus Top, Bottom, AtomicConcept, Negation, MultiConjunction, MultiDisjunction,
	//       Exists, All
	//    2. MultiConjunction und MultiDisjunction haben min. 2 Kinder
	//
	// beachte: z.B. (male AND female) und (female AND male) sind ungleich; sie sind aber
	//          gleich, wenn sie vorher in ordered negation normal form umgewandelt worden, da
	//          dadurch die Anordnung der Kinder festgelegt wird
	// 1: Konzept 1 ist größer
	//
	// Ordnung für atomare Konzepte: Stringvergleich
	// Ordnung für atomare Rollen: Stringvergleich
	public int compare(Description concept1, Description concept2) {
		if(concept1 instanceof Nothing) {
			if(concept2 instanceof Nothing)
				return 0;
			else
				return -1;
		} else if(concept1 instanceof NamedClass) {
			if(concept2 instanceof Nothing)
				return 1;
			else if(concept2 instanceof NamedClass)
				return ((NamedClass)concept1).getName().compareTo(((NamedClass)concept2).getName());
			else
				return -1;
		} else if(concept1 instanceof Thing) {
			if(concept2 instanceof Nothing || concept2 instanceof NamedClass)
				return 1;
			else if(concept2 instanceof Thing)
				return 0;
			else
				return -1;
		} else if(concept1 instanceof Negation) {
			if(concept2.getChildren().size()<1)
				return 1;
			else if(concept2 instanceof Negation)
				return compare(concept1.getChild(0), concept2.getChild(0));
			else
				return -1;
		} else if(concept1 instanceof ObjectSomeRestriction) {
			if(concept2.getChildren().size()<1 || concept2 instanceof Negation)
				return 1;
			else if(concept2 instanceof ObjectSomeRestriction) {
				int roleCompare = rc.compare(((ObjectQuantorRestriction)concept1).getRole(), ((ObjectQuantorRestriction)concept2).getRole());
				if(roleCompare == 0)
					return compare(concept1.getChild(0), concept2.getChild(0));
				else
					return roleCompare;
			}	
			else
				return -1;
		} else if(concept1 instanceof ObjectAllRestriction) {
			if(concept2.getChildren().size()<1 || concept2 instanceof Negation || concept2 instanceof ObjectSomeRestriction)
				return 1;
			else if(concept2 instanceof ObjectAllRestriction) {
				int roleCompare = rc.compare(((ObjectQuantorRestriction)concept1).getRole(), ((ObjectQuantorRestriction)concept2).getRole());
				if(roleCompare == 0)
					return compare(concept1.getChild(0), concept2.getChild(0));
				else
					return roleCompare;
			} else
				return -1;
		} else if(concept1 instanceof Intersection) {
			if(concept2.getChildren().size()<2)
				return 1;
			else if(concept2 instanceof Intersection) {
				int nrOfChildrenConcept1 = concept1.getChildren().size();
				int nrOfChildrenConcept2 = concept2.getChildren().size();
				
				if(nrOfChildrenConcept1>nrOfChildrenConcept2)
					return 1;
				else if(nrOfChildrenConcept1==nrOfChildrenConcept2) {
					for(int i=0; i<nrOfChildrenConcept1; i++) {
						int compareValue = compare(concept1.getChild(i),concept2.getChild(i));
						if(compareValue>0)
							return 1;
						else if(compareValue<0)
							return -1;
					}
					return 0;
				} else
					return -1;
			} else
				return -1;
		} else if(concept1 instanceof Union) {
			if(concept2.getChildren().size()<2 || concept2 instanceof Intersection)
				return 1;
			else if(concept2 instanceof Union) {
				int nrOfChildrenConcept1 = concept1.getChildren().size();
				int nrOfChildrenConcept2 = concept2.getChildren().size();
				
				if(nrOfChildrenConcept1>nrOfChildrenConcept2)
					return 1;
				else if(nrOfChildrenConcept1==nrOfChildrenConcept2) {
					for(int i=0; i<nrOfChildrenConcept1; i++) {
						int compareValue = compare(concept1.getChild(i),concept2.getChild(i));
						if(compareValue>0)
							return 1;
						else if(compareValue<0)
							return -1;
					}
					return 0;
				} else
					return -1;
			} else
				return -1;		
		} else
			throw new RuntimeException(concept1.toString());
	}
	
	/*
	private int compareRole(Role r1, Role r2) {
		return r1.toString().compareTo(r2.toString());
	}
	*/

	// TODO: Vergleich zwischen ConceptComparators: immer identisch
	// (testen, ob das bessere Performance bringt)
	@Override		
	public boolean equals(Object o) {
		return (o instanceof ConceptComparator);
	}
	
}