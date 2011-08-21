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

import org.dllearner.core.Score;
import org.dllearner.core.owl.Individual;

/**
 * The score class is used to store how well a class description did
 * on a learning problem.
 * 
 * @author Jens Lehmann
 *
 */
public abstract class ScorePosNeg extends Score {
	
	private static final long serialVersionUID = -4646131678864109469L;

	public abstract double getScoreValue();
	
	// example coverage
	public abstract Set<Individual> getCoveredPositives();
	public abstract Set<Individual> getCoveredNegatives();
	public abstract Set<Individual> getNotCoveredPositives();
	public abstract Set<Individual> getNotCoveredNegatives();	
	
	/**
	 * The score of a concept depends on how good it classifies the
	 * examples of a learning problem and the length of the concept
	 * itself. If a given concept is known to have equal classification
	 * properties than the concept this score object is based on, then
	 * this method can be used to calculate its score value by using the
	 * length of this concept as parameter.
	 * 
	 * @param newLength Length of the concept.
	 * @return Score.
	 */
	public abstract ScorePosNeg getModifiedLengthScore(int newLength);
	
}
