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

import org.semanticweb.owlapi.model.OWLObject;

import java.io.Serializable;

/**
 * The score class is used to store how well a hypothesis did
 * on a learning problem. Depending on the learning problem at hand,
 * different criteria can be used. (Similar learning problems probably
 * score hypothesis in a similar way.)
 * 
 * TODO: Maybe we don't really need a score, but only EvaluatedDescription.
 * 
 * @author Jens Lehmann
 *
 */
public abstract class Score implements Serializable{

	private static final long serialVersionUID = -6479328496461875019L;

	/**
	 * This method returns a value, which indicates how accurate a
	 * hypothesis solves a learning problem. 
	 * 
	 * @see AbstractLearningProblem#getAccuracyOrTooWeak(OWLObject, double)
	 * @return A value between 0 and 1 indicating the quality (of a hypothesis).
	 */	
	public abstract double getAccuracy();
	
	
}
