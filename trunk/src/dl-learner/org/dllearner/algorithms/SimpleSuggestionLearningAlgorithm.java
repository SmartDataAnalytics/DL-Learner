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

import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.configurators.SimpleSuggestionLearningAlgorithmConfigurator;
import org.dllearner.core.options.ConfigEntry;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.Thing;
import org.dllearner.learningproblems.ScorePosNeg;

/**
 * Algorithm for getting "simple" suggestions, e.g. it tests some of the most likely candidates on whether 
 * they are solutions of a learning problem.
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
	private ScorePosNeg solutionScore;
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
	public EvaluatedDescriptionPosNeg getCurrentlyBestEvaluatedDescription() {
		return new EvaluatedDescriptionPosNeg(bestSollution, solutionScore);
	}

	public static String getName() {
		return "simple suggestion algorithm";
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

//	@Override
	public ScorePosNeg getSolutionScore() {
		return solutionScore;
	}

	public void run() {

	}

	public Set<Description> getSimpleSuggestions(ReasonerComponent rs, Set<Individual> indi) {
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
