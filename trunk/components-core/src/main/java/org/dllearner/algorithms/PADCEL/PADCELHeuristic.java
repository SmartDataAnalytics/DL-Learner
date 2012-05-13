package org.dllearner.algorithms.PADCEL;

import java.util.Comparator;

/**
 * Interface for heuristics used in PADCEL
 * 
 * @author An C. Tran
 * 
 */
public interface PADCELHeuristic extends Comparator<PADCELNode> {

	public int compare(PADCELNode node1, PADCELNode node2);

	public double getScore(PADCELNode node);
}
