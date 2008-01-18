package org.dllearner.kb.sparql.query;

import java.net.URI;

import org.dllearner.kb.sparql.configuration.SparqlEndpoint;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

public class CachedSparqlQuery {

//SpecificSparqlEndpoint specificSparqlEndpoint;
Cache cache;
SparqlQuery sparqlQuery;
boolean debug_no_cache=false;

	public CachedSparqlQuery(SparqlEndpoint endpoint,Cache c) {
		//this.specificSparqlEndpoint=endpoint;
		this.sparqlQuery=new SparqlQuery(endpoint);
		this.cache=c;
		
	}
	public CachedSparqlQuery(SparqlQuery sparqlQuery,Cache c) {
		
		this.sparqlQuery=sparqlQuery;
		this.cache=c;
		
	}

	public String getAsXMLString(URI u, String sparql){
		String FromCache = cache.get(u.toString(), sparql);
		if(debug_no_cache) {
			FromCache=null;
			}
		String xml = null;
		// if not in cache get it from EndPoint
		if (FromCache == null) {
			//configuration.increaseNumberOfuncachedSparqlQueries();
			
				xml = this.sparqlQuery.getAsXMLString(sparql); 
					//sendAndReceiveSPARQL(sparql);
			
			//p(sparql);
			// System.out.println(xml);
			if(!debug_no_cache) {
				cache.put(u.toString(), sparql, xml);
			}
			//System.out.print("\n");
		} else {
			//configuration.increaseNumberOfCachedSparqlQueries();
			xml = FromCache;
			//System.out.println("FROM CACHE");
		}
		
		return xml;
	}
}
