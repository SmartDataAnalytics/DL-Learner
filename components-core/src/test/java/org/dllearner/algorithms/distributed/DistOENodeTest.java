package org.dllearner.algorithms.distributed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.TreeSet;
import java.util.UUID;

import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectComplementOfImpl;

public class DistOENodeTest {

	@Test
	public void testConstructor01() {
		OWLClassExpression ce1 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeClass")));
		DistOENode node1 = new DistOENode(null, ce1, 0.23);

		assertEquals(ce1, node1.getDescription());
		assertEquals(null, node1.getParent());
		assertEquals(0.23, node1.getAccuracy(), 0);
		// horizontal expansion set to | description | -1
		// here | not SomeClass | = 2 --> horizontal expansion = 1
		assertEquals(1, node1.getHorizontalExpansion());
		assertEquals(false, node1.isInUse());
		assertEquals(false, node1.isDisabled());
		assertNotNull(node1.getUUID());

		OWLClassImpl ce2 = new OWLClassImpl(IRI.create("http://example.com/AnotherClass"));
		DistOENode node2 = new DistOENode(node1, ce2, 0.42);
		assertEquals(ce2, node2.getDescription());
		assertEquals(node1, node2.getParent());
		assertEquals(0.42, node2.getAccuracy(), 0);
		assertEquals(0, node2.getHorizontalExpansion());
		assertEquals(false, node2.isInUse());
		assertEquals(false, node2.isDisabled());
		assertNotNull(node2.getUUID());
	}

	@Test
	public void testAddChildGetChildren() {
		OWLClassExpression ce1 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeClass")));
		DistOENode node1 = new DistOENode(null, ce1, 0.23);

		OWLClassImpl ce2 = new OWLClassImpl(IRI.create("http://example.com/AnotherClass"));
		DistOENode node2 = new DistOENode(null, ce2, 0.42);

		OWLClassImpl ce3 = new OWLClassImpl(IRI.create("http://example.com/YetAnotherClass"));
		DistOENode node3 = new DistOENode(null, ce3, 0.42);

		assertEquals(0, node1.getChildren().size());

		node1.addChild(node2);
		assertEquals(1, node1.getChildren().size());
		assertTrue(node1.getChildren().contains(node2));
		assertEquals(node1, node2.getParent());

		node1.addChild(node3);
		assertEquals(2, node1.getChildren().size());
		assertTrue(node1.getChildren().contains(node2));
		assertTrue(node1.getChildren().contains(node3));
		assertEquals(node1, node2.getParent());
		assertEquals(node1, node3.getParent());
	}

	@Test
	public void testGetExpression() {
		OWLClassExpression ce = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeClass")));
		DistOENode node = new DistOENode(null, ce, 0.23);

		assertEquals(ce, node.getExpression());
	}

	@Test
	public void testCopyTo01() {
		/*
		 *   A   <--.             B
		 *  / \      \            |
		 * A1 A2      `--------  B1
		 *                        |
		 *     _                 B2
		 *   _| |_
		 *   \   /
		 *    \ /
		 *     v
		 *
		 *     A
		 *   / | \
		 * A1 A2 B1'
		 *        |
		 *       B2
		 */

		// A tree
		OWLClassExpression ce1 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeClass")));
		DistOENode rootA = new DistOENode(null, ce1, 0.23);

		OWLClassImpl ce2 = new OWLClassImpl(IRI.create("http://example.com/AnotherClass"));
		DistOENode nodeA1 = new DistOENode(null, ce2, 0.42);

		OWLClassImpl ce3 = new OWLClassImpl(IRI.create("http://example.com/YetAnotherClass"));
		DistOENode nodeA2 = new DistOENode(null, ce3, 0.77);

		rootA.addChild(nodeA1);
		rootA.addChild(nodeA2);

		// B tree
		OWLClassExpression ce4 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeOtherClass")));
		DistOENode rootB = new DistOENode(null, ce4, 0.33);

		OWLClassImpl ce5 = new OWLClassImpl(IRI.create("http://example.com/SthElse"));
		DistOENode nodeB1 = new DistOENode(rootB, ce5, 0.68);
		nodeB1.setInUse(true);

		OWLClassImpl ce6 = new OWLClassImpl(IRI.create("http://example.com/ChildCls"));
		DistOENode nodeB2 = new DistOENode(nodeB1, ce6, 0.68);

		rootB.addChild(nodeB1);
		nodeB1.addChild(nodeB2);

		assertEquals(rootB, nodeB1.getParent());
		assertEquals(2, rootA.getChildren().size());

		nodeB1.copyTo(rootA);
		// now root A should have 3 child nodes...
		assertEquals(3, rootA.getChildren().size());
		// ...root A should be the parent of B1'...
		DistOENode nodeB1Prime = rootA.getChildren().get(2);
		assertEquals(rootA, nodeB1Prime.getParent());
		// ...and B1' should be a copy of B1...
		assertFalse(nodeB1.equals(nodeB1Prime));
		// ...with the same values...
		assertEquals(nodeB1.getAccuracy(), nodeB1Prime.getAccuracy(), 0);
		assertEquals(nodeB1.getDescription(), nodeB1Prime.getDescription());
		assertEquals(nodeB1.getHorizontalExpansion(),
				nodeB1Prime.getHorizontalExpansion());
		assertEquals(nodeB1.getRefinementCount(), nodeB1Prime.getRefinementCount());
		assertTrue(nodeB1.isInUse());
		// ...and the same child nodes...
		assertEquals(1, nodeB1Prime.getChildren().size());
		assertEquals(nodeB1.getChildren().get(0), nodeB1Prime.getChildren().get(0));
	}

	@Test
	public void testEquals01() {
		/*
		 * node object vs. null
		 */
		OWLClassExpression ce = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeClass")));
		DistOENode node = new DistOENode(null, ce, 0.23);
		assertFalse(node.equals(null));
	}

	@Test
	public void testEquals02() {
		/*
		 * different nodes
		 */
		OWLClassExpression ce1 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeClass")));
		DistOENode node1 = new DistOENode(null, ce1, 0.23);

		OWLClassImpl ce2 = new OWLClassImpl(IRI.create("http://example.com/AnotherClass"));
		DistOENode node2 = new DistOENode(null, ce2, 0.42);

		assertFalse(node1.equals(node2));
		assertFalse(node2.equals(node1));
	}

	@Test
	public void testEquals03() {
		/*
		 * same descriptions but different nodes
		 */
		OWLClassExpression ce1 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeClass")));
		DistOENode node1 = new DistOENode(null, ce1, 0.23);
		DistOENode node2 = new DistOENode(null, ce1, 0.23);

		assertFalse(node1.equals(node2));
		assertFalse(node2.equals(node1));
	}

	@Test
	public void testEquals04() {
		/*
		 * same nodes
		 */
		OWLClassExpression ce1 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeClass")));
		DistOENode node = new DistOENode(null, ce1, 0.23);

		assertTrue(node.equals(node));
	}

	/** Should find node based on matching UUID and class expression */
	@Test
	public void testFindCorrespondingLocalNode01() {
		/*
		 *   root
		 *  /    \
		 * N1    N2
		 *      /  \
		 *     N3  N4
		 *     |
		 *     N5  <-- node to find
		 *
		 */
		OWLClassExpression ce1 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeClass")));
		DistOENode root = new DistOENode(null, ce1, 0.23);

		OWLClassImpl ce2 = new OWLClassImpl(IRI.create("http://example.com/AnotherClass"));
		DistOENode node1 = new DistOENode(null, ce2, 0.42);

		OWLClassImpl ce3 = new OWLClassImpl(IRI.create("http://example.com/YetAnotherClass"));
		DistOENode node2 = new DistOENode(null, ce3, 0.77);

		OWLClassExpression ce4 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeOtherClass")));
		DistOENode node3 = new DistOENode(null, ce4, 0.33);

		OWLClassImpl ce5 = new OWLClassImpl(IRI.create("http://example.com/SthElse"));
		DistOENode node4 = new DistOENode(null, ce5, 0.68);

		OWLClassImpl ce6 = new OWLClassImpl(IRI.create("http://example.com/ChildCls"));
		DistOENode node5 = new DistOENode(null, ce6, 0.68);

		root.addChild(node1);
		root.addChild(node2);
		node2.addChild(node3);
		node2.addChild(node4);
		node3.addChild(node5);

		long msbs = node5.getUUID().getMostSignificantBits();
		long lsbs = node5.getUUID().getLeastSignificantBits();
		DistOENode searchNode = new DistOENode(null, ce6, .23, new UUID(msbs, lsbs));

		DistOENode foundNode = root.findCorrespondingLocalNode(searchNode);
		assertFalse(searchNode == node5);
		assertEquals(foundNode, node5);
		assertTrue(foundNode == node5);
	}

	/** Should not find node since UUID does not match */
	@Test
	public void testFindCorrespondingLocalNode02() {
		/*
		 *   root
		 *  /    \
		 * N1    N2
		 *      /  \
		 *     N3  N4
		 *     |
		 *     N5  <-- node to find
		 *
		 */
		OWLClassExpression ce1 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeClass")));
		DistOENode root = new DistOENode(null, ce1, 0.23);

		OWLClassImpl ce2 = new OWLClassImpl(IRI.create("http://example.com/AnotherClass"));
		DistOENode node1 = new DistOENode(null, ce2, 0.42);

		OWLClassImpl ce3 = new OWLClassImpl(IRI.create("http://example.com/YetAnotherClass"));
		DistOENode node2 = new DistOENode(null, ce3, 0.77);

		OWLClassExpression ce4 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeOtherClass")));
		DistOENode node3 = new DistOENode(null, ce4, 0.33);

		OWLClassImpl ce5 = new OWLClassImpl(IRI.create("http://example.com/SthElse"));
		DistOENode node4 = new DistOENode(null, ce5, 0.68);

		OWLClassImpl ce6 = new OWLClassImpl(IRI.create("http://example.com/ChildCls"));
		DistOENode node5 = new DistOENode(null, ce6, 0.68);

		root.addChild(node1);
		root.addChild(node2);
		node2.addChild(node3);
		node2.addChild(node4);
		node3.addChild(node5);

		long msbs = node5.getUUID().getMostSignificantBits();
		long lsbs = node5.getUUID().getLeastSignificantBits();
		lsbs++;  // <--- UUID modified --> should not match anymore
		DistOENode searchNode = new DistOENode(null, ce6, .23, new UUID(msbs, lsbs));

		DistOENode foundNode = root.findCorrespondingLocalNode(searchNode);
		assertFalse(searchNode == node5);
		assertNull(foundNode);
	}

	/** Should not find node since description does not match */
	@Test
	public void testFindCorrespondingLocalNode03() {
		/*
		 *   root
		 *  /    \
		 * N1    N2
		 *      /  \
		 *     N3  N4
		 *     |
		 *     N5  <-- node to find
		 *
		 */
		OWLClassExpression ce1 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeClass")));
		DistOENode root = new DistOENode(null, ce1, 0.23);

		OWLClassImpl ce2 = new OWLClassImpl(IRI.create("http://example.com/AnotherClass"));
		DistOENode node1 = new DistOENode(null, ce2, 0.42);

		OWLClassImpl ce3 = new OWLClassImpl(IRI.create("http://example.com/YetAnotherClass"));
		DistOENode node2 = new DistOENode(null, ce3, 0.77);

		OWLClassExpression ce4 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeOtherClass")));
		DistOENode node3 = new DistOENode(null, ce4, 0.33);

		OWLClassImpl ce5 = new OWLClassImpl(IRI.create("http://example.com/SthElse"));
		DistOENode node4 = new DistOENode(null, ce5, 0.68);

		OWLClassImpl ce6 = new OWLClassImpl(IRI.create("http://example.com/ChildCls"));
		DistOENode node5 = new DistOENode(null, ce6, 0.68);

		root.addChild(node1);
		root.addChild(node2);
		node2.addChild(node3);
		node2.addChild(node4);
		node3.addChild(node5);

		long msbs = node5.getUUID().getMostSignificantBits();
		long lsbs = node5.getUUID().getLeastSignificantBits();
		//                    'wrong' description ----v
		DistOENode searchNode = new DistOENode(null, ce5, .23, new UUID(msbs, lsbs));

		DistOENode foundNode = root.findCorrespondingLocalNode(searchNode);
		assertFalse(searchNode == node5);
		assertNull(foundNode);
	}

	@Test
	public void testFindCorrespondingLocalNode04() {
		/*
		 *   root
		 *  /    \
		 * N1    N2
		 *      /  \
		 *     N3  N4
		 *     | ^--.
		 *     N5    `- node to find
		 *
		 */
		OWLClassExpression ce1 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeClass")));
		DistOENode root = new DistOENode(null, ce1, 0.23);

		OWLClassImpl ce2 = new OWLClassImpl(IRI.create("http://example.com/AnotherClass"));
		DistOENode node1 = new DistOENode(null, ce2, 0.42);

		OWLClassImpl ce3 = new OWLClassImpl(IRI.create("http://example.com/YetAnotherClass"));
		DistOENode node2 = new DistOENode(null, ce3, 0.77);

		OWLClassExpression ce4 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeOtherClass")));
		DistOENode node3 = new DistOENode(null, ce4, 0.33);

		OWLClassImpl ce5 = new OWLClassImpl(IRI.create("http://example.com/SthElse"));
		DistOENode node4 = new DistOENode(null, ce5, 0.68);

		OWLClassImpl ce6 = new OWLClassImpl(IRI.create("http://example.com/ChildCls"));
		DistOENode node5 = new DistOENode(null, ce6, 0.68);

		root.addChild(node1);
		root.addChild(node2);
		node2.addChild(node3);
		node2.addChild(node4);
		node3.addChild(node5);

		long msbs = node3.getUUID().getMostSignificantBits();
		long lsbs = node3.getUUID().getLeastSignificantBits();
		DistOENode searchNode = new DistOENode(null, ce4, .23, new UUID(msbs, lsbs));

		DistOENode foundNode = root.findCorrespondingLocalNode(searchNode);
		assertFalse(searchNode == node3);
		assertEquals(foundNode, node3);
		assertTrue(foundNode == node3);
	}

	/** get descendants of root */
	@Test
	public void testGetNodeAndDescendantsAndSetUsed01() {
		/*
		 *   root
		 *  /    \
		 * N1    N2
		 *      /  \
		 *     N3  N4
		 *     |
		 *     N5
		 *
		 */
		OWLClassExpression ce1 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeClass")));
		DistOENode root = new DistOENode(null, ce1, 0.23);

		OWLClassImpl ce2 = new OWLClassImpl(IRI.create("http://example.com/AnotherClass"));
		DistOENode node1 = new DistOENode(null, ce2, 0.42);

		OWLClassImpl ce3 = new OWLClassImpl(IRI.create("http://example.com/YetAnotherClass"));
		DistOENode node2 = new DistOENode(null, ce3, 0.77);

		OWLClassExpression ce4 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeOtherClass")));
		DistOENode node3 = new DistOENode(null, ce4, 0.33);

		OWLClassImpl ce5 = new OWLClassImpl(IRI.create("http://example.com/SthElse"));
		DistOENode node4 = new DistOENode(null, ce5, 0.68);

		OWLClassImpl ce6 = new OWLClassImpl(IRI.create("http://example.com/ChildCls"));
		DistOENode node5 = new DistOENode(null, ce6, 0.68);

		root.addChild(node1);
		root.addChild(node2);
		node2.addChild(node3);
		node2.addChild(node4);
		node3.addChild(node5);

		assertFalse(root.isInUse());
		assertFalse(node1.isInUse());
		assertFalse(node2.isInUse());
		assertFalse(node3.isInUse());
		assertFalse(node4.isInUse());
		assertFalse(node5.isInUse());

		TreeSet<DistOENode> nodeAndDescendants =
				root.getNodeAndDescendantsAndSetUsed();

		assertEquals(6, nodeAndDescendants.size());
		for (DistOENode node : nodeAndDescendants) {
			assertTrue(node.isInUse());
		}

		assertTrue(root.isInUse());
		assertTrue(node1.isInUse());
		assertTrue(node2.isInUse());
		assertTrue(node3.isInUse());
		assertTrue(node4.isInUse());
		assertTrue(node5.isInUse());
	}

	/** get descendants of N3 */
	@Test
	public void testGetNodeAndDescendantsAndSetUsed02() {
		/*
		 *   root
		 *  /    \
		 * N1    N2
		 *      /  \
		 *     N3  N4
		 *     |
		 *     N5
		 *
		 */
		OWLClassExpression ce1 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeClass")));
		DistOENode root = new DistOENode(null, ce1, 0.23);

		OWLClassImpl ce2 = new OWLClassImpl(IRI.create("http://example.com/AnotherClass"));
		DistOENode node1 = new DistOENode(null, ce2, 0.42);

		OWLClassImpl ce3 = new OWLClassImpl(IRI.create("http://example.com/YetAnotherClass"));
		DistOENode node2 = new DistOENode(null, ce3, 0.77);

		OWLClassExpression ce4 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeOtherClass")));
		DistOENode node3 = new DistOENode(null, ce4, 0.33);

		OWLClassImpl ce5 = new OWLClassImpl(IRI.create("http://example.com/SthElse"));
		DistOENode node4 = new DistOENode(null, ce5, 0.68);

		OWLClassImpl ce6 = new OWLClassImpl(IRI.create("http://example.com/ChildCls"));
		DistOENode node5 = new DistOENode(null, ce6, 0.68);

		root.addChild(node1);
		root.addChild(node2);
		node2.addChild(node3);
		node2.addChild(node4);
		node3.addChild(node5);

		assertFalse(root.isInUse());
		assertFalse(node1.isInUse());
		assertFalse(node2.isInUse());
		assertFalse(node3.isInUse());
		assertFalse(node4.isInUse());
		assertFalse(node5.isInUse());

		TreeSet<DistOENode> nodeAndDescendants =
				node3.getNodeAndDescendantsAndSetUsed();

		assertEquals(2, nodeAndDescendants.size());
		for (DistOENode node : nodeAndDescendants) {
			assertTrue(node.isInUse());
		}

		assertFalse(root.isInUse());
		assertFalse(node1.isInUse());
		assertFalse(node2.isInUse());
		assertTrue(node3.isInUse());
		assertFalse(node4.isInUse());
		assertTrue(node5.isInUse());
	}

	/** get descendants of N4 */
	@Test
	public void testGetNodeAndDescendantsAndSetUsed03() {
		/*
		 *   root
		 *  /    \
		 * N1    N2
		 *      /  \
		 *     N3  N4
		 *     |
		 *     N5
		 *
		 */
		OWLClassExpression ce1 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeClass")));
		DistOENode root = new DistOENode(null, ce1, 0.23);

		OWLClassImpl ce2 = new OWLClassImpl(IRI.create("http://example.com/AnotherClass"));
		DistOENode node1 = new DistOENode(null, ce2, 0.42);

		OWLClassImpl ce3 = new OWLClassImpl(IRI.create("http://example.com/YetAnotherClass"));
		DistOENode node2 = new DistOENode(null, ce3, 0.77);

		OWLClassExpression ce4 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeOtherClass")));
		DistOENode node3 = new DistOENode(null, ce4, 0.33);

		OWLClassImpl ce5 = new OWLClassImpl(IRI.create("http://example.com/SthElse"));
		DistOENode node4 = new DistOENode(null, ce5, 0.68);

		OWLClassImpl ce6 = new OWLClassImpl(IRI.create("http://example.com/ChildCls"));
		DistOENode node5 = new DistOENode(null, ce6, 0.68);

		root.addChild(node1);
		root.addChild(node2);
		node2.addChild(node3);
		node2.addChild(node4);
		node3.addChild(node5);

		assertFalse(root.isInUse());
		assertFalse(node1.isInUse());
		assertFalse(node2.isInUse());
		assertFalse(node3.isInUse());
		assertFalse(node4.isInUse());
		assertFalse(node5.isInUse());

		TreeSet<DistOENode> nodeAndDescendants =
				node4.getNodeAndDescendantsAndSetUsed();

		assertEquals(1, nodeAndDescendants.size());
		for (DistOENode node : nodeAndDescendants) {
			assertTrue(node.isInUse());
		}

		assertFalse(root.isInUse());
		assertFalse(node1.isInUse());
		assertFalse(node2.isInUse());
		assertFalse(node3.isInUse());
		assertTrue(node4.isInUse());
		assertFalse(node5.isInUse());
	}

	/** get descendants of root */
	@Test
	public void testGetNodeAndDescendants01() {
		/*
		 *   root
		 *  /    \
		 * N1    N2
		 *      /  \
		 *     N3  N4
		 *     |
		 *     N5
		 *
		 */
		OWLClassExpression ce1 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeClass")));
		DistOENode root = new DistOENode(null, ce1, 0.23);

		OWLClassImpl ce2 = new OWLClassImpl(IRI.create("http://example.com/AnotherClass"));
		DistOENode node1 = new DistOENode(null, ce2, 0.42);

		OWLClassImpl ce3 = new OWLClassImpl(IRI.create("http://example.com/YetAnotherClass"));
		DistOENode node2 = new DistOENode(null, ce3, 0.77);

		OWLClassExpression ce4 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeOtherClass")));
		DistOENode node3 = new DistOENode(null, ce4, 0.33);

		OWLClassImpl ce5 = new OWLClassImpl(IRI.create("http://example.com/SthElse"));
		DistOENode node4 = new DistOENode(null, ce5, 0.68);

		OWLClassImpl ce6 = new OWLClassImpl(IRI.create("http://example.com/ChildCls"));
		DistOENode node5 = new DistOENode(null, ce6, 0.68);

		root.addChild(node1);
		root.addChild(node2);
		node2.addChild(node3);
		node2.addChild(node4);
		node3.addChild(node5);

		assertFalse(root.isInUse());
		assertFalse(node1.isInUse());
		assertFalse(node2.isInUse());
		assertFalse(node3.isInUse());
		assertFalse(node4.isInUse());
		assertFalse(node5.isInUse());

		TreeSet<DistOENode> nodeAndDescendants = root.getNodeAndDescendants();

		assertEquals(6, nodeAndDescendants.size());
		for (DistOENode node : nodeAndDescendants) {
			assertFalse(node.isInUse());
		}

		assertFalse(root.isInUse());
		assertFalse(node1.isInUse());
		assertFalse(node2.isInUse());
		assertFalse(node3.isInUse());
		assertFalse(node4.isInUse());
		assertFalse(node5.isInUse());
	}

	/** get descendants of N3 */
	@Test
	public void testGetNodeAndDescendants02() {
		/*
		 *   root
		 *  /    \
		 * N1    N2
		 *      /  \
		 *     N3  N4
		 *     |
		 *     N5
		 *
		 */
		OWLClassExpression ce1 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeClass")));
		DistOENode root = new DistOENode(null, ce1, 0.23);

		OWLClassImpl ce2 = new OWLClassImpl(IRI.create("http://example.com/AnotherClass"));
		DistOENode node1 = new DistOENode(null, ce2, 0.42);

		OWLClassImpl ce3 = new OWLClassImpl(IRI.create("http://example.com/YetAnotherClass"));
		DistOENode node2 = new DistOENode(null, ce3, 0.77);

		OWLClassExpression ce4 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeOtherClass")));
		DistOENode node3 = new DistOENode(null, ce4, 0.33);

		OWLClassImpl ce5 = new OWLClassImpl(IRI.create("http://example.com/SthElse"));
		DistOENode node4 = new DistOENode(null, ce5, 0.68);

		OWLClassImpl ce6 = new OWLClassImpl(IRI.create("http://example.com/ChildCls"));
		DistOENode node5 = new DistOENode(null, ce6, 0.68);

		root.addChild(node1);
		root.addChild(node2);
		node2.addChild(node3);
		node2.addChild(node4);
		node3.addChild(node5);

		assertFalse(root.isInUse());
		assertFalse(node1.isInUse());
		assertFalse(node2.isInUse());
		assertFalse(node3.isInUse());
		assertFalse(node4.isInUse());
		assertFalse(node5.isInUse());

		TreeSet<DistOENode> nodeAndDescendants = node3.getNodeAndDescendants();

		assertEquals(2, nodeAndDescendants.size());
		for (DistOENode node : nodeAndDescendants) {
			assertFalse(node.isInUse());
		}

		assertFalse(root.isInUse());
		assertFalse(node1.isInUse());
		assertFalse(node2.isInUse());
		assertFalse(node3.isInUse());
		assertFalse(node4.isInUse());
		assertFalse(node5.isInUse());
	}

	/** get descendants of N4 */
	@Test
	public void testGetNodeAndDescendants03() {
		/*
		 *   root
		 *  /    \
		 * N1    N2
		 *      /  \
		 *     N3  N4
		 *     |
		 *     N5
		 *
		 */
		OWLClassExpression ce1 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeClass")));
		DistOENode root = new DistOENode(null, ce1, 0.23);

		OWLClassImpl ce2 = new OWLClassImpl(IRI.create("http://example.com/AnotherClass"));
		DistOENode node1 = new DistOENode(null, ce2, 0.42);

		OWLClassImpl ce3 = new OWLClassImpl(IRI.create("http://example.com/YetAnotherClass"));
		DistOENode node2 = new DistOENode(null, ce3, 0.77);

		OWLClassExpression ce4 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeOtherClass")));
		DistOENode node3 = new DistOENode(null, ce4, 0.33);

		OWLClassImpl ce5 = new OWLClassImpl(IRI.create("http://example.com/SthElse"));
		DistOENode node4 = new DistOENode(null, ce5, 0.68);

		OWLClassImpl ce6 = new OWLClassImpl(IRI.create("http://example.com/ChildCls"));
		DistOENode node5 = new DistOENode(null, ce6, 0.68);

		root.addChild(node1);
		root.addChild(node2);
		node2.addChild(node3);
		node2.addChild(node4);
		node3.addChild(node5);

		assertFalse(root.isInUse());
		assertFalse(node1.isInUse());
		assertFalse(node2.isInUse());
		assertFalse(node3.isInUse());
		assertFalse(node4.isInUse());
		assertFalse(node5.isInUse());

		TreeSet<DistOENode> nodeAndDescendants = node4.getNodeAndDescendants();

		assertEquals(1, nodeAndDescendants.size());
		for (DistOENode node : nodeAndDescendants) {
			assertFalse(node.isInUse());
		}

		assertFalse(root.isInUse());
		assertFalse(node1.isInUse());
		assertFalse(node2.isInUse());
		assertFalse(node3.isInUse());
		assertFalse(node4.isInUse());
		assertFalse(node5.isInUse());
	}

	@Test
	public void testIncHorizontalExpansion() {
		OWLClassExpression ce = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeClass")));
		DistOENode node = new DistOENode(null, ce, 0.23);

		assertEquals(1, node.getHorizontalExpansion());
		node.incHorizontalExpansion();
		assertEquals(2, node.getHorizontalExpansion());
		node.incHorizontalExpansion();
		assertEquals(3, node.getHorizontalExpansion());
	}

	@Test
	public void testIsRoot() {
		OWLClassExpression ce1 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeClass")));
		DistOENode root = new DistOENode(null, ce1, 0.23);

		OWLClassImpl ce2 = new OWLClassImpl(IRI.create("http://example.com/AnotherClass"));
		DistOENode node1 = new DistOENode(null, ce2, 0.42);

		OWLClassImpl ce3 = new OWLClassImpl(IRI.create("http://example.com/YetAnotherClass"));
		DistOENode node2 = new DistOENode(null, ce3, 0.77);

		root.addChild(node1);
		node2.addChild(node2);

		assertTrue(root.isRoot());
		assertFalse(node1.isRoot());
		assertFalse(node2.isRoot());
	}

	@Test
	public void TestUpdateWithDescriptionScoreValsFrom() {
		OWLClassExpression ce1 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeClass")));
		DistOENode root = new DistOENode(null, ce1, 0.23);

		OWLClassImpl ce2 = new OWLClassImpl(IRI.create("http://example.com/AnotherClass"));
		DistOENode node1 = new DistOENode(root, ce2, 0.42);

		OWLClassImpl ce3 = new OWLClassImpl(IRI.create("http://example.com/YetAnotherClass"));
		DistOENode node2 = new DistOENode(node1, ce3, 0.77);

		root.addChild(node1);
		node1.addChild(node2);

		OWLClassExpression uce1 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/SomeUpdatedClass")));
		DistOENode updateNode1 = new DistOENode(null, uce1, 0.24);
		updateNode1.horizontalExpansion = 24;

		OWLClassImpl uce2 = new OWLClassImpl(
				IRI.create("http://example.com/AnotherUpdatedClass"));
		DistOENode updateNode2 = new DistOENode(null, uce2, 0.43);
		updateNode2.horizontalExpansion = 43;

		OWLClassImpl uce3 = new OWLClassImpl(
				IRI.create("http://example.com/YetAnotherUpdatedClass"));
		DistOENode updatedNode3 = new DistOENode(null, uce3, 0.78);
		updatedNode3.horizontalExpansion = 78;

		assertEquals(ce1, root.getDescription());
		assertEquals(null, root.getParent());
		assertEquals(0.23, root.getAccuracy(), 0);
		assertEquals(1, root.getHorizontalExpansion());

		root.updateWithDescriptionScoreValsFrom(updateNode1);
		assertEquals(ce1, root.getDescription());
		assertEquals(null, root.getParent());
		assertEquals(0.24, root.getAccuracy(), 0);
		assertEquals(24, root.getHorizontalExpansion());


		assertEquals(ce2, node1.getDescription());
		assertEquals(root, node1.getParent());
		assertEquals(0.42, node1.getAccuracy(), 0);
		assertEquals(0, node1.getHorizontalExpansion());

		node1.updateWithDescriptionScoreValsFrom(updateNode2);
		assertEquals(ce2, node1.getDescription());
		assertEquals(root, node1.getParent());
		assertEquals(0.43, node1.getAccuracy(), 0);
		assertEquals(43, node1.getHorizontalExpansion());

		assertEquals(ce3, node2.getDescription());
		assertEquals(node1, node2.getParent());
		assertEquals(0.77, node2.getAccuracy(), 0);
		assertEquals(0, node2.getHorizontalExpansion());

		node2.updateWithDescriptionScoreValsFrom(updatedNode3);
		assertEquals(ce3, node2.getDescription());
		assertEquals(node1, node2.getParent());
		assertEquals(0.78, node2.getAccuracy(), 0);
		assertEquals(78, node2.getHorizontalExpansion());
	}
}
