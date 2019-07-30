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
package org.dllearner.algorithms.qtl.util.filters;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.util.NodeComparator;
import org.dllearner.kb.SparqlEndpointKS;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Lorenz Buehmann
 *
 */
public class PredicateExistenceFilterDBpedia extends PredicateExistenceFilter{
	
	private String PATH = "org/dllearner/algorithms/qtl/dbpedia_meaningless_properties.txt";
	private SparqlEndpointKS ks;
	
	public PredicateExistenceFilterDBpedia(SparqlEndpointKS ks) {
		this.ks = ks;
		Set<Node> existentialMeaninglessProperties = new TreeSet<>(new NodeComparator());
		
		try {
			List<String> lines = Files.readLines(new File(this.getClass().getClassLoader().getResource(PATH).toURI()), Charsets.UTF_8);
			for (String line : lines) {
				if(!line.trim().isEmpty() && !line.startsWith("#")) {
					existentialMeaninglessProperties.add(NodeFactory.createURI(line.trim()));
				}
			}
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
		setExistentialMeaninglessProperties(existentialMeaninglessProperties);
	}
	
	private void analyze() {
		Set<Node> existentialMeaninglessProperties = new TreeSet<>(new NodeComparator());
		
		StringBuilder sb = new StringBuilder();
		// check data properties
		String query = "SELECT ?p ?range WHERE {?p a owl:DatatypeProperty . ?p rdfs:range ?range .}";
		
		QueryExecution qe = ks.getQueryExecutionFactory().createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			
			Resource property = qs.getResource("p");
			Resource range = qs.getResource("range");
			
//			if(range.equals(XSD.xdouble)) {
				existentialMeaninglessProperties.add(property.asNode());
//			}
		}
		qe.close();
		for (Node p : existentialMeaninglessProperties) {
			sb.append(p).append("\n");
		}
		existentialMeaninglessProperties.clear();
		sb.append("\n\n");
		
		// check object properties
		query = "SELECT ?p WHERE {?p a owl:ObjectProperty .}";

		qe = ks.getQueryExecutionFactory().createQueryExecution(query);
		rs = qe.execSelect();
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();

			Resource property = qs.getResource("p");
			existentialMeaninglessProperties.add(property.asNode());
			
		}
		qe.close();
		
		for (Node p : existentialMeaninglessProperties) {
			sb.append(p).append("\n");
		}
		try {
			Files.asCharSink(new File("dbpedia_meaningless_properties.txt"), Charsets.UTF_8).write(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
