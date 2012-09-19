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

import java.util.List;
import java.util.Map;

public class Intersection extends Description {

	/**
	 * 
	 */
	private static final long serialVersionUID = 296837418292087387L;

	public Intersection() {
		
	}
	
	public Intersection(Description... children) {
		for(Description child : children) {
			addChild(child);
		}
	}
	
	public Intersection(List<Description> children) {
		for(Description child : children) {
			addChild(child);
		}
	}
	
	@Override
	public int getArity() {
		return children.size();
	}

	public int getLength() {
		int length = 0;
		for(Description child : children) {
			length += child.getLength();
		}
		return length + children.size() - 1;
	}

	public String toString(String baseURI, Map<String,String> prefixes) {
		if(children.size()==0)
			return "EMPTY_AND";
		
		String ret = "(";
		for(int i=0; i<children.size()-1; i++) {
			ret += children.get(i).toString(baseURI, prefixes) + " AND "; 
		}
		ret += children.get(children.size()-1).toString(baseURI, prefixes) + ")";
		return ret;
	}
	
	public String toKBSyntaxString(String baseURI, Map<String,String> prefixes) {
		if(children.size()==0)
			return "EMPTY_AND";
		
		String ret = "(";
		String bracketCollect = "";
		for(int i=0; i<children.size()-1; i++) {
			ret += children.get(i).toKBSyntaxString(baseURI, prefixes) + " AND "; 
			if( i != (children.size()-2) ) { 
				ret += "(";
				bracketCollect += ")";
			}
		}
		
		ret += children.get(children.size()-1).toKBSyntaxString(baseURI, prefixes) + ")";
		ret += bracketCollect;
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.Description#toManchesterSyntaxString()
	 */
	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String,String> prefixes) {
		if(children.size()==0)
			return "EMPTY_AND";
		
		String ret = "(";
		for(int i=0; i<children.size()-1; i++) {
			ret += children.get(i).toManchesterSyntaxString(baseURI, prefixes) + " and "; 
		}
		ret += children.get(children.size()-1).toManchesterSyntaxString(baseURI, prefixes) + ")";
		return ret;
	}		
	
	@Deprecated
	public String toStringOld() {
		String ret = "MULTI_AND [";
		for(Description child : children) {
			ret += child.toString() + ",";
		}
		ret += "]";
		return ret;
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
