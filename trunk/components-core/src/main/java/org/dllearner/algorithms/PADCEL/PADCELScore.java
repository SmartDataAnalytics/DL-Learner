package org.dllearner.algorithms.PADCEL;

import org.dllearner.core.Score;

/**
 * This class implement methods that will be used to create an EvaluatedDescription
 * (this provide information for sorting the node in search tree)
 *  
 * @author An C. Tran
 *
 */
@SuppressWarnings("serial")
public class PADCELScore extends Score {
	
	double accuracy;	
	double correctness;

	public PADCELScore(PADCELNode node) {
		this.accuracy = node.getAccuracy();
		this.correctness = node.getCorrectness();
	}
	
	public PADCELScore(double accuracy, double correctness) {
		this.accuracy = accuracy;
		this.correctness = correctness;
	}
	
	@Override
	public double getAccuracy() {
		return this.accuracy;
	}
	
	public double getCorrectness() {
		return this.correctness;
	}
	

}
