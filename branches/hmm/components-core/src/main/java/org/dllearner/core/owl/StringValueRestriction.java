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
 * 
 * String value restriction, e.g. \exists description hasValue "interesting item".
 * 
 * @author Jens Lehmann
 *
 */
public class StringValueRestriction extends DatatypeValueRestriction {

	private String stringValue;
	
	public StringValueRestriction(DatatypeProperty restrictedPropertyExpression, String value) {
		// TODO: we pass it as typed property, although it could be untyped
		super(restrictedPropertyExpression, new TypedConstant(value.toString(), OWL2Datatype.STRING.getDatatype()));
		stringValue = value.toString();
	}

	private static final long serialVersionUID = 5651982376457935975L;
	
	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String,String> prefixes) {
		return restrictedPropertyExpression.toString(baseURI, prefixes) + " value " + value.toManchesterSyntaxString(baseURI, prefixes);
	}

	@Override
	public int getLength() {
		return 3;
	}

	public String getStringValue() {
		return stringValue;
	}
	
	@Override
	public String toKBSyntaxString(String baseURI, Map<String, String> prefixes) {
		return "(" + restrictedPropertyExpression.toKBSyntaxString(baseURI, prefixes) + " STRINGVALUE " + value.toKBSyntaxString(baseURI, prefixes) + ")";
	}	
}
