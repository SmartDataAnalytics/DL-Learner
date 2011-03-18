package org.dllearner.algorithm.tbsl.ltag.parser;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.dllearner.algorithm.tbsl.ltag.data.Category;
import org.dllearner.algorithm.tbsl.ltag.data.TreeNode;
import org.dllearner.algorithm.tbsl.sem.util.Pair;

/**
 * ParseGrammar is an internal object for Parser. It contains the trees of the
 * global grammar filtered by GrammarFilter.filter(). Each element of
 * ParseGrammar is a Pair of a TreeNode and an ID. This ID is a local ID for the
 * current Parser run. Therefore ParseGrammar also holds a mapping from local
 * IDs to global IDs from a TAG.
 * 
 * @param auxTrees
 *            is a List of auxiliary trees and their local ids
 * @param initTrees
 *            is a List of elementary trees and their local ids
 * @param index
 *            holds a mapping between a local ID and the corresponding tree (1:1
 *            relationship)
 * @param localIdsToGlobalIds
 *            holds a mapping between a local ID and the corresponding global ID
 *            in the TAG (n:1 relationship)
 */

public class ParseGrammar extends ArrayList<Pair<TreeNode, Short>> {

	private static final long serialVersionUID = 3474795610859576772L;

	private ArrayList<Pair<TreeNode, Short>> auxTrees;
	private ArrayList<Pair<TreeNode, Short>> initTrees;
	private Hashtable<Short, TreeNode> index;
	private Hashtable<Short, Integer> localIdsToGlobalIds;

	ParseGrammar(int inputLength) {

		int capacity = (int) (inputLength / 0.75);
		initTrees = new ArrayList<Pair<TreeNode, Short>>();
		auxTrees = new ArrayList<Pair<TreeNode, Short>>();
		index = new Hashtable<Short, TreeNode>(capacity);
		localIdsToGlobalIds = new Hashtable<Short, Integer>();

	}

	List<Pair<TreeNode, Short>> getAdjunctTrees(ParseState state) {

		List<Pair<TreeNode, Short>> output = new ArrayList<Pair<TreeNode, Short>>();

		// if node at dot allows no adjunction return empty list
		if (state.dot.getAdjConstraint()) {
			return output;
		}

		for (Pair<TreeNode, Short> pair : this.getAuxTrees()) {
			TreeNode tree = pair.getFirst();
			short tid = pair.getSecond();

			if (tid != state.tid
					&& tree.getCategory().equals(state.dot.getCategory())
					&& !state.usedTrees.contains(tid)) {

				output.add(new Pair<TreeNode, Short>(tree, tid));

			}

		}

		return output;

	}

	List<Pair<TreeNode, Short>> getAuxTrees() {
		return auxTrees;
	}

	List<Pair<TreeNode, Short>> getInitTrees() {
		return initTrees;
	}

	List<Pair<TreeNode, Short>> getDPInitTrees() {

		List<Pair<TreeNode, Short>> output = new ArrayList<Pair<TreeNode, Short>>();

		for (Pair<TreeNode, Short> pair : this) {
			if (!pair.getFirst().isAuxTree()
					&& pair.getFirst().getCategory().equals(Category.DP)) {
				output.add(pair);
			}
		}
		return output;
	}

	Hashtable<Short, TreeNode> getIndex() {
		return index;
	}

	Hashtable<Short, Integer> getLocalIdsToGlobalIds() {
		return localIdsToGlobalIds;
	}

	public String toString() {

		String out = this.size() + " Trees:\n[\n";

		for (Pair<TreeNode, Short> pair : this) {

			String t = pair.getFirst().toFileString();
			short id = pair.getSecond();
			String anchor = pair.getFirst().getAnchor().trim();

			out += " " + id + "\t" + anchor + " || " + t + "\n";

		}

		return out + "]\n";
	}

	void setAuxTrees(ArrayList<Pair<TreeNode, Short>> auxTrees) {
		this.auxTrees = auxTrees;
	}

	void setInitTrees(ArrayList<Pair<TreeNode, Short>> initTrees) {
		this.initTrees = initTrees;
	}

	void setIndex(Hashtable<Short, TreeNode> index) {
		this.index = index;
	}

	void setLocalIdsToGlobalIds(Hashtable<Short, Integer> localIdsToGlobalIds) {
		this.localIdsToGlobalIds = localIdsToGlobalIds;
	}

}
