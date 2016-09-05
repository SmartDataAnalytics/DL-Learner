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

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.utilities.QueryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * {@inheritDoc}
 * @author Lorenz Buehmann
 *
 */
public class TreeBasedConciseBoundedDescriptionGenerator implements ConciseBoundedDescriptionGenerator{

	private static final Logger logger = LoggerFactory.getLogger(TreeBasedConciseBoundedDescriptionGenerator.class);

	private Set<String> allowedPropertyNamespaces = new TreeSet<>();
	private Set<String> allowedObjectNamespaces = new TreeSet<>();

	private QueryExecutionFactory qef;

	AtomicInteger inIndex = new AtomicInteger(0);
	AtomicInteger outIndex = new AtomicInteger(0);
	AtomicInteger predIndex = new AtomicInteger(0);

	public TreeBasedConciseBoundedDescriptionGenerator(QueryExecutionFactory qef) {
		this.qef = qef;
	}


	/* (non-Javadoc)
	 * @see org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator#getConciseBoundedDescription(java.lang.String, int, boolean)
	 */
	public Model getConciseBoundedDescription(String resourceURI, CBDStructureTree structureTree) throws Exception {
		logger.trace("Computing CBD for {} ...", resourceURI);
		long start = System.currentTimeMillis();
		String query = generateQuery(resourceURI, structureTree);
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
	public Model getConciseBoundedDescription(String resourceURI, int depth, boolean withTypesForLeafs) {
		throw new NotImplementedException("please use getConciseBoundedDescription(String resourceURI, CBDStructureTree structureTree) instead.");
	}

	@Override
	public void addAllowedPropertyNamespaces(Set<String> namespaces) {
		this.allowedPropertyNamespaces.addAll(namespaces);
	}
	
	@Override
	public void addAllowedObjectNamespaces(Set<String> namespaces) {
		this.allowedObjectNamespaces.addAll(namespaces);
	}
	
	/**
	 * A SPARQL CONSTRUCT query is created, to get a RDF graph for the given example with a specific recursion depth.
	 * @param resource The example resource for which a CONSTRUCT query is created.
	 * @return the SPARQL query
	 */
	private String generateQuery(String resource, CBDStructureTree structureTree){
		reset();
		StringBuilder sb = new StringBuilder();
		sb.append("CONSTRUCT {\n");
		append(sb, structureTree, "<" + resource + ">");
		sb.append("} WHERE {\n");
		reset();
		append(sb, structureTree, "<" + resource + ">");
		sb.append("}");

		return sb.toString();
	}

	private void append(StringBuilder query, CBDStructureTree tree, String rootVar) {
		List<CBDStructureTree> children = tree.getChildren();
		for (CBDStructureTree child : children) {
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
			append(query, child, var);
		}
	}

	private void reset() {
		inIndex = new AtomicInteger(0);
		outIndex = new AtomicInteger(0);
		predIndex = new AtomicInteger(0);
	}

	@Override
	public void addPropertiesToIgnore(Set<String> properties) {

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
		CBDStructureTree cbdTree = QueryUtils.getOptimalCBDStructure(QueryFactory.create(query));
		System.out.println(cbdTree.toStringVerbose());
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
		SparqlEndpointKS ks = new SparqlEndpointKS(endpoint);
		ks.init();
		TreeBasedConciseBoundedDescriptionGenerator cbdGen = new TreeBasedConciseBoundedDescriptionGenerator(ks.getQueryExecutionFactory());
		Model cbd = cbdGen.getConciseBoundedDescription("http://dbpedia.org/resource/Dan_Gauthier", cbdTree);
		System.out.println(cbd.size());
//		cbd.write(System.out, "NTRIPLES");
	}

	

}
