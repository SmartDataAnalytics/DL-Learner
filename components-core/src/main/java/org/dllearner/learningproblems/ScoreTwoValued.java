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