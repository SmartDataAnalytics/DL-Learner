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

import java.util.Set;

import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;

/**
 * @author Jens Lehmann
 *
 */
public class EvaluatedDescriptionPosOnly extends EvaluatedDescription {

	private static final long serialVersionUID = 4014754537024635033L;
	private ScorePosOnly score2;
	
	public EvaluatedDescriptionPosOnly(Description description, ScorePosOnly score) {
		super(description, score);
		score2 = score;
	}

	@Override
	public String toString() {
		return description.toString() + "(accuracy: " + getAccuracy() + ")";
	}
	
	/**
	 * @see org.dllearner.learningproblems.ScorePosNeg#getCoveredPositives()
	 * @return Positive examples covered by the description.
	 */
	public Set<Individual> getCoveredPositives() {
		return score2.getCoveredInstances();
	}	
	
	public Set<Individual> getNotCoveredPositives() {
		return score2.getNotCoveredPositives();
	}		
	
	public Set<Individual> getAdditionalInstances() {
		return score2.getAdditionalInstances();
	}
}
