//package org.dllearner.common.index;
//
//import org.dllearner.kb.sparql.SparqlEndpoint;
//
//import com.hp.hpl.jena.rdf.model.Model;
//
//public class VirtuosoDatatypePropertiesIndex extends SPARQLPropertiesIndex{
//	
//	public VirtuosoDatatypePropertiesIndex(SparqlEndpoint endpoint) {
//		super(endpoint);
//		init();
//	}
//	
//	public VirtuosoDatatypePropertiesIndex(Model model) {
//		super(model);
//		init();
//	}
//	
//	public VirtuosoDatatypePropertiesIndex(SPARQLIndex index) {
//		super(index);
//		init();
//	}
//	
//	private void init(){
//		super.queryTemplate = "SELECT ?uri WHERE {\n" +
////				"?s ?uri ?o.\n" + 
//				"?uri a owl:DatatypeProperty.\n" + 
//				"?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label.\n" +
//				"?label bif:contains '\"%s\"'}\n" +
//				"LIMIT %d OFFSET %d";
//		
//		super.queryWithLabelTemplate = "PREFIX owl:<http://www.w3.org/2002/07/owl#>  SELECT DISTINCT ?uri ?label WHERE {\n" +
////				"?s ?uri ?o.\n" + 
//				"?uri a owl:DatatypeProperty.\n" + 
//				"?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label.\n" +
//				"?label bif:contains '\"%s\"'}\n" +
//				"LIMIT %d OFFSET %d";
//	}
//	
//
//}
