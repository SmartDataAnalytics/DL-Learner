package org.dllearner.algorithms.distributed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DistOENodeTree2 extends TreeSet<DistOENode2> implements Serializable {

	private static final long serialVersionUID = 1884888812047344468L;
	private static Logger logger = LoggerFactory.getLogger(DistOENodeTree2.class);

	protected DistOENode2 root;
	private Map<DistOENode2, DistOENode2> parentOf;
	private Map<DistOENode2, List<DistOENode2>> childrenOf;
	private HashSet<DistOENode2> nodesInUse;
	private HashMap<UUID, DistOENode2> id2node;

	// <---------------------------- constructors ---------------------------->
	public DistOENodeTree2() {
		super(new DistOEHeuristicRuntime2());

		initHelperMaps();
	}

	public DistOENodeTree2(DistOENode2 root) {
		this(root, new DistOEHeuristicRuntime2());
	}

	public DistOENodeTree2(DistOENode2 root, AbstractDistHeuristic2 heuristic) {
		super(heuristic);
		initHelperMaps();
		setRoot(root);
	}

	private void initHelperMaps() {
		parentOf = new HashMap<DistOENode2, DistOENode2>();
		childrenOf = new HashMap<DistOENode2, List<DistOENode2>>();
		nodesInUse = new HashSet<DistOENode2>();
		id2node = new HashMap<UUID, DistOENode2>();
	}
	// </--------------------------- constructors ---------------------------->

	// <-------------------------- interface methods ------------------------->
	// </------------------------- interface methods ------------------------->

	// <------------------------ non-interface methods ----------------------->
	public void add(DistOENode2 node, DistOENode2 parentNode) {
		childrenOf.get(parentNode).add(node);
		parentOf.put(node, parentNode);

		if (childrenOf.containsKey(node)) {
			throw new RuntimeException(node + " already in tree " + this);
		}

		childrenOf.put(node, new ArrayList<DistOENode2>());
		node.setTree(this);
		add(node);

		id2node.put(node.getUUID(), node);
	}

	public List<DistOENode2> getChildren(DistOENode2 node) {
		return childrenOf.get(node);
	}

	public DistOENode2 getParent(DistOENode2 node) {
		return parentOf.get(node);
	}

	public DistOENodeTree2 getSubTreeCopyAndSetInUse(DistOENode2 node) {
		DistOENode2 newRoot = DistOENode2.copyWithId(node);
		DistOENodeTree2 subTree = new DistOENodeTree2(newRoot);

		if (node.isInUse()) {
			subTree.nodesInUse.add(newRoot);
		} else {
			nodesInUse.add(node);
		}

		for (DistOENode2 child : node.getChildren()) {
			subTree.recursivelyCopyFromRemoteAndSetInUse(child, newRoot);
		}

		return subTree;
	}

	public boolean isInUse(DistOENode2 node) {
		return nodesInUse.contains(node);
	}

	public boolean isRoot(DistOENode2 node) {
		if (root == null) return false;
		else return root.equals(node);
	}

	public void mergeWithAndUnblock(DistOENodeTree2 remoteTree) {
		// TODO: only unblock if remote node is not blocked

		DistOENode2 remoteRoot = remoteTree.getRoot();
		DistOENode2 mergeNode = id2node.get(remoteRoot.getUUID());

		recursivelyCopyTo(mergeNode, remoteRoot);
	}

	private void recursivelyCopyFromRemoteAndSetInUse(
			DistOENode2 node, DistOENode2 parent) {

		DistOENode2 nodeCopy = DistOENode2.copyWithId(node);
		add(nodeCopy, parent);

		if (node.isInUse()) nodesInUse.add(nodeCopy);
		node.setInUse();

		for (DistOENode2 child : node.getChildren()) {
			recursivelyCopyFromRemoteAndSetInUse(child, nodeCopy);
		}
	}

	private void recursivelyAdd(DistOENode2 targetNode, DistOENode2 nodeToAdd) {
		DistOENode2 copyToAdd = DistOENode2.copyWithId(nodeToAdd);
		add(copyToAdd, targetNode);

		for (DistOENode2 remoteChild : nodeToAdd.getChildren()) {
			recursivelyAdd(copyToAdd, remoteChild);
		}
	}

	private void recursivelyCopyTo(DistOENode2 targetNode, DistOENode2 nodeToCopy) {
		targetNode.updateWithValsFrom(nodeToCopy);

		if (!nodeToCopy.isInUse()) {
			// the local node (targetNode) was set in use since the copy
			// nodeToCopy was made and sent out
			nodesInUse.remove(targetNode);
		}

		for (DistOENode2 remoteChild : nodeToCopy.getChildren()) {
			DistOENode2 localCorrespNode = id2node.get(remoteChild.getUUID());

			if (localCorrespNode != null) {
				recursivelyCopyFromRemoteAndSetInUse(localCorrespNode, remoteChild);

			} else {
				recursivelyAdd(targetNode, remoteChild);
			}
		}
	}

	public void reset() {
		root = null;
		parentOf.clear();
		childrenOf.clear();
		nodesInUse.clear();
		clear();
	}

	public void setInUse(DistOENode2 node) {
		nodesInUse.add(node);
	}
	// </----------------------- non-interface methods ----------------------->

	// <------------------------ getter/setter methods ----------------------->
	public DistOENode2 getRoot() {
		return root;
	}

	public void setRoot(DistOENode2 root) {
		if (this.root != null) {
			throw new RuntimeException("root node in " + this + "already set!");

		} else {
			this.root = root;
			root.setTree(this);

			parentOf.put(root, null);
			childrenOf.put(root, new ArrayList<DistOENode2>());
			add(root);
			id2node.put(root.getUUID(), root);
		}

	}
	// </----------------------- getter/setter methods ----------------------->
}
