package org.dllearner.algorithm.tbsl.ltag.parser;

import java.util.ArrayList;
import java.util.Hashtable;

import org.dllearner.algorithm.tbsl.ltag.data.TreeNode;

/**
 * 
 * A DerivationTree contains a List of Operations that are to be executed on an
 * initial tree in order to obtain a DerivedTree
 * @param initTreeID TreeID of the initial tree
 * @param operations List of parsing operations executed during a parse path
 * @param treeMappings Hashtable that maps TreeIDs to trees.
 *  
 **/

public class DerivationTree {

	private short initTreeID;
	private ArrayList<Operation> operations;
	private Hashtable<Short, TreeNode> treeMappings;

	DerivationTree(short tid) {

		initTreeID = tid;
		operations = new ArrayList<Operation>();
		treeMappings = new Hashtable<Short, TreeNode>();

	}

	public String toString() {
		return "initTree: #" + initTreeID + " | " + operations.toString();
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + initTreeID;
		result = prime * result
				+ ((operations == null) ? 0 : operations.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof DerivationTree))
			return false;
		DerivationTree other = (DerivationTree) obj;
		if (initTreeID != other.initTreeID)
			return false;
		if (operations == null) {
			if (other.operations != null)
				return false;
		} else if (!operations.equals(other.operations))
			return false;
		return true;
	}

	public short getInitTreeID() {
		return initTreeID;
	}

	public void setInitTreeID(short initTreeID) {
		this.initTreeID = initTreeID;
	}

	public ArrayList<Operation> getOperations() {
		return operations;
	}

	public void setOperations(ArrayList<Operation> operations) {
		this.operations = operations;
	}

	public Hashtable<Short, TreeNode> getTreeMappings() {
		return treeMappings;
	}

	public void setTreeMappings(Hashtable<Short, TreeNode> treeMappings) {
		this.treeMappings = treeMappings;
	}

}
