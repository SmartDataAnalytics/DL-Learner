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

import static org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor.getOWLClassExpression;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.core.owl.AsymmetricObjectPropertyAxiom;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.AxiomVisitor;
import org.dllearner.core.owl.BooleanDatatypePropertyAssertion;
import org.dllearner.core.owl.ClassAssertionAxiom;
import org.dllearner.core.owl.DataRange;
import org.dllearner.core.owl.Datatype;
import org.dllearner.core.owl.DatatypePropertyDomainAxiom;
import org.dllearner.core.owl.DatatypePropertyRangeAxiom;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.DifferentIndividualsAxiom;
import org.dllearner.core.owl.DisjointClassesAxiom;
import org.dllearner.core.owl.DisjointDatatypePropertyAxiom;
import org.dllearner.core.owl.DisjointObjectPropertyAxiom;
import org.dllearner.core.owl.DoubleDatatypePropertyAssertion;
import org.dllearner.core.owl.EquivalentClassesAxiom;
import org.dllearner.core.owl.EquivalentDatatypePropertiesAxiom;
import org.dllearner.core.owl.EquivalentObjectPropertiesAxiom;
import org.dllearner.core.owl.FunctionalDatatypePropertyAxiom;
import org.dllearner.core.owl.FunctionalObjectPropertyAxiom;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.InverseFunctionalObjectPropertyAxiom;
import org.dllearner.core.owl.InverseObjectPropertyAxiom;
import org.dllearner.core.owl.IrreflexiveObjectPropertyAxiom;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyAssertion;
import org.dllearner.core.owl.ObjectPropertyDomainAxiom;
import org.dllearner.core.owl.ObjectPropertyRangeAxiom;
import org.dllearner.core.owl.ReflexiveObjectPropertyAxiom;
import org.dllearner.core.owl.StringDatatypePropertyAssertion;
import org.dllearner.core.owl.SubClassAxiom;
import org.dllearner.core.owl.SubDatatypePropertyAxiom;
import org.dllearner.core.owl.SubObjectPropertyAxiom;
import org.dllearner.core.owl.SymmetricObjectPropertyAxiom;
import org.dllearner.core.owl.TransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * A converter from DL-Learner axioms to OWL API axioms based on the visitor
 * pattern.
 * 
 * TODO: Investigate whether OWLOntologyManager and OWLOntology should be
 * removed as parameters. It would be natural to have a DL-Learner KB as input
 * and an OWL API OWLOntology as output.
 * 
 * @author Jens Lehmann
 * 
 */
public class OWLAPIAxiomConvertVisitor implements AxiomVisitor {

	OWLDataFactory factory;
	private OWLOntology ontology;
	private OWLOntologyManager manager;
	private OWLAxiom lastAxiom;

	/**
	 * Creates a default visitor with ontology URI "http://example.com"
	 * and default ontology manager.
	 */
	public OWLAPIAxiomConvertVisitor() {
		manager = OWLManager.createOWLOntologyManager();
		IRI ontologyIRI = IRI.create("http://example.com");
		try {
			ontology = manager.createOntology(ontologyIRI);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}		
		factory = manager.getOWLDataFactory();
	}
	
	public OWLAPIAxiomConvertVisitor(OWLOntologyManager manager, OWLOntology ontology) {
		this.manager = manager;
		this.ontology = ontology;
		factory = manager.getOWLDataFactory();
	}
	
	public static void fillOWLOntology(OWLOntologyManager manager, OWLOntology ontology, KB kb) {
		OWLAPIAxiomConvertVisitor converter = new OWLAPIAxiomConvertVisitor(manager, ontology);
		for(Axiom axiom : kb.getTbox())
			axiom.accept(converter);
		for(Axiom axiom : kb.getRbox())
			axiom.accept(converter);
		for(Axiom axiom : kb.getAbox())
			axiom.accept(converter);		
	}
	
	public static OWLAxiom convertAxiom(Axiom axiom) {
		OWLAPIAxiomConvertVisitor converter = new OWLAPIAxiomConvertVisitor();
		axiom.accept(converter);
		return converter.lastAxiom;
	}
	
	// convencience function for adding an axiom to the ontology
	private void addAxiom(OWLAxiom axiom) {
		AddAxiom addAxiom = new AddAxiom(ontology, axiom);
		try {
			manager.applyChange(addAxiom);
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		}
		lastAxiom = axiom;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.dl.AxiomVisitor#visit(org.dllearner.core.dl.ObjectPropertyAssertion)
	 */
	public void visit(ObjectPropertyAssertion axiom) {
		OWLObjectProperty role = factory.getOWLObjectProperty(IRI
				.create(((ObjectPropertyAssertion) axiom).getRole().getName()));
		OWLIndividual i1 = factory.getOWLNamedIndividual(IRI.create(((ObjectPropertyAssertion) axiom)
				.getIndividual1().getName()));
		OWLIndividual i2 = factory.getOWLNamedIndividual(IRI.create(((ObjectPropertyAssertion) axiom)
				.getIndividual2().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLObjectPropertyAssertionAxiom(role, i1, i2);
		addAxiom(axiomOWLAPI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.owl.AssertionalAxiomVisitor#visit(org.dllearner.core.owl.ClassAssertionAxiom)
	 */
	public void visit(ClassAssertionAxiom axiom) {
		OWLClassExpression d = getOWLClassExpression(axiom.getConcept());
		OWLIndividual i = factory.getOWLNamedIndividual(IRI.create(((ClassAssertionAxiom) axiom)
				.getIndividual().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLClassAssertionAxiom(d, i);
		addAxiom(axiomOWLAPI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.owl.AssertionalAxiomVisitor#visit(org.dllearner.core.owl.DoubleDatatypePropertyAssertion)
	 */
	public void visit(DoubleDatatypePropertyAssertion axiom) {
		OWLIndividual i = factory.getOWLNamedIndividual(IRI.create(axiom.getIndividual().getName()));
		OWLDataProperty dp = factory.getOWLDataProperty(IRI.create(axiom.getDatatypeProperty().getName()));
		double value = axiom.getValue();
		OWLLiteral valueConstant = factory.getOWLLiteral(value);
		OWLAxiom axiomOWLAPI = factory.getOWLDataPropertyAssertionAxiom(dp, i, valueConstant);
		addAxiom(axiomOWLAPI);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.AssertionalAxiomVisitor#visit(org.dllearner.core.owl.BooleanDatatypePropertyAssertion)
	 */
	public void visit(BooleanDatatypePropertyAssertion axiom) {
		OWLIndividual i = factory.getOWLNamedIndividual(IRI.create(axiom.getIndividual().getName()));
		OWLDataProperty dp = factory.getOWLDataProperty(IRI.create(axiom.getDatatypeProperty().getName()));
		boolean value = axiom.getValue();
		OWLLiteral valueConstant = factory.getOWLLiteral(value);
		OWLAxiom axiomOWLAPI = factory.getOWLDataPropertyAssertionAxiom(dp, i, valueConstant);
		addAxiom(axiomOWLAPI);		
	}	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.owl.PropertyAxiomVisitor#visit(org.dllearner.core.owl.FunctionalObjectPropertyAxiom)
	 */
	public void visit(FunctionalObjectPropertyAxiom axiom) {
		OWLObjectProperty role = factory.getOWLObjectProperty(
				IRI.create(((FunctionalObjectPropertyAxiom) axiom).getRole().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLFunctionalObjectPropertyAxiom(role);
		addAxiom(axiomOWLAPI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.owl.PropertyAxiomVisitor#visit(org.dllearner.core.owl.InverseObjectPropertyAxiom)
	 */
	public void visit(InverseObjectPropertyAxiom axiom) {
		OWLObjectProperty role = factory.getOWLObjectProperty(
				IRI.create(((InverseObjectPropertyAxiom) axiom).getRole().getName()));
		OWLObjectProperty inverseRole = factory.getOWLObjectProperty(
				IRI.create(((InverseObjectPropertyAxiom) axiom).getInverseRole().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLInverseObjectPropertiesAxiom(role, inverseRole);
		addAxiom(axiomOWLAPI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.owl.PropertyAxiomVisitor#visit(org.dllearner.core.owl.SymmetricObjectPropertyAxiom)
	 */
	public void visit(SymmetricObjectPropertyAxiom axiom) {
		OWLObjectProperty role = factory.getOWLObjectProperty(
				IRI.create(((SymmetricObjectPropertyAxiom) axiom).getRole().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLSymmetricObjectPropertyAxiom(role);
		addAxiom(axiomOWLAPI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.owl.PropertyAxiomVisitor#visit(org.dllearner.core.owl.TransitiveObjectPropertyAxiom)
	 */
	public void visit(TransitiveObjectPropertyAxiom axiom) {
		OWLObjectProperty role = factory.getOWLObjectProperty(
				IRI.create(axiom.getRole().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLTransitiveObjectPropertyAxiom(role);
		addAxiom(axiomOWLAPI);
	}
	
	@Override
	public void visit(ReflexiveObjectPropertyAxiom axiom) {
		OWLObjectProperty role = factory.getOWLObjectProperty(
				IRI.create(axiom.getRole().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLFunctionalObjectPropertyAxiom(role);
		addAxiom(axiomOWLAPI);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.PropertyAxiomVisitor#visit(org.dllearner.core.owl.SubObjectPropertyAxiom)
	 */
	public void visit(SubObjectPropertyAxiom axiom) {
		OWLObjectProperty role = factory.getOWLObjectProperty(
				IRI.create(((SubObjectPropertyAxiom) axiom).getRole().getName()));
		OWLObjectProperty subRole = factory.getOWLObjectProperty(
				IRI.create(((SubObjectPropertyAxiom) axiom).getSubRole().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLSubObjectPropertyOfAxiom(subRole, role);
		addAxiom(axiomOWLAPI);	
	}	
	
	@Override
	public void visit(EquivalentObjectPropertiesAxiom axiom) {
		Set<OWLObjectProperty> properties = new HashSet<OWLObjectProperty>();
		for(ObjectProperty prop : axiom.getEquivalentProperties()){
			properties.add(factory.getOWLObjectProperty(IRI.create(prop.getName())));
		}
		OWLAxiom axiomOWLAPI = factory.getOWLEquivalentObjectPropertiesAxiom(properties);
		addAxiom(axiomOWLAPI);	
		
	}

	@Override
	public void visit(EquivalentDatatypePropertiesAxiom axiom) {
		OWLDataProperty role = factory.getOWLDataProperty(
				IRI.create(axiom.getRole().getName()));
		OWLDataProperty equivRole = factory.getOWLDataProperty(
				IRI.create(axiom.getEquivalentRole().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLEquivalentDataPropertiesAxiom(equivRole, role);
		addAxiom(axiomOWLAPI);
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.owl.TerminologicalAxiomVisitor#visit(org.dllearner.core.owl.EquivalentClassesAxiom)
	 */
	public void visit(EquivalentClassesAxiom axiom) {
		OWLClassExpression d1 = getOWLClassExpression(axiom.getConcept1());
		OWLClassExpression d2 = getOWLClassExpression(axiom.getConcept2());
		Set<OWLClassExpression> ds = new HashSet<OWLClassExpression>();
		ds.add(d1);
		ds.add(d2);
		OWLAxiom axiomOWLAPI = factory.getOWLEquivalentClassesAxiom(ds);
		addAxiom(axiomOWLAPI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.owl.TerminologicalAxiomVisitor#visit(org.dllearner.core.owl.SubClassAxiom)
	 */
	public void visit(SubClassAxiom axiom) {
		OWLClassExpression d1 = getOWLClassExpression(axiom.getSubConcept());
		OWLClassExpression d2 = getOWLClassExpression(axiom.getSuperConcept());
		Set<OWLClassExpression> ds = new HashSet<OWLClassExpression>();
		ds.add(d1);
		ds.add(d2);
		OWLAxiom axiomOWLAPI = factory.getOWLSubClassOfAxiom(d1,d2);
		addAxiom(axiomOWLAPI);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.PropertyAxiomVisitor#visit(org.dllearner.core.owl.DatatypePropertyDomainAxiom)
	 */
	public void visit(DatatypePropertyDomainAxiom datatypePropertyDomainAxiom) {
		OWLClassExpression d = getOWLClassExpression(datatypePropertyDomainAxiom.getDomain());
		OWLDataProperty dp = factory.getOWLDataProperty(IRI.create(datatypePropertyDomainAxiom.getProperty().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLDataPropertyDomainAxiom(dp, d);
		addAxiom(axiomOWLAPI);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.PropertyAxiomVisitor#visit(org.dllearner.core.owl.ObjectPropertyDomainAxiom)
	 */
	public void visit(ObjectPropertyDomainAxiom objectPropertyDomainAxiom) {
		OWLClassExpression d = getOWLClassExpression(objectPropertyDomainAxiom.getDomain());
		OWLObjectProperty op = factory.getOWLObjectProperty(IRI.create(objectPropertyDomainAxiom.getProperty().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLObjectPropertyDomainAxiom(op, d);
		addAxiom(axiomOWLAPI);		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.PropertyAxiomVisitor#visit(org.dllearner.core.owl.DatatypePropertyRangeAxiom)
	 */
	public void visit(DatatypePropertyRangeAxiom axiom) {
		DataRange dr = axiom.getRange();
		Datatype dt = (Datatype) dr;
		OWLDatatype odt = factory.getOWLDatatype(IRI.create(dt.getURI()));
		OWLDataProperty dp = factory.getOWLDataProperty(IRI.create(axiom.getProperty().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLDataPropertyRangeAxiom(dp, odt);
		addAxiom(axiomOWLAPI);		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.PropertyAxiomVisitor#visit(org.dllearner.core.owl.ObjectPropertyRangeAxiom)
	 */
	public void visit(ObjectPropertyRangeAxiom axiom) {
		OWLClassExpression d = getOWLClassExpression(axiom.getRange());
		OWLObjectProperty op = factory.getOWLObjectProperty(IRI.create(axiom.getProperty().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLObjectPropertyRangeAxiom(op, d);
		addAxiom(axiomOWLAPI);		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.AssertionalAxiomVisitor#visit(org.dllearner.core.owl.DifferentIndividualsAxiom)
	 */
	public void visit(DifferentIndividualsAxiom axiom) {
		Set<Individual> individuals = axiom.getIndividuals();
		Set<OWLIndividual> owlAPIIndividuals = new HashSet<OWLIndividual>();
		for(Individual individual : individuals)
			owlAPIIndividuals.add(factory.getOWLNamedIndividual(IRI.create(individual.getName())));
		OWLAxiom axiomOWLAPI = factory.getOWLDifferentIndividualsAxiom(owlAPIIndividuals);
		addAxiom(axiomOWLAPI);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.TerminologicalAxiomVisitor#visit(org.dllearner.core.owl.DisjointClassesAxiom)
	 */
	public void visit(DisjointClassesAxiom axiom) {
		Collection<Description> descriptions = axiom.getDescriptions();
		Set<OWLClassExpression> owlAPIDescriptions = new HashSet<OWLClassExpression>();
		for(Description description : descriptions)
			owlAPIDescriptions.add(getOWLClassExpression(description));
		OWLAxiom axiomOWLAPI = factory.getOWLDisjointClassesAxiom(owlAPIDescriptions);
		addAxiom(axiomOWLAPI);		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.AssertionalAxiomVisitor#visit(org.dllearner.core.owl.StringDatatypePropertyAssertion)
	 */
	@Override
	public void visit(StringDatatypePropertyAssertion axiom) {
		//throw new UnsupportedOperationException("String datatype conversion not implemented");
		OWLIndividual i = factory.getOWLNamedIndividual(IRI.create(axiom.getIndividual().getName()));
		OWLDataProperty dp = factory.getOWLDataProperty(IRI.create(axiom.getDatatypeProperty().getName()));
		String value = axiom.getValue();
		OWLLiteral valueConstant = factory.getOWLLiteral(value);
		OWLAxiom axiomOWLAPI = factory.getOWLDataPropertyAssertionAxiom(dp, i, valueConstant);
		addAxiom(axiomOWLAPI);
	}

	@Override
	public void visit(FunctionalDatatypePropertyAxiom axiom) {
		OWLDataProperty role = factory.getOWLDataProperty(
				IRI.create(axiom.getRole().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLFunctionalDataPropertyAxiom(role);
		addAxiom(axiomOWLAPI);
		
	}

	@Override
	public void visit(SubDatatypePropertyAxiom axiom) {
		OWLDataProperty role = factory.getOWLDataProperty(
				IRI.create(axiom.getRole().getName()));
		OWLDataProperty subRole = factory.getOWLDataProperty(
				IRI.create(axiom.getSubRole().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLSubDataPropertyOfAxiom(subRole, role);
		addAxiom(axiomOWLAPI);
		
	}

	@Override
	public void visit(DisjointObjectPropertyAxiom axiom) {
		OWLObjectProperty role = factory.getOWLObjectProperty(
				IRI.create(axiom.getRole().getName()));
		OWLObjectProperty disjointRole = factory.getOWLObjectProperty(
				IRI.create(axiom.getDisjointRole().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLDisjointObjectPropertiesAxiom(role, disjointRole);
		addAxiom(axiomOWLAPI);
		
	}

	@Override
	public void visit(DisjointDatatypePropertyAxiom axiom) {
		OWLDataProperty role = factory.getOWLDataProperty(
				IRI.create(axiom.getRole().getName()));
		OWLDataProperty disjointRole = factory.getOWLDataProperty(
				IRI.create(axiom.getDisjointRole().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLDisjointDataPropertiesAxiom(role, disjointRole);
		addAxiom(axiomOWLAPI);
		
	}

	@Override
	public void visit(InverseFunctionalObjectPropertyAxiom axiom) {
		OWLObjectProperty role = factory.getOWLObjectProperty(
				IRI.create(axiom.getRole().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLInverseFunctionalObjectPropertyAxiom(role);
		addAxiom(axiomOWLAPI);
		
	}

	@Override
	public void visit(AsymmetricObjectPropertyAxiom axiom) {
		OWLObjectProperty role = factory.getOWLObjectProperty(
				IRI.create(axiom.getRole().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLAsymmetricObjectPropertyAxiom(role);
		addAxiom(axiomOWLAPI);
		
	}

	@Override
	public void visit(IrreflexiveObjectPropertyAxiom axiom) {
		OWLObjectProperty role = factory.getOWLObjectProperty(
				IRI.create(axiom.getRole().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLIrreflexiveObjectPropertyAxiom(role);
		addAxiom(axiomOWLAPI);
		
	}

	

	

}
