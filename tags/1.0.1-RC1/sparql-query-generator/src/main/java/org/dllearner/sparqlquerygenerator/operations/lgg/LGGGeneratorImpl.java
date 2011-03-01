/**
 * Copyright (C) 2007-2010, Jens Lehmann
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
package org.dllearner.sparqlquerygenerator.operations.lgg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class LGGGeneratorImpl<N> implements LGGGenerator<N>{
	
	private Logger logger = Logger.getLogger(LGGGeneratorImpl.class);

	@Override
	public QueryTree<N> getLGG(QueryTree<N> tree1, QueryTree<N> tree2) {
		return getLGG(tree1, tree2, false);
	}
	
	@Override
	public QueryTree<N> getLGG(QueryTree<N> tree1, QueryTree<N> tree2,
			boolean learnFilters) {
		return computeLGG(tree1, tree2, learnFilters);
	}

	@Override
	public QueryTree<N> getLGG(Set<QueryTree<N>> trees) {
		return getLGG(trees, false);
	}
	
	@Override
	public QueryTree<N> getLGG(Set<QueryTree<N>> trees, boolean learnFilters) {
		List<QueryTree<N>> treeList = new ArrayList<QueryTree<N>>(trees);
		
		logger.info("Computing LGG for");
		for(int i = 0; i < treeList.size(); i++){
			logger.info(treeList.get(i).getStringRepresentation());
			if(i != treeList.size() - 1){
				logger.info("and");
			}
		}
		
		if(trees.size() == 1){
			return trees.iterator().next();
		}
		QueryTree<N> lgg = computeLGG(treeList.get(0), treeList.get(1), learnFilters);
		for(int i = 2; i < treeList.size(); i++){
			lgg = computeLGG(lgg, treeList.get(i), learnFilters);
		}
		
		logger.info("LGG = ");
		logger.info(lgg.getStringRepresentation());
		
		return lgg;
	}
	
	private QueryTree<N> computeLGG(QueryTree<N> tree1, QueryTree<N> tree2, boolean learnFilters){
		logger.debug("Computing LGG for");
		logger.debug(tree1.getStringRepresentation());
		logger.debug("and");
		logger.debug(tree2.getStringRepresentation());
		QueryTree<N> lgg = new QueryTreeImpl<N>(tree1.getUserObject());
		
//		if(!lgg.getUserObject().equals(tree2.getUserObject())){
//			lgg.setUserObject((N)"?");
//			if(learnFilters){
//				try {
//					int value1 = Integer.parseInt(((String)tree1.getUserObject()));
//					int value2 = Integer.parseInt(((String)tree2.getUserObject()));
//					if(value1 < value2){
//						lgg.addChild(new QueryTreeImpl<N>((N)String.valueOf(value1)), ">=-FILTER");
//						lgg.addChild(new QueryTreeImpl<N>((N)String.valueOf(value2)), "<=-FILTER");
//					} else {
//						lgg.addChild(new QueryTreeImpl<N>((N)String.valueOf(value2)), ">=-FILTER");
//						lgg.addChild(new QueryTreeImpl<N>((N)String.valueOf(value1)), "<=-FILTER");
//					}
//					
//				} catch (NumberFormatException e) {
//					
//				}
//			}
//		}
		if(!lgg.getUserObject().equals(tree2.getUserObject())){
			lgg.setUserObject((N)"?");
		}
		
		Set<QueryTreeImpl<N>> addedChildren;
		QueryTreeImpl<N> lggChild;
		for(Object edge : tree1.getEdges()){
			logger.debug("Regarding egde: " + edge);
			addedChildren = new HashSet<QueryTreeImpl<N>>();
			for(QueryTree<N> child1 : tree1.getChildren(edge)){
				for(QueryTree<N> child2 : tree2.getChildren(edge)){
					lggChild = (QueryTreeImpl<N>) computeLGG(child1, child2, learnFilters);
					boolean add = true;
					for(QueryTreeImpl<N> addedChild : addedChildren){
						logger.debug("Subsumption test");
						if(addedChild.isSubsumedBy(lggChild)){
							logger.debug("Previously added child");
							logger.debug(addedChild.getStringRepresentation());
							logger.debug("is subsumed by");
							logger.debug(lggChild.getStringRepresentation());
							logger.debug("so we can skip adding the LGG");
							add = false;
							break;
						} else if(lggChild.isSubsumedBy(addedChild)){
							logger.debug("Computed LGG");
							logger.debug(lggChild.getStringRepresentation());
							logger.debug("is subsumed by previously added child");
							logger.debug(addedChild.getStringRepresentation());
							logger.debug("so we can remove it");
							lgg.removeChild(addedChild);
						} 
					}
					if(add){
						lgg.addChild(lggChild, edge);
						addedChildren.add(lggChild);
						logger.debug("Adding child");
						logger.debug(lggChild.getStringRepresentation());
					} 
				}
			}
		}
		logger.debug("Computed LGG:");
		logger.debug(lgg.getStringRepresentation());
		return lgg;
	}

}
