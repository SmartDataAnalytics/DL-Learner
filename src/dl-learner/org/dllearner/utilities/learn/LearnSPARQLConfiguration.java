package org.dllearner.utilities.learn;

import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasoningService;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlEndpoint;

public class LearnSPARQLConfiguration {

	

	//	 SparqlKnowledgeSource
	public SparqlEndpoint sparqlEndpoint = SparqlEndpoint.getEndpointDBpedia();
	public int recursiondepth = 1;
	public boolean closeAfterRecursion = true;
	public boolean randomizeCache = false;
	public String predefinedFilter = "YAGO";
	
	
	public double noisePercentage = 15;
	public int maxExecutionTimeInSeconds = 30;
	public int minExecutionTimeInSeconds = 0;
	public int guaranteeXgoodDescriptions = 40;
	
	public boolean useAllConstructor = false;
	public boolean useExistsConstructor = true;
	public boolean useCardinalityRestrictions = false;
	public boolean useNegation = false;
	
	public boolean writeSearchTree = false;
	public String searchTreeFile = "log/searchTree.txt";
	public boolean replaceSearchTree = true;
	public String logLevel = "TRACE";
	
	
	
	public void applyConfigEntries(ComponentManager cm, KnowledgeSource ks, LearningProblem lp, ReasoningService rs, LearningAlgorithm la) {
		try {
			
			// KNOWLEDGESOURCE
			cm.applyConfigEntry(ks, "url", sparqlEndpoint.getURL().toString());
			cm.applyConfigEntry(ks, "predefinedEndpoint", "DBPEDIA");
			cm.applyConfigEntry(ks, "recursionDepth", recursiondepth);
			cm.applyConfigEntry(ks, "closeAfterRecursion", closeAfterRecursion);
			cm.applyConfigEntry(ks, "predefinedFilter", predefinedFilter);
			if (randomizeCache)
				cm.applyConfigEntry(ks, "cacheDir", "cache/"
						+ System.currentTimeMillis() + "");
			else {
				cm.applyConfigEntry(ks, "cacheDir", Cache.getDefaultCacheDir());
			}

		
			// LEARNINGALGORITHM
			cm.applyConfigEntry(la, "useAllConstructor", useAllConstructor);
			cm.applyConfigEntry(la, "useExistsConstructor", useExistsConstructor);
			cm.applyConfigEntry(la, "useCardinalityRestrictions", useCardinalityRestrictions);
			cm.applyConfigEntry(la, "useNegation", useNegation);
			
			cm.applyConfigEntry(la, "minExecutionTimeInSeconds", minExecutionTimeInSeconds);
			cm.applyConfigEntry(la, "maxExecutionTimeInSeconds",
					maxExecutionTimeInSeconds);
			cm.applyConfigEntry(la, "guaranteeXgoodDescriptions",
					guaranteeXgoodDescriptions);
			
			cm.applyConfigEntry(la, "writeSearchTree", writeSearchTree);
			cm.applyConfigEntry(la, "searchTreeFile", searchTreeFile);
			cm.applyConfigEntry(la, "replaceSearchTree", replaceSearchTree);
			
			cm.applyConfigEntry(la, "noisePercentage", noisePercentage);
			cm.applyConfigEntry(la, "logLevel", logLevel);
			/*
			 * if(ignoredConcepts.size()>0)
			 * cm.applyConfigEntry(la,"ignoredConcepts",ignoredConcepts);
			 */

		
		} catch (Exception e) {
			e.printStackTrace();
		}
		// return null;

	}
}
