package org.dllearner.algorithms.PADCEL;

import java.util.HashSet;
import java.util.Set;

import org.dllearner.algorithms.celoe.OENode;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;

/**
 * Generation time and node type is added into PADCELNode. This information is necessary for some
 * reduction algorithms
 * 
 * @author An C. Tran
 * 
 */
public class PADCELExtraNode extends PADCELNode {

	private double generationTime = Double.MIN_VALUE; // time in ms that the node was generated
	private int type = -1;

	Set<OENode> compositeNodes = new HashSet<OENode>();

	/**
	 * ============================================================================================
	 * Constructor with correctness of the description
	 * 
	 * @param parentNode
	 *            Parent node of this node
	 * @param description
	 *            Description of the node
	 * @param accuracy
	 *            Accuracy of the node
	 * @param distance
	 *            Distance between the node and the learning problem
	 * @param correctness
	 *            Correctness of the node
	 */
	public PADCELExtraNode(OENode parentNode, Description description, double accuracy,
			double correctness) {
		super(parentNode, description, accuracy, correctness);
	}

	public PADCELExtraNode(OENode parentNode, Description description, double accuracy) {
		super(parentNode, description, accuracy);
	}

	public PADCELExtraNode(PADCELNode node) {
		super(node.getParent(), node.getDescription(), node.getAccuracy(), node.getCorrectness());
		setCoveredPositiveExamples(node.getCoveredPositiveExamples());
		setCoveredNegativeExamples(node.getCoveredNegativeExamples());
	}

	public PADCELExtraNode(PADCELNode node, Set<Individual> cp, double generationTime) {
		super(node.getParent(), node.getDescription(), node.getAccuracy(), node.getCorrectness());
		super.coveredPositiveExamples = cp;
		this.generationTime = generationTime;
	}

	/**
	 * ============================================================================================
	 * Constructor with the correctness and the generation time of the description
	 * 
	 * @param parentNode
	 *            Parent node of this node
	 * @param description
	 *            Description of the node
	 * @param accuracy
	 *            Accuracy of the node
	 * @param distance
	 *            Distance between the node and the learning problem
	 * @param correctness
	 *            Correctness of the node
	 * @param genTime
	 *            Time in ms that the work used to generate this node
	 */
	public PADCELExtraNode(PADCELNode parentNode, Description description, double accuracy,
			double correctness, double genTime) {
		super(parentNode, description, accuracy, correctness);
		this.coveredPositiveExamples = null;
		this.generationTime = genTime;
	}

	/**
	 * ============================================================================================
	 * Constructor with the set of positive examples covered by the description of the node
	 * 
	 * @param parentNode
	 * @param description
	 * @param accuracy
	 * @param distance
	 * @param correctness
	 * @param cn
	 *            Covered positive examples
	 */
	public PADCELExtraNode(PADCELNode parentNode, Description description, double accuracy,
			double correctness, Set<Individual> cp) {
		super(parentNode, description, accuracy, correctness);
		super.setCoveredPositiveExamples(cp);
	}

	// -------------------------
	// getters and setters
	// -------------------------
	public void setCoveredPositiveExamples(Set<Individual> cpn) {
		super.setCoveredPositiveExamples(cpn);
	}

	public double getGenerationTime() {
		return generationTime;
	}

	public void setGenerationTime(double d) {
		this.generationTime = d;
	}

	public void setType(int t) {
		this.type = t;
	}

	public int getType() {
		return this.type;
	}

	public void setCompositeList(Set<PADCELExtraNode> compositeNodes) {
		this.compositeNodes.addAll(compositeNodes);
	}

	public Set<OENode> getCompositeNodes() {
		return this.compositeNodes;
	}
}
