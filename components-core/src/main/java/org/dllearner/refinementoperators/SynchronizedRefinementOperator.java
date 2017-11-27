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

import org.dllearner.core.ComponentInitException;
import org.dllearner.utilities.owl.OWLClassExpressionLengthMetric;
import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.List;
import java.util.Set;

/**
 * A wrapper class that makes the call of the refinement methods synchronized, i.e.
 * it's supposed to provide a thread-safe implementation.
 * 
 * TODO This only works if the used datastructures do not need additional 
 * synchronization.
 * @author Lorenz Buehmann
 *
 */
// not for conf
public class SynchronizedRefinementOperator extends AbstractRefinementOperator implements LengthLimitedRefinementOperator{
	
	private final LengthLimitedRefinementOperator delegate;

	public SynchronizedRefinementOperator(LengthLimitedRefinementOperator delegate) {
		this.delegate = delegate;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() throws ComponentInitException {
		synchronized (delegate) {
			delegate.init();
		}
		
		initialized = true;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.refinementoperators.RefinementOperator#refine(org.semanticweb.owlapi.model.OWLClassExpression)
	 */
	@Override
	public Set<OWLClassExpression> refine(OWLClassExpression description) {
		synchronized (delegate) {
			return delegate.refine(description);
		}
	}

	/* (non-Javadoc)
	 * @see org.dllearner.refinementoperators.LengthLimitedRefinementOperator#refine(org.semanticweb.owlapi.model.OWLClassExpression, int)
	 */
	@Override
	public Set<OWLClassExpression> refine(OWLClassExpression description, int maxLength) {
		synchronized (delegate) {
			return delegate.refine(description, maxLength);
		}
	}

	/* (non-Javadoc)
	 * @see org.dllearner.refinementoperators.LengthLimitedRefinementOperator#refine(org.semanticweb.owlapi.model.OWLClassExpression, int, java.util.List)
	 */
	@Override
	public Set<OWLClassExpression> refine(OWLClassExpression description, int maxLength,
			List<OWLClassExpression> knownRefinements) {
		synchronized (delegate) {
			return delegate.refine(description, maxLength, knownRefinements);
		}
	}

	@Override
	public void setLengthMetric(OWLClassExpressionLengthMetric lengthMetric) {
		synchronized (delegate) {
			delegate.setLengthMetric(lengthMetric);
		}
	}

	@Override
	public OWLClassExpressionLengthMetric getLengthMetric() {
		synchronized (delegate) {
			return delegate.getLengthMetric();
		}
	}

	/**
	 * @return the wrapped refinement operator
	 */
	public LengthLimitedRefinementOperator getDelegate() {
		return delegate;
	}

}
