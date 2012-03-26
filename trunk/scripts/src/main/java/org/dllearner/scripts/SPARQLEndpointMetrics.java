package org.dllearner.scripts;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
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
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

public class SPARQLEndpointMetrics {
	
	//parameters for thread pool
		//Parallel running Threads(Executor) on System
		private static int corePoolSize = 5;
		//Maximum Threads allowed in Pool
		private static int maximumPoolSize = 20;
		//Keep alive time for waiting threads for jobs(Runnable)
		private static long keepAliveTime = 10;
		
		private static int queryTimeout = 30;

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
		
		final BufferedWriter output = new BufferedWriter(new FileWriter("log/endpointMetrics.html", true));
		
		final StringBuffer sb = new StringBuffer();
		sb.append("<table border=\"1\">\n");
		sb.append("<tr><th>endpoint</th><th>#classes</th><th>#op</th><th>#dp</th><th>URL</th><th>ERROR</th></tr>\n");
		output.append(sb.toString());
		output.flush();
		
		// perform enrichment on endpoints
		for(final Entry<String,SparqlEndpoint> endpoint : endpoints.entrySet()) {
			
			threadPool.execute(new Runnable() {
				
				@Override
				public void run() {
					SparqlEndpoint se = endpoint.getValue();
					String name = endpoint.getKey();
//					System.out.println(se);
					
					String filter = "FILTER()";
					
					try {
						//count classes
//						String query = "SELECT (COUNT(DISTINCT ?type) AS ?cnt) WHERE {?s a ?type.}";
//						int classCnt = executeQuery(query, se, 20).next().getLiteral("cnt").getInt();
//						
//						//count object properties
//						query = "SELECT (COUNT(DISTINCT ?p) AS ?cnt) WHERE {?s ?p ?o.}";
//						int opCnt = executeQuery(query, se, 20).next().getLiteral("cnt").getInt();
//						
//						//count data properties
//						query = "SELECT (COUNT(DISTINCT ?p) AS ?cnt) WHERE {?s ?p ?o.}";
//						int dpCnt = executeQuery(query, se, 20).next().getLiteral("cnt").getInt();
//						
//						//count individuals
//						query = "SELECT (COUNT(DISTINCT ?s) AS ?cnt) WHERE {?s a ?type.}";
//						int indCnt = executeQuery(query, se, 20).next().getLiteral("cnt").getInt();
						
						String query = "SELECT DISTINCT ?type WHERE {?s a ?type. ?type a <http://www.w3.org/2002/07/owl#Class>}";
						int classCnt = countEntities(query, se);
						
						//count object properties
						query = "SELECT DISTINCT ?p WHERE {?s ?p ?o. ?p a <http://www.w3.org/2002/07/owl#ObjectProperty>}";
						int opCnt = countEntities(query, se);
						
						//count data properties
						query = "SELECT DISTINCT ?p WHERE {?s ?p ?o. ?p a <http://www.w3.org/2002/07/owl#DatatypeProperty>}";
						int dpCnt = countEntities(query, se);
						
						//count individuals
//						query = "SELECT (COUNT(DISTINCT ?s) AS ?cnt) WHERE {?s a ?type.}";
//						int indCnt = executeQuery(query, se, 20).next().getLiteral("cnt").getInt();
						
						String line = "<tr><td>" + name + "</td><td>"
						+ classCnt + "</td><td>"
						+ opCnt + "</td><td>"
						+ dpCnt + "</td><td>"
						+ se.getURL() + "</td><td>"
						+ "" + "</td></tr>\n";
						
						sb.append(line);
						output.append(line);
						output.flush();
						System.out.println(sb);
					} catch (Exception e) {
						Throwable t = e.getCause();
						String errorCode;
						if(t == null){
							errorCode = e.getClass().getSimpleName();
						} else {
							errorCode = t.getMessage();
						}
						String line = "<tr><td>" + name + "</td><td>-1</td><td>-1</td><td>-1</td><td>" + se.getURL() + "</td><td>" + errorCode + "</tr>\n";
						sb.append(line);
						try {
							output.append(line);
							output.flush();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
					
				}
			});
		}
		threadPool.shutdown();
		System.out.println(sb);
		
	}
	
	private static int countEntities(String baseQuery, SparqlEndpoint endpoint) throws Exception{
		int cnt = 0;
		int limit = 1000;
		int offset = 0;
		ResultSet rs;
		int tmp = 0;
		do{
			String query = baseQuery + " LIMIT " + limit + " OFFSET " + offset;System.out.println(endpoint.getURL() + ": " + query);
			rs = executeQuery(query, endpoint);
			tmp = 0;
			while(rs.hasNext()){
				rs.next();
				tmp++;
			}
			cnt += tmp;
			offset += limit;
		} while (tmp >= limit);
		
		return cnt;
	}
	
	private static ResultSet executeQuery(String queryString, SparqlEndpoint endpoint) throws Exception{
		try {
			QueryEngineHTTP qe = new QueryEngineHTTP(endpoint.getURL().toString(), queryString);
			qe.setDefaultGraphURIs(endpoint.getDefaultGraphURIs());
			qe.setTimeout(queryTimeout * 1000);
			return qe.execSelect();
		} catch (Exception e) {
			throw e;
		}
	}

}
