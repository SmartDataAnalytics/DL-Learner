/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.dllearner.core.owl.DataRange;
import org.dllearner.core.owl.DatatypeExactCardinalityRestriction;
import org.dllearner.core.owl.DatatypeMaxCardinalityRestriction;
import org.dllearner.core.owl.DatatypeMinCardinalityRestriction;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypeSomeRestriction;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.ObjectExactCardinalityRestriction;
import org.dllearner.core.owl.ObjectMaxCardinalityRestriction;
import org.dllearner.core.owl.ObjectMinCardinalityRestriction;
import org.dllearner.core.owl.ObjectOneOf;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyExpression;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.ObjectValueRestriction;
import org.dllearner.core.owl.Thing;
import org.dllearner.core.owl.Union;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;

public class DLLearnerDescriptionConvertVisitor implements OWLClassExpressionVisitor{
	
	private Stack<Description> stack = new Stack<Description>();
	
	public Description getDLLearnerDescription() {
		return stack.pop();
	}

	public static Description getDLLearnerDescription(OWLClassExpression description) {
		DLLearnerDescriptionConvertVisitor converter = new DLLearnerDescriptionConvertVisitor();
		description.accept(converter);
		return converter.getDLLearnerDescription();
	}
	
	@Override
	public void visit(OWLClass description) {
		if(description.isOWLNothing()){
			stack.push(Nothing.instance);
		} else if(description.isOWLThing()){
			stack.push(Thing.instance);
		} else {
			stack.push(new NamedClass(description.getIRI().toURI()));
		}
	}

	@Override
	public void visit(OWLObjectIntersectionOf description) {
		List<Description> descriptions = new ArrayList<Description>();
		for(OWLClassExpression child : description.getOperands()){
			child.accept(this);
			descriptions.add(stack.pop());
		}
		stack.push(new Intersection(descriptions));
	}

	@Override
	public void visit(OWLObjectUnionOf description) {
		List<Description> descriptions = new ArrayList<Description>();
		for(OWLClassExpression child : description.getOperands()){
			child.accept(this);
			descriptions.add(stack.pop());
		}
		stack.push(new Union(descriptions));
		
	}

	@Override
	public void visit(OWLObjectComplementOf description) {
		description.getOperand().accept(this);
		Description d = stack.pop();
		stack.push(new Negation(d));
	}

	@Override
	public void visit(OWLObjectSomeValuesFrom description) {
		ObjectPropertyExpression role = new ObjectProperty(description.getProperty().asOWLObjectProperty().
				getIRI().toString());
		description.getFiller().accept(this);
		Description d = stack.pop();
		stack.push(new ObjectSomeRestriction(role, d));
	}

	@Override
	public void visit(OWLObjectAllValuesFrom description) {
		ObjectPropertyExpression role = new ObjectProperty(description.getProperty().asOWLObjectProperty().
				getIRI().toString());
		description.getFiller().accept(this);
		Description d = stack.pop();
		stack.push(new ObjectSomeRestriction(role, d));		
	}

	@Override
	public void visit(OWLObjectHasValue description) {
		ObjectPropertyExpression role = new ObjectProperty(description.getProperty().asOWLObjectProperty().
				getIRI().toString());
		Individual ind = new Individual(description.getValue().asOWLNamedIndividual().getIRI().toString());
		stack.push(new ObjectValueRestriction((ObjectProperty)role, ind));
	}

	@Override
	public void visit(OWLObjectMinCardinality description) {
		ObjectPropertyExpression role = new ObjectProperty(description.getProperty().asOWLObjectProperty().
				getIRI().toString());
		description.getFiller().accept(this);
		Description d = stack.pop();
		int min = description.getCardinality();
		stack.push(new ObjectMinCardinalityRestriction(min, role, d));
	}

	@Override
	public void visit(OWLObjectExactCardinality description) {
		ObjectPropertyExpression role = new ObjectProperty(description.getProperty().asOWLObjectProperty().
				getIRI().toString());
		description.getFiller().accept(this);
		Description d = stack.pop();
		int minmax = description.getCardinality();
		stack.push(new ObjectExactCardinalityRestriction(minmax, role, d));
	}

	@Override
	public void visit(OWLObjectMaxCardinality description) {
		ObjectPropertyExpression role = new ObjectProperty(description.getProperty().asOWLObjectProperty().
				getIRI().toString());
		description.getFiller().accept(this);
		Description d = stack.pop();
		int max = description.getCardinality();
		stack.push(new ObjectMaxCardinalityRestriction(max, role, d));
	}

	@Override
	public void visit(OWLObjectHasSelf description) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OWLObjectOneOf description) {
		stack.push(new ObjectOneOf(OWLAPIConverter.convertIndividuals(description.getIndividuals())));
	}

	@Override
	public void visit(OWLDataSomeValuesFrom description) {
		DatatypeProperty property = new DatatypeProperty(description.getProperty().asOWLDataProperty()
				.getIRI().toString());
		DataRange dataRange = OWLAPIConverter.convertDatatype(description.getFiller().asOWLDatatype());
		stack.push(new DatatypeSomeRestriction(property, dataRange));
	}

	@Override
	public void visit(OWLDataAllValuesFrom description) {
//		DatatypeProperty property = new DatatypeProperty(description.getProperty().asOWLDataProperty()
//				.getURI().toString());
//		DataRange dataRange = OWLAPIConverter.convertDatatype(description.getFiller().asOWLDataType());
//		stack.push(new DatatypeAllRestriction(property, dataRange));
		throw new Error("Unsupported owl construct " + description.getClass() + ". Please inform a DL-Learner developer to add it.");		
	}

	@Override
	public void visit(OWLDataHasValue description) {
//		DatatypeProperty property = new DatatypeProperty(description.getProperty().asOWLDataProperty()
//				.getURI().toString());
//		Constant c = OWLAPIConverter.convertConstant(description.getValue());
//		
//		DataRange dataRange = OWLAPIConverter.convertDatatype(description.getFiller().asOWLDataType());
//		stack.push(new DatatypeValueRestriction(property, dataRange));
		
	}

	@Override
	public void visit(OWLDataMinCardinality description) {
		DatatypeProperty property = new DatatypeProperty(description.getProperty().asOWLDataProperty()
				.getIRI().toString());
		DataRange dataRange = OWLAPIConverter.convertDatatype(description.getFiller().asOWLDatatype());
		int min = description.getCardinality();
		stack.push(new DatatypeMinCardinalityRestriction(property, dataRange,min));
		
	}

	@Override
	public void visit(OWLDataExactCardinality description) {
		DatatypeProperty property = new DatatypeProperty(description.getProperty().asOWLDataProperty()
				.getIRI().toString());
		DataRange dataRange = OWLAPIConverter.convertDatatype(description.getFiller().asOWLDatatype());
		int minmax = description.getCardinality();
		stack.push(new DatatypeExactCardinalityRestriction(property, dataRange, minmax));
		
	}

	@Override
	public void visit(OWLDataMaxCardinality description) {
		DatatypeProperty property = new DatatypeProperty(description.getProperty().asOWLDataProperty()
				.getIRI().toString());
		DataRange dataRange = OWLAPIConverter.convertDatatype(description.getFiller().asOWLDatatype());
		int max = description.getCardinality();
		stack.push(new DatatypeMaxCardinalityRestriction(property, dataRange, max));
		
	}

}
