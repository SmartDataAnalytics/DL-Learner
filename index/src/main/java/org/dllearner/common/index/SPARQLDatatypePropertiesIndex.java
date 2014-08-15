//package org.dllearner.common.index;
//
//import org.dllearner.kb.sparql.SparqlEndpoint;
//
//import com.hp.hpl.jena.rdf.model.Model;
//
//public class SPARQLDatatypePropertiesIndex extends SPARQLPropertiesIndex{
//	
//	public SPARQLDatatypePropertiesIndex(SparqlEndpoint endpoint) {
//		super(endpoint);
//		init();
//	}
//	
//	public SPARQLDatatypePropertiesIndex(Model model) {
//		super(model);
//		init();
//	}
//	
//	public SPARQLDatatypePropertiesIndex(SPARQLIndex index) {
//		super(index);
//		init();
//	}
//	
//	private void init(){
//		super.queryTemplate = "SELECT ?uri WHERE {\n" +
////				"?s ?uri ?o.\n" + 
//				"?uri a owl:DatatypeProperty." + 
//				"?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label\n" +
//				"FILTER(REGEX(STR(?label), '%s', 'i'))}\n" +
//				"LIMIT %d OFFSET %d";
//		
//		super.queryWithLabelTemplate = "PREFIX owl:<http://www.w3.org/2002/07/owl#>  SELECT DISTINCT ?uri ?label WHERE {\n" +
////				"?s ?uri ?o.\n" + 
//				"?uri a owl:DatatypeProperty." + 
//				"?uri <http://www.w3.org/2000/01/rdf-schema#label> ?label\n" +
//				"FILTER(REGEX(STR(?label), '%s', 'i'))}\n" +
//				"LIMIT %d OFFSET %d";
//	}
//	
//
//}
