package org.dllearner.scripts;

import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;

public class SPARQLEndpointMetrics {
	
	//parameters for thread pool
		//Parallel running Threads(Executor) on System
		private static int corePoolSize = 1;
		//Maximum Threads allowed in Pool
		private static int maximumPoolSize = 20;
		//Keep alive time for waiting threads for jobs(Runnable)
		private static long keepAliveTime = 10;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		Logger.getRootLogger().setLevel(Level.WARN);
		Logger.getLogger("org.dllearner").setLevel(Level.WARN); // seems to be needed for some reason (?)
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(consoleAppender);		
		
		// get all SPARQL endpoints and their graphs - the key is a name-identifier
		Map<String,SparqlEndpoint> endpoints = new TreeMap<String,SparqlEndpoint>();
		Map<SparqlEndpoint, String> namespaces = new TreeMap<SparqlEndpoint, String>();
		
		String query = "";
		query += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n";
		query += "PREFIX void: <http://rdfs.org/ns/void#> \n";
		query += "PREFIX dcterms: <http://purl.org/dc/terms/> \n";
		query += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";
		query += "PREFIX ov: <http://open.vocab.org/terms/> \n";
		query += "SELECT * \n";
		query += "WHERE { \n";
		query += "   ?item rdf:type void:Dataset . \n";
		query += "   ?item dcterms:isPartOf <http://ckan.net/group/lodcloud> . \n";
		query += "   ?item void:sparqlEndpoint ?endpoint . \n";
//		query += "   ?item dcterms:subject ?subject . \n";
//		query += "   ?item rdfs:label ?label . \n";
		query += "   ?item ov:shortName ?shortName . \n";
		query += " OPTIONAL{?item <http://www.w3.org/ns/sparql-service-description#namedGraph> ?defaultGraph} \n";
		query += "}";
//		query += "LIMIT 20";
		System.out.println("Getting list of SPARQL endpoints from LATC DSI:");
		System.out.println(query);
		
		// contact LATC DSI/MDS
		SparqlEndpoint dsi = new SparqlEndpoint(new URL("http://api.talis.com/stores/latc-mds/services/sparql"));
		SparqlQuery sq = new SparqlQuery(query, dsi);
		ResultSet rs = sq.send();
		while(rs.hasNext()) {
			QuerySolution qs = rs.next();
			String endpoint = qs.get("endpoint").toString();
			String shortName = qs.get("shortName").toString();
			Resource r = qs.getResource("defaultGraph");
			if(r != null){
				String defaultGraph = qs.get("defaultGraph").toString();System.out.println(defaultGraph);
			}
			
			endpoints.put(shortName, new SparqlEndpoint(new URL(endpoint)));
		}
		System.out.println(endpoints.size() + " endpoints detected.");
		
		ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(endpoints.size());
		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue);
		
		final StringBuffer sb = new StringBuffer();
		sb.append("<table border=\"1\">");
		sb.append("<tr><th>#classes</th><th>#op</th><th>#dp</th><th>#individuals</th></tr>");
		
		// perform enrichment on endpoints
		for(final Entry<String,SparqlEndpoint> endpoint : endpoints.entrySet()) {
			
			threadPool.execute(new Runnable() {
				
				@Override
				public void run() {
					SparqlEndpoint se = endpoint.getValue();
//					System.out.println(se);
					
					String filter = "FILTER()";
					
					try {
						//count classes
						String query = "SELECT (COUNT(DISTINCT ?type) AS ?cnt) WHERE {?s a ?type.}";
						int classCnt = new SparqlQuery(query, se).send().next().getLiteral("cnt").getInt();
						
						//count object properties
						query = "SELECT (COUNT(DISTINCT ?p) AS ?cnt) WHERE {?s ?p ?o.}";
						int opCnt = new SparqlQuery(query, se).send().next().getLiteral("cnt").getInt();
						
						//count data properties
						query = "SELECT (COUNT(DISTINCT ?p) AS ?cnt) WHERE {?s ?p ?o.}";
						int dpCnt = new SparqlQuery(query, se).send().next().getLiteral("cnt").getInt();
						
						//count individuals
						query = "SELECT (COUNT(DISTINCT ?s) AS ?cnt) WHERE {?s a ?type.}";
						int indCnt = new SparqlQuery(query, se).send().next().getLiteral("cnt").getInt();
						
						sb.append("<tr><td>" + classCnt + "</td><td>" + opCnt + "</td>" + "</td><td>" + dpCnt + "</td><td>" + indCnt + "</td></tr>\n");
					} catch (Exception e) {
						sb.append("");
					}
					
				}
			});
		}
		threadPool.shutdown();
		System.out.println(sb);

	}

}
