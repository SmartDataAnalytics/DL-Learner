package org.dllearner.core;

import java.util.Set;

import org.dllearner.core.owl.Individual;

public abstract class Score {
	public abstract double getScore();
	
	/**
	 * The score of a concept depends on how good it classifies the
	 * examples of a learning problem and the length of the concept
	 * itself. If a given concept is known to have equal classification
	 * properties than the concept this score object is based on, then
	 * this method can be used to calculate its score value by using the
	 * length of this concept as parameter.
	 * 
	 * @param newLength Length of the concept.
	 * @return Score.
	 */
	public abstract Score getModifiedLengthScore(int newLength);
	
	public abstract Set<Individual> getCoveredPositives();
	public abstract Set<Individual> getCoveredNegatives();
	public abstract Set<Individual> getNotCoveredPositives();
	
	// public abstract int getNrOfMiss
}
