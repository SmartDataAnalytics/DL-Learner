/**
 * 
 */
package org.dllearner.test;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.celoe.OEHeuristicRuntime;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.Thing;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.MaterializableFastInstanceChecker;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.owl.DLSyntaxObjectRenderer;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;
import org.dllearner.utilities.owl.OWLPunningDetector;
import org.junit.Assert;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

/**
 * @author Lorenz Buehmann
 *
 */
public class PunningTest {
	
	public OWLOntology loadExample() throws OWLOntologyCreationException{
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(this.getClass().getClassLoader().getResourceAsStream("punning_example.ttl"));
		return ontology;
	}
	
	@Test
	public void testPunningExists() throws OWLOntologyCreationException, ComponentInitException{
		OWLOntology ontology = loadExample();
		
		Set<OWLNamedIndividual> individuals = ontology.getIndividualsInSignature();
		Set<OWLClass> classes = ontology.getClassesInSignature();
		
		SetView<IRI> intersection = Sets.intersection(toIRI(individuals), toIRI(classes));
		System.out.println("Entities that are class and individual:\n" + intersection);
		Assert.assertTrue(!intersection.isEmpty());
		
	}
	
	private Set<IRI> toIRI(Set<? extends OWLEntity> entities){
		Set<IRI> iris = new HashSet<IRI>();
		for (OWLEntity e : entities) {
			iris.add(e.getIRI());
		}
		return iris;
	}
	
	@Test
	public void testPunning() throws OWLOntologyCreationException, ComponentInitException{
		ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
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
		
		MaterializableFastInstanceChecker rc = new MaterializableFastInstanceChecker(ks);
//		rc.setUseMaterializationCaching(false);
		rc.setHandlePunning(true);
		rc.setUseMaterializationCaching(false);
		rc.init();
		rc.setBaseURI("http://ex.org/");
		
		PosNegLPStandard lp = new PosNegLPStandard(rc);
		lp.setPositiveExamples(posExamples);
		lp.setNegativeExamples(negExamples);
		lp.init();
		
		CELOE la = new CELOE(lp, rc);
		la.setWriteSearchTree(true);
		la.setSearchTreeFile("log/punning_search_tree.txt");
		la.setReplaceSearchTree(true);
		la.setMaxNrOfResults(50);
		la.setMaxExecutionTimeInSeconds(20);
		la.setExpandAccuracy100Nodes(true);
		OEHeuristicRuntime heuristic = new OEHeuristicRuntime();
//		heuristic.setExpansionPenaltyFactor(0.001);
//		la.setHeuristic(heuristic);
		la.init();
		((RhoDRDown)la.getOperator()).setUseNegation(false);
//		la.start();
		
		System.out.println("Classes: " + ontology.getClassesInSignature());
		System.out.println("Individuals: " + ontology.getIndividualsInSignature());
		
		Description d = new Intersection(new NamedClass("http://ex.org/Fahrzeug"));
		System.out.println(OWLAPIDescriptionConvertVisitor.getOWLClassExpression(d));
		SortedSet<Individual> individuals = rc.getIndividuals(d);
		System.out.println(individuals);

		d = new Intersection(new NamedClass("http://ex.org/Fahrzeug"), new ObjectSomeRestriction(
				OWLPunningDetector.punningProperty, Thing.instance));
		System.out.println(OWLAPIDescriptionConvertVisitor.getOWLClassExpression(d));
		individuals = rc.getIndividuals(d);
		System.out.println(individuals);

		d = new Intersection(new NamedClass("http://ex.org/Fahrzeug"), new ObjectSomeRestriction(
				OWLPunningDetector.punningProperty, new ObjectSomeRestriction(new ObjectProperty(
						"http://ex.org/bereifung"), Thing.instance)));
		System.out.println(OWLAPIDescriptionConvertVisitor.getOWLClassExpression(d));
		individuals = rc.getIndividuals(d);
		System.out.println(individuals);
		
		d = new Intersection(new NamedClass("http://ex.org/Fahrzeug"), new ObjectSomeRestriction(
				OWLPunningDetector.punningProperty, 
//				new ObjectSomeRestriction(new ObjectProperty("http://ex.org/bereifung"), 
						Thing.instance));
		System.out.println(OWLAPIDescriptionConvertVisitor.getOWLClassExpression(d));
		individuals = rc.getIndividuals(d);
		System.out.println(individuals);
		
		//get some refinements
		System.out.println("###############");
		System.out.println("Refinements:");
		Set<Description> refinements = la.getOperator().refine(d, d.getLength() + 4);
		for (Description ref : refinements) {
			System.out.println(OWLAPIDescriptionConvertVisitor.getOWLClassExpression(ref));
			System.out.println(lp.getAccuracyOrTooWeak(ref, 0d));
		}
		System.out.println("###############");
		

		d = new Intersection(new NamedClass("http://ex.org/Fahrzeug"), new ObjectSomeRestriction(
				OWLPunningDetector.punningProperty, new ObjectSomeRestriction(new ObjectProperty(
						"http://ex.org/bereifung"), new ObjectSomeRestriction(OWLPunningDetector.punningProperty,
						Thing.instance))));
		System.out.println(OWLAPIDescriptionConvertVisitor.getOWLClassExpression(d));
		individuals = rc.getIndividuals(d);
		System.out.println(individuals);
//		List<? extends EvaluatedDescription> currentlyBestEvaluatedDescriptions = la.getCurrentlyBestEvaluatedDescriptions(100);
//		for (EvaluatedDescription ed : currentlyBestEvaluatedDescriptions) {
//			System.out.println(ed);
//		}
	}

}
