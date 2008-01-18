package org.dllearner.kb.sparql.query;

import org.dllearner.kb.sparql.configuration.SparqlEndpoint;

public class CachedSparqlQuery {

	String key;
	String queryString;
	Cache cache;
	SparqlEndpoint endpoint;
	SparqlQuery sparqlQuery;
	boolean debug_no_cache = false;

	public CachedSparqlQuery(SparqlEndpoint e, Cache c, String key,
			String queryString) {
		this.endpoint = e;
		this.cache = c;
		this.key = key;
		this.queryString = queryString;
		this.sparqlQuery = new SparqlQuery(queryString, e);
	}

	// URI u, String sparql
	@Deprecated
	public String send() {

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
