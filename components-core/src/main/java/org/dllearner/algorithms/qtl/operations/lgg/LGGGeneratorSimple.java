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
package org.dllearner.algorithms.qtl.operations.lgg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.graph.NodeFactory;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.NodeInv;
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
import org.dllearner.kb.sparql.SparqlEndpoint;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.vocabulary.FOAF;

/**
 * An LGG generator based on syntax and structure only, i.e. without taking into account any type of
 * Semantics.
 *
 * @author Lorenz Bühmann
 *
 */
public class LGGGeneratorSimple extends AbstractLGGGenerator {

	private boolean complete = true;
	
	@Override
	protected RDFResourceTree computeLGG(RDFResourceTree tree1, RDFResourceTree tree2, boolean learnFilters){
		subCalls++;
		
		// 1. compare the root node
		// if both root nodes have same URI or literal value, just return one of the two trees as LGG
		if ((tree1.isResourceNode() || tree1.isLiteralValueNode()) && tree1.getData().equals(tree2.getData())) {
			logger.trace("Early termination. Tree 1 {}  and tree 2 {} describe the same resource.", tree1, tree2);
			return tree1;
		}
		
		// handle literal nodes with same datatype
		if (tree1.isLiteralNode() && tree2.isLiteralNode()) {
			RDFDatatype d1 = tree1.getData().getLiteralDatatype();
			RDFDatatype d2 = tree2.getData().getLiteralDatatype();

			if (d1 != null && d1.equals(d2)) {
				return new RDFResourceTree(d1);
			}
		}
		
		// else create new empty tree
		RDFResourceTree lgg = new RDFResourceTree();

		// 2. compare the edges
		// we only have to compare edges contained in both trees
		// outgoing edges
		List<Set<Node>> commonEdges = new ArrayList<>();
		commonEdges.add(Sets.intersection(
				tree1.getEdges().stream().filter(e -> !(e instanceof NodeInv)).collect(Collectors.toSet()),
				tree2.getEdges().stream().filter(e -> !(e instanceof NodeInv)).collect(Collectors.toSet())));
		// incoming edges
		commonEdges.add(Sets.intersection(
				tree1.getEdges().stream().filter(e -> e instanceof NodeInv).collect(Collectors.toSet()),
				tree2.getEdges().stream().filter(e -> e instanceof NodeInv).collect(Collectors.toSet())));

		for (Set<Node> edges : commonEdges) {
			for (Node edge : edges) {
				if(stop || isTimeout()) {
					complete = false;
					break;
				}
				Set<RDFResourceTree> addedChildren = new HashSet<>();
				// loop over children of first tree
				for (RDFResourceTree child1 : tree1.getChildren(edge)) {
					if(stop || isTimeout()) {
						complete = false;
						break;
					}
					// loop over children of second tree
					for (RDFResourceTree child2 : tree2.getChildren(edge)) {
						if(stop || isTimeout()) {
							complete = false;
							break;
						}
						// compute the LGG
						RDFResourceTree lggChild = computeLGG(child1, child2, learnFilters);

						// check if there was already a more specific child computed before
						// and if so don't add the current one
						boolean add = true;
						for (Iterator<RDFResourceTree> it = addedChildren.iterator(); it.hasNext() && !stop && !isTimeout(); ) {
							RDFResourceTree addedChild = it.next();
							if (QueryTreeUtils.isSubsumedBy(addedChild, lggChild)) {
								//							logger.trace("Skipped adding: Previously added child {} is subsumed by {}.",
								//									addedChild.getStringRepresentation(),
								//									lggChild.getStringRepresentation());
								add = false;
								break;
							} else if (QueryTreeUtils.isSubsumedBy(lggChild, addedChild)) {
								//							logger.trace("Removing child node: {} is subsumed by previously added child {}.",
								//									lggChild.getStringRepresentation(),
								//									addedChild.getStringRepresentation());
								lgg.removeChild(addedChild, edge);
								it.remove();
							}
						}
						if (add) {
							lgg.addChild(lggChild, edge);
							addedChildren.add(lggChild);
							//						logger.trace("Adding child {}", lggChild.getStringRepresentation());
						}
					}
				}
			}
		}

		return lgg;
	}

	public boolean isComplete() {
		return complete;
	}

	public static void main(String[] args) throws Exception {
		// knowledge base
//		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
//		QueryExecutionFactory qef = FluentQueryExecutionFactory
//				.http(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs()).config()
//				.withCache(CacheUtilsH2.createCacheFrontend("/tmp/cache", false, TimeUnit.DAYS.toMillis(60)))
//				.withPagination(10000).withDelay(50, TimeUnit.MILLISECONDS).end().create();
//
//		// tree generation
//		ConciseBoundedDescriptionGenerator cbdGenerator = new ConciseBoundedDescriptionGeneratorImpl(qef);
//		int maxDepth = 2;
//		cbdGenerator.setRecursionDepth(maxDepth);
//
//		QueryTreeFactory treeFactory = new QueryTreeFactoryBase();
//		treeFactory.setMaxDepth(maxDepth);
//		treeFactory.addDropFilters(
//				new PredicateDropStatementFilter(StopURIsDBpedia.get()),
//				new PredicateDropStatementFilter(StopURIsRDFS.get()),
//				new PredicateDropStatementFilter(StopURIsOWL.get()),
//				new NamespaceDropStatementFilter(
//						Sets.newHashSet(
//								"http://dbpedia.org/property/",
//								"http://purl.org/dc/terms/",
//								"http://dbpedia.org/class/yago/",
//								"http://www.w3.org/2003/01/geo/wgs84_pos#",
//								"http://www.georss.org/georss/",
//								FOAF.getURI()
//								)
//								)
//				);
//		List<RDFResourceTree> trees = new ArrayList<>();
//		List<String> resources = Lists.newArrayList("http://dbpedia.org/resource/Leipzig", "http://dbpedia.org/resource/Dresden");
//		for(String resource : resources){
//			try {
//				System.out.println(resource);
//				Model model = cbdGenerator.getConciseBoundedDescription(resource);
//				RDFResourceTree tree = treeFactory.getQueryTree(ResourceFactory.createResource(resource), model);
//				System.out.println(tree.getStringRepresentation());
//				trees.add(tree);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		
		// LGG computation
		LGGGenerator lggGen = new LGGGeneratorSimple();
//		RDFResourceTree lgg = lggGen.getLGG(trees);
//
//		System.out.println("LGG");
//		System.out.println(lgg.getStringRepresentation());
//		System.out.println(QueryTreeUtils.toSPARQLQueryString(lgg));
//		System.out.println(QueryTreeUtils.toOWLClassExpression(lgg));

		Node edge = NodeFactory.createURI("p");
		Node edgeInv = new NodeInv(NodeFactory.createURI("p"));

		RDFResourceTree tree1 = new RDFResourceTree(NodeFactory.createURI("urn:a"));
		tree1.addChild(new RDFResourceTree(NodeFactory.createURI("urn:c")), edge);
		tree1.addChild(new RDFResourceTree(NodeFactory.createURI("urn:d")), edgeInv);
		System.out.println(tree1.getStringRepresentation());

		RDFResourceTree tree2 = new RDFResourceTree(NodeFactory.createURI("urn:b"));
		tree2.addChild(new RDFResourceTree(NodeFactory.createURI("urn:c")), edge);
		tree2.addChild(new RDFResourceTree(NodeFactory.createURI("urn:d")), edgeInv);
		System.out.println(tree2.getStringRepresentation());

		RDFResourceTree lgg = lggGen.getLGG(tree1, tree2);
		System.out.println("LGG");
		System.out.println(lgg.getStringRepresentation());
		System.out.println(QueryTreeUtils.toSPARQLQueryString(lgg));
	}

}
