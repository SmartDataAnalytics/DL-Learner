package org.dllearner.utilities;

import java.util.Comparator;
import java.util.Set;

import org.dllearner.core.owl.BooleanValueRestriction;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypeSomeRestriction;
import org.dllearner.core.owl.DoubleMaxValue;
import org.dllearner.core.owl.DoubleMinValue;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.ObjectCardinalityRestriction;
import org.dllearner.core.owl.ObjectMaxCardinalityRestriction;
import org.dllearner.core.owl.ObjectMinCardinalityRestriction;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.SimpleDoubleDataRange;
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
		// classes higher up are in the source code have lower value
		// (they appear first in class descriptions, because sorted sets
		// usually use an ascending order)
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
		} else if(concept1 instanceof BooleanValueRestriction) {
			if(concept2 instanceof Nothing || concept2 instanceof NamedClass) {
				return 1;
			} else if(concept2 instanceof BooleanValueRestriction) {
				// first criterion: name of the properties
				int cmp = rc.compare(((BooleanValueRestriction)concept1).getRestrictedPropertyExpresssion(), ((BooleanValueRestriction)concept2).getRestrictedPropertyExpresssion());

				// second criterion: value of the properties (it should rarely happen that
				// both boolean values are present since this is a contradiction or superfluous)
				if(cmp == 0) {
					boolean val1 = ((BooleanValueRestriction)concept1).getBooleanValue();
					boolean val2 = ((BooleanValueRestriction)concept2).getBooleanValue();
					if(val1) {
						if(val2)
							return 0;
						else
							return 1;
					} else {
						if(val2)
							return -1;
						else
							return 0;						
					}
				} else
					return cmp;				
			} else
				return -1;
		} else if(concept1 instanceof DatatypeSomeRestriction) {
			if(concept2 instanceof Nothing || concept2 instanceof NamedClass || concept2 instanceof BooleanValueRestriction) {
				return 1;
			} else if(concept2 instanceof DatatypeSomeRestriction) {
				DatatypeSomeRestriction dsr = (DatatypeSomeRestriction) concept1;
				DatatypeProperty dp = (DatatypeProperty) dsr.getRestrictedPropertyExpression();
				DatatypeSomeRestriction dsr2 = (DatatypeSomeRestriction) concept2;
				DatatypeProperty dp2 = (DatatypeProperty) dsr2.getRestrictedPropertyExpression();				
				
				// first criterion: name of the properties
				int cmp = rc.compare(dp, dp2);

				if(cmp == 0) {
					SimpleDoubleDataRange dr = (SimpleDoubleDataRange) dsr.getDataRange();
					SimpleDoubleDataRange dr2 = (SimpleDoubleDataRange) dsr2.getDataRange();					
					
					// equal classes
					if((dr instanceof DoubleMaxValue && dr2 instanceof DoubleMaxValue)
							|| (dr instanceof DoubleMinValue && dr2 instanceof DoubleMinValue)) {
						double val1 = dr.getValue();
						double val2 = dr2.getValue();
						if(val1 > val2)
							return 1;
						else if(val1 == val2)
							return 0;
						else
							return -1;		
						
					} else if(dr instanceof DoubleMaxValue)
						return 1;
					else
						return -1;
				} else
					return cmp;				
			} else
				return -1;
		} else if(concept1 instanceof Thing) {
			if(concept2 instanceof Nothing || concept2 instanceof NamedClass || concept2 instanceof BooleanValueRestriction || concept2 instanceof DatatypeSomeRestriction)
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
		} else if(concept1 instanceof ObjectMinCardinalityRestriction) {
			if(concept2.getChildren().size()<1 || concept2 instanceof Negation || concept2 instanceof ObjectQuantorRestriction)
				return 1;
			// first criterion: object property
			// second criterion: number
			// third criterion: children
			else if(concept2 instanceof ObjectMinCardinalityRestriction) {
				int roleCompare = rc.compare(((ObjectCardinalityRestriction)concept1).getRole(), ((ObjectCardinalityRestriction)concept2).getRole());
				if(roleCompare == 0) {
					Integer number1 = ((ObjectCardinalityRestriction)concept1).getNumber();
					Integer number2 = ((ObjectCardinalityRestriction)concept2).getNumber();
					int numberCompare = number1.compareTo(number2);
					if(numberCompare == 0)
						return compare(concept1.getChild(0), concept2.getChild(0));
					else
						return numberCompare;
				} else
					return roleCompare;
			} else
				return -1;			
		} else if(concept1 instanceof ObjectMaxCardinalityRestriction) {
			if(concept2.getChildren().size()<1 || concept2 instanceof Negation || concept2 instanceof ObjectQuantorRestriction || concept2 instanceof ObjectMinCardinalityRestriction)
				return 1;
			// first criterion: object property
			// second criterion: number
			// third criterion: children
			else if(concept2 instanceof ObjectMaxCardinalityRestriction) {
				int roleCompare = rc.compare(((ObjectCardinalityRestriction)concept1).getRole(), ((ObjectCardinalityRestriction)concept2).getRole());
				if(roleCompare == 0) {
					Integer number1 = ((ObjectCardinalityRestriction)concept1).getNumber();
					Integer number2 = ((ObjectCardinalityRestriction)concept2).getNumber();
					int numberCompare = number1.compareTo(number2);
					if(numberCompare == 0)
						return compare(concept1.getChild(0), concept2.getChild(0));
					else
						return numberCompare;
				} else
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

	// TODO: Vergleich zwischen ConceptComparators: immer identisch
	// (testen, ob das bessere Performance bringt)
	@Override		
	public boolean equals(Object o) {
		return (o instanceof ConceptComparator);
	}
	
}
