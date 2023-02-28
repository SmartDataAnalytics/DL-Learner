package org.dllearner.algorithms.parcel;


/**
 * Implements the heuristic used to expand the search tree. Dimensions used: <br>
 * + correctness: main value<br>
 * + horizontal expansion: penalty<br>
 * + accuracy gained from the parent node: bonus<br>
 * + refinement nodes: penalty<br>
 * + concept type + name (org.dllearner.utilities.owl.ConceptComparator)
 *
 * @author An C. Tran
 */
public class ParCELDefaultHeuristic implements ParCELHeuristic {

    //correct
    protected double correctnessFactor = 1.0;

    // penalty for long descriptions
    protected double expansionPenaltyFactor = 0.05;    //0.01, 0.05

    // bonus for gained accuracy
    protected double gainBonusFactor = 0.2; //0.2;        //0.1, 0.2

    // penalty if a node description has very many refinements since exploring
    // such a node is computationally very expensive
    protected double nodeRefinementPenalty = 0.0001;

    // award for node with high accuracy
    protected double accuracyAwardFactor = 0.5;    //0.01


    /**
     * Compare two node
     *
     * @param node1 Node to compare
     * @param node2 Node to compare
     * @return 1 if node1 "greater" than node2 and vice versa
     */
    @Override
    public int compare(ParCELNode node1, ParCELNode node2) {
        double diff = getNodeScore(node1) - getNodeScore(node2);

        if (diff > 0) { // node1 has better score than node2
            return 1;
        } else if (diff < 0) {
            return -1;
        } else {
            // syntactic comparison as final comparison criterion
            int comp = node1.getDescription().compareTo(node2.getDescription());
//			return comp;

            // this allows duplicate descriptions exists in the set (with dif. horz. value)
            if (comp != 0)
                return comp;
            else
                return -1;
        }
    }

    /**
     * Calculate score for a node which is used as the searching heuristic
     *
     * @param node Node to be scored
     * @return Score of the node
     */
    protected double getNodeScore(ParCELNode node) {

        // the scoring mainly bases on correctness
        double score = node.getCorrectness();// * correctnessFactor;

        // bonus for the accuracy gained
        if (!node.isRoot()) {
            double parentAccuracy = ((ParCELNode) (node.getParent())).getAccuracy();
            score += (parentAccuracy - node.getAccuracy()) * gainBonusFactor;
        }

        // award node with high accuracy
        score += node.getAccuracy() * accuracyAwardFactor;

        // penalty for horizontal expansion
        score -= node.getHorizontalExpansion() * expansionPenaltyFactor;
        //score -= node.getDescription().getLength() * expansionPenaltyFactor;

        score -= node.getRefinementCount() * nodeRefinementPenalty;

        return score;
    }

    @Override
    public double getScore(ParCELNode node) {
        return this.getNodeScore(node);
    }

    public double getCorrectnessFactor() {
        return correctnessFactor;
    }

    public void setCorrectnessFactor(double correctnessFactor) {
        this.correctnessFactor = correctnessFactor;
    }

    public double getExpansionPenaltyFactor() {
        return expansionPenaltyFactor;
    }

    public void setExpansionPenaltyFactor(double expansionPenaltyFactor) {
        this.expansionPenaltyFactor = expansionPenaltyFactor;
        //System.out.println("ExpansionPenaltyFactor changed: " + expansionPenaltyFactor);
    }

    public double getGainBonusFactor() {
        return gainBonusFactor;
    }

    public void setGainBonusFactor(double gainBonusFactor) {
        this.gainBonusFactor = gainBonusFactor;
    }

    public double getNodeRefinementPenalty() {
        return nodeRefinementPenalty;
    }

    public void setNodeRefinementPenalty(double nodeRefinementPenalty) {
        this.nodeRefinementPenalty = nodeRefinementPenalty;
    }

    public double getAccuracyAwardFactor() {
        return accuracyAwardFactor;
    }

    public void setAccuracyAwardFactor(double accuracyAwardFactor) {
        this.accuracyAwardFactor = accuracyAwardFactor;
    }


}
