package org.dllearner.utilities.learn;

import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponent;

public class LearnConfiguration {

	public double noisePercentage = 0;
	public int maxExecutionTimeInSeconds = 0;
	public int minExecutionTimeInSeconds = 0;
	public int guaranteeXgoodDescriptions = 1;
	
	public SortedSet<String> ignoredConcepts = new TreeSet<String>();
	
	public boolean useAllConstructor = false;
	public boolean useExistsConstructor = true;
	public boolean useCardinalityRestrictions = false;
	public boolean useNegation = false;
	
	public boolean writeSearchTree = false;
	public String searchTreeFile = "log/searchTree.txt";
	public boolean replaceSearchTree = true;
	
	
	public void applyConfigEntries(ComponentManager cm, KnowledgeSource ks, LearningProblem lp, ReasonerComponent rs, AbstractCELA la) {
		try {
			
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
			
			if(ignoredConcepts.size()>0) {
			 cm.applyConfigEntry(la,"ignoredConcepts",ignoredConcepts);
			}

		
		} catch (Exception e) {
			e.printStackTrace();
		}
		// return null;

	}
}
