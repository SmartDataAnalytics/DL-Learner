package org.dllearner.algorithm.tbsl.cli;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.algorithm.tbsl.learning.NoTemplateFoundException;
import org.dllearner.algorithm.tbsl.learning.SPARQLTemplateBasedLearner;
import org.dllearner.algorithm.tbsl.templator.Templator;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.ini4j.InvalidFileFormatException;

public class CLI {
	

	    public static void main(String[] args) throws InvalidFileFormatException, FileNotFoundException, IOException {
	    	
//	    	Logger.getLogger(SPARQLTemplateBasedLearner.class).setLevel(Level.OFF);
	    	
			SPARQLTemplateBasedLearner learner = new SPARQLTemplateBasedLearner();
			SparqlEndpoint endpoint = new SparqlEndpoint(new URL("http://live.dbpedia.org/sparql"), 
					Collections.<String>singletonList(""), Collections.<String>emptyList());
	    	
	        System.out.println("======= TBSL v0.1 =============");       
	        System.out.println("\nType ':q' to quit.");

	        while (true) {
	            String question = getStringFromUser("Question > ").trim(); 
	            
	            if (question.equals(":q")) {
	                System.exit(0);
	            }
	         
	            learner.setEndpoint(endpoint);
				learner.setQuestion(question);
				try {
					learner.learnSPARQLQueries();
					String learnedQuery = learner.getBestSPARQLQuery();
					if(learnedQuery != null){
						System.out.println("Learned query:\n" + learnedQuery);
					} else {
						System.out.println("Could not learn a SPARQL query.");
					}
				} catch (NoTemplateFoundException e) {
					System.out.println("Sorry, could not generate a template.");
				}
	            
	        }
	    }

	    public static String getStringFromUser(String msg) {
	        String str = "";
	        try {
	        	System.out.println("\n===========================================\n");
	            System.out.print(msg);
	            str = new BufferedReader(new InputStreamReader(System.in)).readLine();
	        } catch (IOException e) {
	        }
	        return str;
	    }

}
