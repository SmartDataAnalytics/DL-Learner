package org.dllearner.learningproblems;

import java.util.Set;

import org.dllearner.core.Score;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * The score class is used to store how well a class description did
 * on a learning problem.
 * 
 * @author Jens Lehmann
 *
 */
public abstract class ScorePosNeg<T extends OWLEntity> extends Score {
	
	private static final long serialVersionUID = -4646131678864109469L;

	public abstract double getScoreValue();
	
	// example coverage
	public abstract Set<T> getCoveredPositives();
	public abstract Set<T> getCoveredNegatives();
	public abstract Set<T> getNotCoveredPositives();
	public abstract Set<T> getNotCoveredNegatives();	
	
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
	public abstract ScorePosNeg<T> getModifiedLengthScore(int newLength);
	
}