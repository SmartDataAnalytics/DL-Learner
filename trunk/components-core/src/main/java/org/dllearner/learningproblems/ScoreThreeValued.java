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

import java.text.DecimalFormat;
import java.util.Set;

import org.dllearner.core.owl.Individual;
import org.dllearner.utilities.Helper;

/**
 * Computes the score (a negative value) by comparing the classification results
 * with ideal results.
 * 
 * TODO: The implementation is not very efficient, because some things are 
 * only computed to be able to present the score results. This means that
 * it would be better to compute only the necessary computations and do
 * the other ones only when they are needed to calculate statistical values.
 * 
 * @author Jens Lehmann
 *
 */
public class ScoreThreeValued extends ScorePosNeg {
	
	private static final long serialVersionUID = -1780084688122949685L;

	public enum ScoreMethod {POSITIVE, FULL};
	
	// configuration options
	private double accuracyPenalty;
	private double errorPenalty;
	private boolean penaliseNeutralExamples;
	private double percentPerLengthUnit;
	
	// potential configuration options (not implemented as such, but one
	// could so)
	private boolean showCorrectClassifications = false;
	private static ScoreMethod scoreMethod = ScoreMethod.POSITIVE;
	
	private Set<Individual> posClassified;
	private Set<Individual> neutClassified;
	private Set<Individual> negClassified;
	private Set<Individual> posExamples;
	private Set<Individual> neutExamples;
	private Set<Individual> negExamples;
	
    private Set<Individual> posAsNeg;
    private Set<Individual> negAsPos;
    private Set<Individual> posAsNeut;
    private Set<Individual> neutAsPos;
    private Set<Individual> neutAsNeg;
    private Set<Individual> negAsNeut;
    private Set<Individual> posAsPos;
    private Set<Individual> negAsNeg;
    private Set<Individual> neutAsNeut;      
    
    private double score;
    private double accuracy;
    private double accuracyOnExamples;
    private double accuracyOnPositiveExamples;
    private double errorRate;
    
    private int nrOfExamples;
    private int conceptLength;
    
    public ScoreThreeValued(int conceptLength,
    		double accuracyPenalty,
    		double errorPenalty,
    		boolean penaliseNeutralExamples,
    		double percentPerLengthUnit,
    		Set<Individual> posClassified,
    		Set<Individual> neutClassified,
    		Set<Individual> negClassified,
    		Set<Individual> posExamples,
    		Set<Individual> neutExamples,
    		Set<Individual> negExamples) {
    	this.conceptLength = conceptLength;
    	this.accuracyPenalty = accuracyPenalty;
    	this.errorPenalty = errorPenalty;
    	this.penaliseNeutralExamples = penaliseNeutralExamples;
    	this.percentPerLengthUnit = percentPerLengthUnit;
    	this.posClassified = posClassified;
    	this.neutClassified = neutClassified;
    	this.negClassified = negClassified;
    	this.posExamples = posExamples;
    	this.neutExamples = neutExamples;
    	this.negExamples = negExamples;
    	nrOfExamples = posExamples.size()+negExamples.size();
    	computeClassificationMatrix();
    	computeStatistics();
    }
    
    private void computeClassificationMatrix() {
        posAsNeg = Helper.intersection(posExamples,negClassified);
        negAsPos = Helper.intersection(negExamples,posClassified);
        posAsNeut = Helper.intersection(posExamples,neutClassified);
        neutAsPos = Helper.intersection(neutExamples,posClassified);
        neutAsNeg = Helper.intersection(neutExamples,negClassified);
        negAsNeut = Helper.intersection(negExamples,neutClassified);
        // die 3 Berechnungen sind nicht so wichtig f�r die Punktzahl, d.h. falls
        // es Performance bringt, dann kann man sie auch ausgliedern
        posAsPos = Helper.intersection(posExamples,posClassified);
        negAsNeg = Helper.intersection(negExamples,negClassified);
        neutAsNeut = Helper.intersection(neutExamples,neutClassified);     	
    }
    
    private void computeStatistics() {     
        score = - posAsNeg.size()*errorPenalty
        - negAsPos.size()*errorPenalty
        - posAsNeut.size()*accuracyPenalty;
        
        if(scoreMethod==ScoreMethod.FULL)
        	score -= negAsNeut.size()*accuracyPenalty;
        
        if(penaliseNeutralExamples)
        	score -= (neutAsPos.size()*accuracyPenalty        
            + neutAsNeg.size()*accuracyPenalty);
        
        // TODO: man könnte hier statt error penality auch accuracy penalty
        // nehmen
        double worstValue = nrOfExamples * errorPenalty;
        // ergibt Zahl zwischen -1 und 0
        score = score / worstValue;
        score -= percentPerLengthUnit * conceptLength;
        
        // die folgenden Berechnungen k�nnten aus Performancegr�nden auch
        // ausgegliedert werden
        // int domainSize = abox.domain.size();
        int numberOfExamples = posExamples.size()+negExamples.size();
        int domainSize = numberOfExamples + neutExamples.size(); 
        int correctlyClassified = posAsPos.size() + negAsNeg.size() + neutAsNeut.size();
        int correctOnExamples = posAsPos.size() + negAsNeg.size();
        int errors = posAsNeg.size() + negAsPos.size();
        
        // Accuracy = Quotient von richtig klassifizierten durch Anzahl Domainelemente
        accuracy = (double) correctlyClassified/domainSize;
        
        // Accuracy on Examples = Quotient von richtig klassifizierten durch Anzahl pos.
        // und neg. Beispiele
        accuracyOnExamples = (double) correctOnExamples/numberOfExamples;
        
        accuracyOnPositiveExamples = (double) posAsPos.size()/posExamples.size(); 
        
        // Error = Quotient von komplett falsch klassifizierten durch Anzahl pos.
        // und neg. Beispiele 
        errorRate = (double) errors/numberOfExamples;
    }

    @Override
    public double getScoreValue() {
        return score;
    }
    
    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("0.00");
        String str = "";
        str += "score method ";
        if(scoreMethod == ScoreMethod.FULL)
        	str += "full";
        else
        	str += "positive";
        if(!penaliseNeutralExamples)
        	str += " (neutral examples not penalized)";
        str += "\n";
        if(showCorrectClassifications) {
            str += "Correctly classified:\n";
            str += "  positive --> positive: " + posAsPos + "\n";
            str += "  neutral --> neutral: " + neutAsNeut + "\n";
            str += "  negative --> negative: " + negAsNeg + "\n";
        }
        str += "Inaccurately classified (penalty of " + df.format(accuracyPenalty) + " per instance):\n";
        str += "  positive --> neutral: " + posAsNeut + "\n";
        if(penaliseNeutralExamples) {
        	str += "  neutral --> positive: " + neutAsPos + "\n";  
        	str += "  neutral --> negative: " + neutAsNeg + "\n";
        }
        if(scoreMethod == ScoreMethod.FULL)
        	str += "  negative --> neutral: " + negAsNeut + "\n"; 
        str += "Classification errors (penalty of " + df.format(errorPenalty) + " per instance):\n";
        str += "  positive --> negative: " + posAsNeg + "\n";
        str += "  negative --> positive: " + negAsPos + "\n";
        str += "Statistics:\n";
        str += "  Score: " + df.format(score) + "\n";
        str += "  Accuracy: " + df.format(accuracy*100) + "%\n";
        str += "  Accuracy on examples: " + df.format(accuracyOnExamples*100) + "%\n";
        str += "  Accuracy on positive examples: " + df.format(accuracyOnPositiveExamples*100) + "%\n";        
        str += "  Error rate: " + df.format(errorRate*100) + "%\n";
        return str;
    }

	public Set<Individual> getNegClassified() {
		return negClassified;
	}

	public Set<Individual> getPosClassified() {
		return posClassified;
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

	@Override
	public ScorePosNeg getModifiedLengthScore(int newLength) {
		return new ScoreThreeValued(newLength, accuracyPenalty, errorPenalty, penaliseNeutralExamples, percentPerLengthUnit, posClassified, neutClassified, negClassified, posExamples, neutExamples, negExamples);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Score#getAccuracy()
	 */
	@Override
	public double getAccuracy() {
		return accuracy;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Score#getNotCoveredNegatives()
	 */
	@Override
	public Set<Individual> getNotCoveredNegatives() {
		return negAsNeg;
	}	
    
}
