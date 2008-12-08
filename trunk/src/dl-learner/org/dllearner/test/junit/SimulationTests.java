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

import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

import org.dllearner.algorithms.el.ELDescriptionNode;
import org.dllearner.algorithms.el.ELDescriptionTree;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.parser.KBParser;
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
		NamedClass a1 = new NamedClass(uri("a1"));
		ELDescriptionNode v1 = new ELDescriptionNode(tree);
		v1.extendLabel(a1);
		ObjectProperty r1 = new ObjectProperty(uri("r1"));
		ELDescriptionNode v2 = new ELDescriptionNode(v1, r1, new TreeSet<NamedClass>());
		ObjectProperty r2 = new ObjectProperty(uri("r2"));
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
     * v3: out=outSC1={v2}, inSC2=outSC2={v2,v4}
     * v4: out=outSC1={v2}, inSC2=outSC2={v2,v3}
	 */
	@Test
	public void test3() {	
		// background knowledge, concepts, roles
		ReasonerComponent rs = TestOntologies.getTestOntology(TestOntology.R1SUBR2);
		ObjectProperty r1 = new ObjectProperty(uri("r1"));
		NamedClass a1 = new NamedClass(uri("a1"));
		NamedClass a2 = new NamedClass(uri("a2"));
		ObjectProperty r2 = new ObjectProperty(uri("r2"));
		
		// iteratively building up the tree (nodeNames is used for logging/debugging)
		ELDescriptionTree tree = new ELDescriptionTree(rs);
		Map<ELDescriptionNode,String> nodeNames = new LinkedHashMap<ELDescriptionNode,String>();				
		ELDescriptionNode v1 = new ELDescriptionNode(tree);
		nodeNames.put(v1, "v1");
//		log("root node v1", tree, nodeNames);
		ELDescriptionNode v2 = new ELDescriptionNode(v1, r1);
		nodeNames.put(v2, "v2");
//		log("edge to v2 added", tree, nodeNames);
		v2.extendLabel(a1);
//		log("a1 added to v2", tree, nodeNames);
		v2.extendLabel(a2);
//		log("a2 added to v2", tree, nodeNames);
		ELDescriptionNode v3 = new ELDescriptionNode(v1, r1);
		nodeNames.put(v3, "v3");
//		log("edge to v3 added", tree, nodeNames);
		v3.extendLabel(a2);
//		log("a2 added to v3", tree, nodeNames);
		ELDescriptionNode v4 = new ELDescriptionNode(v1, r2);
		nodeNames.put(v4, "v4");
//		log("edge to v4 added", tree, nodeNames);
		v4.extendLabel(a1);
//		log("a1 added to v4", tree, nodeNames);
//		log("tree 3", tree, nodeNames);
		
		assertEmpty(v1);
		
		assertAllIn(v2, v3, v4);
		assertOutSC2(v2, v3, v4);
		assertOutSC1(v2);
		assertOut(v2);
		
		assertOut(v3,v2);
		assertOutSC1(v3,v2);
		assertSC2(v3, v2, v4);
		assertInSC1(v3);
		assertIn(v3);
		
		assertOut(v4,v2);
		assertOutSC1(v4,v2);
		assertSC2(v4, v2, v3);
		assertInSC1(v4);
		assertIn(v4);			
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
		Map<ELDescriptionNode,String> nodeNames = new LinkedHashMap<ELDescriptionNode,String>();				
		
		ObjectProperty r1 = new ObjectProperty(uri("r1"));
		ObjectProperty r2 = new ObjectProperty(uri("r2"));
		ObjectProperty r3 = new ObjectProperty(uri("r3"));
		NamedClass a1 = new NamedClass(uri("a1"));
		NamedClass a2 = new NamedClass(uri("a2"));
		NamedClass a3 = new NamedClass(uri("a3"));		

		ELDescriptionNode v1 = new ELDescriptionNode(tree);
		nodeNames.put(v1, "v1");
		ELDescriptionNode v2 = new ELDescriptionNode(v1, r1, a2, a3);
		nodeNames.put(v2, "v2");
		log("v2 added", tree, nodeNames);
		ELDescriptionNode v3 = new ELDescriptionNode(v1, r1);
		nodeNames.put(v3, "v3");
		log("v3 added", tree, nodeNames);
		ELDescriptionNode v4 = new ELDescriptionNode(v2, r1, a1);
		nodeNames.put(v4, "v4");
		log("v4 added", tree, nodeNames);
		ELDescriptionNode v5 = new ELDescriptionNode(v2, r3);
		nodeNames.put(v5, "v5");
		log("tmp 1", tree, nodeNames);
		v5.extendLabel(a1);
		log("tmp 2", tree, nodeNames);
		v5.extendLabel(a2);
		log("v5 added", tree, nodeNames);
		v2.refineEdge(1, r2);
		log("edge refined", tree, nodeNames);
		ELDescriptionNode v6 = new ELDescriptionNode(v3, r3);
		nodeNames.put(v6, "v6");
		log("v6 added", tree, nodeNames);		
		v6.extendLabel(a3);
		log("tree 4", tree, nodeNames);
		
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
	
	/**
	 *                  v_1
     *                /     \
     *               r_2    r_1
     *              /         \
     *            v_2         v_3
     *            /  |        |  \
     *          r_1 r_1      r_1 r_2
     *          /    |        |    \
     *        v_4   v_5      v_6   v_7
     *        / |   |  \      |     |
     *      r_2 r_1 r_2 r_2  r_1   r_2
     *      /   |   |    |    |     |
     *    v_8  v_9 v_10 v_11 v_12  v_13
     *    A_1  A_2  A_2 A_1  A_2   A_2 
     *    
     *    
	 * inSC1:
	 * (v_8,{v_9,..,v_13}), (v_9,{v_10,v_12,v_13}),... (Pattern wiederholt sich dann fuer die A_1 bzw A_2 Blaetter), (v_4,{v_5,v_6,v_7}),... (selbiges hier) (v_2,{v_3}), (v_3,{v_2})
	 *
	 * outSC1:
	 * (v_8,{v_11}), v_9,{v_8, v_10,...v_13}),... Pattern wiederholt sich
	 * fuer restliche Knoten gilt inSC1=outSC1
	 * 
	 * inSC2:
	 * {v_8,...,v_13}2, (v_4,{v_5, v_6, v_7}), (v_5,{v_7}), (v_6,{v_7})
	 * (v_2,{v_3})
	 * 
	 * outSC2:
	 * {v_8,...,v_13}2, (v_5,{v_4}), (v_6,{v_4}), (v_7,{v_5, v_6}), (v_3,{v_2})
	 * 
	 * Baum ist nicht minimal. 
	 */
	@Test
	public void test5() {
		ReasonerComponent rs = TestOntologies.getTestOntology(TestOntology.SIMPLE3);
		ELDescriptionTree tree = new ELDescriptionTree(rs);
		Map<ELDescriptionNode,String> nodeNames = new LinkedHashMap<ELDescriptionNode,String>();				
				
	}
	
	private void log(String message, ELDescriptionTree tree, Map<ELDescriptionNode,String> nodeNames) {
		// print underlined message
		System.out.println(message);
		for(int i=0; i<=message.length(); i++) {
			System.out.print("=");
		}
		System.out.println("\n");
		System.out.println(tree.toSimulationString(nodeNames));
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
	
	@SuppressWarnings("unused")
	private void assertSC(ELDescriptionNode node, ELDescriptionNode... nodesOut) {
		assertIn(node, nodesOut);
		assertOut(node, nodesOut);
	}
	
	@SuppressWarnings("unused")
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
	
	// we use the standard KB file prefix
	private String uri(String localname) {
		return KBParser.getInternalURI(localname);
	}
}
