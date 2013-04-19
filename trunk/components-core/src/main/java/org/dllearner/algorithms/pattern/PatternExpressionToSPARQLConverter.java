package org.dllearner.algorithms.pattern;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLPropertyExpressionVisitor;

public class PatternExpressionToSPARQLConverter implements OWLClassExpressionVisitor, OWLPropertyExpressionVisitor{
	
	private String sparql = "";
	private Stack<String> variables = new Stack<String>();
	private Map<OWLEntity, String> variablesMapping;
	
	private int classCnt = 0;
	private int propCnt = 0;
	private int indCnt = 0;

	public PatternExpressionToSPARQLConverter() {
		variablesMapping = new HashMap<OWLEntity, String>();
	}
	
	public String convert(String root, OWLClassExpression expr){
		sparql = "";
		variables.push(root);
		expr.accept(this);
		return sparql;
	}
	
	private String getVariable(OWLEntity entity){
		String var = variablesMapping.get(entity);
		if(var == null){
			if(entity.isOWLClass()){
				var = "?cls" + classCnt++;
			} else if(entity.isOWLObjectProperty() || entity.isOWLDataProperty()){
				var = "?p" + propCnt++;
			} else if(entity.isOWLNamedIndividual()){
				var = buildIndividualVariable();
			} 
			variablesMapping.put(entity, var);
		}
		return var;
	}
	
	private String buildIndividualVariable(){
		return "?s" + indCnt++;
	}
	
	private String triple(String subject, String predicate, String object){
		return subject + " " + predicate + " " + object + ".\n";
	}

	@Override
	public void visit(OWLClass expr) {
		sparql += variables.peek() + " a " + getVariable(expr) +".\n";
	}
	
	@Override
	public void visit(OWLObjectProperty property) {
		
	}
	
	@Override
	public void visit(OWLDataProperty property) {
	}

	@Override
	public void visit(OWLObjectIntersectionOf expr) {
		Set<OWLClassExpression> operands = expr.getOperands();
		String variable = variables.peek();
		for (OWLClassExpression operand : operands) {
			sparql += convert(variable, operand);
		}
	}

	@Override
	public void visit(OWLObjectUnionOf expr) {
	}

	@Override
	public void visit(OWLObjectComplementOf expr) {
	}

	@Override
	public void visit(OWLObjectSomeValuesFrom expr) {
		String variable = variables.peek();
		OWLObjectPropertyExpression property = expr.getProperty();
		if(!property.isAnonymous()){
			String objectVariable = buildIndividualVariable();
			variables.push(objectVariable);
			//build the triple for the property
			sparql += triple(variable, getVariable(property.asOWLObjectProperty()), objectVariable);
			//build the rest according to the role filler
			OWLClassExpression filler = expr.getFiller();
			filler.accept(this);
			variables.pop();
			
		} else {
			System.err.println("TODO: complex property expressions");
		}
		
	}

	@Override
	public void visit(OWLObjectAllValuesFrom expr) {
		String variable = variables.peek();
		OWLObjectPropertyExpression property = expr.getProperty();
		if(!property.isAnonymous()){
			String objectVariable = buildIndividualVariable();
			variables.push(objectVariable);
			//build the triple for the property
			sparql += triple(variable, getVariable(property.asOWLObjectProperty()), objectVariable);
			OWLClassExpression filler = expr.getFiller();
			filler.accept(this);
			//build the rest according to the role filler
			sparql += "FILTER NOT EXISTS{";
			objectVariable = buildIndividualVariable();
			variables.push(objectVariable);
			sparql += triple(variable, getVariable(property.asOWLObjectProperty()), objectVariable);
			variables.pop();
			sparql += "}";
			
			
		} else {
			System.err.println("TODO: complex property expressions");
		}
	}

	@Override
	public void visit(OWLObjectHasValue expr) {
	}

	@Override
	public void visit(OWLObjectMinCardinality expr) {
	}

	@Override
	public void visit(OWLObjectExactCardinality expr) {
	}

	@Override
	public void visit(OWLObjectMaxCardinality expr) {
	}

	@Override
	public void visit(OWLObjectHasSelf expr) {
	}

	@Override
	public void visit(OWLObjectOneOf expr) {
	}

	@Override
	public void visit(OWLDataSomeValuesFrom expr) {
	}

	@Override
	public void visit(OWLDataAllValuesFrom expr) {
	}

	@Override
	public void visit(OWLDataHasValue expr) {
	}

	@Override
	public void visit(OWLDataMinCardinality expr) {
	}

	@Override
	public void visit(OWLDataExactCardinality expr) {
	}

	@Override
	public void visit(OWLDataMaxCardinality expr) {
	}

	@Override
	public void visit(OWLObjectInverseOf property) {
	}

}
