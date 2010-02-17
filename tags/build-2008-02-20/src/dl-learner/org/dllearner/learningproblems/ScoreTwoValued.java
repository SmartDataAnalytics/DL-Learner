package org.dllearner.learningproblems;

import java.util.Set;

import org.dllearner.core.Score;
import org.dllearner.core.owl.Individual;

/**
 * 
 * TODO: accuracy-Berechnung (positive+negative Beispiele muessen dafuer bekannt sein)
 * 
 * @author jl
 *
 */
public class ScoreTwoValued extends Score {

    private Set<Individual> posAsPos;	
    private Set<Individual> posAsNeg;
    private Set<Individual> negAsPos;
    private Set<Individual> negAsNeg;    
    private double score;
    private double classificationScore;
    private int nrOfExamples;
    private int conceptLength;
    private double percentPerLengthUnit;
	
	public ScoreTwoValued(int conceptLength, double percentPerLengthUnit, Set<Individual> posAsPos, Set<Individual> posAsNeg, Set<Individual> negAsPos, Set<Individual> negAsNeg) {
    	this.conceptLength = conceptLength;
    	this.percentPerLengthUnit = percentPerLengthUnit;
		this.posAsPos = posAsPos;
		this.posAsNeg = posAsNeg;
		this.negAsPos = negAsPos;
		this.negAsNeg = negAsNeg;
		nrOfExamples = posAsPos.size()+posAsNeg.size()+negAsPos.size()+negAsNeg.size();
		computeScore();
	}
	
	private void computeScore() {
		// - Anzahl falscher Klassifikationen
		classificationScore = - posAsNeg.size() - negAsPos.size();
		// Anteil falscher Klassifikationen (Zahl zwischen -1 und 0)
		classificationScore = classificationScore / (double) nrOfExamples;
		// Berücksichtigung des Längenfaktors
		score = classificationScore - percentPerLengthUnit * conceptLength;
	}
	
	@Override
	public double getScore() {
		return score;
	}

	@Override
	public String toString() {
		String str = "";
		str += "score: " + score + "\n";
		str += "posAsPos: " + posAsPos + "\n";
		str += "positive examples classified as negative: " + posAsNeg + "\n";
		str += "negative examples classified as positive: " + negAsPos + "\n";
		return str;
	}

	@Override
	public Set<Individual> getCoveredNegatives() {
		return negAsPos;
	}

	@Override
	public Set<Individual> getCoveredPositives() {
		return posAsPos;
	}
	
	@Override
	public Set<Individual> getNotCoveredPositives() {
		return posAsNeg;
	}

	/*
	@Override
	public double getModifiedLengthScore(int newLength) {
		return classificationScore - Config.percentPerLengthUnit * newLength;
	}
	*/
	
	@Override
	public Score getModifiedLengthScore(int newLength) {
		return new ScoreTwoValued(newLength, percentPerLengthUnit, posAsPos, posAsNeg, negAsPos, negAsNeg);
	}	
}
