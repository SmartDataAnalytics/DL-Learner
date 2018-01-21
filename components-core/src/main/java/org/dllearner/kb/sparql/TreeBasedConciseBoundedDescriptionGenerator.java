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
package org.dllearner.kb.sparql;

import com.google.common.collect.Lists;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.impl.LiteralLabel;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.WebContent;
import org.apache.jena.riot.system.ErrorHandler;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.riot.web.HttpNames;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.util.FmtUtils;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.utilities.QueryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * {@inheritDoc}
 * @author Lorenz Buehmann
 *
 */
public class TreeBasedConciseBoundedDescriptionGenerator implements ConciseBoundedDescriptionGenerator{

	private static final Logger logger = LoggerFactory.getLogger(TreeBasedConciseBoundedDescriptionGenerator.class);
	private SparqlEndpoint endpoint;

	private Set<String> allowedPropertyNamespaces = new TreeSet<>();
	private Set<String> allowedObjectNamespaces = new TreeSet<>();

	private QueryExecutionFactory qef;

	private AtomicInteger inIndex = new AtomicInteger(0);
	private AtomicInteger outIndex = new AtomicInteger(0);
	private AtomicInteger predIndex = new AtomicInteger(0);

	private boolean useUnionOptimization = true;
	private boolean workaround = false;

	public TreeBasedConciseBoundedDescriptionGenerator(QueryExecutionFactory qef) {
		this.qef = qef;
	}

	public void setWorkaround(boolean workaround) {
		this.workaround = workaround;
	}

	public void setEndpoint(SparqlEndpoint endpoint) {
		this.endpoint = endpoint;
	}

	public Model getConciseBoundedDescription(LiteralLabel literal, CBDStructureTree structureTree) throws Exception {
		logger.trace("Computing CBD for {} ...", literal);
		long start = System.currentTimeMillis();
		String query = generateQuery(literal, structureTree);
		System.out.println(query);

		if(workaround) {
			return constructWithReplacement(endpoint, query);
		}

		try(QueryExecution qe = qef.createQueryExecution(query)) {
			Model model = qe.execConstruct();
			long end = System.currentTimeMillis();
			logger.trace("Got {} triples in {} ms.", model.size(), (end - start));
			return model;
		} catch(Exception e) {
			logger.error("CBD retrieval failed when using query\n{}", query);
			throw new Exception("CBD retrieval failed when using query\n" + query, e);
		}
	}

	/* (non-Javadoc)
             * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#getConciseBoundedDescription(java.lang.String, int, boolean)
             */
	public Model getConciseBoundedDescription(String resourceURI, CBDStructureTree structureTree) throws Exception {
		logger.trace("Computing CBD for {} ...", resourceURI);
		long start = System.currentTimeMillis();
		String query = generateQuery(resourceURI, structureTree);
		System.out.println(QueryFactory.create(query));

		if(workaround) {
			return constructWithReplacement(endpoint, query);
		}
		
		try(QueryExecution qe = qef.createQueryExecution(query)) {
			Model model = qe.execConstruct();
			long end = System.currentTimeMillis();
			logger.trace("Got {} triples in {} ms.", model.size(), (end - start));
			return model;
		} catch(Exception e) {
			throw new Exception("CBD retrieval failed when using query\n" + query, e);
		}
	}

	@Override
	public Model getConciseBoundedDescription(String resource, int depth, boolean withTypesForLeafs) {
		throw new NotImplementedException("please use getConciseBoundedDescription(String resourceURI, CBDStructureTree structureTree) instead.");
	}

	@Override
	public void setAllowedPropertyNamespaces(Set<String> namespaces) {
		this.allowedPropertyNamespaces.addAll(namespaces);
	}
	
	@Override
	public void setAllowedObjectNamespaces(Set<String> namespaces) {
		this.allowedObjectNamespaces.addAll(namespaces);
	}

	/**
	 * A SPARQL CONSTRUCT query is created, to get a RDF graph for the given example with a specific recursion depth.
	 * @param literal The example resource for which a CONSTRUCT query is created.
	 * @return the SPARQL query
	 */
	private String generateQuery(LiteralLabel literal, CBDStructureTree structureTree){
		reset();

		// get paths to leaf nodes
		List<List<CBDStructureTree>> pathsToLeafs = QueryTreeUtils.getPathsToLeafs(structureTree);

		StringBuilder query = new StringBuilder();
		String rootToken = FmtUtils.stringForNode(NodeFactory.createLiteral(literal));

		query.append("CONSTRUCT {\n");
		// the CONSTRUCT template
		append(query, structureTree, rootToken, true);
		query.append("} WHERE {\n");
		reset();
		// the query pattern
		append(query, structureTree, rootToken, false);
		query.append("}");

		return query.toString();
	}

	/**
	 * A SPARQL CONSTRUCT query is created, to get a RDF graph for the given example with a specific recursion depth.
	 * @param resource The example resource for which a CONSTRUCT query is created.
	 * @return the SPARQL query
	 */
	private String generateQuery(String resource, CBDStructureTree structureTree){
		reset();

		// get paths to leaf nodes
		List<List<CBDStructureTree>> pathsToLeafs = QueryTreeUtils.getPathsToLeafs(structureTree);

		StringBuilder query = new StringBuilder();
		String rootToken = "<" + resource + ">";

		query.append("CONSTRUCT {\n");
		// the CONSTRUCT template
		append(query, structureTree, rootToken, true);
		query.append("} WHERE {\n");
		reset();
		// the query pattern
		append(query, structureTree, rootToken, false);
		query.append("}");

		return query.toString();
	}

	private void append(StringBuilder query, CBDStructureTree tree, String rootVar, boolean isConstructTemplate) {
		// use optimization if enabled
		if(useUnionOptimization) {
			appendUnionOptimized2(query, tree, rootVar, isConstructTemplate);
			return;
		}

		tree.getChildren().forEach(child -> {
			// check if we have to put it into an OPTIONAL clause
			boolean optionalNeeded = !isConstructTemplate && child.isOutNode() && !tree.isRoot() && !tree.isInNode();

			// open OPTIONAL if necessary
			if(optionalNeeded) {
				query.append("OPTIONAL {");
			}

			// append triple pattern
			String var;
			if(child.isInNode()) {
				var = "?x_in" + inIndex.getAndIncrement();
				String predVar = "?p" + predIndex.getAndIncrement();
				query.append(String.format("%s %s %s .\n", var, predVar, rootVar));
			} else {
				var = "?x_out" + outIndex.getAndIncrement();
				String predVar = "?p" + predIndex.getAndIncrement();
				query.append(String.format("%s %s %s .\n", rootVar, predVar, var));
			}

			// recursively process the child node
			append(query, child, var, isConstructTemplate);

			// close OPTIONAL if necessary
			if(optionalNeeded) {
				query.append("}");
			}
		});
	}

	private void appendUnionOptimized(StringBuilder query, CBDStructureTree tree, String rootVar, boolean isConstructTemplate) {
		List<List<CBDStructureTree>> paths = QueryTreeUtils.getPathsToLeafs(tree);

		List<String> tpClusters = paths.stream().map(path -> {
			StringBuilder currentVar = new StringBuilder(rootVar);
			StringBuilder tps = new StringBuilder();
			AtomicBoolean lastOut = new AtomicBoolean(false);
			StringBuilder appendix = new StringBuilder();
			path.forEach(node -> {
				boolean optionalNeeded = !isConstructTemplate && lastOut.get() && node.isOutNode();

				// open OPTIONAL if necessary
				if(optionalNeeded) {
					tps.append("OPTIONAL {");
					appendix.append("}");
				}

				// append triple pattern
				String var;
				if (node.isInNode()) {
					var = "?x_in" + inIndex.getAndIncrement();
					String predVar = "?p" + predIndex.getAndIncrement();
					tps.append(String.format("%s %s %s .\n", var, predVar, currentVar.toString()));
				} else {
					var = "?x_out" + outIndex.getAndIncrement();
					String predVar = "?p" + predIndex.getAndIncrement();
					tps.append(String.format("%s %s %s .\n", currentVar.toString(), predVar, var));
					lastOut.set(true);
				}
				currentVar.setLength(0);
				currentVar.append(var);
			});

			// add closing braces for OPTIONAL if used
			tps.append(appendix);

			return tps.toString();
		}).collect(Collectors.toList());

		String queryPart = tpClusters.stream()
				.map(s -> isConstructTemplate ? s : "{" + s + "}")
				.collect(Collectors.joining(isConstructTemplate ? "" : " UNION "));
		query.append(queryPart);
	}

	private void appendUnionOptimized2(StringBuilder query, CBDStructureTree tree, String rootVar, boolean isConstructTemplate) {
		List<List<CBDStructureTree>> paths = QueryTreeUtils.getPathsToLeafs(tree);

		// get all sub-paths
		paths = paths.stream().flatMap(path -> {
							   List<List<CBDStructureTree>> subPaths = new ArrayList<>();
							   for (int length = 1; length <= path.size(); length++) {
								   subPaths.add(path.subList(0, length));
							   }
							   return subPaths.stream();
						   }).collect(Collectors.toList());

		String rdfTypeFilter = "FILTER(%s != <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>)\n";


		List<String> tpClusters = paths.stream().map(path -> {
			StringBuilder currentVar = new StringBuilder(rootVar);
			StringBuilder tps = new StringBuilder();
			AtomicBoolean lastOut = new AtomicBoolean(false);

				path.forEach(node -> {
					// append triple pattern
					String var;
					if (node.isInNode()) {
						if(lastOut.get() && !isConstructTemplate) {
							tps.append(String.format(rdfTypeFilter, "?p_" + (predIndex.get() - 1)));
						}
						var = "?x_in" + inIndex.getAndIncrement();
						String predVar = "?p" + predIndex.getAndIncrement();
						tps.append(String.format("%s %s %s .\n", var, predVar, currentVar.toString()));

					} else {
						var = "?x_out" + outIndex.getAndIncrement();
						String predVar = "?p" + predIndex.getAndIncrement();
						tps.append(String.format("%s %s %s .\n", currentVar.toString(), predVar, var));
						lastOut.set(true);
					}
					currentVar.setLength(0);
					currentVar.append(var);
				});

				return tps.toString();
			}).collect(Collectors.toList());


		String queryPart = tpClusters.stream()
				.map(s -> isConstructTemplate ? s : "{" + s + "}")
				.collect(Collectors.joining(isConstructTemplate ? "" : " UNION "));
		query.append(queryPart);
	}

	/**
	 * Reset variables indices
	 */
	private void reset() {
		inIndex = new AtomicInteger(0);
		outIndex = new AtomicInteger(0);
		predIndex = new AtomicInteger(0);
	}

	@Override
	public void setIgnoredProperties(Set<String> properties) {

	}

	public void setUseUnionOptimization(boolean useUnionOptimization) {
		this.useUnionOptimization = useUnionOptimization;
	}

	public static void main(String[] args) throws Exception {
		String query = "PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
				"PREFIX  owl:  <http://www.w3.org/2002/07/owl#>\n" +
				"PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
				"PREFIX  foaf: <http://xmlns.com/foaf/0.1/>\n" +
				"\n" +
				"SELECT DISTINCT  ?uri\n" +
				"WHERE\n" +
				"  { ?x  <http://dbpedia.org/ontology/director>  <http://dbpedia.org/resource/William_Shatner> ;\n" +
				"        <http://dbpedia.org/ontology/starring>  ?uri\n" +
				"  }";

		query = "PREFIX  dbo:  <http://dbpedia.org/ontology/>\n" +
				"PREFIX  :     <http://dbpedia.org/resource/>\n" +
				"\n" +
				"SELECT DISTINCT  ?uri\n" +
				"WHERE\n" +
				"  { :The_Three_Dancers\n" +
				"              dbo:author    ?person .\n" +
				"    ?person   dbo:movement  ?uri\n" +
				"  }";
		query = "PREFIX  dbo:  <http://dbpedia.org/ontology/>\n" +
				"PREFIX  :     <http://dbpedia.org/resource/>\n" +
				"\n" +
				"SELECT DISTINCT  ?uri\n" +
				"WHERE\n" +
				"  { ?uri dbo:author    ?person . \n" +
				"    ?person   dbo:movement  :Test\n ." +
				"?in_0 dbo:starring    ?uri . ?in_1 dbo:starring    ?in_0 . ?in_0 dbo:book    ?o_0 ." +
				"  }";
		CBDStructureTree cbdTree = QueryUtils.getOptimalCBDStructure(QueryFactory.create(query));
		cbdTree = CBDStructureTree.fromTreeString("root:[in:[out:[]],out:[in:[],out:[out:[]]]]");

		System.out.println(cbdTree.toStringVerbose());
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
		endpoint = SparqlEndpoint.create("http://sake.informatik.uni-leipzig.de:8890/sparql", "http://dbpedia.org");
		SparqlEndpointKS ks = new SparqlEndpointKS(endpoint);
		ks.setQueryDelay(0);
		ks.setUseCache(false);
		ks.setRetryCount(0);
		ks.init();

//		QueryExecutionFactory qef = ks.getQueryExecutionFactory();
//
//		String q = "CONSTRUCT {\n" +
//				"<http://www4.wiwiss.fu-berlin.de/sider/resource/drugs/2232> ?p0 ?x_out0 .\n" +
//				"} WHERE {\n" +
//				"{<http://www4.wiwiss.fu-berlin.de/sider/resource/drugs/2232> ?p0 ?x_out0 .\n" +
//				"}}";
//		Model model = ModelFactory.createDefaultModel();
//		// Parser to first error or warning.
//		ErrorHandler errHandler = ErrorHandlerFactory.errorHandlerWarn;
//		model.getReader().setProperty("error-mode","lax");
//
//		System.out.println(model.size());
//
//
//		System.exit(0);
		TreeBasedConciseBoundedDescriptionGenerator cbdGen = new TreeBasedConciseBoundedDescriptionGenerator(ks.getQueryExecutionFactory());
		Model cbd = cbdGen.getConciseBoundedDescription("http://dbpedia.org/resource/Dan_Gauthier", cbdTree);
		System.out.println(cbd.size());

		cbdGen.setUseUnionOptimization(false);
		cbd = cbdGen.getConciseBoundedDescription("http://dbpedia.org/resource/Dan_Gauthier", cbdTree);
		System.out.println(cbd.size());
//		cbd.write(System.out, "NTRIPLES");
	}

	private Model constructWithReplacement(SparqlEndpoint endpoint, String query) throws Exception{
		QueryEngineHTTP qe = new QueryEngineHTTP(endpoint.getURL().toString(), query);
		qe.setDefaultGraphURIs(endpoint.getDefaultGraphURIs());
		String request = qe.toString().replace("GET ", "");

		URL url = new URL(request);
		java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.addRequestProperty(HttpNames.hAccept, WebContent.contentTypeRDFXML);
		try(BufferedReader rdr = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
			Model model = ModelFactory.createDefaultModel();
			String buf = null;
			StringBuilder doc = new StringBuilder();
			while ((buf = rdr.readLine()) != null) {
				// Apply regex on buf
				if(buf.contains("&#")) {
					buf = buf.replace("&#", "");
				}
				// build output
				doc.append(buf);
			}
			try(InputStream is = new ByteArrayInputStream(doc.toString().getBytes(StandardCharsets.UTF_8))) {
				model.read(is, null);
			}
			return model;
		}
	}

}
