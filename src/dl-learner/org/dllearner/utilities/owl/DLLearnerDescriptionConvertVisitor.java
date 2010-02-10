package org.dllearner.utilities.owl;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.ObjectExactCardinalityRestriction;
import org.dllearner.core.owl.ObjectMaxCardinalityRestriction;
import org.dllearner.core.owl.ObjectMinCardinalityRestriction;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyExpression;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.ObjectValueRestriction;
import org.dllearner.core.owl.Union;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataAllRestriction;
import org.semanticweb.owl.model.OWLDataExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLDataValueRestriction;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLDescriptionVisitor;
import org.semanticweb.owl.model.OWLObjectAllRestriction;
import org.semanticweb.owl.model.OWLObjectComplementOf;
import org.semanticweb.owl.model.OWLObjectExactCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectIntersectionOf;
import org.semanticweb.owl.model.OWLObjectMaxCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectMinCardinalityRestriction;
import org.semanticweb.owl.model.OWLObjectOneOf;
import org.semanticweb.owl.model.OWLObjectSelfRestriction;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectUnionOf;
import org.semanticweb.owl.model.OWLObjectValueRestriction;

public class DLLearnerDescriptionConvertVisitor implements OWLDescriptionVisitor{
	
	private Stack<Description> stack = new Stack<Description>();
	
	public Description getDLLearnerDescription() {
		return stack.pop();
	}

	public static Description getDLLearnerDescription(OWLDescription description) {
		DLLearnerDescriptionConvertVisitor converter = new DLLearnerDescriptionConvertVisitor();
		description.accept(converter);
		return converter.getDLLearnerDescription();
	}
	
	@Override
	public void visit(OWLClass description) {
		stack.push(new NamedClass(description.getURI()));
	}

	@Override
	public void visit(OWLObjectIntersectionOf description) {
		List<Description> descriptions = new ArrayList<Description>();
		for(OWLDescription child : description.getOperands()){
			child.accept(this);
			descriptions.add(stack.pop());
		}
		stack.push(new Intersection(descriptions));
	}

	@Override
	public void visit(OWLObjectUnionOf description) {
		List<Description> descriptions = new ArrayList<Description>();
		for(OWLDescription child : description.getOperands()){
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
	public void visit(OWLObjectSomeRestriction description) {
		ObjectPropertyExpression role = new ObjectProperty(description.getProperty().asOWLObjectProperty().
				getURI().toString());
		description.getFiller().accept(this);
		Description d = stack.pop();
		stack.push(new ObjectSomeRestriction(role, d));
	}

	@Override
	public void visit(OWLObjectAllRestriction description) {
		ObjectPropertyExpression role = new ObjectProperty(description.getProperty().asOWLObjectProperty().
				getURI().toString());
		description.getFiller().accept(this);
		Description d = stack.pop();
		stack.push(new ObjectSomeRestriction(role, d));		
	}

	@Override
	public void visit(OWLObjectValueRestriction description) {
		ObjectPropertyExpression role = new ObjectProperty(description.getProperty().asOWLObjectProperty().
				getURI().toString());
		Individual ind = new Individual(description.getValue().getURI().toString());
		stack.push(new ObjectValueRestriction((ObjectProperty)role, ind));
	}

	@Override
	public void visit(OWLObjectMinCardinalityRestriction description) {
		ObjectPropertyExpression role = new ObjectProperty(description.getProperty().asOWLObjectProperty().
				getURI().toString());
		description.getFiller().accept(this);
		Description d = stack.pop();
		int min = description.getCardinality();
		stack.push(new ObjectMinCardinalityRestriction(min, role, d));
	}

	@Override
	public void visit(OWLObjectExactCardinalityRestriction description) {
		ObjectPropertyExpression role = new ObjectProperty(description.getProperty().asOWLObjectProperty().
				getURI().toString());
		description.getFiller().accept(this);
		Description d = stack.pop();
		int minmax = description.getCardinality();
		stack.push(new ObjectExactCardinalityRestriction(minmax, role, d));
	}

	@Override
	public void visit(OWLObjectMaxCardinalityRestriction description) {
		ObjectPropertyExpression role = new ObjectProperty(description.getProperty().asOWLObjectProperty().
				getURI().toString());
		description.getFiller().accept(this);
		Description d = stack.pop();
		int max = description.getCardinality();
		stack.push(new ObjectMaxCardinalityRestriction(max, role, d));
	}

	@Override
	public void visit(OWLObjectSelfRestriction description) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OWLObjectOneOf description) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OWLDataSomeRestriction description) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OWLDataAllRestriction description) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OWLDataValueRestriction description) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OWLDataMinCardinalityRestriction description) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OWLDataExactCardinalityRestriction description) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(OWLDataMaxCardinalityRestriction description) {
		// TODO Auto-generated method stub
		
	}

}
