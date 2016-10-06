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

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGeneratorSimple;

import java.util.Random;

/**
 * A test class to analyze the performance of the application of different LGG
 * implementations in dependence on the size and complexity of the input trees.
 * 
 * @author Lorenz Buehmann
 *
 */
public class LGGPerformance {
	
//	@Test
	public void testPerformance() {
		LGGGenerator lggGen = new LGGGeneratorSimple();
		
		RDFResourceTree child1Common = new RDFResourceTree(NodeFactory.createURI("ab"));
		RDFResourceTree child2Common = new RDFResourceTree(NodeFactory.createURI("aabb"));
		RDFResourceTree child3Common = new RDFResourceTree(NodeFactory.createURI("aaabbb"));
		Node edge1Common = NodeFactory.createURI("p");
		Node edge2Common = NodeFactory.createURI("q");
		Node edge3Common = NodeFactory.createURI("r");
		
		Random rnd = new Random(123);
		
		int level1 = 20;
		int level2 = 30;
		int level3 = 20;
		
		String var = "a";
		RDFResourceTree tree1 = new RDFResourceTree(NodeFactory.createURI(var));
		for(int i = 0; i < level1; i++) {
			
			RDFResourceTree child1;
			if(rnd.nextBoolean()) {
				child1 = new RDFResourceTree(child1Common);
			} else {
				child1 = new RDFResourceTree(NodeFactory.createURI(var + "_" + i));
			}
			Node edge1 = edge1Common;
			tree1.addChild(child1, edge1);
			
			for (int j = 0; j < level2; j++) {
				
				RDFResourceTree child2;
				if(rnd.nextBoolean()) {
					child2 = new RDFResourceTree(child2Common);
				} else {
					child2 = new RDFResourceTree(NodeFactory.createURI(var + var + "_" + j));
				}
				Node edge2 = rnd.nextBoolean() ? edge2Common : NodeFactory.createURI("q" + j);
				child1.addChild(child2, edge2);
				
				for (int k = 0; k < level3; k++) {
					
					RDFResourceTree child3;
					if(rnd.nextBoolean()) {
						child3 = new RDFResourceTree(child3Common);
					} else {
						child3 = new RDFResourceTree(NodeFactory.createURI(var + var + "_" + k));
					}
					Node edge3 = rnd.nextBoolean() ? edge3Common : NodeFactory.createURI("r" + k);
					child2.addChild(child3, edge3);
				}
			}
		}
		
		var = "b";
		RDFResourceTree tree2 = new RDFResourceTree(NodeFactory.createURI("b"));
			for(int i = 0; i < level1; i++) {
			
			RDFResourceTree child1;
			if(rnd.nextBoolean()) {
				child1 = new RDFResourceTree(child1Common);
			} else {
				child1 = new RDFResourceTree(NodeFactory.createURI(var + "_" + i));
			}
			Node edge1 = edge1Common;
			tree2.addChild(child1, edge1);
			
			for (int j = 0; j < level2; j++) {
				
				RDFResourceTree child2;
				if(rnd.nextBoolean()) {
					child2 = new RDFResourceTree(child2Common);
				} else {
					child2 = new RDFResourceTree(NodeFactory.createURI(var + var + "_" + j));
				}
				Node edge2 = rnd.nextBoolean() ? edge2Common : NodeFactory.createURI("q" + j);
				child1.addChild(child2, edge2);
				
				for (int k = 0; k < level3; k++) {
					
					RDFResourceTree child3;
					if(rnd.nextBoolean()) {
						child3 = new RDFResourceTree(child3Common);
					} else {
						child3 = new RDFResourceTree(NodeFactory.createURI(var + var + "_" + k));
					}
					Node edge3 = rnd.nextBoolean() ? edge3Common : NodeFactory.createURI("r" + k);
					child2.addChild(child3, edge3);
				}
			}
		}
		
		long start = System.currentTimeMillis();
		RDFResourceTree lgg = lggGen.getLGG(tree1, tree2);
		long end = System.currentTimeMillis();
		System.out.println("Operation took " + (end - start) + "ms");
		
		System.out.println(lgg.getStringRepresentation());
	}

}
