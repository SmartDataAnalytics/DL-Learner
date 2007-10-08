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
package org.dllearner.learningproblems;

import java.util.Collection;
import java.util.LinkedList;

import org.dllearner.core.BooleanConfigOption;
import org.dllearner.core.ConfigOption;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.Score;
import org.dllearner.core.StringSetConfigOption;
import org.dllearner.core.dl.Concept;
import org.dllearner.core.dl.Negation;

/**
 * The aim of this learning problem is to find an appropriate inclusion axiom
 * given positive and negative examples. 
 * 
 * This is similar to the definition learning problem, but here the positive 
 * and negative examples usually do not follow when the inclusion is added to 
 * the knowledge base. This raises the question how the quality of a concept
 * with respect to this learning problem can be measured. Due to the fact that
 * the inclusion does not entail examples, we have to look at the negation of
 * the concept we are looking at. For a good solution it is the case that
 * no positive examples follow from the negated concept, the negative 
 * examples follow from it. This means that the standard definition learning
 * problem and the inclusion learning problem can be seen as two possible
 * weakenings of the strict definition learning problem. (Of course, both problems
 * can yield the same solution.) 
 * 
 * @author Jens Lehmann
 *
 */
public class PosNegInclusionLP extends PosNegLP implements InclusionLP {

	private PosNegDefinitionLP definitionLP;
	
	public PosNegInclusionLP(ReasoningService reasoningService) {
		super(reasoningService);
	}

	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(new StringSetConfigOption("positiveExamples",
				"positive examples"));
		options.add(new StringSetConfigOption("negativeExamples",
				"negative examples"));
		options.add(new BooleanConfigOption("useRetrievalForClassficiation", 
				"Specifies whether to use retrieval or instance checks for testing a concept."));
		return options;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#getName()
	 */
	public static String getName() {
		return "inclusion learning problem";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() {
		super.init();
		definitionLP = new PosNegDefinitionLP(reasoningService, negativeExamples, positiveExamples);
	}
	
	/**
	 * Calls the same method on the standard definition learning problem, but 
	 * negates the concept before and permutes positive and negative examples.
	 * 
	 * @see org.dllearner.learningproblems.PosNegLP#coveredNegativeExamplesOrTooWeak(org.dllearner.core.dl.Concept)
	 */
	@Override
	public int coveredNegativeExamplesOrTooWeak(Concept concept) {
		return definitionLP.coveredNegativeExamplesOrTooWeak(new Negation(concept));
	}

	/** 
	 * Calls the same method on the standard definition learning problem, but 
	 * negates the concept before and permutes positive and negative examples.
	 * 
	 * @see org.dllearner.core.LearningProblemNew#computeScore(org.dllearner.core.dl.Concept)
	 */
	@Override
	public Score computeScore(Concept concept) {
		return definitionLP.computeScore(new Negation(concept));
	}

}
