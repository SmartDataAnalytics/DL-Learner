/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
package org.dllearner.utilities;

import java.util.Comparator;

import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyExpression;

/**
 * Compares two object properties.
 * 
 * @author Jens Lehmann
 *
 */
public class RoleComparator implements Comparator<ObjectPropertyExpression> {

	public int compare(ObjectPropertyExpression r1, ObjectPropertyExpression r2) {
		
		if(r1 instanceof ObjectProperty) {
			if(r2 instanceof ObjectProperty) {
				return r1.getName().compareTo(r2.getName());
				// zweite Rolle ist invers
			} else {
				return -1;
			}
		// 1. Rolle ist invers
		} else {
			if(r1 instanceof ObjectProperty) {
				return 1;
			} else {
				return r1.getName().compareTo(r2.getName());
			}
		}
		
	}

}
