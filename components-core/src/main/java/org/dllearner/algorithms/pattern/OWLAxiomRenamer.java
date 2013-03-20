package org.dllearner.algorithms.pattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
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
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
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
	private OWLClassExpressionRenamer renamer;
	private OWLAxiom renamedAxiom;
	
	public OWLAxiomRenamer(OWLDataFactory df) {
		this.df = df;
	}
	
	public OWLAxiom rename(OWLAxiom axiom){
		Map<OWLEntity, OWLEntity> renaming = new HashMap<OWLEntity, OWLEntity>();
		renamer = new OWLClassExpressionRenamer(df, renaming);
		axiom.accept(this);
		return renamedAxiom;
	}
	
	@Override
	public void visit(OWLSubClassOfAxiom axiom) {
		OWLClassExpression subClass = axiom.getSubClass();
		subClass = renamer.rename(subClass);
		OWLClassExpression superClass = axiom.getSuperClass();
		superClass = renamer.rename(superClass);
		renamedAxiom = df.getOWLSubClassOfAxiom(subClass, superClass);
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
		OWLDataPropertyExpression property = axiom.getProperty();
		property = renamer.rename(property);
		OWLClassExpression domain = axiom.getDomain();
		domain = renamer.rename(domain);
		renamedAxiom = df.getOWLDataPropertyDomainAxiom(property, domain);
	}

	@Override
	public void visit(OWLObjectPropertyDomainAxiom axiom) {
		OWLObjectPropertyExpression property = axiom.getProperty();
		property = renamer.rename(property);
		OWLClassExpression domain = axiom.getDomain();
		domain = renamer.rename(domain);
		renamedAxiom = df.getOWLObjectPropertyDomainAxiom(property, domain);
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
		OWLObjectPropertyExpression property = axiom.getProperty();
		property = renamer.rename(property);
		OWLClassExpression range = axiom.getRange();
		range = renamer.rename(range);
		renamedAxiom = df.getOWLObjectPropertyDomainAxiom(property, range);
	}

	@Override
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
	}

	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
		OWLObjectPropertyExpression property = axiom.getProperty();
		property = renamer.rename(property);
		renamedAxiom = df.getOWLFunctionalObjectPropertyAxiom(property);
	}

	@Override
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {
	}

	@Override
	public void visit(OWLDisjointUnionAxiom axiom) {
	}

	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
		OWLObjectPropertyExpression property = axiom.getProperty();
		property = renamer.rename(property);
		renamedAxiom = df.getOWLSymmetricObjectPropertyAxiom(property);
	}

	@Override
	public void visit(OWLDataPropertyRangeAxiom axiom) {
		OWLDataPropertyExpression property = axiom.getProperty();
		property = renamer.rename(property);
		OWLDataRange range = axiom.getRange();
		range = renamer.rename(range);
		renamedAxiom = df.getOWLDataPropertyRangeAxiom(property, range);
	}

	@Override
	public void visit(OWLFunctionalDataPropertyAxiom axiom) {
		OWLDataPropertyExpression property = axiom.getProperty();
		property = renamer.rename(property);
		renamedAxiom = df.getOWLFunctionalDataPropertyAxiom(property);
	}

	@Override
	public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
	}

	@Override
	public void visit(OWLClassAssertionAxiom axiom) {
		OWLClassExpression classExpression = axiom.getClassExpression();
		classExpression = renamer.rename(classExpression);
		OWLIndividual individual = axiom.getIndividual();
		individual = renamer.rename(individual);
		renamedAxiom = df.getOWLClassAssertionAxiom(classExpression, individual);
	}

	@Override
	public void visit(OWLEquivalentClassesAxiom axiom) {
		List<OWLClassExpression> classExpressions = axiom.getClassExpressionsAsList();
		List<OWLClassExpression> renamedClassExpressions = new ArrayList<OWLClassExpression>();
		for (OWLClassExpression expr : classExpressions) {
			renamedClassExpressions.add(renamer.rename(expr));
		}
		renamedAxiom = df.getOWLEquivalentClassesAxiom(new TreeSet<OWLClassExpression>(renamedClassExpressions));
	}

	@Override
	public void visit(OWLDataPropertyAssertionAxiom axiom) {
	}

	@Override
	public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
		OWLObjectPropertyExpression property = axiom.getProperty();
		property = renamer.rename(property);
		renamedAxiom = df.getOWLTransitiveObjectPropertyAxiom(property);
	}

	@Override
	public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		OWLObjectPropertyExpression property = axiom.getProperty();
		property = renamer.rename(property);
		renamedAxiom = df.getOWLIrreflexiveObjectPropertyAxiom(property);
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
	public void visit(SWRLRule axiom) {
	}
	
	

}
