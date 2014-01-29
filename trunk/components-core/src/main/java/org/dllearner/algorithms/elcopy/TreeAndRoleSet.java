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

package org.dllearner.algorithms.elcopy;

import java.util.Set;

import org.dllearner.core.owl.Property;

/**
 * Convenience class representing an EL description tree and a set of roles.
 * 
 * @author Jens Lehmann
 *
 */
public class TreeAndRoleSet {

	private ELDescriptionTree tree;
	private Set<Property> roles;
	
	public TreeAndRoleSet(ELDescriptionTree tree, Set<Property> roles) {
		this.tree = tree;
		this.roles = roles;
	}

	/**
	 * @return the tree
	 */
	public ELDescriptionTree getTree() {
		return tree;
	}

	/**
	 * @return the roles
	 */
	public Set<Property> getRoles() {
		return roles;
	}
	
	@Override
	public String toString() {
		return "("+tree.toDescriptionString() + "," + roles.toString()+")";
	}
	
}
