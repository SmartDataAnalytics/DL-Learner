package org.dllearner.algorithm.tbsl.ltag.data;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.algorithm.tbsl.ltag.agreement.Feature;


/**
 * A TerminalNode represents a terminal node in a TreeNode tree. It has a
 * category of lowest projection (N,V,etc.) and contains the terminal string.
 **/

public class TerminalNode implements TreeNode {

	private String terminal;
	Category category;
	Tree parent;
	private Feature feature;

	public TerminalNode(String string, Category cat) {

		setTerminal(string);
		category = cat;
		parent = null;

	}

	public TerminalNode adjoin(String label, TreeNode tree) {
		return this;
	}

	public TerminalNode substitute(String index, TreeNode tree) {
		return this;
	}

	public TerminalNode replaceFoot(List<TreeNode> trees) {
		return this;
	}

	public boolean isAuxTree() {
		return false;
	}

	public List<FootNode> getFootNodes() {
		List<FootNode> output = new ArrayList<FootNode>();
		return output;
	}

	public List<TerminalNode> getTerminalNodes() {

		List<TerminalNode> output = new ArrayList<TerminalNode>();

		output.add(this);

		return output;

	}
	
	public Category getCategory() {
		return category;
	}

	public void setCategory(Category cat) {
		category = cat;
	}

	public List<TreeNode> getChildren() {
		ArrayList<TreeNode> output = new ArrayList<TreeNode>();
		return output;
	}

	public void setChildren(List<TreeNode> treelist) {

	}

	public TerminalNode clone() {
		TerminalNode out = new TerminalNode(getTerminal(), category);
		out.setFeature(feature);
		return out;
	}
	
	public Tree getParent() {
		return parent;
	}

	public void setParent(Tree tree) {
		parent = tree;
	}

	public void setParentForTree() {
	}

	public String toString() {
		return "(" + getTerminal() + ")";
	}

	public String toString(String indent) {
		return indent + category + "(" + getTerminal() + ")";
	}

	public String toFileString() {
		String caseStr = "";
		if (this.getFeature()!=null) {
			caseStr = this.getFeature().toString().toLowerCase();
		}
		return this.getCategory().toString()+caseStr+":'"+getTerminal()+"'";
	}
	
	public TreeNode getRightSibling() {
		int idx = this.parent.children.indexOf(this);
		// if this is rightmost children of parent,
		if (idx == parent.children.size() - 1) {
			return null;
		}

		else {
			// return right sibling
			return parent.children.get(idx + 1);
		}
	}

	public boolean getAdjConstraint() {
		return false;
	}

	public void setAdjConstraint(boolean x) {
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((category == null) ? 0 : category.hashCode());
		result = prime * result
				+ ((getTerminal() == null) ? 0 : getTerminal().hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TerminalNode))
			return false;
		TerminalNode other = (TerminalNode) obj;
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (getTerminal() == null) {
			if (other.getTerminal() != null)
				return false;
		} else if (!getTerminal().equals(other.getTerminal()))
			return false;
		return true;
	}

	public String getAnchor() {
		if (getTerminal().equals("")) {
			return "";
		}
		return getTerminal()+" ";
	}

	public void setTerminal(String terminal) {
		this.terminal = terminal;
	}

	public String getTerminal() {
		return terminal;
	}

	public Feature getFeature() {
		return feature;
	}
	
	public void setFeature(Feature f) {
		feature = f;
	}

	public TreeNode isGovernedBy(Category cat) {
		if (this.getParent() == null) {
			return null;
		} else if (this.getParent().getCategory().equals(cat)){
			return this.getParent();
		} else {
			return this.getParent().isGovernedBy(cat);
		}
	}
	
}
