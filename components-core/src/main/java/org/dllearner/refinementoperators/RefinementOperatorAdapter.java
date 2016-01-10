package org.dllearner.refinementoperators;

import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

/**
 * Adapter for {@link RefinementOperator} interface.
 * 
 * @author Jens Lehmann
 *
 */
public abstract class RefinementOperatorAdapter implements LengthLimitedRefinementOperator {
	
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

}
