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

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.dllearner.core.StringRenderer;
import org.dllearner.core.StringRenderer.Rendering;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.vocab.OWLFacet;

import javax.annotation.Nonnull;
import java.util.*;

public class OWLClassExpressionToSPARQLConverter implements OWLClassExpressionVisitor, OWLPropertyExpressionVisitor, OWLDataRangeVisitor{
	
	private String sparql = "";
	private Stack<String> variables = new Stack<>();
	
	private Multimap<Integer, OWLEntity> properties = HashMultimap.create();
	
	private Map<Integer, Boolean> intersection;
	private Set<? extends OWLEntity> variableEntities = new HashSet<>();
	
	private VariablesMapping mapping;
	private boolean ignoreGenericTypeStatements = true;
	private OWLClassExpression expr;
	
	private int cnt;
	
	private String outerGroupBy;
	
	private Map<OWLClassExpression, Set<String>> groupingVars = new HashMap<>();
	private Map<OWLClassExpression, Set<String>> havingConditions = new HashMap<>();
	
	Stack<String> parentVar = new Stack<>();
	Stack<OWLClassExpression> parent = new Stack<>();
	
	public OWLClassExpressionToSPARQLConverter(VariablesMapping mapping) {
		this.mapping = mapping;
	}
	
	public OWLClassExpressionToSPARQLConverter() {
		mapping = new VariablesMapping();
	}
	
	public VariablesMapping getVariablesMapping() {
		return mapping;
	}

	public String convert(String rootVariable, OWLClassExpression expr){
		this.expr = expr;
		reset();
		variables.push(rootVariable);
		parent.push(expr);
		parentVar.push(rootVariable);
		expr.accept(this);
		return sparql;
	}
	
	public String convert(String rootVariable, OWLPropertyExpression expr){
		variables.push(rootVariable);
		sparql = "";
		expr.accept(this);
		return sparql;
	}
	
	public Query asQuery(String rootVariable, OWLClassExpression expr){
		return asQuery(rootVariable, expr, Collections.<OWLEntity>emptySet());
	}
	
	public Query asQuery(String rootVariable, OWLClassExpression expr, boolean countQuery){
		String queryString = "SELECT ";
		String triplePattern = convert(rootVariable, expr);
		if(countQuery){
			queryString += "(COUNT(DISTINCT " + rootVariable + ") AS ?cnt) WHERE {";
		} else {
			queryString += "DISTINCT " + rootVariable + " WHERE {";
		}
		queryString += triplePattern;
		queryString += "}";
		if(outerGroupBy != null) {
			queryString += outerGroupBy;
		}
		return QueryFactory.create(queryString, Syntax.syntaxARQ);
	}
	
	public Query asCountQuery(OWLClassExpression expr){
		String rootVariable = "?s";
		String queryString = "SELECT (COUNT(DISTINCT " + rootVariable + ") AS ?cnt) WHERE {";
		String triplePattern = convert(rootVariable, expr);
		queryString += triplePattern;
		queryString += "}";
		return QueryFactory.create(queryString, Syntax.syntaxARQ);
	}
	
	public Query asQuery(String rootVariable, OWLClassExpression expr, Set<? extends OWLEntity> variableEntities){
		return asQuery(rootVariable, expr, variableEntities, false);
	}
	
	public Query asQuery(String rootVariable, OWLClassExpression expr, Set<? extends OWLEntity> variableEntities, boolean count){
		this.variableEntities = variableEntities;
		String queryString = "SELECT DISTINCT ";
		String triplePattern = convert(rootVariable, expr);
		if(variableEntities.isEmpty()){
			queryString += rootVariable + " WHERE {";
		} else {
			for (OWLEntity owlEntity : variableEntities) {
				String var = mapping.get(owlEntity);
				queryString += var + " ";
			}
			if(count){
				queryString += "(COUNT(DISTINCT " + rootVariable + ") AS ?cnt)"; 
			} else {
				queryString += rootVariable;
			}
			queryString += " WHERE {";
		}
		
		queryString += triplePattern;
		queryString += "}";
		if(!variableEntities.isEmpty()){
			if(count){
				queryString += "GROUP BY ";
				for (OWLEntity owlEntity : variableEntities) {
					String var = mapping.get(owlEntity);
					queryString += var;
				}
				queryString += " ORDER BY DESC(?cnt)";
			}
		}
		
		// append GROUP BY clause if necessary
		Set<String> groupByVars = groupingVars.get(expr);
		if(groupByVars != null) {
			queryString += "GROUP BY " + Joiner.on(" ").join(groupByVars);
			Set<String> conditions = havingConditions.get(expr);
			if(conditions != null) {
				queryString += " HAVING(";
				queryString += Joiner.on(" & ").join(conditions);
				queryString += ")";
			}
		}
		return QueryFactory.create(queryString, Syntax.syntaxARQ);
	}
	
	private void reset(){
		variables.clear();
		properties.clear();
		sparql = "";
		intersection = new HashMap<>();
		mapping.reset();
		cnt = 1;
	}
	
	private int modalDepth(){
		return variables.size();
	}
	
	private boolean inIntersection(){
		return intersection.containsKey(modalDepth()) ? intersection.get(modalDepth()) : false;
	}
	
	private void enterIntersection(){
		intersection.put(modalDepth(), true);
	}
	
	private void leaveIntersection(){
		intersection.remove(modalDepth());
	}
	
	private String triple(String subject, String predicate, String object){
		return (subject.startsWith("?") ? subject : "<" + subject + ">") + " " + 
				(predicate.startsWith("?") || predicate.equals("a") ? predicate : "<" + predicate + ">") + " " +
				(object.startsWith("?") ? object : object) + ".\n";
	}
	
	private String triple(String subject, String predicate, OWLLiteral object){
		return (subject.startsWith("?") ? subject : "<" + subject + ">") + " " + 
				(predicate.startsWith("?") || predicate.equals("a") ? predicate : "<" + predicate + ">") + " " +
				render(object) + ".\n";
	}
	
	private String triple(String subject, String predicate, OWLEntity object){
		return (subject.startsWith("?") ? subject : "<" + subject + ">") + " " + 
				(predicate.startsWith("?") || predicate.equals("a") ? predicate : "<" + predicate + ">") + " " +
				render(object) + ".\n";
	}
	
	private String triple(String subject, OWLEntity predicate, OWLEntity object){
		return (subject.startsWith("?") ? subject : "<" + subject + ">") + " " + 
				render(predicate) + " " +
				render(object) + ".\n";
	}
	
	private String triple(String subject, OWLEntity predicate, String object){
		return (subject.startsWith("?") ? subject : "<" + subject + ">") + " " + 
				render(predicate) + " " +
				object + ".\n";
	}
	
	private String triple(String subject, OWLEntity predicate, OWLLiteral object){
		return (subject.startsWith("?") ? subject : "<" + subject + ">") + " " + 
				render(predicate) + " " +
				render(object) + ".\n";
	}
	
	private String triple(String subject, String predicate, OWLIndividual object){
		return (subject.startsWith("?") ? subject : "<" + subject + ">") + " " + 
				(predicate.startsWith("?") || predicate.equals("a") ? predicate : "<" + predicate + ">") + " " +
				"<" + object.toStringID() + ">.\n";
	}
	
	private String render(OWLEntity entity){
		String s;
		if(variableEntities.contains(entity)){
			s = mapping.getVariable(entity);
		} else {
			s = "<" + entity.toStringID() + ">";
		}
		if(entity.isOWLObjectProperty()){
			properties.put(modalDepth(), entity);
		}
		return s;
	}
	
	private String render(OWLLiteral literal){
		return "\"" + literal.getLiteral() + "\"^^<" + literal.getDatatype().toStringID() + ">";
	}
	
	private String newCountVar() {
		return "?cnt" + cnt++;
	}

	@Override
	public void visit(OWLObjectProperty property) {}

	@Override
	public void visit(OWLObjectInverseOf property) {}

	@Override
	public void visit(OWLDataProperty property) {}

	@Override
	public void visit(@Nonnull OWLAnnotationProperty property) {}

	@Override
	public void visit(OWLClass ce) {
		if(ce.equals(expr) || !ignoreGenericTypeStatements || !ce.isOWLThing()){
			sparql += triple(variables.peek(), "a", render(ce));
		}
	}

	@Override
	public void visit(OWLObjectIntersectionOf ce) {
		enterIntersection();
		List<OWLClassExpression> operands = ce.getOperandsAsList();
		for (OWLClassExpression operand : operands) {
			operand.accept(this);
		}
		Collection<OWLEntity> props = properties.get(modalDepth());
		if(props.size() > 1){
			Collection<String> vars = new TreeSet<>();
			for (OWLEntity p : props) {
				if(mapping.containsKey(p)){
					vars.add(mapping.get(p));
				}
			}
			if(vars.size() == 2){
				List<String> varList = new ArrayList<>(vars);
				sparql += "FILTER(" + varList.get(0) + "!=" + varList.get(1) + ")";
			}
		}
		leaveIntersection();
	}

	@Override
	public void visit(OWLObjectUnionOf ce) {
		List<OWLClassExpression> operands = ce.getOperandsAsList();
		for (int i = 0; i < operands.size()-1; i++) {
			sparql += "{";
			OWLClassExpression operand = operands.get(i);
			parent.push(operand);
			parentVar.push(variables.peek());
			operand.accept(this);
			parent.pop();
			sparql += "}";
			sparql += " UNION ";
		}
		sparql += "{";
		operands.get(operands.size()-1).accept(this);
		sparql += "}";
	}

	@Override
	public void visit(OWLObjectComplementOf ce) {
		String subject = variables.peek();
		if(!inIntersection() && modalDepth() == 1){
			sparql += triple(subject, "?p", "?o");
		} 
		sparql += "FILTER NOT EXISTS {";
		ce.getOperand().accept(this);
		sparql += "}";
	}

	@Override
	public void visit(OWLObjectSomeValuesFrom ce) {
		String objectVariable = mapping.newIndividualVariable();
		OWLObjectPropertyExpression propertyExpression = ce.getProperty();
		if(propertyExpression.isAnonymous()){
			//property expression is inverse of a property
			sparql += triple(objectVariable, propertyExpression.getNamedProperty(), variables.peek());
		} else {
			sparql += triple(variables.peek(), propertyExpression.getNamedProperty(), objectVariable);
		}
		OWLClassExpression filler = ce.getFiller();
		variables.push(objectVariable);
		filler.accept(this);
		variables.pop();
	}

	@Override
	public void visit(OWLObjectAllValuesFrom ce) {
		String subject = variables.peek();
		String objectVariable = mapping.newIndividualVariable();
		OWLObjectPropertyExpression propertyExpression = ce.getProperty();
		OWLObjectProperty predicate = propertyExpression.getNamedProperty();
		OWLClassExpression filler = ce.getFiller();
		
		if(filler.isOWLThing()) {
			sparql += triple(variables.peek(), mapping.newPropertyVariable(), objectVariable);
		} else {
			if(propertyExpression.isAnonymous()){ //property expression is inverse of a property
				sparql += triple(objectVariable, predicate, variables.peek());
			} else {
				sparql += triple(variables.peek(), predicate, objectVariable);
			}
			
			// restrict filler
				String var = mapping.newIndividualVariable();
				String cntVar1 = newCountVar();
				sparql += "{SELECT " + subject + " (COUNT(" + var + ") AS " + cntVar1 + ") WHERE {";
				sparql += triple(subject, predicate, var);
				variables.push(var);
				filler.accept(this);
				variables.pop();
				sparql += "} GROUP BY " + subject + "}";

				var = mapping.newIndividualVariable();
				String cntVar2 = newCountVar();
				sparql += "{SELECT " + subject + " (COUNT(" + var + ") AS " + cntVar2 + ") WHERE {";
				sparql += triple(subject, predicate, var);
				sparql += "} GROUP BY " + subject + "}";

				sparql += "FILTER(" + cntVar1 + "=" + cntVar2 + ")";
		}
		
	}

	@Override
	public void visit(OWLObjectHasValue ce) {
		OWLObjectPropertyExpression propertyExpression = ce.getProperty();
		OWLNamedIndividual value = ce.getFiller().asOWLNamedIndividual();
		if(propertyExpression.isAnonymous()){
			//property expression is inverse of a property
			sparql += triple(value.toStringID(), propertyExpression.getNamedProperty(), variables.peek());
		} else {
			sparql += triple(variables.peek(), propertyExpression.getNamedProperty(), value);
		}
	}

	@Override
	public void visit(OWLObjectMinCardinality ce) {
		processObjectCardinalityRestriction(ce);
	}

	@Override
	public void visit(OWLObjectExactCardinality ce) {
		processObjectCardinalityRestriction(ce);
	}

	@Override
	public void visit(OWLObjectMaxCardinality ce) {
		processObjectCardinalityRestriction(ce);
	}
	
	public void processObjectCardinalityRestriction(OWLObjectCardinalityRestriction ce) {
		String subjectVariable = variables.peek();
		String objectVariable = mapping.newIndividualVariable();
		OWLObjectPropertyExpression propertyExpression = ce.getProperty();
		int cardinality = ce.getCardinality();
		
		String comparator;
		if(ce instanceof OWLObjectMinCardinality) {
			comparator = ">=";
		} else if(ce instanceof OWLObjectExactCardinality) {
			comparator = "=";
		} else {
			comparator = "<=";
		}
		
//		sparql += "{SELECT " + subjectVariable + " WHERE {";
		
		if(propertyExpression.isAnonymous()){
			//property expression is inverse of a property
			sparql += triple(objectVariable, propertyExpression.getNamedProperty(), subjectVariable);
		} else {
			sparql += triple(subjectVariable, propertyExpression.getNamedProperty(), objectVariable);
		}
		
		OWLClassExpression filler = ce.getFiller();
		variables.push(objectVariable);
		filler.accept(this);
		variables.pop();
		
		String havingCondition = "COUNT(" + objectVariable + ")" + comparator + cardinality;
		
		groupingVars.put(parent.peek(), Sets.newHashSet(parentVar.peek()));
		havingConditions.put(parent.peek(), Sets.newHashSet(havingCondition));
		
//		sparql += "} GROUP BY " + subjectVariable + " HAVING(" + havingCondition + ")}";
	}
	
	public void processDataCardinalityRestriction(OWLDataCardinalityRestriction ce) {
		String subjectVariable = variables.peek();
		String objectVariable = mapping.newIndividualVariable();
		OWLDataPropertyExpression propertyExpression = ce.getProperty();
		int cardinality = ce.getCardinality();
		
		String comparator;
		if(ce instanceof OWLDataMinCardinality) {
			comparator = ">=";
		} else if(ce instanceof OWLDataExactCardinality) {
			comparator = "=";
		} else {
			comparator = "<=";
		}
		
		sparql += "{SELECT " + subjectVariable + " WHERE {";
		
		if(propertyExpression.isAnonymous()){
			//property expression is inverse of a property
			sparql += triple(objectVariable, propertyExpression.asOWLDataProperty(), subjectVariable);
		} else {
			sparql += triple(subjectVariable, propertyExpression.asOWLDataProperty(), objectVariable);
		}
		
		OWLDataRange filler = ce.getFiller();
		variables.push(objectVariable);
		filler.accept(this);
		variables.pop();
		
		sparql += "} GROUP BY " + subjectVariable + " HAVING(COUNT(" + objectVariable + ")" + comparator + cardinality + ")}";
	}

	@Override
	public void visit(OWLObjectHasSelf ce) {
		String subject = variables.peek();
		OWLObjectPropertyExpression property = ce.getProperty();
		sparql += triple(subject, property.getNamedProperty(), subject);
	}

	@Override
	public void visit(OWLObjectOneOf ce) {
		String subject = variables.peek();
		if(modalDepth() == 1){
			sparql += triple(subject, "?p", "?o");
		} 
		sparql += "FILTER(" + subject + " IN (";
		String values = "";
		for (OWLIndividual ind : ce.getIndividuals()) {
			if(!values.isEmpty()){
				values += ",";
			}
			values += "<" + ind.toStringID() + ">";
		}
		sparql += values;
		sparql +=  "))"; 
		
	}

	@Override
	public void visit(OWLDataSomeValuesFrom ce) {
		String objectVariable = mapping.newIndividualVariable();
		OWLDataPropertyExpression propertyExpression = ce.getProperty();
		sparql += triple(variables.peek(), propertyExpression.asOWLDataProperty(), objectVariable);
		OWLDataRange filler = ce.getFiller();
		variables.push(objectVariable);
		filler.accept(this);
		variables.pop();
	}

	@Override
	public void visit(OWLDataAllValuesFrom ce) {
		String subject = variables.peek();
		String objectVariable = mapping.newIndividualVariable();
		OWLDataPropertyExpression propertyExpression = ce.getProperty();
		String predicate = propertyExpression.asOWLDataProperty().toStringID();
		OWLDataRange filler = ce.getFiller();
		sparql += triple(variables.peek(), predicate, objectVariable);
		
		String var = mapping.newIndividualVariable();
		sparql += "{SELECT " + subject + " (COUNT(" + var + ") AS ?cnt1) WHERE {";
		sparql += triple(subject, predicate, var);
		variables.push(var);
		filler.accept(this);
		variables.pop();
		sparql += "} GROUP BY " + subject + "}";
		
		var = mapping.newIndividualVariable();
		sparql += "{SELECT " + subject + " (COUNT(" + var + ") AS ?cnt2) WHERE {";
		sparql += triple(subject, predicate, var);
		sparql += "} GROUP BY " + subject + "}";
		
		sparql += "FILTER(?cnt1=?cnt2)";
	}

	@Override
	public void visit(OWLDataHasValue ce) {
		OWLDataPropertyExpression propertyExpression = ce.getProperty();
		OWLLiteral value = ce.getFiller();
		sparql += triple(variables.peek(), propertyExpression.asOWLDataProperty(), value);
	}

	@Override
	public void visit(OWLDataMinCardinality ce) {
		processDataCardinalityRestriction(ce);
	}

	@Override
	public void visit(OWLDataExactCardinality ce) {
		processDataCardinalityRestriction(ce);
	}

	@Override
	public void visit(OWLDataMaxCardinality ce) {
		processDataCardinalityRestriction(ce);
	}
	
	@Override
	public void visit(OWLDatatype node) {
		if(ignoreGenericTypeStatements && !node.isRDFPlainLiteral() && !node.isTopDatatype()){
			sparql += "FILTER(DATATYPE(" + variables.peek() + "=<" + node.getIRI().toString() + ">))";
		}
	}

	@Override
	public void visit(OWLDataOneOf node) {
		String subject = variables.peek();
		if(modalDepth() == 1){
			sparql += triple(subject, "?p", "?o");
		} 
		sparql += "FILTER(" + subject + " IN (";
		String values = "";
		for (OWLLiteral value : node.getValues()) {
			if(!values.isEmpty()){
				values += ",";
			}
			values += render(value);
		}
		sparql += values;
		sparql +=  "))"; 
	}

	@Override
	public void visit(OWLDataComplementOf node) {
	}

	@Override
	public void visit(OWLDataIntersectionOf node) {
	}

	@Override
	public void visit(OWLDataUnionOf node) {
	}

	@Override
	public void visit(OWLDatatypeRestriction node) {
		Set<OWLFacetRestriction> facets = node.getFacetRestrictions();
		
		for (OWLFacetRestriction facetRestriction : facets) {
			OWLFacet facet = facetRestriction.getFacet();
			OWLLiteral value = facetRestriction.getFacetValue();
			
			switch(facet) {
			case FRACTION_DIGITS:
				break;
			case LANG_RANGE:
				break;
			case LENGTH:
				break;
			case MAX_EXCLUSIVE: sparql += "FILTER(" + variables.peek() + " < " + "\"" + value.getLiteral() + "\"^^<" + value.getDatatype().toStringID() + ">)";
				break;
			case MAX_INCLUSIVE: sparql += "FILTER(" + variables.peek() + " <= " + "\"" + value.getLiteral() + "\"^^<" + value.getDatatype().toStringID() + ">)";
				break;
			case MAX_LENGTH:
				break;
			case MIN_EXCLUSIVE: sparql += "FILTER(" + variables.peek() + " > " + "\"" + value.getLiteral() + "\"^^<" + value.getDatatype().toStringID() + ">)";
				break;
			case MIN_INCLUSIVE: sparql += "FILTER(" + variables.peek() + " >= " + "\"" + value.getLiteral() + "\"^^<" + value.getDatatype().toStringID() + ">)";
				break;
			case MIN_LENGTH:
				break;
			case PATTERN:
				break;
			case TOTAL_DIGITS:
				break;
			default:
				break;
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		StringRenderer.setRenderer(Rendering.DL_SYNTAX);
		OWLClassExpressionToSPARQLConverter converter = new OWLClassExpressionToSPARQLConverter();
		
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = man.getOWLDataFactory();
		PrefixManager pm = new DefaultPrefixManager();
		pm.setDefaultPrefix("http://dbpedia.org/ontology/");
		
		OWLClass clsA = df.getOWLClass("A", pm);
		OWLClass clsB = df.getOWLClass("B", pm);
		OWLClass clsC = df.getOWLClass("C", pm);
		
		OWLObjectProperty propR = df.getOWLObjectProperty("r", pm);
		OWLObjectProperty propS = df.getOWLObjectProperty("s", pm);
		
		OWLDataProperty dpT = df.getOWLDataProperty("t", pm);
		OWLDataRange booleanRange = df.getBooleanOWLDatatype();
		OWLLiteral lit = df.getOWLLiteral(1);
		
		OWLIndividual indA = df.getOWLNamedIndividual("a", pm);
		OWLIndividual  indB = df.getOWLNamedIndividual("b", pm);
		
		String rootVar = "?x";
		
		OWLClassExpression expr = clsA;
		String query = converter.asQuery(rootVar, expr).toString();
		System.out.println(expr + "\n" + query);
		
		expr = df.getOWLObjectSomeValuesFrom(propR, clsB);
		query = converter.asQuery(rootVar, expr).toString();
		System.out.println(expr + "\n" + query);
		
		expr = df.getOWLObjectIntersectionOf(
				df.getOWLObjectSomeValuesFrom(propR, clsB),
				clsB);
		query = converter.asQuery(rootVar, expr).toString();
		System.out.println(expr + "\n" + query);
		
		expr = df.getOWLObjectUnionOf(
				clsA,
				clsB);
		query = converter.asQuery(rootVar, expr).toString();
		System.out.println(expr + "\n" + query);
		
		expr = df.getOWLObjectHasValue(propR, indA);
		query = converter.asQuery(rootVar, expr).toString();
		System.out.println(expr + "\n" + query);
		
		expr = df.getOWLObjectAllValuesFrom(propR, df.getOWLThing());
		query = converter.asQuery(rootVar, expr).toString();
		System.out.println(expr + "\n" + query);
		
		expr = df.getOWLObjectAllValuesFrom(propR, clsB);
		query = converter.asQuery(rootVar, expr).toString();
		System.out.println(expr + "\n" + query);
		
		expr = df.getOWLObjectAllValuesFrom(df.getOWLObjectProperty("language", pm), df.getOWLClass("Language", pm));
		query = converter.asQuery(rootVar, expr).toString();
		System.out.println(expr + "\n" + query);
		
		expr = df.getOWLObjectMinCardinality(2, df.getOWLObjectProperty("language", pm), df.getOWLClass("Language", pm));
		query = converter.asQuery(rootVar, expr).toString();
		System.out.println(expr + "\n" + query);
		
		expr = df.getOWLObjectIntersectionOf(
				df.getOWLClass("Place", pm),
				df.getOWLObjectMinCardinality(
						2, 
						df.getOWLObjectProperty("language", pm), 
						df.getOWLClass("Language", pm)));
		query = converter.asQuery(rootVar, expr).toString();
		System.out.println(expr + "\n" + query);
		
		expr = df.getOWLObjectOneOf(indA, indB);
		query = converter.asQuery(rootVar, expr).toString();
		System.out.println(expr + "\n" + query);
		
		expr = df.getOWLObjectSomeValuesFrom(propR, df.getOWLObjectOneOf(indA, indB));
		query = converter.asQuery(rootVar, expr).toString();
		System.out.println(expr + "\n" + query);
		
		expr = df.getOWLObjectIntersectionOf(
				clsA, 
				df.getOWLObjectHasSelf(propR));
		query = converter.asQuery(rootVar, expr).toString();
		System.out.println(expr + "\n" + query);
		
		expr = df.getOWLObjectIntersectionOf(
				clsA, 
				df.getOWLDataSomeValuesFrom(dpT, booleanRange));
		query = converter.asQuery(rootVar, expr).toString();
		System.out.println(expr + "\n" + query);
		
		expr = df.getOWLObjectIntersectionOf(
				clsA, 
				df.getOWLDataHasValue(dpT, lit));
		query = converter.asQuery(rootVar, expr).toString();
		System.out.println(expr + "\n" + query);
		
		expr = df.getOWLObjectIntersectionOf(
				clsA, 
				df.getOWLDataMinCardinality(2, dpT, booleanRange));
		query = converter.asQuery(rootVar, expr).toString();
		System.out.println(expr + "\n" + query);
		
		expr = df.getOWLObjectComplementOf(clsB);
		query = converter.asQuery(rootVar, expr).toString();
		System.out.println(expr + "\n" + query);
		
		expr = df.getOWLObjectIntersectionOf(
				clsA, 
				df.getOWLObjectComplementOf(clsB));
		query = converter.asQuery(rootVar, expr).toString();
		System.out.println(expr + "\n" + query);
		
		expr = df.getOWLObjectSomeValuesFrom(propR, 
				df.getOWLObjectIntersectionOf(
						clsA, 
						df.getOWLObjectComplementOf(clsB)));
		query = converter.asQuery(rootVar, expr).toString();
		System.out.println(expr + "\n" + query);
		
		expr = df.getOWLDataAllValuesFrom(dpT, booleanRange);
		query = converter.asQuery(rootVar, expr).toString();
		System.out.println(expr + "\n" + query);
		
		expr = df.getOWLDataAllValuesFrom(dpT,df.getOWLDataOneOf(lit));
		query = converter.asQuery(rootVar, expr).toString();
		System.out.println(expr + "\n" + query);
		
		//variable entity
		expr = df.getOWLObjectIntersectionOf(
				df.getOWLObjectSomeValuesFrom(propR, clsB),
				clsB, df.getOWLObjectSomeValuesFrom(propS, clsA));
		query = converter.asQuery(rootVar, expr, Sets.newHashSet(propR, propS)).toString();
		System.out.println(expr + "\n" + query);
		
		expr = df.getOWLObjectIntersectionOf(
				df.getOWLObjectSomeValuesFrom(propR, df.getOWLObjectIntersectionOf(df.getOWLObjectSomeValuesFrom(propS, clsA), clsC)),
				clsB);
		query = converter.asQuery(rootVar, expr, Sets.newHashSet(propR, propS)).toString();
		System.out.println(expr + "\n" + query);
		
		expr = df.getOWLObjectSomeValuesFrom(
				propS, 
				df.getOWLObjectMaxCardinality(
						4, 
						df.getOWLObjectProperty(IRI.create("http://dl-learner.org/carcinogenesis#hasAtom")), 
						df.getOWLThing()
				)
				);
		query = converter.asQuery(rootVar, expr).toString();
		System.out.println(expr + "\n" + query);
		
		expr = df.getOWLObjectAllValuesFrom(propR, df.getOWLThing());
		query = converter.asQuery(rootVar, expr).toString();
		System.out.println(expr + "\n" + query);

		expr = df.getOWLObjectSomeValuesFrom(propR, df.getOWLThing());
		query = converter.asQuery(rootVar, expr).toString();
		System.out.println(expr + "\n" + query);}
}