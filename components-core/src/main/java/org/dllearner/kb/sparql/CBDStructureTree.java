package org.dllearner.kb.sparql;

import org.dllearner.algorithms.qtl.datastructures.impl.GenericTree;

/**
 * @author Lorenz Buehmann
 */
public class CBDStructureTree extends GenericTree<String, CBDStructureTree> {

	private static final String IN_NODE = "in";
	private static final String OUT_NODE = "out";

	public CBDStructureTree(String data) {
		super(data);
	}

	public boolean isInNode() {
		return data.equals(IN_NODE);
	}

	public boolean isOutNode() {
		return data.equals(OUT_NODE);
	}

	public CBDStructureTree addInNode() {
		CBDStructureTree child = new CBDStructureTree(IN_NODE);
		addChild(child);
		return child;
	}

	public CBDStructureTree addOutNode() {
		CBDStructureTree child = new CBDStructureTree(OUT_NODE);
		addChild(child);
		return child;
	}
}
