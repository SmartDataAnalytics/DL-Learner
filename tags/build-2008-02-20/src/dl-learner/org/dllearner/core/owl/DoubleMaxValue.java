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
package org.dllearner.core.owl;

import java.util.Map;

/**
 * Double data range restricted by a maximum value, e.g. 
 * hasAge <= 65.
 * 
 * @author Jens Lehmann
 *
 */
public class DoubleMaxValue extends DoubleDataRange {

	private double value;
	
	public DoubleMaxValue(double value) {
		this.value = value;
	}

	/**
	 * @return The maximum value.
	 */
	public double getValue() {
		return value;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#getLength()
	 */
	public int getLength() {
		return 2;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toString(java.lang.String, java.util.Map)
	 */
	public String toString(String baseURI, Map<String, String> prefixes) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void accept(KBElementVisitor visitor) {
		visitor.visit(this);
	}	
	
}