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

import java.util.Map;

/**
 * @author Jens Lehmann
 *
 */
public class DatatypeExactCardinalityRestriction extends DatatypeCardinalityRestriction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6125552354406848242L;

	/**
	 * @param datatypeProperty
	 * @param dataRange
	 * @param cardinality
	 */
	public DatatypeExactCardinalityRestriction(DatatypeProperty datatypeProperty,
			DataRange dataRange, int cardinality) {
		super(datatypeProperty, dataRange, cardinality);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.Concept#getArity()
	 */
	@Override
	public int getArity() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#getLength()
	 */
	public int getLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toString(java.lang.String, java.util.Map)
	 */
	public String toString(String baseURI, Map<String, String> prefixes) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String toKBSyntaxString(String baseURI, Map<String, String> prefixes) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.Description#toManchesterSyntaxString()
	 */
	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String,String> prefixes) {
		// TODO Auto-generated method stub
		return null;
	}		
	
	@Override
	public void accept(DescriptionVisitor visitor) {
		visitor.visit(this);
	}	

	public void accept(KBElementVisitor visitor) {
		visitor.visit(this);
	}


}
