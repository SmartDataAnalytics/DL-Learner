package org.dllearner.algorithms.distributed.containers;

import org.dllearner.algorithms.distributed.DistOENodeTree;
import org.dllearner.utilities.owl.EvaluatedDescriptionSet;
import org.semanticweb.owlapi.model.OWLClassExpression;

public class NodeTreeContainer implements MessageContainer {

	private static final long serialVersionUID = 5296350341118811268L;

	private DistOENodeTree tree;
	private double bestAccuracy;
	private OWLClassExpression bestDescription;
	private EvaluatedDescriptionSet bestEvaluatedDescriptions;

	public NodeTreeContainer(DistOENodeTree tree) {
		this.tree = tree;
//		this(tree, -1, (OWLClassExpression) null,
//				(EvaluatedDescriptionSet) null);
	}

	public NodeTreeContainer(DistOENodeTree tree, double bestAccuracy,
			OWLClassExpression bestDescription,
			EvaluatedDescriptionSet bestEvaluatedDescriptions) {
		this.tree = tree;
		this.bestAccuracy = bestAccuracy;
		this.bestDescription = bestDescription;
		this.bestEvaluatedDescriptions = bestEvaluatedDescriptions;
	}

	@Override
	public String toString() {
		return "[" + tree.getRoot().toString() + " ...] (" + tree.size() + ")";
	}

	public DistOENodeTree getTree() {
		return tree;
	}

	public double getBestAccuracy() {
		return bestAccuracy;
	}

	public OWLClassExpression getBestDescription() {
		return bestDescription;
	}

	public EvaluatedDescriptionSet getBestEvaluatedDescriptions() {
		return bestEvaluatedDescriptions;
	}
}
