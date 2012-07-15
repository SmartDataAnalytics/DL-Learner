package org.dllearner.algorithm.tbsl.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.keyvalue.DefaultMapEntry;
import org.dllearner.kb.sparql.ExtractionDBCache;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.utilities.MapUtils;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class UnknownPropertyHelper {
	
	public enum SymPropertyDirection {
		VAR_LEFT, VAR_RIGHT, UNKNOWN
	}
	
	private SparqlEndpoint endpoint;
	private ExtractionDBCache cache;
	
	public UnknownPropertyHelper(SparqlEndpoint endpoint, ExtractionDBCache cache) {
		this.endpoint = endpoint;
		this.cache = cache;
	}
	
	public static void getPopularity(SparqlEndpoint endpoint, ExtractionDBCache cache, String type, String resource){
		String query = String.format("SELECT ?p COUNT(?x) WHERE {?x a <%s>. <%s> ?p ?x.} GROUP BY ?p", type, resource);
		System.out.println(query);
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		while(rs.hasNext()){
			System.out.println(rs.next());
		}
		
		query = String.format("SELECT ?p COUNT(?x) WHERE {?x a <%s>. ?x ?p <%s>.} GROUP BY ?p", type, resource);
		rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		while(rs.hasNext()){
			System.out.println(rs.next());
		}
	}
	
	
	public static List<Entry<String, Integer>> getMostFrequentProperties(SparqlEndpoint endpoint, ExtractionDBCache cache, String type, String resource, SymPropertyDirection direction){
		Map<String, Integer> property2Frequency = new HashMap<String, Integer>();
		String query;
		ResultSet rs;
		if(direction == SymPropertyDirection.VAR_LEFT){
			query = String.format("SELECT ?p (COUNT(?x) AS ?cnt) WHERE {?x a <%s>. ?x ?p <%s>.} GROUP BY ?p ORDER BY DESC(?cnt)", type, resource);
			rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				String propertyURI = qs.getResource("p").getURI();
				int cnt = qs.getLiteral("cnt").getInt();
				property2Frequency.put(propertyURI, cnt);
			}
		} else if(direction == SymPropertyDirection.VAR_RIGHT){
			query = String.format("SELECT ?p (COUNT(?x) AS ?cnt) WHERE {?x a <%s>. <%s> ?p ?x.} GROUP BY ?p ORDER BY DESC(?cnt)", type, resource);
			rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				String propertyURI = qs.getResource("p").getURI();
				int cnt = qs.getLiteral("cnt").getInt();
				property2Frequency.put(propertyURI, cnt);
			}
		} else if(direction == SymPropertyDirection.UNKNOWN){
			
		} 
		List<Entry<String, Integer>> sortedProperty2Frequency = MapUtils.sortByValues(property2Frequency);
		return sortedProperty2Frequency;
	}
	
	public static SymPropertyDirection getDirection(SparqlEndpoint endpoint, ExtractionDBCache cache, String typeURI, String propertyURI){
		String query = String.format("SELECT (COUNT(?x) AS ?cnt) WHERE {?x a <%s>. ?x <%s> ?o.}", typeURI, propertyURI);
		ResultSet rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		int classLeftCnt = 0;
		while(rs.hasNext()){
			classLeftCnt = rs.next().getLiteral("cnt").getInt();
		}
		
		query = String.format("SELECT (COUNT(?x) AS ?cnt) WHERE {?x a <%s>. ?o <%s> ?x.}", typeURI, propertyURI);
		rs = SparqlQuery.convertJSONtoResultSet(cache.executeSelectQuery(endpoint, query));
		int classRightCnt = 0;
		while(rs.hasNext()){
			classRightCnt = rs.next().getLiteral("cnt").getInt();
		}
		if(classLeftCnt > classRightCnt){
			return SymPropertyDirection.VAR_LEFT;
		} else if(classRightCnt > classLeftCnt){
			return SymPropertyDirection.VAR_RIGHT;
		} else {
			return SymPropertyDirection.UNKNOWN;
		}
	}

}
