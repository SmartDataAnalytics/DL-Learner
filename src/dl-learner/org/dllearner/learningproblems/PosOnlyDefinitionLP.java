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
import java.util.Set;
import java.util.SortedSet;

import org.dllearner.core.CommonConfigMappings;
import org.dllearner.core.ConfigEntry;
import org.dllearner.core.ConfigOption;
import org.dllearner.core.InvalidConfigOptionValueException;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.Score;
import org.dllearner.core.StringSetConfigOption;
import org.dllearner.core.dl.Concept;
import org.dllearner.core.dl.Individual;
import org.dllearner.utilities.Helper;

/**
 * Definition learning problem from only positive examples.
 * 
 * @author Jens Lehmann
 *
 */
public class PosOnlyDefinitionLP extends PosOnlyLP implements DefinitionLP {

	protected SortedSet<Individual> positiveExamples;
	protected SortedSet<Individual> pseudoNegatives;
	
	private PosNegDefinitionLP definitionLP;
	
	public PosOnlyDefinitionLP(ReasoningService reasoningService) {
		super(reasoningService);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.ConfigEntry)
	 */
	@Override
	@SuppressWarnings( { "unchecked" })
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		String name = entry.getOptionName();
		if (name.equals("positiveExamples"))
			positiveExamples = CommonConfigMappings
					.getIndividualSet((Set<String>) entry.getValue());
	}
	
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(new StringSetConfigOption("positiveExamples",
				"positive examples"));
		return options;
	}	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#getName()
	 */
	public static String getName() {
		return "positive only definition learning problem";
	}
	
	/**
	 * @return the positiveExamples
	 */
	public SortedSet<Individual> getPositiveExamples() {
		return positiveExamples;
	}

	/**
	 * @return the pseudoNegatives
	 */
	public SortedSet<Individual> getPseudoNegatives() {
		return pseudoNegatives;
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
	public Score computeScore(Concept concept) {
		// TODO need to implement class <code>ScoreOneValued</code>
		return null;
	}
	
	/**
	 * 
	 * @param concept
	 * @return -1 for too weak, otherwise the number of pseudo-negatives (more is usually worse).
	 */
	public int coveredPseudoNegativeExamplesOrTooWeak(Concept concept) {
		return definitionLP.coveredNegativeExamplesOrTooWeak(concept);
	}
	
}
