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
 * An evaluated hypothesis in the OWL language is a any OWL object like an OWL axiom, an OWL class expression, etc.
 * and its score.
 *
 * @author Lorenz Buehmann
 */
public abstract class EvaluatedHypothesisOWL<T extends OWLObject, S extends Score>
		extends EvaluatedHypothesis<T, S> {

	private static final long serialVersionUID = 1106431570510815033L;
	
	/**
	 * Constructs an evaluated hypothesis using its score.
	 * @param hypothesis The hypothesis, which was evaluated.
	 * @param score The score of the hypothesis.
	 */
	public EvaluatedHypothesisOWL(T hypothesis, S score) {
		super(hypothesis, score);
	}
	

}
