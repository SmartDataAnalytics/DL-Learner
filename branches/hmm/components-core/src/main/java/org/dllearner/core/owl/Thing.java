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

/**
 * Implementation of owl:thing/TOP.
 * 
 * TODO: According to the OWL 1.1 spec, owl:thing is special instance of
 * class, so it might be better to put a method there for retrieving 
 * a/the instance of owl:thing. However, some algorithms require parent
 * links e.g. in EXISTS r.TOP we may need to know where TOP belongs
 * (especially for genetic operators). This is instance dependant, i.e. 
 * two different instances of TOP can have different parent links.
 * 
 * @author Jens Lehmann
 *
 */
public class Thing extends Description {

	/**
	 * 
	 */
	private static final long serialVersionUID = -880276915058868775L;
	public static final Thing instance = new Thing();
	
	public static final URI uri = URI.create("http://www.w3.org/2002/07/owl#Thing");
	
    public String toString(String baseURI, Map<String,String> prefixes) {
        return "TOP";
    }
    
    public String toKBSyntaxString(String baseURI, Map<String,String> prefixes) {
        return "TOP";
    }

	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String, String> prefixes) {
		// in Protege 3.3 owl:Thing
		// in Protege 4.0 only Thing
		//return "owl:Thing";
		return "Thing";
		
	}	  
	
	public URI getURI(){
		return uri;
	}
    
	public int getLength() {
		return 1;
	}

	@Override
	public int getArity() {
		return 0;
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
