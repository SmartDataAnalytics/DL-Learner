package org.dllearner.test;

import java.net.MalformedURLException;
import java.net.URL;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.owllink.OWLlinkHTTPXMLReasonerFactory;
import org.semanticweb.owlapi.owllink.OWLlinkReasoner;
import org.semanticweb.owlapi.owllink.OWLlinkReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.NodeSet;

public class OWLLinkReasonerTest {

	/**
	 * @param args
	 * @throws OWLOntologyCreationException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws OWLOntologyCreationException, MalformedURLException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		OWLOntology ontology = manager.createOntology(IRI.create("tutorial"));
		OWLClass A = manager.getOWLDataFactory().getOWLClass(IRI.create("http://tutorial#A"));
		OWLClass B = manager.getOWLDataFactory().getOWLClass(IRI.create("http://tutorial#B"));
		OWLAxiom a = manager.getOWLDataFactory().getOWLSubClassOfAxiom(A, B);
		manager.addAxiom(ontology, a);
		
		OWLlinkHTTPXMLReasonerFactory factory = new OWLlinkHTTPXMLReasonerFactory();
		URL url = new URL("http://localhost:8080");//Configure the server end-point
		OWLlinkReasonerConfiguration config = new OWLlinkReasonerConfiguration(url);
		OWLlinkReasoner reasoner = factory.createReasoner(ontology, config);
		
		NodeSet<OWLClass> classes = reasoner.getSubClasses(manager.getOWLDataFactory().getOWLThing(), true);
		System.out.println(classes.getFlattened());

	}

}
