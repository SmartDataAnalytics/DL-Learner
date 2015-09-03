package org.dllearner.algorithms.distributed;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.dllearner.algorithms.SearchTreeNode;
import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.model.OWLClassExpression;

public class DistOENode2 implements SearchTreeNode, Serializable {
	private static final long serialVersionUID = -2105646490584496365L;
	private static DecimalFormat dfPercent = new DecimalFormat("0.00%");

	protected double accuracy;
	private final OWLClassExpression description;

	protected int horizontalExpansion;

	/**
	 * the refinement count corresponds to the number of refinements of the
	 * OWLClassExpression in this node - it is a better heuristic indicator
	 * than child count (and avoids the problem that adding children changes
	 * the heuristic value) */
	protected int refinementCount = 0;

	private DistOENodeTree2 tree;
	private final UUID uuid;

	// <---------------------------- constructors ---------------------------->
	public DistOENode2(OWLClassExpression description, double accuracy) {
		this(description, accuracy, UUID.randomUUID());
	}

	public DistOENode2(OWLClassExpression description, double accuracy, UUID uuid) {
		this.description = description;
		this.accuracy = accuracy;
		this.uuid = uuid;
		horizontalExpansion = OWLClassExpressionUtils.getLength(description);
	}
	// </--------------------------- constructors ---------------------------->


	// <-------------------------- interface methods ------------------------->
	@Override
	public OWLClassExpression getExpression() {
		return description;
	}

	@Override
	public List<DistOENode2> getChildren() {
		return tree.getChildren(this);
	}

	@Override
	public String toString() {
		return getShortDescription(null);
	}
	// </------------------------- interface methods ------------------------->

	// <------------------------ non-interface methods ----------------------->
	public static DistOENode2 copy(DistOENode2 nodeToCopy) {
		DistOENode2 newNode = new DistOENode2(
				nodeToCopy.getDescription(), nodeToCopy.getAccuracy());

		newNode.setRefinementCount(nodeToCopy.getRefinementCount());
		newNode.horizontalExpansion = nodeToCopy.getHorizontalExpansion();

		return newNode;
	}

	public static DistOENode2 copyWithId(DistOENode2 nodeToCopy) {
		DistOENode2 newNode = new DistOENode2(
				nodeToCopy.getDescription(), nodeToCopy.getAccuracy(),
				nodeToCopy.uuid);

		newNode.setRefinementCount(nodeToCopy.getRefinementCount());
		newNode.horizontalExpansion = nodeToCopy.getHorizontalExpansion();

		return newNode;
	}

	public boolean equals(DistOENode2 other) {
		if (other == null) return false;

		return uuid.equals(other.getUUID());
	}

	public DistOENode2 getParent() {
		return tree.getParent(this);
	}

	public String getShortDescription(String baseURI) {
		return getShortDescription(baseURI, null);
	}

	public String getShortDescription(String baseURI, Map<String, String> prefixes) {
		String ret = OWLAPIRenderers.toDLSyntax(description) + " [";
//		ret += "score" + NLPHeuristic.getNodeScore(this) + ",";
		ret += "acc:" + dfPercent.format(accuracy) + ", ";
		ret += "he:" + horizontalExpansion + ", ";
//		ret += "c:" + children.size() + ", ";
		ret += "ref:" + refinementCount + "]";
		return ret;
	}

	public void incHorizontalExpansion() {
		horizontalExpansion++;
	}

	public boolean isInUse() {
		if (tree == null) return false;
		else return tree.isInUse(this);
	}

	public boolean isRoot() {
		return tree.isRoot(this);
	}

	public void setInUse() {
		if (tree != null) tree.setInUse(this);
	}

	public void updateWithValsFrom(DistOENode2 other) {
		accuracy = other.getAccuracy();
		horizontalExpansion = other.horizontalExpansion;
		refinementCount = other.refinementCount;
	}
	// </----------------------- non-interface methods ----------------------->

	// <--------------------------- getters/setters -------------------------->
	// accuracy
	public double getAccuracy() {
		return accuracy;
	}

	// description
	public OWLClassExpression getDescription() {
		return description;
	}

	// horizontal expansion
	public int getHorizontalExpansion() {
		return horizontalExpansion;
	}

	// refinement count
	public int getRefinementCount() {
		return refinementCount;
	}

	public void setRefinementCount(int refinementCount) {
		this.refinementCount = refinementCount;
	}

	//tree
	protected void setTree(DistOENodeTree2 tree) {
		this.tree = tree;
	}

	// uuid
	public UUID getUUID() {
		return uuid;
	}
	// </-------------------------- getters/setters -------------------------->
}
