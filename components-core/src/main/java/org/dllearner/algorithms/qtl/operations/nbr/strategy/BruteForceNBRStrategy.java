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
package org.dllearner.algorithms.qtl.operations.nbr.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;

import org.apache.jena.graph.Node;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class BruteForceNBRStrategy implements NBRStrategy {
	
	private static final Logger logger = Logger.getLogger(BruteForceNBRStrategy.class);

	@Override
	public RDFResourceTree computeNBR(RDFResourceTree posExampleTree, List<RDFResourceTree> negExampleTrees) {
		logger.info("Making NBR on");
		logger.info(posExampleTree.getStringRepresentation());
		logger.info("with negative examples");
		for(RDFResourceTree tree : negExampleTrees){
			logger.info(tree.getStringRepresentation());
		}
		
		RDFResourceTree nbr = new RDFResourceTree(posExampleTree);
		if(subsumesTrees(posExampleTree, negExampleTrees)){
			logger.info("Warning: Positive example already covers all negative examples. Skipping NBR computation...");
			return nbr;
		}
		
		Set<RDFResourceTree> tested = new HashSet<>();
		Node edge;
		RDFResourceTree parent;
		while(!(tested.size() == nbr.getLeafs().size()) ){
			for(RDFResourceTree leaf : nbr.getLeafs()){
				if(leaf.isRoot()){
					return nbr;
				}
				parent = leaf.getParent();
				edge = parent.getEdgeToChild(leaf);
				parent.removeChild(leaf);
				boolean isSubsumedBy = false;
				for(RDFResourceTree negTree : negExampleTrees){
					isSubsumedBy = QueryTreeUtils.isSubsumedBy(negTree, nbr);
					if(isSubsumedBy){
						break;
					}
				}
				if(isSubsumedBy){
					tested.add(leaf);
					parent.addChild(leaf, edge);
				}
				
			}
		}
		return nbr;
	}

	@Override
	public List<RDFResourceTree> computeNBRs(RDFResourceTree posExampleTree,
			List<RDFResourceTree> negExampleTrees) {
		logger.info("Making NBR on");
		logger.info(posExampleTree.getStringRepresentation());
		logger.info("with negative examples");
		for(RDFResourceTree tree : negExampleTrees){
			logger.info(tree.getStringRepresentation());
		}
		
		if(subsumesTrees(posExampleTree, negExampleTrees)){
			logger.info("Warning: Positive example already covers all negative examples. Skipping NBR computation...");
			return Collections.singletonList(posExampleTree);
		}
		
		List<RDFResourceTree> nbrs = new ArrayList<>();
		
		compute(posExampleTree, negExampleTrees, nbrs);
		
		return nbrs;
	}
	
	private void compute(RDFResourceTree posExampleTree,
			List<RDFResourceTree> negExampleTrees, List<RDFResourceTree> nbrs) {
		
		RDFResourceTree nbr = new RDFResourceTree(posExampleTree);
		if(subsumesTrees(posExampleTree, negExampleTrees)){
//			nbrs.add(posExampleTree);
			return;
		}
		
		for(RDFResourceTree n : nbrs){
			removeTree(nbr, n);
		}
		
		if(!subsumesTrees(nbr, negExampleTrees)){
			Set<RDFResourceTree> tested = new HashSet<>();
			Node edge;
			RDFResourceTree parent;
			while(!(tested.size() == nbr.getLeafs().size()) ){
				for(RDFResourceTree leaf : nbr.getLeafs()){
					parent = leaf.getParent();
					edge = parent.getEdgeToChild(leaf);
					parent.removeChild(leaf);
					boolean isSubsumedBy = false;
					for(RDFResourceTree negTree : negExampleTrees){
						isSubsumedBy = QueryTreeUtils.isSubsumedBy(negTree, nbr);
						if(isSubsumedBy){
							break;
						}
					}
					if(isSubsumedBy){
						tested.add(leaf);
						parent.addChild(leaf, edge);
					}
					
				}
			}
			nbrs.add(nbr);
			compute(posExampleTree, negExampleTrees, nbrs);
			
		}
		
	}
	
	private boolean subsumesTrees(RDFResourceTree posExampleTree,
			List<RDFResourceTree> negExampleTrees){
		
		for(RDFResourceTree negTree : negExampleTrees){
			boolean subsumesTree = QueryTreeUtils.isSubsumedBy(negTree, posExampleTree);
			if(subsumesTree){
				return true;
			}
		}
		return false;
	}
	
	private void removeTree(RDFResourceTree tree, RDFResourceTree node){
		for(RDFResourceTree child1 : node.getChildren()){
			Node edge = node.getEdgeToChild(child1);
			for(RDFResourceTree child2 : tree.getChildren(edge)){
				if(child1.isLeaf() && child1.getData().equals(child2.getData())){
					child2.getParent().removeChild(child2, edge);
				} else {
					removeTree(child2, child1);
				}
			}
		}
	}

}
