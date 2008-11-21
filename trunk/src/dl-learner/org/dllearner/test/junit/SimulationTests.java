/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
 *
 */
package org.dllearner.test.junit;

import static org.junit.Assert.*;

import java.util.TreeSet;

import org.dllearner.algorithms.el.ELDescriptionNode;
import org.dllearner.algorithms.el.ELDescriptionTree;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.test.junit.TestOntologies.TestOntology;
import org.junit.Test;

/**
 * We test whether level-restricted, non-reflexive simulations on
 * EL description trees are correctly computed.
 * 
 * @author Jens Lehmann
 *
 */
public class SimulationTests {

	/**
	 * Empty tree - empty simulation.
	 *
	 */
	@Test
	public void test1() {
		// perform test with empty background knowledge and TOP concept
		ReasonerComponent rs = TestOntologies.getTestOntology(TestOntology.EMPTY);
		ELDescriptionTree tree = new ELDescriptionTree(rs);
		ELDescriptionNode root = new ELDescriptionNode(tree);
		
		// simulation relation should be empty
		assertEmpty(root);
	}
	
	/**
	 *   v1:{A1}
	 *	   / \
	 *	 r1  r2
	 *	 /     \
	 *	v2:{} v3:{}
     *
	 *	v1: -
	 *	v2: in=inSC1=inSC2=out=outSC1=outSC2={v3}
	 *	v3: in=inSC1=inSC2=out=outSC1=outSC2={v2}
	 */
	@Test
	public void test2() {
		// perform test with empty background knowledge and A1 AND EXISTS r1.TOP AND EXISTS r2.TOP
		ReasonerComponent rs = TestOntologies.getTestOntology(TestOntology.EMPTY);
		ELDescriptionTree tree = new ELDescriptionTree(rs);
		NamedClass a1 = new NamedClass("a1");
		ELDescriptionNode v1 = new ELDescriptionNode(tree);
		v1.extendLabel(a1);
		ObjectProperty r1 = new ObjectProperty("r1");
		ELDescriptionNode v2 = new ELDescriptionNode(v1, r1, new TreeSet<NamedClass>());
		ObjectProperty r2 = new ObjectProperty("r2");
		ELDescriptionNode v3 = new ELDescriptionNode(v1, r2, new TreeSet<NamedClass>());
				
		assertEmpty(v1);
		assertAll(v2, v3);
		assertAll(v3, v2);
	}
	
	/**
	 * K: r1 \sqsubset r2
     *
     *           v1:{}
     *       /     |     \
     *     r1     r1      r2
     *     /       |        \
     * v2:{A1,A2} v3:{A2} v4:{A1}
     *
     * v1: -
     * v2: in=inSC1=inSC2=outSC2={v3,v4}
     * v3: inSC2=outSC2={v2,v4}
     * v4: inSC2=outSC2={v2,v3}
	 */
	@Test
	public void test3() {
		ReasonerComponent rs = TestOntologies.getTestOntology(TestOntology.R1SUBR2);
		ELDescriptionTree tree = new ELDescriptionTree(rs);
		ELDescriptionNode v1 = new ELDescriptionNode(tree);
		ObjectProperty r1 = new ObjectProperty("r1");
		NamedClass a1 = new NamedClass("a1");
		NamedClass a2 = new NamedClass("a2");
		ELDescriptionNode v2 = new ELDescriptionNode(v1, r1, a1, a2);
		ELDescriptionNode v3 = new ELDescriptionNode(v1, r1, a2);
		ObjectProperty r2 = new ObjectProperty("r2");
		ELDescriptionNode v4 = new ELDescriptionNode(v1, r2, a1);
		
		System.out.println("v1:\n" + v1.toSimulationString());
		System.out.println("v2:\n" + v2.toSimulationString());
		System.out.println("v3:\n" + v3.toSimulationString());	
		System.out.println("v4:\n" + v4.toSimulationString());
		
		assertEmpty(v1);
		
		assertAllIn(v2, v3, v4);
		assertOutSC2(v2, v3, v4);
		assertOutSC1(v2);
		assertOut(v2);
		
		assertSC2(v3, v2, v4);
		assertSC1(v3);
		assertSC(v3);
		
		assertSC2(v4, v2, v3);
		assertSC1(v4);
		assertSC(v4);			
	}
	
	/**
	 * K: r2 \sqsubset r3; A2 \sqsubset A3
	 * 
	 *            v1: {}
     *           /      \
     *         r1        r1
     *         /          \
     *     v2:{A2,A3}    v3:{}
     *     /      |        |
     *   r1      r2        r3
     *   /        |        |
     * v4:{A1} v5:{A1,A2} v6:{A3}
	 *
	 * v1: -
     * v2: in=inSC1=inSC2={v3}, out=outSC1=outSC2={}
     * v3: in=inSC1=inSC2={}, out=outSC1=outSC2={v2}
     * v4: out=outSC1={v5}, outSC2=inSC2={v5,v6}, in=inSC1={}
     * v5: out=outSC1={}, in=inSC1=inSC2=outSC2={v4,v6}
     * v6: out=outSC1={v5}, outSC2=inSC2={v4,v5}, in=inSC1={}
	 *
	 */
	@Test
	public void test4() {
		ReasonerComponent rs = TestOntologies.getTestOntology(TestOntology.SIMPLE2);
		ELDescriptionTree tree = new ELDescriptionTree(rs);
		ObjectProperty r1 = new ObjectProperty("r1");
		ObjectProperty r2 = new ObjectProperty("r2");
		ObjectProperty r3 = new ObjectProperty("r3");
		NamedClass a1 = new NamedClass("a1");
		NamedClass a2 = new NamedClass("a2");
		NamedClass a3 = new NamedClass("a3");		
		ELDescriptionNode v1 = new ELDescriptionNode(tree);
		ELDescriptionNode v2 = new ELDescriptionNode(v1, r1, a2, a3);
		ELDescriptionNode v3 = new ELDescriptionNode(v1, r1);
		ELDescriptionNode v4 = new ELDescriptionNode(v2, r1, a1);
		ELDescriptionNode v5 = new ELDescriptionNode(v2, r3, a1, a2);
		v2.refineEdge(1, r2);
		ELDescriptionNode v6 = new ELDescriptionNode(v3, r3, a3);
		
		assertEmpty(v1);
		
		assertAllIn(v2, v3);
		assertAllOut(v2);
		
		assertAllIn(v3);
		assertAllOut(v2);
		
		assertSC2(v4,v5,v6);
		assertInSC1(v4);
		assertIn(v4);
		assertOut(v4,v5);
		assertOutSC1(v4,v5);
		
		assertAllIn(v5,v4,v6);
		assertOutSC2(v5,v4,v6);
		assertOutSC1(v5);
		assertOut(v5);
		
		assertSC2(v6,v4,v5);
		assertInSC1(v6);
		assertIn(v6);
		assertOut(v6,v5);
		assertOutSC1(v6,v5);		
	}
	
	// all relations (in, inSC1, inSC2) should have the 
	// the specified node set as value
	private void assertAll(ELDescriptionNode node, ELDescriptionNode... nodes) {
		assertAllIn(node, nodes);
		assertAllOut(node, nodes);
	}
	
	// all in relations (in, inSC1, inSC2) should have the 
	// specified node set as value
	private void assertAllIn(ELDescriptionNode node, ELDescriptionNode... nodesIn) {
		assertIn(node, nodesIn);
		assertInSC1(node, nodesIn);
		assertInSC2(node, nodesIn);
	}
	
	// all out relations (out, outSC1, outSC2) should have the 
	// specified node set as value
	private void assertAllOut(ELDescriptionNode node, ELDescriptionNode... nodesOut) {
		assertOut(node, nodesOut);
		assertOutSC1(node, nodesOut);
		assertOutSC2(node, nodesOut);
	}
	
	private void assertSC(ELDescriptionNode node, ELDescriptionNode... nodesOut) {
		assertIn(node, nodesOut);
		assertOut(node, nodesOut);
	}
	
	private void assertSC1(ELDescriptionNode node, ELDescriptionNode... nodesOut) {
		assertInSC1(node, nodesOut);
		assertOutSC1(node, nodesOut);
	}	
	
	private void assertSC2(ELDescriptionNode node, ELDescriptionNode... nodesOut) {
		assertInSC2(node, nodesOut);
		assertOutSC2(node, nodesOut);
	}
	
	private void assertInSC1(ELDescriptionNode node, ELDescriptionNode... nodes) {
		for(ELDescriptionNode nodeTmp : nodes) {
			assertTrue(node.getInSC1().contains(nodeTmp));
		}
		assertTrue(node.getInSC1().size() == nodes.length);
	}
	
	private void assertInSC2(ELDescriptionNode node, ELDescriptionNode... nodes) {
		for(ELDescriptionNode nodeTmp : nodes) {
			assertTrue(node.getInSC2().contains(nodeTmp));
		}
		assertTrue(node.getInSC2().size() == nodes.length);
	}	
	
	private void assertIn(ELDescriptionNode node, ELDescriptionNode... nodes) {
		for(ELDescriptionNode nodeTmp : nodes) {
			assertTrue(node.getIn().contains(nodeTmp));
		}
		assertTrue(node.getIn().size() == nodes.length);
	}	
	
	private void assertOutSC2(ELDescriptionNode node, ELDescriptionNode... nodes) {
		for(ELDescriptionNode nodeTmp : nodes) {
			assertTrue(node.getOutSC2().contains(nodeTmp));
		}
		assertTrue(node.getOutSC2().size() == nodes.length);
	}		
	
	private void assertOutSC1(ELDescriptionNode node, ELDescriptionNode... nodes) {
		for(ELDescriptionNode nodeTmp : nodes) {
			assertTrue(node.getOutSC1().contains(nodeTmp));
		}
		assertTrue(node.getOutSC1().size() == nodes.length);
	}	
	
	private void assertOut(ELDescriptionNode node, ELDescriptionNode... nodes) {
		for(ELDescriptionNode nodeTmp : nodes) {
			assertTrue(node.getOut().contains(nodeTmp));
		}
		assertTrue(node.getOut().size() == nodes.length);
	}		
	
	// all simulation relations should be empty for this node
	private void assertEmpty(ELDescriptionNode node) {
		assertTrue(node.getIn().isEmpty());
		assertTrue(node.getInSC1().isEmpty());
		assertTrue(node.getInSC2().isEmpty());
		assertTrue(node.getOut().isEmpty());
		assertTrue(node.getOutSC1().isEmpty());
		assertTrue(node.getOutSC2().isEmpty());		
	}
}
