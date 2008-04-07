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
 * Allows to specify the value of a boolean datatype restriction,
 * e.g. ChemicalSubstance AND EXISTS acidTest = true
 * 
 * @author Jens Lehmann
 *
 */
public class BooleanDataRange implements DataRange {

	private boolean isTrue;
	
	public BooleanDataRange(boolean isTrue) {
		this.isTrue = isTrue;
	}
	
	/**
	 * @return The boolean value of this restriction.
	 */
	public boolean isTrue() {
		return isTrue;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#getLength()
	 */
	public int getLength() {
		// length is 1, because we have either true or false
		return 1;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toString(java.lang.String, java.util.Map)
	 */
	public String toString(String baseURI, Map<String, String> prefixes) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public String toKBSyntaxString (String baseURI, Map<String, String> prefixes) {
		// TODO Auto-generated method stub
		return null;
	}
	

	public void accept(KBElementVisitor visitor) {
		visitor.visit(this);
	}	
}
