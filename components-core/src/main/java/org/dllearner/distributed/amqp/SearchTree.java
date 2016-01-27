package org.dllearner.distributed.amqp;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.dllearner.utilities.datastructures.AbstractSearchTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchTree extends AbstractSearchTree<OENode> {

	private static Logger logger = LoggerFactory.getLogger(SearchTree.class);

	private Set<UUID> blocked;
	private Set<UUID> disabled;
	private Map<UUID, Integer> tmpAggregators;

	public SearchTree(Comparator<OENode> comparator) {
		super(comparator);
		nodes = new TreeSet<>(sortOrderComp);
		blocked = new TreeSet<UUID>();
		disabled = new TreeSet<UUID>();
		tmpAggregators = new HashMap<UUID, Integer>();
	}

	public void setBlocked(OENode node) {
		blocked.add(node.getUUID());
	}

	public void setUnblocked(OENode node) {
		blocked.remove(node.getUUID());
	}

	public boolean isBlocked(OENode node) {
		return blocked.contains(node.getUUID());
	}

	public void setDisabled(OENode node) {
		disabled.add(node.getUUID());
	}

	public boolean isDisabled(OENode node) {
		return disabled.contains(node.getUUID());
	}

	/**
	 * This method should be called to get a copy of the subtree of the next
	 * best node. So it is assumed that the rootNode object stems from the same
	 * search tree where cutSubTree is called on. This means in particular that
	 * this method might not work in case a node was received via the network
	 * because object identity might not be preserved after serialization and
	 * deserialization (required to transmit the node).
	 *
	 * @param rootNode The root node of the search tree to cut out
	 * @return the sub tree copy from this with rootNode as root node
	 */
	public SearchTree cutSubTreeCopy(OENode rootNode) {
		SearchTree subTreeCopy = new SearchTree(new OEHeuristicRuntime());

		if (nodes.contains(rootNode)) {
			OENode rootNodeCopy = rootNode.copyAndSetBlocked();
			subTreeCopy.setRoot(rootNodeCopy);

			for (OENode child : rootNode.getChildren()) {
				recursivelyCopyTo(child, rootNodeCopy, subTreeCopy);
			}
		}

		return subTreeCopy;
	}

	public SearchTree cutSubtreeCopyIfNotBiggerThan(OENode rootNode, int maxNodes) {
		if (isSubTreeGreaterThanX(rootNode, maxNodes)) {
			return null;
		} else {
			return cutSubTreeCopy(rootNode);
		}
	}

	protected boolean isSubTreeGreaterThanX(OENode rootNode, int x) {
		UUID uuid = UUID.randomUUID();
		tmpAggregators.put(uuid, new Integer(0));

		countNodesToMax(rootNode, uuid, x);

		boolean isGreaterThanX = tmpAggregators.get(uuid) > x;
		tmpAggregators.remove(uuid);

		return isGreaterThanX;
	}

	/**
	 * This method should be called recursively and stop when the current count
	 * (acc) will be greater than max. So it does not really count all but just
	 * up to max + 1 nodes.
	 *
	 * @param node The current node that is considered for counting
	 * @param acc The accumulated sum
	 * @param max The max number of nodes we want to check whether this search
	 * tree is greater
	 */
	private void countNodesToMax(OENode node, UUID cntrId, int max) {
		tmpAggregators.put(cntrId, tmpAggregators.get(cntrId) + 1);

		if (tmpAggregators.get(cntrId) > max) return;

		else {
			for (OENode child : node.getChildren()) {
				countNodesToMax(child, cntrId, max);
				if (tmpAggregators.get(cntrId) > max) break;
			}
		}
	}

	private void recursivelyCopyTo(OENode node, OENode parent,
			SearchTree searchTree) {

		OENode nodeCopy = node.copyAndSetBlocked();
		searchTree.addNode(parent, nodeCopy);

		for (OENode child : node.getChildren()) {
			recursivelyCopyTo(child, nodeCopy, searchTree);
		}
	}

	public void updateAndSetUnblocked(SearchTree subTree) {
		OENode local = findLocalNode(subTree.getRoot());

		if (local == null) {
			logger.warn("updateAndSetUnblocked(...) was called with sub " +
					"tree " + subTree + " that could not be found.");
			return;
		}

		updateAndSetUnblockedRecursively(local, subTree.getRoot());
	}

	private void updateAndSetUnblockedRecursively(OENode local, OENode nonLocal) {
		updatePrepare(local);
		local.update(nonLocal);
		updateDone(local);
		setUnblocked(local);

		OENode localChild;

		for (OENode nonLocalChild : nonLocal.getChildren()) {
			localChild = findLocalNode(nonLocalChild);

			// child node does not exist in local tree --> needs to be added
			if (localChild == null) {
				localChild = new OENode(nonLocalChild.getDescription(),
						nonLocalChild.getAccuracy(), nonLocalChild.getUUID());
				localChild.setRefinementCount(nonLocalChild.getRefinementCount());
				addNode(local, localChild);
			}

			updateAndSetUnblockedRecursively(localChild, nonLocalChild);
		}
	}

	protected OENode findLocalNode(OENode other) {
		for (OENode node : nodes) {
			if (node.equals(other)) return node;
		}

		return null;
	}

	public boolean contains(OENode node) {
		for (OENode other : nodes) {
			if (node.equals(other)) return true;
		}
		return false;
	}
}
