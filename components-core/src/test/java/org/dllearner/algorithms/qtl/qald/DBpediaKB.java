/**
 * 
 */
package org.dllearner.algorithms.qtl.qald;

import java.net.URL;

import org.dllearner.algorithms.qtl.util.PrefixCCPrefixMapping;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.utilities.PrefixCCMap;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.shared.PrefixMapping;

/**
 * @author Lorenz Buehmann
 *
 */
public class DBpediaKB extends KB {
	
	public DBpediaKB() throws Exception{
		id = "DBpedia";
		
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
		endpoint = new SparqlEndpoint(
//			new URL("http://akswnc3.informatik.uni-leipzig.de:8860/sparql"), 
				new URL("http://sake.informatik.uni-leipzig.de:8890/sparql"), 
				"http://dbpedia.org");
		ks = new SparqlEndpointKS(endpoint);
		ks.setCacheDir("./cache-qtl/qtl-qald-iswc2015-cache;mv_store=false");
		ks.init();
		
		reasoner = new SPARQLReasoner(ks);
		reasoner.setPrecomputeClassHierarchy(true);
		reasoner.setPrecomputeObjectPropertyHierarchy(true);
		reasoner.setPrecomputeDataPropertyHierarchy(true);
		reasoner.init();
		
		questionFiles = Lists.newArrayList(
				"org/dllearner/algorithms/qtl/qald-4_multilingual_train.xml",
				"org/dllearner/algorithms/qtl/qald-4_multilingual_test.xml"
				);
		
		baseIRI = "http://dbpedia.org/resource/";
		prefixMapping = PrefixMapping.Factory.create().withDefaultMappings(PrefixMapping.Standard);
		prefixMapping.setNsPrefix("dbo", "http://dbpedia.org/ontology/");
		prefixMapping.setNsPrefix("wiki", "http://wikidata.dbpedia.org/resource/");
		prefixMapping.setNsPrefix("odp-dul", "http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#");
		prefixMapping.setNsPrefix("schema", "http://schema.org/");
		
		
		
	}

}
