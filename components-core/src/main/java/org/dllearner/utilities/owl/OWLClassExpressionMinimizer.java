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
package org.dllearner.utilities.owl;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.dllearner.core.AbstractReasonerComponent;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Lorenz Buehmann
 *
 */
public class OWLClassExpressionMinimizer implements OWLClassExpressionVisitorEx<OWLClassExpression>{
	
	private OWLDataFactory df;
	private AbstractReasonerComponent reasoner;
	private OWLObjectDuplicator objectDuplicator;
	
	private boolean beautify = true;
	
	private Map<OWLClassExpression,Map<OWLClassExpression,Boolean>> cachedSubclassOf = new TreeMap<>();

	public OWLClassExpressionMinimizer(OWLDataFactory dataFactory, AbstractReasonerComponent reasoner) {
		this.df = dataFactory;
		this.reasoner = reasoner;
		
		objectDuplicator = new OWLObjectDuplicator(dataFactory);
	}
	
	public OWLClassExpression minimize(OWLClassExpression ce){
		return ce.accept(this);
	}
	
	public OWLClassExpression minimizeClone(OWLClassExpression ce){
		OWLObjectDuplicator objectDuplicator = new OWLObjectDuplicator(df);
		OWLClassExpression clone = objectDuplicator.duplicateObject(ce);
		return clone.accept(this);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLClass)
	 */
	@Override
	public OWLClassExpression visit(OWLClass ce) {
		return ce;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectIntersectionOf)
	 */
	@Override
	public OWLClassExpression visit(OWLObjectIntersectionOf ce) {
		List<OWLClassExpression> operands = ce.getOperandsAsList();
		//replace operands by the short form
		for (int i = 0; i < operands.size(); i++) {
			operands.set(i, operands.get(i).accept(this));
		}
		
		List<OWLClassExpression> oldOperands = new ArrayList<>(new TreeSet<>(operands));
		List<OWLClassExpression> newOperands = new ArrayList<>(operands);
		
		if(newOperands.size() == 1){
			return newOperands.iterator().next().accept(this);
		}
		
		for (int i = 0; i < oldOperands.size(); i++) {
			OWLClassExpression op1 = oldOperands.get(i);
			for (int j = i + 1; j < oldOperands.size(); j++) {
				OWLClassExpression op2 = oldOperands.get(j);
				
				//remove operand if it is a super class
				if(isSubClassOf(op1, op2)){
					newOperands.remove(op2);
				} else if(isSubClassOf(op2, op1)){
					newOperands.remove(op1);
				}
			}
		}
		
		// combine facet restrictions with same p
		Multimap<OWLDataPropertyExpression, OWLDataSomeValuesFrom> map = HashMultimap.create();
		for (OWLClassExpression operand : newOperands) {
			if(operand instanceof OWLDataSomeValuesFrom) {
				map.put(((OWLDataSomeValuesFrom) operand).getProperty(), (OWLDataSomeValuesFrom) operand);
			}
		}
		for (Entry<OWLDataPropertyExpression, Collection<OWLDataSomeValuesFrom>> entry : map.asMap().entrySet()) {
			OWLDataPropertyExpression dp = entry.getKey();
			Collection<OWLDataSomeValuesFrom> datapropertyRestrictions = entry.getValue();
			
			if(datapropertyRestrictions.size() > 1) {
				Set<OWLFacetRestriction> facetRestrictions = new TreeSet<>();
				for (OWLDataSomeValuesFrom restriction : datapropertyRestrictions) {
					OWLDataRange dataRange = restriction.getFiller();
					if(dataRange instanceof OWLDatatypeRestriction) {
						facetRestrictions.addAll(((OWLDatatypeRestriction) dataRange).getFacetRestrictions());
					}
				}
				if(facetRestrictions.size() > 1) {
					OWLDatatype datatype = ((OWLDatatypeRestriction)datapropertyRestrictions.iterator().next().getFiller()).getDatatype();
					OWLDataRange newDataRange = df.getOWLDatatypeRestriction(datatype, facetRestrictions);
					OWLClassExpression newRestriction = df.getOWLDataSomeValuesFrom(dp, newDataRange);
					newOperands.removeAll(datapropertyRestrictions);
					newOperands.add(newRestriction);
				}
			}
		}
		
		if(newOperands.size() == 1){
			return newOperands.iterator().next().accept(this);
		}
		
		return df.getOWLObjectIntersectionOf(new HashSet<>(newOperands));
	}

	private boolean isSubClassOf(OWLClassExpression subClass, OWLClassExpression superClass) {
		return superClass.isOWLThing() || reasoner.isSuperClassOf(superClass, subClass);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectUnionOf)
	 */
	@Override
	public OWLClassExpression visit(OWLObjectUnionOf ce) {
		List<OWLClassExpression> operands = ce.getOperandsAsList();
		//replace operands by the short form
		for (int i = 0; i < operands.size(); i++) {
			operands.set(i, operands.get(i).accept(this));
		}
		List<OWLClassExpression> oldOperands = new ArrayList<>(new TreeSet<>(operands));
		List<OWLClassExpression> newOperands = new ArrayList<>(operands);
		
		if(newOperands.size() == 1){
			return newOperands.iterator().next().accept(this);
		}
		
		for (int i = 0; i < oldOperands.size(); i++) {
			OWLClassExpression op1 = oldOperands.get(i);
			for (int j = i + 1; j < oldOperands.size(); j++) {
				OWLClassExpression op2 = oldOperands.get(j);
				
				//remove operand if it is a subclass
				if(isSubClassOf(op2, op1)){
					newOperands.remove(op2);
				} else if(isSubClassOf(op1, op2)){
					newOperands.remove(op1);
				} else if(isSubClassOf(op1, df.getOWLObjectComplementOf(op2)) || isSubClassOf(op2, df.getOWLObjectComplementOf(op1))) {
					// check for C or not C
					return df.getOWLThing();
				}
			}
		}
		
		if(newOperands.size() == 1){
			return newOperands.iterator().next().accept(this);
		}
		
		return df.getOWLObjectUnionOf(new HashSet<>(newOperands));
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectComplementOf)
	 */
	@Override
	public OWLClassExpression visit(OWLObjectComplementOf ce) {
		OWLClassExpression operand = ce.getOperand();
		OWLClassExpression shortenedOperand = operand.accept(this);
		if(shortenedOperand.isOWLThing()){// \neg \top \equiv \bot
			return df.getOWLNothing();
		} else if(shortenedOperand.isOWLNothing()){// \neg \bot \equiv \top
			return df.getOWLThing();
		} else if(operand != shortenedOperand){
			return df.getOWLObjectComplementOf(shortenedOperand);
		}
		return ce;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom)
	 */
	@Override
	public OWLClassExpression visit(OWLObjectSomeValuesFrom ce) {
		OWLClassExpression filler = ce.getFiller();
		OWLClassExpression shortendedFiller = filler.accept(this);
		if(shortendedFiller.isOWLNothing()){
			return df.getOWLNothing();
		} else if(filler != shortendedFiller){
			return df.getOWLObjectSomeValuesFrom(ce.getProperty(), shortendedFiller);
		}
		return ce;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectAllValuesFrom)
	 */
	@Override
	public OWLClassExpression visit(OWLObjectAllValuesFrom ce) {
		OWLClassExpression filler = ce.getFiller();
		OWLClassExpression shortendedFiller = filler.accept(this);
		if(shortendedFiller.isOWLThing()){// \forall r.\top \equiv \top
			return df.getOWLThing();
		} else if(beautify && shortendedFiller.isOWLNothing()) {// \forall r.\bot to \neg \exists r.\top
			return df.getOWLObjectComplementOf(df.getOWLObjectSomeValuesFrom(ce.getProperty(), df.getOWLThing()));
		} else if(filler != shortendedFiller){
			return df.getOWLObjectAllValuesFrom(ce.getProperty(), shortendedFiller);
		}
		return ce;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectHasValue)
	 */
	@Override
	public OWLClassExpression visit(OWLObjectHasValue ce) {
		return ce;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectMinCardinality)
	 */
	@Override
	public OWLClassExpression visit(OWLObjectMinCardinality ce) {
		// >= 0 r.C \equiv \top
		int cardinality = ce.getCardinality();
		if (cardinality == 0) {
			return df.getOWLThing();
		}
		OWLClassExpression filler = ce.getFiller();
		OWLClassExpression shortendedFiller = filler.accept(this);
		if(shortendedFiller.isOWLNothing()){// >= n r.\bot \equiv \bot if n != 0
			return df.getOWLNothing();
		} else if(filler != shortendedFiller){
			return df.getOWLObjectMinCardinality(ce.getCardinality(), ce.getProperty(), shortendedFiller);
		}
		return ce;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectExactCardinality)
	 */
	@Override
	public OWLClassExpression visit(OWLObjectExactCardinality ce) {
		OWLClassExpression filler = ce.getFiller();
		OWLClassExpression shortendedFiller = filler.accept(this);
		if(filler != shortendedFiller){
			return df.getOWLObjectExactCardinality(ce.getCardinality(), ce.getProperty(), shortendedFiller);
		}
		return ce;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectMaxCardinality)
	 */
	@Override
	public OWLClassExpression visit(OWLObjectMaxCardinality ce) {
		OWLClassExpression filler = ce.getFiller();
		OWLClassExpression shortendedFiller = filler.accept(this);
		if(shortendedFiller.isOWLNothing()){// <= n r.\bot \equiv \top
			return df.getOWLThing();
		} else if(beautify && ce.getCardinality() == 0) {// we rewrite <= 0 r C to \neg \exists r C - easier to read for humans
			return df.getOWLObjectComplementOf(df.getOWLObjectSomeValuesFrom(ce.getProperty(), shortendedFiller));
		} else if(filler != shortendedFiller){
			return df.getOWLObjectMaxCardinality(ce.getCardinality(), ce.getProperty(), shortendedFiller);
		}
		return ce;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectHasSelf)
	 */
	@Override
	public OWLClassExpression visit(OWLObjectHasSelf ce) {
		return ce;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLObjectOneOf)
	 */
	@Override
	public OWLClassExpression visit(OWLObjectOneOf ce) {
		return ce;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataSomeValuesFrom)
	 */
	@Override
	public OWLClassExpression visit(OWLDataSomeValuesFrom ce) {
		return ce;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataAllValuesFrom)
	 */
	@Override
	public OWLClassExpression visit(OWLDataAllValuesFrom ce) {
		return ce;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataHasValue)
	 */
	@Override
	public OWLClassExpression visit(OWLDataHasValue ce) {
		return ce;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataMinCardinality)
	 */
	@Override
	public OWLClassExpression visit(OWLDataMinCardinality ce) {
		return ce;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataExactCardinality)
	 */
	@Override
	public OWLClassExpression visit(OWLDataExactCardinality ce) {
		return ce;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx#visit(org.semanticweb.owlapi.model.OWLDataMaxCardinality)
	 */
	@Override
	public OWLClassExpression visit(OWLDataMaxCardinality ce) {
		return ce;
	}
}
