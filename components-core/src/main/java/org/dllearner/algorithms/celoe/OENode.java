package org.dllearner.algorithms.celoe;

import java.text.DecimalFormat;
import java.util.Map;

import org.dllearner.core.AbstractSearchTreeNode;
import org.dllearner.utilities.datastructures.SearchTreeNode;
import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * A node in the search tree of the ontology engineering algorithm.
 * 
 * Differences to the node structures in other algorithms (this may change):
 * - covered examples are not stored in node (i.e. coverage needs to be recomputed
 * for child nodes, which costs time but saves memory)
 * - only evaluated nodes are stored
 * - too weak nodes are not stored
 * - redundant nodes are not stored (?)
 * - only accuracy is stored to make the node structure reusable for different
 *   learning problems and -algorithms
 * 
 * @author Jens Lehmann
 *
 */
public class OENode extends AbstractSearchTreeNode<OENode> implements SearchTreeNode {

	protected OWLClassExpression description;
	
	protected double accuracy;
	
	protected int horizontalExpansion;
		
	// the refinement count corresponds to the number of refinements of the
	// OWLClassExpression in this node - it is a better heuristic indicator than child count
	// (and avoids the problem that adding children changes the heuristic value)
	private int refinementCount = 0;
	
	private static DecimalFormat dfPercent = new DecimalFormat("0.00%");
	
	public OENode(OWLClassExpression description, double accuracy) {
		this.description = description;
		this.accuracy = accuracy;
		this.horizontalExpansion = OWLClassExpressionUtils.getLength(description) - 1;
	}
	
//	public OENode(OENode parentNode, OWLClassExpression description, double accuracy) {
//		this(description, accuracy);
//		this.setParent(parentNode);
//	}

	public void incHorizontalExpansion() {
		horizontalExpansion++;
	}
	
	public boolean isRoot() {
		return (parent == null);
	}
	
	/**
	 * @return the description
	 */
	public OWLClassExpression getDescription() {
		return description;
	}

	@Override
	public OWLClassExpression getExpression() {
		return getDescription();
	}
	
	/**
	 * @return the accuracy
	 */
	public double getAccuracy() {
		return accuracy;
	}

	/**
	 * @return the horizontalExpansion
	 */
	public int getHorizontalExpansion() {
		return horizontalExpansion;
	}
	
	public String getShortDescription(String baseURI) {
		return getShortDescription(baseURI, null);
	}
	
	public String getShortDescription(String baseURI, Map<String, String> prefixes) {
		String ret = OWLAPIRenderers.toDLSyntax(description) + " [";
//		String ret = OWLAPIRenderers.toManchesterOWLSyntax(description) + " [";
//		ret += "score" + NLPHeuristic.getNodeScore(this) + ",";
		ret += "acc:" + dfPercent.format(accuracy) + ", ";
		ret += "he:" + horizontalExpansion + ", ";
		ret += "c:" + children.size() + ", ";
		ret += "ref:" + refinementCount + "]";
		return ret;
	}
	
	@Override
	public String toString() {
		return getShortDescription(null);
	}

	/**
	 * @return the refinementCount
	 */
	public int getRefinementCount() {
		return refinementCount;
	}

	/**
	 * @param refinementCount the refinementCount to set
	 */
	public void setRefinementCount(int refinementCount) {
		this.refinementCount = refinementCount;
	}
}