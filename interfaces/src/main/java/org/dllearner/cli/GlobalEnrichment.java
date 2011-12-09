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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

	// parameters
	private static double threshold = 0.8;
	private static int nrOfAxiomsToLearn = 10;
	private static boolean useInference = true;
	
	// directory for generated schemata
	private static String baseDir = "log/lod-enriched/";
	
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
		
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		Logger.getRootLogger().setLevel(Level.WARN);
		Logger.getLogger("org.dllearner").setLevel(Level.WARN); // seems to be needed for some reason (?)
		Logger.getRootLogger().removeAllAppenders();
		Logger.getRootLogger().addAppender(consoleAppender);		
		
		// get all SPARQL endpoints and their graphs - the key is a name-identifier
		Map<String,SparqlEndpoint> endpoints = new HashMap<String,SparqlEndpoint>();
		
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
		
		// perform enrichment on endpoints
		for(Entry<String,SparqlEndpoint> endpoint : endpoints.entrySet()) {
			// run enrichment
			SparqlEndpoint se = endpoint.getValue();
			String name = endpoint.getKey();
			System.out.println("Enriching " + name + " using " + se);
			Enrichment e = new Enrichment(se, null, threshold, nrOfAxiomsToLearn, useInference, false);
			e.start();
			// save results to a file
			SparqlEndpointKS ks = new SparqlEndpointKS(se);
			List<AlgorithmRun> runs = e.getAlgorithmRuns();
			List<OWLAxiom> axioms = new LinkedList<OWLAxiom>();
			for(AlgorithmRun run : runs) {
				axioms.addAll(e.toRDF(run.getAxioms(), run.getAlgorithm(), run.getParameters(), ks));
			}
			Model model = e.getModel(axioms);
			File f = new File(baseDir + name + ".ttl"); 
			model.write(new FileOutputStream(f), "TURTLE");
		}
	}

}
