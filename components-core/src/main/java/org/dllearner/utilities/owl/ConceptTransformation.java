/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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

import com.google.common.collect.Sets;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.NNF;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.*;

/**
 * Concept transformation and concept checking methods.
 * 
 * @author Jens Lehmann
 */
public class ConceptTransformation {

	public static long cleaningTimeNs = 0;
	public static long onnfTimeNs = 0;
	private static long onnfTimeNsStart = 0;
	public static long shorteningTimeNs = 0;

	private static final OWLDataFactory df = new OWLDataFactoryImpl();
	private static final OWLObjectDuplicator DUPLICATOR = new OWLObjectDuplicator(df);
	private static final OWLClassExpressionCleaner CLASS_EXPRESSION_CLEANER = new OWLClassExpressionCleaner(df);
	
	public static OWLClassExpression cleanConceptNonRecursive(OWLClassExpression concept) {
		return cleanConcept(concept);
	}
	
	public static OWLClassExpression cleanConcept(OWLClassExpression concept) {
		long cleaningTimeNsStart = System.nanoTime();
		OWLClassExpression cleanedConcept = concept.accept(CLASS_EXPRESSION_CLEANER);
		cleaningTimeNs += System.nanoTime() - cleaningTimeNsStart;
		return cleanedConcept;
	}
	
	/**
	 * Returns the class expression in negation normal form.
	 * @param ce the class expression
	 * @return the class expression in negation normal form
	 */
	public static OWLClassExpression nnf(OWLClassExpression ce) {
		NNF nnfGen = new NNF(df);
		return ce.accept(nnfGen);
	}
	
	/**
	 * Expand the class expression by adding \exists r.\top for all properties r
	 * that are involved in some \forall r.C on the same modal depth. 
	 * @param ce the class expression to expand
	 * @return
	 */
	public static OWLClassExpression appendSomeValuesFrom(OWLClassExpression ce) {
		// if forall semantics is someonly
		if (ce instanceof OWLObjectIntersectionOf) {
			Set<OWLClassExpression> newOperands = new HashSet<>();
			Set<OWLObjectPropertyExpression> universallyQuantifiedProperties = new HashSet<>();
			Set<OWLObjectPropertyExpression> existentiallyQuantifiedProperties = new HashSet<>();
			for (OWLClassExpression operand : ((OWLObjectIntersectionOf) ce).getOperands()) {
				newOperands.add(appendSomeValuesFrom(operand));
				if(operand instanceof OWLObjectAllValuesFrom) {
					universallyQuantifiedProperties.add(((OWLObjectAllValuesFrom) operand).getProperty());
				} else if(operand instanceof OWLObjectSomeValuesFrom) {
					existentiallyQuantifiedProperties.add(((OWLObjectSomeValuesFrom) operand).getProperty());
				}
			}
			for (OWLObjectPropertyExpression ope : Sets.difference(universallyQuantifiedProperties, existentiallyQuantifiedProperties)) {
				newOperands.add(df.getOWLObjectSomeValuesFrom(ope, df.getOWLThing()));
			}
			return df.getOWLObjectIntersectionOf(newOperands);
		} 
//		else if(ce instanceof OWLObjectUnionOf) {
//			Set<OWLClassExpression> newOperands = new HashSet<OWLClassExpression>();
//			
//			for (OWLClassExpression operand : ((OWLObjectUnionOf) ce).getOperands()) {
//				OWLClassExpression newOperand = appendSomeValuesFrom(operand);
//				if(newOperand instanceof OWLObjectAllValuesFrom) {
//					newOperand = df.getOWLObjectIntersectionOf(ce,
//							df.getOWLObjectSomeValuesFrom(((OWLObjectAllValuesFrom) newOperand).getProperty(), df.getOWLThing()));
//				} 
//				newOperands.add(newOperand);
//			}
//			
//			return df.getOWLObjectUnionOf(newOperands);
//		}
		return ce.getNNF();
	}
	
	// nimmt Konzept in Negationsnormalform und wendet äquivalenzerhaltende
	// Regeln an, die TOP und BOTTOM aus Disjunktion/Konjunktion entfernen
	public static OWLClassExpression applyEquivalenceRules(OWLClassExpression concept) {
		
		OWLClassExpression conceptClone = DUPLICATOR.duplicateObject(concept);
		
		// remove \top and \bot from disjunction 
		if(conceptClone instanceof OWLObjectUnionOf) {
			SortedSet<OWLClassExpression> newOperands = new TreeSet<>();
			for (OWLClassExpression op : ((OWLObjectUnionOf) conceptClone).getOperandsAsList()) {
				OWLClassExpression c = applyEquivalenceRules(op);
				
				if(c.isOWLThing()) {// \top in C => C \equiv \top
					return df.getOWLThing();
				} 
				else if(c.isOWLNothing()) {// \bot in C => remove
					
				} else {
					newOperands.add(c);
				}
			}
			
			// if there are no children the last child was \bot
			if (newOperands.isEmpty()) {
				return df.getOWLNothing();
			}if (newOperands.size() == 1) {
				return newOperands.first();
			} else {
				return df.getOWLObjectUnionOf(newOperands);
			}
		} else if(conceptClone instanceof OWLObjectIntersectionOf) {// remove \top and \bot from intersection 
			SortedSet<OWLClassExpression> newOperands = new TreeSet<>();
			for (OWLClassExpression op : ((OWLObjectIntersectionOf) conceptClone).getOperandsAsList()) {
				OWLClassExpression c = applyEquivalenceRules(op);
				
				if(c.isOWLThing()) {// \top in C => remove
					
				} else if(c.isOWLNothing()) {// \bot in C => C \equiv \bot
					return df.getOWLNothing();
				} else {
					newOperands.add(c);
				}
			}
			
			// if there are no children the last child was \top
			if (newOperands.isEmpty()) {
				return df.getOWLThing();
			} else if (newOperands.size() == 1) {
				return newOperands.first();
			} else {
				return df.getOWLObjectIntersectionOf(newOperands);
			}
		}		
		
		return conceptClone;
	}
	
	/**
	 * Tries to shorten a concept, e.g. male AND male is shortened to male. 
	 * @param concept The input concepts.
	 * @return A shortened version of the concept (equal to the input concept if it cannot be shortened).
	 */
	public static OWLClassExpression getShortConcept(OWLClassExpression concept) {
		long shorteningTimeNsStart = System.nanoTime();
		OWLClassExpression clone = DUPLICATOR.duplicateObject(concept);
		shorteningTimeNs += System.nanoTime() - shorteningTimeNsStart;
		return clone;
	}
	
	/**
	 * Method to determine, whether a class expression is minimal,
	 * e.g. \forall r.\top (\equiv \top) or male \sqcup male are not
	 * minimal.	This method performs heuristic sanity checks (it will
	 * not try to find semantically equivalent shorter descriptions).
	 * @param description Input description.
	 * @return True if a superfluous construct has been found.
	 */
	public static boolean isDescriptionMinimal(OWLClassExpression description) {
		int length = OWLClassExpressionUtils.getLength(description);
		int length2 = OWLClassExpressionUtils.getLength(ConceptTransformation.getShortConcept(description));
		if(length2 < length)
			return false;
        return !ConceptTransformation.findEquivalences(description);
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
			Set<OWLClassExpression> newOperands = new TreeSet<>();
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
			
			OWLObjectPropertyExpression pe = ((OWLObjectSomeValuesFrom) description).getProperty();
			OWLClassExpression filler = ((OWLObjectSomeValuesFrom) description).getFiller();
			
			if(pe.isAnonymous()) {
				if(filler.isOWLThing()) {
					filler = rs.getDomain(pe.getNamedProperty());
				} else if(filler.isAnonymous()){
					filler = replaceRange(filler, rs);
				}
			} else {
				if(filler.isOWLThing()) {
					filler = rs.getRange(pe.asOWLObjectProperty());
				} else if(filler.isAnonymous()){
					filler = replaceRange(filler, rs);
				}
			}
			
			rewrittenClassExpression = df.getOWLObjectSomeValuesFrom(pe, filler);
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
		
		List<OWLClassExpression> children = new ArrayList<>(OWLClassExpressionUtils.getChildren(description));
		List<OWLClassExpression> subChildren = new ArrayList<>(OWLClassExpressionUtils.getChildren(subDescription));

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
	 * @param description A description.
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
	 * @param description A description.
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
				OWLObjectPropertyExpression pe = (OWLObjectPropertyExpression)((OWLRestriction) description).getProperty();
				OWLObjectProperty op = pe.getNamedProperty();
				PropertyContext currentContextCopy = (PropertyContext) currentContext.clone();
				// if we have an all-restriction, we return it; otherwise we call the child
				// (if it exists)
				if(description instanceof OWLObjectAllValuesFrom) {
					OWLClassExpression filler = ((OWLObjectAllValuesFrom) description).getFiller();
					currentContextCopy.add(op);
//					System.out.println("cc: " + currentContext);
					TreeSet<PropertyContext> contexts = new TreeSet<>();
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
					return new TreeSet<>();
				}
			} else {
				return new TreeSet<>();
			}
		// for non-restrictions, we collect contexts over all children
		} else {
			TreeSet<PropertyContext> contexts = new TreeSet<>();
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
