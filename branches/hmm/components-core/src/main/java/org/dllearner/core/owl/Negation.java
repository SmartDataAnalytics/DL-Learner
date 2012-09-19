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


public class Negation extends Description {

	/*
    @Override
    protected void calculateSets(FlatABox abox, SortedSet<String> adcPosSet, SortedSet<String> adcNegSet) {
        children.get(0).calculateSets(abox, posSet, negSet);
        posSet = children.get(0).negSet;
        negSet = children.get(0).posSet;
    }
    */

	/**
	 * 
	 */
	private static final long serialVersionUID = -3007095278542800128L;

	public Negation(Description c) {
		addChild(c);
	}
	
    public String toString(String baseURI, Map<String,String> prefixes) {
        return "(NOT " +children.get(0).toString(baseURI, prefixes) + ")";
    }
    
    public String toKBSyntaxString(String baseURI, Map<String,String> prefixes) {
        //TODO brackets removed, but they maybe have to be here
    	return "NOT " +children.get(0).toKBSyntaxString(baseURI, prefixes) + "";
    }

	public int getLength() {
		return 1 + children.get(0).getLength();
	}

	@Override
	public int getArity() {
		return 1;
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
        return "(not " +children.get(0).toManchesterSyntaxString(baseURI, prefixes) + ")";		
	}	
}
