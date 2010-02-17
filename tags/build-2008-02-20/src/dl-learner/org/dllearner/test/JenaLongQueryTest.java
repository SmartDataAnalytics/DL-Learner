package org.dllearner.test;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.engine.http.HttpQuery;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;



/**
 * this class was submitted to Jena Bug Tracker
 * 
 */
public class JenaLongQueryTest {

	public static void main(String[] args) {
		String url = "http://dbpedia.openlinksw.com:8890/sparql";
		String defaultgraph = "http://dbpedia.org";
		String shortQuery = "SELECT ?predicate ?object " +
				"WHERE { " +
				"<http://dbpedia.org/resource/Philolaus> ?predicate ?object. "+
				"FILTER( "+
				"(!isLiteral(?object)) "+
				"&&( !regex(str(?predicate), 'http://dbpedia.org/property/relatedInstance') ) "+
				"&&( !regex(str(?predicate), 'http://dbpedia.org/property/wikiPageUsesTemplate') ) "+
				"&&( !regex(str(?predicate), 'http://www.w3.org/2002/07/owl#sameAs') ) "+
				"&&( !regex(str(?predicate), 'http://xmlns.com/foaf/0.1/') ) "+
				"&&( !regex(str(?predicate), 'http://www.w3.org/2004/02/skos/core') ) "+
				"&&( !regex(str(?object), 'http://xmlns.com/foaf/0.1/') ) "+
				"&&( !regex(str(?object), 'http://upload.wikimedia.org/wikipedia') ) "+
				"&&( !regex(str(?object), 'http://www4.wiwiss.fu-berlin.de/flickrwrappr') ) "+
				"&&( !regex(str(?object), 'http://dbpedia.org/resource/Template') ) "+
				"&&( !regex(str(?object), 'http://upload.wikimedia.org/wikipedia/commons') ) "+
				"&&( !regex(str(?object), 'http://www.w3.org/2006/03/wn/wn20/instances/synset') ) "+
				"&&( !regex(str(?object), 'http://dbpedia.org/resource/Category:') ) "+
				"&&( !regex(str(?object), 'http://www.w3.org/2004/02/skos/core') ) "+
				"&&( !regex(str(?object), 'http://www.geonames.org') )).} ";
		
		String longQuery="SELECT ?predicate ?object " +
				"WHERE { " +
				"<http://dbpedia.org/resource/Philolaus> ?predicate ?object. "+
				"FILTER( "+
				"(!isLiteral(?object)) "+
				"&&( !regex(str(?predicate), 'http://dbpedia.org/property/relatedInstance') ) "+
				"&&( !regex(str(?predicate), 'http://dbpedia.org/property/wikiPageUsesTemplate') ) "+
				"&&( !regex(str(?predicate), 'http://www.w3.org/2002/07/owl#sameAs') ) "+
				"&&( !regex(str(?predicate), 'http://xmlns.com/foaf/0.1/') ) "+
				"&&( !regex(str(?predicate), 'http://www.w3.org/2004/02/skos/core') ) "+
				"&&( !regex(str(?object), 'http://xmlns.com/foaf/0.1/') ) "+
				"&&( !regex(str(?object), 'http://upload.wikimedia.org/wikipedia') ) "+
				"&&( !regex(str(?object), 'http://www4.wiwiss.fu-berlin.de/flickrwrappr') ) "+
				"&&( !regex(str(?object), 'http://dbpedia.org/resource/Template') ) "+
				"&&( !regex(str(?object), 'http://upload.wikimedia.org/wikipedia/commons') ) "+
				"&&( !regex(str(?object), 'http://www.w3.org/2006/03/wn/wn20/instances/synset') ) "+
				"&&( !regex(str(?object), 'http://www.w3.org/2006/03/wn/wn20/instances/synset') ) "+
				"&&( !regex(str(?object), 'http://www.w3.org/2006/03/wn/wn20/instances/synset') ) "+
				"&&( !regex(str(?object), 'http://www.w3.org/2006/03/wn/wn20/instances/synset') ) "+
				"&&( !regex(str(?object), 'http://www.w3.org/2006/03/wn/wn20/instances/synset') ) "+
				"&&( !regex(str(?object), 'http://www.w3.org/2006/03/wn/wn20/instances/synset') ) "+
				"&&( !regex(str(?object), 'http://dbpedia.org/resource/Category:') ) "+
				"&&( !regex(str(?object), 'http://www.w3.org/2004/02/skos/core') ) "+
				"&&( !regex(str(?object), 'http://www.geonames.org') )).} ";
		
		ResultSet rs;
		String xml;
		QueryEngineHTTP queryExecution;
		HttpQuery.urlLimit = 3*1024 ;
		queryExecution = new QueryEngineHTTP(url, shortQuery);
		queryExecution.addDefaultGraph(defaultgraph);
		rs = queryExecution.execSelect();
		xml = ResultSetFormatter.asXMLString(rs);
		System.out.println("Short Query ResultSet length: "+xml.length());
		
		try{
		queryExecution=new QueryEngineHTTP(url,longQuery);
		queryExecution.addDefaultGraph(defaultgraph);
		
		//queryExecution.getContext().
		rs = queryExecution.execSelect();
		xml = ResultSetFormatter.asXMLString(rs);
		System.out.println("Long Query ResultSet length: "+xml.length()+"\n");
		System.out.println("Long query XML: "+xml);
		}catch (Exception e) {e.printStackTrace();}
		//
		
		String queryWithIncreasingLength="";
		for (int i = 0; i < 100; i++) {
			queryWithIncreasingLength = makeQueryString ( i);
			queryExecution=new QueryEngineHTTP(url,queryWithIncreasingLength);
			queryExecution.addDefaultGraph(defaultgraph);
			rs = queryExecution.execSelect();
			System.out.println("Query with length: "+queryWithIncreasingLength.length()
					+" produces ResultSet length: "+ResultSetFormatter.toList(rs).size());
		}
		
	}
	
	public static String makeQueryString ( int charsToAdd){
		String add="";
		
		for (int i = 0; i <charsToAdd; i++) {
			add+="0123456789"+"0123456789";
		}
		// NOTE THE LAST LINE
		String query = "SELECT ?predicate ?object " +
		"WHERE { " +
		"<http://dbpedia.org/resource/Philolaus> ?predicate ?object. "+
		"FILTER( "+
		"(!isLiteral(?object)) "+
		"&&( !regex(str(?predicate), 'http://dbpedia.org/property/relatedInstance') ) "+
		"&&( !regex(str(?predicate), 'http://dbpedia.org/property/wikiPageUsesTemplate') ) "+
		"&&( !regex(str(?predicate), 'http://www.w3.org/2002/07/owl#sameAs') ) "+
		"&&( !regex(str(?predicate), 'http://xmlns.com/foaf/0.1/') ) "+
		"&&( !regex(str(?predicate), 'http://www.w3.org/2004/02/skos/core') ) "+
		"&&( !regex(str(?object), 'http://xmlns.com/foaf/0.1/') ) "+
		"&&( !regex(str(?object), 'http://upload.wikimedia.org/wikipedia') ) "+
		"&&( !regex(str(?object), 'http://www4.wiwiss.fu-berlin.de/flickrwrappr') ) "+
		"&&( !regex(str(?object), 'http://dbpedia.org/resource/Template') ) "+
		"&&( !regex(str(?object), 'http://upload.wikimedia.org/wikipedia/commons') ) "+
		"&&( !regex(str(?object), 'http://www.w3.org/2006/03/wn/wn20/instances/synset') ) "+
		"&&( !regex(str(?object), 'http://dbpedia.org/resource/Category:') ) "+
		"&&( !regex(str(?object), 'http://www.w3.org/2004/02/skos/core') ) "+
		"&&( !regex(str(?object), 'http://www.geonames.org"+add+"') )).} ";
		
		
		return query;
	}
	
	

}
