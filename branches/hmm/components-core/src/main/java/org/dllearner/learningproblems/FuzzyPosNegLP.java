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

package org.dllearner.learningproblems;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.options.BooleanConfigOption;
import org.dllearner.core.options.CommonConfigMappings;
import org.dllearner.core.options.CommonConfigOptions;
import org.dllearner.core.options.ConfigEntry;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.InvalidConfigOptionValueException;
import org.dllearner.core.options.StringConfigOption;
import org.dllearner.core.options.StringSetConfigOption;
import org.dllearner.core.options.fuzzydll.FuzzyExample;
import org.dllearner.core.options.fuzzydll.ObjectSetConfigOption;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.fuzzydll.FuzzyIndividual;
import org.dllearner.utilities.Helper;

/**
 * @author Jens Lehmann
 *
 */
public abstract class FuzzyPosNegLP extends AbstractLearningProblem {
	
	protected SortedSet<Individual> positiveExamples;
	protected SortedSet<Individual> negativeExamples;
	protected SortedSet<Individual> allExamples;
	
	protected SortedSet<FuzzyIndividual> fuzzyExamples;

	protected Map<Individual,Double> fuzzyEx;
	
	public Map<Individual, Double> getFuzzyEx() {
		return fuzzyEx;
	}

	public void setFuzzyEx(Map<Individual, Double> fuzzyEx) {
		fuzzyExamples = new TreeSet<FuzzyIndividual>();
		
		Iterator it = fuzzyEx.keySet().iterator();		
		
		while (it.hasNext()) {
			Individual i = (Individual) it.next();
			this.fuzzyExamples.add(new FuzzyIndividual(i.getName(), fuzzyEx.get(i).doubleValue()));
		}
	}

	protected boolean useRetrievalForClassification = false;
	protected UseMultiInstanceChecks useMultiInstanceChecks = UseMultiInstanceChecks.TWOCHECKS;
	protected double percentPerLengthUnit = 0.05;
	protected double totalTruth = 0;

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
	
	public FuzzyPosNegLP() {
		
	}
	
	public FuzzyPosNegLP(AbstractReasonerComponent reasoningService) {
		super(reasoningService);
	}
	
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		// changed by Josue
		options.add(new ObjectSetConfigOption("fuzzyExamples",
				"fuzzy examples",null, true, false));		
		// TODO positiveExamples and negativeExamples must desapear here
		options.add(new StringSetConfigOption("positiveExamples",
				"positive examples",null, true, false));
		options.add(new StringSetConfigOption("negativeExamples",
				"negative examples",null, true, false));
		options.add(new BooleanConfigOption("useRetrievalForClassficiation", 
				"Specifies whether to use retrieval or instance checks for testing a concept. - NO LONGER FULLY SUPPORTED.", false));
		options.add(CommonConfigOptions.getPercentPerLenghtUnitOption(0.05));
		StringConfigOption multiInstanceChecks = new StringConfigOption("useMultiInstanceChecks", "See UseMultiInstanceChecks enum. - NO LONGER FULLY SUPPORTED.","twoChecks");
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
		// added by Josue
		if (name.equals("fuzzyExamples")){
			fuzzyExamples = CommonConfigMappings.getFuzzyIndividualSet((Set<FuzzyExample>) entry.getValue());
			for (FuzzyIndividual fuzzyExample : fuzzyExamples) {
				totalTruth += fuzzyExample.getTruthDegree();
			}
		}
		// TODO delete positiveExamples & negativeExamples
		else if (name.equals("positiveExamples"))
			positiveExamples = CommonConfigMappings
					.getIndividualSet((Set<String>) entry.getValue()); // changed by Josue
		else if (name.equals("negativeExamples"))
			negativeExamples = CommonConfigMappings
					.getIndividualSet((Set<String>) entry.getValue()); // changed by Josue
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
		// commented by Josue as now there's no need of + and - examples (more code need to be deleted in this sense)
		// allExamples = Helper.union(positiveExamples, negativeExamples);
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

	public SortedSet<FuzzyIndividual> getFuzzyExamples() {
		return fuzzyExamples;
	}

	public void setFuzzyExamples(SortedSet<FuzzyIndividual> fuzzyExamples) {
		this.fuzzyExamples = fuzzyExamples;
	}
	
}
