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
package org.dllearner.core.ref;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorExAdapter;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Lorenz Buehmann
 *
 */
public abstract class ClassExpressionRefinementOperatorBase extends
		OWLClassExpressionVisitorExAdapter<SortedSet<OWLClassExpression>> implements ClassExpressionRefinementOperator {
	
	protected OWLReasoner reasoner;
	protected OWLDataFactory dataFactory;

	public ClassExpressionRefinementOperatorBase(OWLReasoner reasoner, OWLDataFactory dataFactory) {
		super(new TreeSet<>());
		this.reasoner = reasoner;
		this.dataFactory = dataFactory;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.ref.RefinementOperator#refineNode(org.dllearner.core.ref.SearchTreeNode)
	 */
	@Override
	public SortedSet<OWLClassExpression> refineNode(OWLClassExpression ce) {
		return ce.accept(this);
	}

}
