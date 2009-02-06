/**
 * Copyright (C) 2007-2009, Jens Lehmann
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

import java.util.Set;

import org.dllearner.core.Score;
import org.dllearner.core.owl.Individual;

/**
 * The score of a class in ontology engineering.
 * 
 * @author Jens Lehmann
 *
 */
public class ClassScore extends Score {

	private double coverage;
	private double protusion;
	
	public ClassScore(double coverage, double protusion) {
		this.coverage = coverage;
		this.protusion = protusion;
	}
	
	/**
	 * @return the coverage
	 */
	public double getCoverage() {
		return coverage;
	}

	/**
	 * @return the protusion
	 */
	public double getProtusion() {
		return protusion;
	}		
	
	// methods required by score class (not used in this context)
	
	@Override
	public double getAccuracy() {
		throw new UnsupportedOperationException();		
	}

	@Override
	public Set<Individual> getCoveredNegatives() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Individual> getCoveredPositives() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Score getModifiedLengthScore(int newLength) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Individual> getNotCoveredNegatives() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Individual> getNotCoveredPositives() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getScore() {
		throw new UnsupportedOperationException();
	}

}
