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
 * An untyped constant is a string which has not been assigned
 * a datatype and can have an optional language tag.
 * 
 * @author Jens Lehmann
 *
 */
public class UntypedConstant extends Constant {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2569002545544130198L;
	private String lang;
	private boolean hasLang = false;
	
	public UntypedConstant(String literal) {
		super(literal);
	}
	
	public UntypedConstant(String literal, String lang) {
		super(literal);
		this.lang = lang;
		hasLang = true;
	}	
	


	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#getLength()
	 */
	public int getLength() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toString(java.lang.String, java.util.Map)
	 */
	public String toString(String baseURI, Map<String, String> prefixes) {
		if(hasLang)
			return literal + "@" + lang;
		else
			return literal;
	}
	
	public String toKBSyntaxString(String baseURI, Map<String, String> prefixes) {
		if(hasLang)
			return literal + "@" + lang;
		else
			return literal;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toManchesterSyntaxString(java.lang.String, java.util.Map)
	 */
	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String, String> prefixes) {
		if(hasLang)
			return "\"" + literal + "\"@" + lang;
		else
			return "\"" + literal + "\"";		
	}
	
	/**
	 * @return the lang
	 */
	public String getLang() {
		return lang;
	}

	/**
	 * @return the hasLang
	 */
	public boolean hasLang() {
		return hasLang;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#accept(org.dllearner.core.owl.KBElementVisitor)
	 */
	public void accept(KBElementVisitor visitor) {
		visitor.visit(this);
	}	
	
	@Override
	public String toString() {
		if(hasLang) {
			return literal + "@" + lang;
		} else {
			return literal;
		}
	}
	
	@Override
	public int compareTo(Constant o) {
		if(o instanceof TypedConstant) {
			return -1;
		}
		String str = literal + lang;
		String str2 = ((UntypedConstant)o).literal + ((UntypedConstant)o).lang;
		return str.compareTo(str2);
	}

}
