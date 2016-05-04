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
package org.dllearner.learningproblems;

import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedAxiom;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLPropertyAxiom;

@ComponentAnn(name = "PropertyAxiomLearningProblem", shortName = "palp", version = 0.6)
public class PropertyAxiomLearningProblem<T extends OWLPropertyAxiom> extends AbstractLearningProblem<AxiomScore, T, EvaluatedAxiom<T>>{

	private OWLProperty propertyToDescribe;

	@Override
	public void init() throws ComponentInitException {
		// TODO Auto-generated method stub
		throw new ComponentInitException("not implemented");
	}

	@Override
	public AxiomScore computeScore(T hypothesis, double noise) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getAccuracyOrTooWeak(T hypothesis, double noise) {
		// TODO Auto-generated method stub
		return 0;
	}
}
