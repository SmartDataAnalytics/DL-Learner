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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractReasonerComponent;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNaryBooleanClassExpression;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLQuantifiedRestriction;
import org.semanticweb.owlapi.model.OWLRestriction;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

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
	
	private static final OWLDataFactory df = new OWLDataFactoryImpl();
	private static final OWLObjectDuplicator DUPLICATOR = new OWLObjectDuplicator(df);
	private static final OWLClassExpressionCleaner CLASS_EXPRESSION_CLEANER = new OWLClassExpressionCleaner(df);
	
	public static OWLClassExpression cleanConceptNonRecursive(OWLClassExpression concept) {
		// cleaningTimeNsStart = System.nanoTime();
		return concept.accept(CLASS_EXPRESSION_CLEANER);
		// cleaningTimeNs += System.nanoTime() - cleaningTimeNsStart;
	}
	
	public static OWLClassExpression cleanConcept(OWLClassExpression concept) {
		// cleaningTimeNsStart = System.nanoTime();
		return concept.accept(CLASS_EXPRESSION_CLEANER);
		// cleaningTimeNs += System.nanoTime() - cleaningTimeNsStart;
	}
	
	// wandelt ein Konzept in Negationsnormalform um
	public static OWLClassExpression transformToNegationNormalForm(OWLClassExpression concept) {
		return concept.getNNF();
	}
	
	// nimmt Konzept in Negationsnormalform und wendet äquivalenzerhaltende
	// Regeln an, die TOP und BOTTOM aus Disjunktion/Konjunktion entfernen
	public static OWLClassExpression applyEquivalenceRules(OWLClassExpression concept) {
		
		OWLClassExpression conceptClone = DUPLICATOR.duplicateObject(concept);
//		conceptClone.getChildren().clear();
//		
//		for(OWLClassExpression c : OWLClassExpressionUtils.getChildren(concept.getChildren()) {
//			conceptClone.addChild(applyEquivalenceRules(c));
//		}		
		
		// return conceptClone;		
		
		// TOP, BOTTOM in Disjunktion entfernen
		if(conceptClone instanceof OWLObjectUnionOf) {
			List<OWLClassExpression> operands = ((OWLObjectUnionOf) conceptClone).getOperandsAsList();
			Iterator<OWLClassExpression> it = operands.iterator();
			while(it.hasNext()) {
				OWLClassExpression c = it.next();
			// for(Concept c : concept.getChildren()) {
				// TOP in Disjunktion => ganze Disjunktion äquivalent zu Top
				if(c.isOWLThing())
					return df.getOWLThing();
				// BOTTOM in Disjunktion => entfernen
				else if(c.isOWLNothing())
					it.remove();
					
			}
			
			// falls nur noch ein Kind übrig bleibt, dann entfällt
			// MultiDisjunction
			if(operands.size()==1)
				return operands.get(0);
			
			// falls keine Kinder übrig bleiben, dann war das letzte Kind
			// BOTTOM
			if(operands.isEmpty())
				return df.getOWLNothing();
			
		} else if(conceptClone instanceof OWLObjectIntersectionOf) {
			List<OWLClassExpression> operands = ((OWLObjectUnionOf) conceptClone).getOperandsAsList();
			Iterator<OWLClassExpression> it = operands.iterator();
			while(it.hasNext()) {
				OWLClassExpression c = it.next();
				// TOP in Konjunktion => entfernen
				if(c.isOWLThing())
					it.remove();
				// BOTTOM in Konjunktion => alles äquivalent zu BOTTOM
				else if(c.isOWLNothing())
					return df.getOWLNothing();							
			}
			
			if(operands.size()==1)
				return operands.get(0);
			
			// falls keine Kinder übrig bleiben, dann war das letzte Kind
			// TOP
			if(operands.isEmpty())
				return df.getOWLThing();
			
			return df.getOWLObjectIntersectionOf(new TreeSet<OWLClassExpression>(operands));
		}		
		
		return conceptClone;
	}
	
	// TODO: aus Effizienzgründen könnte man noch eine nicht-rekursive Methode entwickeln, die
	// nur die obere Ebene umwandelt
	public static void transformToOrderedNegationNormalFormNonRecursive(OWLClassExpression concept) {
		// onnfTimeNsStart = System.nanoTime();
		
		// Liste der Kinder sortieren
//		Collections.sort(concept.getChildren(), conceptComparator);
		
		// onnfTimeNs += System.nanoTime() - onnfTimeNsStart;
	}
	
	// wandelt ein Konzept in geordnete Negationsnormalform um;
	// es wird angenommen, dass das Eingabekonzept in Negationsnormalform und
	// "sauber" ist
	public static void transformToOrderedForm(OWLClassExpression concept) {
		
		/**
		// alle Kinderkonzepte in geordnete Negationsnormalform bringen
		for(OWLClassExpression child : concept.getChildren()) {
			transformToOrderedForm(child, conceptComparator);
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
		**/
	}
	/*
	public static OWLClassExpression transformToMultiClean(OWLClassExpression concept) {
		concept = transformToMulti(concept);
		cleanConcept(concept);
		return concept;
	}
	
	// ersetzt einfache Disjunktionen/Konjunktionen durch Multi
	public static OWLClassExpression transformToMulti(OWLClassExpression concept) {
		// alle Kinderkonzepte in geordnete Negationsnormalform bringen
		List<OWLClassExpression> multiChildren = new LinkedList<OWLClassExpression>();
		
		// es müssen veränderte Kinder entfernt und neu hinzugefügt werden
		// (einfache Zuweisung mit = funktioniert nicht, da die Pointer die gleichen
		// bleiben)
		Iterator<OWLClassExpression> it = concept.getChildren().iterator();
		while(it.hasNext()) {
			Description child = it.next();
			multiChildren.add(transformToMulti(child));
			it.remove();
		}
		
		for(OWLClassExpression multiChild : multiChildren)
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
	public static OWLClassExpression getShortConceptNonRecursive(OWLClassExpression concept) {
		return concept;
	}
	
	/**
	 * Tries to shorten a concept, e.g. male AND male is shortened to male. 
	 * @param concept The input concepts.
	 * @param conceptComparator A comparator for concepts.
	 * @return A shortened version of the concept (equal to the input concept if it cannot be shortened).
	 */
	public static OWLClassExpression getShortConcept(OWLClassExpression concept) {
		shorteningTimeNsStart = System.nanoTime();
		// deep copy des Konzepts, da es nicht verändert werden darf
		// (Nachteil ist, dass auch Konzepte kopiert werden, bei denen sich gar
		// nichts ändert)
		OWLClassExpression clone = DUPLICATOR.duplicateObject(concept);
		clone = getShortConcept(clone, 0);
		// return getShortConcept(concept, conceptComparator, 0);
		shorteningTimeNs += System.nanoTime() - shorteningTimeNsStart;
		return clone;
	}
	
	// das Eingabekonzept darf nicht modifiziert werden
	private static OWLClassExpression getShortConcept(OWLClassExpression concept, int recDepth) {
		//probably no longer necessary as OWL API uses sets for class expressions
		return concept;
		/**
		//if(recDepth==0)
		//	System.out.println(concept);
		
		// Kinder schrittweise ersetzen
		// TODO: effizienter wäre nur zu ersetzen, wenn sich etwas geändert hat
		List<OWLClassExpression> tmp = new LinkedList<OWLClassExpression>(); 
		Iterator<OWLClassExpression> it = concept.getChildren().iterator();
		while(it.hasNext()) {
			OWLClassExpression c = it.next();
			// concept.addChild(getShortConcept(c, conceptComparator));
			OWLClassExpression newChild = getShortConcept(c, recDepth+1);
			// Vergleich, ob es sich genau um die gleichen Objekte handelt
			// (es wird explizit == statt equals verwendet)
			if(c != newChild) {
				tmp.add(newChild);
				it.remove();	
			}
		}
		for(OWLClassExpression child : tmp)
			concept.addChild(child);
		
		if(concept instanceof OWLNaryBooleanClassExpression) {
			// Verkürzung geschieht einfach durch einfügen in eine geordnete Menge
			SortedSet<OWLClassExpression> newChildren = new TreeSet<OWLClassExpression>();
			newChildren.addAll(((OWLNaryBooleanClassExpression) concept).getOperands());
			// falls sich Kinderliste auf ein Element reduziert hat, dann gebe nur
			// dieses Element zurück (umschließende Konjunktion/Disjunktion entfällt)
			if(newChildren.size()==1)
				return newChildren.first();
			// ev. geht das noch effizienter, wenn man keine neue Liste erstellen 
			// muss(?) => Listen erstellen dürfte allerdings sehr schnell gehen
			if(concept instanceof OWLObjectIntersectionOf)
				return df.getOWLObjectIntersectionOf(newChildren);
			else
				return df.getOWLObjectUnionOf(newChildren);
		} else
			return concept;
		**/
	}	
	
	/**
	 * Method to determine, whether a class OWLClassExpression is minimal,
	 * e.g. \forall r.\top (\equiv \top) or male \sqcup male are not
	 * minimal.	This method performs heuristic sanity checks (it will
	 * not try to find semantically equivalent shorter descriptions).
	 * @param OWLClassExpression Input description.
	 * @return True if a superfluous construct has been found.
	 */
	public static boolean isDescriptionMinimal(OWLClassExpression description) {
		int length = OWLClassExpressionUtils.getLength(description);
		int length2 = OWLClassExpressionUtils.getLength(ConceptTransformation.getShortConcept(description));
		if(length2 < length)
			return false;
		if(ConceptTransformation.findEquivalences(description))
			return false;
		return true;
	}	
 
	private static boolean findEquivalences(OWLClassExpression description) {
		// \exists r.\bot \equiv \bot
		if(description instanceof OWLObjectSomeValuesFrom && ((OWLObjectSomeValuesFrom)description).getFiller().isOWLNothing())
			return true;
		// \forall r.\top \equiv \top
		if(description instanceof OWLObjectAllValuesFrom && ((OWLObjectAllValuesFrom)description).getFiller().isOWLThing())
			return true;
		// check children
		for(OWLClassExpression child : OWLClassExpressionUtils.getChildren(description)) {
			if(findEquivalences(child))
				return true;
		}
		// false if none of the checks was successful
		return false;
	}
	
	// replaces EXISTS hasChild.TOP with EXISTS hasChild.Person, 
	// i.e. TOP is replaced by the range of the property; 
	// this is semantically equivalent, but easier to read for some people
	public static OWLClassExpression replaceRange(OWLClassExpression description, AbstractReasonerComponent rs) {
		OWLClassExpression rewrittenClassExpression = description;
		if(description instanceof OWLNaryBooleanClassExpression){
			Set<OWLClassExpression> newOperands = new TreeSet<OWLClassExpression>(((OWLObjectIntersectionOf) description).getOperands());
			for (OWLClassExpression operand : ((OWLNaryBooleanClassExpression) description).getOperands()) {
				newOperands.add(replaceRange(operand, rs));
			}
			if(description instanceof OWLObjectIntersectionOf){
				rewrittenClassExpression = df.getOWLObjectIntersectionOf(newOperands);
			} else {
				rewrittenClassExpression = df.getOWLObjectUnionOf(newOperands);
			}
		} else if(description instanceof OWLObjectSomeValuesFrom) {
			// \exists r.\bot \equiv \bot
			OWLObjectProperty property = ((OWLObjectSomeValuesFrom) description).getProperty().asOWLObjectProperty();
			OWLClassExpression filler = ((OWLObjectSomeValuesFrom) description).getFiller();
			if(filler.isOWLThing()) {
				OWLClassExpression range = rs.getRange(property);
				filler = range;
			} else if(filler.isAnonymous()){
				filler = replaceRange(filler, rs);
			}
			rewrittenClassExpression = df.getOWLObjectSomeValuesFrom(property, filler);
		}
		return rewrittenClassExpression;
	}
	
	/**
	 * Tests whether a description is a subdescription in the sense that when
	 * parts of <code>description</code> can be removed to yield <code>subdescription</code>.
	 * 
	 * @param description A description.
	 * @param subDescription A potential subdescription.
	 * @return True if <code>subdescription</code> is indeed a sub description and false
	 * otherwise.
	 */
	public static boolean isSubdescription(OWLClassExpression description, OWLClassExpression subDescription) {
//		if(OWLClassExpression instanceof Thing) {
//			return (subDescription instanceof Thing);
//		} else if(OWLClassExpression instanceof Nothing) {
//			return (subDescription instanceof Thing);
//		} else if(OWLClassExpression instanceof NamedClass) {
//			return ((subDescription instanceof NamedClass) && (((NamedClass)description).toStringID().equals(((NamedClass)subDescription).toStringID())));
//		}
		
		List<OWLClassExpression> children = new ArrayList<OWLClassExpression>(OWLClassExpressionUtils.getChildren(description));
		List<OWLClassExpression> subChildren = new ArrayList<OWLClassExpression>(OWLClassExpressionUtils.getChildren(subDescription));

		// no children: both have to be equal
		if(children.size()==0) {
			return (description.compareTo(subDescription)==0);
		// one child: both have to be of the same class, type, and the first
		// child has to be sub OWLClassExpression of the other child
		} else if(children.size()==1) {
			return (subChildren.size() == 1) && description.getClass().equals(subDescription.getClass()) && isSubdescription(children.get(0), subChildren.get(0));
		// intersection or union
		} else {
			// test whether subdescription corresponds to an element of the 
			// intersection/union
			if(subChildren.size()<2) {
				for(OWLClassExpression child : children) {
					if(isSubdescription(child, subDescription)) {
						return true;
					}
				}
				return false;
			}
			
			// make sure that both are of the same type and subdescription actually has fewer children
			if(!description.getClass().equals(subDescription.getClass()) || subChildren.size() > children.size()) {
				return false;
			}
			
			// comparing everything is quadratic; the faster linear variant (below)
			// using 
			
			for(OWLClassExpression subChild : subChildren) {
				boolean foundMatch = false;
				for(OWLClassExpression child : children) {
					if(isSubdescription(child, subChild)) {
						foundMatch = true;
						break;
					}
				}
				if(!foundMatch) {
					return false;
				}
			}
			
			return true;
			
//			// method core; traverse the descriptions in linear time using ordered
//			// normal form (TODO: does not always work e.g. A2 \sqcap (A1 \sqcup A3)
			// and A1 \sqcap A2 -> it won't find the A2 match because it has advanced
			// beyond it already)
//			int j = 0;
//			for(OWLClassExpression child : children) {
//				if(isSubdescription(child, subChildren.get(j))) {
//					j++;
//				}
//				if(j == subChildren.size()) {
//					return true;
//				}
//			}
//			// there is at least one child we could not match
//			return false;
		}
	}
	
	/**
	 * Counts occurrences of \forall in description.
	 * @param OWLClassExpression A description.
	 * @return Number of \forall occurrences.
	 */
	public static int getForallOccurences(OWLClassExpression description) {
		int count = 0;
		for (OWLClassExpression expression : description.getNestedClassExpressions()) {
			if(expression instanceof OWLObjectAllValuesFrom) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Gets the "contexts" of all \forall occurrences in a description. A context
	 * is a set of properties, i.e. in \exists hasChild.\exists hasBrother.\forall hasChild.male,
	 * the context of the only \forall occurrence is [hasChild, hasBrother, hasChild]. 
	 * @param OWLClassExpression A description.
	 * @return Set of property contexts.
	 */
	public static SortedSet<PropertyContext> getForallContexts(OWLClassExpression description) {
		return getForallContexts(description, new PropertyContext());
	}
	
	@SuppressWarnings("rawtypes")
	private static SortedSet<PropertyContext> getForallContexts(OWLClassExpression description, PropertyContext currentContext) {
		// the context changes if we have a restriction
		if(description instanceof OWLRestriction) {
			if(((OWLRestriction) description).isObjectRestriction()){
				OWLObjectProperty op = ((OWLObjectPropertyExpression)((OWLRestriction) description).getProperty()).asOWLObjectProperty();
				PropertyContext currentContextCopy = (PropertyContext) currentContext.clone();
				// if we have an all-restriction, we return it; otherwise we call the child
				// (if it exists)
				if(description instanceof OWLObjectAllValuesFrom) {
					OWLClassExpression filler = ((OWLObjectAllValuesFrom) description).getFiller();
					currentContextCopy.add(op);
//					System.out.println("cc: " + currentContext);
					TreeSet<PropertyContext> contexts = new TreeSet<PropertyContext>();
					contexts.add(currentContextCopy);
					contexts.addAll(getForallContexts(filler, currentContextCopy));
					return contexts;
				// restriction with one child
				} else if(description instanceof OWLQuantifiedRestriction) {
					OWLClassExpression filler = (OWLClassExpression) ((OWLQuantifiedRestriction) description).getFiller();
					currentContextCopy.add(op);
					return getForallContexts(filler, currentContextCopy);
				// restrictions without a child (has value)
				} else {
					return new TreeSet<PropertyContext>();
				}
			} else {
				return new TreeSet<PropertyContext>();
			}
		// for non-restrictions, we collect contexts over all children
		} else {
			TreeSet<PropertyContext> contexts = new TreeSet<PropertyContext>();
			if(description instanceof OWLNaryBooleanClassExpression){
				for(OWLClassExpression child : ((OWLNaryBooleanClassExpression) description).getOperands()) {
//					System.out.println("testing child " + child + " " + currentContext);
					contexts.addAll(getForallContexts(child, currentContext));
				}
			}
			return contexts;
		}
	}
}
