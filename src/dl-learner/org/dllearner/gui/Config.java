package org.dllearner.gui;

/**
 * Copyright (C) 2007, Jens Lehmann
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

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.ReasoningService;

/**
 * config
 * 
 * this class save all together used variables
 * 
 * @author Tilo Hielscher
 * 
 */

public class Config {
	private ComponentManager cm = ComponentManager.getInstance();
	private KnowledgeSource source;
	private File selectedFile;
	private ReasonerComponent reasoner;
	private ReasoningService rs;
	private String[] kbBoxItems = {"Pleae select a type", "KBFile", "OWLFile", "SparqleEndpoint"};
	private Set<String> exampleSet = new HashSet<String>();
	private LearningProblem lp;
	private LearningAlgorithm la;
	
	/**
	 * status should show witch variables are set
	 * status[0] ... cm
	 * status[1] ... KnowledgeSource
	 * status[2] ... File or URL 
	 * status[3] ... Resoner
	 * status[4] ... ReasoningService
	 * status[5] ... ExampleSet
	 * status[6] ... LearningProblem
	 */
	protected static boolean[] status = new boolean[8];
	
	protected boolean getStatus(int position) {
		if (status[position]) 
			return true;
		else 
			return false;
	}

	protected ComponentManager getComponentManager() {
		return cm;
	}
	
	protected void setComponentManager (ComponentManager input) {
		cm = input;
	}

	protected File getFile () {
		return selectedFile;
	}
	
	protected void setFile (File input) {
		status[2] = true;
		selectedFile = input;
	}
	
	protected ReasonerComponent getReasoner () {
		return reasoner;
	}

	protected void setReasoner (ReasonerComponent input) {
		status[3] = true;
		reasoner = input;
	}
	
	
	protected ReasoningService getReasoningService () {
		return rs;
	}
	
	protected void setReasoningService (ReasoningService input) {
		status[4] = true;
		rs = input; 
	}
	
	protected String[] getKBBoxItems() {
		return kbBoxItems;
	}
	
	protected KnowledgeSource getKnowledgeSource() {
		return source;
	}

	protected void setKnowledgeSource(KnowledgeSource input) {
		status[1] = true;
		source = input;
	}

	protected void setExampleSet(Set<String> input) {
		status[5] = true;
		exampleSet = input;
	}
	
	protected Set<String> getExampleSet () {
		return exampleSet;
	}
	
	protected void setLearningProblem (LearningProblem input) {
		status[6] = true;
		lp = input;
	}
	
	protected LearningProblem  getLearningProblem () {
		return lp;
	}
	
	protected void setLearningAlgorithm (LearningAlgorithm input) {
		status[6] = true;
		la = input;
	}
	
	protected LearningAlgorithm  getLearningAlgorithm () {
		return la;
	}

	
}
