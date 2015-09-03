package org.dllearner.algorithms.distributed.containers;

import org.dllearner.algorithms.distributed.DistOENodeTree2;
import org.dllearner.utilities.owl.EvaluatedDescriptionSet;
import org.semanticweb.owlapi.model.OWLClassExpression;

public class NodeTreeContainer2 implements MessageContainer {
	private static final long serialVersionUID = 6662673346574604637L;

	private DistOENodeTree2 tree;
	private double bestAccuracy;
	private OWLClassExpression bestDescription;
	private EvaluatedDescriptionSet bestEvaluatedDescriptions;

	public NodeTreeContainer2(DistOENodeTree2 tree) {
		this.tree = tree;
	}

	public NodeTreeContainer2(DistOENodeTree2 tree, double bestAccuracy,
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

	public DistOENodeTree2 getTree() {
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
