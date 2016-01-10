package org.dllearner.algorithms.el;

import java.util.Comparator;

import org.dllearner.core.Heuristic;

/**
 * Marker interface for heuristics in the EL learning
 * algorithms. A heuristic implements a method
 * to decide which one of two given nodes seems to be more
 * promising with respect to the learning problem we consider.
 * 
 * @author Jens Lehmann
 *
 */
public interface ELHeuristic extends Comparator<SearchTreeNode>, Heuristic {

}
