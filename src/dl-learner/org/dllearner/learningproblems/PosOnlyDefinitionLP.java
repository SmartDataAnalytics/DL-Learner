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

import org.dllearner.core.ReasoningService;
import org.dllearner.core.Score;
import org.dllearner.core.owl.Description;
import org.dllearner.utilities.Helper;

/**
 * Definition learning problem from only positive examples.
 * 
 * @author Jens Lehmann
 *
 */
public class PosOnlyDefinitionLP extends PosOnlyLP implements DefinitionLP {
	
	private PosNegDefinitionLP definitionLP;
	
	public PosOnlyDefinitionLP(ReasoningService reasoningService) {
		super(reasoningService);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#getName()
	 */
	public static String getName() {
		return "positive only definition learning problem";
	}
	
	public static String getUsage() {
		return "problem = posOnlyDefinition;";
	}	

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() {
		// by default we test all other instances of the knowledge base
		pseudoNegatives = Helper.difference(reasoningService.getIndividuals(), positiveExamples);
		
		// create an instance of a standard definition learning problem
		// instanciated with pseudo-negatives
		definitionLP = new PosNegDefinitionLP(reasoningService, positiveExamples, pseudoNegatives);
		// TODO: we must make sure that the problem also gets the same 
		// reasoning options (i.e. options are the same up to reversed example sets)
		definitionLP.init();		
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningProblemNew#computeScore(org.dllearner.core.dl.Concept)
	 */
	@Override
	public Score computeScore(Description concept) {
		// TODO need to implement class <code>ScoreOneValued</code>
		return null;
	}
	
	/**
	 * 
	 * @param concept
	 * @return -1 for too weak, otherwise the number of pseudo-negatives (more is usually worse).
	 */
	public int coveredPseudoNegativeExamplesOrTooWeak(Description concept) {
		return definitionLP.coveredNegativeExamplesOrTooWeak(concept);
	}
	
}
