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

public enum OWL2Datatype {

    DOUBLE ( "http://www.w3.org/2001/XMLSchema#double"),
    INT ("http://www.w3.org/2001/XMLSchema#int"),
    INTEGER ("http://www.w3.org/2001/XMLSchema#integer"),
    BOOLEAN   ("http://www.w3.org/2001/XMLSchema#boolean"),
    STRING ("http://www.w3.org/2001/XMLSchema#string"),
    DATE ("http://www.w3.org/2001/XMLSchema#date"),
    DATETIME ("http://www.w3.org/2001/XMLSchema#dateTime");	
	
    private Datatype datatype;
    
	private OWL2Datatype(String str) {
		datatype = new Datatype(str);
	}    
    
	public Datatype getDatatype() {
		return datatype;
	}

	public URI getURI() {
		return datatype.getURI();
	}

}
