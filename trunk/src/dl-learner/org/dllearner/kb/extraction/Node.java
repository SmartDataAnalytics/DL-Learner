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
package org.dllearner.kb.extraction;

import java.net.URI;
import java.util.Set;
import java.util.Vector;


/**
 * Abstract class. defines functions to expand the nodes
 * 
 * @author Sebastian Hellmann
 * 
 */
public abstract class Node implements Comparable<Node> {

	final String subclass = "http://www.w3.org/2000/01/rdf-schema#subClassOf";
	final String rdftype = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	final String objectProperty = "http://www.w3.org/2002/07/owl#ObjectProperty";
	final String classns = "http://www.w3.org/2002/07/owl#Class";
	final String thing = "http://www.w3.org/2002/07/owl#Thing";

	URI uri;
	// protected String type;
	protected boolean expanded = false;

	public Node(URI u) {
		this.uri = u;
	}

	/**
	 * Nodes are expanded with a certain context, given by the typedSparqlQuery
	 * and the manipulator
	 * 
	 * @param typedSparqlQuery
	 * @param manipulator
	 * @return Vector<Node> all Nodes that are new because of expansion
	 */
	public abstract Vector<Node> expand(
			TypedSparqlQueryInterface typedSparqlQuery, Manipulator manipulator);

	/**
	 * gets type defs for properties like rdf:type SymmetricProperties
	 * 
	 * @param typedSparqlQuery
	 * @param manipulator
	 * @return Vector<Node>
	 */
	public abstract void expandProperties(
			TypedSparqlQueryInterface typedSparqlQuery, Manipulator manipulator);

	/**
	 * output
	 * 
	 * @return a set of n-triple
	 */
	public abstract Set<String> toNTriple();

	@Override
	public String toString() {
		return "Node: " + uri + ":" + this.getClass().getSimpleName();

	}

	public URI getURI() {
		return uri;
	}

	public boolean equals(Node n) {
		if (this.uri.equals(n.uri))
			return true;
		else
			return false;
	}

	public int compareTo(Node n) {
		return this.uri.toString().compareTo(n.uri.toString());
	}

}
