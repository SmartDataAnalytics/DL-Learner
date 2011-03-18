package org.dllearner.algorithm.tbsl.ltag.data;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.algorithm.tbsl.ltag.agreement.Feature;


/**
 * A FootNode represents a TreeNode in an auxiliary tree which has the same
 * category as the root node. No adjunction is allowed at a FootNode.
 **/

public class FootNode implements TreeNode {

	Category category;
	Tree parent;
	boolean NA = false;

	public FootNode(Category cat) {
		category = cat;
		parent = null;
	}

	public FootNode clone() {
		FootNode out = new FootNode(category);
		out.NA = NA;
		return out;
	}
	
	public TreeNode adjoin(String label, TreeNode tree)
			throws UnsupportedOperationException {

		if (tree.isAuxTree()) {

			FootNode output = new FootNode(category);

			output.setChildren(new ArrayList<TreeNode>());

			return output;
		} else {
			throw new UnsupportedOperationException(
					"adjoin failed at foot node because the following argument is not an auxiliary tree:\n"
							+ tree.toString());
		}
	}

	public FootNode substitute(String index, TreeNode tree) {
		return this;
	}

	public Tree replaceFoot(List<TreeNode> trees) {
		Tree output = new Tree();

		output.setCategory(category);
		output.setChildren(trees);

		return output;
	}

	public boolean isAuxTree() {

		List<FootNode> footNodes = getFootNodes();

		if (footNodes.size() == 1) {
			FootNode footNode = footNodes.get(0);
			return (category.equals(footNode.category));
		} else {
			return false;
		}
	}

	public List<FootNode> getFootNodes() {

		List<FootNode> output = new ArrayList<FootNode>();

		output.add(this);

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
		return category + "*";
	}

	public String toFileString() {
		return this.getCategory().toString()+"*";
	}
	
	public String toString(String indent) {
		return indent + category + "*";
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
		return NA;
	}

	public void setAdjConstraint(boolean x) {
		NA = x;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((category == null) ? 0 : category.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof FootNode))
			return false;
		FootNode other = (FootNode) obj;
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		return true;
	}

	public String getAnchor() {
		return "";
	}

	public Feature getFeature() {
		return null;
	}
	
	public void setFeature(Feature f) {}


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
