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
package org.dllearner.core;

import java.util.Set;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.learningproblems.ScoreTwoValued;

/**
 * This represents a class description, which has been
 * evaluated by the learning algorithm, i.e. it has been checked
 * which examples it covers. It can be used as return value for
 * learning algorithms to make it easier for applications to
 * assess how good an offered class description is and how it
 * classifies particular examples.
 * 
 * @author Jens Lehmann
 *
 */
public class EvaluatedDescription {

	private Description description;
	private Score score;
	
	public EvaluatedDescription(Description description, Score score) {
		this.description = description;
		this.score = score;
	}
	
	public EvaluatedDescription(Description description, Set<Individual> posAsPos, Set<Individual> posAsNeg, Set<Individual> negAsPos, Set<Individual> negAsNeg) {
		this.description = description;
		// usually core methods should not depend on methods outside of the core package (except utilities)
		// in this case, this is just a convenience constructor
		score = new ScoreTwoValued(posAsPos, posAsNeg, negAsPos, negAsNeg);
	}

	public Description getDescription() {
		return description;
	}
	
	public int getDescriptionLength() {
		return description.getLength();
	}
	
	public int getDescriptionDepth() {
		return description.getDepth();
	}
	
	/**
	 * @return
	 * @see org.dllearner.core.Score#getAccuracy()
	 */
	public double getAccuracy() {
		return score.getAccuracy();
	}

	/**
	 * @return
	 * @see org.dllearner.core.Score#getCoveredNegatives()
	 */
	public Set<Individual> getCoveredNegatives() {
		return score.getCoveredNegatives();
	}

	/**
	 * @return
	 * @see org.dllearner.core.Score#getCoveredPositives()
	 */
	public Set<Individual> getCoveredPositives() {
		return score.getCoveredPositives();
	}

	/**
	 * @return
	 * @see org.dllearner.core.Score#getNotCoveredNegatives()
	 */
	public Set<Individual> getNotCoveredNegatives() {
		return score.getNotCoveredNegatives();
	}

	/**
	 * @return
	 * @see org.dllearner.core.Score#getNotCoveredPositives()
	 */
	public Set<Individual> getNotCoveredPositives() {
		return score.getNotCoveredPositives();
	}
	
}
