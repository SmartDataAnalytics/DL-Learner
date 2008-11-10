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
package org.dllearner.utilities.learn;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.algorithms.refexamples.ExampleBasedROLComponent;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.reasoning.FastInstanceChecker;

/**
 * A mix of components, which are typically combined to create a full 
 * learning task.
 * 
 * @author Jens Lehmann
 *
 */
public class ComponentCombo {

	private Set<KnowledgeSource> sources;
	private ReasonerComponent reasoner;
	private LearningProblem problem;
	private LearningAlgorithm algorithm;
	
	public ComponentCombo(KnowledgeSource source, ReasonerComponent reasoner, LearningProblem problem, LearningAlgorithm algorithm) {
		this(getSourceSet(source), reasoner, problem, algorithm);
	}	
	
	public ComponentCombo(Set<KnowledgeSource> sources, ReasonerComponent reasoner, LearningProblem problem, LearningAlgorithm algorithm) {
		this.sources = sources;
		this.reasoner = reasoner;
		this.problem = problem;
		this.algorithm = algorithm;
	}		
	
	private static Set<KnowledgeSource> getSourceSet(KnowledgeSource source) {
		Set<KnowledgeSource> sources = new HashSet<KnowledgeSource>();
		sources.add(source);
		return sources;
	}
	
	public ComponentCombo(URL owlFile, Set<String> posExamples, Set<String> negExamples) {
		ComponentManager cm = ComponentManager.getInstance();
		KnowledgeSource source = cm.knowledgeSource(OWLFile.class);
		sources = getSourceSet(source);
		reasoner = cm.reasoner(FastInstanceChecker.class, source);
		problem = cm.learningProblem(PosNegDefinitionLP.class, reasoner);
		cm.applyConfigEntry(problem, "positiveExamples", posExamples);
		cm.applyConfigEntry(problem, "negativeExamples", negExamples);
		try {
			algorithm = cm.learningAlgorithm(ExampleBasedROLComponent.class, problem, reasoner);
		} catch (LearningProblemUnsupportedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the sources
	 */
	public Set<KnowledgeSource> getSources() {
		return sources;
	}

	/**
	 * @return the reasoner
	 */
	public ReasonerComponent getReasoner() {
		return reasoner;
	}

	/**
	 * @return the problem
	 */
	public LearningProblem getProblem() {
		return problem;
	}

	/**
	 * @return the algorithm
	 */
	public LearningAlgorithm getAlgorithm() {
		return algorithm;
	}	
	

}
