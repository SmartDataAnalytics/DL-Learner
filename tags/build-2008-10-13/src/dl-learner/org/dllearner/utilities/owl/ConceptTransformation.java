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
package org.dllearner.utilities.owl;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.ReasoningService;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.Union;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.ObjectPropertyExpression;
import org.dllearner.core.owl.ObjectQuantorRestriction;
import org.dllearner.core.owl.Thing;

/**
 * Concept transformation and concept checking methods.
 * 
 * @author Jens Lehmann
 */
public class ConceptTransformation {

	public static long cleaningTimeNs = 0;
	private static long cleaningTimeNsStart = 0;	
	public static long onnfTimeNs = 0;
	private static long onnfTimeNsStart = 0;
	public static long shorteningTimeNs = 0;
	private static long shorteningTimeNsStart = 0;
	
	public static void cleanConceptNonRecursive(Description concept) {
		// cleaningTimeNsStart = System.nanoTime();
		
		if(concept instanceof Intersection || concept instanceof Union) {

			List<Description> deleteChilds = new LinkedList<Description>();
			
			for(Description child : concept.getChildren()) {
				if((concept instanceof Intersection && child instanceof Intersection)
						|| (concept instanceof Union && child instanceof Union)) {
					deleteChilds.add(child);
				}
			}
			
			for(Description dc : deleteChilds) {
				// alle Kinder des zu löschenden Konzeptes hinzufügen
				for(Description dcChild : dc.getChildren()) {
					concept.addChild(dcChild);
				}
				// Konzept selber löschen
				concept.removeChild(dc);
			}
			
		}
		
		// cleaningTimeNs += System.nanoTime() - cleaningTimeNsStart;
	}
	
	

	// eliminiert Disjunktionen in Disjunktionen bzw. Konjunktionen in Konjunktionen
	public static void cleanConcept(Description concept) {
		
		// Rekursion (verändert Eingabekonzept)
		for(Description child : concept.getChildren()) {
			cleanConcept(child);
		}
		
		cleaningTimeNsStart = System.nanoTime();
		/*
		if(concept instanceof Bottom || concept instanceof Top || concept instanceof AtomicConcept)
			return concept;
		else if(concept instanceof Negation)
			return new Negation(concept.getChild(0));
		else if(concept instanceof Exists)
			return new Exists(((Quantification)concept).getRole(),cleanConcept(concept.getChild(0)));
		else if(concept instanceof All)
			return new All(((Quantification)concept).getRole(),cleanConcept(concept.getChild(0)));
		*/
		if(concept instanceof Intersection || concept instanceof Union) {

			List<Description> deleteChilds = new LinkedList<Description>();
			
			for(Description child : concept.getChildren()) {
				if((concept instanceof Intersection && child instanceof Intersection)
						|| (concept instanceof Union && child instanceof Union)) {
					deleteChilds.add(child);
				}
			}
			
			for(Description dc : deleteChilds) {
				// alle Kinder des zu löschenden Konzeptes hinzufügen
				for(Description dcChild : dc.getChildren()) {
					concept.addChild(dcChild);
				}
				// Konzept selber löschen
				concept.removeChild(dc);
			}
			
		}
		cleaningTimeNs += System.nanoTime() - cleaningTimeNsStart;
			
	}
	
	// wandelt ein Konzept in Negationsnormalform um
	public static Description transformToNegationNormalForm(Description concept) {
		if(concept instanceof Negation) {
			Description child = concept.getChild(0);
			
			if(child.getChildren().size()==0) {
				// NOT TOP = BOTTOM
				if(child instanceof Thing)
					return new Nothing();
				// NOT BOTTOM = TOP
				else if(child instanceof Nothing)
					return new Thing();
				// atomares Konzept: NOT A wird zurückgegeben
				else if(child instanceof NamedClass)
					return concept;
				else
					throw new RuntimeException("Conversion to negation normal form not supported for " + concept);
			} else {
				if(child instanceof Negation) {
					// doppelte Negation hebt sich auf
					return transformToNegationNormalForm(child.getChild(0));
				} else if(child instanceof ObjectQuantorRestriction) {
					ObjectPropertyExpression r = ((ObjectQuantorRestriction)child).getRole();
					// Negation nach innen
					Description c = new Negation(child.getChild(0));
					// Exists
					if(child instanceof ObjectSomeRestriction)
						return new ObjectAllRestriction(r,transformToNegationNormalForm(c));
					// All
					else
						return new ObjectSomeRestriction(r,transformToNegationNormalForm(c));					
				} else if(child instanceof Intersection) {
					// wg. Negation wird Konjunktion zu Disjunktion
					Union md = new Union();
					for(Description c : child.getChildren()) {
						md.addChild(transformToNegationNormalForm(new Negation(c)));
					}
					return md;
				} else if(child instanceof Union) {
					Intersection mc = new Intersection();
					for(Description c : child.getChildren()) {
						mc.addChild(transformToNegationNormalForm(new Negation(c)));
					}			
					return mc;
				} else
					throw new RuntimeException("Conversion to negation normal form not supported for " + concept);
			}
		// keine Negation
		} else {

			Description conceptClone = (Description) concept.clone();
			conceptClone.getChildren().clear();
			
			for(Description c : concept.getChildren()) {
				conceptClone.addChild(transformToNegationNormalForm(c));
			}		
			
			return conceptClone;
		}
	}
	

	@SuppressWarnings("unused")
	private boolean containsTop(Description concept) {
		for(Description c : concept.getChildren()) {
			if(c instanceof Thing)
				return true;
		}
		return false;
	}
	
	@SuppressWarnings("unused")
	private boolean containsBottom(Description concept) {
		for(Description c : concept.getChildren()) {
			if(c instanceof Nothing)
				return true;
		}
		return false;
	}	
	
	// nimmt Konzept in Negationsnormalform und wendet äquivalenzerhaltende
	// Regeln an, die TOP und BOTTOM aus Disjunktion/Konjunktion entfernen
	public static Description applyEquivalenceRules(Description concept) {
		
		Description conceptClone = (Description) concept.clone();
		conceptClone.getChildren().clear();
		
		for(Description c : concept.getChildren()) {
			conceptClone.addChild(applyEquivalenceRules(c));
		}		
		
		// return conceptClone;		
		
		// TOP, BOTTOM in Disjunktion entfernen
		if(concept instanceof Union) {
			Iterator<Description> it = conceptClone.getChildren().iterator();
			while(it.hasNext()) {
				Description c = it.next();
			// for(Concept c : concept.getChildren()) {
				// TOP in Disjunktion => ganze Disjunktion äquivalent zu Top
				if(c instanceof Thing)
					return new Thing();
				// BOTTOM in Disjunktion => entfernen
				else if(c instanceof Nothing)
					it.remove();
					
			}
			
			// falls nur noch ein Kind übrig bleibt, dann entfällt
			// MultiDisjunction
			if(conceptClone.getChildren().size()==1)
				return conceptClone.getChild(0);
			
			// falls keine Kinder übrig bleiben, dann war das letzte Kind
			// BOTTOM
			if(conceptClone.getChildren().size()==0)
				return new Nothing();
			
		} else if(concept instanceof Intersection) {
			Iterator<Description> it = conceptClone.getChildren().iterator();
			while(it.hasNext()) {
				Description c = it.next();
				// TOP in Konjunktion => entfernen
				if(c instanceof Thing)
					it.remove();
				// BOTTOM in Konjunktion => alles äquivalent zu BOTTOM
				else if(c instanceof Nothing)
					return new Nothing();							
			}
			
			if(conceptClone.getChildren().size()==1)
				return conceptClone.getChild(0);
			
			// falls keine Kinder übrig bleiben, dann war das letzte Kind
			// TOP
			if(conceptClone.getChildren().size()==0)
				return new Thing();					
		}		
		
		return conceptClone;
	}
	
	// TODO: aus Effizienzgründen könnte man noch eine nicht-rekursive Methode entwickeln, die
	// nur die obere Ebene umwandelt
	public static void transformToOrderedNegationNormalFormNonRecursive(Description concept, Comparator<Description> conceptComparator) {
		// onnfTimeNsStart = System.nanoTime();
		
		// Liste der Kinder sortieren
		Collections.sort(concept.getChildren(), conceptComparator);
		
		// onnfTimeNs += System.nanoTime() - onnfTimeNsStart;
	}
	
	// wandelt ein Konzept in geordnete Negationsnormalform um;
	// es wird angenommen, dass das Eingabekonzept in Negationsnormalform und
	// "sauber" ist
	public static void transformToOrderedNegationNormalForm(Description concept, Comparator<Description> conceptComparator) {
		
		// alle Kinderkonzepte in geordnete Negationsnormalform bringen
		for(Description child : concept.getChildren()) {
			transformToOrderedNegationNormalForm(child, conceptComparator);
		}
		
		onnfTimeNsStart = System.nanoTime();
		// Liste der Kinder sortieren
		Collections.sort(concept.getChildren(), conceptComparator);
		
		// Konvertierung von Liste in Array => Array sortieren => Rekonvertierung in Liste
		// List<Concept> childList = concept.getChildren();
		// Concept[] childArray = (Concept[]) childList.toArray(); 
		// Arrays.sort(childArray, conceptComparator);
		// childList = Arrays.asList(childArray);
		onnfTimeNs += System.nanoTime() - onnfTimeNsStart;
	}
	/*
	public static Description transformToMultiClean(Description concept) {
		concept = transformToMulti(concept);
		cleanConcept(concept);
		return concept;
	}
	
	// ersetzt einfache Disjunktionen/Konjunktionen durch Multi
	public static Description transformToMulti(Description concept) {
		// alle Kinderkonzepte in geordnete Negationsnormalform bringen
		List<Description> multiChildren = new LinkedList<Description>();
		
		// es müssen veränderte Kinder entfernt und neu hinzugefügt werden
		// (einfache Zuweisung mit = funktioniert nicht, da die Pointer die gleichen
		// bleiben)
		Iterator<Description> it = concept.getChildren().iterator();
		while(it.hasNext()) {
			Description child = it.next();
			multiChildren.add(transformToMulti(child));
			it.remove();
		}
		
		for(Description multiChild : multiChildren)
			concept.addChild(multiChild);
			
		if(concept instanceof Disjunction)
			return new MultiDisjunction(concept.getChildren());
		
		if(concept instanceof Conjunction)
			return new MultiConjunction(concept.getChildren());
		
		return concept;
	}
	*/
	// liefert ein ev. verkürztes Konzept, wenn in Disjunktionen bzw.
	// Konjunktionen Elemente mehrfach vorkommen
	// (erstmal nicht-rekursiv implementiert)
	public static Description getShortConceptNonRecursive(Description concept, ConceptComparator conceptComparator) {
		if(concept instanceof Union || concept instanceof Intersection) {
			// Verkürzung geschieht einfach durch einfügen in eine geordnete Menge
			Set<Description> newChildren = new TreeSet<Description>(conceptComparator);
			newChildren.addAll(concept.getChildren());
			// ev. geht das noch effizienter, wenn man keine neue Liste erstellen 
			// muss(?) => Listen erstellen dürfte allerdings sehr schnell gehen
			if(concept instanceof Intersection)
				return new Intersection(new LinkedList<Description>(newChildren));
			else
				return new Union(new LinkedList<Description>(newChildren));
		} else
			return concept;
	}
	
	public static Description getShortConcept(Description concept, ConceptComparator conceptComparator) {
		shorteningTimeNsStart = System.nanoTime();
		// deep copy des Konzepts, da es nicht verändert werden darf
		// (Nachteil ist, dass auch Konzepte kopiert werden, bei denen sich gar
		// nichts ändert)
		Description clone = (Description) concept.clone();
		clone = getShortConcept(clone, conceptComparator, 0);
		// return getShortConcept(concept, conceptComparator, 0);
		shorteningTimeNs += System.nanoTime() - shorteningTimeNsStart;
		return clone;
	}
	
	// das Eingabekonzept darf nicht modifiziert werden
	private static Description getShortConcept(Description concept, ConceptComparator conceptComparator, int recDepth) {
		
		//if(recDepth==0)
		//	System.out.println(concept);
		
		// Kinder schrittweise ersetzen
		// TODO: effizienter wäre nur zu ersetzen, wenn sich etwas geändert hat
		List<Description> tmp = new LinkedList<Description>(); 
		Iterator<Description> it = concept.getChildren().iterator();
		while(it.hasNext()) {
			Description c = it.next();
			// concept.addChild(getShortConcept(c, conceptComparator));
			Description newChild = getShortConcept(c, conceptComparator,recDepth+1);
			// Vergleich, ob es sich genau um die gleichen Objekte handelt
			// (es wird explizit == statt equals verwendet)
			if(c != newChild) {
				tmp.add(newChild);
				it.remove();	
			}
		}
		for(Description child : tmp)
			concept.addChild(child);
		
		if(concept instanceof Union || concept instanceof Intersection) {
			// Verkürzung geschieht einfach durch einfügen in eine geordnete Menge
			SortedSet<Description> newChildren = new TreeSet<Description>(conceptComparator);
			newChildren.addAll(concept.getChildren());
			// falls sich Kinderliste auf ein Element reduziert hat, dann gebe nur
			// dieses Element zurück (umschließende Konjunktion/Disjunktion entfällt)
			if(newChildren.size()==1)
				return newChildren.first();
			// ev. geht das noch effizienter, wenn man keine neue Liste erstellen 
			// muss(?) => Listen erstellen dürfte allerdings sehr schnell gehen
			if(concept instanceof Intersection)
				return new Intersection(new LinkedList<Description>(newChildren));
			else
				return new Union(new LinkedList<Description>(newChildren));
		} else
			return concept;
	}	
	
	/**
	 * Method to determine, whether a class description is minimal,
	 * e.g. \forall r.\top (\equiv \top) or male \sqcup male are not
	 * minimal.	This method performs heuristic sanity checks (it will
	 * not try to find semantically equivalent shorter descriptions).
	 * @param description Input description.
	 * @return True if a superfluous construct has been found.
	 */
	public static boolean isDescriptionMinimal(Description description) {
		ConceptComparator cc = new ConceptComparator();
		int length = description.getLength();
		int length2 = ConceptTransformation.getShortConcept(description, cc).getLength();
		if(length2 < length)
			return false;
		if(ConceptTransformation.findEquivalences(description))
			return false;
		return true;
	}	
 
	private static boolean findEquivalences(Description description) {
		// \exists r.\bot \equiv \bot
		if(description instanceof ObjectSomeRestriction && description.getChild(0) instanceof Nothing)
			return true;
		// \forall r.\top \equiv \top
		if(description instanceof ObjectAllRestriction && description.getChild(0) instanceof Thing)
			return true;
		// check children
		for(Description child : description.getChildren()) {
			if(findEquivalences(child))
				return true;
		}
		// false if none of the checks was successful
		return false;
	}
	
	// replaces EXISTS hasChild.TOP with EXISTS hasChild.Person, 
	// i.e. TOP is replaced by the range of the property; 
	// this is semantically equivalent, but easier to read for some people
	public static void replaceRange(Description description, ReasoningService rs) {
		if(description instanceof ObjectSomeRestriction && description.getChild(0) instanceof Thing) {
			ObjectPropertyExpression p = ((ObjectSomeRestriction)description).getRole();
			if(p instanceof ObjectProperty) {
				// replace TOP with range of propery
				description.removeChild(description.getChild(0));
				description.addChild(rs.getRange((ObjectProperty)p));
			}
		}
		
		for(Description child : description.getChildren()) {
			replaceRange(child, rs);
		}
	}
	
}
