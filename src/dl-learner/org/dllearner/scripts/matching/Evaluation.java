/**
 * Copyright (C) 2007-2009, Jens Lehmann
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
package org.dllearner.scripts.matching;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Performs an evaluation of a matching method method by analising it 
 * on a test set. 
 * 
 * @author Jens Lehmann
 *
 */
public class Evaluation {

	private int tests;
	private int noMatchCount;
	private int correctMatchCount;
	private int incorrectMatchCount;
	private int matchCount;
	private double precision;
	private double recall;

	private static Logger logger = Logger.getLogger(Evaluation.class);
	
	// map from DBpedia to LinkedGeoData
	public Evaluation(Map<URI,URI> testMatches) throws IOException {
		
		tests = 0;
		noMatchCount = 0;
		correctMatchCount = 0;
		incorrectMatchCount = 0;
		
		for(Entry<URI,URI> match : testMatches.entrySet()) {
			// find point in DBpedia file:
			// approach 1:
			// step 1: locate point in DBpedia file
			// step 2: read all information about point
			// step 3: write a method converting this information into a DBpedia point

			// "problem": might be good to put all relevant DBpedia and GeoData points in
			// memory to efficiently evaluate a lot of parameter settings without
			// requiring to perform slow HTTP or SPARQL requests
			
			logger.trace("searching match for " + match.getKey() + "...");
			
			DBpediaPoint dbpediaPoint = null;
			try {
				dbpediaPoint = new DBpediaPoint(match.getKey());
			} catch (Exception e) {
				logger.debug(e.getMessage());
				continue;
			}
			URI matchedURI = DBpediaLinkedGeoData.findGeoDataMatch(dbpediaPoint);
			
			URI testURI = match.getValue();
			
			// no match found
			if(matchedURI == null) {
				noMatchCount++;
				logger.trace("  ... no match found");
			// correct match found
			} else if(matchedURI.equals(testURI)) {
				correctMatchCount++;
				logger.trace("  ... " + testURI + " correctly detected");
			// incorrect match found
			} else {
				incorrectMatchCount++;
				logger.trace("  ... " + matchedURI + " detected, but " + testURI + " is correct");
			}
			
			tests++;
		}
		
		matchCount = correctMatchCount + incorrectMatchCount;
		// determine proportion of correct matchings
		precision = correctMatchCount / (double) matchCount;
		// determine proportion of correct matches
		recall = correctMatchCount / (double) tests;
	}
	
	public int getCorrectMatchCount() {
		return correctMatchCount;
	}

	public int getIncorrectMatchCount() {
		return incorrectMatchCount;
	}

	public int getMatchCount() {
		return matchCount;
	}

	public int getNoMatchCount() {
		return noMatchCount;
	}

	public double getPrecision() {
		return precision;
	}

	public double getRecall() {
		return recall;
	}

	public int getTests() {
		return tests;
	}

	public static void main(String args[]) throws IOException {
		
		Logger.getRootLogger().setLevel(Level.TRACE);
		// test file
		String testFile = "log/geodata/owlsameas_en.dat";
		// map for collecting matches
		Map<URI,URI> matches = new HashMap<URI,URI>();
		// read file line by line to collect matches
		BufferedReader br = new BufferedReader(new FileReader(testFile));
		String line;
		while ((line = br.readLine()) != null) {
			String[] tmp = line.split("\t");
//			System.out.println(line);
//			for(String test : tmp) {
//				System.out.println(test);
//			}
			
			matches.put(URI.create(tmp[1]), URI.create(tmp[0] + "#id"));
		}
		// perform evaluation and print results
		Evaluation eval = new Evaluation(matches);
		System.out.println("precision: " + eval.getPrecision());
		System.out.println("recall: " + eval.getRecall());
	}
	
}
