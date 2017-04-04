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
package org.dllearner.algorithms.pattern;

import com.google.common.collect.Sets;
import org.semanticweb.owlapi.model.*;

import java.util.*;

public class OWLAxiomRenamer implements OWLAxiomVisitor {

	private OWLDataFactory df;
	private OWLClassExpressionRenamer expressionRenamer;
	private OWLAxiom renamedAxiom;
	
	private boolean normalizeABoxAxioms = true;
	private boolean ignoreTrivialAxioms = true;//ignore Thing(a),SubClassOf(A,Thing),SubPropertyOf(A,TopProperty)
	
	public OWLAxiomRenamer(OWLDataFactory df) {
		this.df = df;
	}
	
	public OWLAxiom rename(OWLAxiom axiom){
		Map<OWLEntity, OWLEntity> renaming = new HashMap<>();
		expressionRenamer = new OWLClassExpressionRenamer(df, renaming);
		boolean multipleClasses = axiom.getClassesInSignature().size() > 1;
		expressionRenamer.setMultipleClasses(multipleClasses);
		axiom.accept(this);
		return renamedAxiom;
	}
	
	@Override
	public void visit(OWLSubClassOfAxiom axiom) {
		OWLClassExpression subClass = axiom.getSubClass();
		subClass = expressionRenamer.rename(subClass);
		OWLClassExpression superClass = axiom.getSuperClass();
		superClass = expressionRenamer.rename(superClass);
		renamedAxiom = df.getOWLSubClassOfAxiom(subClass, superClass);
	}

	@Override
	public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		OWLObjectPropertyExpression property = axiom.getProperty();
		property = expressionRenamer.rename(property);
		OWLIndividual subject = axiom.getSubject();
		subject = expressionRenamer.rename(subject);
		OWLIndividual object = axiom.getObject();
		object = expressionRenamer.rename(object);
		renamedAxiom = df.getOWLNegativeObjectPropertyAssertionAxiom(property, subject, object);
	}

	@Override
	public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
		OWLObjectPropertyExpression property = axiom.getProperty();
		property = expressionRenamer.rename(property);
		renamedAxiom = df.getOWLAsymmetricObjectPropertyAxiom(property);
	}

	@Override
	public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
		OWLObjectPropertyExpression property = axiom.getProperty();
		property = expressionRenamer.rename(property);
		renamedAxiom = df.getOWLReflexiveObjectPropertyAxiom(property);
	}

	@Override
	public void visit(OWLDisjointClassesAxiom axiom) {
		Set<OWLClassExpression> classExpressions = axiom.getClassExpressions();
		Set<OWLClassExpression> renamedClassExpressions = new HashSet<>();
		for (OWLClassExpression classExpression : classExpressions) {
			renamedClassExpressions.add(expressionRenamer.rename(classExpression));
		}
		renamedClassExpressions = Sets.newHashSet(df.getOWLClass(IRI.create(PatternConstants.NS + "A_1")), PatternConstants.CLASS_SET);
		renamedAxiom = df.getOWLDisjointClassesAxiom(renamedClassExpressions);
	}

	@Override
	public void visit(OWLDataPropertyDomainAxiom axiom) {
		OWLDataPropertyExpression property = axiom.getProperty();
		property = expressionRenamer.rename(property);
		OWLClassExpression domain = axiom.getDomain();
		domain = expressionRenamer.rename(domain);
		renamedAxiom = df.getOWLDataPropertyDomainAxiom(property, domain);
	}

	@Override
	public void visit(OWLObjectPropertyDomainAxiom axiom) {
		OWLObjectPropertyExpression property = axiom.getProperty();
		property = expressionRenamer.rename(property);
		OWLClassExpression domain = axiom.getDomain();
		domain = expressionRenamer.rename(domain);
		renamedAxiom = df.getOWLObjectPropertyDomainAxiom(property, domain);
	}

	@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		Set<OWLObjectPropertyExpression> properties = axiom.getProperties();
		Set<OWLObjectPropertyExpression> renamedProperties = new HashSet<>();
		for (OWLObjectPropertyExpression property : properties) {
			renamedProperties.add(expressionRenamer.rename(property));
		}
		renamedAxiom = df.getOWLEquivalentObjectPropertiesAxiom(renamedProperties);
	}

	@Override
	public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		OWLDataPropertyExpression property = axiom.getProperty();
		property = expressionRenamer.rename(property);
		OWLIndividual subject = axiom.getSubject();
		subject = expressionRenamer.rename(subject);
		OWLLiteral object = axiom.getObject();
		object = expressionRenamer.rename(object);
		renamedAxiom = df.getOWLNegativeDataPropertyAssertionAxiom(property, subject, object);
	}

	@Override
	public void visit(OWLDifferentIndividualsAxiom axiom) {
		Set<OWLIndividual> renamedIndividuals = new HashSet<>();
		if(normalizeABoxAxioms){
			renamedIndividuals.add(df.getOWLNamedIndividual(IRI.create("http://dl-learner.org/pattern/a")));
			renamedIndividuals.add(df.getOWLNamedIndividual(IRI.create("http://dl-learner.org/pattern/b")));
		} else {
			for(OWLIndividual ind : axiom.getIndividuals()){
				renamedIndividuals.add(expressionRenamer.rename(ind));
			}
		}
		renamedAxiom = df.getOWLDifferentIndividualsAxiom(renamedIndividuals);
	}

	@Override
	public void visit(OWLDisjointDataPropertiesAxiom axiom) {
		Set<OWLDataPropertyExpression> properties = axiom.getProperties();
		Set<OWLDataPropertyExpression> renamedProperties = new HashSet<>();
		for (OWLDataPropertyExpression property : properties) {
			renamedProperties.add(expressionRenamer.rename(property));
		}
		renamedAxiom = df.getOWLDisjointDataPropertiesAxiom(renamedProperties);
	}

	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
		Set<OWLObjectPropertyExpression> properties = axiom.getProperties();
		Set<OWLObjectPropertyExpression> renamedProperties = new HashSet<>();
		for (OWLObjectPropertyExpression property : properties) {
			renamedProperties.add(expressionRenamer.rename(property));
		}
		renamedAxiom = df.getOWLDisjointObjectPropertiesAxiom(renamedProperties);
	}

	@Override
	public void visit(OWLObjectPropertyRangeAxiom axiom) {
		OWLObjectPropertyExpression property = axiom.getProperty();
		property = expressionRenamer.rename(property);
		OWLClassExpression range = axiom.getRange();
		range = expressionRenamer.rename(range);
		renamedAxiom = df.getOWLObjectPropertyDomainAxiom(property, range);
	}

	@Override
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		OWLObjectPropertyExpression property = axiom.getProperty();
		property = expressionRenamer.rename(property);
		OWLIndividual subject = axiom.getSubject();
		subject = expressionRenamer.rename(subject);
		OWLIndividual object = axiom.getObject();
		object = expressionRenamer.rename(object);
		renamedAxiom = df.getOWLObjectPropertyAssertionAxiom(property, subject, object);
	}

	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
		OWLObjectPropertyExpression property = axiom.getProperty();
		property = expressionRenamer.rename(property);
		renamedAxiom = df.getOWLFunctionalObjectPropertyAxiom(property);
	}

	@Override
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {
		OWLObjectPropertyExpression subProperty = axiom.getSubProperty();
		subProperty = expressionRenamer.rename(subProperty);
		OWLObjectPropertyExpression superProperty = axiom.getSuperProperty();
		superProperty = expressionRenamer.rename(superProperty);
		renamedAxiom = df.getOWLSubObjectPropertyOfAxiom(subProperty, superProperty);
	}

	@Override
	public void visit(OWLDisjointUnionAxiom axiom) {
		OWLClass cls = axiom.getOWLClass();
		cls = expressionRenamer.rename(cls).asOWLClass();
		Set<OWLClassExpression> classExpressions = axiom.getClassExpressions();
		Set<OWLClassExpression> renamedClassExpressions = new HashSet<>();
		for (OWLClassExpression classExpression : classExpressions) {
			renamedClassExpressions.add(expressionRenamer.rename(classExpression));
		}
		renamedAxiom = df.getOWLDisjointUnionAxiom(cls, renamedClassExpressions);
	}

	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
		OWLObjectPropertyExpression property = axiom.getProperty();
		property = expressionRenamer.rename(property);
		renamedAxiom = df.getOWLSymmetricObjectPropertyAxiom(property);
	}

	@Override
	public void visit(OWLDataPropertyRangeAxiom axiom) {
		OWLDataPropertyExpression property = axiom.getProperty();
		property = expressionRenamer.rename(property);
		OWLDataRange range = axiom.getRange();
		range = expressionRenamer.rename(range);
		renamedAxiom = df.getOWLDataPropertyRangeAxiom(property, range);
	}

	@Override
	public void visit(OWLFunctionalDataPropertyAxiom axiom) {
		OWLDataPropertyExpression property = axiom.getProperty();
		property = expressionRenamer.rename(property);
		renamedAxiom = df.getOWLFunctionalDataPropertyAxiom(property);
	}

	@Override
	public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
		Set<OWLDataPropertyExpression> properties = axiom.getProperties();
		Set<OWLDataPropertyExpression> renamedProperties = new HashSet<>();
		for (OWLDataPropertyExpression property : properties) {
			renamedProperties.add(expressionRenamer.rename(property));
		}
		renamedAxiom = df.getOWLEquivalentDataPropertiesAxiom(renamedProperties);
	}

	@Override
	public void visit(OWLClassAssertionAxiom axiom) {
		OWLClassExpression classExpression = axiom.getClassExpression();
		classExpression = expressionRenamer.rename(classExpression);
		OWLIndividual individual = axiom.getIndividual();
		individual = expressionRenamer.rename(individual);
		renamedAxiom = df.getOWLClassAssertionAxiom(classExpression, individual);
	}

	@Override
	public void visit(OWLEquivalentClassesAxiom axiom) {
		List<OWLClassExpression> classExpressions = axiom.getClassExpressionsAsList();
		List<OWLClassExpression> renamedClassExpressions = new ArrayList<>();
		for (OWLClassExpression expr : classExpressions) {
			renamedClassExpressions.add(expressionRenamer.rename(expr));
		}
		renamedAxiom = df.getOWLEquivalentClassesAxiom(new TreeSet<>(renamedClassExpressions));
	}

	@Override
	public void visit(OWLDataPropertyAssertionAxiom axiom) {
		OWLDataPropertyExpression property = axiom.getProperty();
		property = expressionRenamer.rename(property);
		OWLIndividual subject = axiom.getSubject();
		subject = expressionRenamer.rename(subject);
		OWLLiteral object = axiom.getObject();
		object = expressionRenamer.rename(object);
		renamedAxiom = df.getOWLDataPropertyAssertionAxiom(property, subject, object);
	}

	@Override
	public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
		OWLObjectPropertyExpression property = axiom.getProperty();
		property = expressionRenamer.rename(property);
		renamedAxiom = df.getOWLTransitiveObjectPropertyAxiom(property);
	}

	@Override
	public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		OWLObjectPropertyExpression property = axiom.getProperty();
		property = expressionRenamer.rename(property);
		renamedAxiom = df.getOWLIrreflexiveObjectPropertyAxiom(property);
	}

	@Override
	public void visit(OWLSubDataPropertyOfAxiom axiom) {
		OWLDataPropertyExpression subProperty = axiom.getSubProperty();
		subProperty = expressionRenamer.rename(subProperty);
		OWLDataPropertyExpression superProperty = axiom.getSuperProperty();
		superProperty = expressionRenamer.rename(superProperty);
		renamedAxiom = df.getOWLSubDataPropertyOfAxiom(subProperty, superProperty);
	}

	@Override
	public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
		OWLObjectPropertyExpression property = axiom.getProperty();
		property = expressionRenamer.rename(property);
		renamedAxiom = df.getOWLInverseFunctionalObjectPropertyAxiom(property);
	}

	@Override
	public void visit(OWLSameIndividualAxiom axiom) {
		Set<OWLIndividual> renamedIndividuals = new HashSet<>();
		if(normalizeABoxAxioms){
			renamedIndividuals.add(df.getOWLNamedIndividual(IRI.create("http://dl-learner.org/pattern/a")));
			renamedIndividuals.add(df.getOWLNamedIndividual(IRI.create("http://dl-learner.org/pattern/b")));
		} else {
			for(OWLIndividual ind : axiom.getIndividuals()){
				renamedIndividuals.add(expressionRenamer.rename(ind));
			}
		}
		renamedAxiom = df.getOWLSameIndividualAxiom(renamedIndividuals);
	}

	@Override
	public void visit(OWLSubPropertyChainOfAxiom axiom) {
		List<OWLObjectPropertyExpression> renamedSubPropertyChain = new ArrayList<>();
		for (OWLObjectPropertyExpression owlObjectPropertyExpression : axiom.getPropertyChain()) {
			renamedSubPropertyChain.add(expressionRenamer.rename(owlObjectPropertyExpression));
		}
		OWLObjectPropertyExpression superProperty = axiom.getSuperProperty();
		superProperty = expressionRenamer.rename(superProperty);
		renamedAxiom = df.getOWLSubPropertyChainOfAxiom(renamedSubPropertyChain, superProperty);
	}

	@Override
	public void visit(OWLInverseObjectPropertiesAxiom axiom) {
		OWLObjectPropertyExpression firstProperty = axiom.getFirstProperty();
		firstProperty = expressionRenamer.rename(firstProperty);
		OWLObjectPropertyExpression secondProperty = axiom.getSecondProperty();
		secondProperty = expressionRenamer.rename(secondProperty);
		renamedAxiom = df.getOWLInverseObjectPropertiesAxiom(firstProperty, secondProperty);
	}

	@Override
	public void visit(OWLHasKeyAxiom axiom) {
		OWLClassExpression classExpression = axiom.getClassExpression();
		classExpression = expressionRenamer.rename(classExpression);
		Set<OWLPropertyExpression> propertyExpressions = axiom.getPropertyExpressions();
		Set<OWLPropertyExpression> renamedPropertyExpressions = new HashSet<>();
		for (OWLPropertyExpression owlPropertyExpression : propertyExpressions) {
			renamedPropertyExpressions.add(expressionRenamer.rename(owlPropertyExpression));
		}
		renamedAxiom = df.getOWLHasKeyAxiom(classExpression, renamedPropertyExpressions);
	}

	@Override
	public void visit(OWLDatatypeDefinitionAxiom axiom) {
	}

	@Override
	public void visit(SWRLRule axiom) {
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
}
