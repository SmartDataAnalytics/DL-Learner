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
 * @author Jens Lehmann
 *
 */
public class DoubleDatatypePropertyAssertion extends DatatypePropertyAssertion {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5879849456632960783L;
	private double value;
	
	public DoubleDatatypePropertyAssertion(DatatypeProperty datatypeProperty, Individual individual, double value) {
		super(datatypeProperty, individual);
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.dl.KBElement#toString(java.lang.String, java.util.Map)
	 */
	public String toString(String baseURI, Map<String, String> prefixes) {
		return datatypeProperty.toString(baseURI, prefixes) + "(" + individual.toString(baseURI, prefixes) + "," + value +")";
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toKBSyntaxString(java.lang.String, java.util.Map)
	 */
	public String toKBSyntaxString(String baseURI, Map<String, String> prefixes) {
		return datatypeProperty.toKBSyntaxString(baseURI, prefixes) + "(" + individual.toKBSyntaxString(baseURI, prefixes) + "," + value +")";
	}

	public double getValue() {
		return value;
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
		return "DOUBLEDATAPROPASSERTION NOT IMPLEMENTED";
	}	
}
