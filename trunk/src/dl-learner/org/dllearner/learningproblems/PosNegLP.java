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
import org.dllearner.core.ReasoningService;
import org.dllearner.core.config.BooleanConfigOption;
import org.dllearner.core.config.CommonConfigMappings;
import org.dllearner.core.config.CommonConfigOptions;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.config.StringConfigOption;
import org.dllearner.core.config.StringSetConfigOption;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.utilities.Helper;

/**
 * @author Jens Lehmann
 *
 */
public abstract class PosNegLP extends LearningProblem {
	
	protected SortedSet<Individual> positiveExamples;
	protected SortedSet<Individual> negativeExamples;
	protected SortedSet<Individual> allExamples;
	
	protected boolean useRetrievalForClassification = false;
	protected UseMultiInstanceChecks useMultiInstanceChecks = UseMultiInstanceChecks.TWOCHECKS;
	protected double percentPerLengthUnit = 0.05;

	/**
	 * If instance checks are used for testing concepts (e.g. no retrieval), then
	 * there are several options to do this. The enumeration lists the supported
	 * options. These options are only important if the reasoning mechanism 
	 * supports sending several reasoning requests at once as it is the case for
	 * DIG reasoners.
	 * 
	 * @author Jens Lehmann
	 *
	 */
	public enum UseMultiInstanceChecks {
		/**
		 * Perform a separate instance check for each example.
		 */
		NEVER,
		/**
		 * Perform one instance check for all positive and one instance check
		 * for all negative examples.
		 */
		TWOCHECKS,
		/**
		 * Perform all instance checks at once.
		 */
		ONECHECK
	};
	
	public PosNegLP(ReasoningService reasoningService) {
		super(reasoningService);
	}
	
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(new StringSetConfigOption("positiveExamples",
				"positive examples",null, true, false));
		options.add(new StringSetConfigOption("negativeExamples",
				"negative examples",null, true, false));
		options.add(new BooleanConfigOption("useRetrievalForClassficiation", 
				"Specifies whether to use retrieval or instance checks for testing a concept.", false));
		options.add(CommonConfigOptions.getPercentPerLenghtUnitOption(0.05));
		StringConfigOption multiInstanceChecks = new StringConfigOption("useMultiInstanceChecks", "See UseMultiInstanceChecks enum.","twoChecks");
		multiInstanceChecks.setAllowedValues(new String[] {"never", "twoChecks", "oneCheck"});
		options.add(multiInstanceChecks);
		return options;
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
		else if (name.equals("negativeExamples"))
			negativeExamples = CommonConfigMappings
					.getIndividualSet((Set<String>) entry.getValue());
		else if (name.equals("useRetrievalForClassficiation")) {
			useRetrievalForClassification = (Boolean) entry.getValue();
		} else if (name.equals("percentPerLengthUnit"))
			percentPerLengthUnit = (Double) entry.getValue();
		else if (name.equals("useMultiInstanceChecks")) {
			String value = (String) entry.getValue();
			if(value.equals("oneCheck"))
				useMultiInstanceChecks = UseMultiInstanceChecks.ONECHECK;
			else if(value.equals("twoChecks"))
				useMultiInstanceChecks = UseMultiInstanceChecks.TWOCHECKS;
			else
				useMultiInstanceChecks = UseMultiInstanceChecks.NEVER;	
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() {
		allExamples = Helper.union(positiveExamples, negativeExamples);
	}
	
	public SortedSet<Individual> getNegativeExamples() {
		return negativeExamples;
	}

	public SortedSet<Individual> getPositiveExamples() {
		return positiveExamples;
	}
	
	public void setNegativeExamples(SortedSet<Individual> set) {
		this.negativeExamples=set;
	}

	public void setPositiveExamples(SortedSet<Individual> set) {
		this.positiveExamples=set;
	}
	
	public abstract int coveredNegativeExamplesOrTooWeak(Description concept);

	public double getPercentPerLengthUnit() {
		return percentPerLengthUnit;
	}
	
}
