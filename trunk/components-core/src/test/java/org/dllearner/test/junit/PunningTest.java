/**
 * 
 */
package org.dllearner.test.junit;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.reasoning.FastInstanceChecker;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Lorenz Buehmann
 *
 */
public class PunningTest {

	@Test
	public void test() throws Exception {
		String triples = "@prefix owl:<http://www.w3.org/2002/07/owl#> . @prefix : <http://other.example.org/ns#> . @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> ."
				+ ":A a owl:NamedIndividual. :A a owl:Class. :A a :B . :B rdfs:subClassOf :A .";
		System.out.println(triples);
		ModelFactory.createDefaultModel().read(new ByteArrayInputStream(triples.getBytes()), null, "TURTLE");
		
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = man.getOWLDataFactory();
		OWLOntology ontology = man.loadOntologyFromOntologyDocument(new ByteArrayInputStream(triples.getBytes()));
		System.out.println(ontology.getIndividualsInSignature());
		System.out.println(ontology.getClassesInSignature());
		OWLReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance();
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
		OWLClass c = df.getOWLClass(IRI.create("http://other.example.org/ns#A"));
		OWLNamedIndividual i = df.getOWLNamedIndividual(IRI.create("http://other.example.org/ns#A"));
		System.out.println(reasoner.getInstances(c, false));
		KnowledgeSource ks = new OWLAPIOntology(ontology);
		AbstractReasonerComponent rc = new FastInstanceChecker(ks);
		rc.init();
		NamedClass cls = new NamedClass("http://other.example.org/ns#A");
		System.out.println(rc.getIndividuals(cls));
		Individual ind = new Individual("http://other.example.org/ns#A");
		System.out.println(rc.hasType(cls, ind));
	}

}
