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

import java.io.Serializable;

/**
 * An object property expression is an object property construct, which
 * can be used in axioms, e.g. complex class descriptions. It can be
 * either an object property or an inverse of an object property.
 * 
 * @author Jens Lehmann
 *
 */
public abstract class ObjectPropertyExpression implements PropertyExpression, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5590591991241290070L;
	protected String name;
	
	public ObjectPropertyExpression(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
}
