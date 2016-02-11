package org.dllearner.algorithms.versionspace;


import org.dllearner.utilities.datastructures.SearchTree;

/**
 *
 * Generates the version space H that contains all possible concepts between TOP and BOTTOM.
 * In theory the number of concepts is infinite, but for learning, we restrict ourselves to H, i.e.
 * H may be only a small subset of all possible concepts (this turns out to be important).
 *
 * For example we could add restrictions on the concepts, e.g. the length or depth.
 *
 * @author Lorenz Buehmann
 *
 */
public interface VersionSpaceGenerator {

	/**
	 * Generate the search tree, i.e. all concepts in the version space.
	 *
	 * @return the search tree
	 */
	RootedDirectedGraph generate();
}
