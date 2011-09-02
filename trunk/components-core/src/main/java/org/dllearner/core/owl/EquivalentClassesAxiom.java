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

public class EquivalentClassesAxiom extends TerminologicalAxiom {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2893732406014114441L;
	private Description concept1;
	private Description concept2;
	
	public EquivalentClassesAxiom(Description concept1, Description concept2) {
		this.concept1 = concept1;
		this.concept2 = concept2;
	}

	public Description getConcept1() {
		return concept1;
	}

	public Description getConcept2() {
		return concept2;
	}

	public int getLength() {
		return 1 + concept1.getLength() + concept2.getLength();
	}
			
	public String toString(String baseURI, Map<String,String> prefixes) {
		return concept1.toString(baseURI, prefixes) + " = " + concept2.toString(baseURI, prefixes);
	}
	
	public String toKBSyntaxString(String baseURI, Map<String,String> prefixes) {
		return concept1.toKBSyntaxString(baseURI, prefixes) + " = " + concept2.toKBSyntaxString(baseURI, prefixes);
	}

	@Override
	public void accept(AxiomVisitor visitor) {
		visitor.visit(this);
	}
	
	public void accept(KBElementVisitor visitor) {
		visitor.visit(this);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toManchesterSyntaxString(java.lang.String, java.util.Map)
	 */
	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String, String> prefixes) {
		return concept1.toManchesterSyntaxString(baseURI, prefixes) + " EquivalentTo: " + concept2.toManchesterSyntaxString(baseURI, prefixes);
	}	
}
