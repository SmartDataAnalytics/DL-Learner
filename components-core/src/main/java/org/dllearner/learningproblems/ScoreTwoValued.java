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

import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;

/**
 * Calculates accuracy and score (with respect to some length penalty) of
 * a class description.
 * 
 * TODO: In fact, a score value influencing a learning algorithm
 * should not be calculated here, but rather in a separate heuristic
 * as there are many methods to calculate such a value. This class
 * should only be used for computing example coverage, accuracy etc.
 * 
 * @author Jens Lehmann
 *
 */
public class ScoreTwoValued<T extends OWLEntity> extends ScorePosNeg<T> {

	private static final long serialVersionUID = 6264873890324824550L;
	
	private Set<T> posAsPos;
    private Set<T> posAsNeg;
    private Set<T> negAsPos;
    private Set<T> negAsNeg;
    private double score;
    private double accuracy;
    private int nrOfExamples;
    private int conceptLength;
    private double percentPerLengthUnit;

	
	public ScoreTwoValued(int conceptLength, double percentPerLengthUnit, Set<T> posAsPos, Set<T> posAsNeg, Set<T> negAsPos, Set<T> negAsNeg, double accuracy) {
    	this.conceptLength = conceptLength;
    	this.percentPerLengthUnit = percentPerLengthUnit;
		this.posAsPos = posAsPos;
		this.posAsNeg = posAsNeg;
		this.negAsPos = negAsPos;
		this.negAsNeg = negAsNeg;
		nrOfExamples = posAsPos.size()+posAsNeg.size()+negAsPos.size()+negAsNeg.size();
		this.accuracy = accuracy;
		score = accuracy - 1 - percentPerLengthUnit * conceptLength;
	}
	
	@Override
	public double getAccuracy() {
		return accuracy;
	}
	
	/**
	 * score = accuracy - 1 - length * length penalty
	 */
	@Override
	public double getScoreValue() {
		return score;
	}

	@Override
	public String toString() {
		String str = "";
		str += "score: " + score + "\n";
		str += "accuracy: " + accuracy + "\n";
		str += "posAsPos (" + posAsPos.size() + "): " + posAsPos + "\n";
		str += "positive examples classified as negative (" + posAsNeg.size() + "): " + posAsNeg + "\n";
		str += "negative examples classified as positive (" + negAsPos.size() + "): " + negAsPos + "\n";
		return str;
	}

	@Override
	public Set<T> getCoveredNegatives() {
		return negAsPos;
	}

	@Override
	public Set<T> getCoveredPositives() {
		return posAsPos;
	}
	
	@Override
	public Set<T> getNotCoveredPositives() {
		return posAsNeg;
	}
	
	@Override
	public Set<T> getNotCoveredNegatives() {
		return negAsNeg;
	}
	
	@Override
	public ScorePosNeg<T> getModifiedLengthScore(int newLength) {
		return new ScoreTwoValued<>(newLength, percentPerLengthUnit, posAsPos, posAsNeg, negAsPos, negAsNeg, accuracy);
	}
	
	/**
	 * @param accuracy the accuracy to set
	 */
	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}

}