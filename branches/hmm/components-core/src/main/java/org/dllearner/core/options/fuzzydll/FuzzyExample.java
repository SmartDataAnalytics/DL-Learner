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

package org.dllearner.core.options.fuzzydll;

public class FuzzyExample implements Comparable<FuzzyExample> {
	private String exampleName;
	private double fuzzyDegree;
	
	public FuzzyExample(String i, double d) {
		this.exampleName = i;
		this.fuzzyDegree = d;
	}

	@Override
	public int compareTo(FuzzyExample fe) {
		return this.getExampleName().compareTo(fe.getExampleName());
	}

	public String getExampleName() {
		return exampleName;
	}

	public void setExampleName(String individual) {
		this.exampleName = individual;
	}

	public double getFuzzyDegree() {
		return fuzzyDegree;
	}

	public void setFuzzyDegree(double fuzzyDegree) {
		this.fuzzyDegree = fuzzyDegree;
	}
}
