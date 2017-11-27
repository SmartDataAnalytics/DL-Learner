/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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

import org.dllearner.core.Score;

/**
 * A simple score implementation which only contains the accuracy.
 *
 * @author Lorenz Buehmann
 *
 */
public class ScoreSimple extends Score {
	
	/**
	 * A minimum simple score object.
	 */
	public static ScoreSimple MIN = new ScoreSimple(Double.NEGATIVE_INFINITY);
	/**
	 * A maximum simple score object.
	 */
	public static ScoreSimple MAX = new ScoreSimple(Double.POSITIVE_INFINITY);
	
	private double accuracy;
	
	public ScoreSimple(double accuracy) {
		this.accuracy = accuracy;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Score#getAccuracy()
	 */
	@Override
	public double getAccuracy() {
		return accuracy;
	}

	@Override
	public String toString() {
		return "ScoreSimple{" +
				"accuracy=" + accuracy +
				'}';
	}
}
