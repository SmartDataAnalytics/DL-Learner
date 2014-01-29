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

import org.dllearner.utilities.Helper;
import org.semanticweb.owlapi.model.IRI;

import com.hp.hpl.jena.vocabulary.RDFS;


/**
 * @author Jens Lehmann
 *
 */
public class Datatype implements DataRange, Comparable<Datatype> {
	
    private IRI iri;
    private boolean isTopDatatype = false;

    public Datatype(String iriString) {
    	iri = IRI.create(iriString);
    	isTopDatatype = iriString.equals(RDFS.Literal.getURI());
    }

	public IRI getIRI() {
		return iri;
	}
	
	/**
	 * @return the isTopDatatype
	 */
	public boolean isTopDatatype() {
		return isTopDatatype;
	}

	@Override
	public String toString() {
		return iri.toString();
	}

	public int getLength() {
		return 1;
	}

	public void accept(KBElementVisitor visitor) {
		visitor.visit(this);
	}	
	
	public String toString(String baseURI, Map<String, String> prefixes) {
		return iri.toString();
	}
	
	public String toKBSyntaxString(String baseURI, Map<String, String> prefixes) {
		return iri.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toManchesterSyntaxString(java.lang.String, java.util.Map)
	 */
	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String, String> prefixes) {
		return Helper.getAbbreviatedString(iri.toString(), baseURI, prefixes);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((iri == null) ? 0 : iri.hashCode());
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
		Datatype other = (Datatype) obj;
		if (iri == null) {
			if (other.iri != null)
				return false;
		} else if (!iri.equals(other.iri))
			return false;
		return true;
	}

	@Override
	public int compareTo(Datatype o) {
		return this.getIRI().compareTo(o.getIRI());
	}	
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.DataRange#isDatatype()
	 */
	@Override
	public boolean isDatatype() {
		return true;
	}
}
