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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.dllearner.utilities.StringTuple;

/**
 * Property node.
 * 
 * @author Sebastian Hellmann
 *
 */
public class PropertyNode extends Node {

	// the a and b part of a property
	private Node a;
	private Node b;
	// specialtypes like owl:symmetricproperty
	private Set<String> specialTypes;

	public PropertyNode(URI u) {
		super(u);
		this.type = "property";

	}

	public PropertyNode(URI u, Node a, Node b) {
		super(u);
		this.type = "property";
		this.a = a;
		this.b = b;
		this.specialTypes = new HashSet<String>();
	}

	@Override
	public Vector<Node> expand(TypedSparqlQueryInterface tsq, Manipulator m) {
		Set<StringTuple> s = tsq.query(uri);
		Vector<Node> Nodes = new Vector<Node>();
		// Manipulation

		Iterator<StringTuple> it = s.iterator();
		while (it.hasNext()) {
			StringTuple t = (StringTuple) it.next();
			try {
				if (t.a.equals(m.type)) {
					specialTypes.add(t.b);
				}
			} catch (Exception e) {
				System.out.println(t);
				e.printStackTrace();
			}

		}
		return Nodes;
	}
	
	// gets the types for properties recursively
	@Override
	public Vector<Node> expandProperties(TypedSparqlQueryInterface tsq, Manipulator m) {
		b.expandProperties(tsq, m);
		return this.expand(tsq, m);
	}
	
	public Node getA() {
		return a;
	}
	
	public Node getB() {
		return b;
	}

	@Override
	public Set<String> toNTriple() {
		Set<String> s = new HashSet<String>();
		s.add("<" + uri + "><" + "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" + "><"
				+ "http://www.w3.org/2002/07/owl#ObjectProperty" + ">.");
		for (String one : specialTypes) {
			s.add("<" + uri + "><" + "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" + "><"
					+ one + ">.");

		}

		return s;
	}
	
	@Override
	public boolean equals(Node n){
		if(this.uri.equals(n.uri))return true;
		else return false;
	}

	@Override
	public int compareTo(Node n){
		return super.compareTo(n);
	}

}
