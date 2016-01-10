package org.dllearner.learningproblems;

import org.dllearner.core.Score;

/**
 * A simple score implementation which only contains the accuracy.
 *
 * @author Lorenz Buehmann
 *
 */
public class ScoreSimple extends Score {
	
	/**
	 * A minimum simple score object.
	 */
	public static ScoreSimple MIN = new ScoreSimple(Double.NEGATIVE_INFINITY);
	/**
	 * A maximum simple score object.
	 */
	public static ScoreSimple MAX = new ScoreSimple(Double.POSITIVE_INFINITY);
	
	private double accuracy;
	
	public ScoreSimple(double accuracy) {
		this.accuracy = accuracy;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Score#getAccuracy()
	 */
	@Override
	public double getAccuracy() {
		return accuracy;
	}
	
	 

}
