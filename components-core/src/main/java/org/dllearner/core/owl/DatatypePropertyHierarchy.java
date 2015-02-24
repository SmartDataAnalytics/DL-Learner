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

import org.semanticweb.owlapi.model.OWLDataProperty;

/**
 * Represents a hierarchy of datatype properties.
 * 
 * TODO: Currently, the role hierarchy pruning algorithm (analogous to the
 * subsumption hierarchy) is not implemented.  
 * 
 * @author Jens Lehmann
 *
 */
public class DatatypePropertyHierarchy {

	TreeMap<OWLDataProperty,SortedSet<OWLDataProperty>> roleHierarchyUp;
	TreeMap<OWLDataProperty,SortedSet<OWLDataProperty>> roleHierarchyDown;	
	TreeSet<OWLDataProperty> mostGeneralRoles = new TreeSet<OWLDataProperty>();
	TreeSet<OWLDataProperty> mostSpecialRoles = new TreeSet<OWLDataProperty>();
	
	public DatatypePropertyHierarchy(Set<OWLDataProperty> atomicRoles, TreeMap<OWLDataProperty,SortedSet<OWLDataProperty>> roleHierarchyUp , TreeMap<OWLDataProperty,SortedSet<OWLDataProperty>> roleHierarchyDown) {
		this.roleHierarchyUp = roleHierarchyUp;
		this.roleHierarchyDown = roleHierarchyDown;
		
		// find most general and most special roles
		for(OWLDataProperty role : atomicRoles) {
			if(getMoreGeneralRoles(role).size()==0)
				mostGeneralRoles.add(role);
			if(getMoreSpecialRoles(role).size()==0)
				mostSpecialRoles.add(role);			
		}
	}
	
	public SortedSet<OWLDataProperty> getMoreGeneralRoles(OWLDataProperty role) {
		// we clone all concepts before returning them such that they cannot be
		// modified externally
		return new TreeSet<OWLDataProperty>(roleHierarchyUp.get(role));	
	}
	
	public SortedSet<OWLDataProperty> getMoreSpecialRoles(OWLDataProperty role) {
		return new TreeSet<OWLDataProperty>(roleHierarchyDown.get(role));
	}	
	
	@Override
	public String toString() {
		String str = "";
		for(OWLDataProperty role : mostGeneralRoles) {
			str += toString(roleHierarchyDown, role, 0);
		}
		return str;
	}
	
	/**
	 * Implements a subsumption check using the hierarchy (no further
	 * reasoning checks are used).
	 * @param subProperty The (supposedly) more special property.
	 * @param superProperty The (supposedly) more general property.
	 * @return True if <code>subProperty</code> is a subproperty of <code>superProperty</code>.
	 */
	public boolean isSubpropertyOf(OWLDataProperty subProperty, OWLDataProperty superProperty) {
		if(subProperty.equals(superProperty)) {
			return true;
		} else {
//			System.out.println("oph: " + subProperty + " " + superProperty);
			for(OWLDataProperty moreGeneralProperty : roleHierarchyUp.get(subProperty)) {	
				if(isSubpropertyOf(moreGeneralProperty, superProperty)) {
					return true;
				}
			}
			// we cannot reach the class via any of the upper classes,
			// so it is not a super class
			return false;
		}
	}	
	
	private String toString(TreeMap<OWLDataProperty,SortedSet<OWLDataProperty>> hierarchy, OWLDataProperty role, int depth) {
		String str = "";
		for(int i=0; i<depth; i++)
			str += "  ";
		str += role.toString() + "\n";
		Set<OWLDataProperty> tmp = hierarchy.get(role);
		if(tmp!=null) {
			for(OWLDataProperty c : tmp)
				str += toString(hierarchy, c, depth+1);
		}
		return str;
	}

	/**
	 * @return The most general roles.
	 */
	public TreeSet<OWLDataProperty> getMostGeneralRoles() {
		return mostGeneralRoles;
	}

	/**
	 * @return The most special roles.
	 */
	public TreeSet<OWLDataProperty> getMostSpecialRoles() {
		return mostSpecialRoles;
	}
	
	
}
