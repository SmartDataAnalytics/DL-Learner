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

import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.configurators.ComponentFactory;
import org.dllearner.core.configurators.PosOnlyLPConfigurator;
import org.dllearner.core.options.CommonConfigMappings;
import org.dllearner.core.options.ConfigEntry;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.InvalidConfigOptionValueException;
import org.dllearner.core.options.StringSetConfigOption;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.datastructures.SetManipulation;

/**
 * A learning problem, where we learn from positive examples only.
 * 
 * @author Jens Lehmann
 *
 */
public abstract class PosOnlyLP extends LearningProblem {

	protected SortedSet<Individual> positiveExamples;
	protected SortedSet<Individual> pseudoNegatives;	

	private PosNegLPStandard definitionLP;
	private PosOnlyLPConfigurator configurator;
	
	@Override
	public PosOnlyLPConfigurator getConfigurator(){
		return configurator;
	}	
	
	public PosOnlyLP(ReasonerComponent reasoningService) {
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
				"positive examples", null, true, false));
		return options;
	}		
	
	public static String getName() {
		return "pos only learning problem";
	}

	
	@Override
	public void init() {
		// by default we test all other instances of the knowledge base
		pseudoNegatives = Helper.difference(reasoner.getIndividuals(), positiveExamples);
		
		// create an instance of a standard definition learning problem
		// instanciated with pseudo-negatives
		definitionLP = ComponentFactory.getPosNegLPStandard(
				reasoner, 
				SetManipulation.indToString(positiveExamples), 
				SetManipulation.indToString(pseudoNegatives));
		//definitionLP = new PosNegDefinitionLP(reasoningService, positiveExamples, pseudoNegatives);
		// TODO: we must make sure that the problem also gets the same 
		// reasoning options (i.e. options are the same up to reversed example sets)
		definitionLP.init();		
	}
	
	public SortedSet<Individual> getPositiveExamples() {
		return positiveExamples;
	}	
	
	/**
	 * @return the pseudoNegatives
	 */
	public SortedSet<Individual> getPseudoNegatives() {
		return pseudoNegatives;
	}	
	

	public int coveredPseudoNegativeExamplesOrTooWeak(Description concept) {
		return definitionLP.coveredNegativeExamplesOrTooWeak(concept);
	}
	
}
