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
public class ObjectPropertyRangeAxiom extends PropertyRangeAxiom {


	/**
	 * 
	 */
	private static final long serialVersionUID = 4076807871628918900L;

	public ObjectPropertyRangeAxiom(ObjectProperty property, Description range) {
		super(property, range);
	}	
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#getLength()
	 */
	public int getLength() {
		return range.getLength() + 2;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toString(java.lang.String, java.util.Map)
	 */
	public String toString(String baseURI, Map<String, String> prefixes) {
		return "Range(" + getProperty() + ", " + getRange() +  ")";
	}
	
	public String toKBSyntaxString(String baseURI, Map<String, String> prefixes) {
		return "OPRANGE(" + property.toKBSyntaxString(baseURI, prefixes) + ") = " + range.toKBSyntaxString(baseURI, prefixes); 	
	}

	@Override
	public Description getRange() {
		return (Description) range;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.Axiom#accept(org.dllearner.core.owl.AxiomVisitor)
	 */
	@Override
	public void accept(AxiomVisitor visitor) {
		visitor.visit(this);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#accept(org.dllearner.core.owl.KBElementVisitor)
	 */
	public void accept(KBElementVisitor visitor) {
		visitor.visit(this);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toManchesterSyntaxString(java.lang.String, java.util.Map)
	 */
	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String, String> prefixes) {
		return "Range(" + getProperty().toManchesterSyntaxString(baseURI, prefixes) + ", " + getRange().toManchesterSyntaxString(baseURI, prefixes) +  ")";
	}

	
}
