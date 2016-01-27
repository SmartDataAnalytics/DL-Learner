/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
package org.dllearner.core;

import java.io.Serializable;
import java.text.DecimalFormat;

import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.OWLObject;

import com.google.common.collect.ComparisonChain;

/**
 * An evaluated hypothesis is a hypothesis and its score.
 * 
 * @author Lorenz Buehmann
 *
 */
public abstract class EvaluatedHypothesis<T extends OWLObject, S extends Score> implements Serializable, Comparable<EvaluatedHypothesis<T, S>>{

	private static final long serialVersionUID = 1106431570510815033L;
	
	protected T hypothesis;
	protected S score;
	
	protected static DecimalFormat dfPercent = new DecimalFormat("0.00%");
	
	/**
	 * Constructs an evaluated hypothesis using its score.
	 * @param hypothesis The hypothesis, which was evaluated.
	 * @param score The score of the hypothesis.
	 */
	public EvaluatedHypothesis(T hypothesis, S score) {
		this.hypothesis = hypothesis;
		this.score = score;
	}
	
	/**
	 * Gets the description, which was evaluated.
	 * @return The underlying description.
	 */
	public T getDescription() {
		return hypothesis;
	}
	
	/**
	 * @return the score
	 */
	public S getScore() {
		return score;
	}
	
	/**
	 * Used for rewriting (simplification, beautification) of 
	 * evaluated hypotheses returned by the learning algorithm.
	 * @param hypothesis The hypothesis to set.
	 */
	public void setDescription(T hypothesis) {
		this.hypothesis = hypothesis;
	}	
	
	/**
	 * @see Score#getAccuracy()
	 * @return Value in this score system.
	 */
	public double getAccuracy() {
		return score.getAccuracy();
	}
	
	@Override
	public String toString() {
		return hypothesis.toString() + " " + dfPercent.format(getAccuracy());
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(@NotNull EvaluatedHypothesis<T, S> o) {
		return ComparisonChain.start()
				.compare(score.getAccuracy(), o.score.getAccuracy())
				.compare(hypothesis, o.getDescription())
				.result();
	}

}
