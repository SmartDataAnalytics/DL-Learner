package org.dllearner.algorithm.tbsl.ltag.data;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.algorithm.tbsl.ltag.agreement.Feature;


/**
 * A SubstNode represents a TreeNode within a tree where the substitution
 * operation is applicable. It has a field index that stores the (string) id of
 * this substitution node
 **/

public class SubstNode implements TreeNode {

	Category category;
	String index;
	Tree parent;
	Feature constraints;

	public SubstNode(String ind, Category cat, Feature f) {

		category = cat;
		index = ind;
		parent = null;
		constraints = f;

	}

	public SubstNode adjoin(String label, TreeNode tree) {
		return this;
	}

	public TreeNode substitute(String ind, TreeNode tree) {
		if (index.equals(ind)) {
			return tree;
		} else {
			return this;
		}

	}

	public SubstNode replaceFoot(List<TreeNode> trees) {
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

	public SubstNode clone() {
		return new SubstNode(index, category, constraints);
	}

	public void setChildren(List<TreeNode> treelist) {

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
		return category.toString() + "[" + index + "]";
	}

	public String toString(String indent) {
		return indent + category.toString() + "[" + index + "]";
	}

	public String toFileString() {
		String constStr = "";
		if (constraints!=null) {
			constStr = constraints.toString();
		}
		return this.getCategory().toString() + "[" + this.index + "]"
				+ constStr;
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
		result = prime * result + ((index == null) ? 0 : index.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof SubstNode))
			return false;
		SubstNode other = (SubstNode) obj;
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (index == null) {
			if (other.index != null)
				return false;
		} else if (!index.equals(other.index))
			return false;
		return true;
	}

	public String getAnchor() {
		return "";
	}
	public TreeNode setAnchor(String a) {
		return this;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public Feature getFeatureConstraints() {
		return constraints;
	}

	public void setFeatureConstraints(Feature f) {
		this.constraints = f;
	}

	public Feature getFeature() {
		return null;
	}
	
	public void setFeature(Feature f) {
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

	@Override
	public void setAnchor(String old_anchor, String new_anchor) {		
	}

}
