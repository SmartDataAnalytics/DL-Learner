/**
 * 
 */
package org.dllearner.refinementoperators;

import java.util.List;
import java.util.Set;

import org.dllearner.core.ComponentInitException;
import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * A wrapper class that makes the call of the refinement methods synchronized, i.e.
 * it's supposed to provide a thread-safe implementation.
 * 
 * TODO This only works if the used datastructures do not need additional 
 * synchronization.
 * @author Lorenz Buehmann
 *
 */
public class SynchronizedRefinementOperator implements LengthLimitedRefinementOperator{
	
	private LengthLimitedRefinementOperator delegate;

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
	
	/**
	 * @return the wrapped refinement operator
	 */
	public LengthLimitedRefinementOperator getDelegate() {
		return delegate;
	}

}
