package org.dllearner.test;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLOntologyStorageException;
import org.semanticweb.owl.model.RemoveAxiom;
import org.semanticweb.owl.model.UnknownOWLOntologyException;

public class PelletBug {

	public static void main(String[] args) throws OWLOntologyCreationException,
			OWLReasonerException, UnknownOWLOntologyException, OWLOntologyStorageException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File f = new File("examples/family/father_oe.owl");
		URI physicalURI = f.toURI();
		OWLOntology ontology = manager.loadOntologyFromPhysicalURI(physicalURI);
		OWLDataFactory factory = manager.getOWLDataFactory();

		// create a view class expressions and an axiom
		String ontologyURI = "http://example.com/father#";
		OWLClass male = factory.getOWLClass(URI.create(ontologyURI + "male"));
		OWLClass female = factory.getOWLClass(URI.create(ontologyURI + "female"));
		OWLClass father = factory.getOWLClass(URI.create(ontologyURI + "father"));
		OWLDescription insat = factory.getOWLObjectIntersectionOf(male, female);
		OWLDescription test = factory.getOWLObjectComplementOf(male);
		OWLAxiom axiom = factory.getOWLEquivalentClassesAxiom(father, test);

		// load ontology
		Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
		ontologies.add(ontology);
		OWLReasoner reasoner = new org.mindswap.pellet.owlapi.Reasoner(manager);
		reasoner.loadOntologies(ontologies);

		// first subsumption check => everything runs smoothly
		boolean result = reasoner.isSubClassOf(female, insat);
		System.out.println("subsumption before: " + result);

		// add axiom causing the ontology to be inconsistent
		try {
			manager.applyChange(new AddAxiom(ontology, axiom));
		} catch (OWLOntologyChangeException e1) {
			e1.printStackTrace();
		}

		// Pellet correctly detects the inconsistency
		try {
			System.out.println("consistent: " + reasoner.isConsistent(ontology));
		} catch (OWLReasonerException e) {
			e.printStackTrace();
		}

		// remove axiom
		try {
			manager.applyChange(new RemoveAxiom(ontology, axiom));
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		}

		// save file to verify that it remained unchanged (it is unchanged)
		manager.saveOntology(ontology, new File("test.owl").toURI());

		// perform subsumption check => Pellet now fails due to an
		// inconsistency, although the ontology is unchanged from the 
		// point of view of the OWL API
		result = reasoner.isSubClassOf(female, insat);
		System.out.println("subsumption after: " + result);

	}

}
