/**
 * Copyright (C) 2007-2010, Jens Lehmann
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
package org.dllearner.sparqlquerygenerator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.sparqlquerygenerator.impl.SPARQLQueryGeneratorImpl;
import org.dllearner.sparqlquerygenerator.operations.lgg.LGGGeneratorImpl;
import org.dllearner.sparqlquerygenerator.operations.nbr.strategy.BruteForceNBRStrategy;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class DBPediaTestScript {
	
	private static final String DBPEDIA_ENDPOINT_URL = "http://dbpedia.aksw.org:8890/sparql";
//	private static final String DBPEDIA_ENDPOINT_URL = "http://dbpedia.org/sparql/";
//	private static final String DBPEDIA_ENDPOINT_URL = "http://dbpedia-live.openlinksw.com/sparql/";
	
	public static void main(String[] args) throws IOException{
		
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		FileAppender fileAppender = new FileAppender( layout, "log/dbpedia_test.log", false );
		Logger logger = Logger.getRootLogger();
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.addAppender(fileAppender);
		logger.setLevel(Level.INFO);		
		Logger.getLogger(SPARQLQueryGeneratorImpl.class).setLevel(Level.INFO);
		Logger.getLogger(LGGGeneratorImpl.class).setLevel(Level.INFO);
		Logger.getLogger(BruteForceNBRStrategy.class).setLevel(Level.INFO);
		
		SPARQLQueryGenerator gen = new SPARQLQueryGeneratorImpl(DBPEDIA_ENDPOINT_URL);
		
		Set<String> posExamples = new HashSet<String>();
		Set<String> negExamples = new HashSet<String>();
		
		if(args[0].equals("-f")){
			BufferedReader bfr = new BufferedReader(new FileReader(args[1]));
            String line;
            while ((line = bfr.readLine())!= null){
            	if(line.startsWith("+")){
					posExamples.add(line.substring(1));
				} else if(line.startsWith("-")){
					negExamples.add(line.substring(1));
				}
            }
		} else {
			for(String example : args){
				if(example.startsWith("+")){
					posExamples.add(example.substring(1));
				} else if(example.startsWith("-")){
					negExamples.add(example.substring(1));
				}
			}
		}
		
		List<String> queries = gen.getSPARQLQueries(posExamples, negExamples);
		
		System.out.println("SPARQL queries: ");
		for(String query : queries){
			System.out.println(query);
		}
	}

}
