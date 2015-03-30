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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBase;
import org.dllearner.algorithms.qtl.util.StopURIsDBpedia;
import org.dllearner.algorithms.qtl.util.StopURIsOWL;
import org.dllearner.algorithms.qtl.util.StopURIsRDFS;
import org.dllearner.algorithms.qtl.util.filters.NamespaceDropStatementFilter;
import org.dllearner.algorithms.qtl.util.filters.PredicateDropStatementFilter;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.QueryExecutionFactoryHttp;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.OWL;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class LGGGeneratorSimple implements LGGGenerator2{
	
	private Logger logger = LoggerFactory.getLogger(LGGGeneratorSimple.class);
	
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
		
		addNumbering(0, lgg);
		
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
		for(int i = 1; i < trees.size(); i++) {
			lgg = getLGG(lgg, trees.get(i), learnFilters);
		}
		mon.stop();
		
		addNumbering(0, lgg);
		
		return lgg;
	}
	
	private void reset() {
		subCalls = 0;
	}
	
	private RDFResourceTree computeLGG(RDFResourceTree tree1, RDFResourceTree tree2, boolean learnFilters){
		subCalls++;
		
		// if both root nodes are resource nodes and have the same URI, just return one of the two tree as LGG
		if(tree1.isResourceNode() && tree1.getData().equals(tree2.getData())){
			logger.trace("Early termination. Tree 1 {}  and tree 2 {} describe the same resource.", tree1, tree2);
			return tree1;
		}
		
		// handle literal nodes, i.e. collect literal values if both are of same datatype
		if(tree1.isLiteralNode() && tree2.isLiteralNode()){
			RDFDatatype d1 = tree1.getData().getLiteralDatatype();
			RDFDatatype d2 = tree2.getData().getLiteralDatatype();

			if(d1 != null && d1.equals(d2)){
				return new RDFResourceTree(d1);
//				((QueryTreeImpl<N>)lgg).addLiterals(((QueryTreeImpl<N>)tree1).getLiterals());
//				((QueryTreeImpl<N>)lgg).addLiterals(((QueryTreeImpl<N>)tree2).getLiterals());
			}
//			lgg.setIsLiteralNode(true);
		}
		
		
		RDFResourceTree lgg = new RDFResourceTree();
		
		Set<RDFResourceTree> addedChildren;
		RDFResourceTree lggChild;
		
		// loop over distinct edges
		for(Node edge : Sets.intersection(tree1.getEdges(), tree2.getEdges())){//if(edge.equals(OWL.sameAs.asNode())) continue;
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
					for(Iterator<RDFResourceTree> it = addedChildren.iterator(); it.hasNext();){
						RDFResourceTree addedChild = it.next();
						if(QueryTreeUtils.isSubsumedBy(addedChild, lggChild)){
//							logger.trace("Skipped adding: Previously added child {} is subsumed by {}.",
//									addedChild.getStringRepresentation(),
//									lggChild.getStringRepresentation());
							add = false;
							break;
						} else if(QueryTreeUtils.isSubsumedBy(lggChild, addedChild)){
//							logger.trace("Removing child node: {} is subsumed by previously added child {}.",
//									lggChild.getStringRepresentation(),
//									addedChild.getStringRepresentation());
							lgg.removeChild(addedChild, edge);
							it.remove();
						} 
					}
					if(add){
						lgg.addChild(lggChild, edge);
						addedChildren.add(lggChild);
//						logger.trace("Adding child {}", lggChild.getStringRepresentation());
					} 
				}
			}
		}
		
		return lgg;
	}
	
	private void addNumbering(int nodeId, RDFResourceTree tree){
//		tree.setId(nodeId);
		for(RDFResourceTree child : tree.getChildren()){
			addNumbering(nodeId++, child);
		}
	}
	
	public static void main(String[] args) throws Exception {
		// knowledge base
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
		QueryExecutionFactory qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
		qef = new QueryExecutionFactoryCacheEx(qef, CacheUtilsH2.createCacheFrontend("/tmp/cache", false, TimeUnit.DAYS.toMillis(60)));
		qef = new QueryExecutionFactoryPaginated(qef);
		
		// tree generation
		ConciseBoundedDescriptionGenerator cbdGenerator = new ConciseBoundedDescriptionGeneratorImpl(qef);
		cbdGenerator.setRecursionDepth(1);
		
		QueryTreeFactory treeFactory = new QueryTreeFactoryBase();
		treeFactory.addDropFilters(
				new PredicateDropStatementFilter(StopURIsDBpedia.get()),
				new PredicateDropStatementFilter(StopURIsRDFS.get()),
				new PredicateDropStatementFilter(StopURIsOWL.get()),
				new NamespaceDropStatementFilter(
						Sets.newHashSet(
								"http://dbpedia.org/property/", 
								"http://purl.org/dc/terms/",
								"http://dbpedia.org/class/yago/",
								"http://www.w3.org/2003/01/geo/wgs84_pos#",
								"http://www.georss.org/georss/",
								FOAF.getURI()
								)
								)
				);
		List<RDFResourceTree> trees = new ArrayList<RDFResourceTree>();
		List<String> resources = Lists.newArrayList("http://dbpedia.org/resource/Leipzig", "http://dbpedia.org/resource/Dresden");
		for(String resource : resources){
			try {
				System.out.println(resource);
				Model model = cbdGenerator.getConciseBoundedDescription(resource);
				RDFResourceTree tree = treeFactory.getQueryTree(ResourceFactory.createResource(resource), model);
				System.out.println(tree.getStringRepresentation());
				trees.add(tree);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// LGG computation
		LGGGenerator2 lggGen = new LGGGeneratorSimple();
		RDFResourceTree lgg = lggGen.getLGG(trees);
		
		System.out.println("LGG");
		System.out.println(lgg.getStringRepresentation());
		System.out.println(QueryTreeUtils.toSPARQLQueryString(lgg));
	}

}
