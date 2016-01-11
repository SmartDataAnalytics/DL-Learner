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

public class AxiomScore extends Score{
	
	private static final long serialVersionUID = 555252118489924570L;
	private double accuracy;
	private double confidence;
	
	private int nrOfPositiveExamples = -1;
	private int nrOfNegativeExamples = -1;
	
	private boolean sampleBased;
	
	public AxiomScore(double accuracy) {
		this(accuracy, false);
	}
	
	public AxiomScore(double accuracy, boolean sampleBased) {
		this.accuracy = accuracy;
		this.sampleBased = sampleBased;
	}
	
	public AxiomScore(double accuracy, double confidence) {
		this(accuracy, confidence, false);
	}
	
	public AxiomScore(double accuracy, double confidence, boolean sampleBased) {
		this.accuracy = accuracy;
		this.confidence = confidence;
		this.sampleBased = sampleBased;
	}
	
	public AxiomScore(double accuracy, double confidence, int nrOfPositiveExamples, int nrOfNegativeExamples) {
		this(accuracy, confidence, nrOfPositiveExamples, nrOfNegativeExamples, false);
	}
	
	public AxiomScore(double accuracy, double confidence, int nrOfPositiveExamples, int nrOfNegativeExamples, boolean sampleBased) {
		this.accuracy = accuracy;
		this.confidence = confidence;
		this.nrOfPositiveExamples = nrOfPositiveExamples;
		this.nrOfNegativeExamples = nrOfNegativeExamples;
		this.sampleBased = sampleBased;
	}

	@Override
	public double getAccuracy() {
		return accuracy;
	}

	/**
	 * @return the confidence value.
	 */
	public double getConfidence(){
		return confidence;
	}
	
	/**
	 * @return whether the score was computed only based on a sample of the knowledge base
	 */
	public boolean isSampleBased() {
		return sampleBased;
	}

	/**
	 * @return the total number of examples used to compute the score.
	 */
	public int getTotalNrOfExamples() {
		return nrOfPositiveExamples + nrOfNegativeExamples;
	}
	
	/**
	 * @return the number of positive examples used to compute the score.
	 */
	public int getNrOfPositiveExamples() {
		return nrOfPositiveExamples;
	}
	
	/**
	 * @return the number of negative examples used to compute the score.
	 */
	public int getNrOfNegativeExamples() {
		return nrOfNegativeExamples;
	}

}
