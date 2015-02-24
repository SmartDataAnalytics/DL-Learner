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
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.owl.DLSyntaxObjectRenderer;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.dllearner.utilities.owl.OWLPunningDetector;
import org.junit.Assert;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

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
		Set<OWLIndividual> posExamples = new HashSet<OWLIndividual>();
		for (String uri : Sets.newHashSet("http://ex.org/TRABANT601#1234", "http://ex.org/S51#2345", "http://ex.org/MIFA23#3456")) {
			posExamples.add(df.getOWLNamedIndividual(IRI.create(uri)));
		}
		Set<OWLIndividual> negExamples = new HashSet<OWLIndividual>();
		for (String uri : Sets.newHashSet("http://ex.org/CLIPSO90MG#4567", "http://ex.org/SIEMENS425#567", "http://ex.org/TATRAT3#678")) {
			negExamples.add(df.getOWLNamedIndividual(IRI.create(uri)));
		}
		
		KnowledgeSource ks = new OWLAPIOntology(ontology);
		ks.init();
		
		FastInstanceChecker rc = new FastInstanceChecker(ks);
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
		
		PrefixManager pm = new DefaultPrefixManager("http://ex.org/");
		OWLClass fahrzeug = df.getOWLClass("Fahrzeug", pm);
		OWLClassExpression d = fahrzeug;
		System.out.println(d);
		SortedSet<OWLIndividual> individuals = rc.getIndividuals(d);
		System.out.println(individuals);

		d = df.getOWLObjectIntersectionOf(
				fahrzeug, 
				df.getOWLObjectSomeValuesFrom(OWLPunningDetector.punningProperty, df.getOWLThing()));
		System.out.println(d);
		individuals = rc.getIndividuals(d);
		System.out.println(individuals);

		d = df.getOWLObjectIntersectionOf(
				fahrzeug,
				df.getOWLObjectSomeValuesFrom(
						OWLPunningDetector.punningProperty,
						df.getOWLObjectSomeValuesFrom(df.getOWLObjectProperty("bereifung", pm), df.getOWLThing())));
		System.out.println(d);
		individuals = rc.getIndividuals(d);
		System.out.println(individuals);
		
		//get some refinements
		System.out.println("###############");
		System.out.println("Refinements:");
		Set<OWLClassExpression> refinements = la.getOperator().refine(d, OWLClassExpressionUtils.getLength(d) + 4);
		for (OWLClassExpression ref : refinements) {
			System.out.println(ref);
			System.out.println(lp.getAccuracyOrTooWeak(ref, 0d));
		}
		System.out.println("###############");
		
		d = df.getOWLObjectIntersectionOf(
				fahrzeug,
				df.getOWLObjectSomeValuesFrom(
						OWLPunningDetector.punningProperty,
						df.getOWLObjectSomeValuesFrom(
								df.getOWLObjectProperty("bereifung", pm),
								df.getOWLObjectSomeValuesFrom(
										OWLPunningDetector.punningProperty,df.getOWLThing()))));
		System.out.println(d);
		individuals = rc.getIndividuals(d);
		System.out.println(individuals);
//		List<? extends EvaluatedDescription> currentlyBestEvaluatedDescriptions = la.getCurrentlyBestEvaluatedDescriptions(100);
//		for (EvaluatedDescription ed : currentlyBestEvaluatedDescriptions) {
//			System.out.println(ed);
//		}
	}

}
