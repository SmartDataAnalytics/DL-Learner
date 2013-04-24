package org.dllearner.utilities.owl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.aksw.commons.collections.diff.ModelDiff;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataComplementOf;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataRangeVisitor;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDataUnionOf;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
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
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLPropertyExpressionVisitor;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;

public class OWLClassExpressionToSPARQLConverter implements OWLClassExpressionVisitor, OWLPropertyExpressionVisitor, OWLDataRangeVisitor{
	
	private String sparql = "";
	private Stack<String> variables = new Stack<String>();
	private Map<OWLEntity, String> variablesMapping;
	
	private int classCnt = 0;
	private int propCnt = 0;
	private int indCnt = 0;
	
	private OWLDataFactory df = new OWLDataFactoryImpl();
	
	private Map<Integer, Boolean> intersection;
	
	public OWLClassExpressionToSPARQLConverter() {
	}

	public String convert(String rootVariable, OWLClassExpression expr){
		reset();
		variables.push(rootVariable);
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
		String queryString = "SELECT DISTINCT " + rootVariable + " WHERE {";
		queryString += convert(rootVariable, expr);
		queryString += "}";
		return QueryFactory.create(queryString, Syntax.syntaxARQ);
	}
	
	private void reset(){
		variables.clear();
		classCnt = 0;
		propCnt = 0;
		indCnt = 0;
		sparql = "";
		intersection = new HashMap<Integer, Boolean>();
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
				(object.startsWith("?") ? object : "<" + object + ">") + ".\n";
	}
	
	private String triple(String subject, String predicate, OWLLiteral object){
		return (subject.startsWith("?") ? subject : "<" + subject + ">") + " " + 
				(predicate.startsWith("?") || predicate.equals("a") ? predicate : "<" + predicate + ">") + " " +
				render(object) + ".\n";
	}
	
	private String triple(String subject, String predicate, OWLIndividual object){
		return (subject.startsWith("?") ? subject : "<" + subject + ">") + " " + 
				(predicate.startsWith("?") || predicate.equals("a") ? predicate : "<" + predicate + ">") + " " +
				"<" + object.toStringID() + ">.\n";
	}
	
	private String render(OWLLiteral literal){
		return "\"" + literal + "\"^^<" + literal.getDatatype().toStringID() + ">";
	}

	@Override
	public void visit(OWLObjectProperty property) {
	}

	@Override
	public void visit(OWLObjectInverseOf property) {
	}

	@Override
	public void visit(OWLDataProperty property) {
	}

	@Override
	public void visit(OWLClass ce) {
		sparql += triple(variables.peek(), "a", ce.toStringID());
	}

	@Override
	public void visit(OWLObjectIntersectionOf ce) {
		enterIntersection();
		List<OWLClassExpression> operands = ce.getOperandsAsList();
		for (OWLClassExpression operand : operands) {
			operand.accept(this);
		}
		leaveIntersection();
	}

	@Override
	public void visit(OWLObjectUnionOf ce) {
		List<OWLClassExpression> operands = ce.getOperandsAsList();
		for (int i = 0; i < operands.size()-1; i++) {
			sparql += "{";
			operands.get(i).accept(this);
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
		String objectVariable = buildIndividualVariable();
		OWLObjectPropertyExpression propertyExpression = ce.getProperty();
		if(propertyExpression.isAnonymous()){
			//property expression is inverse of a property
			sparql += triple(objectVariable, propertyExpression.getNamedProperty().toStringID(), variables.peek());
		} else {
			sparql += triple(variables.peek(), propertyExpression.getNamedProperty().toStringID(), objectVariable);
		}
		OWLClassExpression filler = ce.getFiller();
		if(filler.isAnonymous()){
			variables.push(objectVariable);
			filler.accept(this);
			variables.pop();
		} else {
			sparql += triple(objectVariable, "a", filler.asOWLClass().toStringID());
		}
		
	}

	@Override
	public void visit(OWLObjectAllValuesFrom ce) {
		String subject = variables.peek();
		String objectVariable = buildIndividualVariable();
		OWLObjectPropertyExpression propertyExpression = ce.getProperty();
		String predicate = propertyExpression.getNamedProperty().toStringID();
		OWLClassExpression filler = ce.getFiller();
		if(propertyExpression.isAnonymous()){
			//property expression is inverse of a property
			sparql += triple(objectVariable, predicate, variables.peek());
		} else {
			sparql += triple(variables.peek(), predicate, objectVariable);
		}
		
		String var = buildIndividualVariable();
		sparql += "{SELECT " + subject + " (COUNT(" + var + ") AS ?cnt1) WHERE {";
		sparql += triple(subject, predicate, var);
		variables.push(var);
		filler.accept(this);
		variables.pop();
		sparql += "} GROUP BY " + subject + "}";
		
		var = buildIndividualVariable();
		sparql += "{SELECT " + subject + " (COUNT(" + var + ") AS ?cnt2) WHERE {";
		sparql += triple(subject, predicate, var);
		sparql += "} GROUP BY " + subject + "}";
		
		sparql += "FILTER(?cnt1=?cnt2)";
		
	}

	@Override
	public void visit(OWLObjectHasValue ce) {
		OWLObjectPropertyExpression propertyExpression = ce.getProperty();
		OWLIndividual value = ce.getValue();
		if(propertyExpression.isAnonymous()){
			//property expression is inverse of a property
			sparql += triple(value.toStringID(), propertyExpression.getNamedProperty().toStringID(), variables.peek());
		} else {
			sparql += triple(variables.peek(), propertyExpression.getNamedProperty().toStringID(), value.toStringID());
		}
	}

	@Override
	public void visit(OWLObjectMinCardinality ce) {
		String subjectVariable = variables.peek();
		String objectVariable = buildIndividualVariable();
		OWLObjectPropertyExpression propertyExpression = ce.getProperty();
		int cardinality = ce.getCardinality();
		sparql += "{SELECT " + subjectVariable + " WHERE {";
		if(propertyExpression.isAnonymous()){
			//property expression is inverse of a property
			sparql += triple(objectVariable, propertyExpression.getNamedProperty().toStringID(), subjectVariable);
		} else {
			sparql += triple(subjectVariable, propertyExpression.getNamedProperty().toStringID(), objectVariable);
		}
		OWLClassExpression filler = ce.getFiller();
		if(filler.isAnonymous()){
			String var = buildIndividualVariable();
			variables.push(var);
			sparql += triple(objectVariable, "a", var);
			filler.accept(this);
			variables.pop();
		} else {
			sparql += triple(objectVariable, "a", filler.asOWLClass().toStringID());
		}
		
		sparql += "} GROUP BY " + subjectVariable + " HAVING(COUNT(" + objectVariable + ")>=" + cardinality + ")}";
		
	}

	@Override
	public void visit(OWLObjectExactCardinality ce) {
		String subjectVariable = variables.peek();
		String objectVariable = buildIndividualVariable();
		OWLObjectPropertyExpression propertyExpression = ce.getProperty();
		int cardinality = ce.getCardinality();
		sparql += "{SELECT " + subjectVariable + " WHERE {";
		if(propertyExpression.isAnonymous()){
			//property expression is inverse of a property
			sparql += triple(objectVariable, propertyExpression.getNamedProperty().toStringID(), subjectVariable);
		} else {
			sparql += triple(subjectVariable, propertyExpression.getNamedProperty().toStringID(), objectVariable);
		}
		OWLClassExpression filler = ce.getFiller();
		if(filler.isAnonymous()){
			String var = buildIndividualVariable();
			variables.push(var);
			sparql += triple(objectVariable, "a", var);
			filler.accept(this);
			variables.pop();
		} else {
			sparql += triple(objectVariable, "a", filler.asOWLClass().toStringID());
		}
		
		sparql += "} GROUP BY " + subjectVariable + " HAVING(COUNT(" + objectVariable + ")=" + cardinality + ")}";
	}

	@Override
	public void visit(OWLObjectMaxCardinality ce) {
		String subjectVariable = variables.peek();
		String objectVariable = buildIndividualVariable();
		OWLObjectPropertyExpression propertyExpression = ce.getProperty();
		int cardinality = ce.getCardinality();
		sparql += "{SELECT " + subjectVariable + " WHERE {";
		if(propertyExpression.isAnonymous()){
			//property expression is inverse of a property
			sparql += triple(objectVariable, propertyExpression.getNamedProperty().toStringID(), subjectVariable);
		} else {
			sparql += triple(subjectVariable, propertyExpression.getNamedProperty().toStringID(), objectVariable);
		}
		OWLClassExpression filler = ce.getFiller();
		if(filler.isAnonymous()){
			String var = buildIndividualVariable();
			variables.push(var);
			sparql += triple(objectVariable, "a", var);
			filler.accept(this);
			variables.pop();
		} else {
			sparql += triple(objectVariable, "a", filler.asOWLClass().toStringID());
		}
		
		sparql += "} GROUP BY " + subjectVariable + " HAVING(COUNT(" + objectVariable + ")<=" + cardinality + ")}";
	}

	@Override
	public void visit(OWLObjectHasSelf ce) {
		String subject = variables.peek();
		OWLObjectPropertyExpression property = ce.getProperty();
		sparql += triple(subject, property.getNamedProperty().toStringID(), subject);
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
		String objectVariable = buildIndividualVariable();
		OWLDataPropertyExpression propertyExpression = ce.getProperty();
		sparql += triple(variables.peek(), propertyExpression.asOWLDataProperty().toStringID(), objectVariable);
		OWLDataRange filler = ce.getFiller();
		variables.push(objectVariable);
		filler.accept(this);
		variables.pop();
	}

	@Override
	public void visit(OWLDataAllValuesFrom ce) {
		String subject = variables.peek();
		String objectVariable = buildIndividualVariable();
		OWLDataPropertyExpression propertyExpression = ce.getProperty();
		String predicate = propertyExpression.asOWLDataProperty().toStringID();
		OWLDataRange filler = ce.getFiller();
		sparql += triple(variables.peek(), predicate, objectVariable);
		
		String var = buildIndividualVariable();
		sparql += "{SELECT " + subject + " (COUNT(" + var + ") AS ?cnt1) WHERE {";
		sparql += triple(subject, predicate, var);
		variables.push(var);
		filler.accept(this);
		variables.pop();
		sparql += "} GROUP BY " + subject + "}";
		
		var = buildIndividualVariable();
		sparql += "{SELECT " + subject + " (COUNT(" + var + ") AS ?cnt2) WHERE {";
		sparql += triple(subject, predicate, var);
		sparql += "} GROUP BY " + subject + "}";
		
		sparql += "FILTER(?cnt1=?cnt2)";
	}

	@Override
	public void visit(OWLDataHasValue ce) {
		OWLDataPropertyExpression propertyExpression = ce.getProperty();
		OWLLiteral value = ce.getValue();
		sparql += triple(variables.peek(), propertyExpression.asOWLDataProperty().toStringID(), value);
	}

	@Override
	public void visit(OWLDataMinCardinality ce) {
		String subjectVariable = variables.peek();
		String objectVariable = buildIndividualVariable();
		OWLDataPropertyExpression propertyExpression = ce.getProperty();
		int cardinality = ce.getCardinality();
		sparql += "{SELECT " + subjectVariable + " WHERE {";
		sparql += triple(subjectVariable, propertyExpression.asOWLDataProperty().toStringID(), objectVariable);
		OWLDataRange filler = ce.getFiller();
		variables.push(objectVariable);
		filler.accept(this);
		variables.pop();
		
		sparql += "} GROUP BY " + subjectVariable + " HAVING(COUNT(" + objectVariable + ")>=" + cardinality + ")}";
	}

	@Override
	public void visit(OWLDataExactCardinality ce) {
		String subjectVariable = variables.peek();
		String objectVariable = buildIndividualVariable();
		OWLDataPropertyExpression propertyExpression = ce.getProperty();
		int cardinality = ce.getCardinality();
		sparql += "{SELECT " + subjectVariable + " WHERE {";
		sparql += triple(subjectVariable, propertyExpression.asOWLDataProperty().toStringID(), objectVariable);
		OWLDataRange filler = ce.getFiller();
		variables.push(objectVariable);
		filler.accept(this);
		variables.pop();
		
		sparql += "} GROUP BY " + subjectVariable + " HAVING(COUNT(" + objectVariable + ")=" + cardinality + ")}";
	}

	@Override
	public void visit(OWLDataMaxCardinality ce) {
		String subjectVariable = variables.peek();
		String objectVariable = buildIndividualVariable();
		OWLDataPropertyExpression propertyExpression = ce.getProperty();
		int cardinality = ce.getCardinality();
		sparql += "{SELECT " + subjectVariable + " WHERE {";
		sparql += triple(subjectVariable, propertyExpression.asOWLDataProperty().toStringID(), objectVariable);
		OWLDataRange filler = ce.getFiller();
		variables.push(objectVariable);
		filler.accept(this);
		variables.pop();
		
		sparql += "} GROUP BY " + subjectVariable + " HAVING(COUNT(" + objectVariable + ")<=" + cardinality + ")}";
	}
	
	@Override
	public void visit(OWLDatatype node) {
		sparql += "FILTER(DATATYPE(" + variables.peek() + "=<" + node.getIRI().toString() + ">))";
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
	}
	
	public static void main(String[] args) throws Exception {
		ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
		OWLClassExpressionToSPARQLConverter converter = new OWLClassExpressionToSPARQLConverter();
		
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = man.getOWLDataFactory();
		PrefixManager pm = new DefaultPrefixManager("http://dbpedia.org/ontology/");
		
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
		
	}

	
	
}