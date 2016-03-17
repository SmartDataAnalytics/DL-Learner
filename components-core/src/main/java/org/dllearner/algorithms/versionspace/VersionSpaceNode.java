package org.dllearner.algorithms.versionspace;

import org.dllearner.core.Score;

import javax.annotation.Nonnull;

/**
 * A node in the version space. It basically wraps a hypothesis as well as a score. Node equality is defined by
 * hypothesis equality, i.e. <code>v_1 = v_2 <-> h(v1) = h(v2)</code>.
 *
 * @author Lorenz Buehmann
 */
public class VersionSpaceNode<T> {

	private T hypothesis;

	private Score score;


	public VersionSpaceNode(@Nonnull T hypothesis) {
		this.hypothesis = hypothesis;
	}

	/**
	 * @return the hypothesis
	 */
	public T getHypothesis() {
		return hypothesis;
	}

	/**
	 * @param score the current score of this node after evaluation
	 */
	public void setScore(@Nonnull Score score) {
		this.score = score;
	}

	/**
	 * @return the current score of this node after evaluation
	 */
	public Score getScore() {
		return score;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof VersionSpaceNode)) return false;

		VersionSpaceNode<?> that = (VersionSpaceNode<?>) o;

		return hypothesis.equals(that.hypothesis);
	}

	@Override
	public int hashCode() {
		return hypothesis.hashCode();
	}

	@Override
	public String toString() {
		return "VersionSpaceNode{" +
				"hypothesis=" + hypothesis +
				", score=" + score +
				'}';
	}
}
