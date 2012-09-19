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

public class SubClassAxiom extends TerminologicalAxiom {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5059709557246520213L;
	private Description subConcept;
	private Description superConcept;
	
	public SubClassAxiom(Description subConcept, Description superConcept) {
		this.subConcept = subConcept;
		this.superConcept = superConcept;
	}

	public Description getSubConcept() {
		return subConcept;
	}

	public Description getSuperConcept() {
		return superConcept;
	}

	public int getLength() {
		return 1 + subConcept.getLength() + superConcept.getLength();
	}
	
	public String toString(String baseURI, Map<String,String> prefixes) {
		return subConcept.toString(baseURI, prefixes) + " SUBCONCEPTOF " + superConcept.toString(baseURI, prefixes);
	}
	
	public String toKBSyntaxString(String baseURI, Map<String,String> prefixes) {
		return subConcept.toKBSyntaxString(baseURI, prefixes) + " SUBCONCEPTOF " + superConcept.toKBSyntaxString(baseURI, prefixes);
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
		// TODO Auto-generated method stub
		return subConcept.toManchesterSyntaxString(baseURI, prefixes) + " subClassOf: " + superConcept.toManchesterSyntaxString(baseURI, prefixes);
	}	
}
