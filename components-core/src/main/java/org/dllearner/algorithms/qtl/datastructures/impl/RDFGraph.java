package org.dllearner.algorithms.qtl.datastructures.impl;

import com.google.common.collect.Sets;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionHttpWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.dllearner.algorithms.qtl.operations.MinimumSteinerTreeApproximation;
import org.dllearner.kb.sparql.*;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.graph.DirectedWeightedPseudograph;

import java.util.Calendar;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Lorenz Buehmann
 */
public class RDFGraph extends DirectedWeightedPseudograph<Node, RDFGraph.Edge> {

	public static class Edge extends DefaultEdge {
		private Node v1;
		private Node v2;
		private Node label;

		public Edge(Node v1, Node v2, Node label) {
			this.v1 = v1;
			this.v2 = v2;
			this.label = label;
		}

		public Node getV1() {
			return v1;
		}

		public Node getV2() {
			return v2;
		}

		public String toString() {
			return label.toString();
		}
	}


	private RDFGraph(Class<Edge> edgeClass) {
		super(edgeClass);
	}

	public RDFGraph() {
		this(Edge.class);
	}


	public static RDFGraph fromModel(Model model) {
		RDFGraph g = new RDFGraph();

		model.listStatements().forEachRemaining(st -> {
			Node s = st.getSubject().asNode();
			Node t = st.getObject().asNode();
			g.addVertex(s);
			g.addVertex(t);
			g.addEdge(s, t, new Edge(s, t, st.getPredicate().asNode()));
		});

		return g;
	}

	public static void main(String[] args) throws Exception {
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();

		QueryExecutionFactory qef = FluentQueryExecutionFactory.
				http(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs())
				.config()
				.withPostProcessor(qe -> ((QueryEngineHTTP) ((QueryExecutionHttpWrapper) qe).getDecoratee()).setModelContentType(
						WebContent.contentTypeRDFXML))
				.withDelay(50, TimeUnit.MILLISECONDS)
				.withPagination(10000)
				.end()
				.create();

		TreeBasedConciseBoundedDescriptionGenerator cbdGen = new TreeBasedConciseBoundedDescriptionGenerator(qef);
		CBDStructureTree cbdStructureTree = CBDStructureTree.fromTreeString("root:[out:[out:[]],in:[out:[]]]");

		Model cbd1 = cbdGen.getConciseBoundedDescription("http://dbpedia.org/resource/Tutta_Rolf", cbdStructureTree);
		Model cbd2 = cbdGen.getConciseBoundedDescription("http://dbpedia.org/resource/Helsinki", cbdStructureTree);

		Model cbd = cbd1.union(cbd2);

		RDFGraph g = RDFGraph.fromModel(cbd);
		System.out.println("|V|=" + g.vertexSet().size() + ", |E|=" + g.edgeSet().size());
//		g.vertexSet().forEach(System.out::println);

		Node startVertex = NodeFactory.createURI("http://dbpedia.org/resource/Tutta_Rolf");

		Set<Node> vertices = Sets.newHashSet("http://dbpedia.org/resource/Tutta_Rolf",
											 "http://dbpedia.org/resource/Helsinki")
				.stream().map(NodeFactory::createURI).collect(Collectors.toSet());


		long s1 = System.currentTimeMillis();
		Tree<Node> steinerTree = MinimumSteinerTreeApproximation.approximateSteinerTree(g, startVertex, vertices);
		long s2 = System.currentTimeMillis();
		System.out.println(steinerTree);
		System.out.println(s2 - s1);

	}

}
