package org.dllearner.algorithms.pattern;

/**
 * @author Lorenz Buehmann
 *
 */
public enum FragmentExtractionStrategy {

	/**
	 * Extract a fragment based on a given number of examples.
	 */
	INDIVIDUALS,
	/**
	 * Extract as much information as possible in a given time.
	 */
	TIME
}
