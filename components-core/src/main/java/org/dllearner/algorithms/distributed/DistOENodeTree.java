package org.dllearner.algorithms.distributed;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.TreeSet;

public class DistOENodeTree extends TreeSet<DistOENode> implements Serializable {

	private static final long serialVersionUID = -1621872429008871002L;

	protected DistOENode root;

	public DistOENodeTree() {
		super(new DistOEHeuristicRuntime());
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
		nodeToAdd.setTree(this);
		add(parentNode);
		add(nodeToAdd);
	}

	public boolean remove(DistOENode node) {
		boolean res = ((TreeSet<DistOENode>) this).remove(node);
		node.setTree(null);

		return res;
	}

	public DistOENode getCorrespondingLocalNode(DistOENode nonLocalNodeToFind) {
		return root.findCorrespondingLocalNode(nonLocalNodeToFind);
	}

	public DistOENodeTree getSubTreeCopyAndSetUsed(DistOENode subTreeRoot) {
		DistOENode subTreeRootCopy = new DistOENode(subTreeRoot);
		subTreeRoot.setParent(null);
		subTreeRoot.setInUse(true);
		DistOENodeTree subTree = new DistOENodeTree(subTreeRootCopy);

		TreeSet<DistOENode> descendants = subTreeRoot.copyNodeAndDescendantsAndSetUsed();
		subTree.addAll(descendants);

		return subTree;
	}

	public DistOENodeTree getSubTreeParanoidCopyAndSetUsed(DistOENode subTreeRoot) {
		remove(subTreeRoot);
		subTreeRoot.setInUse(true);
		add(subTreeRoot);

		DistOENodeTree subTree = new DistOENodeTree(subTreeRoot);

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oout = new ObjectOutputStream(bout);
			oout.writeObject(subTreeRoot);
			oout.flush();
			oout.close();
			ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
			DistOENode subTreeRootCopy = (DistOENode) oin.readObject();
			subTree.add(subTreeRootCopy);
			oin.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		TreeSet<DistOENode> descendants = subTreeRoot.copyNodeAndDescendants();
		for (DistOENode desc : descendants) {
			remove(desc);
			desc.setInUse(true);
			add(desc);

			bout = new ByteArrayOutputStream();
			try {
				ObjectOutputStream oout = new ObjectOutputStream(bout);
				oout.writeObject(desc);
				oout.flush();
				oout.close();
				ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(bout.toByteArray()));
				DistOENode node = (DistOENode) oin.readObject();

				subTree.add(node);
				oin.close();
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		return subTree;
	}

	public DistOENodeTree getSubTree(DistOENode subTreeRoot) {
		return getSubTree(subTreeRoot, false);
	}

	private DistOENodeTree getSubTree(DistOENode subTreeRoot, boolean setUsed) {
		DistOENodeTree subTree = new DistOENodeTree(subTreeRoot);
		if (setUsed) subTreeRoot.setInUse(true);

		TreeSet<DistOENode> descendants = subTreeRoot.copyNodeAndDescendants();
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
