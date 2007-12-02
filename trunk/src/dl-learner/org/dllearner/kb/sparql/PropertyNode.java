package org.dllearner.kb.sparql;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class PropertyNode extends Node {

	private Node a;
	private Node b;
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
	public Vector<Node> expand(TypedSparqlQuery tsq, Manipulator m) {
		Set<Tupel> s = tsq.query(uri);
		Vector<Node> Nodes = new Vector<Node>();
		// Manipulation

		Iterator<Tupel> it = s.iterator();
		while (it.hasNext()) {
			Tupel t = (Tupel) it.next();
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

	@Override
	public boolean isProperty() {
		return true;
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

}
