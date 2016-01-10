package org.dllearner.core;

import org.semanticweb.owlapi.model.OWLObject;

import java.io.Serializable;

/**
 * The score class is used to store how well a hypothesis did
 * on a learning problem. Depending on the learning problem at hand,
 * different criteria can be used. (Similar learning problems probably
 * score hypothesis in a similar way.)
 * 
 * TODO: Maybe we don't really need a score, but only EvaluatedDescription.
 * 
 * @author Jens Lehmann
 *
 */
public abstract class Score implements Serializable{

	private static final long serialVersionUID = -6479328496461875019L;

	/**
	 * This method returns a value, which indicates how accurate a
	 * hypothesis solves a learning problem. 
	 * 
	 * @see AbstractLearningProblem#getAccuracyOrTooWeak(OWLObject, double)
	 * @return A value between 0 and 1 indicating the quality (of a hypothesis).
	 */	
	public abstract double getAccuracy();
	
	
}
