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
package org.dllearner.algorithms;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.Score;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.owl.Description;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosOnlyDefinitionLP;

/**
 * The DBpedia Navigation suggestor takes a knowledge fragment extracted
 * from DBpedia, performs some preprocessing steps, invokes a learning
 * algorithm, and then performs postprocessing steps. It does not 
 * implement a completely new learning algorithm itself, but uses the
 * example based refinement operator learning algorithm.
 * 
 * @author Jens Lehmann
 *
 */
public class DBpediaNavigationSuggestor extends LearningAlgorithm {

	public DBpediaNavigationSuggestor(PosNegLP learningProblem, ReasoningService rs) {

	}
	
	public DBpediaNavigationSuggestor(PosOnlyDefinitionLP learningProblem, ReasoningService rs) {

	}	
	
	@Override
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		// TODO Auto-generated method stub
	}

	@Override
	public void init() throws ComponentInitException {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void start() {
		// TODO Auto-generated method stub
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}	
	
	@Override
	public Description getBestSolution() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Score getSolutionScore() {
		// TODO Auto-generated method stub
		return null;
	}	
 
}
