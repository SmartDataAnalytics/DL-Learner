package org.dllearner.kb.sparql.query;

import org.dllearner.kb.sparql.configuration.SparqlEndpoint;

public class CachedSparqlQueryTest extends SparqlQuery {

	private Cache cache;
	private String key;
	
	public CachedSparqlQueryTest(SparqlEndpoint endpoint, Cache cache, String key,
			String queryString) {
		super(queryString,endpoint);
		this.cache = cache;
		this.key = key;
	}
	
	public void send()
	{
		String FromCache = cache.get(key, queryString);
		
		// if not in cache get it from EndPoint
		if (FromCache == null) {
			super.send();
			this.cache.put(key, queryString, getAsJSON());
		} else {
			this.rs=SparqlQuery.JSONtoResultSet(FromCache);
			System.out.println("FROM CACHE");
		}
	}
}
