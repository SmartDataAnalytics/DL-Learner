package org.dllearner.algorithms.qtl.datastructures;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeVisitor;
import org.apache.jena.graph.Node_Concrete;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.sparql.util.NodeComparator;

/**
 * @author Lorenz Buehmann
 */
public class NodeInv extends Node_Concrete implements Comparable<NodeInv>{

	private final Node node;

	public NodeInv(Node node) {
		super(node.getURI());
		this.node = node;
	}

	public Node getNode() {
		return node;
	}

	@Override
	public Object visitWith(NodeVisitor v) {
		{ return v.visitURI( (Node_URI) node, (String) label ); }
	}

	@Override
	public boolean isURI() {
		return true;
	}

	@Override
	public String getURI() {
		return node.getURI();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof NodeInv)) return false;

		NodeInv nodeInv = (NodeInv) o;

		return node.equals(nodeInv.node);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + node.hashCode();
		return result;
	}

	@Override
	public int compareTo(NodeInv o) {
		if(this.getClass().equals(o.getClass())) {
			return new NodeComparator().compare(this, o);
		} else {
			return -1;
		}
	}
}
