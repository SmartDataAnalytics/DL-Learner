/**
 * Copyright (C) 2007-2008, Jens Lehmann
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

import java.net.URI;
import java.util.Map;

/**
 * @author Jens Lehmann
 *
 */
public enum Datatype implements DataRange {
	
    DOUBLE ("http://www.w3.org/2001/XMLSchema#double"),
    INT ("http://www.w3.org/2001/XMLSchema#int"),
    BOOLEAN   ("http://www.w3.org/2001/XMLSchema#boolean");

    private URI uri;

    private Datatype(String uriString) {
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

	public String toString(String baseURI, Map<String, String> prefixes) {
		return uri.toString();
	}
	
	public String toKBSyntaxString(String baseURI, Map<String, String> prefixes) {
		return uri.toString();
	}
	
	public void accept(KBElementVisitor visitor) {
		visitor.visit(this);
	}	
}
