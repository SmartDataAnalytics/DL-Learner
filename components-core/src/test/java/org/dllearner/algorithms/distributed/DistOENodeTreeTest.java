package org.dllearner.algorithms.distributed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectComplementOfImpl;

public class DistOENodeTreeTest {

	@Test
	public void testConstructor01() {
		OWLClass owlThing = OWLManager.getOWLDataFactory().getOWLThing();
		DistOENodeTree tree = new DistOENodeTree();

		assertNull(tree.root);
		assertEquals(0, tree.size());
	}

	@Test
	public void testConstructor02() {
		DistOENode root = new DistOENode(
				null,
				new OWLClassImpl(IRI.create("http://example.com/SomeCls")),
				0);
		DistOEHeuristicRuntime heuristic = new DistOEHeuristicRuntime();

		DistOENodeTree tree = new DistOENodeTree(root, heuristic);

		assertEquals(root, tree.root);
		assertEquals(1, tree.size());
	}

	@Test
	public void testConstructor03() {
		DistOENode root = new DistOENode(
				null,
				new OWLClassImpl(IRI.create("http://example.com/SomeCls")),
				0);

		DistOENodeTree tree = new DistOENodeTree(root);

		assertEquals(root, tree.root);
		assertEquals(1, tree.size());
	}

	/**
	 * result should be null if tree does not contain a node similar to the
	 * searched one
	 */
	@Test
	public void testAddGetNode01() {
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

		DistOENodeTree tree = new DistOENodeTree(root);
		assertEquals(1, tree.size());

		tree.add(node1, root);
		assertEquals(2, tree.size());

		tree.add(node2, root);
		assertEquals(3, tree.size());

		// there should be not duplicates, so calling add again should have no
		// effect
		tree.add(node2, root);
		assertEquals(3, tree.size());

		tree.add(node3, node2);
		assertEquals(4, tree.size());

		tree.add(node4, node2);
		assertEquals(5, tree.size());

		tree.add(node5, node3);
		assertEquals(6, tree.size());

		DistOENode searchNode1 = new DistOENode(null, ce1, 0.22);
		DistOENode foundNode = tree.getCorrespondingLocalNode(searchNode1);
		assertNull(foundNode);
	}

	/**
	 * find root
	 */
	@Test
	public void testAddGetNode02() {
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

		DistOENodeTree tree = new DistOENodeTree(root);
		assertEquals(1, tree.size());

		tree.add(node1, root);
		assertEquals(2, tree.size());

		tree.add(node2, root);
		assertEquals(3, tree.size());

		// there should be not duplicates, so calling add again should have no
		// effect
		tree.add(node2, root);
		assertEquals(3, tree.size());

		tree.add(node3, node2);
		assertEquals(4, tree.size());

		tree.add(node4, node2);
		assertEquals(5, tree.size());

		tree.add(node5, node3);
		assertEquals(6, tree.size());

		long msbs = root.uuid.getMostSignificantBits();
		long lsbs = root.uuid.getLeastSignificantBits();
		DistOENode searchNode = new DistOENode(null, ce1, 0.22, new UUID(msbs, lsbs));
		DistOENode foundNode = tree.getCorrespondingLocalNode(searchNode);

		assertFalse(searchNode == root);
		assertEquals(foundNode, root);
		assertTrue(foundNode == root);
	}

	/**
	 * find N2
	 */
	@Test
	public void testAddGetNode03() {
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

		DistOENodeTree tree = new DistOENodeTree(root);
		assertEquals(1, tree.size());

		tree.add(node1, root);
		assertEquals(2, tree.size());

		tree.add(node2, root);
		assertEquals(3, tree.size());

		// there should be not duplicates, so calling add again should have no
		// effect
		tree.add(node2, root);
		assertEquals(3, tree.size());

		tree.add(node3, node2);
		assertEquals(4, tree.size());

		tree.add(node4, node2);
		assertEquals(5, tree.size());

		tree.add(node5, node3);
		assertEquals(6, tree.size());

		long msbs = node2.uuid.getMostSignificantBits();
		long lsbs = node2.uuid.getLeastSignificantBits();
		DistOENode searchNode = new DistOENode(null, ce3, 0.22, new UUID(msbs, lsbs));
		DistOENode foundNode = tree.getCorrespondingLocalNode(searchNode);

		assertFalse(searchNode == node2);
		assertEquals(foundNode, node2);
		assertTrue(node2 == foundNode);
	}

	/**
	 * find N5
	 */
	@Test
	public void testAddGetNode04() {
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

		DistOENodeTree tree = new DistOENodeTree(root);
		assertEquals(1, tree.size());

		tree.add(node1, root);
		assertEquals(2, tree.size());

		tree.add(node2, root);
		assertEquals(3, tree.size());

		// there should be not duplicates, so calling add again should have no
		// effect
		tree.add(node2, root);
		assertEquals(3, tree.size());

		tree.add(node3, node2);
		assertEquals(4, tree.size());

		tree.add(node4, node2);
		assertEquals(5, tree.size());

		tree.add(node5, node3);
		assertEquals(6, tree.size());

		long msbs = node5.uuid.getMostSignificantBits();
		long lsbs = node5.uuid.getLeastSignificantBits();
		DistOENode searchNode = new DistOENode(null, ce6, 0.22, new UUID(msbs, lsbs));
		DistOENode foundNode = tree.getCorrespondingLocalNode(searchNode);

		assertFalse(searchNode == node5);
		assertEquals(node5, foundNode);
		assertTrue(node5 == foundNode);
	}

	/** get whole tree as subtree */
	@Test
	public void testGetSubTreeAndSetUsed01() {
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

		DistOENodeTree tree = new DistOENodeTree(root);
		tree.add(node1, root);
		tree.add(node2, root);
		tree.add(node3, node2);
		tree.add(node4, node2);
		tree.add(node5, node3);

		DistOENodeTree subTree = tree.getSubTreeAndSetUsed(root);

		assertFalse(subTree == tree);
		assertEquals(6, subTree.size());

		assertTrue(subTree.contains(root));
		assertTrue(root.isInUse());

		assertTrue(subTree.contains(node1));
		assertTrue(node1.isInUse());

		assertTrue(subTree.contains(node2));
		assertTrue(node2.isInUse());

		assertTrue(subTree.contains(node3));
		assertTrue(node3.isInUse());

		assertTrue(subTree.contains(node4));
		assertTrue(node4.isInUse());

		assertTrue(subTree.contains(node5));
		assertTrue(node5.isInUse());
	}

	@Test
	public void testGetSubTreeAndSetUsed02() {
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

		DistOENodeTree tree = new DistOENodeTree(root);
		tree.add(node1, root);
		tree.add(node2, root);
		tree.add(node3, node2);
		tree.add(node4, node2);
		tree.add(node5, node3);

		DistOENodeTree subTree = tree.getSubTreeAndSetUsed(node2);

		assertFalse(subTree == tree);
		assertEquals(4, subTree.size());

		assertFalse(root.isInUse());
		assertFalse(node1.isInUse());

		assertTrue(subTree.contains(node2));
		assertTrue(node2.isInUse());

		assertTrue(subTree.contains(node3));
		assertTrue(node3.isInUse());

		assertTrue(subTree.contains(node4));
		assertTrue(node4.isInUse());

		assertTrue(subTree.contains(node5));
		assertTrue(node5.isInUse());
	}

	/** get leaf as subtree*/
	@Test
	public void testGetSubTreeAndSetUsed03() {
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

		DistOENodeTree tree = new DistOENodeTree(root);
		tree.add(node1, root);
		tree.add(node2, root);
		tree.add(node3, node2);
		tree.add(node4, node2);
		tree.add(node5, node3);

		DistOENodeTree subTree = tree.getSubTreeAndSetUsed(node4);

		assertFalse(subTree == tree);
		assertEquals(1, subTree.size());

		assertFalse(root.isInUse());
		assertFalse(node1.isInUse());
		assertFalse(node2.isInUse());
		assertFalse(node3.isInUse());

		assertTrue(subTree.contains(node4));
		assertTrue(node4.isInUse());

		assertFalse(node5.isInUse());
	}

	/** while tree as subtree */
	@Test
	public void testGetSubTree01() {
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

		DistOENodeTree tree = new DistOENodeTree(root);
		tree.add(node1, root);
		tree.add(node2, root);
		tree.add(node3, node2);
		tree.add(node4, node2);
		tree.add(node5, node3);

		DistOENodeTree subTree = tree.getSubTree(root);

		assertFalse(subTree == tree);
		assertEquals(6, subTree.size());

		assertTrue(subTree.contains(root));
		assertFalse(root.isInUse());

		assertTrue(subTree.contains(node1));
		assertFalse(node1.isInUse());

		assertTrue(subTree.contains(node2));
		assertFalse(node2.isInUse());

		assertTrue(subTree.contains(node3));
		assertFalse(node3.isInUse());

		assertTrue(subTree.contains(node4));
		assertFalse(node4.isInUse());

		assertTrue(subTree.contains(node5));
		assertFalse(node5.isInUse());
	}

	@Test
	public void testGetSubTree02() {
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

		DistOENodeTree tree = new DistOENodeTree(root);
		tree.add(node1, root);
		tree.add(node2, root);
		tree.add(node3, node2);
		tree.add(node4, node2);
		tree.add(node5, node3);

		DistOENodeTree subTree = tree.getSubTree(node2);

		assertFalse(subTree == tree);
		assertEquals(4, subTree.size());

		assertFalse(root.isInUse());
		assertFalse(node1.isInUse());

		assertTrue(subTree.contains(node2));
		assertFalse(node2.isInUse());

		assertTrue(subTree.contains(node3));
		assertFalse(node3.isInUse());

		assertTrue(subTree.contains(node4));
		assertFalse(node4.isInUse());

		assertTrue(subTree.contains(node5));
		assertFalse(node5.isInUse());
	}

	/** leaf as subtree */
	@Test
	public void testGetSubTree03() {
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

		DistOENodeTree tree = new DistOENodeTree(root);
		tree.add(node1, root);
		tree.add(node2, root);
		tree.add(node3, node2);
		tree.add(node4, node2);
		tree.add(node5, node3);

		DistOENodeTree subTree = tree.getSubTree(node4);

		assertFalse(subTree == tree);
		assertEquals(1, subTree.size());

		assertFalse(root.isInUse());
		assertFalse(node1.isInUse());
		assertFalse(node2.isInUse());
		assertFalse(node3.isInUse());

		assertTrue(subTree.contains(node4));
		assertFalse(node4.isInUse());

		assertFalse(node5.isInUse());
	}

	@Test
	public void testMergeWithAndUnblock01() {
		/*
		 *   root  .--- merge -.
		 *  /    \ v
		 * N1    N2            N2'
		 *      /  \         /  |  \
		 *     N3  N4      N3'  N4' N6
		 *     |           |    |
		 *     N5          N5'  N7
		 *                      |
		 *                      N8
		 */


		// target tree
		OWLClassExpression ce1 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/RootCls")));
		DistOENode root = new DistOENode(null, ce1, 0.23);

		OWLClassImpl ce2 = new OWLClassImpl(IRI.create("http://example.com/Class1"));
		DistOENode node1 = new DistOENode(null, ce2, 0.42);

		OWLClassImpl ce3 = new OWLClassImpl(IRI.create("http://example.com/Class2"));
		DistOENode node2 = new DistOENode(null, ce3, 0.77);
		node2.setInUse(true);

		OWLClassExpression ce4 = new OWLObjectComplementOfImpl(
				new OWLClassImpl(IRI.create("http://example.com/Class3")));
		DistOENode node3 = new DistOENode(null, ce4, 0.33);
		node3.setInUse(true);

		OWLClassImpl ce5 = new OWLClassImpl(IRI.create("http://example.com/Class4"));
		DistOENode node4 = new DistOENode(null, ce5, 0.68);
		node4.setInUse(true);

		OWLClassImpl ce6 = new OWLClassImpl(IRI.create("http://example.com/Class5"));
		DistOENode node5 = new DistOENode(null, ce6, 0.68);
		node5.setInUse(true);

		DistOENodeTree targetTree = new DistOENodeTree(root);
		targetTree.add(node1, root);
		targetTree.add(node2, root);
		targetTree.add(node3, node2);
		targetTree.add(node4, node2);
		targetTree.add(node5, node3);

		// tree to merge
		UUID node2uuid = new UUID(
				node2.getUUID().getMostSignificantBits(),
				node2.getUUID().getLeastSignificantBits());
		DistOENode node2u = new DistOENode(null, ce3, 0.77, node2uuid);
		node2u.setInUse(true);

		UUID node3uuid = new UUID(
				node3.getUUID().getMostSignificantBits(),
				node3.getUUID().getLeastSignificantBits());
		DistOENode node3u = new DistOENode(null, ce4, 0.34, node3uuid);
		node3u.setInUse(true);

		UUID node4uuid = new UUID(
				node4.getUUID().getMostSignificantBits(),
				node4.getUUID().getLeastSignificantBits());
		DistOENode node4u = new DistOENode(null, ce5, 0.68, node4uuid);
		node4u.setInUse(true);

		UUID node5uuid = new UUID(
				node5.getUUID().getMostSignificantBits(),
				node5.getUUID().getLeastSignificantBits());
		DistOENode node5u = new DistOENode(null, ce6, 0.68, node5uuid);
		node5u.setInUse(true);

		OWLClassExpression ce7 = new OWLClassImpl(
				IRI.create("http://example.com/Class6"));
		DistOENode node6 = new DistOENode(null,  ce7, 0.71);
		node6.setInUse(true);

		OWLClassExpression ce8 = new OWLClassImpl(
				IRI.create("http://example.com/Class7"));
		DistOENode node7 = new DistOENode(null,  ce8, 0.72);
		node7.setInUse(true);

		OWLClassExpression ce9 = new OWLClassImpl(
				IRI.create("http://example.com/Class8"));
		DistOENode node8 = new DistOENode(null,  ce9, 0.73);
		node8.setInUse(true);

		DistOENodeTree mergeTree = new DistOENodeTree(node2u);
		mergeTree.add(node3u, node2u);
		mergeTree.add(node4u, node2u);
		mergeTree.add(node5u, node3u);
		mergeTree.add(node6, node2u);
		mergeTree.add(node7, node4u);
		mergeTree.add(node8, node7);

		assertEquals(6, targetTree.size());
		targetTree.mergeWithAndUnblock(mergeTree);
		// root, node1 - node5 remain in target tree...
		assertEquals(9, targetTree.size());
		assertTrue(targetTree.contains(root));
		assertTrue(targetTree.contains(node1));
		assertTrue(targetTree.contains(node2));
		assertTrue(targetTree.contains(node3));
		assertTrue(targetTree.contains(node4));
		assertTrue(targetTree.contains(node5));
		// ...and are all unblocked
		assertFalse(root.isInUse());
		assertFalse(root.isDisabled());
		assertFalse(node1.isInUse());
		assertFalse(node1.isDisabled());
		assertFalse(node2.isInUse());
		assertFalse(node2.isDisabled());
		assertFalse(node3.isInUse());
		assertFalse(node3.isDisabled());
		assertFalse(node4.isInUse());
		assertFalse(node4.isDisabled());
		assertFalse(node5.isInUse());
		assertFalse(node5.isDisabled());

		// for node6 - node8 there are new nodes in the target tree with
		// same attributes...
		DistOENode corrNode6 = targetTree.getCorrespondingLocalNode(node6);
		assertEquals(node6.getAccuracy(), corrNode6.getAccuracy(), 0);
		assertEquals(node6.getDescription(), corrNode6.getDescription());
		assertEquals(node6.getHorizontalExpansion(), corrNode6.getHorizontalExpansion());
		assertEquals(node6.getRefinementCount(), corrNode6.getRefinementCount());
		assertEquals(node6.getUUID(), corrNode6.getUUID());

		DistOENode corrNode7 = targetTree.getCorrespondingLocalNode(node7);
		assertEquals(node7.getAccuracy(), corrNode7.getAccuracy(), 0);
		assertEquals(node7.getDescription(), corrNode7.getDescription());
		assertEquals(node7.getHorizontalExpansion(), corrNode7.getHorizontalExpansion());
		assertEquals(node7.getRefinementCount(), corrNode7.getRefinementCount());
		assertEquals(node7.getUUID(), corrNode7.getUUID());

		DistOENode corrNode8 = targetTree.getCorrespondingLocalNode(node8);
		assertEquals(node8.getAccuracy(), corrNode8.getAccuracy(), 0);
		assertEquals(node8.getDescription(), corrNode8.getDescription());
		assertEquals(node8.getHorizontalExpansion(), corrNode8.getHorizontalExpansion());
		assertEquals(node8.getRefinementCount(), corrNode8.getRefinementCount());
		assertEquals(node8.getUUID(), corrNode8.getUUID());

		// ...which should also be not blocked
		assertFalse(corrNode6.isInUse());
		assertFalse(corrNode6.isDisabled());
		assertFalse(corrNode7.isInUse());
		assertFalse(corrNode7.isDisabled());
		assertFalse(corrNode8.isInUse());
		assertFalse(corrNode8.isDisabled());
	}

	@Test
	public void testReset() {
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

		DistOENodeTree tree = new DistOENodeTree(root);
		tree.add(node1, root);
		tree.add(node2, root);
		tree.add(node3, node2);
		tree.add(node4, node2);
		tree.add(node5, node3);

		assertEquals(6, tree.size());
		tree.reset();
		assertEquals(0, tree.size());
		assertNull(tree.getRoot());

	}
}
