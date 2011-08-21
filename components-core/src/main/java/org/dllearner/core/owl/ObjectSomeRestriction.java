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
public class ObjectSomeRestriction extends ObjectQuantorRestriction {

    /**
	 * 
	 */
	private static final long serialVersionUID = 858960420513908151L;

	public ObjectSomeRestriction(ObjectPropertyExpression role, Description c) {
    	super(role,c);
    }
    
    public String toString(String baseURI, Map<String,String> prefixes) {
        return "EXISTS " + restrictedPropertyExpression.toString(baseURI, prefixes) + "." + children.get(0).toString(baseURI, prefixes);
    }
    
    public String toKBSyntaxString(String baseURI, Map<String,String> prefixes) {
        return "EXISTS " + restrictedPropertyExpression.toKBSyntaxString(baseURI, prefixes) + "." + children.get(0).toKBSyntaxString(baseURI, prefixes);
    }

	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String, String> prefixes) {
	    return restrictedPropertyExpression.toString(baseURI, prefixes) + " some " + children.get(0).toManchesterSyntaxString(baseURI, prefixes);
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
}
