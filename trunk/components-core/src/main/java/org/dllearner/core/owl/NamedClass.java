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

import java.io.Serializable;
import java.net.URI;
import java.util.Map;

import org.dllearner.utilities.Helper;

/**
 * Represents an atomic concept in a knowledge base / ontology, 
 * e.g. "car", "person".
 * 
 * @author Jens Lehmann
 *
 */
public class NamedClass extends Description implements Entity, NamedKBElement, Comparable<NamedClass>, Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -2316344469212256752L;
	private String name;
    
	public NamedClass(String name) {
        this.name = name;
    }    
    
	public NamedClass(URI uri) {
        this.name = uri.toString();
    }
	
    public String getName() {
		return name;
	}
    
    public URI getURI() {
    	return URI.create(name);
    }
    
	public int getLength() {
		return 1;
	}

	@Override
	public int getArity() {
		return 0;
	}

    public String toString(String baseURI, Map<String,String> prefixes) {
    	return Helper.getAbbreviatedString(name, baseURI, prefixes);
    }
    
    public String toKBSyntaxString(String baseURI, Map<String,String> prefixes) {
    	return "\"" + Helper.getAbbreviatedString(name, baseURI, prefixes) + "\"";
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
	 * @see org.dllearner.core.owl.Description#toManchesterSyntaxString(java.lang.String, java.util.Map)
	 */
	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String, String> prefixes) {
		return Helper.getAbbreviatedString(name, baseURI, prefixes);
	}	
	
	public int compareTo(NamedClass o) {
		return name.compareTo(o.name);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		NamedClass other = (NamedClass) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}	
	
	
}
