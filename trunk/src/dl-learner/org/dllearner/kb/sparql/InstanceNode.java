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

// a node in the graph that is an instance
public class InstanceNode extends Node {

	Set<ClassNode> classes = new HashSet<ClassNode>();
	Set<StringTuple> datatypes = new HashSet<StringTuple>();
	Set<PropertyNode> properties = new HashSet<PropertyNode>();

	public InstanceNode(URI u) {
		super(u);
		this.type = "instance";

	}

	@Override
	public Vector<Node> expand(TypedSparqlQuery tsq, Manipulator m) {

		Set<StringTuple> s = tsq.query(uri);
		// Manipulation
		m.check(s, this);
		// System.out.println("fffffff"+m);
		Vector<Node> Nodes = new Vector<Node>();

		Iterator<StringTuple> it = s.iterator();
		while (it.hasNext()) {
			StringTuple t = (StringTuple) it.next();

			try {
				if (t.a.equals(m.type)) {
					ClassNode tmp = new ClassNode(new URI(t.b));
					classes.add(tmp);
					Nodes.add(tmp);
				} else {
					InstanceNode tmp = new InstanceNode(new URI(t.b));
					properties.add(new PropertyNode(new URI(t.a), this, tmp));
					Nodes.add(tmp);

				}
			} catch (Exception e) {
				System.out.println("Problem with: " + t);
				e.printStackTrace();
			}

		}
		expanded = true;
		return Nodes;
	}

	@Override
	public Vector<Node> expandProperties(TypedSparqlQuery tsq, Manipulator m) {
		for (PropertyNode one : properties) {
			one.expandProperties(tsq, m);
		}
		return new Vector<Node>();
	}

	@Override
	public Set<String> toNTriple() {
		Set<String> s = new HashSet<String>();
		s.add("<" + uri + "><" + "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" + "><"
				+ "http://www.w3.org/2002/07/owl#Thing" + ">.");
		for (ClassNode one : classes) {
			s.add("<" + uri + "><" + "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" + "><"
					+ one.getURI() + ">.");
			s.addAll(one.toNTriple());
		}
		for (PropertyNode one : properties) {
			s.add("<" + uri + "><" + one.getURI() + "><" + one.getB().getURI() + ">.");
			s.addAll(one.toNTriple());
			s.addAll(one.getB().toNTriple());
		}

		return s;
	}

}
