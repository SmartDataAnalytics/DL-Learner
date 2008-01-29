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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.dllearner.core.Component;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.ReasoningService;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.kb.sparql.SparqlQuery;

/**
 * Stores the state of a DL-Learner client session.
 * 
 * @author Jens Lehmann
 *
 */
public class ClientState {

	// stores the mapping between component IDs and component
	// (note that this allows us to keep all references to components even
	// if they are not used anymore e.g. a deleted knowledge source)
	private Map<Integer,Component> componentIDs = new HashMap<Integer,Component>(); 
	
	private Set<KnowledgeSource> knowledgeSources = new HashSet<KnowledgeSource>();
	
	private Map<Integer, SparqlQuery> queryIDs = new HashMap<Integer, SparqlQuery>();
	
	private LearningProblem learningProblem;
	
	private ReasonerComponent reasonerComponent;
	private ReasoningService reasoningService;
	
	private LearningAlgorithm learningAlgorithm;

	private Random rand=new Random();
	
	private boolean isAlgorithmRunning = false;
	
	private int generateComponentID(Component component) {
		int id;
		do {
			id = rand.nextInt();
		} while(componentIDs.keySet().contains(id));
		componentIDs.put(id, component);
		return id;		
	}
	
	private int generateQueryID(SparqlQuery query) {
		int id;
		Random rand = new Random();
		do {
			id = rand.nextInt();
		} while (queryIDs.keySet().contains(id));
		queryIDs.put(id, query);
		return id;
	}
	
	public int addQuery(SparqlQuery query){
		return this.generateQueryID(query);
	}
	
	public SparqlQuery getQuery(int id){
		return queryIDs.get(id);
	}
	
	/**
	 * @return the isAlgorithmRunning
	 */
	public boolean isAlgorithmRunning() {
		return isAlgorithmRunning;
	}

	/**
	 * @param isAlgorithmRunning the isAlgorithmRunning to set
	 */
	public void setAlgorithmRunning(boolean isAlgorithmRunning) {
		this.isAlgorithmRunning = isAlgorithmRunning;
	}
	
//	public Component getComponent(Class<? extends Component> componentClass) throws UnknownComponentException {
//		if(learningProblem.getClass().equals(componentClass))
//			return learningProblem;
//		else if(learningAlgorithm.getClass().equals(componentClass))
//			return learningAlgorithm;
//		else if(reasonerComponent.getClass().equals(componentClass))
//			return reasonerComponent;
//		else if(KnowledgeSource.class.isAssignableFrom(componentClass)) {
//			
//			
//			for(KnowledgeSource ks : knowledgeSources) {
//				if(ks.getClass().equals(componentClass))
//					return ks;
//			}
//			throw new UnknownComponentException(componentClass.getName());
//		} else
//			throw new UnknownComponentException(componentClass.getName());
//	}

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
				|| (source instanceof SparqlKnowledgeSource && ((SparqlKnowledgeSource)source).getURL().toString().equals(url)) ) {
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
	public int setLearningProblem(LearningProblem learningProblem) {
		this.learningProblem = learningProblem;
		return generateComponentID(learningProblem);
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
	public int setReasonerComponent(ReasonerComponent reasonerComponent) {
		this.reasonerComponent = reasonerComponent;
		reasoningService = new ReasoningService(reasonerComponent);
		return generateComponentID(reasonerComponent);
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
	public int setLearningAlgorithm(LearningAlgorithm learningAlgorithm) {
		this.learningAlgorithm = learningAlgorithm;
		return generateComponentID(learningAlgorithm);
	}

	/**
	 * @return the reasoningService
	 */
	public ReasoningService getReasoningService() {
		return reasoningService;
	}

	/**
	 * @param key
	 * @return
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public Component getComponent(int id) {
		return componentIDs.get(id);
	}

	/**
	 * @param e
	 * @return
	 */
	public int addKnowledgeSource(KnowledgeSource ks) {
		knowledgeSources.add(ks);
		return generateComponentID(ks);
		
	}

	public boolean removeKnowledgeSource(int componentID) {
		return knowledgeSources.remove(componentIDs.get(componentID));
	}
	
	/**
	 * @return the knowledgeSources
	 */
	public Set<KnowledgeSource> getKnowledgeSources() {
		return knowledgeSources;
	}
}
