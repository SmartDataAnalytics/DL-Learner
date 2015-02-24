/**
 * 
 */
package org.dllearner.learningproblems;

import java.util.Set;

import org.dllearner.core.Score;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * @author Lorenz Buehmann
 *
 */
public class QueryTreeScore extends Score {
	
	private double score;
	
	private double accuracy;
	
	private double distancePenalty;
	
	private double specifityScore;
	private int nrOfSpecificNodes;
	
	private Set<OWLIndividual> posAsPos;	
    private Set<OWLIndividual> posAsNeg;
    private Set<OWLIndividual> negAsPos;
    private Set<OWLIndividual> negAsNeg;    

	public QueryTreeScore(double score, double accuracy, 
			Set<OWLIndividual> posAsPos, Set<OWLIndividual> posAsNeg, Set<OWLIndividual> negAsPos, Set<OWLIndividual> negAsNeg,
			double specifityScore, int nrOfSpecificNodes) {
		super();
		this.score = score;
		this.accuracy = accuracy;
		this.posAsPos = posAsPos;
		this.posAsNeg = posAsNeg;
		this.negAsPos = negAsPos;
		this.negAsNeg = negAsNeg;
		this.specifityScore = specifityScore;
		this.nrOfSpecificNodes = nrOfSpecificNodes;
	}
	
	/**
	 * @return the score
	 */
	public double getScore() {
		return score;
	}
	
	/**
	 * @param score the score to set
	 */
	public void setScore(double score) {
		this.score = score;
	}
	
	/**
	 * @param distancePenalty the distancePenalty to set
	 */
	public void setDistancePenalty(double distancePenalty) {
		this.distancePenalty = distancePenalty;
	}
	
	/**
	 * @return the distancePenalty
	 */
	public double getDistancePenalty() {
		return distancePenalty;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Score#getAccuracy()
	 */
	@Override
	public double getAccuracy() {
		return accuracy;
	}
	
	/**
	 * @param accuracy the accuracy to set
	 */
	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}
	
	public Set<OWLIndividual> getCoveredNegatives() {
		return negAsPos;
	}

	public Set<OWLIndividual> getCoveredPositives() {
		return posAsPos;
	}
	
	public Set<OWLIndividual> getNotCoveredPositives() {
		return posAsNeg;
	}
	
	public Set<OWLIndividual> getNotCoveredNegatives() {
		return negAsNeg;
	}		
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return score
				 + "(accuracy=" + accuracy 
				 + "(+" + posAsPos.size() + "/" + (posAsPos.size() + posAsNeg.size())
				 + "|-" + negAsPos.size() + "/" + (negAsPos.size() + negAsNeg.size()) + ")|"
				 + "specifity=" + specifityScore + "(" + nrOfSpecificNodes + ")|"
				 		+ "penalty=" + distancePenalty + ")";   
	}

}
