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

package org.dllearner.prolog;

/**
 * 
 * @author Sebastian Bader
 * 
 */
public class PredicateDefinition {
	private String name;
	private int arity;

	public PredicateDefinition(String name, int arity) {
		super();
		this.name = name;
		this.arity = arity;
	}

	public PredicateDefinition(Atom atom) {
		this(atom.getName(), atom.getArity());
	}

	public int getArity() {
		return arity;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return name.hashCode() * (arity + 1);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		try {
			PredicateDefinition pd = (PredicateDefinition) obj;
			if (pd.getArity() != getArity())
				return false;
			if (!pd.getName().equals(getName()))
				return false;
		} catch (ClassCastException cce) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return name + "/" + arity;
	}

}
