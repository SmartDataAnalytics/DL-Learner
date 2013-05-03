package org.dllearner.algorithms.pattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
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
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;

public class OWLAxiomRenamer implements OWLAxiomVisitor {

	private OWLDataFactory df;
	private OWLClassExpressionRenamer expressionRenamer;
	private OWLAxiom renamedAxiom;
	
	public OWLAxiomRenamer(OWLDataFactory df) {
		this.df = df;
	}
	
	public OWLAxiom rename(OWLAxiom axiom){
		Map<OWLEntity, OWLEntity> renaming = new HashMap<OWLEntity, OWLEntity>();
		expressionRenamer = new OWLClassExpressionRenamer(df, renaming);
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
		Set<OWLClassExpression> renamedClassExpressions = new HashSet<OWLClassExpression>();
		for (OWLClassExpression classExpression : classExpressions) {
			renamedClassExpressions.add(expressionRenamer.rename(classExpression));
		}
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
		Set<OWLObjectPropertyExpression> renamedProperties = new HashSet<OWLObjectPropertyExpression>();
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
		Set<OWLIndividual> renamedIndividuals = new HashSet<OWLIndividual>();
		for(OWLIndividual ind : axiom.getIndividuals()){
			renamedIndividuals.add(expressionRenamer.rename(ind));
		}
		renamedAxiom = df.getOWLDifferentIndividualsAxiom(renamedIndividuals);
	}

	@Override
	public void visit(OWLDisjointDataPropertiesAxiom axiom) {
		Set<OWLDataPropertyExpression> properties = axiom.getProperties();
		Set<OWLDataPropertyExpression> renamedProperties = new HashSet<OWLDataPropertyExpression>();
		for (OWLDataPropertyExpression property : properties) {
			renamedProperties.add(expressionRenamer.rename(property));
		}
		renamedAxiom = df.getOWLDisjointDataPropertiesAxiom(renamedProperties);
	}

	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
		Set<OWLObjectPropertyExpression> properties = axiom.getProperties();
		Set<OWLObjectPropertyExpression> renamedProperties = new HashSet<OWLObjectPropertyExpression>();
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
		Set<OWLClassExpression> renamedClassExpressions = new HashSet<OWLClassExpression>();
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
		Set<OWLDataPropertyExpression> renamedProperties = new HashSet<OWLDataPropertyExpression>();
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
		List<OWLClassExpression> renamedClassExpressions = new ArrayList<OWLClassExpression>();
		for (OWLClassExpression expr : classExpressions) {
			renamedClassExpressions.add(expressionRenamer.rename(expr));
		}
		renamedAxiom = df.getOWLEquivalentClassesAxiom(new TreeSet<OWLClassExpression>(renamedClassExpressions));
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
		Set<OWLIndividual> renamedIndividuals = new HashSet<OWLIndividual>();
		for(OWLIndividual ind : axiom.getIndividuals()){
			renamedIndividuals.add(expressionRenamer.rename(ind));
		}
		renamedAxiom = df.getOWLSameIndividualAxiom(renamedIndividuals);
	}

	@Override
	public void visit(OWLSubPropertyChainOfAxiom axiom) {
		OWLObjectPropertyExpression superProperty = axiom.getSuperProperty();
		superProperty = expressionRenamer.rename(superProperty);
		List<OWLObjectPropertyExpression> subPropertyChain = axiom.getPropertyChain();
		List<OWLObjectPropertyExpression> renamedSubPropertyChain = axiom.getPropertyChain();
		for (OWLObjectPropertyExpression owlObjectPropertyExpression : subPropertyChain) {
			renamedSubPropertyChain.add(expressionRenamer.rename(owlObjectPropertyExpression));
		}
		renamedAxiom = df.getOWLSubPropertyChainOfAxiom(subPropertyChain, superProperty);
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
		Set<OWLPropertyExpression<?, ?>> propertyExpressions = axiom.getPropertyExpressions();
		Set<OWLPropertyExpression<?, ?>> renamedPropertyExpressions = new HashSet<OWLPropertyExpression<?,?>>();
		for (OWLPropertyExpression<?, ?> owlPropertyExpression : propertyExpressions) {
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
