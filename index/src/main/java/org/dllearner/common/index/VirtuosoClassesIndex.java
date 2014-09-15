//package org.dllearner.common.index;
//
//import org.dllearner.kb.sparql.ExtractionDBCache;
//import org.dllearner.kb.sparql.SparqlEndpoint;
//
//import com.hp.hpl.jena.rdf.model.Model;
//
//public class VirtuosoClassesIndex extends SPARQLIndex{
//
//	public VirtuosoClassesIndex(SparqlEndpoint endpoint) {
//		super(endpoint);
//		init();
//	}
//	
//	public VirtuosoClassesIndex(SparqlEndpoint endpoint, ExtractionDBCache cache) {
//		super(endpoint, cache);
//		init();
//	}
//	
//	public VirtuosoClassesIndex(Model model) {
//		super(model);
//		init();
//	}
//	
//	private void init(){
//		super.queryTemplate = "SELECT DISTINCT ?uri WHERE {\n" +
//				"{?s a ?uri} UNION {?uri a owl:Class}.\n" + 
////				"?s a ?uri.\n" + 
//				"?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label.\n" +
//				"?label bif:contains '\"%s\"'}\n" +
//				"LIMIT %d OFFSET %d";
//		
//		super.queryWithLabelTemplate = "SELECT DISTINCT ?uri ?label WHERE {\n" +
//				"{?s a ?uri} UNION {?uri a owl:Class}.\n" + 
////				"?s a ?uri.\n" + 
//				"?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label.\n" +
//				"?label bif:contains '\"%s\"'}\n" +
//				"LIMIT %d OFFSET %d";
//	}
//}
