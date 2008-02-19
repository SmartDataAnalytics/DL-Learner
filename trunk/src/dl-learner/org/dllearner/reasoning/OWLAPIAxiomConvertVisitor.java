/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
 *
 */
package org.dllearner.reasoning;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.AxiomVisitor;
import org.dllearner.core.owl.ClassAssertionAxiom;
import org.dllearner.core.owl.DatatypePropertyDomainAxiom;
import org.dllearner.core.owl.DatatypePropertyRangeAxiom;
import org.dllearner.core.owl.DoubleDatatypePropertyAssertion;
import org.dllearner.core.owl.EquivalentClassesAxiom;
import org.dllearner.core.owl.FunctionalObjectPropertyAxiom;
import org.dllearner.core.owl.InverseObjectPropertyAxiom;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.ObjectPropertyAssertion;
import org.dllearner.core.owl.ObjectPropertyDomainAxiom;
import org.dllearner.core.owl.ObjectPropertyRangeAxiom;
import org.dllearner.core.owl.SubClassAxiom;
import org.dllearner.core.owl.SubObjectPropertyAxiom;
import org.dllearner.core.owl.SymmetricObjectPropertyAxiom;
import org.dllearner.core.owl.TransitiveObjectPropertyAxiom;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLTypedConstant;
import org.semanticweb.owl.vocab.XSDVocabulary;

// static import for easy access to the description converter
import static org.dllearner.reasoning.OWLAPIDescriptionConvertVisitor.getOWLDescription;

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

	public OWLAPIAxiomConvertVisitor(OWLOntologyManager manager, OWLOntology ontology, KB kb) {
		this.manager = manager;
		this.ontology = ontology;
		factory = manager.getOWLDataFactory();
	}
	
	public static void fillOWLOntology(OWLOntologyManager manager, OWLOntology ontology, KB kb) {
		OWLAPIAxiomConvertVisitor converter = new OWLAPIAxiomConvertVisitor(manager, ontology, kb);
		for(Axiom axiom : kb.getTbox())
			axiom.accept(converter);
		for(Axiom axiom : kb.getRbox())
			axiom.accept(converter);
		for(Axiom axiom : kb.getAbox())
			axiom.accept(converter);		
	}
	
	// convencience function for adding an axiom to the ontology
	private void addAxiom(OWLAxiom axiom) {
		AddAxiom addAxiom = new AddAxiom(ontology, axiom);
		try {
			manager.applyChange(addAxiom);
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.dl.AxiomVisitor#visit(org.dllearner.core.dl.ObjectPropertyAssertion)
	 */
	public void visit(ObjectPropertyAssertion axiom) {
		OWLObjectProperty role = factory.getOWLObjectProperty(URI
				.create(((ObjectPropertyAssertion) axiom).getRole().getName()));
		OWLIndividual i1 = factory.getOWLIndividual(URI.create(((ObjectPropertyAssertion) axiom)
				.getIndividual1().getName()));
		OWLIndividual i2 = factory.getOWLIndividual(URI.create(((ObjectPropertyAssertion) axiom)
				.getIndividual2().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLObjectPropertyAssertionAxiom(i1, role, i2);
		addAxiom(axiomOWLAPI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.owl.AssertionalAxiomVisitor#visit(org.dllearner.core.owl.ClassAssertionAxiom)
	 */
	public void visit(ClassAssertionAxiom axiom) {
		OWLDescription d = getOWLDescription(axiom.getConcept());
		OWLIndividual i = factory.getOWLIndividual(URI.create(((ClassAssertionAxiom) axiom)
				.getIndividual().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLClassAssertionAxiom(i, d);
		addAxiom(axiomOWLAPI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.owl.AssertionalAxiomVisitor#visit(org.dllearner.core.owl.DoubleDatatypePropertyAssertion)
	 */
	public void visit(DoubleDatatypePropertyAssertion axiom) {
		OWLIndividual i = factory.getOWLIndividual(URI.create(axiom.getIndividual().getName()));
		OWLDataProperty dp = factory.getOWLDataProperty(URI.create(axiom.getDatatypeProperty().getName()));
		Double value = axiom.getValue();
		OWLDataType doubleType = factory.getOWLDataType(XSDVocabulary.DOUBLE.getURI());
		OWLTypedConstant valueConstant = factory.getOWLTypedConstant(value.toString(), doubleType);
		OWLAxiom axiomOWLAPI = factory.getOWLDataPropertyAssertionAxiom(i, dp, valueConstant);
		addAxiom(axiomOWLAPI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.owl.PropertyAxiomVisitor#visit(org.dllearner.core.owl.FunctionalObjectPropertyAxiom)
	 */
	public void visit(FunctionalObjectPropertyAxiom axiom) {
		OWLObjectProperty role = factory.getOWLObjectProperty(
				URI.create(((FunctionalObjectPropertyAxiom) axiom).getRole().getName()));
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
				URI.create(((InverseObjectPropertyAxiom) axiom).getRole().getName()));
		OWLObjectProperty inverseRole = factory.getOWLObjectProperty(
				URI.create(((InverseObjectPropertyAxiom) axiom).getInverseRole().getName()));
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
				URI.create(((SymmetricObjectPropertyAxiom) axiom).getRole().getName()));
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
				URI.create(axiom.getRole().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLTransitiveObjectPropertyAxiom(role);
		addAxiom(axiomOWLAPI);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.PropertyAxiomVisitor#visit(org.dllearner.core.owl.SubObjectPropertyAxiom)
	 */
	public void visit(SubObjectPropertyAxiom axiom) {
		OWLObjectProperty role = factory.getOWLObjectProperty(
				URI.create(((SubObjectPropertyAxiom) axiom).getRole().getName()));
		OWLObjectProperty subRole = factory.getOWLObjectProperty(
				URI.create(((SubObjectPropertyAxiom) axiom).getSubRole().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLSubObjectPropertyAxiom(subRole, role);
		addAxiom(axiomOWLAPI);	
	}	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dllearner.core.owl.TerminologicalAxiomVisitor#visit(org.dllearner.core.owl.EquivalentClassesAxiom)
	 */
	public void visit(EquivalentClassesAxiom axiom) {
		OWLDescription d1 = getOWLDescription(axiom.getConcept1());
		OWLDescription d2 = getOWLDescription(axiom.getConcept2());
		Set<OWLDescription> ds = new HashSet<OWLDescription>();
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
		OWLDescription d1 = getOWLDescription(axiom.getSubConcept());
		OWLDescription d2 = getOWLDescription(axiom.getSuperConcept());
		Set<OWLDescription> ds = new HashSet<OWLDescription>();
		ds.add(d1);
		ds.add(d2);
		OWLAxiom axiomOWLAPI = factory.getOWLSubClassAxiom(d1,d2);
		addAxiom(axiomOWLAPI);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.PropertyAxiomVisitor#visit(org.dllearner.core.owl.DatatypePropertyDomainAxiom)
	 */
	public void visit(DatatypePropertyDomainAxiom datatypePropertyDomainAxiom) {
		OWLDescription d = getOWLDescription(datatypePropertyDomainAxiom.getDomain());
		OWLDataProperty dp = factory.getOWLDataProperty(URI.create(datatypePropertyDomainAxiom.getProperty().getName()));
		OWLAxiom axiomOWLAPI = factory.getOWLDataPropertyDomainAxiom(dp, d);
		addAxiom(axiomOWLAPI);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.PropertyAxiomVisitor#visit(org.dllearner.core.owl.ObjectPropertyDomainAxiom)
	 */
	public void visit(ObjectPropertyDomainAxiom objectPropertyDomainAxiom) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.PropertyAxiomVisitor#visit(org.dllearner.core.owl.DatatypePropertyRangeAxiom)
	 */
	public void visit(DatatypePropertyRangeAxiom axiom) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.owl.PropertyAxiomVisitor#visit(org.dllearner.core.owl.ObjectPropertyRangeAxiom)
	 */
	public void visit(ObjectPropertyRangeAxiom axiom) {
		// TODO Auto-generated method stub
		
	}



}
