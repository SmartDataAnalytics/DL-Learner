/**
 * Copyright (C) 2007, Jens Lehmann
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
 *
 */
package org.dllearner.core.owl;

import java.io.Serializable;
import java.net.URI;
import java.util.Map;

import org.dllearner.utilities.Helper;

/**
 * Represents an invididual in a knowledge base / ontology.
 * 
 * @author Jens Lehmann
 *
 */
public class Individual implements Entity, NamedKBElement, Comparable<Individual>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1831526393296388784L;
	private String name;

	public String getName() {
		return name;
	}

    public URI getURI() {
    	return URI.create(name);
    }	
	
	public Individual(String name) {
		this.name = name;
	}
	
	public int getLength() {
		return 1;
	}

	public int compareTo(Individual o) {
//		System.out.println(o);
		return name.compareTo(o.name);
	}
    
	@Override
	public boolean equals(Object o) {
		if(o==null) {
			return false;
		}
		return (compareTo((Individual)o)==0);
	}
	
    @Override
    public String toString() {
    	    return name;
    }	
	
    public String toString(String baseURI, Map<String,String> prefixes) {
    	return  Helper.getAbbreviatedString(name, baseURI, prefixes);
    }
    
    public String toKBSyntaxString(String baseURI, Map<String,String> prefixes) {
    	return "\"" + Helper.getAbbreviatedString(name, baseURI, prefixes) + "\"";
    }
    
    public String toManchesterSyntaxString(String baseURI, Map<String, String> prefixes) {
		return Helper.getAbbreviatedString(name, baseURI, prefixes);
	}	
    
	public void accept(KBElementVisitor visitor) {
		visitor.visit(this);
	}    

}
