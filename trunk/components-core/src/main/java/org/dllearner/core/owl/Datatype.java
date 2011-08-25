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

import java.net.URI;
import java.util.Map;

/**
 * @author Jens Lehmann
 *
 */
public class Datatype implements DataRange, Comparable<Datatype> {
	
    private URI uri;

    public Datatype(String uriString) {
    	uri = URI.create(uriString);
    }

	public URI getURI() {
		return uri;
	}

	@Override
	public String toString() {
		return uri.toString();
	}

	public int getLength() {
		return 1;
	}

	public void accept(KBElementVisitor visitor) {
		visitor.visit(this);
	}	
	
	public String toString(String baseURI, Map<String, String> prefixes) {
		return uri.toString();
	}
	
	public String toKBSyntaxString(String baseURI, Map<String, String> prefixes) {
		return uri.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.KBElement#toManchesterSyntaxString(java.lang.String, java.util.Map)
	 */
	@Override
	public String toManchesterSyntaxString(String baseURI, Map<String, String> prefixes) {
		return uri.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
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
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	@Override
	public int compareTo(Datatype o) {
		return this.getURI().compareTo(o.getURI());
	}	
}
