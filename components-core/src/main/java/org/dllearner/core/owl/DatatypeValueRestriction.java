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
 * Examples for datatype value restrictions:
 * Male AND hasAge HASVALUE 18
 * Male AND hasDriverLicense HASVALUE true
 * 
 * @author Jens Lehmann
 *
 */
public abstract class DatatypeValueRestriction extends ValueRestriction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3263813180892609631L;

	public DatatypeValueRestriction(DatatypeProperty restrictedPropertyExpression, Constant value) {
		super(restrictedPropertyExpression, value);
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.Description#toManchesterSyntaxString()
	 */
	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String,String> prefixes) {
		return restrictedPropertyExpression.toString(baseURI, prefixes) + " value " + value.toManchesterSyntaxString(baseURI, prefixes);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.Description#getArity()
	 */
	@Override
	public int getArity() {
		return 2;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#getLength()
	 */
	// we do not add the + 1 here because e.g. for boolean values we
	// probably do not want to add it while for double value we may
	// add it (because "<=" ">=" are possible while boolean has only "=") 
//	public int getLength() {
//		return 1 + restrictedPropertyExpression.getLength() + value.getLength();
//	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toString(java.lang.String, java.util.Map)
	 */
	public String toString(String baseURI, Map<String, String> prefixes) {
		return restrictedPropertyExpression.toString(baseURI, prefixes) + " value " + value.toString(baseURI, prefixes);
	}
	
	public String toKBSyntaxString(String baseURI, Map<String, String> prefixes) {
		return restrictedPropertyExpression.toKBSyntaxString(baseURI, prefixes) + " = " + value.toKBSyntaxString(baseURI, prefixes);
	}

	@Override
	public DatatypeProperty getRestrictedPropertyExpression() {
		return (DatatypeProperty) restrictedPropertyExpression;
	}
	
	@Override
	public Constant getValue() {
		return (Constant) value;
	}	
	
	@Override
	public void accept(DescriptionVisitor visitor) {
		visitor.visit(this);
	}	
	
	public void accept(KBElementVisitor visitor) {
		visitor.visit(this);
	}	
}
