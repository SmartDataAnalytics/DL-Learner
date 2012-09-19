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

package org.dllearner.utilities.components;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.FastInstanceChecker;

/**
 * A mix of components, which are typically combined to create a full 
 * learning task.
 * 
 * Add more constructors if you like (they should be useful in general,
 * not just for a very specific scenario).
 * 
 * @author Jens Lehmann
 *
 */
public class ComponentCombo {

	private Set<AbstractKnowledgeSource> sources;
	private AbstractReasonerComponent reasoner;
	private AbstractLearningProblem problem;
	private AbstractCELA algorithm;
	
	/**
	 * Builds a component combination object from the specified components. 
	 * @param source A knowledge source.
	 * @param reasoner A reasoner.
	 * @param problem A learning problem.
	 * @param algorithm A learning algorithm.
	 */
	public ComponentCombo(AbstractKnowledgeSource source, AbstractReasonerComponent reasoner, AbstractLearningProblem problem, AbstractCELA algorithm) {
		this(getSourceSet(source), reasoner, problem, algorithm);
	}	
	
	/**
	 * Builds a component combination object from the specified components. 
	 * @param sources A set of knowledge sources.
	 * @param reasoner A reasoner.
	 * @param problem A learning problem.
	 * @param algorithm A learning algorithm.
	 */	
	public ComponentCombo(Set<AbstractKnowledgeSource> sources, AbstractReasonerComponent reasoner, AbstractLearningProblem problem, AbstractCELA algorithm) {
		this.sources = sources;
		this.reasoner = reasoner;
		this.problem = problem;
		this.algorithm = algorithm;
	}		
	
	private static Set<AbstractKnowledgeSource> getSourceSet(AbstractKnowledgeSource source) {
		Set<AbstractKnowledgeSource> sources = new HashSet<AbstractKnowledgeSource>();
		sources.add(source);
		return sources;
	}

	/**
	 * Builds a standard combination of components. Currently, this is an OWL
	 * File, the FastInstanceChecker reasoning algorithm, a definition learning
	 * problem with positive and negative examples, and the example based
	 * refinement algorithm.
	 * @param owlFile URL of an OWL file (background knowledge).
	 * @param posExamples Set of positive examples.
	 * @param negExamples Set of negative examples.
	 */
	public ComponentCombo(URL owlFile, Set<String> posExamples, Set<String> negExamples) {
		ComponentManager cm = ComponentManager.getInstance();
		AbstractKnowledgeSource source = cm.knowledgeSource(OWLFile.class);
		sources = getSourceSet(source);
		reasoner = cm.reasoner(FastInstanceChecker.class, source);
		problem = cm.learningProblem(PosNegLPStandard.class, reasoner);
		cm.applyConfigEntry(problem, "positiveExamples", posExamples);
		cm.applyConfigEntry(problem, "negativeExamples", negExamples);
		try {
			algorithm = cm.learningAlgorithm(OCEL.class, problem, reasoner);
		} catch (LearningProblemUnsupportedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialise all components.
	 * @throws ComponentInitException Thrown if a component could not be initialised properly.
	 */
	public void initAll() throws ComponentInitException {
		for(AbstractKnowledgeSource source : sources) {
			source.init();
		}
		reasoner.init();
		problem.init();
		algorithm.init();
	}
	
	/**
	 * @return the sources
	 */
	public Set<AbstractKnowledgeSource> getSources() {
		return sources;
	}

	/**
	 * @return the reasoner
	 */
	public AbstractReasonerComponent getReasoner() {
		return reasoner;
	}

	/**
	 * @return the problem
	 */
	public AbstractLearningProblem getProblem() {
		return problem;
	}

	/**
	 * @return the algorithm
	 */
	public AbstractCELA getAlgorithm() {
		return algorithm;
	}	
	

}
