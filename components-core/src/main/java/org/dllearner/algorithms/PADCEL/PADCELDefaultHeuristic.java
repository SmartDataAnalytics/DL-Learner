package org.dllearner.algorithms.PADCEL;

import org.dllearner.utilities.owl.ConceptComparator;

/**
 * Implements the heuristic used to expand the search tree. Dimensions used: <br>
 * + correctness: main value<br>
 * + horizontal expansion: penalty<br>
 * + accuracy gained from the parent node: bonus<br>
 * + refinement nodes: penalty<br>
 * + concept type + name (org.dllearner.utilities.owl.ConceptComparator)
 * 
 * @author An C. Tran
 * 
 */
public class PADCELDefaultHeuristic implements PADCELHeuristic {

	// penalty for long descriptions
	protected double expansionPenaltyFactor = 0.1;

	// bonus for gained accuracy
	protected double gainBonusFactor = 0.2;

	// penalty if a node description has very many refinements since exploring
	// such a node is computationally very expensive
	protected double nodeRefinementPenalty = 0.0001;

	// award for node with high accuracy
	protected double accuracyAwardFactor = 0.01;

	// syntactic comparison as final comparison criterion
	protected ConceptComparator conceptComparator = new ConceptComparator();

	/**
	 * Compare two node
	 * 
	 * @param node1
	 *            Node to compare
	 * @param node2
	 *            Node to compare
	 * 
	 * @return 1 if node1 "greater" than node2 and vice versa
	 */
	public int compare(PADCELNode node1, PADCELNode node2) {
		double diff = getNodeScore(node1) - getNodeScore(node2);

		if (diff > 0) { // node1 has better score than node2
			return 1;
		} else if (diff < 0) {
			return -1;
		} else {
			int comp = conceptComparator.compare(node1.getDescription(), node2.getDescription());

			// this allows duplicate descriptions exists in the set
			if (comp != 0)
				return comp;
			else
				return -1;

		}
	}

	/**
	 * Calculate score for a node which is used as the searching heuristic
	 * 
	 * @param node
	 *            Node to be scored
	 * 
	 * @return Score of the node
	 */
	protected double getNodeScore(PADCELNode node) {

		// the scoring mainly bases on correctness
		double score = node.getCorrectness();

		// bonus for the accuracy gained
		if (!node.isRoot()) {
			double parentAccuracy = ((PADCELNode) (node.getParent())).getAccuracy();
			score += (parentAccuracy - node.getAccuracy()) * gainBonusFactor;
		}

		// award node with high accuracy
		score += node.getAccuracy() * accuracyAwardFactor;

		// penalty for horizontal expansion
		score -= node.getHorizontalExpansion() * expansionPenaltyFactor;
		// score -= node.getDescription().getLength() * expansionPenaltyFactor;

		return score;
	}

	public double getScore(PADCELNode node) {
		return this.getNodeScore(node);
	}
}
