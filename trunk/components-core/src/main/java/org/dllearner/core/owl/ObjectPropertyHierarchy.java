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

import org.dllearner.utilities.owl.RoleComparator;

/**
 * Represents a hierarchy of object properties (roles in Description Logics).
 * 
 * @author Jens Lehmann
 *
 */
public class ObjectPropertyHierarchy {

	RoleComparator rc = new RoleComparator();
	TreeMap<ObjectProperty,SortedSet<ObjectProperty>> roleHierarchyUp;
	TreeMap<ObjectProperty,SortedSet<ObjectProperty>> roleHierarchyDown;	
	TreeSet<ObjectProperty> mostGeneralRoles = new TreeSet<ObjectProperty>(rc);
	TreeSet<ObjectProperty> mostSpecialRoles = new TreeSet<ObjectProperty>(rc);
	
	ObjectProperty topRole = new ObjectProperty("http://www.w3.org/2002/07/owl#topObjectProperty");
	ObjectProperty botRole = new ObjectProperty("http://www.w3.org/2002/07/owl#bottomObjectProperty");
	
	public ObjectPropertyHierarchy(Set<ObjectProperty> atomicRoles, TreeMap<ObjectProperty,SortedSet<ObjectProperty>> roleHierarchyUp , TreeMap<ObjectProperty,SortedSet<ObjectProperty>> roleHierarchyDown) {
		this.roleHierarchyUp = roleHierarchyUp;
		this.roleHierarchyDown = roleHierarchyDown;
		
		// find most general and most special roles
		for(ObjectProperty role : atomicRoles) {
			SortedSet<ObjectProperty> moreGen = getMoreGeneralRoles(role);
			SortedSet<ObjectProperty> moreSpec = getMoreSpecialRoles(role);
			if(moreGen.size()==0 || (moreGen.size()==1 && moreGen.first().equals(topRole)))
				mostGeneralRoles.add(role);
			if(moreSpec.size()==0 || (moreSpec.size()==1 && moreSpec.first().equals(botRole)))
				mostSpecialRoles.add(role);			
		}
	}
	
	public SortedSet<ObjectProperty> getMoreGeneralRoles(ObjectProperty role) {
		// we clone all concepts before returning them such that they cannot be
		// modified externally
		return new TreeSet<ObjectProperty>(roleHierarchyUp.get(role));	
	}
	
	public SortedSet<ObjectProperty> getMoreSpecialRoles(ObjectProperty role) {
		return new TreeSet<ObjectProperty>(roleHierarchyDown.get(role));
	}
	
	/**
	 * Implements a subsumption check using the hierarchy (no further
	 * reasoning checks are used).
	 * @param subProperty The (supposedly) more special property.
	 * @param superProperty The (supposedly) more general property.
	 * @return True if <code>subProperty</code> is a subproperty of <code>superProperty</code>.
	 */
	public boolean isSubpropertyOf(ObjectProperty subProperty, ObjectProperty superProperty) {
		if(subProperty.equals(superProperty)) {
			return true;
		} else {
//			System.out.println("oph: " + subProperty + " " + superProperty);
			for(ObjectProperty moreGeneralProperty : roleHierarchyUp.get(subProperty)) {	
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
		for(ObjectProperty role : mostGeneralRoles) {
			str += toString(roleHierarchyDown, role, 0);
		}
		return str;
	}
	
	private String toString(TreeMap<ObjectProperty,SortedSet<ObjectProperty>> hierarchy, ObjectProperty role, int depth) {
		String str = "";
		for(int i=0; i<depth; i++)
			str += "  ";
		str += role.toString() + "\n";
		Set<ObjectProperty> tmp = hierarchy.get(role);
		if(tmp!=null) {
			for(ObjectProperty c : tmp)
				str += toString(hierarchy, c, depth+1);
		}
		return str;
	}

	/**
	 * @return The most general roles.
	 */
	public TreeSet<ObjectProperty> getMostGeneralRoles() {
		return mostGeneralRoles;
	}

	/**
	 * @return The most special roles.
	 */
	public TreeSet<ObjectProperty> getMostSpecialRoles() {
		return mostSpecialRoles;
	}

}
