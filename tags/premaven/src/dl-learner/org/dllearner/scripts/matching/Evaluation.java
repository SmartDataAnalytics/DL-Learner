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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.DataFormatException;

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
	private int discarded;
	private int noMatchCount;
	private int correctMatchCount;
	private int incorrectMatchCount;
	private int matchCount;
	private double precision;
	private double recall;

	private Map<POIClass, Integer> testsPerClass = new HashMap<POIClass, Integer>();
	private Map<POIClass, Integer> noMatchPerClass = new HashMap<POIClass, Integer>();
	private Map<POIClass, Integer> correctMatchPerClass = new HashMap<POIClass, Integer>();
	private Map<POIClass, Integer> incorrectMatchPerClass = new HashMap<POIClass, Integer>();
	
	private static Logger logger = Logger.getLogger(Evaluation.class);
	
	// map from DBpedia to LinkedGeoData
	public Evaluation(Map<URI,URI> testMatches) throws IOException {
		
		tests = 0;
		discarded = 0;
		noMatchCount = 0;
		correctMatchCount = 0;
		incorrectMatchCount = 0;
		
		// init counts
		for(POIClass poiClass : POIClass.values()) {
			testsPerClass.put(poiClass, 0);
			noMatchPerClass.put(poiClass, 0);
			correctMatchPerClass.put(poiClass, 0);
			incorrectMatchPerClass.put(poiClass, 0);
		}
		
		for(Entry<URI,URI> match : testMatches.entrySet()) {
			// find point in DBpedia file:
			// approach 1:
			// step 1: locate point in DBpedia file
			// step 2: read all information about point
			// step 3: write a method converting this information into a DBpedia point

			// "problem": might be good to put all relevant DBpedia and GeoData points in
			// memory to efficiently evaluate a lot of parameter settings without
			// requiring to perform slow HTTP or SPARQL requests
			
//			logger.trace("searching match for " + match.getKey() + "...");
			
			// we make the assumption that we always want to match against nodes
			if(match.getValue().toString().contains("/way/")) 
				continue;
			
			DBpediaPoint dbpediaPoint = null;
			try {
				dbpediaPoint = new DBpediaPoint(match.getKey());
			} catch (Exception e) {
//				System.out.println("discarded: " + match.getKey());
				logger.debug(e.getMessage());
				discarded++;
				continue;
			}
			
			URI matchedURI = null;
			
			if(dbpediaPoint.getPoiClass() == null) {
				if(dbpediaPoint.getClasses().length == 0) {
					System.out.println("skipping " +  dbpediaPoint.getUri() + " (unknown POI type)");
				} else {
					System.out.println("skipping " +  dbpediaPoint.getUri() + " (unsupported POI type)");
				}
				continue;
			} else {
				logger.info("Eval: searching match for " + match.getKey() + "(" + dbpediaPoint.getPoiClass() + ") ...");
				matchedURI = DBpediaLinkedGeoData.findGeoDataMatch(dbpediaPoint);
			}
			
			URI testURI = match.getValue();
			
			// no match found
			if(matchedURI == null) {
				noMatchCount++;
				inc(noMatchPerClass, dbpediaPoint.getPoiClass());
				logger.info("Eval:  ... no match found");
			// correct match found
			} else if(matchedURI.equals(testURI)) {
				correctMatchCount++;
				inc(correctMatchPerClass, dbpediaPoint.getPoiClass());
				logger.info("Eval:  ... " + testURI + " correctly detected");
			// incorrect match found
			} else {
				incorrectMatchCount++;
				inc(incorrectMatchPerClass, dbpediaPoint.getPoiClass());
				logger.info("Eval:  ... " + matchedURI + " detected, but " + testURI + " is correct");
			}
			
			tests++;
			inc(testsPerClass, dbpediaPoint.getPoiClass());
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

	public int getDiscarded() {
		return discarded;
	}	
	
	private void inc(Map<POIClass,Integer> map, POIClass poiClass) {
//		if(map.containsKey(poiClass)) {
			map.put(poiClass, map.get(poiClass)+1);
//		} else {
//			map.put(poiClass, 1);
//		}
	}
	
	public Integer getCorrectMatchPerClass(POIClass poiClass) {
		return correctMatchPerClass.get(poiClass);
	}	
	
	public Integer getIncorrectMatchPerClass(POIClass poiClass) {
		return incorrectMatchPerClass.get(poiClass);
	}	
	
	public Integer getTestsPerClass(POIClass poiClass) {
		return testsPerClass.get(poiClass);
	}	
	
	public Integer getMatchPerClass(POIClass poiClass) {
		return incorrectMatchPerClass.get(poiClass) + correctMatchPerClass.get(poiClass);
	}	
	
	public Integer getNoMatchPerClass(POIClass poiClass) {
		return noMatchPerClass.get(poiClass);
	}
	
	public double getPrecisionPerClass(POIClass poiClass) {
		if(getMatchPerClass(poiClass) == 0) {
			return 0;
		} else {
			return correctMatchPerClass.get(poiClass) / (double) getMatchPerClass(poiClass);
		}
	}
	
	public double getRecallPerClass(POIClass poiClass) {
		if(testsPerClass.get(poiClass) == 0) {
			return 0;
		} else {
			return correctMatchPerClass.get(poiClass) / (double) testsPerClass.get(poiClass);
		}
	}		
	
	public static void main(String args[]) throws IOException, DataFormatException {
		
		Logger.getRootLogger().setLevel(Level.INFO);
		// test file
		File testFile = new File("log/geodata/owlsameas_en.dat");
		// map for collecting matches
		Map<URI,URI> matches = Utility.getMatches(testFile);
		// perform evaluation and print results
		System.out.println(new Date());
		Evaluation eval = new Evaluation(matches);
		System.out.println(new Date());

		for(POIClass poiClass : POIClass.values()) {
			System.out.println();
			System.out.println("summary for POI class " + poiClass + ":");
			System.out.println(eval.getTestsPerClass(poiClass) + " points tested");
			System.out.println("precision: " + eval.getPrecisionPerClass(poiClass) + " (" + eval.getCorrectMatchPerClass(poiClass) + "/" + eval.getMatchPerClass(poiClass) + ")");
			System.out.println("recall: " + eval.getRecallPerClass(poiClass) + " (" + eval.getCorrectMatchPerClass(poiClass) + "/" + eval.getTestsPerClass(poiClass) + ")");			
		}
		
		System.out.println("");
		System.out.println("Overall summary:");
		System.out.println(eval.getTests() + " points tested (" + eval.getDiscarded() + " discarded)");
		System.out.println("precision: " + eval.getPrecision() + " (" + eval.getCorrectMatchCount() + "/" + eval.getMatchCount() + ")");
		System.out.println("recall: " + eval.getRecall() + " (" + eval.getCorrectMatchCount() + "/" + eval.getTests() + ")");
				
	}
	
}
