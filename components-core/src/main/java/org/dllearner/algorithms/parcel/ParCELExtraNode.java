package org.dllearner.algorithms.parcel;

import java.util.HashSet;
import java.util.Set;

import org.dllearner.algorithms.celoe.OENode;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * Generation time and node type properties are added. This information is needed by some
 * reduction algorithms
 * 
 * @author An C. Tran
 * 
 */
public class ParCELExtraNode extends ParCELNode {

	protected double generationTime = Double.MIN_VALUE; // time in ms that the node was generated
	protected double extraInfo = Double.MIN_VALUE;
	protected int type = -1;

	/**
	 * Nodes in the search tree that constitute this node (in case this node a a combination of a
	 * description with counter partial definitions) to form a partial definition
	 */
	final Set<OENode> compositeNodes = new HashSet<>();


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
	public ParCELExtraNode(ParCELNode node, Set<OWLIndividual> cp) {
		super(node.getParent(), node.getDescription(), node.getAccuracy(), node.getCorrectness(), node.getCompleteness());
		super.coveredPositiveExamples = cp;
	}

	/**
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
	 * Constructor with the set of positive examples covered by the description of the node
	 * 
	 * @param parentNode
	 * @param description
	 * @param accuracy
	 * @param correctness
	 * @param cp Covered positive examples
	 */
	public ParCELExtraNode(ParCELNode parentNode, OWLClassExpression description, double accuracy,
			double correctness, double completeness, Set<OWLIndividual> cp)
	{
		super(parentNode, description, accuracy, correctness, completeness);
		super.setCoveredPositiveExamples(cp);
	}

	
	/**
	 * Constructor with the set of positive examples covered by the description of the node
	 * 
	 * @param parentNode
	 * @param description
	 * @param accuracy
	 * @param correctness
	 * @param cn Covered positive examples
	 */
	public ParCELExtraNode(ParCELNode parentNode, OWLClassExpression description, double accuracy,
                           double correctness, double completeness, Set<OWLIndividual> cp, Set<OWLIndividual> cn)
	{
		super(parentNode, description, accuracy, correctness, completeness);
		super.setCoveredPositiveExamples(cp);
		super.setCoveredNegativeExamples(cn);
	}
	
	
	// -------------------------
	// getters and setters
	// -------------------------

	public double getGenerationTime() {
		return generationTime;
	}

	public void setGenerationTime(double d) {
		this.generationTime = d;
	}

	public double getExtraInfo() {
		return extraInfo;
	}

	public void setExtraInfo(double d) {
		this.extraInfo = d;
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
