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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedSet;

import org.dllearner.core.ComponentInitException;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.Score;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.core.config.InvalidConfigOptionValueException;
import org.dllearner.core.config.StringConfigOption;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.learningproblems.PosNegDefinitionLP;
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
	
	private ReasoningService rs;
	private String filename;
	
	public DBpediaNavigationSuggestor(LearningProblem learningProblem, ReasoningService rs) {
		this.rs=rs;
	}
	
	public static Collection<Class<? extends LearningProblem>> supportedLearningProblems() {
		Collection<Class<? extends LearningProblem>> problems = new LinkedList<Class<? extends LearningProblem>>();
		problems.add(LearningProblem.class);
		return problems;
	}
	
	public DBpediaNavigationSuggestor(PosOnlyDefinitionLP learningProblem, ReasoningService rs) {
		System.out.println("test1");
	}
	
	public DBpediaNavigationSuggestor(PosNegDefinitionLP learningProblem, ReasoningService rs) {
		System.out.println("test2");
	}
	
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		options.add(new StringConfigOption("dumpFileName", "name of the file for the dump"));
		return options;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.ConfigEntry)
	 */
	@Override
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		String name = entry.getOptionName();
		if (name.equals("dumpFileName"))
			filename = (String) entry.getValue();
	}

	@Override
	public void init() throws ComponentInitException {
		// TODO Auto-generated method stub
		SortedSet<Individual> list=rs.getIndividuals();
		Iterator<Individual> iter=list.iterator();
		while (iter.hasNext())
			System.out.println(iter.next().toString());
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
