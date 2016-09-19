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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.jena.datatypes.RDFDatatype;
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
import org.dllearner.utilities.OwlApiJenaUtils;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLProperty;

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
	protected RDFResourceTree computeLGG(RDFResourceTree tree1, RDFResourceTree tree2, boolean learnFilters){
		subCalls++;
		
		// 1. compare the root node
		// if both root nodes have same URI or literal value, just return one of the two trees as LGG
		if((tree1.isResourceNode() || tree1.isLiteralValueNode()) && tree1.getData().equals(tree2.getData())){
			logger.trace("Early termination. Tree 1 {}  and tree 2 {} describe the same resource.", tree1, tree2);
			return tree1;
		}
		
		// handle literal nodes with same datatype
		if(tree1.isLiteralNode() && tree2.isLiteralNode()){
			RDFDatatype d1 = tree1.getData().getLiteralDatatype();
			RDFDatatype d2 = tree2.getData().getLiteralDatatype();

			if(d1 != null && d1.equals(d2)){
				return new RDFResourceTree(d1);
				// TODO collect literal values
			}
		}
		
		// else create new empty tree
		RDFResourceTree lgg = new RDFResourceTree();
		
		// 2. compare the edges
		// we only have to compare edges which are 
		// a) contained in both trees
		// b) related via subsumption, i.e. p1 ⊑ p2
		
		// get edges of tree 2 connected via subsumption
		Set<Triple<Node, Node, Node>> relatedEdges = getRelatedEdges(tree1, tree2);
		for (Triple<Node, Node, Node> entry : relatedEdges){
			Node edge1 = entry.getLeft();//System.out.println("e1:" + edge1);
			Node edge2 = entry.getMiddle();
			Node lcs = entry.getRight();

			Set<RDFResourceTree> addedChildren = new HashSet<>();

			// loop over children of first tree
			for(RDFResourceTree child1 : tree1.getChildren(edge1)){//System.out.println("c1:" + child1);
					// loop over children of second tree
					for(RDFResourceTree child2 : tree2.getChildren(edge2)){//System.out.println("c2:" + child2);
						RDFResourceTree lggChild;

						// special case: rdf:type relation
						if(edge1.equals(RDF.type.asNode())) {
							if(QueryTreeUtils.isSubsumedBy(child1, child2, Entailment.RDFS)) {
								lggChild = child2;
							} else if(QueryTreeUtils.isSubsumedBy(child2, child1, Entailment.RDFS)) {
								lggChild = child1;
							} else {
								lggChild = computeLGG(child1, child2, learnFilters);
							}
						} else {
							// compute the LGG
							lggChild = computeLGG(child1, child2, learnFilters);
						}

						// check if there was already a more specific child computed before
						// and if so don't add the current one
						boolean add = true;
						for(Iterator<RDFResourceTree> it = addedChildren.iterator(); it.hasNext();){
							RDFResourceTree addedChild = it.next();
							
							if(QueryTreeUtils.isSubsumedBy(addedChild, lggChild, reasoner, edge1.equals(RDF.type.asNode()))){
//								logger.trace("Skipped adding: Previously added child {} is subsumed by {}.",
//										addedChild.getStringRepresentation(),
//										lggChild.getStringRepresentation());
								add = false;
								break;
							} else if(QueryTreeUtils.isSubsumedBy(lggChild, addedChild, reasoner, edge1.equals(RDF.type.asNode()))){
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

	@Override
	protected boolean isSubTreeOf(RDFResourceTree tree1, RDFResourceTree tree2) {
		return false;
	}

	@Override
	protected List<Set<Node>> getRelatedPredicates(RDFResourceTree tree1, RDFResourceTree tree2) {
		return null;
	}

	@Override
	protected RDFResourceTree preProcess(RDFResourceTree tree) {
		QueryTreeUtils.keepMostSpecificTypes(tree, reasoner);
		return tree;
	}

	@Override
	protected RDFResourceTree postProcess(RDFResourceTree tree) {
		// prune the tree according to the given entailment
		QueryTreeUtils.prune(tree, reasoner, entailment);
		return tree;
	}

	/*
		 * For each edge in tree 1 we compute the related edges in tree 2.
		 */
	private Set<Triple<Node, Node, Node>> getRelatedEdges(RDFResourceTree tree1, RDFResourceTree tree2) {
		Set<Triple<Node, Node, Node>> result = new HashSet<>();
		System.out.println(tree1 + "::::::" + tree2);

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

			split2.get(false).stream()
					.filter(e2 -> {
						RDFResourceTree child = tree2.getChildren(e2).iterator().next();
						return dataproperty && child.isLiteralNode() || !dataproperty && !child.isLiteralNode();
					} )
					.forEach(e2 -> {
						System.out.println(e1 + "---" + e2);
						Node lcs = NonStandardReasoningServices.getLeastCommonSubsumer(reasoner, e1, e2,
																					   EntityType.OBJECT_PROPERTY);

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

	private Node getLeastCommonSubsumerClass(Node cls1, Node cls2) {
		return null;
	}

	private Node getLeastCommonSubsumerProperty(Node p1, Node p2) {
		OWLProperty lcs = getLeastCommonSubsumerProperty(reasoner,
				OwlApiJenaUtils.asOWLEntity(p1, EntityType.OBJECT_PROPERTY),
				OwlApiJenaUtils.asOWLEntity(p2, EntityType.OBJECT_PROPERTY));

		if(lcs != null) {
			return OwlApiJenaUtils.asNode(lcs);
		}

		return null;
	}

	public static OWLProperty getLeastCommonSubsumerProperty(AbstractReasonerComponent reasoner, OWLProperty p1, OWLProperty p2) {

		if(p1.equals(p2)) {
			return p1;
		}

		SortedSet<OWLProperty> superProperties1 = reasoner.getSuperProperties(p1);
		if(superProperties1.contains(p2)) {
			return p2;
		}

		SortedSet<OWLProperty> superProperties2 = reasoner.getSuperProperties(p2);
		if(superProperties2.contains(p1)) {
			return p1;
		}

		Sets.SetView<OWLProperty> intersection = Sets.intersection(superProperties1, superProperties2);

		if(!intersection.isEmpty()) {
			return intersection.iterator().next();
		}

		for (OWLProperty sup1 : superProperties1) {
			for (OWLProperty sup2 : superProperties2) {
				OWLProperty lcs = getLeastCommonSubsumerProperty(reasoner, sup1, sup2);

				if(lcs != null) {
					return lcs;
				}
			}
		}

		return null;
	}

	private boolean isBuiltInEntity(Node n) {
		return n.getNameSpace().equals(RDF.getURI()) ||
				n.getNameSpace().equals(RDFS.getURI()) ||
				n.getNameSpace().equals(OWL.getURI());
	}
	
	public static void main(String[] args) throws Exception {
		StringRenderer.setRenderer(Rendering.DL_SYNTAX);
		// knowledge base
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
		QueryExecutionFactory qef = FluentQueryExecutionFactory
				.http(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs()).config()
				.withCache(CacheUtilsH2.createCacheFrontend("/tmp/cache", false, TimeUnit.DAYS.toMillis(60)))
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
		LGGGenerator lggGen = new LGGGeneratorRDFS(reasoner);
		RDFResourceTree lgg = lggGen.getLGG(trees);

		System.out.println("LGG");
		System.out.println(lgg.getStringRepresentation());
		System.out.println(QueryTreeUtils.toSPARQLQueryString(lgg));
		System.out.println(QueryTreeUtils.toOWLClassExpression(lgg));
	}

}
