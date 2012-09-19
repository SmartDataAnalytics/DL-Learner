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
 * This class represents restrictions on datatypes, such as
 * Man AND EXISTS hasAge >= 18.
 * 
 * TODO: connect this with a data range and a datatype property
 * 
 * @author Jens Lehmann
 *
 */
public class DatatypeSomeRestriction extends DatatypeQuantorRestriction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9190086621306032225L;
	DataRange dataRange;
	
	/**
	 * @param datatypeProperty
	 */
	public DatatypeSomeRestriction(DatatypeProperty datatypeProperty, DataRange dataRange) {
		super(datatypeProperty);
		this.dataRange = dataRange;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.Description#getArity()
	 */
	@Override
	public int getArity() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#getLength()
	 */
	public int getLength() {
		return 1 + dataRange.getLength();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toString(java.lang.String, java.util.Map)
	 */
	public String toString(String baseURI, Map<String, String> prefixes) {
		return restrictedPropertyExpression.toString(baseURI, prefixes) + dataRange.toString(baseURI, prefixes);
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toKBSyntaxString(java.lang.String, java.util.Map)
	 */
	public String toKBSyntaxString(String baseURI, Map<String, String> prefixes) {
		return restrictedPropertyExpression.toKBSyntaxString(baseURI, prefixes) + dataRange.toKBSyntaxString(baseURI, prefixes);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.Description#accept(org.dllearner.core.owl.DescriptionVisitor)
	 */
	@Override
	public void accept(DescriptionVisitor visitor) {
		visitor.visit(this);
	}	

	public void accept(KBElementVisitor visitor) {
		visitor.visit(this);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.Description#toManchesterSyntaxString()
	 */
	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String,String> prefixes) {
		return restrictedPropertyExpression.toManchesterSyntaxString(baseURI, prefixes) + dataRange.toManchesterSyntaxString(baseURI, prefixes);
	}

	/**
	 * @return the dataRange
	 */
	public DataRange getDataRange() {
		return dataRange;
	}	
}
