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
package org.dllearner.kb.extraction;

import java.util.List;
import java.util.SortedSet;

import org.dllearner.kb.aquisitors.TupelAquisitor;
import org.dllearner.kb.manipulator.Manipulator;



/**
 * Abstract class. defines functions to expand the nodes
 * 
 * @author Sebastian Hellmann
 * 
 */
public abstract class Node implements Comparable<Node> {

	

	protected String uri;
	// protected String type;
	protected boolean expanded = false;

	public Node(String uri) {
		this.uri = uri;
	}

	/**
	 * Nodes are expanded with a certain context, given by the typedSparqlQuery
	 * and the manipulator
	 * 
	 * @param typedSparqlQuery
	 * @param manipulator
	 * @return Vector<Node> all Nodes that are new because of expansion
	 */
	public abstract List<Node> expand(
			TupelAquisitor TupelAquisitor, Manipulator manipulator);

	/**
	 * gets type defs for properties like rdf:type SymmetricProperties
	 * 
	 * @param typedSparqlQuery
	 * @param manipulator
	 * @return Vector<Node>
	 */
	public abstract void expandProperties(
			TupelAquisitor TupelAquisitor, Manipulator manipulator);

	/**
	 * output
	 * 
	 * @return a set of n-triple
	 */
	public abstract SortedSet<String> toNTriple();

	@Override
	public String toString() {
		return "Node: " + uri + ":" + this.getClass().getSimpleName();

	}

	public String getURI() {
		return uri;
	}
	
	public abstract List<Node> getAllNodesAsList(List<Node> l);

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
