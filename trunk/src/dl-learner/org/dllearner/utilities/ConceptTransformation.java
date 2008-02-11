package org.dllearner.utilities;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.owl.All;
import org.dllearner.core.owl.AtomicConcept;
import org.dllearner.core.owl.Bottom;
import org.dllearner.core.owl.Concept;
import org.dllearner.core.owl.Conjunction;
import org.dllearner.core.owl.Disjunction;
import org.dllearner.core.owl.Exists;
import org.dllearner.core.owl.MultiConjunction;
import org.dllearner.core.owl.MultiDisjunction;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.ObjectPropertyExpression;
import org.dllearner.core.owl.Quantification;
import org.dllearner.core.owl.Top;

// ev. kann man diese Klasse später in ein anderes Paket ziehen, da sie nicht direkt mit
// refinement zu tun hat
public class ConceptTransformation {

	public static long cleaningTimeNs = 0;
	private static long cleaningTimeNsStart = 0;	
	public static long onnfTimeNs = 0;
	private static long onnfTimeNsStart = 0;
	public static long shorteningTimeNs = 0;
	private static long shorteningTimeNsStart = 0;
	
	public static void cleanConceptNonRecursive(Concept concept) {
		// cleaningTimeNsStart = System.nanoTime();
		
		if(concept instanceof MultiConjunction || concept instanceof MultiDisjunction) {

			List<Concept> deleteChilds = new LinkedList<Concept>();
			
			for(Concept child : concept.getChildren()) {
				if((concept instanceof MultiConjunction && child instanceof MultiConjunction)
						|| (concept instanceof MultiDisjunction && child instanceof MultiDisjunction)) {
					deleteChilds.add(child);
				}
			}
			
			for(Concept dc : deleteChilds) {
				// alle Kinder des zu löschenden Konzeptes hinzufügen
				for(Concept dcChild : dc.getChildren()) {
					concept.addChild(dcChild);
				}
				// Konzept selber löschen
				concept.removeChild(dc);
			}
			
		}
		
		// cleaningTimeNs += System.nanoTime() - cleaningTimeNsStart;
	}
	
	

	// eliminiert Disjunktionen in Disjunktionen bzw. Konjunktionen in Konjunktionen
	public static void cleanConcept(Concept concept) {
		
		// Rekursion (verändert Eingabekonzept)
		for(Concept child : concept.getChildren()) {
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
		if(concept instanceof MultiConjunction || concept instanceof MultiDisjunction) {

			List<Concept> deleteChilds = new LinkedList<Concept>();
			
			for(Concept child : concept.getChildren()) {
				if((concept instanceof MultiConjunction && child instanceof MultiConjunction)
						|| (concept instanceof MultiDisjunction && child instanceof MultiDisjunction)) {
					deleteChilds.add(child);
				}
			}
			
			for(Concept dc : deleteChilds) {
				// alle Kinder des zu löschenden Konzeptes hinzufügen
				for(Concept dcChild : dc.getChildren()) {
					concept.addChild(dcChild);
				}
				// Konzept selber löschen
				concept.removeChild(dc);
			}
			
		}
		cleaningTimeNs += System.nanoTime() - cleaningTimeNsStart;
			
	}
	
	// wandelt ein Konzept in Negationsnormalform um
	public static Concept transformToNegationNormalForm(Concept concept) {
		if(concept instanceof Negation) {
			Concept child = concept.getChild(0);
			
			if(child.getChildren().size()==0) {
				// NOT TOP = BOTTOM
				if(child instanceof Top)
					return new Bottom();
				// NOT BOTTOM = TOP
				else if(child instanceof Bottom)
					return new Top();
				// atomares Konzept: NOT A wird zurückgegeben
				else if(child instanceof AtomicConcept)
					return concept;
				else
					throw new RuntimeException("Conversion to negation normal form not supported for " + concept);
			} else {
				if(child instanceof Negation) {
					// doppelte Negation hebt sich auf
					return transformToNegationNormalForm(child.getChild(0));
				} else if(child instanceof Quantification) {
					ObjectPropertyExpression r = ((Quantification)child).getRole();
					// Negation nach innen
					Concept c = new Negation(child.getChild(0));
					// Exists
					if(child instanceof Exists)
						return new All(r,transformToNegationNormalForm(c));
					// All
					else
						return new Exists(r,transformToNegationNormalForm(c));					
				} else if(child instanceof MultiConjunction) {
					// wg. Negation wird Konjunktion zu Disjunktion
					MultiDisjunction md = new MultiDisjunction();
					for(Concept c : child.getChildren()) {
						md.addChild(transformToNegationNormalForm(new Negation(c)));
					}
					return md;
				} else if(child instanceof MultiDisjunction) {
					MultiConjunction mc = new MultiConjunction();
					for(Concept c : child.getChildren()) {
						mc.addChild(transformToNegationNormalForm(new Negation(c)));
					}			
					return mc;
				} else
					throw new RuntimeException("Conversion to negation normal form not supported for " + concept);
			}
		// keine Negation
		} else {

			Concept conceptClone = (Concept) concept.clone();
			conceptClone.getChildren().clear();
			
			for(Concept c : concept.getChildren()) {
				conceptClone.addChild(transformToNegationNormalForm(c));
			}		
			
			return conceptClone;
		}
	}
	

	@SuppressWarnings("unused")
	private boolean containsTop(Concept concept) {
		for(Concept c : concept.getChildren()) {
			if(c instanceof Top)
				return true;
		}
		return false;
	}
	
	@SuppressWarnings("unused")
	private boolean containsBottom(Concept concept) {
		for(Concept c : concept.getChildren()) {
			if(c instanceof Bottom)
				return true;
		}
		return false;
	}	
	
	// nimmt Konzept in Negationsnormalform und wendet äquivalenzerhaltende
	// Regeln an, die TOP und BOTTOM aus Disjunktion/Konjunktion entfernen
	public static Concept applyEquivalenceRules(Concept concept) {
		
		Concept conceptClone = (Concept) concept.clone();
		conceptClone.getChildren().clear();
		
		for(Concept c : concept.getChildren()) {
			conceptClone.addChild(applyEquivalenceRules(c));
		}		
		
		// return conceptClone;		
		
		// TOP, BOTTOM in Disjunktion entfernen
		if(concept instanceof MultiDisjunction) {
			Iterator<Concept> it = conceptClone.getChildren().iterator();
			while(it.hasNext()) {
				Concept c = it.next();
			// for(Concept c : concept.getChildren()) {
				// TOP in Disjunktion => ganze Disjunktion äquivalent zu Top
				if(c instanceof Top)
					return new Top();
				// BOTTOM in Disjunktion => entfernen
				else if(c instanceof Bottom)
					it.remove();
					
			}
			
			// falls nur noch ein Kind übrig bleibt, dann entfällt
			// MultiDisjunction
			if(conceptClone.getChildren().size()==1)
				return conceptClone.getChild(0);
			
			// falls keine Kinder übrig bleiben, dann war das letzte Kind
			// BOTTOM
			if(conceptClone.getChildren().size()==0)
				return new Bottom();
			
		} else if(concept instanceof MultiConjunction) {
			Iterator<Concept> it = conceptClone.getChildren().iterator();
			while(it.hasNext()) {
				Concept c = it.next();
				// TOP in Konjunktion => entfernen
				if(c instanceof Top)
					it.remove();
				// BOTTOM in Konjunktion => alles äquivalent zu BOTTOM
				else if(c instanceof Bottom)
					return new Bottom();							
			}
			
			if(conceptClone.getChildren().size()==1)
				return conceptClone.getChild(0);
			
			// falls keine Kinder übrig bleiben, dann war das letzte Kind
			// TOP
			if(conceptClone.getChildren().size()==0)
				return new Top();					
		}		
		
		return conceptClone;
	}
	
	// TODO: aus Effizienzgründen könnte man noch eine nicht-rekursive Methode entwickeln, die
	// nur die obere Ebene umwandelt
	public static void transformToOrderedNegationNormalFormNonRecursive(Concept concept, Comparator<Concept> conceptComparator) {
		// onnfTimeNsStart = System.nanoTime();
		
		// Liste der Kinder sortieren
		Collections.sort(concept.getChildren(), conceptComparator);
		
		// onnfTimeNs += System.nanoTime() - onnfTimeNsStart;
	}
	
	// wandelt ein Konzept in geordnete Negationsnormalform um;
	// es wird angenommen, dass das Eingabekonzept in Negationsnormalform und
	// "sauber" ist
	public static void transformToOrderedNegationNormalForm(Concept concept, Comparator<Concept> conceptComparator) {
		
		// alle Kinderkonzepte in geordnete Negationsnormalform bringen
		for(Concept child : concept.getChildren()) {
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
	
	// testet, ob Konzept keine einfachen Konjunktionen bzw. Disjunktionen
	// enthält
	public boolean isMulti(Concept concept) {
		if(concept instanceof AtomicConcept || concept instanceof Top || concept instanceof Bottom)
			return true;
		
		if(concept instanceof Disjunction || concept instanceof Conjunction)
			return false;
		
		for(Concept child : concept.getChildren()) {
			boolean test = isMulti(child);
			if(!test)
				return false;
		}
		
		return true;
	}
	
	public static Concept transformToMultiClean(Concept concept) {
		concept = transformToMulti(concept);
		cleanConcept(concept);
		return concept;
	}
	
	// ersetzt einfache Disjunktionen/Konjunktionen durch Multi
	public static Concept transformToMulti(Concept concept) {
		// alle Kinderkonzepte in geordnete Negationsnormalform bringen
		List<Concept> multiChildren = new LinkedList<Concept>();
		
		// es müssen veränderte Kinder entfernt und neu hinzugefügt werden
		// (einfache Zuweisung mit = funktioniert nicht, da die Pointer die gleichen
		// bleiben)
		Iterator<Concept> it = concept.getChildren().iterator();
		while(it.hasNext()) {
			Concept child = it.next();
			multiChildren.add(transformToMulti(child));
			it.remove();
		}
		
		for(Concept multiChild : multiChildren)
			concept.addChild(multiChild);
			
		if(concept instanceof Disjunction)
			return new MultiDisjunction(concept.getChildren());
		
		if(concept instanceof Conjunction)
			return new MultiConjunction(concept.getChildren());
		
		return concept;
	}
	
	// liefert ein ev. verkürztes Konzept, wenn in Disjunktionen bzw.
	// Konjunktionen Elemente mehrfach vorkommen
	// (erstmal nicht-rekursiv implementiert)
	public static Concept getShortConceptNonRecursive(Concept concept, ConceptComparator conceptComparator) {
		if(concept instanceof MultiDisjunction || concept instanceof MultiConjunction) {
			// Verkürzung geschieht einfach durch einfügen in eine geordnete Menge
			Set<Concept> newChildren = new TreeSet<Concept>(conceptComparator);
			newChildren.addAll(concept.getChildren());
			// ev. geht das noch effizienter, wenn man keine neue Liste erstellen 
			// muss(?) => Listen erstellen dürfte allerdings sehr schnell gehen
			if(concept instanceof MultiConjunction)
				return new MultiConjunction(new LinkedList<Concept>(newChildren));
			else
				return new MultiDisjunction(new LinkedList<Concept>(newChildren));
		} else
			return concept;
	}
	
	public static Concept getShortConcept(Concept concept, ConceptComparator conceptComparator) {
		shorteningTimeNsStart = System.nanoTime();
		// deep copy des Konzepts, da es nicht verändert werden darf
		// (Nachteil ist, dass auch Konzepte kopiert werden, bei denen sich gar
		// nichts ändert)
		Concept clone = (Concept) concept.clone();
		clone = getShortConcept(clone, conceptComparator, 0);
		// return getShortConcept(concept, conceptComparator, 0);
		shorteningTimeNs += System.nanoTime() - shorteningTimeNsStart;
		return clone;
	}
	
	// das Eingabekonzept darf nicht modifiziert werden
	private static Concept getShortConcept(Concept concept, ConceptComparator conceptComparator, int recDepth) {
		
		//if(recDepth==0)
		//	System.out.println(concept);
		
		// Kinder schrittweise ersetzen
		// TODO: effizienter wäre nur zu ersetzen, wenn sich etwas geändert hat
		List<Concept> tmp = new LinkedList<Concept>(); 
		Iterator<Concept> it = concept.getChildren().iterator();
		while(it.hasNext()) {
			Concept c = it.next();
			// concept.addChild(getShortConcept(c, conceptComparator));
			Concept newChild = getShortConcept(c, conceptComparator,recDepth+1);
			// Vergleich, ob es sich genau um die gleichen Objekte handelt
			// (es wird explizit == statt equals verwendet)
			if(c != newChild) {
				tmp.add(newChild);
				it.remove();	
			}
		}
		for(Concept child : tmp)
			concept.addChild(child);
		
		if(concept instanceof MultiDisjunction || concept instanceof MultiConjunction) {
			// Verkürzung geschieht einfach durch einfügen in eine geordnete Menge
			SortedSet<Concept> newChildren = new TreeSet<Concept>(conceptComparator);
			newChildren.addAll(concept.getChildren());
			// falls sich Kinderliste auf ein Element reduziert hat, dann gebe nur
			// dieses Element zurück (umschließende Konjunktion/Disjunktion entfällt)
			if(newChildren.size()==1)
				return newChildren.first();
			// ev. geht das noch effizienter, wenn man keine neue Liste erstellen 
			// muss(?) => Listen erstellen dürfte allerdings sehr schnell gehen
			if(concept instanceof MultiConjunction)
				return new MultiConjunction(new LinkedList<Concept>(newChildren));
			else
				return new MultiDisjunction(new LinkedList<Concept>(newChildren));
		} else
			return concept;
	}	
	
}
