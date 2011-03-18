package org.dllearner.algorithm.tbsl.ltag.data;

import java.util.List;

import org.dllearner.algorithm.tbsl.ltag.agreement.Feature;


public interface TreeNode {
	
	public TreeNode adjoin(String label, TreeNode tree);

	public TreeNode substitute(String index, TreeNode tree);

	public TreeNode replaceFoot(List<TreeNode> trees);

	public boolean isAuxTree();

	public TreeNode getParent();
	
	public List<TreeNode> getChildren();
	
	public void setChildren(List<TreeNode> treelist);
	
	public Category getCategory();
	
	public void setCategory(Category cat);
	
	public List<FootNode> getFootNodes();
	
	public TreeNode getRightSibling();

	public void setParent(Tree tree);
	
	public void setParentForTree();

	public String toString(String string);
	
	public boolean getAdjConstraint();
	
	public void setAdjConstraint (boolean x);

	public String getAnchor();

	public TreeNode clone();

	public String toFileString();

	public Feature getFeature();
	
	public void setFeature(Feature f);

	public List<TerminalNode> getTerminalNodes();
	
	public TreeNode isGovernedBy(Category cat);
	
}