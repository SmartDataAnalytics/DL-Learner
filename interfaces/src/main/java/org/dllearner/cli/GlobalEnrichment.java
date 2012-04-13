/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.dllearner.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.jsp.SkipPageException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.cli.Enrichment.AlgorithmRun;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.utilities.Files;
import org.semanticweb.owlapi.model.OWLAxiom;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * Enriches all of the LOD cloud.
 * 
 * @author Jens Lehmann
 * 
 */
public class GlobalEnrichment {
	
	//whether or not to skip endpoints which caused exceptions in a run before
	private static boolean skipFailedEndpoints = true;
	//whether or not to skip endpoints which returned no axioms during the learning process
	private static boolean skipEmptyEndpoints = true;
	//whether or not to skip endpoints on which we could learn something
	private static boolean skipSuccessfulEndpoints = true;

	// parameters
	private static double threshold = 0.8;
	private static int nrOfAxiomsToLearn = 10;
	private static int queryChunkSize = 1000;
	private static int maxExecutionTimeInSeconds = 10;
	private static boolean useInference = true;
	private static boolean omitExistingAxioms = false;
	
	// directory for generated schemata
	private static String baseDir = "log/lod-enriched/";
	
	
	//parameters for thread pool
	//Parallel running Threads(Executor) on System
	private static int corePoolSize = 1;
	//Maximum Threads allowed in Pool
	private static int maximumPoolSize = 20;
	//Keep alive time for waiting threads for jobs(Runnable)
	private static long keepAliveTime = 10;
	
	/**
	 * @param args
	 * @throws MalformedURLException 
	 * @throws LearningProblemUnsupportedException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ComponentInitException 
	 * @throws SecurityException 
	 * @throws IllegalArgumentException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws MalformedURLException, IllegalArgumentException, SecurityException, ComponentInitException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, LearningProblemUnsupportedException, FileNotFoundException {
		new File(baseDir).mkdirs();
		
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		Logger.getRootLogger().setLevel(Level.WARN);
		Logger.getLogger("org.dllearner").setLevel(Level.WARN); // seems to be needed for some reason (?)
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(consoleAppender);		
		
		// get all SPARQL endpoints and their graphs - the key is a name-identifier
		Map<String,SparqlEndpoint> endpoints = new TreeMap<String,SparqlEndpoint>();
		
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
			endpoints.put(shortName, new SparqlEndpoint(new URL(endpoint)));
		}
		System.out.println(endpoints.size() + " endpoints detected.");
		
		TreeSet<String> blacklist = new TreeSet<String>();
		blacklist.add("rkb-explorer-crime"); // computation never completes
		
		//remove endpoints which failed in a run before
		if(skipFailedEndpoints){
			for(String name : getErrorList()){
				endpoints.remove(name);
			}
		}
		if(skipEmptyEndpoints){
			for(String name : getEmptyList()){
				endpoints.remove(name);
			}
		}
		if(skipSuccessfulEndpoints){
			for(String name : getSuccessList()){
				endpoints.remove(name);
			}
		}
		
		ArrayBlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(endpoints.size());
		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue);
		
		
		// perform enrichment on endpoints
		for(final Entry<String,SparqlEndpoint> endpoint : endpoints.entrySet()) {
			
			threadPool.execute(new Runnable() {
				
				@Override
				public void run() {
					// run enrichment
					SparqlEndpoint se = endpoint.getValue();
					String name = endpoint.getKey();
					
					File f = new File(baseDir + File.separator + "success" + File.separator + name + ".ttl"); 
					File log = new File(baseDir + File.separator + "failed" + File.separator + name + ".log");
					
					System.out.println("Enriching " + name + " using " + se.getURL());
					Enrichment e = new Enrichment(se, null, threshold, nrOfAxiomsToLearn, useInference, 
							false, queryChunkSize, maxExecutionTimeInSeconds, omitExistingAxioms);
					
					e.maxEntitiesPerType = 3; // hack for faster testing of endpoints
					
//					if(blacklist.contains(name)) {
//						continue;
//					}
					
					boolean success = false;
					// run enrichment script - we make a case distinguish to see which kind of problems we get
					// (could be interesting for statistics later on)
					try {
						try {
							e.start();
							success = true;
						} catch (Exception ex){
							write2File(ex, se);
							ex.printStackTrace();
							ex.printStackTrace(new PrintStream(log));
						} catch(StackOverflowError error) {
							error.printStackTrace(new PrintStream(log));
							Files.appendToFile(log, "stack overflows could be caused by cycles in class hierarchies");
							error.printStackTrace();
						}
					} catch (FileNotFoundException e2) {
						e2.printStackTrace();
					} 
					/*catch(ResultSetException ex) {
						try {
							ex.printStackTrace(new PrintStream(log));
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						Files.appendToFile(log, ex.getMessage());
						ex.printStackTrace();
					} catch(QueryExceptionHTTP ex) {
						try {
							ex.printStackTrace(new PrintStream(log));
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						Files.appendToFile(log, ex.getMessage());
						ex.printStackTrace();				
					} 
					catch(Exception ex) {
						System.out.println("class of exception: " + ex.getClass());
					}*/
					
					// save results to a file (TODO: check if enrichment format 
					if(success) {
						SparqlEndpointKS ks = new SparqlEndpointKS(se);
						List<AlgorithmRun> runs = e.getAlgorithmRuns();
						List<OWLAxiom> axioms = new LinkedList<OWLAxiom>();
						int axiomCnt = 0;
						for(AlgorithmRun run : runs) {
							axiomCnt += e.getGeneratedOntology().getLogicalAxiomCount();
							axioms.addAll(e.toRDF(run.getAxioms(), run.getAlgorithm(), run.getParameters(), ks));
						}
						Model model = e.getModel(axioms);			
						try {
							if(axiomCnt == 0){
								f = f = new File(baseDir + File.separator + "success/empty" + File.separator + name + ".ttl"); 
							}
							model.write(new FileOutputStream(f), "TURTLE");
						} catch (FileNotFoundException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}				
					}
					
				}
			});
			
		}
		threadPool.shutdown();
	}
	
	public static void write2File(Exception e, SparqlEndpoint endpoint) {
		try {
			File file = new File(baseDir + File.separator + "errors" + File.separator + e.getClass().getName());
			if(!file.exists()){
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file, true);
			fw.append(endpoint.getURL().toString() + "\n");
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static List<String> getErrorList(){
		List<String> errorNames = new ArrayList<String>();
		File dir = new File(baseDir + "/failed/");
		dir.mkdirs();
		for(File file : dir.listFiles()){
			errorNames.add(file.getName().replace(".log", ""));
		}
		return errorNames;
	}
	
	public static List<String> getEmptyList(){
		List<String> errorNames = new ArrayList<String>();
		File dir = new File(baseDir + "/success/empty/");
		dir.mkdirs();
		for(File file : dir.listFiles()){
			errorNames.add(file.getName().replace(".ttl", ""));
		}
		return errorNames;
	}
	
	public static List<String> getSuccessList(){
		List<String> errorNames = new ArrayList<String>();
		File dir = new File(baseDir + "/success/");
		dir.mkdirs();
		for(File file : dir.listFiles()){
			errorNames.add(file.getName().replace(".ttl", ""));
		}
		return errorNames;
	}

}
