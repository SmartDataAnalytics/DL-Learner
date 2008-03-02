package org.dllearner.gui;

/**
 * Copyright (C) 2007-2008, Jens Lehmann
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

import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.ReasonerComponent;

// import org.dllearner.core.Component;

/**
 * Config save all together used variables: ComponentManager, KnowledgeSource,
 * Reasoner, ReasoningService, LearningProblem, LearningAlgorithm; also inits of
 * these components.
 * 
 * @author Tilo Hielscher
 */
public class Config {
	private ComponentManager cm = ComponentManager.getInstance();
	private KnowledgeSource source;
	private KnowledgeSource oldSource;
	private ReasonerComponent reasoner;
	private ReasonerComponent oldReasoner;
	private ReasoningService rs;
	private LearningProblem lp;
	private LearningProblem oldLearningProblem;
	private LearningAlgorithm la;
	private LearningAlgorithm oldLearningAlgorithm;
	private boolean[] isInit = new boolean[4];
	private Boolean threadIsRunning = false;
	private Long algorithmRunStartTime = null;
	private Long algorithmRunStopTime = null;

	/**
	 * Get ComponentManager.
	 * 
	 * @return ComponentManager
	 */
	public ComponentManager getComponentManager() {
		return this.cm;
	}

	/**
	 * It is necessary for init KnowledgeSource.
	 * 
	 * @return true, if url was set otherwise false
	 */
	public Boolean isSetURL() {
		if (cm.getConfigOptionValue(source, "url") != null
				|| cm.getConfigOptionValue(source, "filename") != null)
			return true;
		else
			return false;
	}

	/**
	 * Set KnowledgeSource.
	 * 
	 * @param knowledgeSource
	 */
	public void setKnowledgeSource(KnowledgeSource knowledgeSource) {
		this.oldSource = this.source;
		this.source = knowledgeSource;
	}

	/**
	 * Get KnowledgeSource.
	 * 
	 * @return KnowledgeSource
	 */
	public KnowledgeSource getKnowledgeSource() {
		return this.source;
	}

	/**
	 * Get old KnowledgeSource.
	 * 
	 * @return old KnowledgeSource
	 */
	public KnowledgeSource getOldKnowledgeSource() {
		return this.oldSource;
	}

	/**
	 * Set Reasoner.
	 * 
	 * @param reasoner
	 */
	public void setReasoner(ReasonerComponent reasoner) {
		this.oldReasoner = this.reasoner;
		this.reasoner = reasoner;
	}

	/**
	 * Get Reasoner.
	 * 
	 * @return reasoner
	 */
	public ReasonerComponent getReasoner() {
		return this.reasoner;
	}

	/**
	 * Get old Reasoner as a set.
	 * 
	 * @return oldReasonerSet.
	 */
	public ReasonerComponent getOldReasonerSet() {
		return this.oldReasoner;
	}

	/**
	 * Set ReasoningService.
	 * 
	 * @param reasoningService
	 */
	public void setReasoningService(ReasoningService reasoningService) {
		this.rs = reasoningService;
	}

	/**
	 * Get ReasoningService.
	 * 
	 * @return ReasoningService
	 */
	public ReasoningService getReasoningService() {
		return this.rs;
	}

	/**
	 * Set LearningProblem.
	 * 
	 * @param learningProblem
	 */
	public void setLearningProblem(LearningProblem learningProblem) {
		this.oldLearningProblem = this.lp;
		this.lp = learningProblem;
	}

	/**
	 * Get LearningProblem.
	 * 
	 * @return learningProblem
	 */
	public LearningProblem getLearningProblem() {
		return this.lp;
	}

	/**
	 * Get old LearningProblem as a set.
	 * 
	 * @return old learningProblemSet.
	 */
	public LearningProblem getOldLearningProblem() {
		return this.oldLearningProblem;
	}

	/**
	 * Set LearningAlgorithm.
	 * 
	 * @param learningAlgorithm
	 */
	public void setLearningAlgorithm(LearningAlgorithm learningAlgorithm) {
		this.oldLearningAlgorithm = this.la;
		this.la = learningAlgorithm;
	}

	/**
	 * Get LearningAlgorithm.
	 * 
	 * @return LearningAlgorithm
	 */
	public LearningAlgorithm getLearningAlgorithm() {
		return this.la;
	}

	/**
	 * Get old LearningAlgorithmSet.
	 * 
	 * @return old LearningAlgorithmSet
	 */
	public LearningAlgorithm getOldLearningAlgorithm() {
		return this.oldLearningAlgorithm;
	}

	/**
	 * KnowledgeSource.init has run?
	 * 
	 * @return true, if init was made, false if not
	 */
	public boolean isInitKnowledgeSource() {
		return isInit[0];
	}

	/**
	 * Set true if you run KnowwledgeSource.init. The inits from other tabs
	 * behind will automatic set to false.
	 */
	public void setInitKnowledgeSource(Boolean is) {
		isInit[0] = is;
		for (int i = 1; i < 4; i++)
			isInit[i] = false;
	}

	/**
	 * Reasoner.init has run?
	 * 
	 * @return true, if init was made, false if not
	 */
	public boolean isInitReasoner() {
		return isInit[1];
	}

	/**
	 * Set true if you run Reasoner.init. The inits from other tabs behind will
	 * automatic set to false.
	 */
	public void setInitReasoner(Boolean is) {
		isInit[1] = is;
		for (int i = 2; i < 4; i++)
			isInit[i] = false;
	}

	/**
	 * LearningProblem.init has run?
	 * 
	 * @return true, if init was made, false if not
	 */
	public boolean isInitLearningProblem() {
		return isInit[2];
	}

	/**
	 * Set true if you run LearningProblem.init. The inits from other tabs
	 * behind will automatic set to false.
	 */
	public void setInitLearningProblem(Boolean is) {
		isInit[2] = is;
		for (int i = 3; i < 4; i++)
			isInit[i] = false;
	}

	/**
	 * LearningAlgorithm.init() has run?
	 * 
	 * @return true, if init was made, false if not
	 */
	public boolean isInitLearningAlgorithm() {
		return isInit[3];
	}

	/**
	 * set true if you run LearningAlgorithm.init
	 */
	public void setInitLearningAlgorithm(Boolean is) {
		isInit[3] = is;
	}

	/**
	 * Set true if you start the algorithm.
	 * 
	 * @param isThreadRunning
	 */
	public void setThreadIsRunning(Boolean isThreadRunning) {
		if (isThreadRunning)
			algorithmRunStartTime = System.nanoTime();
		else if (algorithmRunStartTime != null)
			if (algorithmRunStartTime < System.nanoTime())
				algorithmRunStopTime = System.nanoTime();
		this.threadIsRunning = isThreadRunning;
	}

	/**
	 * Get true if algorithm has started, false if not.
	 * 
	 * @return true if algorithm is running, false if not.
	 */
	public Boolean getThreadIsRunning() {
		return this.threadIsRunning;
	}

	/**
	 * Get time in ns for run of algorithm.
	 * 
	 * @return time in ns
	 */
	public Long getAlgorithmRunTime() {
		if (algorithmRunStartTime != null && algorithmRunStopTime != null)
			if (algorithmRunStartTime < algorithmRunStopTime)
				return algorithmRunStopTime - algorithmRunStartTime;
		return null;
	}
}
