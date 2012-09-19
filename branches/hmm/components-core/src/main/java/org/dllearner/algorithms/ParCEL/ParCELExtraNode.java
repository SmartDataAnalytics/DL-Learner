package org.dllearner.algorithms.ParCEL;

import java.util.HashSet;
import java.util.Set;

import org.dllearner.algorithms.celoe.OENode;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;

/**
 * Generation time and node type is added into ParCELNode. This information is necessary for some
 * reduction algorithms
 * 
 * @author An C. Tran
 * 
 */
public class ParCELExtraNode extends ParCELNode {

	private double generationTime = Double.MIN_VALUE; // time in ms that the node was generated
	private int type = -1;

	/**
	 * Nodes in the search tree that constitute this node (in case this node a a combination of a
	 * description with counter partial definitions)
	 */
	Set<OENode> compositeNodes = new HashSet<OENode>();


	/**
	 * Constructor
	 * 
	 * @param parentNode
	 * @param description
	 * @param accuracy
	 */
	/*
	public ParCELExtraNode(OENode parentNode, Description description, double accuracy) {
		super(parentNode, description, accuracy);
	}
	 */
	
	/**
	 * ============================================================================================
	 * Constructor
	 * 
	 * @param parentNode
	 * @param description
	 * @param accuracy
	 * @param correctness
	 */
	/*
	public ParCELExtraNode(OENode parentNode, Description description, double accuracy,
			double correctness) {
		super(parentNode, description, accuracy, correctness);
	}
	*/


	/**
	 * Create a ParCELExtraNode from an OENode
	 * @param node
	 */
	public ParCELExtraNode(ParCELNode node) {
		super(node.getParent(), node.getDescription(), node.getAccuracy(), node.getCorrectness(), node.getCompleteness());
		setCoveredPositiveExamples(node.getCoveredPositiveExamples());
		setCoveredNegativeExamples(node.getCoveredNegativeExamples());
	}

	/**
	 * Create a node with a set of positive examples covered by the description of the node
	 * 
	 * @param node
	 * @param cp
	 */
	public ParCELExtraNode(ParCELNode node, Set<Individual> cp) {
		super(node.getParent(), node.getDescription(), node.getAccuracy(), node.getCorrectness(), node.getCompleteness());
		super.coveredPositiveExamples = cp;
	}

	/**
	 * ============================================================================================
	 * Constructor with the correctness and the generation time of the description
	 * 
	 * @param parentNode
	 * @param description
	 * @param accuracy
	 * @param correctness
	 */
	/*
	public ParCELExtraNode(ParCELNode parentNode, Description description, double accuracy,
			double correctness) {
		super(parentNode, description, accuracy, correctness);
		this.coveredPositiveExamples = null;
	}
	 
	 */
	/**
	 * ============================================================================================
	 * Constructor with the set of positive examples covered by the description of the node
	 * 
	 * @param parentNode
	 * @param description
	 * @param accuracy
	 * @param correctness
	 * @param cn
	 *            Covered positive examples
	 */
	public ParCELExtraNode(ParCELNode parentNode, Description description, double accuracy,
			double correctness, double completeness, Set<Individual> cp) {
		super(parentNode, description, accuracy, correctness, completeness);
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

	public void setCompositeList(Set<ParCELExtraNode> compositeNodes) {
		this.compositeNodes.addAll(compositeNodes);
	}

	public Set<OENode> getCompositeNodes() {
		return this.compositeNodes;
	}
}
