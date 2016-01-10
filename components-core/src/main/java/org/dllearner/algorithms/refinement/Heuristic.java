package org.dllearner.algorithms.refinement;

import java.util.Comparator;

/**
 * Marker interface for heuristics in the refinement operator
 * based learning approach. A heuristic implements a method
 * to decide which one of two given nodes seems to be more
 * promising with respect to the learning problem we consider.
 * 
 * @author Jens Lehmann
 *
 */
public interface Heuristic extends Comparator<Node>{

}
