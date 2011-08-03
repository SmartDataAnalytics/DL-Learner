package org.dllearner.learningproblems;

import org.dllearner.core.Score;

public class AxiomScore extends Score{
	
	private static final long serialVersionUID = 555252118489924570L;
	private double accuracy;
	
	public AxiomScore(double accuracy) {
		this.accuracy = accuracy;
	}

	@Override
	public double getAccuracy() {
		return accuracy;
	}

}
