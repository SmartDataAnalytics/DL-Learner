package org.dllearner.algorithms.versionspace;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that picks a node randomly from the set of nodes in the given version space.
 *
 * @author Lorenz Buehmann
 */
public class RandomNodePicker {

	private VersionSpace space;

	private final List<VersionSpaceNode> nodes;

	private final RandomDataGenerator rnd = new RandomDataGenerator();

	public RandomNodePicker(VersionSpace space) {
		this.space = space;

		// put all nodes into list
		nodes = new ArrayList<>(space.vertexSet());
	}

	/**
	 * @return a random node from the version space
	 */
	public VersionSpaceNode selectRandomNode() {
		return nodes.get(rnd.nextInt(0, nodes.size()));
	}
}
