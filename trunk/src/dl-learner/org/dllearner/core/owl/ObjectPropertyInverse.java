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

import java.util.Map;

import org.dllearner.utilities.Helper;

/**
 * Represents the inverse of a property expression. It can be used
 * in axioms e.g. complex class descriptions. For instance:
 * 
 * father = male AND isChildOf^-1
 * 
 * This way, you can refer to an inverse of an object property without
 * actually giving it a name (you could name it isParentOf in this case).
 * 
 * @author Jens Lehmann
 *
 */
public class ObjectPropertyInverse extends ObjectPropertyExpression {
	
	public ObjectPropertyInverse(String name) {
		super(name);
	}

	public int getLength() {
		return 2;
	}
		
	public String toString(String baseURI, Map<String,String> prefixes) {
		return Helper.getAbbreviatedString(name, baseURI, prefixes) + "-";
	}
	
	public String toKBSyntaxString(String baseURI, Map<String,String> prefixes) {
		return Helper.getAbbreviatedString(name, baseURI, prefixes) + "-";
	}
	
	public void accept(KBElementVisitor visitor) {
		visitor.visit(this);
	}	
}
