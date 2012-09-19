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
 * Convenience class for boolean value restrictions.
 * 
 * @author Jens Lehmann
 *
 */
public class BooleanValueRestriction extends DatatypeValueRestriction implements Comparable<BooleanValueRestriction>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1653555689466334140L;
	private boolean booleanValue;
	
	/**
	 * TODO: Internally a typed constant with datatype boolean and 
	 * strings "true" or "false" is created. This is a clean way to
	 * implement boolean value restrictions. However, if they are
	 * created millions of times during the run of an algorithm, 
	 * this may cause unnecessary delays. 
	 * Possible Solution: It may be good to create a BooleanConstant 
	 * class, which just holds the boolean value and only performs 
	 * operations when requested.
	 * 
	 * @param restrictedPropertyExpression
	 * @param value
	 */
	public BooleanValueRestriction(DatatypeProperty restrictedPropertyExpression, Boolean value) {
		super(restrictedPropertyExpression, new TypedConstant(value.toString(), OWL2Datatype.BOOLEAN.getDatatype()));
		booleanValue = value;
	}

	public boolean getBooleanValue() {
		return booleanValue;
	}

	/**
	 * Boolean value restrictions have length 2, because they encode two
	 * pieces of information: the property and the boolean value.
	 */
	public int getLength() {
		return 2;
	}
	
	@Override
	public String toKBSyntaxString(String baseURI, Map<String,String> prefixes) {
		String ret= "("+restrictedPropertyExpression.toKBSyntaxString(baseURI, prefixes) + " IS " + ((String)value.toKBSyntaxString(baseURI, prefixes)).toUpperCase()+")";
		return ret;
		
	}

	@Override
	public int compareTo(BooleanValueRestriction o) {
		int ret = getRestrictedPropertyExpression().compareTo(o.getRestrictedPropertyExpression());
		if(ret == 0){//if properties are the same we compare the boolean values
			ret = Boolean.valueOf(getBooleanValue()).compareTo(Boolean.valueOf(o.getBooleanValue())); 
		}
		return ret;
	}
}
