package org.dllearner.algorithm.tbsl.ltag.parser;

import org.dllearner.algorithm.tbsl.ltag.data.SubstNode;
import org.dllearner.algorithm.tbsl.ltag.data.TreeNode;

/**
 * Operation represents a LTAG operation. It consists of a tree ID for the tree
 * where something is adjoined or substituted into; a tree ID for the adjoining
 * or substituting tree and an address in tree 1 to denote where the
 * adjunction/substitution is happening.
 **/
class Operation {

	private short tid1; // Tree id for the tree where tid2 adjoins/substitutes into
	private short tid2; // Tree id for the adjoining/substituting tree
	private TreeNode address; // address at tid1 where tid2 adjoins
	private OperationType type; // Type of Operation (SUBSTITUTION or ADJUNCTION)

	public String toString() {
		String dotStr = address.getCategory().toString();
		if (address.getParent() == null) {
			dotStr = "0";
		}
		if (type.equals(OperationType.SUBSTITUTION)) {
			SubstNode addr = (SubstNode) address;
			return "SUB<#" + tid1 + ",@" + dotStr + "[" + addr.getIndex() + "],#"
					+ tid2 + ">";
		} else {
			return "ADJ<#" + tid1 + ",@" + dotStr + ",#" + tid2 + ">";
		}
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + tid1;
		result = prime * result + tid2;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Operation))
			return false;
		Operation other = (Operation) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (tid1 != other.tid1)
			return false;
		if (tid2 != other.tid2)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	short getTid1() {
		return tid1;
	}

	void setTid1(short tid1) {
		this.tid1 = tid1;
	}

	short getTid2() {
		return tid2;
	}

	void setTid2(short tid2) {
		this.tid2 = tid2;
	}

	TreeNode getAddress() {
		return address;
	}

	void setAddress(TreeNode address) {
		this.address = address;
	}

	OperationType getType() {
		return type;
	}

	void setType(OperationType type) {
		this.type = type;
	}

}
