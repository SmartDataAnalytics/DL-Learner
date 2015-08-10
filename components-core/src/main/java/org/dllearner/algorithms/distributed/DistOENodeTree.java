package org.dllearner.algorithms.distributed;

import java.util.TreeSet;

public class DistOENodeTree extends TreeSet<DistOENode> {

	private static final long serialVersionUID = -1621872429008871002L;

	protected DistOENode root;

	public DistOENodeTree() {
		super(new DistOEHeuristicRuntime());
//		this(
//				new DistOENode(null, OWLManager.getOWLDataFactory().getOWLThing(), 0),
//				new DistOEHeuristicRuntime());
		System.out.println("###################### DistOENodeTree() called ################");
	}

	public DistOENodeTree(DistOENode root) {
		this(root, new DistOEHeuristicRuntime());
	}

	public DistOENodeTree(DistOENode root, AbstractDistHeuristic heuristic) {
		super(heuristic);
		this.root = root;
		add(root);
	}

	public void add(DistOENode nodeToAdd, DistOENode parentNode) {
		remove(parentNode);
		parentNode.addChild(nodeToAdd);
		add(parentNode);
		add(nodeToAdd);
	}
	public DistOENode getCorrespondingLocalNode(DistOENode nonLocalNodeToFind) {
		return root.findCorrespondingLocalNode(nonLocalNodeToFind);
	}

	public DistOENodeTree getSubTreeAndSetUsed(DistOENode subTreeRoot) {
		DistOENodeTree subTree = new DistOENodeTree(subTreeRoot);
		subTreeRoot.setInUse(true);

		TreeSet<DistOENode> descendants = subTreeRoot.getNodeAndDescendantsAndSetUsed();
		subTree.addAll(descendants);

		return subTree;
	}

	public DistOENodeTree getSubTree(DistOENode subTreeRoot) {
		return getSubTree(subTreeRoot, false);
	}

	private DistOENodeTree getSubTree(DistOENode subTreeRoot, boolean setUsed) {
		DistOENodeTree subTree = new DistOENodeTree(subTreeRoot);
		if (setUsed) subTreeRoot.setInUse(true);

		TreeSet<DistOENode> descendants = subTreeRoot.getNodeAndDescendants();
		subTree.addAll(descendants);

		return subTree;
	}

	public void mergeWithAndUnblock(DistOENodeTree otherTree) {
		DistOENode localMergeRoot = getCorrespondingLocalNode(otherTree.getRoot());
		if (localMergeRoot == null) throw new RuntimeException("No suitable merge point found (" + otherTree.getRoot() + ")");
		mergeAtAndUnblock(localMergeRoot, otherTree);
	}

	private void mergeAtAndUnblock(DistOENode localMergeRoot, DistOENodeTree treeToMerge) {
		if (!contains(localMergeRoot)) throw new RuntimeException(localMergeRoot + " not in " + this);

		// will be added again after possible modifications
		DistOENode mergeTreeRoot = treeToMerge.getRoot();
		remove(localMergeRoot);
		localMergeRoot.updateWithDescriptionScoreValsFrom(mergeTreeRoot);
		localMergeRoot.setInUse(false);

		for (DistOENode childNode : mergeTreeRoot.getChildren()) {
			DistOENode correspondingLocalNode = localMergeRoot.findCorrespondingLocalNode(childNode);
			if (correspondingLocalNode == null) {
				DistOENode newLocalChild = childNode.copyTo(localMergeRoot);
				add(newLocalChild);
				mergeAtAndUnblock(newLocalChild, treeToMerge.getSubTree(childNode));

			} else if (!contains(correspondingLocalNode)) {
				throw new RuntimeException(correspondingLocalNode + " not in " + this);

			} else {
				mergeAtAndUnblock(correspondingLocalNode, treeToMerge.getSubTree(childNode));
			}
		}

		add(localMergeRoot);
	}

	public void reset() {
		root = null;
		clear();
	}

	// <-------------------------- getter and setter -------------------------->
	public DistOENode getRoot() {
		return root;
	}

	public void setRoot(DistOENode root) {
		this.root = root;
		add(root);
	}
	// </------------------------- getter and setter -------------------------->
}
