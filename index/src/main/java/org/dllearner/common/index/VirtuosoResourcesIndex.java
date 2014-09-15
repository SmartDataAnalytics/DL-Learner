//package org.dllearner.common.index;
//
//import org.dllearner.kb.sparql.ExtractionDBCache;
//import org.dllearner.kb.sparql.SparqlEndpoint;
//
//import com.hp.hpl.jena.rdf.model.Model;
//
//public class VirtuosoResourcesIndex extends SPARQLIndex{
//	
//	public VirtuosoResourcesIndex(SparqlEndpoint endpoint) {
//		this(endpoint, null);
//	}
//	
//	public VirtuosoResourcesIndex(Model model) {
//		super(model);
//		init();
//	}
//	
//	public VirtuosoResourcesIndex(VirtuosoResourcesIndex index) {
//		super(index);
//	}
//	
//	public VirtuosoResourcesIndex(SparqlEndpoint endpoint, ExtractionDBCache cache) {
//		super(endpoint, cache);
//		init();
//	}
//	
//	private void init(){
//		super.queryTemplate = "SELECT DISTINCT ?uri WHERE {\n" +
//				"?uri a ?type.\n" + 
//				"?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label.\n" +
//				"?label bif:contains '\"%s\"'}\n" +
//				"LIMIT %d OFFSET %d";
//		
//		super.queryWithLabelTemplate = "SELECT DISTINCT ?uri ?label WHERE {\n" +
//				"?uri a ?type.\n" + 
//				"?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label.\n" +
//				"?label bif:contains '\"%s\"'}\n" +
//				"LIMIT %d OFFSET %d";
//	}
//	
//	
//}
