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

import java.util.SortedSet;

import org.dllearner.core.ConfigEntry;
import org.dllearner.core.InvalidConfigOptionValueException;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.Score;
import org.dllearner.core.dl.Concept;
import org.dllearner.core.dl.Individual;

/**
 * @author Jens Lehmann
 *
 */
public class DefinitionLPThreeValued extends DefinitionLP {

	public DefinitionLPThreeValued(ReasoningService reasoningService) {
		super(reasoningService);
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#getName()
	 */
	public static String getName() {
		return "two valued definition learning problem";
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.ConfigEntry)
	 */
	@Override
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.learningproblems.DefinitionLP#computeScore(org.dllearner.core.dl.Concept)
	 */
	@Override
	public Score computeScore(Concept concept) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.learningproblems.DefinitionLP#coveredNegativeExamplesOrTooWeak(org.dllearner.core.dl.Concept)
	 */
	@Override
	public int coveredNegativeExamplesOrTooWeak(Concept concept) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.learningproblems.DefinitionLP#getNegativeExamples()
	 */
	@Override
	public SortedSet<Individual> getNegativeExamples() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.learningproblems.DefinitionLP#getPositiveExamples()
	 */
	@Override
	public SortedSet<Individual> getPositiveExamples() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.learningproblems.DefinitionLP#getReasoningService()
	 */
	@Override
	public ReasoningService getReasoningService() {
		// TODO Auto-generated method stub
		return null;
	}

	public SortedSet<Individual> getNeutralExamples() {
		throw new UnsupportedOperationException();
	}
}
