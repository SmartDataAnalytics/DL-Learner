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

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import uk.ac.manchester.cs.owl.owlapi.OWLObjectPropertyImpl;

/**
 * Represents a hierarchy of object properties (roles in Description Logics).
 * 
 * @author Jens Lehmann
 *
 */
public class ObjectPropertyHierarchy {

	TreeMap<OWLObjectProperty,SortedSet<OWLObjectProperty>> roleHierarchyUp;
	TreeMap<OWLObjectProperty,SortedSet<OWLObjectProperty>> roleHierarchyDown;	
	TreeSet<OWLObjectProperty> mostGeneralRoles = new TreeSet<OWLObjectProperty>();
	TreeSet<OWLObjectProperty> mostSpecialRoles = new TreeSet<OWLObjectProperty>();
	
	OWLObjectProperty topRole = new OWLObjectPropertyImpl(OWLRDFVocabulary.OWL_TOP_OBJECT_PROPERTY.getIRI());
	OWLObjectProperty botRole = new OWLObjectPropertyImpl(OWLRDFVocabulary.OWL_BOTTOM_OBJECT_PROPERTY.getIRI());
	
	public ObjectPropertyHierarchy(Set<OWLObjectProperty> atomicRoles, TreeMap<OWLObjectProperty,SortedSet<OWLObjectProperty>> roleHierarchyUp , TreeMap<OWLObjectProperty,SortedSet<OWLObjectProperty>> roleHierarchyDown) {
		this.roleHierarchyUp = roleHierarchyUp;
		this.roleHierarchyDown = roleHierarchyDown;
		
		// find most general and most special roles
		for(OWLObjectProperty role : atomicRoles) {
			SortedSet<OWLObjectProperty> moreGen = getMoreGeneralRoles(role);
			SortedSet<OWLObjectProperty> moreSpec = getMoreSpecialRoles(role);
			if(moreGen.size()==0 || (moreGen.size()==1 && moreGen.first().equals(topRole)))
				mostGeneralRoles.add(role);
			if(moreSpec.size()==0 || (moreSpec.size()==1 && moreSpec.first().equals(botRole)))
				mostSpecialRoles.add(role);			
		}
	}
	
	public SortedSet<OWLObjectProperty> getMoreGeneralRoles(OWLObjectProperty role) {
		// we clone all concepts before returning them such that they cannot be
		// modified externally
		return new TreeSet<OWLObjectProperty>(roleHierarchyUp.get(role));	
	}
	
	public SortedSet<OWLObjectProperty> getMoreSpecialRoles(OWLObjectProperty role) {
		return new TreeSet<OWLObjectProperty>(roleHierarchyDown.get(role));
	}
	
	/**
	 * Implements a subsumption check using the hierarchy (no further
	 * reasoning checks are used).
	 * @param subProperty The (supposedly) more special property.
	 * @param superProperty The (supposedly) more general property.
	 * @return True if <code>subProperty</code> is a subproperty of <code>superProperty</code>.
	 */
	public boolean isSubpropertyOf(OWLObjectProperty subProperty, OWLObjectProperty superProperty) {
		if(subProperty.equals(superProperty)) {
			return true;
		} else {
//			System.out.println("oph: " + subProperty + " " + superProperty);
			for(OWLObjectProperty moreGeneralProperty : roleHierarchyUp.get(subProperty)) {	
				if(isSubpropertyOf(moreGeneralProperty, superProperty)) {
					return true;
				}
			}
			// we cannot reach the class via any of the upper classes,
			// so it is not a super class
			return false;
		}
	}	
	
	@Override
	public String toString() {
		String str = "";
		for(OWLObjectProperty role : mostGeneralRoles) {
			str += toString(roleHierarchyDown, role, 0);
		}
		return str;
	}
	
	private String toString(TreeMap<OWLObjectProperty,SortedSet<OWLObjectProperty>> hierarchy, OWLObjectProperty role, int depth) {
		String str = "";
		for(int i=0; i<depth; i++)
			str += "  ";
		str += role.toString() + "\n";
		Set<OWLObjectProperty> tmp = hierarchy.get(role);
		if(tmp!=null) {
			for(OWLObjectProperty c : tmp)
				str += toString(hierarchy, c, depth+1);
		}
		return str;
	}

	/**
	 * @return The most general roles.
	 */
	public TreeSet<OWLObjectProperty> getMostGeneralRoles() {
		return mostGeneralRoles;
	}

	/**
	 * @return The most special roles.
	 */
	public TreeSet<OWLObjectProperty> getMostSpecialRoles() {
		return mostSpecialRoles;
	}

}
