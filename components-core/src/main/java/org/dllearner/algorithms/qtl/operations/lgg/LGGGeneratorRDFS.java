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

import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBase;
import org.dllearner.algorithms.qtl.util.*;
import org.dllearner.algorithms.qtl.util.filters.NamespaceDropStatementFilter;
import org.dllearner.algorithms.qtl.util.filters.ObjectDropStatementFilter;
import org.dllearner.algorithms.qtl.util.filters.PredicateDropStatementFilter;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.StringRenderer;
import org.dllearner.core.StringRenderer.Rendering;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.NonStandardReasoningServices;
import org.semanticweb.owlapi.model.EntityType;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * An LGG generator with RDFS entailment enabled.
 * @author Lorenz Bühmann
 *
 */
public class LGGGeneratorRDFS extends AbstractLGGGenerator {

	protected Entailment entailment = Entailment.SIMPLE;
	protected AbstractReasonerComponent reasoner;

	/**
	 * @param reasoner the underlying reasoner used for RDFS entailment
	 */
	public LGGGeneratorRDFS(AbstractReasonerComponent reasoner) {
		this.reasoner = reasoner;
		this.entailment = Entailment.RDFS;
	}

	@Override
	protected boolean isSubTreeOf(RDFResourceTree tree1, RDFResourceTree tree2) {
		return QueryTreeUtils.isSubsumedBy(tree1, tree2, reasoner, tree1.isClassNode());
	}

	@Override
	protected RDFResourceTree preProcess(RDFResourceTree tree) {
		QueryTreeUtils.keepMostSpecificTypes(tree, reasoner);

		return QueryTreeUtils.materializeTypes(tree, reasoner);
	}

	@Override
	protected RDFResourceTree postProcess(RDFResourceTree tree) {
		// prune the tree according to the given entailment
		QueryTreeUtils.prune(tree, reasoner, entailment);
		return tree;
	}

	@Override
	protected Set<Triple<Node, Node, Node>> getRelatedEdges(RDFResourceTree tree1, RDFResourceTree tree2) {
		Set<Triple<Node, Node, Node>> result = new HashSet<>();

		Predicate<Node> isBuiltIn = n -> isBuiltInEntity(n);

		// split by built-in and non-built-in predicates
		Map<Boolean, List<Node>> split1 = tree1.getEdges().stream().collect(Collectors.partitioningBy(isBuiltIn));
		Map<Boolean, List<Node>> split2 = tree2.getEdges().stream().collect(Collectors.partitioningBy(isBuiltIn));

//		SortedSet<Node> edges1 = tree1.getEdges().stream().filter(e -> !isBuiltInEntity(e))
//				.collect(Collectors.toCollection(() -> new TreeSet<>(new NodeComparatorInv())));
//		SortedSet<Node> edges2 = tree2.getEdges().stream().filter(e -> !isBuiltInEntity(e))
//				.collect(Collectors.toCollection(() -> new TreeSet<>(new NodeComparatorInv())));

		for (Node e1 : split1.get(false)) {
			boolean dataproperty = tree1.getChildren(e1).iterator().next().isLiteralNode();
			EntityType entityType = dataproperty ? EntityType.DATA_PROPERTY : EntityType.OBJECT_PROPERTY;

			split2.get(false).stream()
					.filter(e2 -> {
						RDFResourceTree child = tree2.getChildren(e2).iterator().next();
						return dataproperty && child.isLiteralNode() || !dataproperty && !child.isLiteralNode();
					} )
					.forEach(e2 -> {
						Node lcs = NonStandardReasoningServices.getLeastCommonSubsumer(reasoner, e1, e2, entityType);

						if(lcs != null) {
							result.add(Triple.of(e1, e2, lcs));
						}
					});
		}

		List<Node> builtInEntities1 = split1.get(true);
		List<Node> builtInEntities2 = split2.get(true);

		Set<Triple<Node, Node, Node>> builtInEntitiesCommon = builtInEntities1.stream().filter(e -> builtInEntities2.contains(e)).map(
				e -> Triple.of(e, e, e)).collect(
				Collectors.toSet());

		result.addAll(builtInEntitiesCommon);

		return result;
	}

	private boolean isBuiltInEntity(Node n) {
		return n.getNameSpace().equals(RDF.getURI()) ||
				n.getNameSpace().equals(RDFS.getURI()) ||
				n.getNameSpace().equals(OWL.getURI());
	}

	@Override
	protected RDFResourceTree processClassNodes(RDFResourceTree tree1, RDFResourceTree tree2) {

		if(tree1.isResourceNode() && tree2.isResourceNode()) {
			System.out.print("LCS(" + tree1 + ", " + tree2 + ")");
			Node lcs = NonStandardReasoningServices.getLeastCommonSubsumer(reasoner,
																			tree1.getData(), tree2.getData(),
																			EntityType.CLASS);
			System.out.println(" = " + lcs);
			if(lcs != null) {
				return new RDFResourceTree(lcs);
			}
		}

		RDFResourceTree lgg = new RDFResourceTree();

		Set<Triple<Node, Node, Node>> relatedEdges = getRelatedEdges(tree1, tree2);
		for (Triple<Node, Node, Node> entry : relatedEdges) {

			Node edge1 = entry.getLeft();
			Node edge2 = entry.getMiddle();
			Node lcs = entry.getRight();

			Set<RDFResourceTree> addedChildren = new HashSet<>();

			// loop over children of first tree
			for(RDFResourceTree child1 : tree1.getChildren(edge1)){//System.out.println("c1:" + child1);

				// loop over children of second tree
				for(RDFResourceTree child2 : tree2.getChildren(edge2)){//System.out.println("c2:" + child2);


					RDFResourceTree lggChild = computeLGG(child1, child2, false);

					// check if there was already a more specific child computed before
					// and if so don't add the current one
					boolean add = true;
					for(Iterator<RDFResourceTree> it = addedChildren.iterator(); it.hasNext();){
						RDFResourceTree addedChild = it.next();

						if(isSubTreeOf(addedChild, lggChild)){
//								logger.trace("Skipped adding: Previously added child {} is subsumed by {}.",
//										addedChild.getStringRepresentation(),
//										lggChild.getStringRepresentation());
							add = false;
							break;
						} else if(isSubTreeOf(lggChild, addedChild)){
//								logger.trace("Removing child node: {} is subsumed by previously added child {}.",
//										lggChild.getStringRepresentation(),
//										addedChild.getStringRepresentation());
							lgg.removeChild(addedChild, lgg.getEdgeToChild(addedChild));
							it.remove();
						}
					}
					if(add){
						lgg.addChild(lggChild, lcs);
						addedChildren.add(lggChild);
//							logger.trace("Adding child {}", lggChild.getStringRepresentation());
					}
				}
			}
		}
		return lgg;
	}
	
	public static void main(String[] args) throws Exception {
		StringRenderer.setRenderer(Rendering.DL_SYNTAX);
		// knowledge base
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
		endpoint = SparqlEndpoint.create("http://sake.informatik.uni-leipzig.de:8890/sparql", "http://dbpedia.org");
		QueryExecutionFactory qef = FluentQueryExecutionFactory
				.http(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs()).config()
				.withCache(CacheUtilsH2.createCacheFrontend(System.getProperty("java.io.tmpdir") + File.separator + "cache", false, TimeUnit.DAYS.toMillis(60)))
				.withPagination(10000).withDelay(50, TimeUnit.MILLISECONDS).end().create();

		// tree generation
		ConciseBoundedDescriptionGenerator cbdGenerator = new ConciseBoundedDescriptionGeneratorImpl(qef);
		int maxDepth = 2;

		QueryTreeFactory treeFactory = new QueryTreeFactoryBase();
		treeFactory.setMaxDepth(maxDepth);
		treeFactory.addDropFilters(
				new PredicateDropStatementFilter(StopURIsDBpedia.get()),
				new PredicateDropStatementFilter(StopURIsRDFS.get()),
				new PredicateDropStatementFilter(StopURIsOWL.get()),
				new ObjectDropStatementFilter(StopURIsOWL.get()),
				new PredicateDropStatementFilter(StopURIsSKOS.get()),
				new ObjectDropStatementFilter(StopURIsSKOS.get()),
				new NamespaceDropStatementFilter(Sets.newHashSet("http://dbpedia.org/property/",
						"http://purl.org/dc/terms/", "http://dbpedia.org/class/yago/",
						"http://www.w3.org/2003/01/geo/wgs84_pos#", "http://www.georss.org/georss/", FOAF.getURI())));
		List<RDFResourceTree> trees = new ArrayList<>();
		List<String> resources = Lists.newArrayList("http://dbpedia.org/resource/Leipzig",
				"http://dbpedia.org/resource/Berlin");
		for (String resource : resources) {
			try {
				System.out.println(resource);
				Model model = cbdGenerator.getConciseBoundedDescription(resource, maxDepth);
				RDFResourceTree tree = treeFactory.getQueryTree(ResourceFactory.createResource(resource), model);
				System.out.println(tree.getStringRepresentation());
				trees.add(tree);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// LGG computation
		SPARQLReasoner reasoner = new SPARQLReasoner(qef);
		reasoner.setPrecomputeClassHierarchy(true);
		reasoner.setPrecomputeObjectPropertyHierarchy(true);
		reasoner.setPrecomputeDataPropertyHierarchy(true);
		reasoner.init();
		reasoner.precomputePropertyDomains();
		reasoner.precomputeObjectPropertyRanges();
		LGGGenerator lggGen = new LGGGeneratorRDFS(reasoner);
		RDFResourceTree lgg = lggGen.getLGG(trees);

		System.out.println("LGG");
		System.out.println(lgg.getStringRepresentation());
		System.out.println(QueryTreeUtils.toSPARQLQueryString(lgg));
		System.out.println(QueryTreeUtils.toOWLClassExpression(lgg));
	}

}
