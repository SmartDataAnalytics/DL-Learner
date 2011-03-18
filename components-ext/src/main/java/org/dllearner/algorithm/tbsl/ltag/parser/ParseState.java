package org.dllearner.algorithm.tbsl.ltag.parser;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.dllearner.algorithm.tbsl.ltag.data.FootNode;
import org.dllearner.algorithm.tbsl.ltag.data.SubstNode;
import org.dllearner.algorithm.tbsl.ltag.data.TerminalNode;
import org.dllearner.algorithm.tbsl.ltag.data.Tree;
import org.dllearner.algorithm.tbsl.ltag.data.TreeNode;

/**
 * A ParseState represents a state during a parse of a certain input string.
 * ParserOperations generate new ParseStates with different parameters and can
 * apply only to certain configurations of ParseStates.
 * 
 * @param tid
 *            is the id of the current tree (unique ID for the input string)
 * @param t
 *            is the TreeNode object (not unique)
 * @param dot
 *            is the TreeNode object within t and denotes the current position
 *            of the dot
 * @param side
 *            defines the position of the dot vertically (left or right)
 * @param pos
 *            defines the position of the dot horizontally (above or below)
 * @param l
 *            is the index of the input string where the tree span begins
 * @param f_l
 * @param f_r
 * @param star
 *            address of the starred node in the tree. Signals a predicted
 *            adjunction
 * @param t_star_l
 * @param b_star_l
 * @param subst
 *            boolean value that indicates if a ParseState is part of a
 *            substitution
 * @param i
 *            current StateSet the ParseState belongs to
 * @param pointer
 *            Hashtable<Integer, AdjunctionPointer>. Stores adjunctions on this
 *            path
 * @param substPointer
 *            Hashtable<Integer, SubstitutionPointer>. Stores substitutions on
 *            this path
 */

class ParseState {

	Short tid; // id of the tree
	TreeNode t; // the dotted tree
	TreeNode dot; // address of the dot in t
	char side; // dot is either left 'l' or right 'r'
	char pos; // dot is either above 'a' or below 'b'
	Integer l; // index of the input string where alpha begins
	Integer f_l; // 
	Integer f_r; //
	TreeNode star; // address of the starred node in tree alpha
	Integer t_star_l; // 
	Integer b_star_l; //
	boolean subst; // shows if tree has been predicted for substitution
	Integer i; // current i the state belongs to
	ArrayList<Short> usedTrees; // saves the TreeIDs of the Trees
	// already used on this path
	Hashtable<Short, AdjunctionPointer> pointer;
	Hashtable<Short, SubstitutionPointer> substPointer;

	// Constructors
	ParseState(TreeNode tree, short treeID) {
		tid = treeID;
		t = tree;
		dot = t; // dot is on root
		side = 'l'; // left
		pos = 'a'; // above
		l = 0; // tree spans from index 0
		i = 0;
		subst = false;
		usedTrees = new ArrayList<Short>();
		pointer = new Hashtable<Short, AdjunctionPointer>();
		substPointer = new Hashtable<Short, SubstitutionPointer>();

	}

	// create new ParseState instance from an existing instance
	ParseState(ParseState state) {
		this.tid = state.tid;
		this.t = state.t;
		this.dot = state.dot;
		this.side = state.side;
		this.pos = state.pos;
		this.l = state.l;
		this.f_l = state.f_l;
		this.f_r = state.f_r;
		this.star = state.star;
		this.t_star_l = state.t_star_l;
		this.b_star_l = state.b_star_l;
		this.i = state.i;
		this.subst = state.subst;
		this.usedTrees = state.usedTrees;
		this.pointer = new Hashtable<Short, AdjunctionPointer>(state.pointer);
		this.substPointer = new Hashtable<Short, SubstitutionPointer>(
				state.substPointer);
	}

	public boolean isEndState() {

		if (!(t.isAuxTree()) && dot == t && side == 'r' && pos == 'a'
				&& l.equals(0) && this.f_l == null && this.f_r == null
				&& this.star == null && this.t_star_l == null
				& this.b_star_l == null) {

			return true;
		}

		return false;
	}

	private boolean isLA() {
		if ((this.side == 'l' && this.pos == 'a')) {
			return true;
		}
		return false;
	}

	private boolean isRA() {
		if ((this.side == 'r' && this.pos == 'a')) {
			return true;
		}
		return false;
	}

	private boolean isLB() {
		if ((this.side == 'l' && this.pos == 'b')) {
			return true;
		}
		return false;
	}

	private boolean isRB() {
		if ((this.side == 'r' && this.pos == 'b')) {
			return true;
		}
		return false;
	}

	public boolean isSPState() {
		if (this.isLA() && (this.dot instanceof SubstNode)) {
			return true;
		}
		return false;
	}

	public boolean isScanState() {
		if (this.isLA() && (this.dot instanceof TerminalNode)) {
			return true;
		}
		return false;
	}

	public boolean isLPState() {
		if (this.isLA() && (!(this.dot instanceof TerminalNode))
				&& (!(this.dot instanceof SubstNode))) {
			return true;
		}
		return false;
	}

	public boolean isLCState() {
		if (this.isLB() && (this.dot instanceof FootNode)) {
			return true;
		}
		return false;
	}

	public boolean isMDDState() {
		if (this.isLB() && (this.dot instanceof Tree)) {
			return true;
		}
		return false;
	}

	public boolean isMDUState() {
		if (this.isRA() && this.dot.getParent() != null) {
			return true;
		}
		return false;
	}

	public boolean isRPState() {
		return this.isRB();
	}

	public boolean isRCState() {
		if (this.isRA() && this.t.getParent() == null && this.t.isAuxTree()
				&& this.dot.equals(this.t)) {
			return true;
		}
		return false;
	}

	public boolean isSCState() {
		if (this.isRA() && this.subst == true && this.t.equals(this.dot)) {
			return true;
		}
		return false;
	}

	public Hashtable<Short, AdjunctionPointer> createNewPointer() {
		Hashtable<Short, AdjunctionPointer> output = new Hashtable<Short, AdjunctionPointer>();

		Iterator<Short> it = pointer.keySet().iterator();
		while (it.hasNext()) {
			short key = it.next();
			short tid = key;
			AdjunctionPointer oldPtr = pointer.get(key);
			AdjunctionPointer newPtr = new AdjunctionPointer(oldPtr.getTid(),
					oldPtr.getDot(), oldPtr.getLp());
			newPtr.setLc(oldPtr.getLc());
			newPtr.setRp(oldPtr.getRp());
			output.put(tid, newPtr);

		}

		return output;
	}

	public Hashtable<Short, SubstitutionPointer> createNewSubstPointer() {
		Hashtable<Short, SubstitutionPointer> output = new Hashtable<Short, SubstitutionPointer>();

		Iterator<Short> it = substPointer.keySet().iterator();
		while (it.hasNext()) {
			short key = it.next();
			short tid = key;
			SubstitutionPointer oldPtr = substPointer.get(key);
			SubstitutionPointer newPtr = new SubstitutionPointer(oldPtr
					.getTid(), oldPtr.getDot(), oldPtr.getSp());
			output.put(tid, newPtr);

		}

		return output;
	}

	public String toString() {
		String starStr = null;
		if (!(star == null)) {
			starStr = star.getCategory().toString();
		}
		if (star != null && star.equals(t)) {
			starStr = "0";
		}
		if (star instanceof FootNode) {
			starStr += "*";
		}

		String dotStr = dot.getCategory().toString();
		if (t.equals(dot)) {
			dotStr = "0";
		}

		String footStr = "";
		if (dot instanceof FootNode) {
			footStr = "*";
		}

		String endStr = "";
		if (this.isEndState()) {
			endStr = "<E>";
		}
		return endStr + this.hashCode() + "\t {[#" + tid + " "
				+ t.getCategory() + "<" + t.getAnchor() + ">" + ", " + dotStr
				+ footStr + ", (" + side + ", " + pos + "), " + l + ", (" + f_l
				+ ", " + f_r + "), " + starStr + ", (" + t_star_l + ", "
				+ b_star_l + "), " + subst + "] | i" + i + " | by: "
				+ usedTrees + " A:" + pointer + " S:" + substPointer + "}\n";
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((b_star_l == null) ? 0 : b_star_l.hashCode());
		result = prime * result + ((dot == null) ? 0 : dot.hashCode());
		result = prime * result + ((f_l == null) ? 0 : f_l.hashCode());
		result = prime * result + ((f_r == null) ? 0 : f_r.hashCode());
		result = prime * result + ((i == null) ? 0 : i.hashCode());
		result = prime * result + ((l == null) ? 0 : l.hashCode());
		result = prime * result + pos;
		result = prime * result + side;
		result = prime * result + ((star == null) ? 0 : star.hashCode());
		result = prime * result + (subst ? 1231 : 1237);
		result = prime * result + ((t == null) ? 0 : t.hashCode());
		result = prime * result
				+ ((t_star_l == null) ? 0 : t_star_l.hashCode());
		result = prime * result + tid;
		result = prime * result
				+ ((usedTrees == null) ? 0 : usedTrees.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ParseState))
			return false;
		ParseState other = (ParseState) obj;
		if (b_star_l == null) {
			if (other.b_star_l != null)
				return false;
		} else if (!b_star_l.equals(other.b_star_l))
			return false;
		if (dot == null) {
			if (other.dot != null)
				return false;
		} else if (!dot.equals(other.dot))
			return false;
		if (f_l == null) {
			if (other.f_l != null)
				return false;
		} else if (!f_l.equals(other.f_l))
			return false;
		if (f_r == null) {
			if (other.f_r != null)
				return false;
		} else if (!f_r.equals(other.f_r))
			return false;
		if (i == null) {
			if (other.i != null)
				return false;
		} else if (!i.equals(other.i))
			return false;
		if (l == null) {
			if (other.l != null)
				return false;
		} else if (!l.equals(other.l))
			return false;
		if (pos != other.pos)
			return false;
		if (side != other.side)
			return false;
		if (star == null) {
			if (other.star != null)
				return false;
		} else if (!star.equals(other.star))
			return false;
		if (subst != other.subst)
			return false;
		if (t == null) {
			if (other.t != null)
				return false;
		} else if (!t.equals(other.t))
			return false;
		if (t_star_l == null) {
			if (other.t_star_l != null)
				return false;
		} else if (!t_star_l.equals(other.t_star_l))
			return false;
		if (tid != other.tid)
			return false;
		if (usedTrees == null) {
			if (other.usedTrees != null)
				return false;
		} else if (!usedTrees.equals(other.usedTrees))
			return false;
		return true;
	}

	Short getTid() {
		return tid;
	}

	void setTid(Short tid) {
		this.tid = tid;
	}

	TreeNode getT() {
		return t;
	}

	void setT(TreeNode t) {
		this.t = t;
	}

	TreeNode getDot() {
		return dot;
	}

	void setDot(TreeNode dot) {
		this.dot = dot;
	}

	char getSide() {
		return side;
	}

	void setSide(char side) {
		this.side = side;
	}

	char getPos() {
		return pos;
	}

	void setPos(char pos) {
		this.pos = pos;
	}

	Integer getL() {
		return l;
	}

	void setL(Integer l) {
		this.l = l;
	}

	Integer getF_l() {
		return f_l;
	}

	void setF_l(Integer fL) {
		f_l = fL;
	}

	Integer getF_r() {
		return f_r;
	}

	void setF_r(Integer fR) {
		f_r = fR;
	}

	TreeNode getStar() {
		return star;
	}

	void setStar(TreeNode star) {
		this.star = star;
	}

	Integer getT_star_l() {
		return t_star_l;
	}

	void setT_star_l(Integer tStarL) {
		t_star_l = tStarL;
	}

	Integer getB_star_l() {
		return b_star_l;
	}

	void setB_star_l(Integer bStarL) {
		b_star_l = bStarL;
	}

	boolean isSubst() {
		return subst;
	}

	void setSubst(boolean subst) {
		this.subst = subst;
	}

	Integer getI() {
		return i;
	}

	void setI(Integer i) {
		this.i = i;
	}

	ArrayList<Short> getUsedTrees() {
		return usedTrees;
	}

	void setUsedTrees(ArrayList<Short> usedTrees) {
		this.usedTrees = usedTrees;
	}

	Hashtable<Short, AdjunctionPointer> getPointer() {
		return pointer;
	}

	void setPointer(Hashtable<Short, AdjunctionPointer> pointer) {
		this.pointer = pointer;
	}

	Hashtable<Short, SubstitutionPointer> getSubstPointer() {
		return substPointer;
	}

	void setSubstPointer(Hashtable<Short, SubstitutionPointer> substPointer) {
		this.substPointer = substPointer;
	}

}
