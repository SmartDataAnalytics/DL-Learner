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
package org.dllearner.refinementoperators;

import java.util.Set;

import org.dllearner.core.Component;
import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * Interface for all refinement operators based on OWL/Description Logics.
 * A refinement operator
 * maps a description to a set of descriptions. For downward refinement
 * operators those descriptions are more special. For upward refinement
 * operators, those descriptions are more general. 
 * 
 * @author Jens Lehmann
 *
 */
public interface RefinementOperator extends Component {

	/**
	 * Standard refinement operation.
	 * @param description The description, which will be refined.
	 * @return A set of refinements.
	 */
	Set<OWLClassExpression> refine(OWLClassExpression description);
	
}
