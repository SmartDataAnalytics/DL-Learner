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
package org.dllearner.cli;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.dllearner.algorithms.BruteForceLearner;
import org.dllearner.algorithms.DBpediaNavigationSuggestor;
import org.dllearner.algorithms.RandomGuesser;
import org.dllearner.algorithms.gp.GP;
import org.dllearner.algorithms.refexamples.ExampleBasedROLComponent;
import org.dllearner.algorithms.refinement.ROLearner;
import org.dllearner.core.Component;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponentOld;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.learningproblems.PosNegInclusionLP;
import org.dllearner.learningproblems.PosOnlyDefinitionLP;
import org.dllearner.reasoning.DIGReasoner;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.FastRetrievalReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;

/**
 * Contains mappings from component classes to strings.
 * Developer please edit the buildMappings() function to add new 
 * CLI mappings.
 * 
 * @author Jens Lehmann
 *
 */
public class ConfMapper {

	// mappings between component classes and their names in conf files
	private static Map<String,Class<? extends KnowledgeSource>> knowledgeSourceMapping = new TreeMap<String,Class<? extends KnowledgeSource>>();
	private static Map<String,Class<? extends ReasonerComponentOld>> reasonerMapping = new TreeMap<String,Class<? extends ReasonerComponentOld>>();
	private static Map<String,Class<? extends LearningProblem>> learningProblemMapping = new TreeMap<String,Class<? extends LearningProblem>>();
	private static Map<String,Class<? extends LearningAlgorithm>> learningAlgorithmMapping = new TreeMap<String,Class<? extends LearningAlgorithm>>();
	private static TreeMap<String,Class<? extends Component>> componentMapping = new TreeMap<String,Class<? extends Component>>();		
	private static HashMap<Class<? extends Component>, String> inverseMapping = new HashMap<Class<? extends Component>, String>();		
	
	// component types
	private static Map<String,Class<? extends Component>> componentTypeMapping = new TreeMap<String,Class<? extends Component>>();
	private static Map<Class<? extends Component>, String> inverseTypeMapping = new HashMap<Class<? extends Component>,String>();	
	
	public ConfMapper() {
		buildMappings();
		buildKeys();
	}
	
	private static void buildMappings() {
		// edit this part manually
		knowledgeSourceMapping.put("owlfile", OWLFile.class);
		knowledgeSourceMapping.put("sparql", SparqlKnowledgeSource.class);
		reasonerMapping.put("digReasoner", DIGReasoner.class);
		reasonerMapping.put("owlAPIReasoner", OWLAPIReasoner.class);
		reasonerMapping.put("fastInstanceChecker", FastInstanceChecker.class);
		reasonerMapping.put("fastRetrievalReasoner", FastRetrievalReasoner.class);
		learningProblemMapping.put("posNegDefinitionLP", PosNegDefinitionLP.class);
		learningProblemMapping.put("posNegInclusionLP", PosNegInclusionLP.class);
		learningProblemMapping.put("posOnlyDefinitionLP", PosOnlyDefinitionLP.class);
		learningAlgorithmMapping.put("random", RandomGuesser.class);
		learningAlgorithmMapping.put("bruteForce", BruteForceLearner.class);		
		learningAlgorithmMapping.put("gp", GP.class);
		learningAlgorithmMapping.put("refinement", ROLearner.class);
		learningAlgorithmMapping.put("refexamples", ExampleBasedROLComponent.class);
		learningAlgorithmMapping.put("dbpediaNavigationSuggestor", DBpediaNavigationSuggestor.class);	
		
		// you do not need to edit anything below
		
		// build union of all
		componentMapping.putAll(knowledgeSourceMapping);
		componentMapping.putAll(reasonerMapping);
		componentMapping.putAll(learningProblemMapping);
		componentMapping.putAll(learningAlgorithmMapping);
		
		// build inverse mapping
		for(Entry<String, Class<? extends Component>> entry : componentMapping.entrySet()) {
			inverseMapping.put(entry.getValue(), entry.getKey());
		}		
	}
	
	private static void buildKeys() {
		// edit this part manually
		componentTypeMapping.put("import", KnowledgeSource.class);
		componentTypeMapping.put("reasoner", ReasonerComponentOld.class);
		componentTypeMapping.put("problem", LearningProblem.class);
		componentTypeMapping.put("algorithm", LearningAlgorithm.class);
		
		// you do not need to edit anything below
		// build inverse mapping
		for(Entry<String, Class<? extends Component>> entry : componentTypeMapping.entrySet()) {
			inverseTypeMapping.put(entry.getValue(), entry.getKey());
		}
	}
	
	public Class<? extends KnowledgeSource> getKnowledgeSourceClass(String confString) {
		return knowledgeSourceMapping.get(confString);
	}	
	
	public Class<? extends ReasonerComponentOld> getReasonerComponentClass(String confString) {
		return reasonerMapping.get(confString);
	}
	
	public Class<? extends LearningProblem> getLearningProblemClass(String confString) {
		return learningProblemMapping.get(confString);
	}
	
	public Class<? extends LearningAlgorithm> getLearningAlgorithmClass(String confString) {
		return learningAlgorithmMapping.get(confString);
	}
	
	public Class<? extends Component> getComponentClass(String confString) {
		return componentMapping.get(confString);
	}
	
	public String getComponentString(Class<? extends Component> clazz) {
		return inverseMapping.get(clazz);
	}
	
	public Class<? extends Component> getComponentTypeClass(String typeString) {
		return componentTypeMapping.get(typeString);
	}	
	
	public String getComponentTypeString(Class<? extends Component> typeClass) {
		return inverseTypeMapping.get(typeClass);
	}
	
	public Set<String> getKnowledgeSources() {
		return knowledgeSourceMapping.keySet();
	}
	
	public Set<String> getReasoners() {
		return reasonerMapping.keySet();
	}
	
	public Set<String> getLearningProblems() {
		return learningProblemMapping.keySet();
	}
	
	public Set<String> getLearningAlgorithms() {
		return learningAlgorithmMapping.keySet();
	}	
	
	public Set<String> getComponents() {
		return componentMapping.keySet();
	}	
	
}
