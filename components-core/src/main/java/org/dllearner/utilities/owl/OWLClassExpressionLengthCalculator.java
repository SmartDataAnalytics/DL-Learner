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

import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * Computes the length of a class expression.
 * 
 * @author Lorenz Buehmann
 *
 */
public class OWLClassExpressionLengthCalculator implements
		OWLClassExpressionVisitor, OWLPropertyExpressionVisitor,
		OWLDataRangeVisitor {

	private OWLClassExpressionLengthMetric metric;
	private int length;

	public OWLClassExpressionLengthCalculator(OWLClassExpressionLengthMetric metric) {
		this.metric = metric;
	}

	public OWLClassExpressionLengthCalculator() {
		this.metric = OWLClassExpressionLengthMetric.getDefaultMetric();
	}

	/**
	 * Computes the length of a class expression.
	 * 
	 * @param ce the class expression
	 * @return the length of the class expression
	 */
	public int getLength(OWLClassExpression ce) {
		ce.accept(this);
		return length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb
	 * .owlapi.model.OWLClass)
	 */
	@Override
	public void visit(OWLClass ce) {
		length += metric.classLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb
	 * .owlapi.model.OWLObjectIntersectionOf)
	 */
	@Override
	public void visit(OWLObjectIntersectionOf ce) {
		List<OWLClassExpression> operands = ce.getOperandsAsList();
		for (OWLClassExpression op : operands) {
			op.accept(this);
		}
		length += (operands.size() - 1) * metric.objectIntersectionLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb
	 * .owlapi.model.OWLObjectUnionOf)
	 */
	@Override
	public void visit(OWLObjectUnionOf ce) {
		List<OWLClassExpression> operands = ce.getOperandsAsList();
		for (OWLClassExpression op : operands) {
			op.accept(this);
		}
		length += (operands.size() - 1) * metric.objectUnionLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb
	 * .owlapi.model.OWLObjectComplementOf)
	 */
	@Override
	public void visit(OWLObjectComplementOf ce) {
		ce.getOperand().accept(this);
		length += metric.objectComplementLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb
	 * .owlapi.model.OWLObjectSomeValuesFrom)
	 */
	@Override
	public void visit(OWLObjectSomeValuesFrom ce) {
		ce.getProperty().accept(this);
		ce.getFiller().accept(this);
		length += metric.objectSomeValuesLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb
	 * .owlapi.model.OWLObjectAllValuesFrom)
	 */
	@Override
	public void visit(OWLObjectAllValuesFrom ce) {
		ce.getProperty().accept(this);
		ce.getFiller().accept(this);
		length += metric.objectAllValuesLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb
	 * .owlapi.model.OWLObjectHasValue)
	 */
	@Override
	public void visit(OWLObjectHasValue ce) {
		ce.getProperty().accept(this);
		length += metric.objectHasValueLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb
	 * .owlapi.model.OWLObjectMinCardinality)
	 */
	@Override
	public void visit(OWLObjectMinCardinality ce) {
		ce.getProperty().accept(this);
		ce.getFiller().accept(this);
		length += metric.objectCardinalityLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb
	 * .owlapi.model.OWLObjectExactCardinality)
	 */
	@Override
	public void visit(OWLObjectExactCardinality ce) {
		ce.getProperty().accept(this);
		ce.getFiller().accept(this);
		length += metric.objectCardinalityLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb
	 * .owlapi.model.OWLObjectMaxCardinality)
	 */
	@Override
	public void visit(OWLObjectMaxCardinality ce) {
		ce.getProperty().accept(this);
		ce.getFiller().accept(this);
		length += metric.objectCardinalityLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb
	 * .owlapi.model.OWLObjectHasSelf)
	 */
	@Override
	public void visit(OWLObjectHasSelf ce) {
		ce.getProperty().accept(this);
		length += metric.objectHasValueLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb
	 * .owlapi.model.OWLObjectOneOf)
	 */
	@Override
	public void visit(OWLObjectOneOf ce) {
		length += metric.objectOneOfLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb
	 * .owlapi.model.OWLDataSomeValuesFrom)
	 */
	@Override
	public void visit(OWLDataSomeValuesFrom ce) {
		ce.getProperty().accept(this);
		ce.getFiller().accept(this);
		length += metric.dataSomeValuesLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb
	 * .owlapi.model.OWLDataAllValuesFrom)
	 */
	@Override
	public void visit(OWLDataAllValuesFrom ce) {
		ce.getProperty().accept(this);
		ce.getFiller().accept(this);
		length += metric.dataAllValuesLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb
	 * .owlapi.model.OWLDataHasValue)
	 */
	@Override
	public void visit(OWLDataHasValue ce) {
		ce.getProperty().accept(this);
		length += metric.dataHasValueLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb
	 * .owlapi.model.OWLDataMinCardinality)
	 */
	@Override
	public void visit(OWLDataMinCardinality ce) {
		ce.getProperty().accept(this);
		ce.getFiller().accept(this);
		length += metric.dataCardinalityLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb
	 * .owlapi.model.OWLDataExactCardinality)
	 */
	@Override
	public void visit(OWLDataExactCardinality ce) {
		ce.getProperty().accept(this);
		ce.getFiller().accept(this);
		length += metric.dataCardinalityLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLClassExpressionVisitor#visit(org.semanticweb
	 * .owlapi.model.OWLDataMaxCardinality)
	 */
	@Override
	public void visit(OWLDataMaxCardinality ce) {
		ce.getProperty().accept(this);
		ce.getFiller().accept(this);
		length += metric.dataCardinalityLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.semanticweb.owlapi.model.OWLPropertyExpressionVisitor#visit(org.
	 * semanticweb.owlapi.model.OWLObjectProperty)
	 */
	@Override
	public void visit(OWLObjectProperty property) {
		length += metric.objectProperyLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.semanticweb.owlapi.model.OWLPropertyExpressionVisitor#visit(org.
	 * semanticweb.owlapi.model.OWLObjectInverseOf)
	 */
	@Override
	public void visit(OWLObjectInverseOf property) {
		length += metric.objectInverseLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.semanticweb.owlapi.model.OWLPropertyExpressionVisitor#visit(org.
	 * semanticweb.owlapi.model.OWLDataProperty)
	 */
	@Override
	public void visit(OWLDataProperty property) {
		length += metric.dataProperyLength;
	}

	@Override
	public void visit(@Nonnull OWLAnnotationProperty property) {}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLDataRangeVisitor#visit(org.semanticweb
	 * .owlapi.model.OWLDatatype)
	 */
	@Override
	public void visit(OWLDatatype node) {
		length += metric.datatypeLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLDataRangeVisitor#visit(org.semanticweb
	 * .owlapi.model.OWLDataOneOf)
	 */
	@Override
	public void visit(OWLDataOneOf node) {
		length += metric.dataOneOfLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLDataRangeVisitor#visit(org.semanticweb
	 * .owlapi.model.OWLDataComplementOf)
	 */
	@Override
	public void visit(OWLDataComplementOf node) {
		length += metric.dataComplementLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLDataRangeVisitor#visit(org.semanticweb
	 * .owlapi.model.OWLDataIntersectionOf)
	 */
	@Override
	public void visit(OWLDataIntersectionOf node) {
		Set<OWLDataRange> operands = node.getOperands();
		for (OWLDataRange op : operands) {
			op.accept(this);
		}
		length += (operands.size() - 1) * metric.dataIntersectionLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLDataRangeVisitor#visit(org.semanticweb
	 * .owlapi.model.OWLDataUnionOf)
	 */
	@Override
	public void visit(OWLDataUnionOf node) {
		Set<OWLDataRange> operands = node.getOperands();
		for (OWLDataRange op : operands) {
			op.accept(this);
		}
		length += (operands.size() - 1) * metric.dataUnionLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLDataRangeVisitor#visit(org.semanticweb
	 * .owlapi.model.OWLDatatypeRestriction)
	 */
	@Override
	public void visit(OWLDatatypeRestriction node) {
		Set<OWLFacetRestriction> facetRestrictions = node
				.getFacetRestrictions();
		length += facetRestrictions.size();
	}
}
