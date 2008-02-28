package org.dllearner.kb.sparql;

import com.hp.hpl.jena.query.ResultSet;

/**
 * The class is used for threaded querying of a Sparql Endpoint.
 * @author Sebastian Knappe
 *
 */
public class SparqlQueryThreaded {
	private Cache cache;
	private SparqlQuery query;
	private String result;
	
	public SparqlQueryThreaded(Cache cache, SparqlQuery query)
	{
		this.cache=cache;
		this.query=query;
		this.result=null;
	}
	
	public void stop() {
		query.getExecution().abort();
		result=null;
	}
	
	public boolean isRunning() {
		return result==null;
	}
	
	public void send()
	{
		result=cache.executeSparqlQuery(query);
	}
	
	public SparqlQuery getSparqlQuery(){
		return query;
	}
	
	public String getResult(){
		return result;
	}
}
