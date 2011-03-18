package org.dllearner.algorithm.tbsl.ltag.parser;

import org.dllearner.algorithm.tbsl.ltag.data.TreeNode;



/**
 * 
 * An AdjunctionPointer extends an OperationPointer with three additional
 * pointers required for this adjunction: A pointer to the LeftPredictor state,
 * a pointer to the LeftCompletor state and a pointer for the RightPredictor
 * state. The RightCompletor parsing operation requires all three pointer being
 * not null.
 **/
class AdjunctionPointer extends OperationPointer {

	private ParseState lp;
	private ParseState lc;
	private ParseState rp;

	AdjunctionPointer(short treeID, TreeNode address, ParseState lp) {
		setTid(treeID);
		setDot(address);
		setLp(lp);
	}

	public String toString() {
		String dotStr = getDot().getCategory().toString();
		if (getDot().getParent() == null) {
			dotStr = "0";
		}
		String output = "<#" + getTid() + " @" + dotStr + " [";
		if (lp == null) {
			output += " ";
		} else {
			output += "+,";
		}
		if (lc == null) {
			output += " ";
		} else {
			output += "+,";
		}
		if (rp == null) {
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
		result = prime * result + ((lc == null) ? 0 : lc.hashCode());
		result = prime * result + ((lp == null) ? 0 : lp.hashCode());
		result = prime * result + ((rp == null) ? 0 : rp.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof AdjunctionPointer))
			return false;
		AdjunctionPointer other = (AdjunctionPointer) obj;
		if (lc == null) {
			if (other.lc != null)
				return false;
		} else if (!lc.equals(other.lc))
			return false;
		if (lp == null) {
			if (other.lp != null)
				return false;
		} else if (!lp.equals(other.lp))
			return false;
		if (rp == null) {
			if (other.rp != null)
				return false;
		} else if (!rp.equals(other.rp))
			return false;
		return true;
	}

	ParseState getLp() {
		return lp;
	}

	void setLp(ParseState lp) {
		this.lp = lp;
	}

	ParseState getLc() {
		return lc;
	}

	void setLc(ParseState lc) {
		this.lc = lc;
	}

	ParseState getRp() {
		return rp;
	}

	void setRp(ParseState rp) {
		this.rp = rp;
	}

}
