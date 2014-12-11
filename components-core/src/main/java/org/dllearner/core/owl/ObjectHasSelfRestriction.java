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
 * Existantial restriction on objects, e.g. \exists hasChild.Male.
 * 
 * @author Jens Lehmann
 *
 */
public class ObjectHasSelfRestriction extends Restriction {

	private static final long serialVersionUID = 858960420513908151L;

	public ObjectHasSelfRestriction(ObjectPropertyExpression role) {
    	super(role);
    }
	
	public ObjectPropertyExpression getRole() {
		return (ObjectPropertyExpression) restrictedPropertyExpression;
	}
    
    public String toString(String baseURI, Map<String,String> prefixes) {
        return "EXISTS " + restrictedPropertyExpression.toString(baseURI, prefixes) + ".SELF";
    }
    
    public String toKBSyntaxString(String baseURI, Map<String,String> prefixes) {
        return "EXISTS " + restrictedPropertyExpression.toKBSyntaxString(baseURI, prefixes) + ".SELF";
    }

	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String, String> prefixes) {
	    return restrictedPropertyExpression.toString(baseURI, prefixes) + ".SELF";
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getChild(0) == null) ? 0 : getChild(0).hashCode());
		result = prime * result + ((getRole() == null) ? 0 : getRole().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ObjectHasSelfRestriction other = (ObjectHasSelfRestriction) obj;
		if (getChild(0) == null) {
			if (other.getChild(0) != null)
				return false;
		} else if (!getChild(0).equals(other.getChild(0)))
			return false;
		if (getRole() == null) {
			if (other.getRole() != null)
				return false;
		} else if (!getRole().equals(other.getRole()))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#getLength()
	 */
	@Override
	public int getLength() {
		return 2;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.Description#getArity()
	 */
	@Override
	public int getArity() {
		return 0;
	}	
	
}
