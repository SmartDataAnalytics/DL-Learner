package org.dllearner.algorithm.tbsl.ltag.parser;

import org.dllearner.algorithm.tbsl.ltag.data.TreeNode;


/**
 * A SubstitutionPointer extends an OperationPointer with a pointer to a state,
 * where a substitution was predicted.
 **/
public class SubstitutionPointer extends OperationPointer {

	private ParseState sp;

	public SubstitutionPointer(short treeID, TreeNode address, ParseState sp) {
		setTid(treeID);
		setDot(address);
		setSp(sp);
	}

	public String toString() {
		String dotStr = getDot().getCategory().toString();
		if (getDot().getParent() == null) {
			dotStr = "0";
		}
		String output = "<#" + getTid() + " @" + dotStr + " [";
		if (sp == null) {
			output += " ";
		} else {
			output += "+";
		}
		output += "]>";
		return output;
	}

	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((sp == null) ? 0 : sp.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof SubstitutionPointer))
			return false;
		SubstitutionPointer other = (SubstitutionPointer) obj;
		if (sp == null) {
			if (other.sp != null)
				return false;
		} else if (!sp.equals(other.sp))
			return false;
		return true;
	}

	ParseState getSp() {
		return sp;
	}

	void setSp(ParseState sp) {
		this.sp = sp;
	}

}
