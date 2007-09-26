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
import org.dllearner.core.IntegerConfigOption;
import org.dllearner.core.InvalidConfigOptionValueException;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.StringSetConfigOption;
import org.dllearner.core.dl.Individual;

/**
 * @author Jens Lehmann
 *
 */
public class DefinitionLPTwoValued extends DefinitionLP {

	private ReasoningService rs;
	
	private SortedSet<Individual> positiveExamples;
	private SortedSet<Individual> negativeExamples;
	
	public DefinitionLPTwoValued(ReasoningService rs) {
		this.rs = rs;
	}
	
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(new StringSetConfigOption("positiveExamples"));
		options.add(new StringSetConfigOption("negativeExamples"));
		return options;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.ConfigEntry)
	 */
	@Override
	@SuppressWarnings({"unchecked"})
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		String name = entry.getOptionName();
		if(name.equals("positiveExamples"))
			positiveExamples = CommonConfigMappings.getIndividualSet((Set<String>) entry.getValue());
		else if(name.equals("negativeExamples"))
			negativeExamples = CommonConfigMappings.getIndividualSet((Set<String>) entry.getValue());
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
	
	public SortedSet<Individual> getNegativeExamples() {
		return negativeExamples;
	}

	public SortedSet<Individual> getPositiveExamples() {
		return positiveExamples;
	}

	// TODO: remove? reasoning service should probably not be accessed via 
	// learning problem
	public ReasoningService getReasoningService() {
		return rs;
	}	
}
