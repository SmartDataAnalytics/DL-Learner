package org.dllearner.algorithms.qtl.util.filters;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.kb.SparqlEndpointKS;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.util.NodeComparator;

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
			Files.write(sb.toString(), new File("dbpedia_meaningless_properties.txt"), Charsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
