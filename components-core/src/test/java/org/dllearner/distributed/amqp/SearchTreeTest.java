package org.dllearner.distributed.amqp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.dllearner.distributed.OEHeuristicRuntime;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;


public class SearchTreeTest {
	private String prefix = "http://dl-learner.org/test/";

	@Test
	public void testSetIsUnBlocked() {
		OENode node1 = new OENode(
				new OWLClassImpl(IRI.create(prefix + "Cls1")), 0.56);
		OENode node2 = new OENode(
				new OWLClassImpl(IRI.create(prefix + "Cls2")), 0.67);
		OENode node3 = new OENode(
				new OWLClassImpl(IRI.create(prefix + "Cls2")), 0.67);
		OENode node4 = new OENode(
				new OWLClassImpl(IRI.create(prefix + "Cls3")), 0.78);

		SearchTree searchTree = new SearchTree(new OEHeuristicRuntime());

		searchTree.setRoot(node1);
		searchTree.addNode(node1, node2);
		searchTree.addNode(node1, node3);
		searchTree.addNode(node3, node4);

		assertEquals(false, searchTree.isBlocked(node1));
		assertEquals(false, searchTree.isBlocked(node2));
		assertEquals(false, searchTree.isBlocked(node3));
		assertEquals(false, searchTree.isBlocked(node4));

		searchTree.setBlocked(node2);

		assertEquals(false, searchTree.isBlocked(node1));
		assertEquals(true, searchTree.isBlocked(node2));
		assertEquals(false, searchTree.isBlocked(node3));
		assertEquals(false, searchTree.isBlocked(node4));

		searchTree.setBlocked(node1);

		assertEquals(true, searchTree.isBlocked(node1));
		assertEquals(true, searchTree.isBlocked(node2));
		assertEquals(false, searchTree.isBlocked(node3));
		assertEquals(false, searchTree.isBlocked(node4));

		searchTree.setBlocked(node4);

		assertEquals(true, searchTree.isBlocked(node1));
		assertEquals(true, searchTree.isBlocked(node2));
		assertEquals(false, searchTree.isBlocked(node3));
		assertEquals(true, searchTree.isBlocked(node4));

		searchTree.setBlocked(node3);

		assertEquals(true, searchTree.isBlocked(node1));
		assertEquals(true, searchTree.isBlocked(node2));
		assertEquals(true, searchTree.isBlocked(node3));
		assertEquals(true, searchTree.isBlocked(node4));


		searchTree.setUnblocked(node2);

		assertEquals(true, searchTree.isBlocked(node1));
		assertEquals(false, searchTree.isBlocked(node2));
		assertEquals(true, searchTree.isBlocked(node3));
		assertEquals(true, searchTree.isBlocked(node4));

		searchTree.setUnblocked(node2);

		assertEquals(true, searchTree.isBlocked(node1));
		assertEquals(false, searchTree.isBlocked(node2));
		assertEquals(true, searchTree.isBlocked(node3));
		assertEquals(true, searchTree.isBlocked(node4));

		searchTree.setUnblocked(node1);

		assertEquals(false, searchTree.isBlocked(node1));
		assertEquals(false, searchTree.isBlocked(node2));
		assertEquals(true, searchTree.isBlocked(node3));
		assertEquals(true, searchTree.isBlocked(node4));

		searchTree.setUnblocked(node4);

		assertEquals(false, searchTree.isBlocked(node1));
		assertEquals(false, searchTree.isBlocked(node2));
		assertEquals(true, searchTree.isBlocked(node3));
		assertEquals(false, searchTree.isBlocked(node4));

		searchTree.setUnblocked(node3);

		assertEquals(false, searchTree.isBlocked(node1));
		assertEquals(false, searchTree.isBlocked(node2));
		assertEquals(false, searchTree.isBlocked(node3));
		assertEquals(false, searchTree.isBlocked(node4));
	}

	private OWLClassImpl newCls(String localPart) {
		return new OWLClassImpl(IRI.create(prefix + localPart));
	}

	@Test
	public void testCutSubTree() {
		/* search tree
		 *
		 *               A
		 *        .------^------.
		 *       B               C
		 *    .--^--.        .---|---.
		 *   D       E      F    G    H
		 *  / \     /|\    / \   |   / \
		 * I   J   K L M  N   O  P  Q   R
		 */

		OENode a = new OENode(newCls("A"), 0.56);
		OENode b = new OENode(newCls("B"), 0.67);
		OENode c = new OENode(newCls("C"), 0.67);
		OENode d = new OENode(newCls("D"), 0.78);
		OENode e = new OENode(newCls("E"), 0.89);
		OENode f = new OENode(newCls("F"), 0.87);
		OENode g = new OENode(newCls("G"), 0.86);
		OENode h = new OENode(newCls("H"), 0.85);
		OENode i = new OENode(newCls("I"), 0.84);
		OENode j = new OENode(newCls("J"), 0.83);
		OENode k = new OENode(newCls("K"), 0.82);
		OENode l = new OENode(newCls("L"), 0.81);
		OENode m = new OENode(newCls("M"), 0.80);
		OENode n = new OENode(newCls("N"), 0.54);
		OENode o = new OENode(newCls("O"), 0.55);
		OENode p = new OENode(newCls("P"), 0.56);
		OENode q = new OENode(newCls("Q"), 0.57);
		OENode r = new OENode(newCls("R"), 0.58);

		SearchTree tree = new SearchTree(new OEHeuristicRuntime());
		tree.setRoot(a);

		tree.addNode(a, b);
		tree.addNode(a, c);

		tree.addNode(b, d);
		tree.addNode(b, e);

		tree.addNode(d, i);
		tree.addNode(d, j);

		tree.addNode(e, k);
		tree.addNode(e, l);
		tree.addNode(e, m);

		tree.addNode(c, f);
		tree.addNode(c, g);
		tree.addNode(c, h);

		tree.addNode(f, n);
		tree.addNode(f, o);

		tree.addNode(g, p);

		tree.addNode(h, q);
		tree.addNode(h, r);


		/* cut subtree
		 *
		 *        C
		 *    .---|---.
		 *   F    G    H
		 *  / \   |   / \
		 * N   O  P  Q   R
		 */

		SearchTree subTree1 = tree.cutSubTreeCopy(c);

		assertEquals(c.getUUID(), subTree1.getRoot().getUUID());

		assertTrue(subTree1.contains(c));
		assertTrue(subTree1.contains(f));
		assertTrue(subTree1.contains(g));
		assertTrue(subTree1.contains(h));
		assertTrue(subTree1.contains(n));
		assertTrue(subTree1.contains(o));
		assertTrue(subTree1.contains(p));
		assertTrue(subTree1.contains(q));
		assertEquals(9, subTree1.size());

		assertEquals(false, subTree1.contains(a));
		assertEquals(false, subTree1.contains(b));
		assertEquals(false, subTree1.contains(d));
		assertEquals(false, subTree1.contains(e));

		/* cut subtree
		 *
		 *    E
		 *   /|\
		 *  K L M
		 */

		SearchTree subTree2 = tree.cutSubTreeCopy(e);

		assertEquals(e.getUUID(), subTree2.getRoot().getUUID());
		assertTrue(subTree2.contains(e));
		assertTrue(subTree2.contains(k));
		assertTrue(subTree2.contains(l));
		assertTrue(subTree2.contains(m));
		assertEquals(4, subTree2.size());

		assertEquals(false, subTree2.contains(a));
		assertEquals(false, subTree2.contains(b));
		assertEquals(false, subTree2.contains(f));
		assertEquals(false, subTree2.contains(p));
	}

	@Test
	public void testContains() {
		OENode node1 = new OENode(newCls("Cls1"), 0.23);
		OENode node2 = new OENode(newCls("Cls2"), 0.34);
		OENode node3 = new OENode(newCls("Cls3"), 0.45);
		OENode node4 = new OENode(newCls("Cls4"), 0.56);
		OENode node5 = new OENode(newCls("Cls5"), 0.67);
		OENode node6 = new OENode(newCls("Cls6"), 0.78);

		// similar == has same UUID
		OENode simimlarNode1 = new OENode(newCls("Cls7"), 0.23, node1.getUUID());
		OENode simimlarNode5 = new OENode(newCls("Cls8"), 0.23, node5.getUUID());

		// nodes not contained
		OENode notContainedNode2 = new OENode(newCls("Cls2"), 0.34);
		OENode notContainedNode6 = new OENode(newCls("Cls6"), 0.78);

		SearchTree tree = new SearchTree(new OEHeuristicRuntime());
		tree.setRoot(node1);
		tree.addNode(node1, node2);
		tree.addNode(node1, node3);

		tree.addNode(node2, node4);

		tree.addNode(node3, node5);
		tree.addNode(node3, node6);

		assertTrue(tree.contains(node1));
		assertTrue(tree.contains(node2));
		assertTrue(tree.contains(node3));
		assertTrue(tree.contains(node4));
		assertTrue(tree.contains(node5));
		assertTrue(tree.contains(node6));

		assertTrue(tree.contains(simimlarNode1));
		assertTrue(tree.contains(simimlarNode5));

		assertEquals(false, tree.contains(notContainedNode2));
		assertEquals(false, tree.contains(notContainedNode6));
	}

	@Test
	public void testFindLocalNode() {
		OENode node1 = new OENode(newCls("Cls1"), 0.23);
		OENode node2 = new OENode(newCls("Cls2"), 0.34);
		OENode node3 = new OENode(newCls("Cls3"), 0.45);
		OENode node4 = new OENode(newCls("Cls4"), 0.56);
		OENode node5 = new OENode(newCls("Cls5"), 0.67);
		OENode node6 = new OENode(newCls("Cls6"), 0.78);

		// similar == has same UUID
		OENode simimlarNode1 = new OENode(newCls("Cls7"), 0.23, node1.getUUID());
		OENode simimlarNode5 = new OENode(newCls("Cls5"), 0.78, node5.getUUID());

		// should not match
		OENode nodeNoMatch2 = new OENode(newCls("Cls2"), 0.34);
		OENode nodeNoMatch3 = new OENode(newCls("Cls3"), 0.45);


		SearchTree tree = new SearchTree(new OEHeuristicRuntime());
		tree.setRoot(node1);
		tree.addNode(node1, node2);
		tree.addNode(node1, node3);

		tree.addNode(node2, node4);

		tree.addNode(node3, node5);
		tree.addNode(node3, node6);

		assertEquals(node1, tree.findLocalNode(simimlarNode1));
		assertEquals(node5, tree.findLocalNode(simimlarNode5));

		assertEquals(null, tree.findLocalNode(nodeNoMatch2));
		assertEquals(null, tree.findLocalNode(nodeNoMatch3));
	}

	@Test
	public void testUpdateAndSetUnblocked() {
		/* search tree
		 *
		 *               A
		 *        .------^------.
		 *       B               C*
		 *    .--^--.        .---|---.
		 *   D       E      F*   G*   H*
		 *  / \     /|\    / \   |   / \
		 * I   J*  K L M  N*  O* P* Q*  R*
		 *
		 * * --> blocked nodes
		 */

		OENode a = new OENode(newCls("A"), 0.56); a.setRefinementCount(23);
		OENode b = new OENode(newCls("B"), 0.67); b.setRefinementCount(24);
		OENode c = new OENode(newCls("C"), 0.67); c.setRefinementCount(25);
		OENode d = new OENode(newCls("D"), 0.78); d.setRefinementCount(26);
		OENode e = new OENode(newCls("E"), 0.89); e.setRefinementCount(27);
		OENode f = new OENode(newCls("F"), 0.87); f.setRefinementCount(28);
		OENode g = new OENode(newCls("G"), 0.86); g.setRefinementCount(29);
		OENode h = new OENode(newCls("H"), 0.85); h.setRefinementCount(30);
		OENode i = new OENode(newCls("I"), 0.84); i.setRefinementCount(31);
		OENode j = new OENode(newCls("J"), 0.83); j.setRefinementCount(32);
		OENode k = new OENode(newCls("K"), 0.82); k.setRefinementCount(33);
		OENode l = new OENode(newCls("L"), 0.81); l.setRefinementCount(34);
		OENode m = new OENode(newCls("M"), 0.80); m.setRefinementCount(35);
		OENode n = new OENode(newCls("N"), 0.54); n.setRefinementCount(36);
		OENode o = new OENode(newCls("O"), 0.55); o.setRefinementCount(37);
		OENode p = new OENode(newCls("P"), 0.56); p.setRefinementCount(38);
		OENode q = new OENode(newCls("Q"), 0.57); q.setRefinementCount(39);
		OENode r = new OENode(newCls("R"), 0.58); r.setRefinementCount(40);

		SearchTree tree = new SearchTree(new OEHeuristicRuntime());
		tree.setRoot(a);

		tree.addNode(a, b); tree.addNode(b, d); tree.addNode(d, i);
		                                        tree.addNode(d, j);

		                    tree.addNode(b, e); tree.addNode(e, k);
		                                        tree.addNode(e, l);
		                                        tree.addNode(e, m);

		tree.addNode(a, c); tree.addNode(c, f); tree.addNode(f, n);
		                                        tree.addNode(f, o);

		                    tree.addNode(c, g); tree.addNode(g, p);

		                    tree.addNode(c, h); tree.addNode(h, q);
		                                        tree.addNode(h, r);

		tree.setBlocked(c);
		tree.setBlocked(f);
		tree.setBlocked(g);
		tree.setBlocked(h);
		tree.setBlocked(j);
		tree.setBlocked(n);
		tree.setBlocked(o);
		tree.setBlocked(p);
		tree.setBlocked(q);
		tree.setBlocked(r);

		SearchTree updateTree1 = new SearchTree(new OEHeuristicRuntime());

		OENode c_ = new OENode(newCls("C"), 0.67, c.getUUID());
		c_.setRefinementCount(42);
		OENode f_ = new OENode(newCls("F"), 0.87, f.getUUID());
		f_.setRefinementCount(43);
		OENode g_ = new OENode(newCls("G"), 0.86, g.getUUID());
		g_.setRefinementCount(44);
		OENode h_ = new OENode(newCls("H"), 0.85, h.getUUID());
		h_.setRefinementCount(45);
		OENode n_ = new OENode(newCls("N"), 0.54, n.getUUID());
		n_.setRefinementCount(47);
		OENode o_ = new OENode(newCls("O"), 0.55, o.getUUID());
		o_.setRefinementCount(48);
		OENode p_ = new OENode(newCls("P"), 0.56, p.getUUID());
		p_.setRefinementCount(49);
		OENode q_ = new OENode(newCls("Q"), 0.57, q.getUUID());
		q_.setRefinementCount(50);
		OENode r_ = new OENode(newCls("R"), 0.58, r.getUUID());
		r_.setRefinementCount(51);
		OENode s_ = new OENode(newCls("S"), 0.59);
		s_.setRefinementCount(52);

		updateTree1.setRoot(c_);

		updateTree1.addNode(c_, f_); updateTree1.addNode(f_, n_);
		                             updateTree1.addNode(f_, o_);

		updateTree1.addNode(c_, g_); updateTree1.addNode(g_, p_);

		updateTree1.addNode(c_, h_); updateTree1.addNode(h_, q_);
		                             updateTree1.addNode(h_, r_); updateTree1.addNode(r_, s_);

		assertEquals(18, tree.size());
		assertTrue(tree.isBlocked(c));
		assertTrue(tree.isBlocked(f));
		assertTrue(tree.isBlocked(g));
		assertTrue(tree.isBlocked(h));
		assertTrue(tree.isBlocked(n));
		assertTrue(tree.isBlocked(o));
		assertTrue(tree.isBlocked(p));
		assertTrue(tree.isBlocked(q));
		assertTrue(tree.isBlocked(r));
		tree.updateAndSetUnblocked(updateTree1);
		assertEquals(19, tree.size());
		assertEquals(c_.getRefinementCount(),c.getRefinementCount());
		assertEquals(false, tree.isBlocked(c));
		assertEquals(f_.getRefinementCount(),f.getRefinementCount());
		assertEquals(false, tree.isBlocked(f));
		assertEquals(g_.getRefinementCount(),g.getRefinementCount());
		assertEquals(false, tree.isBlocked(g));
		assertEquals(h_.getRefinementCount(),h.getRefinementCount());
		assertEquals(false, tree.isBlocked(h));
		assertEquals(n_.getRefinementCount(),n.getRefinementCount());
		assertEquals(false, tree.isBlocked(n));
		assertEquals(o_.getRefinementCount(),o.getRefinementCount());
		assertEquals(false, tree.isBlocked(o));
		assertEquals(p_.getRefinementCount(),p.getRefinementCount());
		assertEquals(false, tree.isBlocked(p));
		assertEquals(q_.getRefinementCount(),q.getRefinementCount());
		assertEquals(false, tree.isBlocked(q));
		assertEquals(r_.getRefinementCount(),r.getRefinementCount());
		assertEquals(false, tree.isBlocked(r));
		assertTrue(tree.contains(s_));

		SearchTree updateTree2 = new SearchTree(new OEHeuristicRuntime());
		OENode j_ = new OENode(newCls("J"), 0.83, j.getUUID());
		j_.setRefinementCount(46);

		OENode t_ = new OENode(newCls("T"), 0.60);
		t_.setRefinementCount(53);
		OENode u_ = new OENode(newCls("U"), 0.61);
		u_.setRefinementCount(54);
		OENode v_ = new OENode(newCls("V"), 0.62);
		v_.setRefinementCount(55);
		OENode w_ = new OENode(newCls("W"), 0.63);
		w_.setRefinementCount(56);

		updateTree2.setRoot(j_);
		updateTree2.addNode(j_, t_);

		updateTree2.addNode(j_, u_); updateTree2.addNode(u_, v_);
		                             updateTree2.addNode(u_, w_);

		tree.updateAndSetUnblocked(updateTree2);
		assertEquals(23, tree.size());
		assertEquals(false, tree.isBlocked(j_));
		assertEquals(j_.getRefinementCount(),j.getRefinementCount());
		assertTrue(tree.contains(t_));
		assertTrue(tree.contains(u_));
		assertTrue(tree.contains(v_));
		assertTrue(tree.contains(w_));
	}
}
