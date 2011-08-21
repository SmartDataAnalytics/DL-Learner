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
 */

package org.dllearner.utilities.learn;

import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.ComponentManager;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;

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
	
	
	public void applyConfigEntries(ComponentManager cm, AbstractKnowledgeSource ks, AbstractLearningProblem lp, AbstractReasonerComponent rs, AbstractCELA la) {
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
