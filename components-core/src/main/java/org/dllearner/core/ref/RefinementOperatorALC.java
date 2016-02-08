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

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

/**
 * @author Lorenz Buehmann
 *
 */
public class RefinementOperatorALC extends ClassExpressionRefinementOperatorBase {
	
	public RefinementOperatorALC(OWLReasoner reasoner, OWLDataFactory dataFactory) {
		super(reasoner, dataFactory);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.util.OWLClassExpressionVisitorExAdapter#visit(org.semanticweb.owlapi.model.OWLClass)
	 */
	@Override
	public SortedSet<OWLClassExpression> visit(OWLClass ce) {
		SortedSet<OWLClassExpression> refinements = new TreeSet<>();
		
		refinements.addAll(reasoner.getSubClasses(ce, true).getFlattened());
		
		return refinements;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.util.OWLClassExpressionVisitorExAdapter#visit(org.semanticweb.owlapi.model.OWLObjectIntersectionOf)
	 */
	@Override
	public SortedSet<OWLClassExpression> visit(OWLObjectIntersectionOf ce) {
		SortedSet<OWLClassExpression> refinements = new TreeSet<>();
		
		for (OWLClassExpression operand : ce.getOperands()) {
			// refine operand
			SortedSet<OWLClassExpression> operandRefinements = refineNode(operand);
			
			for (OWLClassExpression operandRefinement : operandRefinements) {
				Set<OWLClassExpression> newOperands = new HashSet<>(ce.getOperands());
				newOperands.remove(operand);
				newOperands.add(operandRefinement);
				
				refinements.add(dataFactory.getOWLObjectIntersectionOf(newOperands));
			}
		}
		
		return refinements;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.util.OWLClassExpressionVisitorExAdapter#visit(org.semanticweb.owlapi.model.OWLObjectUnionOf)
	 */
	@Override
	public SortedSet<OWLClassExpression> visit(OWLObjectUnionOf ce) {
		SortedSet<OWLClassExpression> refinements = new TreeSet<>();

		for (OWLClassExpression operand : ce.getOperands()) {
			// refine operand
			SortedSet<OWLClassExpression> operandRefinements = refineNode(operand);
			
			for (OWLClassExpression operandRefinement : operandRefinements) {
				Set<OWLClassExpression> newOperands = new HashSet<>(ce.getOperands());
				newOperands.remove(operand);
				newOperands.add(operandRefinement);
				
				refinements.add(dataFactory.getOWLObjectUnionOf(newOperands));
			}
		}
		
		return refinements;
	}
	
	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.util.OWLClassExpressionVisitorExAdapter#visit(org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom)
	 */
	@Override
	public SortedSet<OWLClassExpression> visit(OWLObjectSomeValuesFrom ce) {
		SortedSet<OWLClassExpression> refinements = new TreeSet<>();
		
		OWLObjectPropertyExpression property = ce.getProperty();
		OWLClassExpression filler = ce.getFiller();
		
		// refine property
		Set<OWLObjectPropertyExpression> subProperties = reasoner.getSubObjectProperties(property, true).getFlattened();
		for (OWLObjectPropertyExpression subProperty : subProperties) {
			refinements.add(dataFactory.getOWLObjectSomeValuesFrom(subProperty, filler));
		}
		
		// refine filler
		SortedSet<OWLClassExpression> fillerRefinements = refineNode(filler);
		for (OWLClassExpression fillerRefinement : fillerRefinements) {
			refinements.add(dataFactory.getOWLObjectSomeValuesFrom(property, fillerRefinement));
		}
		
		return refinements;
	}
	
	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.util.OWLClassExpressionVisitorExAdapter#visit(org.semanticweb.owlapi.model.OWLObjectAllValuesFrom)
	 */
	@Override
	public SortedSet<OWLClassExpression> visit(OWLObjectAllValuesFrom ce) {
		SortedSet<OWLClassExpression> refinements = new TreeSet<>();
		
		OWLObjectPropertyExpression property = ce.getProperty();
		OWLClassExpression filler = ce.getFiller();
		
		// refine property
		Set<OWLObjectPropertyExpression> subProperties = reasoner.getSubObjectProperties(property, true).getFlattened();
		for (OWLObjectPropertyExpression subProperty : subProperties) {
			refinements.add(dataFactory.getOWLObjectAllValuesFrom(subProperty, filler));
		}
		
		// refine filler
		SortedSet<OWLClassExpression> fillerRefinements = refineNode(filler);
		for (OWLClassExpression fillerRefinement : fillerRefinements) {
			refinements.add(dataFactory.getOWLObjectAllValuesFrom(property, fillerRefinement));
		}
		
		return refinements;
	}
	
	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.util.OWLClassExpressionVisitorExAdapter#visit(org.semanticweb.owlapi.model.OWLDataSomeValuesFrom)
	 */
	@Override
	public SortedSet<OWLClassExpression> visit(OWLDataSomeValuesFrom ce) {
		SortedSet<OWLClassExpression> refinements = new TreeSet<>();
		
		OWLDataPropertyExpression property = ce.getProperty();
		OWLDataRange filler = ce.getFiller();
		
		// refine property
		Set<OWLDataProperty> subProperties = reasoner.getSubDataProperties(property.asOWLDataProperty(), true).getFlattened();
		for (OWLDataProperty subProperty : subProperties) {
			refinements.add(dataFactory.getOWLDataSomeValuesFrom(subProperty, filler));
		}
		
		// refine filler
		// TODO
		
		return refinements;
	}
	
	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.util.OWLClassExpressionVisitorExAdapter#visit(org.semanticweb.owlapi.model.OWLDataAllValuesFrom)
	 */
	@Override
	public SortedSet<OWLClassExpression> visit(OWLDataAllValuesFrom ce) {
		SortedSet<OWLClassExpression> refinements = new TreeSet<>();
		
		OWLDataPropertyExpression property = ce.getProperty();
		OWLDataRange filler = ce.getFiller();
		
		// refine property
		Set<OWLDataProperty> subProperties = reasoner.getSubDataProperties(property.asOWLDataProperty(), true).getFlattened();
		for (OWLDataProperty subProperty : subProperties) {
			refinements.add(dataFactory.getOWLDataSomeValuesFrom(subProperty, filler));
		}
		
		// refine filler
		// TODO
		
		return refinements;
	}

}
