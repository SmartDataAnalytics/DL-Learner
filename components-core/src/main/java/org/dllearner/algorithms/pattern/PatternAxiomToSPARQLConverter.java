package org.dllearner.algorithms.pattern;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
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
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
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

import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;

public class PatternAxiomToSPARQLConverter implements OWLAxiomVisitor {

	private String root = "?x";
	private PatternExpressionToSPARQLConverter expressionConverter;
	private String sparql;

	public PatternAxiomToSPARQLConverter() {
	}

	public Query convert(OWLAxiom axiom) {
		expressionConverter = new PatternExpressionToSPARQLConverter();
		
		sparql = "SELECT " + root + " WHERE {\n";
		axiom.accept(this);
		sparql += "}";
		
		return QueryFactory.create(sparql, Syntax.syntaxARQ);
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
		sparql += expressionConverter.convert(root, subClass);
		OWLClassExpression superClass = axiom.getSuperClass();
		sparql += expressionConverter.convert(root, superClass);
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
	public void visit(SWRLRule axiom) {
	}

	public static void main(String[] args) throws Exception {
		ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
		PatternAxiomToSPARQLConverter converter = new PatternAxiomToSPARQLConverter();
		
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = man.getOWLDataFactory();
		PrefixManager pm = new DefaultPrefixManager("http://examples.org/ontology#");
		
		OWLClass clsA = df.getOWLClass("A", pm);
		OWLClass clsB = df.getOWLClass("B", pm);
		OWLClass clsC = df.getOWLClass("C", pm);
		
		OWLObjectProperty propR = df.getOWLObjectProperty("r", pm);
		OWLObjectProperty propS = df.getOWLObjectProperty("s", pm);
		
		OWLIndividual indA = df.getOWLNamedIndividual("a", pm);
		OWLIndividual  indB = df.getOWLNamedIndividual("b", pm);
		
		OWLAxiom axiom = df.getOWLSubClassOfAxiom(
				clsA, 
				clsB);
		Query query = converter.convert(axiom);
		System.out.println(axiom + "\n" + query);
		
		axiom = df.getOWLSubClassOfAxiom(
				clsA,
				df.getOWLObjectSomeValuesFrom(propR, clsB));
		query = converter.convert(axiom);
		System.out.println(axiom + "\n" + query);
		
		axiom = df.getOWLSubClassOfAxiom(
				clsA,
				df.getOWLObjectIntersectionOf(
						df.getOWLObjectSomeValuesFrom(propR, clsB),
						clsB));
		query = converter.convert(axiom);
		System.out.println(axiom + "\n" + query);
		
		axiom = df.getOWLSubClassOfAxiom(
				clsA,
				df.getOWLObjectAllValuesFrom(propR, clsB));
		query = converter.convert(axiom);
		System.out.println(axiom + "\n" + query);
		
	}

}
