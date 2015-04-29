/**
 * 
 */
package org.dllearner.algorithms.qtl.experiments;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.dllearner.algorithms.qtl.util.StopURIsDBpedia;
import org.dllearner.algorithms.qtl.util.StopURIsOWL;
import org.dllearner.algorithms.qtl.util.StopURIsRDFS;
import org.dllearner.algorithms.qtl.util.StopURIsSKOS;
import org.dllearner.algorithms.qtl.util.filters.NamespaceDropStatementFilter;
import org.dllearner.algorithms.qtl.util.filters.ObjectDropStatementFilter;
import org.dllearner.algorithms.qtl.util.filters.PredicateDropStatementFilter;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.util.iterator.Filter;

/**
 * @author Lorenz Buehmann
 *
 */
public class DBpediaDataset extends Dataset {
	
	static SparqlEndpoint endpoint;
	
	static {
		try {
			endpoint = SparqlEndpoint.getEndpointDBpedia();
			endpoint = new SparqlEndpoint(
//			new URL("http://akswnc3.informatik.uni-leipzig.de:8860/sparql"), 
					new URL("http://sake.informatik.uni-leipzig.de:8890/sparql"), 
					"http://dbpedia.org");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public DBpediaDataset() {
		// set KS
		try {
			ks = new SparqlEndpointKS(endpoint);
			ks.setCacheDir("./cache-qtl/qtl-qald-iswc2015-cache;mv_store=false");
			ks.init();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}
		
		// load SPARQL queries
		try {
			URL url = this.getClass().getClassLoader().getResource("org/dllearner/algorithms/qtl/iswc2015-benchmark-queries.txt");
			File file = new File(url.toURI()); 
			sparqlQueries = Files.readLines(file, Charsets.UTF_8);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		reasoner = new SPARQLReasoner(ks);
		try {
			reasoner.init();
		} catch (ComponentInitException e) {
			e.printStackTrace();
		}
		
		baseIRI = "http://dbpedia.org/resource/";
		prefixMapping = PrefixMapping.Factory.create().withDefaultMappings(PrefixMapping.Standard);
		prefixMapping.setNsPrefix("dbo", "http://dbpedia.org/ontology/");
		prefixMapping.setNsPrefix("wiki", "http://wikidata.dbpedia.org/resource/");
		prefixMapping.setNsPrefix("odp-dul", "http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#");
		prefixMapping.setNsPrefix("schema", "http://schema.org/");
	}
	
	public List<Filter<Statement>> getQueryTreeFilters() {
		return Lists.<Filter<Statement>>newArrayList(
			new PredicateDropStatementFilter(StopURIsDBpedia.get()),
			new ObjectDropStatementFilter(StopURIsDBpedia.get()),
			new PredicateDropStatementFilter(StopURIsRDFS.get()),
			new PredicateDropStatementFilter(StopURIsOWL.get()),
			new ObjectDropStatementFilter(StopURIsOWL.get()),
			new PredicateDropStatementFilter(StopURIsSKOS.get()),
			new ObjectDropStatementFilter(StopURIsSKOS.get()),
			new NamespaceDropStatementFilter(
			Sets.newHashSet(
					"http://dbpedia.org/property/", 
					"http://purl.org/dc/terms/",
					"http://dbpedia.org/class/yago/"
	//				,FOAF.getURI()
					)
					),
					new PredicateDropStatementFilter(
							Sets.newHashSet(
									"http://www.w3.org/2002/07/owl#equivalentClass", 
									"http://www.w3.org/2002/07/owl#disjointWith"))
			);
	}

}
