package org.dllearner.kb.sparql;

import java.net.URI;
import java.util.Set;
import java.util.Vector;

public abstract class Node {
	URI uri;
	protected String type;
	protected boolean expanded = false;

	// Hashtable<String,Node> classes=new Hashtable<String,Node>();
	// Hashtable<String,Node> instances=new Hashtable<String,Node>();;
	// Hashtable<String,Node> datatype=new Hashtable<String,Node>();;

	public Node(URI u) {
		this.uri = u;

	}

	/*
	 * public void checkConsistency(){ if (type.equals("class") && (
	 * instances.size()>0 || datatype.size()>0)){ System.out.println("Warning,
	 * inconsistent:"+this.toString()); }
	 *  }
	 */

	public abstract Vector<Node> expand(TypedSparqlQuery tsq, Manipulator m);

	public abstract Set<String> toNTriple();

	@Override
	public String toString() {
		return "Node: " + uri + ":" + type;

	}

	public URI getURI() {
		return uri;
	}

}
