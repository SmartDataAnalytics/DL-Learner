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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.ReasoningService;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.SparqlEndpoint;

/**
 * Stores the state of a DL-Learner client session.
 * 
 * @author Jens Lehmann
 *
 */
public class State {

	private Set<KnowledgeSource> knowledgeSources = new HashSet<KnowledgeSource>();
	
	private LearningProblem learningProblem;
	
	private ReasonerComponent reasonerComponent;
	private ReasoningService reasoningService;
	
	private LearningAlgorithm learningAlgorithm;

	/**
	 * @return the knowledgeSource
	 */
	public Set<KnowledgeSource> getKnowledgeSources() {
		return knowledgeSources;
	}

	/**
	 * @param knowledgeSources the knowledgeSource to set
	 */
	public void setKnowledgeSources(Set<KnowledgeSource> knowledgeSources) {
		this.knowledgeSources = knowledgeSources;
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.Set#add(java.lang.Object)
	 */
	public boolean addKnowledgeSource(KnowledgeSource ks) {
		return knowledgeSources.add(ks);
	}

	/**
	 * Removes a knowledge source with the given URL (independant of its type).
	 * @param url URL of the OWL file or SPARQL Endpoint.
	 * @return True if a knowledge source was deleted, false otherwise.
	 */
	public boolean removeKnowledgeSource(String url) {
		Iterator<KnowledgeSource> it = knowledgeSources.iterator(); 
		while(it.hasNext()) {
			KnowledgeSource source = it.next();
			if((source instanceof OWLFile && ((OWLFile)source).getURL().toString().equals(url))
				|| (source instanceof SparqlEndpoint && ((SparqlEndpoint)source).getURL().toString().equals(url)) ) {
				it.remove();
				return true;
			}
		}
		return false;
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
	 * Sets the reasoner component and creates the corresponding
	 * <code>ReasoningService</code> instance.
	 * 
	 * @param reasonerComponent the reasonerComponent to set
	 */
	public void setReasonerComponent(ReasonerComponent reasonerComponent) {
		this.reasonerComponent = reasonerComponent;
		reasoningService = new ReasoningService(reasonerComponent);
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

	/**
	 * @return the reasoningService
	 */
	public ReasoningService getReasoningService() {
		return reasoningService;
	}
	
}
