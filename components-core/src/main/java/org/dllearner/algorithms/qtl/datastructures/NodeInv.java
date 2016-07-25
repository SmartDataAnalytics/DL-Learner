package org.dllearner.algorithms.qtl.datastructures;

import com.google.common.collect.ComparisonChain;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_URI;
import org.apache.jena.sparql.util.NodeComparator;

/**
 * @author Lorenz Buehmann
 */
public class NodeInv extends Node_URI implements Comparable<NodeInv>{

	private final Node node;

	public NodeInv(Node node) {
		super(node.getURI());
		this.node = node;
	}

	public Node getNode() {
		return node;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof NodeInv)) return false;
		if (!super.equals(o)) return false;

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
		int match = this.getClass().equals(o.getClass()) ? 1 : 0;
		if(match == 1) {
			return new NodeComparator().compare(this, o);
		} else {
			return -1;
		}
	}
}
