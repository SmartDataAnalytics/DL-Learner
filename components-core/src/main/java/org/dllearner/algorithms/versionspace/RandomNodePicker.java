package org.dllearner.algorithms.versionspace;

import org.apache.commons.math3.random.RandomDataGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that picks a node randomly from the set of nodes in the given version space.
 *
 * @author Lorenz Buehmann
 */
public class RandomNodePicker<T extends VersionSpaceNode> {

	private VersionSpace space;

	private final List<T> nodes;

	private final RandomDataGenerator rnd = new RandomDataGenerator();

	public RandomNodePicker(VersionSpace space) {
		this.space = space;

		// put all nodes into list
		nodes = new ArrayList<>(space.vertexSet());
	}

	/**
	 * @return a random node from the version space
	 */
	public T selectRandomNode() {
		return nodes.get(rnd.nextInt(0, nodes.size() - 1));
	}
}
