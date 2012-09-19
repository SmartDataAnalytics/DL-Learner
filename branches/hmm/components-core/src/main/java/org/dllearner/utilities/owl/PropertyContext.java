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

import java.util.LinkedList;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.Thing;

/**
 * A property context is a utility class which specifies the
 * position of constructs with respect to properties of a 
 * construct in a class description. For instance, the A
 * in \exists r.\exists s.A occurs in property context [r,s].
 * 
 * @author Jens Lehmann
 *
 */
public class PropertyContext extends LinkedList<ObjectProperty> implements Comparable<PropertyContext> {

	private static final long serialVersionUID = -4403308689522524077L;

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(PropertyContext context) {
		// we first distinguish on size - simpler contexts come first
		int diff = context.size() - size();
		if(diff != 0) {
			return diff;
		}
			
		for(int i=0; i<size(); i++) {
			int cmp = get(i).getName().compareTo(context.get(i).getName());
			if(cmp != 0) {
				return cmp;
			}
		}
		
		return 0;
	}

	/**
	 * Transforms context [r,s] to \exists r.\exists s.\top.
	 * @return A description with existential quantifiers and \top corresponding
	 * to the context.
	 */
	public Description toExistentialContext() {
		Description d = Thing.instance;
		for(int i = size()-1; i>=0; i--) {
			d = new ObjectSomeRestriction(get(i), d);
		}
		return d;
	}
	
}
