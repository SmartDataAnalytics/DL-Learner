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

package org.dllearner.utilities.owl;

import java.util.Comparator;
import java.util.Set;

import org.dllearner.core.owl.BooleanValueRestriction;
import org.dllearner.core.owl.Constant;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypeSomeRestriction;
import org.dllearner.core.owl.DatatypeValueRestriction;
import org.dllearner.core.owl.DoubleMaxValue;
import org.dllearner.core.owl.DoubleMinValue;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.ObjectCardinalityRestriction;
import org.dllearner.core.owl.ObjectMaxCardinalityRestriction;
import org.dllearner.core.owl.ObjectMinCardinalityRestriction;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.ObjectValueRestriction;
import org.dllearner.core.owl.SimpleDoubleDataRange;
import org.dllearner.core.owl.Union;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.ObjectQuantorRestriction;
import org.dllearner.core.owl.Thing;

/**
 * Implements a total order on class descriptions. Note that the
 * comparator is, of course, inconsistent with equals on class
 * descriptions, because currently two class descriptions are considered
 * equal if they refer to the same memory address (Java standard), while
 * this comparator takes the syntax of class descriptions into account.
 *  
 * TODO Improve implementation (better not to rely on number of children,
 * make better use of the class hierarchy to avoid too many instanceof
 * operations e.g. boolean description [union, intersection, negation],
 * restrictions [card., quantor], classes [top, named, bottom] could
 * be the first decision criterion).
 * TODO Add a description how exactly the order is defined.
 * 
 * @author Jens Lehmann
 *
 */
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
		
		// test whether Intersection/Union really have more than
		// one child (wastes some performance, but violating those
		// restrictions can lead to difficult-to-find bugs);
		// we test on concept2, which is the problematic case;
		// comment out if not needed;
		// note that the code also makes some further assumptions
		// about how many children certain constructs can have, but
		// all of those are obvious and unlikely to be cause by
		// programming errors
		// TODO: does not work at the moment, because some code 
		// relies on temporarily have these structurs with only
		// one child
//		if((concept2 instanceof Intersection || concept2 instanceof Union) && concept2.getChildren().size() < 2) {
//			throw new Error("Intersection/Union must have at least two children " + concept2);
//		}
		
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
				int cmp = rc.compare(((BooleanValueRestriction)concept1).getRestrictedPropertyExpression(), ((BooleanValueRestriction)concept2).getRestrictedPropertyExpression());

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
		} else if(concept1 instanceof ObjectValueRestriction) {
			if(concept2 instanceof Nothing || concept2 instanceof NamedClass || concept2 instanceof BooleanValueRestriction || concept2 instanceof DatatypeSomeRestriction) {
				return 1;
			} else if(concept2 instanceof ObjectValueRestriction) {
				int roleCompare = rc.compare(((ObjectValueRestriction)concept1).getRestrictedPropertyExpression(), ((ObjectValueRestriction)concept2).getRestrictedPropertyExpression());
				
				if(roleCompare == 0) {
					Individual value1 = ((ObjectValueRestriction)concept1).getIndividual();
					Individual value2 = ((ObjectValueRestriction)concept2).getIndividual();
					return value1.compareTo(value2);
				} else {
					return roleCompare;
				}
			} else
				return -1;			
		} else if(concept1 instanceof DatatypeValueRestriction) {
			if(concept2 instanceof Nothing || concept2 instanceof NamedClass || concept2 instanceof BooleanValueRestriction || concept2 instanceof DatatypeSomeRestriction || concept2 instanceof ObjectValueRestriction) {
				return 1;
			} else if(concept2 instanceof DatatypeValueRestriction) {
				int roleCompare = rc.compare(((DatatypeValueRestriction)concept1).getRestrictedPropertyExpression(), ((DatatypeValueRestriction)concept2).getRestrictedPropertyExpression());
				
				if(roleCompare == 0) {
					Constant value1 = ((DatatypeValueRestriction)concept1).getValue();
					Constant value2 = ((DatatypeValueRestriction)concept2).getValue();
					return value1.compareTo(value2);
				} else {
					return roleCompare;
				}
			} else
				return -1;			
		} else if(concept1 instanceof Thing) {
			if(concept2 instanceof Nothing || concept2 instanceof NamedClass || concept2 instanceof BooleanValueRestriction || concept2 instanceof DatatypeSomeRestriction || concept2 instanceof ObjectValueRestriction || concept2 instanceof DatatypeValueRestriction)
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
