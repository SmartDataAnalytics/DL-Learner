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

import org.dllearner.algorithms.qtl.cache.QueryTreeCache;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.NodeType;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.Vars;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class LGGGeneratorImpl2 implements LGGGenerator2{
	
	private Logger logger = LoggerFactory.getLogger(LGGGeneratorImpl2.class);
	private int nodeId;
	
	private Monitor mon = MonitorFactory.getTimeMonitor("lgg");
	
	private int subCalls;
	
	@Override
	public RDFResourceTree getLGG(RDFResourceTree tree1, RDFResourceTree tree2) {
		return getLGG(tree1, tree2, false);
	}
	
	@Override
	public RDFResourceTree getLGG(RDFResourceTree tree1, RDFResourceTree tree2,
			boolean learnFilters) {
		
		reset();
		
		mon.start();
		RDFResourceTree lgg = computeLGG(tree1, tree2, learnFilters);
		mon.stop();
		
		addNumbering(lgg);
		
		return lgg;
	}

	@Override
	public RDFResourceTree getLGG(List<RDFResourceTree> trees) {
		return getLGG(trees, false);
	}
	
	@Override
	public RDFResourceTree getLGG(List<RDFResourceTree> trees, boolean learnFilters) {
		// if there is only 1 tree return it
		if(trees.size() == 1){
			return trees.get(0);
		}
		
		// lgg(t_1, t_n)
		mon.start();
		RDFResourceTree lgg = trees.get(0);
		for(int i = 1; i <= trees.size(); i++) {
			lgg = getLGG(lgg, trees.get(i), learnFilters);
		}
		mon.stop();
		
		addNumbering(lgg);
		
		return lgg;
	}
	
	private void reset() {
		nodeId = 0;
		subCalls = 0;
	}
	
	private RDFResourceTree computeLGG(RDFResourceTree tree1, RDFResourceTree tree2, boolean learnFilters){
		subCalls++;
		
		// if both root nodes are resource nodes and have the same URI just return one of the two tree as LGG
		if(tree1.isResourceNode() && tree1.getData().equals(tree2.getData())){
			logger.debug("Early termination. Tree 1(" + tree1 + ") and tree 2(" + tree2 + ") describe the same resource.");
			return tree1;
		}
		
		RDFResourceTree lgg = new RDFResourceTree(0, NodeFactory.createVariable(""));
		
		if(tree1.isLiteralNode() && tree2.isLiteralNode()){
			RDFDatatype d1 = tree1.getData().getLiteralDatatype();
			RDFDatatype d2 = tree2.getData().getLiteralDatatype();

			if(d1 != null && d1.equals(d2)){
//				((QueryTreeImpl<N>)lgg).addLiterals(((QueryTreeImpl<N>)tree1).getLiterals());
//				((QueryTreeImpl<N>)lgg).addLiterals(((QueryTreeImpl<N>)tree2).getLiterals());
			}
//			lgg.setIsLiteralNode(true);
		}
		
		Set<RDFResourceTree> addedChildren;
		RDFResourceTree lggChild;
		
		// loop over distinct edges
		for(Node edge : tree1.getEdges()){
			addedChildren = new HashSet<RDFResourceTree>();
			// loop over children of first tree
			for(RDFResourceTree child1 : tree1.getChildren(edge)){
				// loop over children of second tree
				for(RDFResourceTree child2 : tree2.getChildren(edge)){
					// compute the LGG
					lggChild = computeLGG(child1, child2, learnFilters);
					
					// check if there was already a more specific child computed before
					// and if so don't add the current one
					boolean add = true;
//					for(RDFResourceTree addedChild : addedChildren){
//						if(logger.isTraceEnabled()){
//							logger.trace("Subsumption test");
//						}
//						if(addedChild.isSubsumedBy(lggChild)){
//							if(logger.isTraceEnabled()){
//								logger.trace("Previously added child");
//								logger.trace(addedChild.getStringRepresentation());
//								logger.trace("is subsumed by");
//								logger.trace(lggChild.getStringRepresentation());
//								logger.trace("so we can skip adding the LGG");
//							}
//							add = false;
//							break;
//						} else if(lggChild.isSubsumedBy(addedChild)){
//							if(logger.isTraceEnabled()){
//								logger.trace("Computed LGG");
//								logger.trace(lggChild.getStringRepresentation());
//								logger.trace("is subsumed by previously added child");
//								logger.trace(addedChild.getStringRepresentation());
//								logger.trace("so we can remove it");
//							}
//							lgg.removeChild(addedChild);
//						} 
//					}
//					if(add){
//						lgg.addChild(lggChild, edge);
//						addedChildren.add(lggChild);
//						if(logger.isTraceEnabled()){
//							logger.trace("Adding child");
//							logger.trace(lggChild.getStringRepresentation());
//						}
//					} 
				}
			}
		}
		if(logger.isTraceEnabled()){
			logger.trace("Computed LGG:");
//			logger.trace(lgg.getStringRepresentation());
		}
		return lgg;
	}
	
	private void addNumbering(RDFResourceTree tree){
//		tree.setId(nodeId++);
		for(RDFResourceTree child : tree.getChildren()){
			addNumbering(child);
		}
	}
	
	public static void main(String[] args) throws Exception {
		LGGGenerator2 lggGen = new LGGGeneratorImpl2();
		
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
//		lggGen.getLGG(trees);
	}

}
