package org.dllearner.algorithm.tbsl.ltag.parser;

import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.dllearner.algorithm.tbsl.ltag.data.TreeNode;
import org.dllearner.algorithm.tbsl.sem.util.Pair;

/**
 * the interface represents an LTAGLexicon. It is implemented by TAG
 * 
 * @author felix
 * 
 */
public interface LTAGLexicon {

	/**
	 * adds a tree, its anchor and the corresponding semantic string to the
	 * grammar. Requires also the id that is used within the grammar to
	 * distinguish between trees. (use this.size() to obtain the correct id).
	 * 
	 * @param id
	 * @param tree
	 * @param semantics
	 * @return
	 */
	public void clear(List<Integer> temps);
	
	public int addTree(int id, Pair<String, TreeNode> tree, List<String> semantics);

	/**
	 * adds a list of trees, their anchors and the corresponding semantics to
	 * the grammar. The list of pairs and the list of semantics should be of the
	 * same size. (calls addTree() for each pair).
	 * 
	 * @param trees 
	 * @param semantics
	 */
	public void addTrees(List<Pair<String, TreeNode>> trees,
			List<List<String>> semantics);

	/**
	 * returns the Hashtable which maps trees to an anchor
	 */
	public Hashtable<String, List<Pair<Integer, TreeNode>>> getAnchorToTrees();

	/**
	 * returns the Set of anchors which contain a wildcard (used by earleyParser.GrammarFilter())
	 */
	public Set<String> getWildCardAnchors();

	public int size();

	/**
	 * returns the Hashtable which maps a TreeId to the corresponding semantic string
	 */
	public Hashtable<Integer, List<String>> getIdToSemantics();

}
