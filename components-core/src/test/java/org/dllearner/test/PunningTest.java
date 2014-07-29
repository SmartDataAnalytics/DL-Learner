/**
 * 
 */
package org.dllearner.test;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Set;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;
import org.junit.Assert;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.google.common.collect.Sets;

/**
 * @author Lorenz Buehmann
 *
 */
public class PunningTest {
	
	
	public OWLOntology makeExampleKB() throws OWLOntologyCreationException{
		String kb = "@prefix owl:<http://www.w3.org/2002/07/owl#> . @prefix :<http://foo.org/> .";
		kb += ":p a owl:ObjectProperty .";
		
		for (int i = 1; i <= 5; i++) {
			kb += ":r" + i + " a owl:ObjectProperty .";
		}
		
		kb += ":A a owl:Class; :r1 :o1; :r2 :o2 .";
		kb += ":B a owl:Class; :r1 :o3; :r3 :o2 .";
		
		for (int i = 1; i <= 10; i++) {
			kb += ":x" + i + " a owl:NamedIndividual .";
		}
		
		int m = 5;
		int n = 5;
		
		//m instances of A
		for (int i = 1; i <= m; i++) {
			kb += ":x" + i + " a :A .";
		}
		
		//n instances of B
		for (int i = 1; i <= n; i++) {
			kb += ":x" + i + " a :A .";
		}
		
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new ByteArrayInputStream(kb.getBytes()));
		return ontology;
	}
	
	public OWLOntology loadExample() throws OWLOntologyCreationException{
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(this.getClass().getClassLoader().getResourceAsStream("punning_example.ttl"));
		return ontology;
	}
	
	@Test
	public void testPunning() throws OWLOntologyCreationException, ComponentInitException{
		OWLOntology ontology = makeExampleKB();
		OWLDataFactory df = new OWLDataFactoryImpl();
		
		//check that A and B are both, individual and class
		OWLClass clsA = df.getOWLClass(IRI.create("http://foo.org/A"));
		OWLClass clsB = df.getOWLClass(IRI.create("http://foo.org/B"));
		OWLIndividual indA = df.getOWLNamedIndividual(IRI.create("http://foo.org/A"));
		OWLIndividual indB = df.getOWLNamedIndividual(IRI.create("http://foo.org/B"));
		
		Set<OWLClass> classes = ontology.getClassesInSignature();
		Set<OWLObjectProperty> properties = ontology.getObjectPropertiesInSignature();
		Set<OWLNamedIndividual> individuals = ontology.getIndividualsInSignature();
		
		System.out.println("Classes:" + classes);
		System.out.println("Properties:" + properties);
		System.out.println("Individuals:" + individuals);
		
		Assert.assertTrue(
				ontology.getClassesInSignature().contains(clsA) && 
				ontology.getClassesInSignature().contains(clsB) &&
				ontology.getIndividualsInSignature().contains(indA) &&
				ontology.getIndividualsInSignature().contains(indB)
				);
		
		KnowledgeSource ks = new OWLAPIOntology(ontology);
		ks.init();
		
		AbstractReasonerComponent rc = new FastInstanceChecker(ks);
		rc.init();
		
		AbstractLearningProblem lp = new PosNegLPStandard(rc);
		lp.init();
		
		AbstractCELA la = new CELOE(lp, rc);
		la.init();
		
		la.start();
	}
	
	@Test
	public void testPunning2() throws OWLOntologyCreationException, ComponentInitException{
		OWLOntology ontology = loadExample();
		OWLDataFactory df = new OWLDataFactoryImpl();
		
		//check that A and B are both, individual and class
		Set<Individual> posExamples = new HashSet<Individual>();
		for (String uri : Sets.newHashSet("http://ex.org/TRABANT601#1234", "http://ex.org/S51#2345", "http://ex.org/MIFA23#3456")) {
			posExamples.add(new Individual(uri));
		}
		Set<Individual> negExamples = new HashSet<Individual>();
		for (String uri : Sets.newHashSet("http://ex.org/CLIPSO90MG#4567", "http://ex.org/SIEMENS425#567", "http://ex.org/TATRAT3#678")) {
			negExamples.add(new Individual(uri));
		}
		
		KnowledgeSource ks = new OWLAPIOntology(ontology);
		ks.init();
		
		AbstractReasonerComponent rc = new FastInstanceChecker(ks);
		rc.init();
		
		PosNegLPStandard lp = new PosNegLPStandard(rc);
		lp.setPositiveExamples(posExamples);
		lp.setNegativeExamples(negExamples);
		lp.init();
		
		AbstractCELA la = new CELOE(lp, rc);
		la.init();
		
		la.start();
	}

}
