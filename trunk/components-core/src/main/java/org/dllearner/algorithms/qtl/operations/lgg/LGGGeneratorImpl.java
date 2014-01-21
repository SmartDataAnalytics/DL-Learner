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
package org.dllearner.algorithms.qtl.operations.lgg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.qtl.cache.QueryTreeCache;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class LGGGeneratorImpl<N> implements LGGGenerator<N>{
	
	private Logger logger = Logger.getLogger(LGGGeneratorImpl.class);
	private int nodeId;
	
	private int calls = 0;

	@Override
	public QueryTree<N> getLGG(QueryTree<N> tree1, QueryTree<N> tree2) {
		return getLGG(tree1, tree2, false);
	}
	
	@Override
	public QueryTree<N> getLGG(QueryTree<N> tree1, QueryTree<N> tree2,
			boolean learnFilters) {
		nodeId = 0;
		calls = 0;
		Monitor mon = MonitorFactory.getTimeMonitor("LGG");
		mon.start();
		QueryTree<N> lgg = computeLGG(tree1, tree2, learnFilters);
		mon.stop();
		addNumbering(lgg);
		logger.debug("Calls needed: " + calls);
		return lgg;
	}

	@Override
	public QueryTree<N> getLGG(List<QueryTree<N>> trees) {
		return getLGG(trees, false);
	}
	
	@Override
	public QueryTree<N> getLGG(List<QueryTree<N>> trees, boolean learnFilters) {
		nodeId = 0;
		List<QueryTree<N>> treeList = new ArrayList<QueryTree<N>>(trees);
		
		if(logger.isDebugEnabled()){
			logger.debug("Computing LGG for");
		}
		
		for(int i = 0; i < treeList.size(); i++){
			if(logger.isDebugEnabled()){
				logger.debug(treeList.get(i).getStringRepresentation());
			}
			
			if(i != treeList.size() - 1){
				if(logger.isDebugEnabled()){
					logger.debug("and");
				}
			}
		}
		
		if(trees.size() == 1){
			return trees.iterator().next();
		}
		Monitor mon = MonitorFactory.getTimeMonitor("LGG");
		mon.start();
		QueryTree<N> lgg = getLGG(treeList.get(0), treeList.get(1), learnFilters);
		if(logger.isDebugEnabled()){
			logger.debug("LGG for 1 and 2:\n" + lgg.getStringRepresentation());
		}
		
		for(int i = 2; i < treeList.size(); i++){
			lgg = getLGG(lgg, treeList.get(i), learnFilters);
			if(logger.isDebugEnabled()){
				logger.debug("LGG for 1-" + (i+1) + ":\n" + lgg.getStringRepresentation());
			}
		}
		
		if(logger.isDebugEnabled()){
			logger.debug("LGG = ");
			logger.debug(lgg.getStringRepresentation());
		}
		mon.stop();
		addNumbering(lgg);
		return lgg;
	}
	
	private QueryTree<N> computeLGG(QueryTree<N> tree1, QueryTree<N> tree2, boolean learnFilters){
		calls++;
		if(logger.isDebugEnabled()){
			logger.debug("Computing LGG for");
			logger.debug(tree1.getStringRepresentation());
			logger.debug("and");
			logger.debug(tree2.getStringRepresentation());
		}
		
		QueryTree<N> lgg;
		//firstly, we check if both root nodes are resource nodes and have the same URI, i.e. the trees describe the same resource 
		//if YES all child nodes should be also the same and we can just return one of the two tree as LGG
		if((tree1.isResourceNode() && tree2.isResourceNode() || tree1.isLiteralNode() && tree2.isLiteralNode()) 
				&& tree1.getUserObject().equals(tree2.getUserObject())){
			if(logger.isDebugEnabled()){
				logger.debug("Early termination. Tree 1(" + tree1 + ") and tree 2(" + tree2 + ") describe the same resource.");
			}
			return tree1;
		}
		//if NO we have to create a new tree as LGG and compute the LGG for the all child node pairs having the same edge to the parent nodes
		lgg = new QueryTreeImpl<N>(tree1.getUserObject());
//		if(tree1.isResourceNode() && tree2.isResourceNode()){
//			lgg.setIsResourceNode(true);
//		}
		
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
		if(!lgg.sameType(tree2) || !lgg.getUserObject().equals(tree2.getUserObject())){
			lgg.setUserObject((N)"?");
			lgg.setIsLiteralNode(false);
			lgg.setIsResourceNode(false);
		} 
		
		if(tree1.isLiteralNode() && tree2.isLiteralNode()){
			RDFDatatype d1 = tree1.getDatatype();
			RDFDatatype d2 = tree2.getDatatype();
//			if(d1 != null && d2 != null && d1 == d2){
			if(d1 == d2){
				((QueryTreeImpl<N>)lgg).addLiterals(((QueryTreeImpl<N>)tree1).getLiterals());
				((QueryTreeImpl<N>)lgg).addLiterals(((QueryTreeImpl<N>)tree2).getLiterals());
			}
			lgg.setIsLiteralNode(true);
		}
		
		Set<QueryTreeImpl<N>> addedChildren;
		QueryTreeImpl<N> lggChild;
		for(Object edge : new TreeSet<Object>(tree1.getEdges())){
			if(logger.isTraceEnabled()){
				logger.trace("Analyzing egde: " + edge);
			}
			addedChildren = new HashSet<QueryTreeImpl<N>>();
			for(QueryTree<N> child1 : tree1.getChildren(edge)){
				for(QueryTree<N> child2 : tree2.getChildren(edge)){
					lggChild = (QueryTreeImpl<N>) computeLGG(child1, child2, learnFilters);
					boolean add = true;
					for(QueryTreeImpl<N> addedChild : addedChildren){
						if(logger.isTraceEnabled()){
							logger.trace("Subsumption test");
						}
						if(addedChild.isSubsumedBy(lggChild)){
							if(logger.isTraceEnabled()){
								logger.trace("Previously added child");
								logger.trace(addedChild.getStringRepresentation());
								logger.trace("is subsumed by");
								logger.trace(lggChild.getStringRepresentation());
								logger.trace("so we can skip adding the LGG");
							}
							add = false;
							break;
						} else if(lggChild.isSubsumedBy(addedChild)){
							if(logger.isTraceEnabled()){
								logger.trace("Computed LGG");
								logger.trace(lggChild.getStringRepresentation());
								logger.trace("is subsumed by previously added child");
								logger.trace(addedChild.getStringRepresentation());
								logger.trace("so we can remove it");
							}
							lgg.removeChild(addedChild);
						} 
					}
					if(add){
						lgg.addChild(lggChild, edge);
						addedChildren.add(lggChild);
						if(logger.isTraceEnabled()){
							logger.trace("Adding child");
							logger.trace(lggChild.getStringRepresentation());
						}
					} 
				}
			}
		}
		if(logger.isTraceEnabled()){
			logger.trace("Computed LGG:");
			logger.trace(lgg.getStringRepresentation());
		}
		return lgg;
	}
	
	private void addNumbering(QueryTree<N> tree){
		tree.setId(nodeId++);
		for(QueryTree<N> child : tree.getChildren()){
			addNumbering(child);
		}
	}
	
	public static void main(String[] args) throws Exception {
		LGGGenerator<String> lggGen = new LGGGeneratorImpl<String>();
		
		List<QueryTree<String>> trees = new ArrayList<QueryTree<String>>();
		QueryTree<String> tree;
		Model model;
		ConciseBoundedDescriptionGenerator cbdGenerator = new ConciseBoundedDescriptionGeneratorImpl(SparqlEndpoint.getEndpointDBpedia(), "cache");
		cbdGenerator.setRecursionDepth(1);
		QueryTreeCache treeCache = new QueryTreeCache();
		List<String> resources = Lists.newArrayList("http://dbpedia.org/resource/Leipzig");//, "http://dbpedia.org/resource/Dresden");
		for(String resource : resources){
			try {
				System.out.println(resource);
				model = cbdGenerator.getConciseBoundedDescription(resource);
				tree = treeCache.getQueryTree(resource, model);
				System.out.println(tree.getStringRepresentation());
				trees.add(tree);
				trees.add(tree);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		lggGen.getLGG(trees);
	}

}
