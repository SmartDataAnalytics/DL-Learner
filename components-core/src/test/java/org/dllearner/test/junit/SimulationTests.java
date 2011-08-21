/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

package org.dllearner.test.junit;

import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.dllearner.algorithms.el.ELDescriptionNode;
import org.dllearner.algorithms.el.ELDescriptionTree;
import org.dllearner.core.AbstractReasonerComponent;
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
		AbstractReasonerComponent rs = TestOntologies.getTestOntology(TestOntology.EMPTY);
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
		AbstractReasonerComponent rs = TestOntologies.getTestOntology(TestOntology.EMPTY);
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
		AbstractReasonerComponent rs = TestOntologies.getTestOntology(TestOntology.R1SUBR2);
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
		AbstractReasonerComponent rs = TestOntologies.getTestOntology(TestOntology.SIMPLE2);
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
//		log("v2 added", tree, nodeNames);
		ELDescriptionNode v3 = new ELDescriptionNode(v1, r1);
		nodeNames.put(v3, "v3");
//		log("v3 added", tree, nodeNames);
		ELDescriptionNode v4 = new ELDescriptionNode(v2, r1, a1);
		nodeNames.put(v4, "v4");
//		log("v4 added", tree, nodeNames);
		ELDescriptionNode v5 = new ELDescriptionNode(v2, r3);
		nodeNames.put(v5, "v5");
//		log("tmp 1", tree, nodeNames);
		v5.extendLabel(a1);
//		log("tmp 2", tree, nodeNames);
		v5.extendLabel(a2);
//		log("v5 added", tree, nodeNames);
		v2.refineEdge(1, r2);
//		log("edge refined", tree, nodeNames);
		ELDescriptionNode v6 = new ELDescriptionNode(v3, r3);
		nodeNames.put(v6, "v6");
//		log("v6 added", tree, nodeNames);		
		v6.extendLabel(a3);
//		log("tree 4", tree, nodeNames);
		
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
     * Knowledge base: A_1\sqsubseteq A_2
                       r_1\sqsubseteq r_2   
     *    
	 * inSC1:
	 * (v_8,{v_9,..,v_13}), (v_9,{v_10,v_12,v_13}),... (Pattern wiederholt sich dann fuer die A_1 bzw A_2 Blaetter), 
	 * (v_4,{v_5,v_6,v_7}),... (selbiges hier) (v_2,{v_3}), (v_3,{v_2})
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
		AbstractReasonerComponent rs = TestOntologies.getTestOntology(TestOntology.SIMPLE3);
		ELDescriptionTree tree = new ELDescriptionTree(rs);
		Map<ELDescriptionNode,String> nodeNames = new LinkedHashMap<ELDescriptionNode,String>();				
				
		ObjectProperty r1 = new ObjectProperty(uri("r1"));
		ObjectProperty r2 = new ObjectProperty(uri("r2"));
		NamedClass a1 = new NamedClass(uri("a1"));
		NamedClass a2 = new NamedClass(uri("a2"));
		
		ELDescriptionNode v1 = new ELDescriptionNode(tree);
		nodeNames.put(v1, "v1");
		ELDescriptionNode v2 = new ELDescriptionNode(v1, r2);
		nodeNames.put(v2, "v2");
		ELDescriptionNode v3 = new ELDescriptionNode(v1, r1);
		nodeNames.put(v3, "v3");
		ELDescriptionNode v4 = new ELDescriptionNode(v2, r1);
		nodeNames.put(v4, "v4");
		ELDescriptionNode v5 = new ELDescriptionNode(v2, r1);
		nodeNames.put(v5, "v5");
		ELDescriptionNode v6 = new ELDescriptionNode(v3, r1);
		nodeNames.put(v6, "v6");
		ELDescriptionNode v7 = new ELDescriptionNode(v3, r2);
		nodeNames.put(v7, "v7");
		ELDescriptionNode v8 = new ELDescriptionNode(v4, r2, a1);
		nodeNames.put(v8, "v8");
		ELDescriptionNode v9 = new ELDescriptionNode(v4, r1, a2);
		nodeNames.put(v9, "v9");
		ELDescriptionNode v10 = new ELDescriptionNode(v5, r2, a2);
		nodeNames.put(v10, "v10");
		ELDescriptionNode v11 = new ELDescriptionNode(v5, r2, a1);
		nodeNames.put(v11, "v11");
		ELDescriptionNode v12 = new ELDescriptionNode(v6, r1, a2);
		nodeNames.put(v12, "v12");
		ELDescriptionNode v13 = new ELDescriptionNode(v7, r2, a2);
		nodeNames.put(v13, "v13");
		
//		log("tree 5", tree, nodeNames);
		
		assertTrue(!tree.isMinimal());
		
		// automatically generated asserts
		
		assertInSC1(v1);
		assertInSC2(v1);
		assertIn(v1);
		assertOutSC1(v1);
		assertOutSC2(v1);
		assertOut(v1);

		assertInSC1(v2,v3);
		assertInSC2(v2,v3);
		assertIn(v2,v3);
		assertOutSC1(v2,v3);
		assertOutSC2(v2);
		assertOut(v2);

		assertInSC1(v3,v2);
		assertInSC2(v3);
		assertIn(v3);
		assertOutSC1(v3,v2);
		assertOutSC2(v3,v2);
		assertOut(v3,v2);

		assertInSC1(v4,v6,v5,v7);
		assertInSC2(v4,v6,v5,v7);
		assertIn(v4,v6,v5,v7);
		assertOutSC1(v4,v6,v5,v7);
		assertOutSC2(v4);
		assertOut(v4);

		assertInSC1(v5,v4,v6,v7);
		assertInSC2(v5,v7);
		assertIn(v5,v7);
		assertOutSC1(v5,v4,v6,v7);
		assertOutSC2(v5,v4);
		assertOut(v5,v4);

		assertInSC1(v6,v4,v5,v7);
		assertInSC2(v6,v7);
		assertIn(v6,v7);
		assertOutSC1(v6,v4,v5,v7);
		assertOutSC2(v6,v4);
		assertOut(v6,v4);

		assertInSC1(v7,v4,v6,v5);
		assertInSC2(v7);
		assertIn(v7);
		assertOutSC1(v7,v4,v6,v5);
		assertOutSC2(v7,v4,v6,v5);
		assertOut(v7,v4,v6,v5);

		assertInSC1(v8,v10,v13,v11,v9,v12);
		assertInSC2(v8,v10,v13,v11,v9,v12);
		assertIn(v8,v10,v13,v11,v9,v12);
		assertOutSC1(v8,v11);
		assertOutSC2(v8,v10,v13,v11,v9,v12);
		assertOut(v8,v11);

		assertInSC1(v9,v10,v13,v12);
		assertInSC2(v9,v10,v13,v11,v12,v8);
		assertIn(v9,v10,v13,v12);
		assertOutSC1(v9,v10,v13,v11,v12,v8);
		assertOutSC2(v9,v10,v13,v11,v12,v8);
		assertOut(v9,v10,v13,v11,v12,v8);

		assertInSC1(v10,v13,v9,v12);
		assertInSC2(v10,v13,v11,v9,v12,v8);
		assertIn(v10,v13,v9,v12);
		assertOutSC1(v10,v13,v11,v9,v12,v8);
		assertOutSC2(v10,v13,v11,v9,v12,v8);
		assertOut(v10,v13,v11,v9,v12,v8);

		assertInSC1(v11,v10,v13,v9,v12,v8);
		assertInSC2(v11,v10,v13,v9,v12,v8);
		assertIn(v11,v10,v13,v9,v12,v8);
		assertOutSC1(v11,v8);
		assertOutSC2(v11,v10,v13,v9,v12,v8);
		assertOut(v11,v8);

		assertInSC1(v12,v10,v13,v9);
		assertInSC2(v12,v10,v13,v11,v9,v8);
		assertIn(v12,v10,v13,v9);
		assertOutSC1(v12,v10,v13,v11,v9,v8);
		assertOutSC2(v12,v10,v13,v11,v9,v8);
		assertOut(v12,v10,v13,v11,v9,v8);

		assertInSC1(v13,v10,v9,v12);
		assertInSC2(v13,v10,v11,v9,v8,v12);
		assertIn(v13,v10,v9,v12);
		assertOutSC1(v13,v10,v11,v9,v8,v12);
		assertOutSC2(v13,v10,v11,v9,v8,v12);
		assertOut(v13,v10,v11,v9,v8,v12);
		
//		logAsserts(tree, nodeNames);
	}
	
	/**
	 *                -------v_22-------
     *               /         |        \
     *              r_1       r_1       r_1
     *             /           |          \
     *           v_19         v_20         v_21
     *           /  \         /  \         /  \
     *         r_2 r_2     r_2  r_2      r_2  r_2
     *        /      |      |     |        |    |
     *       v_13   v_14   v_15  v_16     v_17  v_18__
     *      /  \    /\      /\     / \     /  |    |  \
     *    r_3 r_4 r_3 r_5 r_3 r_5 r_4 r_5 r_4 r_5 r_3 r_4
     *     |   |   |   |   |   |   |   |   |   |   |    |
     *    v_1 v_2 v_3 v_4 v_5 v_6 v_7 v_8 v_9 v_10 v_11 v_12
     *
	 * SC1=inSC1=outSC1={v_1,..,v_12}2 U {v_13,..,v_18}2 U {v_19,v_20,v_21}2
	 *
	 * SC2={v_1,..,v_12}2 U {(v_13, v_18), (v_14,v_15), (v_16,v_17)} U
	 * {(v_18, v_13), (v_15,v_14), (v_17,v_16)}
	 * 
	 * S={v_1,..,v_12}2 
	 */
	@Test
	public void test6() {
		AbstractReasonerComponent rs = TestOntologies.getTestOntology(TestOntology.FIVE_ROLES);
		ELDescriptionTree tree = new ELDescriptionTree(rs);
		Map<ELDescriptionNode,String> nodeNames = new LinkedHashMap<ELDescriptionNode,String>();				
		
		ObjectProperty r1 = new ObjectProperty(uri("r1"));
		ObjectProperty r2 = new ObjectProperty(uri("r2"));
		ObjectProperty r3 = new ObjectProperty(uri("r3"));
		ObjectProperty r4 = new ObjectProperty(uri("r4"));
		ObjectProperty r5 = new ObjectProperty(uri("r5"));
	
		ELDescriptionNode v22 = new ELDescriptionNode(tree);
		nodeNames.put(v22, "v22");
		ELDescriptionNode v21 = new ELDescriptionNode(v22, r1);
		nodeNames.put(v21, "v21");
		ELDescriptionNode v20 = new ELDescriptionNode(v22, r1);
		nodeNames.put(v20, "v20");
		ELDescriptionNode v19 = new ELDescriptionNode(v22, r1);
		nodeNames.put(v19, "v19");
		ELDescriptionNode v18 = new ELDescriptionNode(v21, r2);
		nodeNames.put(v18, "v18");
		ELDescriptionNode v17 = new ELDescriptionNode(v21, r2);
		nodeNames.put(v17, "v17");
		ELDescriptionNode v16 = new ELDescriptionNode(v20, r2);
		nodeNames.put(v16, "v16");
		ELDescriptionNode v15 = new ELDescriptionNode(v20, r2);
		nodeNames.put(v15, "v15");
		ELDescriptionNode v14 = new ELDescriptionNode(v19, r2);
		nodeNames.put(v14, "v14");
		ELDescriptionNode v13 = new ELDescriptionNode(v19, r2);
		nodeNames.put(v13, "v13");
		ELDescriptionNode v12 = new ELDescriptionNode(v18, r4);
		nodeNames.put(v12, "v12");
		ELDescriptionNode v11 = new ELDescriptionNode(v18, r3);
		nodeNames.put(v11, "v11");
		ELDescriptionNode v10 = new ELDescriptionNode(v17, r5);
		nodeNames.put(v10, "v10");
		ELDescriptionNode v9 = new ELDescriptionNode(v17, r4);
		nodeNames.put(v9, "v9");
		ELDescriptionNode v8 = new ELDescriptionNode(v16, r5);
		nodeNames.put(v8, "v8");
		ELDescriptionNode v7 = new ELDescriptionNode(v16, r4);
		nodeNames.put(v7, "v7");
		ELDescriptionNode v6 = new ELDescriptionNode(v15, r5);
		nodeNames.put(v6, "v6");
		ELDescriptionNode v5 = new ELDescriptionNode(v15, r3);
		nodeNames.put(v5, "v5");
		ELDescriptionNode v4 = new ELDescriptionNode(v14, r5);
		nodeNames.put(v4, "v4");
		ELDescriptionNode v3 = new ELDescriptionNode(v14, r3);
		nodeNames.put(v3, "v3");
		ELDescriptionNode v2 = new ELDescriptionNode(v13, r4);
		nodeNames.put(v2, "v2");
		ELDescriptionNode v1 = new ELDescriptionNode(v13, r3);
		nodeNames.put(v1, "v1");

//		log("tree 6", tree, nodeNames);
//		logAsserts(tree, nodeNames);
		
		assertTrue(tree.isMinimal());
		
		// automatically added asserts		
		assertInSC1(v22);
		assertInSC2(v22);
		assertIn(v22);
		assertOutSC1(v22);
		assertOutSC2(v22);
		assertOut(v22);

		assertInSC1(v21,v20,v19);
		assertInSC2(v21);
		assertIn(v21);
		assertOutSC1(v21,v20,v19);
		assertOutSC2(v21);
		assertOut(v21);

		assertInSC1(v20,v21,v19);
		assertInSC2(v20);
		assertIn(v20);
		assertOutSC1(v20,v21,v19);
		assertOutSC2(v20);
		assertOut(v20);

		assertInSC1(v19,v20,v21);
		assertInSC2(v19);
		assertIn(v19);
		assertOutSC1(v19,v20,v21);
		assertOutSC2(v19);
		assertOut(v19);

		assertInSC1(v18,v14,v16,v15,v17,v13);
		assertInSC2(v18,v13);
		assertIn(v18,v13);
		assertOutSC1(v18,v14,v16,v15,v17,v13);
		assertOutSC2(v18,v13);
		assertOut(v18,v13);

		assertInSC1(v17,v14,v16,v18,v15,v13);
		assertInSC2(v17,v16);
		assertIn(v17,v16);
		assertOutSC1(v17,v14,v16,v18,v15,v13);
		assertOutSC2(v17,v16);
		assertOut(v17,v16);

		assertInSC1(v16,v14,v18,v15,v17,v13);
		assertInSC2(v16,v17);
		assertIn(v16,v17);
		assertOutSC1(v16,v14,v18,v15,v17,v13);
		assertOutSC2(v16,v17);
		assertOut(v16,v17);

		assertInSC1(v15,v14,v16,v18,v17,v13);
		assertInSC2(v15,v14);
		assertIn(v15,v14);
		assertOutSC1(v15,v14,v16,v18,v17,v13);
		assertOutSC2(v15,v14);
		assertOut(v15,v14);

		assertInSC1(v14,v16,v18,v15,v17,v13);
		assertInSC2(v14,v15);
		assertIn(v14,v15);
		assertOutSC1(v14,v16,v18,v15,v17,v13);
		assertOutSC2(v14,v15);
		assertOut(v14,v15);

		assertInSC1(v13,v14,v16,v18,v15,v17);
		assertInSC2(v13,v18);
		assertIn(v13,v18);
		assertOutSC1(v13,v14,v16,v18,v15,v17);
		assertOutSC2(v13,v18);
		assertOut(v13,v18);

		assertInSC1(v12,v3,v8,v5,v11,v4,v9,v1,v2,v7,v6,v10);
		assertInSC2(v12,v3,v8,v5,v11,v4,v9,v1,v2,v7,v6,v10);
		assertIn(v12,v3,v8,v5,v11,v4,v9,v1,v2,v7,v6,v10);
		assertOutSC1(v12,v3,v8,v5,v11,v4,v9,v1,v2,v7,v6,v10);
		assertOutSC2(v12,v3,v8,v5,v11,v4,v9,v1,v2,v7,v6,v10);
		assertOut(v12,v3,v8,v5,v11,v4,v9,v1,v2,v7,v6,v10);

		assertInSC1(v11,v3,v8,v5,v4,v9,v1,v2,v7,v6,v10,v12);
		assertInSC2(v11,v3,v8,v5,v4,v9,v1,v2,v7,v6,v10,v12);
		assertIn(v11,v3,v8,v5,v4,v9,v1,v2,v7,v6,v10,v12);
		assertOutSC1(v11,v3,v8,v5,v4,v9,v1,v2,v7,v6,v10,v12);
		assertOutSC2(v11,v3,v8,v5,v4,v9,v1,v2,v7,v6,v10,v12);
		assertOut(v11,v3,v8,v5,v4,v9,v1,v2,v7,v6,v10,v12);

		assertInSC1(v10,v3,v8,v5,v11,v4,v9,v1,v2,v7,v6,v12);
		assertInSC2(v10,v3,v8,v5,v11,v4,v9,v1,v2,v7,v6,v12);
		assertIn(v10,v3,v8,v5,v11,v4,v9,v1,v2,v7,v6,v12);
		assertOutSC1(v10,v3,v8,v5,v11,v4,v9,v1,v2,v7,v6,v12);
		assertOutSC2(v10,v3,v8,v5,v11,v4,v9,v1,v2,v7,v6,v12);
		assertOut(v10,v3,v8,v5,v11,v4,v9,v1,v2,v7,v6,v12);

		assertInSC1(v9,v3,v8,v5,v11,v4,v1,v2,v7,v6,v10,v12);
		assertInSC2(v9,v3,v8,v5,v11,v4,v1,v2,v7,v6,v10,v12);
		assertIn(v9,v3,v8,v5,v11,v4,v1,v2,v7,v6,v10,v12);
		assertOutSC1(v9,v3,v8,v5,v11,v4,v1,v2,v7,v6,v10,v12);
		assertOutSC2(v9,v3,v8,v5,v11,v4,v1,v2,v7,v6,v10,v12);
		assertOut(v9,v3,v8,v5,v11,v4,v1,v2,v7,v6,v10,v12);

		assertInSC1(v8,v3,v5,v11,v4,v9,v1,v2,v7,v6,v10,v12);
		assertInSC2(v8,v3,v5,v11,v4,v9,v1,v2,v7,v6,v10,v12);
		assertIn(v8,v3,v5,v11,v4,v9,v1,v2,v7,v6,v10,v12);
		assertOutSC1(v8,v3,v5,v11,v4,v9,v1,v2,v7,v6,v10,v12);
		assertOutSC2(v8,v3,v5,v11,v4,v9,v1,v2,v7,v6,v10,v12);
		assertOut(v8,v3,v5,v11,v4,v9,v1,v2,v7,v6,v10,v12);

		assertInSC1(v7,v3,v8,v5,v11,v4,v9,v1,v2,v6,v10,v12);
		assertInSC2(v7,v3,v8,v5,v11,v4,v9,v1,v2,v6,v10,v12);
		assertIn(v7,v3,v8,v5,v11,v4,v9,v1,v2,v6,v10,v12);
		assertOutSC1(v7,v3,v8,v5,v11,v4,v9,v1,v2,v6,v10,v12);
		assertOutSC2(v7,v3,v8,v5,v11,v4,v9,v1,v2,v6,v10,v12);
		assertOut(v7,v3,v8,v5,v11,v4,v9,v1,v2,v6,v10,v12);

		assertInSC1(v6,v3,v8,v5,v11,v4,v9,v1,v2,v7,v10,v12);
		assertInSC2(v6,v3,v8,v5,v11,v4,v9,v1,v2,v7,v10,v12);
		assertIn(v6,v3,v8,v5,v11,v4,v9,v1,v2,v7,v10,v12);
		assertOutSC1(v6,v3,v8,v5,v11,v4,v9,v1,v2,v7,v10,v12);
		assertOutSC2(v6,v3,v8,v5,v11,v4,v9,v1,v2,v7,v10,v12);
		assertOut(v6,v3,v8,v5,v11,v4,v9,v1,v2,v7,v10,v12);

		assertInSC1(v5,v3,v8,v11,v4,v9,v1,v2,v7,v10,v6,v12);
		assertInSC2(v5,v3,v8,v11,v4,v9,v1,v2,v7,v10,v6,v12);
		assertIn(v5,v3,v8,v11,v4,v9,v1,v2,v7,v10,v6,v12);
		assertOutSC1(v5,v3,v8,v11,v4,v9,v1,v2,v7,v10,v6,v12);
		assertOutSC2(v5,v3,v8,v11,v4,v9,v1,v2,v7,v10,v6,v12);
		assertOut(v5,v3,v8,v11,v4,v9,v1,v2,v7,v10,v6,v12);

		assertInSC1(v4,v3,v8,v11,v5,v9,v1,v2,v7,v10,v6,v12);
		assertInSC2(v4,v3,v8,v11,v5,v9,v1,v2,v7,v10,v6,v12);
		assertIn(v4,v3,v8,v11,v5,v9,v1,v2,v7,v10,v6,v12);
		assertOutSC1(v4,v3,v8,v11,v5,v9,v1,v2,v7,v10,v6,v12);
		assertOutSC2(v4,v3,v8,v11,v5,v9,v1,v2,v7,v10,v6,v12);
		assertOut(v4,v3,v8,v11,v5,v9,v1,v2,v7,v10,v6,v12);

		assertInSC1(v3,v8,v11,v5,v4,v9,v1,v2,v7,v10,v6,v12);
		assertInSC2(v3,v8,v11,v5,v4,v9,v1,v2,v7,v10,v6,v12);
		assertIn(v3,v8,v11,v5,v4,v9,v1,v2,v7,v10,v6,v12);
		assertOutSC1(v3,v8,v11,v5,v4,v9,v1,v2,v7,v10,v6,v12);
		assertOutSC2(v3,v8,v11,v5,v4,v9,v1,v2,v7,v10,v6,v12);
		assertOut(v3,v8,v11,v5,v4,v9,v1,v2,v7,v10,v6,v12);

		assertInSC1(v2,v8,v3,v11,v5,v4,v9,v1,v7,v10,v6,v12);
		assertInSC2(v2,v8,v3,v11,v5,v4,v9,v1,v7,v10,v6,v12);
		assertIn(v2,v8,v3,v11,v5,v4,v9,v1,v7,v10,v6,v12);
		assertOutSC1(v2,v8,v3,v11,v5,v4,v9,v1,v7,v10,v6,v12);
		assertOutSC2(v2,v8,v3,v11,v5,v4,v9,v1,v7,v10,v6,v12);
		assertOut(v2,v8,v3,v11,v5,v4,v9,v1,v7,v10,v6,v12);

		assertInSC1(v1,v8,v3,v11,v5,v4,v9,v2,v7,v10,v6,v12);
		assertInSC2(v1,v8,v3,v11,v5,v4,v9,v2,v7,v10,v6,v12);
		assertIn(v1,v8,v3,v11,v5,v4,v9,v2,v7,v10,v6,v12);
		assertOutSC1(v1,v8,v3,v11,v5,v4,v9,v2,v7,v10,v6,v12);
		assertOutSC2(v1,v8,v3,v11,v5,v4,v9,v2,v7,v10,v6,v12);
		assertOut(v1,v8,v3,v11,v5,v4,v9,v2,v7,v10,v6,v12);

		// adding an edge leads to a non-minimal tree (it collapses)
		new ELDescriptionNode(v13, r5);
		assertTrue(!tree.isMinimal());
		
	}
	
	@Test
	public void test7() {
		AbstractReasonerComponent rs = TestOntologies.getTestOntology(TestOntology.SIMPLE);
		ELDescriptionTree tree = new ELDescriptionTree(rs);
		
		ObjectProperty has = new ObjectProperty(uri("has"));
		ObjectProperty hasChild = new ObjectProperty(uri("hasChild"));
		NamedClass human = new NamedClass(uri("human"));
		NamedClass animal = new NamedClass(uri("animal"));		
		
		ELDescriptionNode v1 = new ELDescriptionNode(tree, human);
		new ELDescriptionNode(v1, has, animal);
		new ELDescriptionNode(v1, hasChild);
		
//		System.out.println(tree.toSimulationString());
		
		assertTrue(tree.isMinimal());
	}
	
	// display a simulation as debug log
	@SuppressWarnings("unused")
	private void log(String message, ELDescriptionTree tree, Map<ELDescriptionNode,String> nodeNames) {
		// print underlined message
		System.out.println(message);
		for(int i=0; i<=message.length(); i++) {
			System.out.print("=");
		}
		System.out.println("\n");
		System.out.println(tree.toSimulationString(nodeNames));
	}
	
	// display Java code for assertions, i.e. the method generates the assertion code
	// (under the assumption that the current algorithm output is correct, which needs
	// to be verified of course)
	@SuppressWarnings("unused")
	private void logAsserts(ELDescriptionTree tree, Map<ELDescriptionNode,String> nodeNames) {
		String str = "";
		for(Entry<ELDescriptionNode,String> entry : nodeNames.entrySet()) {
			String nodeName = entry.getValue();
			ELDescriptionNode node = entry.getKey();
			str += "assertInSC1" + getAssertString(node, nodeName, node.getInSC1(), nodeNames);
			str += "assertInSC2" + getAssertString(node, nodeName, node.getInSC2(), nodeNames);
			str += "assertIn" + getAssertString(node, nodeName, node.getIn(), nodeNames);
			str += "assertOutSC1" + getAssertString(node, nodeName, node.getOutSC1(), nodeNames);
			str += "assertOutSC2" + getAssertString(node, nodeName, node.getOutSC2(), nodeNames);
			str += "assertOut" + getAssertString(node, nodeName, node.getOut(), nodeNames);
			str += "\n";
		}		
		System.out.println(str);
	}
	
	// convenience method 
	private String getAssertString(ELDescriptionNode node, String nodeName, Set<ELDescriptionNode> nodes, Map<ELDescriptionNode,String> nodeNames) {
		if(nodes.isEmpty()) {
			return "(" + nodeName+");\n";
		} else {
			return "(" + nodeName+","+ELDescriptionNode.toString(nodes, nodeNames) + ");\n";
		}
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
