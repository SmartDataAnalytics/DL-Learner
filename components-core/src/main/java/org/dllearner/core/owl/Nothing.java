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
 * Implementation of owl:Nothing/BOTTOM.
 * 
 * @author Jens Lehmann
 *
 */
public class Nothing extends Description {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3053885252153066318L;
	public static final Nothing instance = new Nothing();	
	
    public String toString(String baseURI, Map<String,String> prefixes) {
        return "BOTTOM";
    }
    
    public String toKBSyntaxString(String baseURI, Map<String,String> prefixes) {
        return "BOTTOM";
    }

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.Description#toManchesterSyntaxString(java.lang.String, java.util.Map)
	 */
	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String, String> prefixes) {
		// in Protege 3.3 owl:Nothing
		// in Protege 4.0 only Nothing
		//return "owl:Nothing";
		return "Nothing";
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
