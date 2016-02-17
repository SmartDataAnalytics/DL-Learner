package org.dllearner.distributed.amqp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.aksw.commons.util.Pair;
import org.dllearner.distributed.amqp.NodeTreeSet.NodeTreeSetIterator;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

/**
 * Tests copied from TreeSet tests and adapted to work with NodeTreeSet
 */
public class NodeTreeSetTest {

	public static class ReversedOENodeComparator extends OEHeuristicRuntime {
		private static final long serialVersionUID = 6570683860440308707L;

		public boolean equals(OENode node1, OENode node2) {
			return Double.compare(node1.getAccuracy(), node2.getAccuracy()) == 0;
		}

		@Override
		public int compare(OENode node1, OENode node2) {
			return -(Double.compare(node1.getAccuracy(), node2.getAccuracy()));
		}
	}

	NodeTreeSet nts;
	OENode nodeArray[] = new OENode[1000];
	String prefix = "http://dl-learner.org/ont/";
	int clsCounter = 1;

	@Test
	public void test_Constructor() {
		assertTrue("Did not construct correct NodeTreeSet", new NodeTreeSet().isEmpty());
	}

	@Test
	public void test_ConstructorLjava_util_Collection() {
		NodeTreeSet myNodeTreeSet = new NodeTreeSet(Arrays.asList(nodeArray));

		assertTrue("NodeTreeSet incorrect size",
				myNodeTreeSet.size() == nodeArray.length);

		for (int counter = 0; counter < nodeArray.length; counter++)
			assertTrue("NodeTreeSet does not contain correct elements", myNodeTreeSet
					.contains(nodeArray[counter]));
	}

	@Test
	public void test_ConstructorLjava_util_Comparator() {
		NodeTreeSet myNodeTreeSet = new NodeTreeSet(new ReversedOENodeComparator());

		assertTrue("Did not construct correct NodeTreeSet", myNodeTreeSet.isEmpty());

		OENode node1 = new OENode(cls(prefix + "XXX23"), 0.234);
		OENode node2 = new OENode(cls(prefix + "XXX23"), 0.567);
		OENode node3 = new OENode(cls(prefix + "XXX42"), 0.890);
		myNodeTreeSet.add(node1);
		myNodeTreeSet.add(node2);
		myNodeTreeSet.add(node3);

		assertTrue(
				"Answered incorrect first element--did not use custom comparator ",
				myNodeTreeSet.first().equals(node3));

		assertTrue(
				"Answered incorrect last element--did not use custom comparator ",
				myNodeTreeSet.last().equals(node1));
	}

	@Test
	public void test_ConstructorLjava_util_SortedSet() {
		ReversedOENodeComparator comp = new ReversedOENodeComparator();
		NodeTreeSet myNodeTreeSet = new NodeTreeSet(comp);

		for (int i = 0; i < nodeArray.length; i++)
			myNodeTreeSet.add(nodeArray[i]);

		NodeTreeSet anotherTreeSet = new NodeTreeSet(myNodeTreeSet);

		assertTrue("NodeTreeSet is not correct size",
				anotherTreeSet.size() == nodeArray.length);

		for (int counter = 0; counter < nodeArray.length; counter++)
			assertTrue("NodeTreeSet does not contain correct elements",
					anotherTreeSet.contains(nodeArray[counter]));

		assertTrue("NodeTreeSet does not answer correct comparator", anotherTreeSet
				.comparator() == comp);
		assertTrue("NodeTreeSet does not use comparator",
				anotherTreeSet.first() == nodeArray[nodeArray.length - 1]);
	}

	@Test
	public void test_addLjava_lang_Object() {
		OENode node = new OENode(cls("ClsSmalles"), 0.);
		nts.add(node);
		assertTrue("Failed to add Object", nts.contains(node));

		nts.add(nodeArray[0]);
		assertTrue("Added existing element", nts.size() == nodeArray.length + 1);
	}

	@Test
	public void test_addAllLjava_util_Collection() {
		NodeTreeSet s = new NodeTreeSet();
		s.addAll(nts);

		assertTrue("Incorrect size after add", s.size() == nts.size());

		NodeTreeSetIterator i = nts.iterator();
		while (i.hasNext())
			assertTrue("Returned incorrect set", s.contains(i.next()));
	}

	@Test
	public void test_clear() {
		nts.clear();
		assertEquals("Returned non-zero size after clear", 0, nts.size());
		assertTrue("Found element in cleared set", !nts.contains(nodeArray[0]));
	}

	@Test
	public void test_clone() {
		NodeTreeSet s = (NodeTreeSet) nts.clone();
		NodeTreeSetIterator i = nts.iterator();
		while (i.hasNext())
			assertTrue("Clone failed to copy all elements", s
					.contains(i.next()));
	}

	@Test
	public void test_comparator() {
		ReversedOENodeComparator comp = new ReversedOENodeComparator();
		NodeTreeSet myNodeTreeSet = new NodeTreeSet(comp);
		assertTrue("Answered incorrect comparator",
				myNodeTreeSet.comparator() == comp);
	}

	@Test
	public void test_containsLjava_lang_Object() {
		assertTrue("Returned false for valid Object", nts
				.contains(nodeArray[nodeArray.length / 2]));
		assertTrue("Returned true for invalid Object", !nts
				.contains(new OENode(cls("SomeCls"), .234)));
		try {
			nts.contains(new Object());
		} catch (ClassCastException e) {
			// Correct
			return;
		}
		fail("Failed to throw exception when passed invalid element");
	}

	@Test
	public void test_first() {
		assertTrue("Returned incorrect first element",
				nts.first() == nodeArray[0]);
	}

	// TODO: implement
//	public void test_headSetLjava_lang_Object() {
//		// Test for method java.util.SortedSet
//		// java.util.TreeSet.headSet(java.lang.Object)
//		Set s = nts.headSet(new Integer(100));
//		assertEquals("Returned set of incorrect size", 100, s.size());
//		for (int i = 0; i < 100; i++)
//			assertTrue("Returned incorrect set", s.contains(nodeArray[i]));
//	}

	@Test
	public void test_isEmpty() {
		assertTrue("Empty set returned false", new NodeTreeSet().isEmpty());
		assertTrue("Non-Empty returned true", !nts.isEmpty());
	}

	@Test
	public void test_iterator() {
		NodeTreeSet s = new NodeTreeSet();
		s.addAll(nts);

		Set<OENode> as = new HashSet<OENode>(Arrays.asList(nodeArray));
		NodeTreeSetIterator i = nts.iterator();

		while (i.hasNext())
			as.remove(i.next());

		assertEquals("Returned incorrect iterator", 0, as.size());
	}

	@Test
	public void test_last() {
		assertTrue("Returned incorrect last element",
				nts.last() == nodeArray[nodeArray.length - 1]);
	}

	@Test
	public void test_removeLjava_lang_Object() {
		nts.remove(nodeArray[0]);
		assertTrue("Failed to remove object", !nts.contains(nodeArray[0]));

		assertTrue("Failed to change size after remove",
				nts.size() == nodeArray.length - 1);

		try {
			nts.remove(new Object());
		} catch (ClassCastException e) {
			// Correct
			return;
		}
		fail("Failed to throw exception when past uncomparable value");
	}

	@Test
	public void test_size() {
		assertTrue("Returned incorrect size", nts.size() == nodeArray.length);
	}

	// TODO: implement
//	public void test_subSetLjava_lang_ObjectLjava_lang_Object() {
//		// Test for method java.util.SortedSet
//		// java.util.TreeSet.subSet(java.lang.Object, java.lang.Object)
//		final int startPos = nodeArray.length / 4;
//		final int endPos = 3 * nodeArray.length / 4;
//		SortedSet aSubSet = nts.subSet(nodeArray[startPos], nodeArray[endPos]);
//		assertTrue("Subset has wrong number of elements",
//				aSubSet.size() == (endPos - startPos));
//		for (int counter = startPos; counter < endPos; counter++)
//			assertTrue("Subset does not contain all the elements it should",
//					aSubSet.contains(nodeArray[counter]));
//
//		int result;
//		try {
//			nts.subSet(nodeArray[3], nodeArray[0]);
//			result = 0;
//		} catch (IllegalArgumentException e) {
//			result = 1;
//		}
//		assertEquals("end less than start should throw", 1, result);
//	}

	// TODO: implement
//	public void test_tailSetLjava_lang_Object() {
//		// Test for method java.util.SortedSet
//		// java.util.TreeSet.tailSet(java.lang.Object)
//		Set s = nts.tailSet(new Integer(900));
//		assertEquals("Returned set of incorrect size", 100, s.size());
//		for (int i = 900; i < nodeArray.length; i++)
//			assertTrue("Returned incorrect set", s.contains(nodeArray[i]));
//	}

	private OWLClassExpression nextCls() {
		return new OWLClassImpl(IRI.create(prefix + (clsCounter++)));
	}

	@Test
	public void test_iter() {
		ArrayList<Pair<Double, OENode>> nodes = new ArrayList<>();
		int maxNodes = 1000;
		Random rnd = new Random();

		NodeTreeSet ntSet = new NodeTreeSet(new ReversedOENodeComparator());
		double acc = 0.10;
		for (int i=0; i<maxNodes; i++) {
			if (rnd.nextBoolean()) {
				acc += 0.01;
			}
			OENode node = new OENode(nextCls(), acc);
			nodes.add(new Pair<Double, OENode>(acc, node));

			ntSet.add(node);
		}

		int cmpCntr = 0;
		for (OENode n : ntSet) {
			assertTrue(n.equals(nodes.get(cmpCntr).getValue()));
			assertEquals((Double) n.getAccuracy(), nodes.get(cmpCntr).getKey());
			cmpCntr++;
		}

		NodeTreeSetIterator it = ntSet.iterator();
		for (int i=0; i<nodes.size(); i++) {
			OENode n = it.next();
			assertTrue(nodes.get(i).getValue().equals(n));
			assertTrue(nodes.get(i).getKey().equals(n.getAccuracy()));
		}
	}

	@Before
	public void setUp() {
		nts = new NodeTreeSet();

		double accuracyStep = 1. / nodeArray.length;
		double accuracy = 0;

		for (int i = 0; i < nodeArray.length; i++) {
			accuracy += accuracyStep;
			OENode n = new OENode(cls("Cls" + i), accuracy);
			nodeArray[i] = n;
			nts.add(n);
		}
	}

	private OWLClass cls(String localPart) {
		return new OWLClassImpl(IRI.create(prefix + localPart));
	}
}
