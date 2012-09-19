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
import java.util.TreeSet;
import java.util.Map.Entry;

import org.dllearner.algorithms.BruteForceLearner;
import org.dllearner.algorithms.RandomGuesser;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.el.ELLearningAlgorithm;
import org.dllearner.algorithms.el.ELLearningAlgorithmDisjunctive;
import org.dllearner.algorithms.fuzzydll.FuzzyCELOE;
import org.dllearner.algorithms.gp.GP;
import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.algorithms.refinement.ROLearner;
import org.dllearner.core.AbstractComponent;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.FuzzyPosNegLPStandard;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.learningproblems.PosNegLPStrict;
import org.dllearner.learningproblems.PosOnlyLP;
import org.dllearner.reasoning.DIGReasoner;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.FastRetrievalReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.fuzzydll.FuzzyOWLAPIReasoner;

/**
 * Contains mappings from component classes to strings.
 * Developer please edit the buildMappings() function to add new 
 * CLI mappings.
 * 
 * TODO: For the web service, it may be interesting to hide some components
 * and/or configuration options or even limit the maximum value of certain
 * options.
 * 
 * @author Jens Lehmann
 *
 */
public class ConfMapper {

	// mappings between component classes and their names in conf files
	private static Map<String,Class<? extends AbstractKnowledgeSource>> knowledgeSourceMapping = new TreeMap<String,Class<? extends AbstractKnowledgeSource>>();
	private static Map<String,Class<? extends AbstractReasonerComponent>> reasonerMapping = new TreeMap<String,Class<? extends AbstractReasonerComponent>>();
	private static Map<String,Class<? extends AbstractLearningProblem>> learningProblemMapping = new TreeMap<String,Class<? extends AbstractLearningProblem>>();
	private static Map<String,Class<? extends AbstractCELA>> learningAlgorithmMapping = new TreeMap<String,Class<? extends AbstractCELA>>();
	private static TreeMap<String,Class<? extends AbstractComponent>> componentMapping = new TreeMap<String,Class<? extends AbstractComponent>>();		
	private static HashMap<Class<? extends AbstractComponent>, String> inverseMapping = new HashMap<Class<? extends AbstractComponent>, String>();		
	
	// component types
	private static Map<String,Class<? extends AbstractComponent>> componentTypeMapping = new TreeMap<String,Class<? extends AbstractComponent>>();
	private static Map<Class<? extends AbstractComponent>, String> inverseTypeMapping = new HashMap<Class<? extends AbstractComponent>,String>();	
	
	// set of available components
	private static Set<String> components = new TreeSet<String>();
	
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
		reasonerMapping.put("fuzzyOwlAPIReasoner", FuzzyOWLAPIReasoner.class); // added by Josue
		reasonerMapping.put("fastInstanceChecker", FastInstanceChecker.class);
		reasonerMapping.put("fastRetrievalReasoner", FastRetrievalReasoner.class);
		learningProblemMapping.put("posNegLPStandard", PosNegLPStandard.class);
		learningProblemMapping.put("fuzzyPosNegLPStandard", FuzzyPosNegLPStandard.class); // added by Josue
		learningProblemMapping.put("posNegLPStrict", PosNegLPStrict.class);
		learningProblemMapping.put("classLearning", ClassLearningProblem.class);
		learningProblemMapping.put("posOnlyLP", PosOnlyLP.class);
		learningAlgorithmMapping.put("random", RandomGuesser.class);
		learningAlgorithmMapping.put("bruteForce", BruteForceLearner.class);		
		learningAlgorithmMapping.put("gp", GP.class);
		learningAlgorithmMapping.put("refinement", ROLearner.class);
//		learningAlgorithmMapping.put("refexamples", OCEL.class);
		learningAlgorithmMapping.put("ocel", OCEL.class);
		learningAlgorithmMapping.put("el", ELLearningAlgorithm.class);
		learningAlgorithmMapping.put("disjunctiveEL", ELLearningAlgorithmDisjunctive.class);
		learningAlgorithmMapping.put("celoe", CELOE.class);
		learningAlgorithmMapping.put("fuzzyCeloe", FuzzyCELOE.class); // added by Josue
		
		// you do not need to edit anything below
		
		// build union of all
		componentMapping.putAll(knowledgeSourceMapping);
		componentMapping.putAll(reasonerMapping);
		componentMapping.putAll(learningProblemMapping);
		componentMapping.putAll(learningAlgorithmMapping);
		
		// build inverse mapping
		for(Entry<String, Class<? extends AbstractComponent>> entry : componentMapping.entrySet()) {
			inverseMapping.put(entry.getValue(), entry.getKey());
		}		
		
		components = componentTypeMapping.keySet();
	}
	
	private static void buildKeys() {
		// edit this part manually
		componentTypeMapping.put("import", AbstractKnowledgeSource.class);
		componentTypeMapping.put("reasoner", AbstractReasonerComponent.class);
		componentTypeMapping.put("problem", AbstractLearningProblem.class);
		componentTypeMapping.put("algorithm", AbstractCELA.class);
		
		// you do not need to edit anything below
		// build inverse mapping
		for(Entry<String, Class<? extends AbstractComponent>> entry : componentTypeMapping.entrySet()) {
			inverseTypeMapping.put(entry.getValue(), entry.getKey());
		}
	}
	
	public Class<? extends AbstractKnowledgeSource> getKnowledgeSourceClass(String confString) {
		return knowledgeSourceMapping.get(confString);
	}	
	
	public Class<? extends AbstractReasonerComponent> getReasonerComponentClass(String confString) {
		return reasonerMapping.get(confString);
	}
	
	public Class<? extends AbstractLearningProblem> getLearningProblemClass(String confString) {
		return learningProblemMapping.get(confString);
	}
	
	public Class<? extends AbstractCELA> getLearningAlgorithmClass(String confString) {
		return learningAlgorithmMapping.get(confString);
	}
	
	public Class<? extends AbstractComponent> getComponentClass(String confString) {
		return componentMapping.get(confString);
	}
	
	public String getComponentString(Class<? extends AbstractComponent> clazz) {
		return inverseMapping.get(clazz);
	}
	
	public Class<? extends AbstractComponent> getComponentTypeClass(String typeString) {
		return componentTypeMapping.get(typeString);
	}	
	
	public String getComponentTypeString(Class<? extends AbstractComponent> typeClass) {
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
		return components;
	}	
	
}
