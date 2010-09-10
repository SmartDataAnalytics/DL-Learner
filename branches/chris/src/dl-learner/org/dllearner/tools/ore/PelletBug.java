package org.dllearner.tools.ore;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.clarkparsia.owlapi.explanation.PelletExplanation;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

public class PelletBug {
	public static void main(String[] args) throws OWLOntologyCreationException {
	       String file = "http://protege.stanford.edu/plugins/owl/owl-library/koala.owl";
	       String NS = "http://protege.stanford.edu/plugins/owl/owl-library/koala.owl";
	       OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	       OWLDataFactory factory = manager.getOWLDataFactory();
	       OWLOntology ontology = manager.loadOntology(IRI.create(file));
	       PelletReasoner reasoner = new PelletReasonerFactory().createReasoner(ontology);
	      
	       
	       OWLDataProperty property = factory.getOWLDataProperty(IRI.create(NS + "#isHardWorking"));
	       OWLClass domain = factory.getOWLClass(IRI.create(NS + "#Animal"));
	       OWLDataPropertyDomainAxiom axiom = factory.getOWLDataPropertyDomainAxiom(property, domain);
	       
	       PelletExplanation expGen = new PelletExplanation(ontology);
	       System.out.println(reasoner.isEntailed(axiom));
	       
	       System.out.println(expGen.getEntailmentExplanations(axiom));
	       
	       OWLDataRange range = factory.getTopDatatype();
	       OWLDataSomeValuesFrom dataSome = factory.getOWLDataSomeValuesFrom(property, range);
	       OWLSubClassOfAxiom subClass = factory.getOWLSubClassOfAxiom(dataSome, domain);      
	       
	       System.out.println(reasoner.isEntailed(subClass));
	       System.out.println(expGen.getEntailmentExplanations(subClass));
	      
	   } 
}
