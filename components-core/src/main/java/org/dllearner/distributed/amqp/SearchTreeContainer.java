package org.dllearner.distributed.amqp;

import org.dllearner.algorithms.celoe.OENode;
import org.dllearner.utilities.datastructures.SearchTree;
import org.dllearner.utilities.owl.EvaluatedDescriptionSet;
import org.semanticweb.owlapi.model.OWLClassExpression;

public class SearchTreeContainer implements MessageContainer {
	// TODO: PW: fix SeachTree instances to non-abstract type
	private static final long serialVersionUID = 6662673346574604637L;

	private SearchTree<OENode> tree;
	private double bestAccuracy;
	private OWLClassExpression bestDescription;
	private EvaluatedDescriptionSet bestEvaluatedDescriptions;
	private int numChecks;

	public SearchTreeContainer(SearchTree<OENode> tree) {
		this.tree = tree;
	}

	public SearchTreeContainer(SearchTree<OENode> tree, double bestAccuracy,
			OWLClassExpression bestDescription,
			EvaluatedDescriptionSet bestEvaluatedDescriptions, int numChecks) {

		this.tree = tree;
		this.bestAccuracy = bestAccuracy;
		this.bestDescription = bestDescription;
		this.bestEvaluatedDescriptions = bestEvaluatedDescriptions;
		this.numChecks = numChecks;
	}

	@Override
	public String toString() {
		return "[" + tree.getRoot().toString() + " ...] (" + tree.size() + ")";
	}

	public SearchTree<OENode> getTree() {
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

	public int getNumChecks() {
		return numChecks;
	}
}
