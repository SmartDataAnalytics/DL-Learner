package org.dllearner.kb.sparql.query;

import org.dllearner.kb.sparql.configuration.SparqlEndpoint;

/**
 * Does the same as SparqlQuery, but uses the cache. key should be an uri or
 * something that can be mapped to a file see cache
 * 
 * @author Jens Lehmann
 * 
 */
public class CachedSparqlQuery {

	String key;
	String queryString;
	Cache cache;
	SparqlEndpoint endpoint;
	SparqlQuery sparqlQuery;
	boolean debug_no_cache = false;

	/**
	 * key should be an uri or something that can be mapped to a file see cache
	 * 
	 * @param endpoint
	 * @param cache
	 * @param key
	 * @param queryString
	 */
	public CachedSparqlQuery(SparqlEndpoint endpoint, Cache cache, String key,
			String queryString) {
		this.endpoint = endpoint;
		this.cache = cache;
		this.key = key;
		this.queryString = queryString;
		this.sparqlQuery = new SparqlQuery(queryString, endpoint);
	}

	/**
	 * sends a query and returns XML using cache
	 * 
	 * @return String xml
	 */
	public String getAsXMLString() {
		String FromCache = cache.get(key, queryString);
		if (debug_no_cache) {
			FromCache = null;
		}
		String xml = null;
		// if not in cache get it from EndPoint
		if (FromCache == null) {
			// configuration.increaseNumberOfuncachedSparqlQueries();

			xml = this.sparqlQuery.getAsXMLString();
			// sendAndReceiveSPARQL(sparql);

			// p(sparql);
			// System.out.println(xml);
			if (!debug_no_cache) {
				cache.put(key, queryString, xml);
			}
			// System.out.print("\n");
		} else {
			// configuration.increaseNumberOfCachedSparqlQueries();
			xml = FromCache;
			// System.out.println("FROM CACHE");
		}

		return xml;
	}

	public void stop() {
		this.sparqlQuery.stop();

	}

	public boolean isRunning() {
		return this.sparqlQuery.isRunning();
	}

}
