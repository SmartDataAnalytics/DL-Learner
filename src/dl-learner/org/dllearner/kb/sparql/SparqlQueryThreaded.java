package org.dllearner.kb.sparql;

import com.hp.hpl.jena.query.ResultSet;

/**
 * The class is used for threaded querying of a Sparql Endpoint.
 * @author Sebastian Knappe
 *
 */
public class SparqlQueryThreaded {
	private boolean isRunning=false;
	private Cache cache;
	private SparqlQuery query;
	
	public SparqlQueryThreaded(Cache cache, SparqlQuery query)
	{
		this.cache=cache;
		this.query=query;
	}
	
	public void stop() {
		query.getExecution().abort();
		isRunning = false;
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	
	public void send()
	{
		isRunning=true;
		cache.executeSparqlQuery(query);
		isRunning=false;
	}
	
	public SparqlQuery getSparqlQuery(){
		return query;
	}
}
