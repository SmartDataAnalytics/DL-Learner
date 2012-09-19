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
 * Restricts the value of an object property to a single individual
 * (corresponds to owl:hasValue)
 * 
 * @author Jens Lehmann
 *
 */
public class ObjectValueRestriction extends ValueRestriction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2437223709767096950L;

	/**
	 * @param property
	 */
	public ObjectValueRestriction(Property property, Individual value) {
		super(property, value);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.Description#toManchesterSyntaxString(java.lang.String, java.util.Map)
	 */
	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String, String> prefixes) {
		return restrictedPropertyExpression.toString(baseURI, prefixes) + " value " + value.toString(baseURI, prefixes);
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
		return 3;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toString(java.lang.String, java.util.Map)
	 */
	public String toString(String baseURI, Map<String, String> prefixes) {
		return restrictedPropertyExpression.toString(baseURI, prefixes) + " hasValue " + value.toString(baseURI, prefixes);
	}	
	
	public String toKBSyntaxString(String baseURI, Map<String, String> prefixes) {
		return "(" + restrictedPropertyExpression.toKBSyntaxString(baseURI, prefixes) + " HASVALUE " + value.toKBSyntaxString(baseURI, prefixes) + ")";
	}	
	
	public Individual getIndividual() {
		return (Individual) value;
	}
	
	@Override
	public void accept(DescriptionVisitor visitor) {
		visitor.visit(this);
	}	
	
	public void accept(KBElementVisitor visitor) {
		visitor.visit(this);
	}	
}
