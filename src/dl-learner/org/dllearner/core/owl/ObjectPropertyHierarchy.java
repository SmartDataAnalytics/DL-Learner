/**
 * Copyright (C) 2007, Jens Lehmann
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
package org.dllearner.core.owl;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dllearner.utilities.owl.RoleComparator;

/**
 * Represents a hierarchy of roles.
 * 
 * @todo Currently, the role hierarchy pruning algorithm (analogous to the
 * subsumption hierarchy) is not implemented. 
 * 
 * @author Jens Lehmann
 *
 */
public class ObjectPropertyHierarchy {

	RoleComparator rc = new RoleComparator();
	TreeMap<ObjectProperty,TreeSet<ObjectProperty>> roleHierarchyUp;
	TreeMap<ObjectProperty,TreeSet<ObjectProperty>> roleHierarchyDown;	
	TreeSet<ObjectProperty> mostGeneralRoles = new TreeSet<ObjectProperty>(rc);
	TreeSet<ObjectProperty> mostSpecialRoles = new TreeSet<ObjectProperty>(rc);
	
	public ObjectPropertyHierarchy(Set<ObjectProperty> atomicRoles, TreeMap<ObjectProperty,TreeSet<ObjectProperty>> roleHierarchyUp , TreeMap<ObjectProperty,TreeSet<ObjectProperty>> roleHierarchyDown) {
		this.roleHierarchyUp = roleHierarchyUp;
		this.roleHierarchyDown = roleHierarchyDown;
		
		// find most general and most special roles
		for(ObjectProperty role : atomicRoles) {
			if(getMoreGeneralRoles(role).size()==0)
				mostGeneralRoles.add(role);
			if(getMoreSpecialRoles(role).size()==0)
				mostSpecialRoles.add(role);			
		}
	}
	
	@SuppressWarnings("unchecked")	
	public SortedSet<ObjectProperty> getMoreGeneralRoles(ObjectProperty role) {
		// we clone all concepts before returning them such that they cannot be
		// modified externally
		return (TreeSet<ObjectProperty>) roleHierarchyUp.get(role).clone();	
	}
	
	@SuppressWarnings("unchecked")
	public SortedSet<ObjectProperty> getMoreSpecialRoles(ObjectProperty role) {
		return (TreeSet<ObjectProperty>) roleHierarchyDown.get(role).clone();
	}			
	
	
	
	@Override
	public String toString() {
		String str = "";
		for(ObjectProperty role : mostGeneralRoles) {
			str += toString(roleHierarchyDown, role, 0);
		}
		return str;
	}
	
	private String toString(TreeMap<ObjectProperty,TreeSet<ObjectProperty>> hierarchy, ObjectProperty role, int depth) {
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
