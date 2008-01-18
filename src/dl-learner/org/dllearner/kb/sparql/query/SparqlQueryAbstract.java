package org.dllearner.kb.sparql.query;

import org.dllearner.kb.sparql.configuration.SpecificSparqlEndpoint;

	public abstract class SparqlQueryAbstract {
	private boolean isRunning = false;
	boolean print_flag=false;
	SpecificSparqlEndpoint specificSparqlEndpoint;
	
	public SparqlQueryAbstract(SpecificSparqlEndpoint endpoint) {
		this.specificSparqlEndpoint=endpoint;
	}
	
	public void send() {
		isRunning = true;
		
		// ... send query 
		// ... check periodically whether isRunning is still true, if not
		// abort the query
	}
	
	public void stop() {
		isRunning = false;
	}

	public boolean isRunning() {
		return isRunning;
	}
	
	
	public abstract String getAsXMLString(String queryString);
	
	public void p(String str){
		if(print_flag){
			System.out.println(str);
		}
	}
	
}
