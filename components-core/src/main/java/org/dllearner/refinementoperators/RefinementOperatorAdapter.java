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

import org.dllearner.core.AbstractComponent;
import org.dllearner.utilities.owl.OWLClassExpressionLengthMetric;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.List;
import java.util.Set;

/**
 * Adapter for {@link RefinementOperator} interface.
 * 
 * @author Jens Lehmann
 *
 */
public abstract class RefinementOperatorAdapter extends AbstractRefinementOperator implements LengthLimitedRefinementOperator {
	
	protected OWLDataFactory df = new OWLDataFactoryImpl();

	/* (non-Javadoc)
	 * @see org.dllearner.refinementoperators.RefinementOperator#refine(org.dllearner.core.owl.Description)
	 */
	@Override
	public abstract Set<OWLClassExpression> refine(OWLClassExpression description);

	/* (non-Javadoc)
	 * @see org.dllearner.refinementoperators.RefinementOperator#refine(org.dllearner.core.owl.Description, int)
	 */
	@Override
	public Set<OWLClassExpression> refine(OWLClassExpression description, int maxLength) {
		throw new UnsupportedOperationException();
	}	
	
	/* (non-Javadoc)
	 * @see org.dllearner.refinementoperators.RefinementOperator#refine(org.dllearner.core.owl.Description, int, java.util.List)
	 */
	@Override
	public Set<OWLClassExpression> refine(OWLClassExpression description, int maxLength,
			List<OWLClassExpression> knownRefinements) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLengthMetric(OWLClassExpressionLengthMetric lengthMetric) {
		throw new UnsupportedOperationException();
	}

	@Override
	public OWLClassExpressionLengthMetric getLengthMetric() { return null; }
}
