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

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.dllearner.core.StringRenderer;
import org.dllearner.core.StringRenderer.Rendering;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;

public class OWLAxiomToSPARQLConverter implements OWLAxiomVisitor{
	
	private String root = "?x";
	private String sparql;
	private OWLClassExpressionToSPARQLConverter expressionConverter;
	
	public String convert(String rootVariable, OWLAxiom axiom){
		this.root = rootVariable;
		sparql = "";
		expressionConverter = new OWLClassExpressionToSPARQLConverter();
		axiom.accept(this);
		return sparql;
	}
	
	public Query asQuery(String rootVariable, OWLAxiom axiom){
		String queryString = "SELECT DISTINCT " + rootVariable + " WHERE {";
		queryString += convert(rootVariable, axiom);
		queryString += "}";
		return QueryFactory.create(queryString, Syntax.syntaxARQ);
	}

	@Override
	public void visit(OWLAnnotationAssertionAxiom axiom) {
	}

	@Override
	public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
	}

	@Override
	public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
	}

	@Override
	public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
	}

	@Override
	public void visit(OWLDeclarationAxiom axiom) {
	}

	@Override
	public void visit(OWLSubClassOfAxiom axiom) {
		OWLClassExpression subClass = axiom.getSubClass();
		String subClassPattern = expressionConverter.convert(root, subClass);
		sparql += subClassPattern;
		
		OWLClassExpression superClass = axiom.getSuperClass();
		String superClassPattern = expressionConverter.convert(root, superClass);
		sparql += superClassPattern;
	}

	@Override
	public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
	}

	@Override
	public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
	}

	@Override
	public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
	}

	@Override
	public void visit(OWLDisjointClassesAxiom axiom) {
	}

	@Override
	public void visit(OWLDataPropertyDomainAxiom axiom) {
	}

	@Override
	public void visit(OWLObjectPropertyDomainAxiom axiom) {
		OWLSubClassOfAxiom subClassOfAxiom = axiom.asOWLSubClassOfAxiom();
	}

	@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
	}

	@Override
	public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
	}

	@Override
	public void visit(OWLDifferentIndividualsAxiom axiom) {
	}

	@Override
	public void visit(OWLDisjointDataPropertiesAxiom axiom) {
	}

	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
	}

	@Override
	public void visit(OWLObjectPropertyRangeAxiom axiom) {
	}

	@Override
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
	}

	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
	}

	@Override
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {
	}

	@Override
	public void visit(OWLDisjointUnionAxiom axiom) {
	}

	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
	}

	@Override
	public void visit(OWLDataPropertyRangeAxiom axiom) {
	}

	@Override
	public void visit(OWLFunctionalDataPropertyAxiom axiom) {
	}

	@Override
	public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
	}

	@Override
	public void visit(OWLClassAssertionAxiom axiom) {
	}

	@Override
	public void visit(OWLEquivalentClassesAxiom axiom) {
	}

	@Override
	public void visit(OWLDataPropertyAssertionAxiom axiom) {
	}

	@Override
	public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
	}

	@Override
	public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
	}

	@Override
	public void visit(OWLSubDataPropertyOfAxiom axiom) {
	}

	@Override
	public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
	}

	@Override
	public void visit(OWLSameIndividualAxiom axiom) {
	}

	@Override
	public void visit(OWLSubPropertyChainOfAxiom axiom) {
	}

	@Override
	public void visit(OWLInverseObjectPropertiesAxiom axiom) {
	}

	@Override
	public void visit(OWLHasKeyAxiom axiom) {
	}

	@Override
	public void visit(OWLDatatypeDefinitionAxiom axiom) {
	}

	@Override
	public void visit(SWRLRule rule) {
	}
	
	public static void main(String[] args) throws Exception {
		StringRenderer.setRenderer(Rendering.DL_SYNTAX);
		OWLAxiomToSPARQLConverter converter = new OWLAxiomToSPARQLConverter();
		
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
		//NAMEDCLASS
		OWLClassExpression subClass = clsA;
		OWLClassExpression superClass = clsB;
		OWLAxiom axiom = df.getOWLSubClassOfAxiom(subClass, superClass);
		String query = converter.asQuery(rootVar, axiom).toString();
		System.out.println(axiom + "\n" + query);
		//EXISTENTIAL RESTRICTION
		superClass = df.getOWLObjectSomeValuesFrom(propR, clsB);
		axiom = df.getOWLSubClassOfAxiom(subClass, superClass);
		query = converter.asQuery(rootVar, axiom).toString();
		System.out.println(axiom + "\n" + query);
		//INTERSECTION
		superClass = df.getOWLObjectIntersectionOf(
				df.getOWLObjectSomeValuesFrom(propR, clsB),
				clsB);
		axiom = df.getOWLSubClassOfAxiom(subClass, superClass);
		query = converter.asQuery(rootVar, axiom).toString();
		System.out.println(axiom + "\n" + query);
		//UNION
		superClass = df.getOWLObjectUnionOf(
				clsB,
				clsC);
		axiom = df.getOWLSubClassOfAxiom(subClass, superClass);
		query = converter.asQuery(rootVar, axiom).toString();
		System.out.println(axiom + "\n" + query);
		//HAS VALUE
		superClass = df.getOWLObjectHasValue(propR, indA);
		axiom = df.getOWLSubClassOfAxiom(subClass, superClass);
		query = converter.asQuery(rootVar, axiom).toString();
		System.out.println(axiom + "\n" + query);
		//UNIVERSAL RESTRICTION
		superClass = df.getOWLObjectAllValuesFrom(propR, clsB);
		axiom = df.getOWLSubClassOfAxiom(subClass, superClass);
		query = converter.asQuery(rootVar, axiom).toString();
		System.out.println(axiom + "\n" + query);
		// ONE OF
		superClass = df.getOWLObjectOneOf(indA, indB);
		axiom = df.getOWLSubClassOfAxiom(subClass, superClass);
		query = converter.asQuery(rootVar, axiom).toString();
		System.out.println(axiom + "\n" + query);
		//existential restriction with one of filler
		superClass = df.getOWLObjectSomeValuesFrom(propR, df.getOWLObjectOneOf(indA, indB));
		axiom = df.getOWLSubClassOfAxiom(subClass, superClass);
		query = converter.asQuery(rootVar, axiom).toString();
		System.out.println(axiom + "\n" + query);
		
		
		
	}

}
