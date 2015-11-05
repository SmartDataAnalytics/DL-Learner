/**
 * 
 */
package org.dllearner.core.ref;

import java.util.SortedSet;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorExAdapter;

/**
 * @author Lorenz Buehmann
 *
 */
public abstract class ClassExpressionRefinementOperatorBase extends
		OWLClassExpressionVisitorExAdapter<SortedSet<OWLClassExpression>> implements ClassExpressionRefinementOperator {
	
	protected OWLReasoner reasoner;
	protected OWLDataFactory dataFactory;

	public ClassExpressionRefinementOperatorBase(OWLReasoner reasoner, OWLDataFactory dataFactory) {
		super(dataFactory.getOWLNothing());
		this.reasoner = reasoner;
		this.dataFactory = dataFactory;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.ref.RefinementOperator#refineNode(org.dllearner.core.ref.SearchTreeNode)
	 */
	@Override
	public SortedSet<OWLClassExpression> refineNode(OWLClassExpression ce) {
		SortedSet<OWLClassExpression> refinements = ce.accept(this);
		return refinements;
	}

}
