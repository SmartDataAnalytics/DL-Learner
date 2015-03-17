/**
 * 
 */
package org.dllearner.algorithms.qtl.operations;

import static org.junit.Assert.*;

import java.util.Set;

import org.dllearner.algorithms.qtl.operations.lcs.LCS;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;

/**
 * @author Lorenz Buehmann
 *
 */
public class LCSTest {
	
	@Test
	public void testRDFConnectedness() {
		Node r = NodeFactory.createURI("r");
		Node s = NodeFactory.createURI("s");
		Node p = NodeFactory.createURI("p");
		Node q = NodeFactory.createURI("q");
		Node t = NodeFactory.createURI("t");
		
		
		/*       p--q--t
		 *      /
		 *     /
		 * r--p--s
		 */
		Set<Triple> triples = Sets.newHashSet(
				Triple.create(r, p, s),
				Triple.create(p, q, t)
				);
				
		
		// r->s = TRUE
		assertTrue(LCS.isRDFConnected(r, s, triples));
		// s->r = FALSE
		assertFalse(LCS.isRDFConnected(s, r, triples));
		// r->p -> TRUE
		assertTrue(LCS.isRDFConnected(r, p, triples));
		// r->t -> TRUE
		assertTrue(LCS.isRDFConnected(r, t, triples));
	}

	/**
	 * Test method for {@link org.dllearner.algorithms.qtl.operations.lcs.LCS#computeLCS(org.dllearner.algorithms.qtl.operations.lcs.LCS.RootedRDFGraph, org.dllearner.algorithms.qtl.operations.lcs.LCS.RootedRDFGraph)}.
	 */
	@Test
	public void testComputeLCS() {
		fail("Not yet implemented");
	}

}
