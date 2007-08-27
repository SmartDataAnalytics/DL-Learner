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
package org.dllearner.reasoning;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dllearner.dl.AtomicRole;
import org.dllearner.utilities.RoleComparator;

/**
 * Represents a hierarchy of roles.
 * 
 * @todo Currently, the role hierarchy pruning algorithm (analogous to the
 * subsumption hierarchy) is not implemented. 
 * 
 * @author Jens Lehmann
 *
 */
public class RoleHierarchy {

	RoleComparator rc = new RoleComparator();
	TreeMap<AtomicRole,TreeSet<AtomicRole>> roleHierarchyUp;
	TreeMap<AtomicRole,TreeSet<AtomicRole>> roleHierarchyDown;	
	TreeSet<AtomicRole> mostGeneralRoles = new TreeSet<AtomicRole>(rc);
	TreeSet<AtomicRole> mostSpecialRoles = new TreeSet<AtomicRole>(rc);
	
	public RoleHierarchy(Set<AtomicRole> atomicRoles, TreeMap<AtomicRole,TreeSet<AtomicRole>> roleHierarchyUp , TreeMap<AtomicRole,TreeSet<AtomicRole>> roleHierarchyDown) {
		this.roleHierarchyUp = roleHierarchyUp;
		this.roleHierarchyDown = roleHierarchyDown;
		
		// find most general and most special roles
		for(AtomicRole role : atomicRoles) {
			if(getMoreGeneralRoles(role).size()==0)
				mostGeneralRoles.add(role);
			if(getMoreSpecialRoles(role).size()==0)
				mostSpecialRoles.add(role);			
		}
	}
	
	@SuppressWarnings("unchecked")	
	public SortedSet<AtomicRole> getMoreGeneralRoles(AtomicRole role) {
		// we clone all concepts before returning them such that they cannot be
		// modified externally
		return (TreeSet<AtomicRole>) roleHierarchyUp.get(role).clone();	
	}
	
	@SuppressWarnings("unchecked")
	public SortedSet<AtomicRole> getMoreSpecialRoles(AtomicRole role) {
		return (TreeSet<AtomicRole>) roleHierarchyDown.get(role).clone();
	}			
	
	
	
	@Override
	public String toString() {
		String str = "";
		for(AtomicRole role : mostGeneralRoles) {
			str += toString(roleHierarchyDown, role, 0);
		}
		return str;
	}
	
	private String toString(TreeMap<AtomicRole,TreeSet<AtomicRole>> hierarchy, AtomicRole role, int depth) {
		String str = "";
		for(int i=0; i<depth; i++)
			str += "  ";
		str += role.toString() + "\n";
		Set<AtomicRole> tmp = hierarchy.get(role);
		if(tmp!=null) {
			for(AtomicRole c : tmp)
				str += toString(hierarchy, c, depth+1);
		}
		return str;
	}

	/**
	 * @return The most general roles.
	 */
	public TreeSet<AtomicRole> getMostGeneralRoles() {
		return mostGeneralRoles;
	}

	/**
	 * @return The most special roles.
	 */
	public TreeSet<AtomicRole> getMostSpecialRoles() {
		return mostSpecialRoles;
	}

}
