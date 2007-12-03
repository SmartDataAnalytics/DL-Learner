/**
 * Copyright (C) 2007, Sebastian Hellmann
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
package org.dllearner.kb.sparql;

import java.net.URI;
import java.util.Set;
import java.util.Vector;

// abstract class 
public abstract class Node {
	URI uri;
	protected String type;
	protected boolean expanded = false;

	public Node(URI u) {
		this.uri = u;
	}

	public abstract Vector<Node> expand(TypedSparqlQuery tsq, Manipulator m);

	public abstract Vector<Node> expandProperties(TypedSparqlQuery tsq, Manipulator m);

	public abstract Set<String> toNTriple();

	@Override
	public String toString() {
		return "Node: " + uri + ":" + type;

	}

	public URI getURI() {
		return uri;
	}

}
