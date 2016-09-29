package org.dllearner.algorithms.qtl.util;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.util.NodeComparator;
import org.dllearner.algorithms.qtl.datastructures.NodeInv;

public class NodeComparatorInv extends NodeComparator {
	@Override
	public int compare(Node o1, Node o2) {
		int val1 = o1 instanceof NodeInv ? 1 : 0;
		int val2 = o2 instanceof NodeInv ? 1 : 0;

		if (val1 == val2) {
			return super.compare(o1, o2);
		}
		return val1 - val2;
	}
}