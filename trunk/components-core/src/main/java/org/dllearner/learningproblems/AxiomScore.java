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

import org.dllearner.core.Score;

public class AxiomScore extends Score{
	
	private static final long serialVersionUID = 555252118489924570L;
	private double accuracy;
	private double confidence;
	
	public AxiomScore(double accuracy) {
		this.accuracy = accuracy;
	}
	
	public AxiomScore(double accuracy, double confidence) {
		this.accuracy = accuracy;
	}

	@Override
	public double getAccuracy() {
		return accuracy;
	}
	
	public double getConfidence(){
		return confidence;
	}

}
