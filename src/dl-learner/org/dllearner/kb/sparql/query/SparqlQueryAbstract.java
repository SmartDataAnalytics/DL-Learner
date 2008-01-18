package org.dllearner.kb.sparql.query;

import org.dllearner.kb.sparql.configuration.SparqlEndpoint;

	public abstract class SparqlQueryAbstract {
	boolean print_flag=false;
	SparqlEndpoint specificSparqlEndpoint;
	
	public SparqlQueryAbstract(SparqlEndpoint endpoint) {
		this.specificSparqlEndpoint=endpoint;
	}
	
	public abstract String getAsXMLString(String queryString);
	//public abstract String getAsXMLString(String queryString);
	
	public void p(String str){
		if(print_flag){
			System.out.println(str);
		}
	}
	
}
