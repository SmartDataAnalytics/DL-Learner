package org.dllearner.autosparql.server.cache;

import org.dllearner.kb.sparql.SparqlEndpoint;

public class FillCacheScript {
	
	public static void main(String[] args){
		DBModelCacheExtended cache = new DBModelCacheExtended("dbpedia_cache", SparqlEndpoint.getEndpointDBpediaLiveAKSW());
		if(args.length != 0 && args[0].equals("-mysql")){
			cache.setUseMySQL(true);
		}
		cache.deleteCache();
		cache.createCache();
		cache.fillCache();
	}

}
