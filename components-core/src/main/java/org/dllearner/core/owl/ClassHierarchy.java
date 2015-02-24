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

package org.dllearner.core.owl;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * Represents a subsumption hierarchy (ignoring equivalent concepts).
 * 
 * @author Jens Lehmann
 * 
 */
public class ClassHierarchy {

	public static Logger logger = LoggerFactory.getLogger(ClassHierarchy.class);
	
	TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>> subsumptionHierarchyUp;
	TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>> subsumptionHierarchyDown;
	
	OWLDataFactory df = new OWLDataFactoryImpl();
	/**
	 * The arguments specify the superclasses and subclasses of each class. This
	 * is used to build the subsumption hierarchy. 
	 * @param subsumptionHierarchyUp Contains super classes for each class.
	 * @param subsumptionHierarchyDown Contains sub classes for each class.
	 */
	public ClassHierarchy(
			TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>> subsumptionHierarchyUp,
			TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>> subsumptionHierarchyDown) {
		
		this.subsumptionHierarchyUp = subsumptionHierarchyUp;
		this.subsumptionHierarchyDown = subsumptionHierarchyDown;
		
	}

	public SortedSet<OWLClassExpression> getSuperClasses(OWLClassExpression concept) {
		SortedSet<OWLClassExpression> result =  subsumptionHierarchyUp.get(concept);
		if(result == null) {
			logger.error("Query for super class of " + concept + " in subsumption hierarchy, but the class is not contained in the (upward) hierarchy, e.g. because the class does not exist or is ignored. Returning empty result instead.");
			return new TreeSet<OWLClassExpression>();
		}
		
		// we copy all concepts before returning them such that they cannot be
		// modified externally
		return new TreeSet<OWLClassExpression>(result);
	}

	public SortedSet<OWLClassExpression> getSubClasses(OWLClassExpression concept) {
		SortedSet<OWLClassExpression> result =  subsumptionHierarchyDown.get(concept);
		if(result == null) {
			logger.error("Query for sub class of " + concept + " in subsumption hierarchy, but the class is not contained in the (downward) hierarchy, e.g. because the class does not exist or is ignored. Returning empty result instead.");
			return new TreeSet<OWLClassExpression>();
		}
		
		return new TreeSet<OWLClassExpression>(result);		
		
		// commented out, because these hacks just worked around a problem
//		if (subsumptionHierarchyDown == null) {
//			return new TreeSet<OWLClassExpression>();
//		} else if (subsumptionHierarchyDown.get(concept) == null) {
//			return new TreeSet<OWLClassExpression>();
//		} else {
//			return (TreeSet<OWLClassExpression>) subsumptionHierarchyDown.get(concept).clone();
//		}
	}
	
	public SortedSet<OWLClassExpression> getSubClasses(OWLClassExpression concept, boolean direct) {
		SortedSet<OWLClassExpression> result =  subsumptionHierarchyDown.get(concept);
		if(result == null) {
			logger.error("Query for sub class of " + concept + " in subsumption hierarchy, but the class is not contained in the (downward) hierarchy, e.g. because the class does not exist or is ignored. Returning empty result instead.");
			return new TreeSet<OWLClassExpression>();
		}
		result.remove(concept);
		for(OWLClassExpression sub : new HashSet<OWLClassExpression>(result)){
			result.addAll(getSubClasses(sub, false));
		}
		
		return new TreeSet<OWLClassExpression>(result);		
		
		// commented out, because these hacks just worked around a problem
//		if (subsumptionHierarchyDown == null) {
//			return new TreeSet<OWLClassExpression>();
//		} else if (subsumptionHierarchyDown.get(concept) == null) {
//			return new TreeSet<OWLClassExpression>();
//		} else {
//			return (TreeSet<OWLClassExpression>) subsumptionHierarchyDown.get(concept).clone();
//		}
	}
	
	public SortedSet<OWLClassExpression> getSuperClasses(OWLClassExpression concept, boolean direct) {
		SortedSet<OWLClassExpression> result =  subsumptionHierarchyUp.get(concept);
		if(result == null) {
			logger.error("Query for super class of " + concept + " in subsumption hierarchy, but the class is not contained in the (downward) hierarchy, e.g. because the class does not exist or is ignored. Returning empty result instead.");
			return new TreeSet<OWLClassExpression>();
		}
		result.remove(concept);
		for(OWLClassExpression sub : new HashSet<OWLClassExpression>(result)){
			result.addAll(getSuperClasses(sub, false));
		}
		
		return new TreeSet<OWLClassExpression>(result);		
	}

	/**
	 * Computes the siblings of the specified OWLClassExpressions. Siblings are all those
	 * classes, which are subclasses of a parent of a class and not equal to the
	 * class itself. Note that retrieving siblings is computationally more 
	 * expensive than descending/ascending the hierarchy as siblings are computed
	 * when required and not cached.
	 * @param OWLClassExpression A named class.
	 * @return A set of named classes, which are siblings of the given class.
	 */
	public SortedSet<OWLClassExpression> getSiblingClasses(OWLClassExpression OWLClassExpression) {
		Set<OWLClassExpression> superClasses = subsumptionHierarchyUp.get(OWLClassExpression);
		TreeSet<OWLClassExpression> siblingClasses = new TreeSet<OWLClassExpression>();
		for(OWLClassExpression superClass : superClasses) {
			siblingClasses.addAll(subsumptionHierarchyDown.get(superClass));
		}
		siblingClasses.remove(OWLClassExpression);
		return siblingClasses;
	}
	
	/**
	 * This method modifies the subsumption hierarchy such that for each class,
	 * there is only a single path to reach it via upward and downward
	 * refinement respectively.
	 */
	public void thinOutSubsumptionHierarchy() {
		TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>> hierarchyDownNew = new TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>>(
				);
		TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>> hierarchyUpNew = new TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>>(
				);

		Set<OWLClassExpression> conceptsInSubsumptionHierarchy = new TreeSet<OWLClassExpression>();
		conceptsInSubsumptionHierarchy.addAll(subsumptionHierarchyUp.keySet());
		conceptsInSubsumptionHierarchy.addAll(subsumptionHierarchyDown.keySet());
		
		// add empty sets for each concept
		for (OWLClassExpression c : conceptsInSubsumptionHierarchy) {
			hierarchyDownNew.put(c, new TreeSet<OWLClassExpression>());
			hierarchyUpNew.put(c, new TreeSet<OWLClassExpression>());
		}

		for (OWLClassExpression c : conceptsInSubsumptionHierarchy) {
			// look whether there are more general concepts
			// (if yes, pick the first one)
			SortedSet<OWLClassExpression> moreGeneral = subsumptionHierarchyUp.get(c);
			if (moreGeneral != null && moreGeneral.size() != 0) {
				OWLClassExpression chosenParent = moreGeneral.first();
				hierarchyDownNew.get(chosenParent).add(c);
			}
		}

		for (OWLClassExpression c : conceptsInSubsumptionHierarchy) {
			SortedSet<OWLClassExpression> moreSpecial = subsumptionHierarchyDown.get(c);
			if (moreSpecial != null && moreSpecial.size() != 0) {
				OWLClassExpression chosenParent = moreSpecial.first();
				hierarchyUpNew.get(chosenParent).add(c);
			}
		}
		
		//owl:Thing
		hierarchyDownNew.put(df.getOWLThing(), subsumptionHierarchyDown.get(df.getOWLThing()));
		//owl:Nothing
		hierarchyUpNew.put(df.getOWLNothing(), subsumptionHierarchyUp.get(df.getOWLNothing()));
		
		subsumptionHierarchyDown = hierarchyDownNew;
		subsumptionHierarchyUp = hierarchyUpNew;
	}

	/**
	 * Implements a subsumption check using the hierarchy (no further reasoning
	 * checks are used).
	 * 
	 * @param subClass
	 *            The (supposedly) more special class.
	 * @param superClass
	 *            The (supposedly) more general class.
	 * @return True if <code>subClass</code> is a subclass of
	 *         <code>superclass</code>.
	 */
	public boolean isSubclassOf(OWLClass subClass, OWLClass superClass) {
		if (subClass.equals(superClass)) {
			return true;
		} else {
			SortedSet<OWLClassExpression> superClasses = subsumptionHierarchyUp.get(subClass);
			if(superClasses != null){
				for (OWLClassExpression moreGeneralClass : subsumptionHierarchyUp.get(subClass)) {
					
					// search the upper classes of the subclass
					if (moreGeneralClass instanceof OWLClass) {
						if (isSubclassOf((OWLClass) moreGeneralClass, superClass)) {
							return true;
						}
						// we reached top, so we can return false (if top is a
						// direct upper
						// class, then no other upper classes can exist)
					} else {
						return false;
					}
				}
			}
			// we cannot reach the class via any of the upper classes,
			// so it is not a super class
			return false;
		}
	}
	
	/**
	 * Implements a subsumption check using the hierarchy (no further reasoning
	 * checks are used).
	 * 
	 * @param subClass
	 *            The (supposedly) more special class.
	 * @param superClass
	 *            The (supposedly) more general class.
	 * @return True if <code>subClass</code> is a subclass of
	 *         <code>superclass</code>.
	 */
	public boolean isSubclassOf(OWLClassExpression subClass, OWLClassExpression superClass) {
		if (subClass.equals(superClass)) {
			return true;
		} else {
			SortedSet<OWLClassExpression> superClasses = subsumptionHierarchyUp.get(subClass);
			if(superClasses != null){
				for (OWLClassExpression moreGeneralClass : superClasses) {
					
					// search the upper classes of the subclass
					if (moreGeneralClass instanceof OWLClass) {
						if (isSubclassOf(moreGeneralClass, superClass)) {
							return true;
						}
						// we reached top, so we can return false (if top is a
						// direct upper
						// class, then no other upper classes can exist)
					} else {
						return false;
					}
				}
			}
			// we cannot reach the class via any of the upper classes,
			// so it is not a super class
			return false;
		}
	}

	@Override
	public String toString() {
		return toString(false);
	}

	public String toString(boolean showUpwardHierarchy) {
		if (showUpwardHierarchy) {
			String str = "downward subsumption:\n";
			str += toString(subsumptionHierarchyDown, df.getOWLThing(), 0);
			str += "upward subsumption:\n";
			str += toString(subsumptionHierarchyUp, df.getOWLNothing(), 0);
			return str;
		} else {
			return toString(subsumptionHierarchyDown, df.getOWLThing(), 0);
		}
	}

	private String toString(TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>> hierarchy,
			OWLClassExpression concept, int depth) {
		String str = "";
		for (int i = 0; i < depth; i++)
			str += "  ";
		str += concept.toString() + "\n";
		Set<OWLClassExpression> tmp = hierarchy.get(concept);
		if (tmp != null) {
			for (OWLClassExpression c : tmp)
				str += toString(hierarchy, c, depth + 1);
		}
		return str;
	}

	@Override
	public ClassHierarchy clone() {
		return new ClassHierarchy(subsumptionHierarchyUp, subsumptionHierarchyDown);		
	}
	
	/**
	 * The method computes a new class hierarchy, which is a copy of this
	 * one, but only the specified classes are allowed to occur. For instance,
	 * if we have subclass relationships between 1sYearStudent, Student, and
	 * Person, but Student is not allowed, then there a is a subclass relationship
	 * between 1stYearStudent and Person.
	 * Currently, owl:Thing and owl:Nothing are always allowed for technical
	 * reasons.
	 * @param allowedClasses The classes, which are allowed to occur in the new
	 * class hierarchy.
	 * @return A copy of this hierarchy, which is restricted to a certain set
	 * of classes.
	 */
	public ClassHierarchy cloneAndRestrict(Set<OWLClass> allowedClasses) {
		// currently TOP and BOTTOM are always allowed
		// (TODO would be easier if Thing/Nothing were declared as named classes)
		Set<OWLClassExpression> allowed = new TreeSet<OWLClassExpression>();
		allowed.addAll(allowedClasses);
		allowed.add(df.getOWLThing());
		allowed.add(df.getOWLNothing());
		
		// create new maps
		TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>> subsumptionHierarchyUpNew
		= new TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>>();
		TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>> subsumptionHierarchyDownNew 
		= new TreeMap<OWLClassExpression, SortedSet<OWLClassExpression>>();
		
		for(Entry<OWLClassExpression, SortedSet<OWLClassExpression>> entry : subsumptionHierarchyUp.entrySet()) {
			OWLClassExpression key = entry.getKey();
			// we only store mappings for allowed classes
			if(allowed.contains(key)) {
				// copy the set of all super classes (we consume them until
				// they are empty)
				TreeSet<OWLClassExpression> superClasses = new TreeSet<OWLClassExpression>(entry.getValue());
				// storage for new super classes
				TreeSet<OWLClassExpression> newSuperClasses = new TreeSet<OWLClassExpression>();
				
				while(!superClasses.isEmpty()) {
					// pick and remove the first element
					OWLClassExpression d = superClasses.pollFirst();
					// case 1: it is allowed, so we add it
					if(allowed.contains(d)) {
						newSuperClasses.add(d);
					// case 2: it is not allowed, so we try its super classes
					} else {
						Set<OWLClassExpression> tmp = subsumptionHierarchyUp.get(d);
						if(tmp != null){
							superClasses.addAll(tmp);
						}
					}
				}
				
				subsumptionHierarchyUpNew.put(key, newSuperClasses);
			}
		}
		
		// downward case is analogous
		for(Entry<OWLClassExpression, SortedSet<OWLClassExpression>> entry : subsumptionHierarchyDown.entrySet()) {
			OWLClassExpression key = entry.getKey();
			if(allowed.contains(key)) {
				TreeSet<OWLClassExpression> subClasses = new TreeSet<OWLClassExpression>(entry.getValue());
				TreeSet<OWLClassExpression> newSubClasses = new TreeSet<OWLClassExpression>();
				
				while(!subClasses.isEmpty()) {
					OWLClassExpression d = subClasses.pollFirst();
					if(allowed.contains(d)) {
						newSubClasses.add(d);
					} else {
						subClasses.addAll(subsumptionHierarchyDown.get(d));
					}
				}
				
				subsumptionHierarchyDownNew.put(key, newSubClasses);
			}
		}		
		
		return new ClassHierarchy(subsumptionHierarchyUpNew, subsumptionHierarchyDownNew);
	}
	
	public Set<OWLAxiom> toOWLAxioms(){
		return toOWLAxioms(df.getOWLThing());
	}
	
	public Set<OWLAxiom> toOWLAxioms(OWLClassExpression concept){
		Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
		Set<OWLClassExpression> subConcepts = subsumptionHierarchyDown.get(concept);
		if (subConcepts != null) {
			for (OWLClassExpression sub : subConcepts){
				axioms.add(df.getOWLSubClassOfAxiom(sub, concept));
				axioms.addAll(toOWLAxioms(sub));
			}
		}
		return axioms;
	}
	
	/**
	 * Checks whether the OWLClassExpression is contained in the hierarchy.
	 * @param OWLClassExpression
	 * @return
	 */
	public boolean contains(OWLClassExpression OWLClassExpression){
		return subsumptionHierarchyUp.containsKey(OWLClassExpression);
	}
	
	public int getDepth2Root(OWLClassExpression OWLClassExpression){
		SortedSet<OWLClassExpression> superClasses = subsumptionHierarchyUp.get(OWLClassExpression);
		int depth = 0;
		if(superClasses != null){
			depth = 1;
			for(OWLClassExpression superClass : superClasses){
				depth += getDepth2Root(superClass);
			}
		}
		return depth;
	}
	
	public SortedSet<OWLClassExpression> getMostGeneralClasses(){
		SortedSet<OWLClassExpression> generalClasses = new TreeSet<OWLClassExpression>();
		boolean add = false;
		SortedSet<OWLClassExpression> superClasses;
		for(OWLClassExpression sub : getSubClasses(df.getOWLThing())){
			superClasses = getSuperClasses(sub);
			superClasses = new TreeSet<OWLClassExpression>();
			superClasses.remove(df.getOWLThing());
			if(superClasses.isEmpty()){
				add = true;
			}
			if(add){
				generalClasses.add(sub);
			}
		}
		return generalClasses;
	}
}
