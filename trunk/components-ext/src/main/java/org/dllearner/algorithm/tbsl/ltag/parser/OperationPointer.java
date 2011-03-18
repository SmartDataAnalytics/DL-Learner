package org.dllearner.algorithm.tbsl.ltag.parser;

import org.dllearner.algorithm.tbsl.ltag.data.TreeNode;


/**
 * An OperationPointer is a superclass for AdjunctionPointer and
 * SubstitutionPointer. It contains a tree ID for the tree that is
 * adjoined/substituted into, and the address in the tree.
 **/

class OperationPointer {

	private short tid;
	private TreeNode dot;

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dot == null) ? 0 : dot.hashCode());
		result = prime * result + tid;
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof OperationPointer))
			return false;
		OperationPointer other = (OperationPointer) obj;
		if (dot == null) {
			if (other.dot != null)
				return false;
		} else if (!dot.equals(other.dot))
			return false;
		if (tid != other.tid)
			return false;
		return true;
	}

	short getTid() {
		return tid;
	}

	void setTid(short tid) {
		this.tid = tid;
	}

	TreeNode getDot() {
		return dot;
	}

	void setDot(TreeNode dot) {
		this.dot = dot;
	}

}
