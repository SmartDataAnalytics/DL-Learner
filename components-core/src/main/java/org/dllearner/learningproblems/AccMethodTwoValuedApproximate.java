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

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

import java.util.Collection;

public interface AccMethodTwoValuedApproximate extends AccMethodApproximate, AccMethodTwoValued {

	/**
	 * calculate approximate accuracy for an expression, according to method
	 * @param description the expression to test
	 * @param positiveExamples set of positive examples
	 * @param negativeExamples set of negative examples
	 * @param noise noise
	 * @return approximate accuracy value or -1 if too weak
	 */
	double getAccApprox2(OWLClassExpression description, Collection<OWLIndividual> positiveExamples, Collection<OWLIndividual> negativeExamples, double noise);
	
}
