package org.dllearner.distributed.amqp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Random;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.lang.NotImplementedException;

public class NodeTreeSet extends TreeSet<OENode> implements Iterable<OENode> {

	private static final long serialVersionUID = 25182147062840258L;

	private HashMap<UUID, OENode> uuid2Node;
	private HashMap<Double, List<UUID>> score2UUID;
	private ArrayList<OENode> nodes;
	private SortedSet<Double> scores;
	private OEHeuristicRuntime comparator;
	private boolean useRandomIterator = false;

	public NodeTreeSet() {
		this(new OEHeuristicRuntime());
	}

	public NodeTreeSet(OEHeuristicRuntime comparator) {
		this.comparator = comparator;
		uuid2Node = new HashMap<UUID, OENode>();
		score2UUID = new HashMap<Double, List<UUID>>();

		scores = new TreeSet<Double>(new DoubleComparator());
		nodes = new ArrayList<OENode>();
	}

	public NodeTreeSet(NodeTreeSet c) {
		this(c.comparator);
		addAll(c);
	}

	public NodeTreeSet(Collection<OENode> c) {
		this();
		addAll(c);
	}

	@Override
	public NodeTreeSetIterator iterator() {
		if (useRandomIterator)
			return new NodeTreeSetRandIterator();
		else
			return new NodeTreeSetIterator();
	}

	@Override
	public NodeTreeSetIterator descendingIterator() {
		return iterator();
	}

	@Override
	public NavigableSet<OENode> descendingSet() {
		return this;
	}

	@Override
	public int size() {
		return nodes.size();
	}

	@Override
	public boolean isEmpty() {
		return nodes.isEmpty();
	}

	@Override
	public boolean contains(Object object) {
		OENode node = (OENode) object;

		return uuid2Node.containsKey(node.getUUID());
	}

	/** mainly for debugging purposes */
	protected void dump() {
		System.out.println("--- start dump ---");
		System.out.println("keys: ");
		for (UUID k : uuid2Node.keySet()) System.out.println("\t" + k);
		System.out.println("nodes: ");
		for (OENode n : nodes) System.out.println("\t" + n + " (" + n.getUUID() + ")");
		System.out.println("--- end dump ---");
	}

	@Override
	public boolean add(OENode node) {
		// do nothing if node is already in the set
		if (uuid2Node.containsKey(node.getUUID())) return false;

		// update uid2Node
		uuid2Node.put(node.getUUID(), node);

		// update score2UUID
		double score = comparator.getNodeScore(node);

		if (!score2UUID.containsKey(score))
			score2UUID.put(score, new ArrayList<UUID>());

		score2UUID.get(score).add(node.getUUID());

		// update nodes
		nodes.add(node);

		// update scores
		scores.add(score);

		return true;
	}

	@Override
	public boolean remove(Object o) {
		OENode node = (OENode) o;

		// do nothing if node is not in the set
		if (!uuid2Node.containsKey(node.getUUID())) return false;

		boolean success = true;
		UUID uuid = node.getUUID();

		// update uid2Node
		uuid2Node.remove(uuid);

		// update score2UUID
		double score = comparator.getNodeScore(node);

		if (!score2UUID.containsKey(score)) {
			/* this should only happen if the score changes based on e.g. the
			 * parent's or children's score. */

			Double formerScore = null;
			// find score entry for UUID
			for (Double s : score2UUID.keySet()) {
				List<UUID> uuids = score2UUID.get(s);

				for (UUID u : uuids) {
					if (node.getUUID().equals(u)) {
						formerScore = s;
						uuids.remove(u);
						break;
					}
				}
			}

			if (formerScore == null) success = false;

			else {
				// remove entry for the found score
				if (score2UUID.get(formerScore).isEmpty()) {
					score2UUID.remove(formerScore);
					scores.remove(formerScore);
				}
			}

		} else {
			List<UUID> uuids = score2UUID.get(score);
			uuids.remove(uuid);

			if (uuids.isEmpty()) {
				score2UUID.remove(score);
				scores.remove(score);
			}
		}

		// update nodes
		nodes.remove(node);

		return success;
	}

	@Override
	public void clear() {
		uuid2Node = new HashMap<UUID, OENode>();
		score2UUID = new HashMap<Double, List<UUID>>();
		nodes = new ArrayList<OENode>();
		scores = new TreeSet<Double>();
	}

	@Override
	public  boolean addAll(Collection<? extends OENode> c) {
		boolean success = true;

		for (OENode e : c) {
			success = success && add(e);
		}
		return success;
	}

	@Override
	public NavigableSet<OENode> subSet(OENode fromElement, boolean fromInclusive,
			OENode toElement, boolean toInclusive) {
		throw new NotImplementedException();
	}

	@Override
	public NavigableSet<OENode> headSet(OENode toElement, boolean inclusive) {
		throw new NotImplementedException();
	}

	@Override
	public NavigableSet<OENode> tailSet(OENode fromElement, boolean inclusive) {
		throw new NotImplementedException();
	}

	@Override
	public SortedSet<OENode> subSet(OENode fromElement, OENode toElement) {
		throw new NotImplementedException();
	}

	@Override
	public SortedSet<OENode> headSet(OENode toElement) {
		throw new NotImplementedException();
	}

	@Override
	public SortedSet<OENode> tailSet(OENode fromElement) {
		throw new NotImplementedException();
	}

	@Override
	public Comparator<? super OENode> comparator() { return comparator; }

	@Override
	public OENode first() {
		OENode first = nodes.get(0);

		for (OENode n : nodes) {
			//                     __n_<_first__
			if (comparator.compare(n, first) < 0) first = n;
		}
		return first;
	}

	@Override
	public OENode last() {
		OENode last = nodes.get(0);

		for (OENode n : nodes) {
			//                     __last_<_n__
			if (comparator.compare(last, n) < 0) last = n;
		}
		return last;
	}

	@Override
	public OENode lower(OENode e) { throw new NotImplementedException(); }

	@Override
	public OENode floor(OENode e) { throw new NotImplementedException(); }

	@Override
	public OENode ceiling(OENode e) { throw new NotImplementedException(); }

	@Override
	public OENode higher(OENode e) { throw new NotImplementedException(); }

	@Override
	public OENode pollFirst() { throw new NotImplementedException(); }

	@Override
	public OENode pollLast() { throw new NotImplementedException(); }

	@Override
	public Object clone() {
		NodeTreeSet clone;
		clone = (NodeTreeSet) super.clone();

		return clone;
	}

	@Override
	public Spliterator<OENode> spliterator() {
		throw new NotImplementedException();
	}

	public OENode getNode(UUID uuid) {
		return uuid2Node.get(uuid);
	}

	public void setUseRandomIterator(boolean useRandomIterator) {
		this.useRandomIterator = useRandomIterator;
	}

	// ------------------------------------------------------------------------

	class NodeTreeSetRandIterator extends NodeTreeSetIterator implements Iterator<OENode> {

		private List<Integer> unseenIdxs;
		private Random rnd;

		public NodeTreeSetRandIterator() {
			unseenIdxs = new ArrayList<Integer>();

			for (int i=0; i<nodes.size(); i++) { unseenIdxs.add(i); }

			rnd = new Random();
		}

		@Override
		public boolean hasNext() {
			return !unseenIdxs.isEmpty();
		}

		@Override
		public OENode next() {
			int i = rnd.nextInt(unseenIdxs.size());
			Integer nodeIdx = unseenIdxs.remove(i);

			return nodes.get(nodeIdx);
		}

	}

	class NodeTreeSetIterator implements Iterator<OENode> {
		/** points to the *current* index position, so the one that corresponds
		 * to the UUID that might have been returned already. Thus it has to be
		 * incremented to get the next UUID. */
		private int uuidIdx;
		private Double currScore;
		private Iterator<Double> scoresIt;

		public NodeTreeSetIterator() {
			uuidIdx = -1;
			scoresIt = scores.iterator();
			currScore = null;
		}

		@Override
		public boolean hasNext() {
			boolean isLastUUID = (currScore == null);

			return !isLastUUID || scoresIt.hasNext();
		}

		@Override
		public OENode next() {
			if (currScore == null) currScore = scoresIt.next();

			List<UUID> uuids = score2UUID.get(currScore);
			UUID uuid = uuids.get(++uuidIdx);

			if (uuidIdx == (uuids.size()-1)) {
				// then we just got the last UUID value in the list

				// reset uuid index and current score
				uuidIdx = -1;
				currScore = null;
			}

			return uuid2Node.get(uuid);
		}
	}

	public OENode get(UUID uuid) {
		return uuid2Node.get(uuid);
	}

	// -----------------------------------------------------------------------

	class DoubleComparator implements Comparator<Double>, Serializable {
		private static final long serialVersionUID = -6420995635720229101L;

		@Override
		public int compare(Double o1, Double o2) {
			return -Double.compare(o1, o2);
		}

	}
}

