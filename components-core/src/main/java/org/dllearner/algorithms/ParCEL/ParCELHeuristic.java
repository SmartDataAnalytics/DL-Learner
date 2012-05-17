package org.dllearner.algorithms.ParCEL;

import java.util.Comparator;

/**
 * Interface for heuristics used in ParCEL
 * 
 * @author An C. Tran
 * 
 */
public interface ParCELHeuristic extends Comparator<ParCELNode> {

	public int compare(ParCELNode node1, ParCELNode node2);

	public double getScore(ParCELNode node);
}
