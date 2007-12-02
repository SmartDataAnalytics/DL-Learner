package org.dllearner.kb.sparql;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class InstanceNode extends Node {

	Set<ClassNode> classes = new HashSet<ClassNode>();
	Set<Tupel> datatypes = new HashSet<Tupel>();
	Set<PropertyNode> properties = new HashSet<PropertyNode>();

	public InstanceNode(URI u) {
		super(u);
		this.type = "instance";

	}

	@Override
	public Vector<Node> expand(TypedSparqlQuery tsq, Manipulator m) {

		Set<Tupel> s = tsq.query(this.URI);
		// Manipulation
		m.check(s, this);
		Vector<Node> Nodes = new Vector<Node>();

		Iterator<Tupel> it = s.iterator();
		while (it.hasNext()) {
			Tupel t = (Tupel) it.next();

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
		this.expanded = true;
		return Nodes;
	}

	@Override
	public boolean isInstance() {
		return true;
	}

	@Override
	public Set<String> toNTriple() {
		Set<String> s = new HashSet<String>();
		s.add("<" + this.URI + "><" + "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" + "><"
				+ "http://www.w3.org/2002/07/owl#Thing" + ">.");
		for (ClassNode one : classes) {
			s.add("<" + this.URI + "><" + "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" + "><"
					+ one.getURI() + ">.");
			s.addAll(one.toNTriple());
		}
		for (PropertyNode one : properties) {
			s.add("<" + this.URI + "><" + one.getURI() + "><" + one.getB().getURI() + ">.");
			s.addAll(one.toNTriple());
			s.addAll(one.getB().toNTriple());
		}

		return s;
	}

}
