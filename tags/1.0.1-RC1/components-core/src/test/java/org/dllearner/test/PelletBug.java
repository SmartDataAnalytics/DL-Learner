package org.dllearner.test;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class PelletBug {

	public static void main(String[] args) throws OWLOntologyCreationException,
			 UnknownOWLOntologyException, OWLOntologyStorageException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File f = new File("examples/family/father_oe.owl");
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(f);
		OWLDataFactory factory = manager.getOWLDataFactory();

		// create a view class expressions and an axiom
		String ontologyURI = "http://example.com/father#";
		OWLClass male = factory.getOWLClass(IRI.create(ontologyURI + "male"));
		OWLClass female = factory.getOWLClass(IRI.create(ontologyURI + "female"));
		OWLClass father = factory.getOWLClass(IRI.create(ontologyURI + "father"));
		OWLClassExpression insat = factory.getOWLObjectIntersectionOf(male, female);
		OWLClassExpression test = factory.getOWLObjectComplementOf(male);
		OWLAxiom axiom = factory.getOWLEquivalentClassesAxiom(father, test);

		// load ontology
		OWLReasoner reasoner = new PelletReasonerFactory().createReasoner(ontology);

		// first subsumption check => everything runs smoothly
		boolean result = reasoner.isEntailed(factory.getOWLSubClassOfAxiom(female, insat));
		System.out.println("subsumption before: " + result);

		// add axiom causing the ontology to be inconsistent
		try {
			manager.applyChange(new AddAxiom(ontology, axiom));
		} catch (OWLOntologyChangeException e1) {
			e1.printStackTrace();
		}

		// Pellet correctly detects the inconsistency
		System.out.println("consistent: " + reasoner.isConsistent());

		// remove axiom
		try {
			manager.applyChange(new RemoveAxiom(ontology, axiom));
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		}

		// save file to verify that it remained unchanged (it is unchanged)
		manager.saveOntology(ontology, IRI.create(new File("test.owl")));

		// perform subsumption check => Pellet now fails due to an
		// inconsistency, although the ontology is unchanged from the 
		// point of view of the OWL API
		result = reasoner.isEntailed(factory.getOWLSubClassOfAxiom(female, insat));
		System.out.println("subsumption after: " + result);

	}

}
