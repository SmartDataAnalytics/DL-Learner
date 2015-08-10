/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dllearner.algorithms.distributed;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

import org.dllearner.algorithms.SearchTreeNode;
import org.dllearner.utilities.owl.OWLAPIRenderers;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.model.OWLClassExpression;

public class DistOENode implements SearchTreeNode, Serializable {

	// <----------------------------- attributes ----------------------------->
	private static final long serialVersionUID = 6036273870961849912L;
	protected final UUID uuid;

	protected OWLClassExpression description;
	protected double accuracy;
	protected int horizontalExpansion;

	/**
	 * A DistOENode object is in the 'in use' state if it is blocked because it
	 * was already sent to a worker process that uses it for concept learning.
	 * Accordingly the node will be unblocked when the worker process returns
	 * its results */
	private boolean inUse;

	/**
	 * A DistOENode object is in the 'disabled' state if it is blocked because
	 * it is known that its refinements will all be redundant or too weak
	 */
	private boolean disabled;

	protected DistOENode parent;
	protected List<DistOENode> children = new LinkedList<DistOENode>();

	// the refinement count corresponds to the number of refinements of the
	// OWLClassExpression in this node - it is a better heuristic indicator than child count
	// (and avoids the problem that adding children changes the heuristic value)
	private int refinementCount = 0;

	private static DecimalFormat dfPercent = new DecimalFormat("0.00%");
	// </---------------------------- attributes ----------------------------->


	// <---------------------------- constructors ---------------------------->
	public DistOENode(DistOENode parentNode, OWLClassExpression description, double accuracy) {
		this(parentNode, description, accuracy, UUID.randomUUID());
	}

	protected DistOENode(DistOENode parentNode, OWLClassExpression description, double accuracy, UUID uuid) {
		this.parent = parentNode;
		this.description = description;
		this.accuracy = accuracy;
		this.uuid = uuid;
		horizontalExpansion = OWLClassExpressionUtils.getLength(description) - 1;
		inUse = false;
		disabled = false;
	}
	// </--------------------------- constructors ---------------------------->


	// <-------------------------- interface methods -------------------------->
	@Override
	public List<DistOENode> getChildren() {
		return children;
	}

	@Override
	public OWLClassExpression getExpression() {
		return description;
	}

	@Override
	public String toString() {
		return getShortDescription(null);
	}
	// </------------------------- interface methods -------------------------->


	// <---------------------------- misc methods ---------------------------->
	public void addChild(DistOENode node) {
		children.add(node);
		// this is evil since it may cause problems in the DistOENodeTree
		node.setParent(this);
	}

	public DistOENode copyTo(DistOENode newParent) {
		DistOENode copiedNode = new DistOENode(newParent, description, accuracy, uuid);
		copiedNode.setRefinementCount(refinementCount);
		copiedNode.setDisabled(disabled);
		copiedNode.setInUse(inUse);
		copiedNode.horizontalExpansion = horizontalExpansion;

		newParent.addChild(copiedNode);

		return copiedNode;
	}

	public boolean equals(DistOENode other) {
		if (other == null) return false;

		return uuid.equals(other.getUUID()) &&
				description.equals(other.getDescription());
	}

	/**
	 * Since we do not assume that object identity of the nodes in a node tree
	 * received from a worker process is preserved we have to check for equality
	 * based on the actual description, accuracy value and horizontal expansion.
	 * The first node from the node tree this node belongs to is returned.
	 * @param node A node from a node tree received from a worker process
	 * @return The first node from the node tree this node belongs to
	 */
	public DistOENode findCorrespondingLocalNode(DistOENode node) {
		if (this.equals(node)) return this;

		for (DistOENode child : children) {
			DistOENode foundNode = child.findCorrespondingLocalNode(node);
			if (foundNode != null) {
				return foundNode;
			}
		}

		return null;
	}

	public TreeSet<DistOENode> getNodeAndDescendantsAndSetUsed() {
		return getNodeAndDescendants(true);
	}

	public TreeSet<DistOENode> getNodeAndDescendants() {
		return getNodeAndDescendants(false);
	}

	private TreeSet<DistOENode> getNodeAndDescendants(boolean setUsed) {
		// FIXME: there should be a mechanism to get the heuristic used in
		// the node tree this node belongs to
		TreeSet<DistOENode> subTree = new TreeSet<DistOENode>(new DistOEHeuristicRuntime());
		subTree.add(this);
		if (setUsed) inUse = true;

		for (DistOENode child : children) {
			subTree.addAll(child.getNodeAndDescendants(setUsed));
		}

		return subTree;
	}

	public String getShortDescription(String baseURI) {
		return getShortDescription(baseURI, null);
	}

	public String getShortDescription(String baseURI, Map<String, String> prefixes) {
		String ret = OWLAPIRenderers.toDLSyntax(description) + " [";
//		ret += "score" + NLPHeuristic.getNodeScore(this) + ",";
		ret += "acc:" + dfPercent.format(accuracy) + ", ";
		ret += "he:" + horizontalExpansion + ", ";
		ret += "c:" + children.size() + ", ";
		ret += "ref:" + refinementCount + "]";
		return ret;
	}

	public void incHorizontalExpansion() {
		horizontalExpansion++;
	}

	public boolean isRoot() {
		return parent == null;
	}

	public String toTreeString() {
		return toTreeString(0, null).toString();
	}

	public String toTreeString(String baseURI) {
		return toTreeString(0, baseURI).toString();
	}

	public String toTreeString(String baseURI, Map<String, String> prefixes) {
		return toTreeString(0, baseURI, prefixes).toString();
	}

	private StringBuilder toTreeString(int depth, String baseURI) {
		StringBuilder treeString = new StringBuilder();

		for (int i = 0; i < depth - 1; i++) {
			treeString.append("  ");
		}

		if (depth != 0) treeString.append("|--> ");

		treeString.append(getShortDescription(baseURI)+"\n");

		for (DistOENode child : children) {
			treeString.append(child.toTreeString(depth+1, baseURI));
		}

		return treeString;
	}

	public StringBuilder toTreeString(int depth, String baseURI, Map<String, String> prefixes) {
		StringBuilder treeString = new StringBuilder();

		for (int i = 0; i < depth - 1; i++) {
			treeString.append("  ");
		}

		if (depth != 0) treeString.append("|--> ");

		treeString.append(getShortDescription(baseURI, prefixes)+"\n");

		for (DistOENode child : children) {
			treeString.append(child.toTreeString(depth+1, baseURI, prefixes));
		}

		return treeString;
	}

	public void updateWithDescriptionScoreValsFrom(DistOENode other) {
		accuracy = other.getAccuracy();
		horizontalExpansion = other.horizontalExpansion;
	}
	// </--------------------------- misc methods ---------------------------->


	// <--------------------------- getters/setters --------------------------->
	public boolean isInUse() {
		return inUse;
	}

	public void setInUse(boolean inUse) {
		this.inUse = inUse;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public UUID getUUID() {
		return uuid;
	}

	public OWLClassExpression getDescription() {
		return description;
	}

	public double getAccuracy() {
		return accuracy;
	}

	public int getHorizontalExpansion() {
		return horizontalExpansion;
	}

	public DistOENode getParent() {
		return parent;
	}

	protected void setParent(DistOENode parent) {
		this.parent = parent;
	}

	public int getRefinementCount() {
		return refinementCount;
	}

	public void setRefinementCount(int refinementCount) {
		this.refinementCount = refinementCount;
	}
	// </-------------------------- getters/setters --------------------------->
}