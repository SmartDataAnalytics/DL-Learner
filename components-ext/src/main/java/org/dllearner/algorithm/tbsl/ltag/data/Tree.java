package org.dllearner.algorithm.tbsl.ltag.data;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.algorithm.tbsl.ltag.agreement.Feature;


public class Tree implements TreeNode {

	Category category;
	List<TreeNode> children;
	Tree parent;
	boolean NA;
	String adjLabel;
	private Feature feature;
	
 	public Tree()
 	{
 		children = new ArrayList<TreeNode>();
 		parent = null;
 		adjLabel = "";
 	}
 	
 	public Tree(Category cat)
 	{
 		children = new ArrayList<TreeNode>();
 		category = cat;
 		parent = null;
 		adjLabel = "";
 	}
	
 	public Tree clone() {
 		Tree output = new Tree();
		output.setCategory(category);
		output.NA = NA;
		output.adjLabel = adjLabel;
		output.feature = feature;
		
		for (TreeNode child : children) {
			output.addChild(child.clone());
		}
		output.setParentForTree();
		return output;
 	}
 	
	public TreeNode substitute( String index, TreeNode tree ) {
		
		Tree output = new Tree();
		
		output.setCategory(category);
		output.setAdjLabel(adjLabel);
		output.setAdjConstraint(NA);

		for (TreeNode child: children)
		{
			output.addChild(child.substitute(index, tree));
		}
		
		return output;	
	}
	
	public Tree adjoin( String label, TreeNode tree ) throws UnsupportedOperationException {

		if ( tree.isAuxTree() ) {

			Tree output = new Tree(category);
			output.setAdjLabel(adjLabel);
			output.setAdjConstraint(NA);
			
			output.setChildren(new ArrayList<TreeNode>());
			
			if ( category != null && adjLabel.equals(label) ) {	
				for (TreeNode child : tree.getChildren()) {
					output.addChild(child.replaceFoot(children));
				}
			}
			else { 
				for (TreeNode child : children) {
					output.addChild(child.adjoin(label,tree));
				}
			}
			return output;
		}	
		else { throw new UnsupportedOperationException("adjoin failed because the following argument is not an auxiliary tree:\n" + tree.toString()); }
	}

	/** Check whether a tree is an auxiliary tree:
	 *  A tree is an auxiliary tree iff it has exactly one footnode 
	 *  and that footnode's category is the same as the root's category.
	 */
	public boolean isAuxTree() {
	
		List<FootNode> footNodes = getFootNodes();
	
		if ( footNodes.size() == 1 ) {
			FootNode footNode = footNodes.get(0);	
			return (category.equals(footNode.category));
		}
		else {
			return false;
		}
	}
	
	// replace FootNode (parallel to substitute)
	public Tree replaceFoot(List<TreeNode> trees ) {
			
		Tree output = new Tree();
		
		output.setCategory(category);
		
		for (TreeNode child : children) {
			output.addChild(child.replaceFoot(trees));
		}
		
		return output;
	}


	public void addChild(TreeNode tree) 
	{
		children.add(tree);
	}
	
	public void setChildren(List<TreeNode> treelist) 
	{ 
		children = treelist; 
	}

	public List<TreeNode> getChildren()
	{
		return children;
	}
	
	public void setCategory(Category cat) 
	{
		category = cat;
	}
	
	public void setAdjLabel(String label) {
		adjLabel = label;
	}
	
	public String getAdjLabel() {
		return adjLabel;
	}
	
	public Category getCategory()
	{
		return category;
	}
	
	public void setParent(Tree tree)
	{
		parent = tree;
	}
	
	public Tree getParent()
	{
		return parent;
	}
	
	public boolean getAdjConstraint () {
		return NA;
	}
	
	public void setAdjConstraint (boolean x) {
		NA = x;
	}
	
	public void setParentForTree() {
		// sets parent pointer for each node under this instance
		
		for ( TreeNode child : children ) {
			child.setParent(this);
			child.setParentForTree();
		}	
	}
	
	public boolean isRoot()
	{
		if ( parent == null ) {
			return true;
		}
		return false;
	}
	
	public TreeNode getLeftMostChild()
	{
		if ( children.size() > 0 ) {
			return children.get(0);
		}
		return null;
	}
	
	public TreeNode getRightSibling()
	{
		int idx = this.parent.children.indexOf(this);
		
		// if this is rightmost children of parent,
		if( idx == parent.children.size()-1 ) {
			return null;
		}
		
		else {
			// return right sibling
			return parent.children.get(idx+1);
		}
	}
	
	public List<FootNode> getFootNodes() 
	{	
		List<FootNode> output = new ArrayList<FootNode>();
		
		for (TreeNode child : children) {
			output.addAll(child.getFootNodes());
		}
		
		return output;
	}

	public List<TerminalNode> getTerminalNodes()
	{
		List<TerminalNode> output = new ArrayList<TerminalNode>();
		
		for (TreeNode child : children) {
			output.addAll(child.getTerminalNodes());
		}
		
		return output;
	}
	
	public String getAnchor()
	{
		String output = "";
		
		for (TreeNode child : children) {
			output += child.getAnchor();
		}
		
		return output;
	}
	public void setAnchor(String old_anchor,String new_anchor) {
		for (TreeNode child : children) {
			child.setAnchor(old_anchor,new_anchor);
		}
	}
	
	public String toString()
	{
		return this.toString("");
		
	}
	public String toString(String indent)
	{
		String string = ""+indent;
		
		if (NA) { string += "^"; }
		
		//if (adjLabel!="") { string+="<"+adjLabel+">"; }
		
		string += category.toString();
		
		for (TreeNode child: children)
		{
			string += "\n"+child.toString(indent+"  ");
		}
		return string;
		
	}

	public String toFileString() {
		String caseStr = "";
		if (this.getFeature()!=null) {
			caseStr = this.getFeature().toString().toLowerCase();
		}
		String res = "("+this.getCategory().toString()+caseStr;
		for (TreeNode child : children) {
			res += " "+child.toFileString();
		}
		res+=")";
		return res;
	}
	
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (NA ? 1231 : 1237);
		result = prime * result
				+ ((category == null) ? 0 : category.hashCode());
		result = prime * result
				+ ((children == null) ? 0 : children.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Tree))
			return false;
		Tree other = (Tree) obj;
		if (NA != other.NA)
			return false;
		if (category == null) {
			if (other.category != null)
				return false;
		} else if (!category.equals(other.category))
			return false;
		if (children == null) {
			if (other.children != null)
				return false;
		} else if (!children.equals(other.children))
			return false;
		return true;
	}

	public Feature getFeature() {
		return feature;
	}
	
	public void setFeature(Feature c) {
		feature = c;
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