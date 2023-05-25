package org.dllearner.algorithms.parcel;

import java.util.Comparator;

/**
 * Interface for heuristics used in ParCEL
 * 
 * @author An C. Tran
 * 
 */
public interface ParCELHeuristic extends Comparator<ParCELNode> {

	int compare(ParCELNode node1, ParCELNode node2);

	double getScore(ParCELNode node);
}
