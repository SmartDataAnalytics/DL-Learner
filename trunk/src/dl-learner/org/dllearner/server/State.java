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
package org.dllearner.server;

import java.util.Set;

import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponent;

/**
 * Stores the state of a DL-Learner client session.
 * 
 * @author Jens Lehmann
 *
 */
public class State {

	private Set<KnowledgeSource> knowledgeSource;
	
	private LearningProblem learningProblem;
	
	private ReasonerComponent reasonerComponent;
	
	private LearningAlgorithm learningAlgorithm;

	/**
	 * @return the knowledgeSource
	 */
	public Set<KnowledgeSource> getKnowledgeSource() {
		return knowledgeSource;
	}

	/**
	 * @param knowledgeSource the knowledgeSource to set
	 */
	public void setKnowledgeSource(Set<KnowledgeSource> knowledgeSource) {
		this.knowledgeSource = knowledgeSource;
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.Set#add(java.lang.Object)
	 */
	public boolean addKnowledgeSource(KnowledgeSource ks) {
		return knowledgeSource.add(ks);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.Set#remove(java.lang.Object)
	 */
	public boolean removeKnowledgeSource(KnowledgeSource ks) {
		return knowledgeSource.remove(ks);
	}

	/**
	 * @return the learningProblem
	 */
	public LearningProblem getLearningProblem() {
		return learningProblem;
	}

	/**
	 * @param learningProblem the learningProblem to set
	 */
	public void setLearningProblem(LearningProblem learningProblem) {
		this.learningProblem = learningProblem;
	}

	/**
	 * @return the reasonerComponent
	 */
	public ReasonerComponent getReasonerComponent() {
		return reasonerComponent;
	}

	/**
	 * @param reasonerComponent the reasonerComponent to set
	 */
	public void setReasonerComponent(ReasonerComponent reasonerComponent) {
		this.reasonerComponent = reasonerComponent;
	}

	/**
	 * @return the learningAlgorithm
	 */
	public LearningAlgorithm getLearningAlgorithm() {
		return learningAlgorithm;
	}

	/**
	 * @param learningAlgorithm the learningAlgorithm to set
	 */
	public void setLearningAlgorithm(LearningAlgorithm learningAlgorithm) {
		this.learningAlgorithm = learningAlgorithm;
	}
	
}
