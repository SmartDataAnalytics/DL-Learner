//package org.dllearner.common.index;
//
//import org.dllearner.kb.sparql.ExtractionDBCache;
//import org.dllearner.kb.sparql.SparqlEndpoint;
//
//import com.hp.hpl.jena.rdf.model.Model;
//
//public class SPARQLClassesIndex extends SPARQLIndex{
//
//	public SPARQLClassesIndex(SparqlEndpoint endpoint) {
//		super(endpoint);
//		init();
//	}
//	
//	public SPARQLClassesIndex(SparqlEndpoint endpoint, ExtractionDBCache cache) {
//		super(endpoint, cache);
//		init();
//	}
//	
//	public SPARQLClassesIndex(Model model) {
//		super(model);
//		init();
//	}
//	
//	private void init(){
//		super.queryTemplate = "SELECT DISTINCT ?uri WHERE {\n" +
//				"?s a ?uri.\n" + 
//				"?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label\n" +
//				"FILTER(REGEX(STR(?label), '%s'))}\n" +
//				"LIMIT %d OFFSET %d";
//		
//		super.queryWithLabelTemplate = "SELECT DISTINCT ?uri ?label WHERE {\n" +
//				"?s a ?uri.\n" + 
//				"?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label\n" +
//				"FILTER(REGEX(STR(?label), '%s'))}\n" +
//				"LIMIT %d OFFSET %d";
//	}
//}
