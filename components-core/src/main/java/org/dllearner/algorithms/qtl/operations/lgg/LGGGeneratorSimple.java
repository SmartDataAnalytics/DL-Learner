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

import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.NodeInv;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An LGG generator based on syntax and structure only, i.e. without taking into account any type of
 * Semantics.
 *
 * @author Lorenz Bühmann
 *
 */
public class LGGGeneratorSimple extends AbstractLGGGenerator {

	@Override
	protected Set<Triple<Node, Node, Node>> getRelatedEdges(RDFResourceTree tree1, RDFResourceTree tree2) {
		return Sets.intersection(tree1.getEdges(), tree2.getEdges())
				.stream()
				.map(e -> Triple.of(e, e, e))
				.collect(Collectors.toSet());
	}

	@Override
	protected boolean isSubTreeOf(RDFResourceTree tree1, RDFResourceTree tree2) {
		return QueryTreeUtils.isSubsumedBy(tree1, tree2);
	}

	public static void main(String[] args) throws Exception {

		String data = "@base <http://foo.bar/> ." +
				"<a> <p1> <b> .\n" +
				"<c> <p2> <b> .\n" +
				"<c> <p3> <d> .";
		Model model = ModelFactory.createDefaultModel();
		RDFDataMgr.read(model, new StringReader(data), "NULL", Lang.TURTLE);

		model.write(System.out);

		String q = "" +
				"BASE <http://foo.bar/> ASK   \n" +
				"WHERE {\n" +
				"  <a>  ((<>|!<>)|^(<>|!<>))* <d> .\n" +
				"}";

		System.out.println(QueryExecutionFactory.create(q, model).execAsk());
//		System.out.println(ResultSetFormatter.asText(QueryExecutionFactory.create(q, model).execSelect()));

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

//		Node edge = NodeFactory.createURI("urn:p");
//		Node edgeInv = new NodeInv(edge);
//
//		RDFResourceTree tree1 = new RDFResourceTree(NodeFactory.createURI("urn:a"));
//		tree1.addChild(new RDFResourceTree(NodeFactory.createURI("urn:c")), edge);
//		tree1.addChild(new RDFResourceTree(NodeFactory.createURI("urn:d")), edgeInv);
//		System.out.println(tree1.getStringRepresentation());
//
//		RDFResourceTree tree2 = new RDFResourceTree(NodeFactory.createURI("urn:b"));
//		tree2.addChild(new RDFResourceTree(NodeFactory.createURI("urn:c")), edge);
//		tree2.addChild(new RDFResourceTree(NodeFactory.createURI("urn:d")), edgeInv);
//		System.out.println(tree2.getStringRepresentation());
//
//		RDFResourceTree lgg = lggGen.getLGG(tree1, tree2);
//		System.out.println("LGG");
//		System.out.println(lgg.getStringRepresentation());
//		System.out.println(QueryTreeUtils.toSPARQLQueryString(lgg));
	}

}
