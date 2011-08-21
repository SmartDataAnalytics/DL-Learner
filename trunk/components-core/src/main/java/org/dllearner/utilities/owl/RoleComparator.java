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

import java.util.Comparator;

import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyInverse;
import org.dllearner.core.owl.PropertyExpression;

/**
 * Compares two property expressions. The order is:
 * datatype properties first, then inverse object properties, then
 * object properties. For equal types, the URI or toString (inverses)
 * is used to fix an order.
 * 
 * @author Jens Lehmann
 *
 */
public class RoleComparator implements Comparator<PropertyExpression> {

	public int compare(PropertyExpression r1, PropertyExpression r2) {
		
		if(r1 instanceof ObjectProperty) {
			if(r2 instanceof ObjectProperty) {
				return ((ObjectProperty)r1).getName().compareTo(((ObjectProperty)r2).getName());
				// second role is inverse or datatype property
			} else {
				return -1;
			}
		// first property is an inverse object property
		} else if(r1 instanceof ObjectPropertyInverse){
			if(r2 instanceof ObjectProperty) {
				return 1;
			} else if(r2 instanceof ObjectPropertyInverse){
				return r1.toString().compareTo(r2.toString());
			} else {
				return -1;
			}
		// r1 is datatype property
		} else {
			if(r2 instanceof DatatypeProperty) {
				return ((DatatypeProperty)r1).getName().compareTo(((DatatypeProperty)r2).getName());
			} else {
				return 1;
			}
		}
		
	}

}
