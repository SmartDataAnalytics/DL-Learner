package org.dllearner.tools.ore;

import java.net.URI;
import java.util.Collections;

import org.mindswap.pellet.owlapi.PelletReasonerFactory;
import org.mindswap.pellet.owlapi.Reasoner;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataSomeRestriction;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLSubClassAxiom;

import com.clarkparsia.explanation.PelletExplanation;

public class PelletBug {
	public static void main(String[] args) throws OWLOntologyCreationException {
	       String file = "http://protege.stanford.edu/plugins/owl/owl-library/koala.owl";
	       String NS = "http://protege.stanford.edu/plugins/owl/owl-library/koala.owl";
	       OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	       OWLDataFactory factory = manager.getOWLDataFactory();
	       OWLOntology ontology = manager.loadOntology(URI.create(file));
	       Reasoner reasoner = new PelletReasonerFactory().createReasoner(manager);
	       reasoner.loadOntology(ontology);
	      
	       
	       OWLDataProperty property = factory.getOWLDataProperty(URI.create(NS + "#isHardWorking"));
	       OWLClass domain = factory.getOWLClass(URI.create(NS + "#Animal"));
	       OWLDataPropertyDomainAxiom axiom = factory.getOWLDataPropertyDomainAxiom(property, domain);
	       
	       PelletExplanation expGen = new PelletExplanation(manager, Collections.singleton(ontology));
	       System.out.println(reasoner.isEntailed(axiom));
	       
//	       System.out.println(expGen.getEntailmentExplanations(axiom));
//	       
//	       OWLDataRange range = factory.getTopDataType();
//	       OWLDataSomeRestriction dataSome = factory.getOWLDataSomeRestriction(property, range);
//	       OWLSubClassAxiom subClass = factory.getOWLSubClassAxiom(dataSome, domain);      
//	       
//	       System.out.println(reasoner.isEntailed(subClass));
//	       System.out.println(expGen.getEntailmentExplanations(subClass));
	      
	   } 
}
