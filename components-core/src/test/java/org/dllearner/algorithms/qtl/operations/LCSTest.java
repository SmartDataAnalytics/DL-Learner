/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.algorithms.qtl.operations;

import static org.junit.Assert.*;

import java.util.Set;

import org.dllearner.algorithms.qtl.operations.lcs.LCS;
import org.junit.Test;

import com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;

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
		
	}

}
