package org.dllearner.test;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

public class PelletBug {

	public static void main(String[] args) throws OWLOntologyCreationException, OWLReasonerException {
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File f = new File("examples/family/father_oe.owl");
		URI physicalURI = f.toURI();
		OWLOntology ontology = manager.loadOntologyFromPhysicalURI(physicalURI);
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		String ontologyURI = "http://example.com/father#";
		
		// super: (http://example.com/father#female AND http://example.com/father#male); sub: http://example.com/father#female
		
		OWLClass male = factory.getOWLClass(URI.create(ontologyURI + "male"));
		OWLClass female = factory.getOWLClass(URI.create(ontologyURI + "female"));
//		OWLDescription negA = factory.getOWLObjectComplementOf(a);
		OWLDescription insat = factory.getOWLObjectIntersectionOf(male, female);
//        OWLClass d = factory.getOWLClass(URI.create(ontologyURI + "#b"));
        
        Set<OWLOntology> ontologies = new HashSet<OWLOntology>();
        ontologies.add(ontology);
        
        OWLReasoner reasoner = new org.mindswap.pellet.owlapi.Reasoner(manager);
        reasoner.loadOntologies(ontologies); 
        
        boolean result = reasoner.isSubClassOf(female, insat);
        System.out.println(result);
        
//        System.out.println(reasoner.getIndividuals(female, true));
	}

}
