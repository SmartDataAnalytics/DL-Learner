package org.dllearner.distributed.amqp;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.dllearner.core.AbstractSearchTreeNode;
import org.dllearner.utilities.datastructures.AbstractSearchTree;
import org.dllearner.utilities.datastructures.SearchTreeNode;
import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.model.OWLClassExpression;

public class OENode extends AbstractSearchTreeNode<OENode> implements SearchTreeNode, Serializable {

	private static final long serialVersionUID = -5599031539635942314L;

	protected OWLClassExpression description;
	protected double accuracy;
	protected int horizontalExpansion;
	protected UUID uuid;

	protected List<UUID> children;
	protected UUID parent;

	/**
	 * the refinement count corresponds to the number of refinements of the
	 * OWLClassExpression in this node - it is a better heuristic indicator
	 * than child count (and avoids the problem that adding children changes
	 * the heuristic value) */
	private int refinementCount = 0;

	private static DecimalFormat dfPercent = new DecimalFormat("0.00%");

	public OENode(OWLClassExpression description, double accuracy) {
		this.description = description;
		this.accuracy = accuracy;
		this.horizontalExpansion = OWLClassExpressionUtils.getLength(description) - 1;
		this.uuid = UUID.randomUUID();
		super.children = null;
		this.children = new LinkedList<UUID>();
	}

	protected OENode(OWLClassExpression description, double accuracy, UUID uuid) {
		this.description = description;
		this.accuracy = accuracy;
		this.horizontalExpansion = OWLClassExpressionUtils.getLength(description) - 1;
		this.uuid = uuid;
		children = new LinkedList<UUID>();
	}

	public boolean equals(OENode other) {
		if (other == null) return false;
		else return uuid.equals(other.getUUID());
	}

	public void incHorizontalExpansion() {
		horizontalExpansion++;
	}

	public boolean isRoot() {
		return (parent == null);
	}

	public OWLClassExpression getDescription() {
		return description;
	}

	@Override
	public OWLClassExpression getExpression() {
		return getDescription();
	}

	public double getAccuracy() {
		return accuracy;
	}

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

	public int getRefinementCount() {
		return refinementCount;
	}

	public void setRefinementCount(int refinementCount) {
		this.refinementCount = refinementCount;
	}

	public UUID getUUID() {
		return uuid;
	}

	public OENode copyAndSetBlocked() {
		return new OENode(description, accuracy, uuid);
	}

	public void update(OENode other) {
		// TODO: PW: check if all these updates are really necessary
		description = other.description;
		accuracy = other.accuracy;
		horizontalExpansion = other.horizontalExpansion;
		refinementCount = other.refinementCount;
	}

	@Override
	protected void setParent(OENode node) {
		if (this.parent == node.getUUID()) {
			return;
		} else if (this.parent != null) {
			throw new Error("Parent already set on node");
		}
		this.parent = node.getUUID();
	}

	@Override
	public void addChild(OENode node) {
		node.setParent(this);
		children.add(node.getUUID());
		node.notifyTrees(this.trees);
	}

	@Override
	public Collection<OENode> getChildren() {
		HashSet<OENode> childNodes = new HashSet<OENode>();

		for (AbstractSearchTree<OENode> tree : trees) {
			for (UUID childUUID : children) {
				OENode childNode = ((SearchTree) tree).getNodeByUUID(childUUID);
				if (childNode != null) childNodes.add(childNode);
			}
		}

		return childNodes;
	}

	@Override
	public OENode getParent() {
		for (AbstractSearchTree<OENode> tree : trees) {
			OENode node = ((SearchTree) tree).getNodeByUUID(parent);
			if (node != null) return node;
		}

		return null;
	}
}
