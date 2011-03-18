package org.dllearner.algorithm.tbsl.ltag.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.dllearner.algorithm.tbsl.ltag.data.TreeNode;
import org.dllearner.algorithm.tbsl.sem.util.Pair;

/**
 * The TAG class extends an ArrayList<Pair<TreeNode,Integer>>
 * 
 * @param index
 *            Holds a mapping between treeIDs and the corresponding tree.
 * @param anchorToTrees
 *            Holds a mapping between an lexical anchor and the corresponding
 *            treelist (1:n relationship)
 * @param idToAnchor
 *            Holds a mapping between a global id and the corresponding anchor
 *            (1:1 relationship)
 * @param idToSemantics
 *            Holds a mapping from a global id to the corresponding string that
 *            can be converted into a DUDE semantic.
 * @param wildCardAnchors
 * 			  The set of anchors that contain a wildchard ".+".
 */

public class TAG extends ArrayList<Pair<TreeNode, Integer>> implements
		LTAGLexicon {

	private Hashtable<Integer, TreeNode> index;
	private Hashtable<String, List<Pair<Integer, TreeNode>>> anchorToTrees;
	private Hashtable<Integer, String> idToAnchor;
	private Hashtable<Integer, List<String>> idToSemantics;
	private Set<String> wildCardAnchors;

	private static final long serialVersionUID = -9208313527699529388L;

	public TAG() {

		index = new Hashtable<Integer, TreeNode>();
		anchorToTrees = new Hashtable<String, List<Pair<Integer, TreeNode>>>();
		wildCardAnchors = new HashSet<String>();
		idToAnchor = new Hashtable<Integer, String>();
		idToSemantics = new Hashtable<Integer, List<String>>();

	}
	
	public void clear(List<Integer> temps) {
		
		for (int t : temps) {
			index.remove(t);
			anchorToTrees.remove(idToAnchor.get(t));
			idToAnchor.remove(t);
			idToSemantics.remove(t);
		}
	}

	public void addTrees(List<Pair<String, TreeNode>> trees,
			List<List<String>> semantics) {

		int id = this.size();

		for (int i = 0; i < trees.size(); i++) {

			id = addTree(id, trees.get(i), semantics.get(i));

		}

	}
	
	public int addTree(int id, Pair<String, TreeNode> pair, List<String> sem) {
		
		String anchor = pair.getFirst();
		TreeNode t = pair.getSecond();

		this.add(new Pair<TreeNode, Integer>(t, id));

		index.put(id, t);

		List<Pair<Integer, TreeNode>> anchorTrees = anchorToTrees
				.get(anchor);

		if (anchorTrees == null) {

			anchorTrees = new ArrayList<Pair<Integer, TreeNode>>();
			anchorTrees.add(new Pair<Integer, TreeNode>(id, t));
			anchorToTrees.put(anchor, anchorTrees);

		} else {

			anchorTrees.add(new Pair<Integer, TreeNode>(id, t));

		}

		// add wildcard anchors such as "does .+ live .+ in"
		// to a separate list.
		if (anchor.contains(".+")) {
			wildCardAnchors.add(anchor);
		}

		idToAnchor.put(id, anchor);

		idToSemantics.put(id, sem);
		
		return id+1;
		
	}

	public String toString() {

		String out = this.size() + " Trees:\n[\n";

		for (Pair<TreeNode, Integer> pair : this) {

			String t = pair.getFirst().toFileString();
			int id = pair.getSecond();
			String anchor = idToAnchor.get(id);

			out += " " + id + "\t" + anchor + " || " + t + " || "
					+ idToSemantics.get(id) + "\n";

		}

		return out + "]\n";
	}

	public Hashtable<Integer, TreeNode> getIndex() {
		return index;
	}

	public Hashtable<String, List<Pair<Integer, TreeNode>>> getAnchorToTrees() {
		return anchorToTrees;
	}

	public Hashtable<Integer, String> getIdToAnchor() {
		return idToAnchor;
	}

	public Hashtable<Integer, List<String>> getIdToSemantics() {
		return idToSemantics;
	}

	public Set<String> getWildCardAnchors() {
		return wildCardAnchors;
	}


}
