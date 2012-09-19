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

public abstract class ObjectCardinalityRestriction extends CardinalityRestriction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4891273304140021612L;
	protected ObjectPropertyExpression role;
	protected int number;
	
	public ObjectCardinalityRestriction(int number, ObjectPropertyExpression role, Description c) {
		super(role, c, number);
		addChild(c);
		this.role = role;
		this.number = number;
	}
	
	public int getLength() {
		return 2 + role.getLength() + getChild(0).getLength();
	}

	public int getNumber() {
		return number;
	}

	public ObjectPropertyExpression getRole() {
		return role;
	}

}
