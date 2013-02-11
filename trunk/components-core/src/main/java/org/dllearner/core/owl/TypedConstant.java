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
 * A constant which has an explicitly assigned datatype.
 * 
 * @author Jens Lehmann
 *
 */
public class TypedConstant extends Constant {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9135242138291085300L;
	private Datatype datatype;
	
	public TypedConstant(String literal, Datatype datatype) {
		super(literal);
		this.datatype = datatype;
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
		return literal;
//		return literal + "^^" + datatype;
	}
	
	public String toKBSyntaxString(String baseURI, Map<String, String> prefixes) {
		return "\"" + literal + "\"";
//		return literal + "^^" + datatype;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toManchesterSyntaxString(java.lang.String, java.util.Map)
	 */
	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String, String> prefixes) {
		// implemented according to http://www.w3.org/TR/owl2-manchester-syntax/
		// (not completely clear because "typedLiteral" and "integerLiteral" definitions there overlap, but hopefully correct)
		if(datatype.equals(OWL2Datatype.INT.getDatatype()) || datatype.equals(OWL2Datatype.DOUBLE.getDatatype())) {
			if(Double.valueOf(literal) >= 0) {
				return "+" + literal;
			} else {
				return "-" + literal;
			}
		} else if(datatype.equals(OWL2Datatype.STRING.getDatatype())) {
			return "\"" + literal + "\"";
		} else {
			return "\"" + literal + "\"^^" + datatype.toManchesterSyntaxString(baseURI, prefixes);
		}
	}

	/**
	 * @return the datatype
	 */
	public Datatype getDatatype() {
		return datatype;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#accept(org.dllearner.core.owl.KBElementVisitor)
	 */
	public void accept(KBElementVisitor visitor) {
		visitor.visit(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(TypedConstant o) {
		// the first criteria is the datatype
		int datatypeComparision = datatype.getIRI().compareTo(datatype.getIRI());
		if(datatypeComparision == 0) {
			// the second criterion is the literal value
			return literal.compareTo(o.literal);
		} else
			return datatypeComparision;
	}	
	
	@Override
	public String toString() {
		return literal + "^^" + datatype;
	}

	@Override
	public int compareTo(Constant o) {
		if(o instanceof UntypedConstant) {
			return 1;
		}
		String str = literal + datatype;
		String str2 = o.literal + ((TypedConstant)o).datatype;
		return str.compareTo(str2);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((datatype == null) ? 0 : datatype.hashCode());
		result = prime * result + ((getLiteral() == null) ? 0 : getLiteral().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypedConstant other = (TypedConstant) obj;
		if (datatype == null) {
			if (other.datatype != null)
				return false;
		} else if (!datatype.equals(other.datatype))
			return false;
		if(!getLiteral().equals(other.getLiteral())){
			return false;
		}
		return true;
	}	

}
