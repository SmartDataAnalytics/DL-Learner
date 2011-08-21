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

import java.net.URI;
import java.util.Map;

import org.dllearner.utilities.Helper;

/**
 * Represents an object property in a knowledge base / ontology, 
 * e.g. "hasChild".
 * 
 * @author Jens Lehmann
 *
 */
public class ObjectProperty extends ObjectPropertyExpression implements Property, Comparable<ObjectProperty>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3343070247923446690L;

	public ObjectProperty(String name) {
		super(name);
	}

	public int getLength() {
		return 1;
	}
	
    public URI getURI() {
    	return URI.create(name);
    }	
	
	@Override		
	public String toString() {
    	    return name;
	}
    
    public String toString(String baseURI, Map<String,String> prefixes) {
    	return  Helper.getAbbreviatedString(name, baseURI, prefixes) ;
    }
    
    public String toKBSyntaxString(String baseURI, Map<String,String> prefixes) {
    	return "\"" + Helper.getAbbreviatedString(name, baseURI, prefixes) + "\"";
    }
	
	public void accept(KBElementVisitor visitor) {
		visitor.visit(this);
	}

	public int compareTo(ObjectProperty o) {
		return name.compareTo(o.name);
	}    
	
	@Override
	public boolean equals(Object nc) {
		// standard equals code - always return true for object identity and
		// false if classes differ
		if(nc == this) {
			return true;
		} else if(getClass() != nc.getClass()) {
			return false;
		}
		// compare on URIs
		return ((ObjectProperty)nc).name.equals(name);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toManchesterSyntaxString(java.lang.String, java.util.Map)
	 */
	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String, String> prefixes) {
		return Helper.getAbbreviatedString(name, baseURI, prefixes);
	}	
}
