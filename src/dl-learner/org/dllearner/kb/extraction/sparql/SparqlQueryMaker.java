package org.dllearner.kb.extraction.sparql;

import org.dllearner.kb.extraction.SparqlQueryType;



public class SparqlQueryMaker {
	
	private SparqlQueryType SparqlQueryType;
	
	public SparqlQueryMaker(SparqlQueryType SparqlQueryType){
		this.SparqlQueryType=SparqlQueryType;
	}
	
	public String makeQueryUsingFilters(String subject){
		String lineend="\n";
		
		String Filter="";
		if(!this.SparqlQueryType.isLiterals())Filter+="!isLiteral(?object))";
		for (String  p : this.SparqlQueryType.getPredicatefilterlist()) {
			Filter+=lineend + filterPredicate(p);
		}
		for (String  o : this.SparqlQueryType.getObjectfilterlist()) {
			Filter+=lineend + filterObject(o);
		}
		
		
		String ret=		
		"SELECT * WHERE { "+lineend +
		"<"+
		subject+
		"> ?predicate ?object. "+ lineend+
		"FILTER( "+lineend +
		"(" +Filter+").}";
		//System.out.println(ret);
		return ret;
	}
	
	
	public String filterObject(String ns){
		 return "&&( !regex(str(?object), '"+ns+"') )";
	}
	public String filterPredicate(String ns){
		 return "&&( !regex(str(?predicate), '"+ns+"') )";
	}
}
