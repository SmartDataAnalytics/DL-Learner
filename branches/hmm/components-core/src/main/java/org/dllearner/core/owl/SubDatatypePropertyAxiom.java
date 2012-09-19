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

public class SubDatatypePropertyAxiom extends PropertyAxiom {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1085651734702155330L;
	private DatatypeProperty role;
	private DatatypeProperty subRole;
	
	public SubDatatypePropertyAxiom(DatatypeProperty subRole, DatatypeProperty role) {
		this.role = role;
		this.subRole = subRole;
	}
	
	public DatatypeProperty getRole() {
		return role;
	}

	public DatatypeProperty getSubRole() {
		return subRole;
	}

	public int getLength() {
		return 1 + role.getLength() + subRole.getLength();
	}
		
	public String toString(String baseURI, Map<String,String> prefixes) {
		return "Subrole(" + subRole.toString(baseURI, prefixes) + "," + role.toString(baseURI, prefixes) + ")";
	}
	
	public String toKBSyntaxString(String baseURI, Map<String,String> prefixes) {
		return "Subrole(" + subRole.toKBSyntaxString(baseURI, prefixes) + "," + role.toKBSyntaxString(baseURI, prefixes) + ")";
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
		return subRole.toString(baseURI, prefixes) + " SubPropertyOf: " + role.toString(baseURI, prefixes);
	}	
}
