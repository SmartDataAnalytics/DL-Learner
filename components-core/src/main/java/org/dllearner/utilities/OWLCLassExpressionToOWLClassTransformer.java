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
package org.dllearner.utilities;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;

import com.google.common.base.Function;

/**
 * Utility class that returns an OWLClass instead of an OWLClassExpression object.
 * @author Lorenz Buehmann
 *
 */
public class OWLCLassExpressionToOWLClassTransformer implements Function<OWLClassExpression, OWLClass>, java.util.function.Function<OWLClassExpression, OWLClass> {
	@Override
	public OWLClass apply(OWLClassExpression input) {
		return input.asOWLClass();
	}
}
