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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.Score;
import org.dllearner.core.config.ConfigEntry;
import org.dllearner.core.configurators.SimpleSuggestionLearningAlgorithmConfigurator;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.Thing;

/**
 * TODO: Javadoc
 * TODO: Extend such that it can really be used as learning algorithm.
 * 
 * @author Christian Kötteritzsch
 *
 */
public class SimpleSuggestionLearningAlgorithm extends LearningAlgorithm implements Runnable {
	
	private SimpleSuggestionLearningAlgorithmConfigurator configurator;
	@Override
	public SimpleSuggestionLearningAlgorithmConfigurator getConfigurator(){
		return configurator;
	}
	
//	private boolean stop = false;
	private Score solutionScore;
	private Description bestSollution;
	private Set<Description> simpleSuggestions;

	public SimpleSuggestionLearningAlgorithm() {
	   	super(null, null);
		this.configurator = new SimpleSuggestionLearningAlgorithmConfigurator(this);
	}

	@Override
	public Description getCurrentlyBestDescription() {
		return bestSollution;
	}

	@Override
	public EvaluatedDescription getCurrentlyBestEvaluatedDescription() {
		return new EvaluatedDescription(bestSollution, solutionScore);
	}

	@Override
	public void stop() {
//		stop = true;
	}

	@Override
	public void start() {

	}

	@Override
	public <T> void applyConfigEntry(ConfigEntry<T> entry) {

	}

	@Override
	public void init() {

	}

	@Override
	public Score getSolutionScore() {
		return solutionScore;
	}

	public void run() {

	}

	public Set<Description> getSimpleSuggestions(ReasoningService rs, Set<Individual> indi) {
		// EXISTS property.TOP
		// ESISTS hasChild
		// EXISTS hasChild.male
		simpleSuggestions = new HashSet<Description>();
		List<ObjectProperty> test = rs.getAtomicRolesList();
		while (test.iterator().hasNext()) {
			test.iterator().next();
			Description d1 = new ObjectSomeRestriction(test.iterator().next(), new Thing());
			test.remove(rs.getAtomicRolesList().iterator().next());
			simpleSuggestions.add(d1);
		}
		return simpleSuggestions;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningAlgorithm#isRunning()
	 */
	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return false;
	}
}
