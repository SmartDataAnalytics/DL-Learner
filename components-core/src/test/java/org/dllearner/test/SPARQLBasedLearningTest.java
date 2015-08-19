/**
 * 
 */
package org.dllearner.test;

import java.util.NavigableSet;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ClassBasedSampleGenerator;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerImplementation;
import org.dllearner.refinementoperators.RhoDRDown;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.OWLOntologyMerger;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.google.common.collect.Sets;

/**
 * @author Lorenz Buehmann
 *
 */
public class SPARQLBasedLearningTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
//		endpoint = new SparqlEndpoint(new URL("http://sake.informatik.uni-leipzig.de:8890/sparql"), "http://dbpedia.org");
		
		// the class to describe
		OWLClass cls = new OWLClassImpl(IRI.create("http://dbpedia.org/ontology/SoccerClub"));
		
		// (optionally) provide the schema
		String ontologyURL = "http://downloads.dbpedia.org/2015-04/dbpedia_2015-04.owl";
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology schemaOntology = man.loadOntology(IRI.create(ontologyURL));
		KnowledgeSource schemaKS = new OWLAPIOntology(schemaOntology);
		schemaKS.init();
		
		// setup the dataset
		SparqlEndpointKS ks = new SparqlEndpointKS(endpoint, schemaKS);
		ks.init();
		
		// extract sample of the knowledge base
		ClassBasedSampleGenerator sampleGen = new ClassBasedSampleGenerator(ks);
		sampleGen.addAllowedPropertyNamespaces(Sets.newHashSet("http://dbpedia.org/ontology/"));
		sampleGen.addAllowedObjectNamespaces(Sets.newHashSet("http://dbpedia.org/ontology/", "http://dbpedia.org/resource/"));
		OWLOntology sampleOntology = sampleGen.getSample(cls);
		
		// add schema axioms to the sample
		man.addAxioms(sampleOntology, schemaOntology.getLogicalAxioms());
		
		// setup knowledge source
		AbstractKnowledgeSource sampleKS = new OWLAPIOntology(sampleOntology);
		ks.init();
		
		// setup reasoner
//		SPARQLReasoner reasoner = new SPARQLReasoner(ks);
		OWLAPIReasoner baseReasoner = new OWLAPIReasoner(sampleKS);
		baseReasoner.setReasonerImplementation(ReasonerImplementation.HERMIT);
		baseReasoner.init();
		ClosedWorldReasoner reasoner = new ClosedWorldReasoner(baseReasoner);
		reasoner.init();
		
		// setup learning problem
		ClassLearningProblem lp = new ClassLearningProblem(reasoner);
		lp.setClassToDescribe(cls);
		lp.init();
		
		// setup refinement operator
		RhoDRDown op = new RhoDRDown();
		op.setReasoner(reasoner);
		op.setUseNegation(false);
		op.init();
		
		// setup learning algorithm
		CELOE celoe = new CELOE(lp, reasoner);
		celoe.setOperator(op);
		celoe.setWriteSearchTree(true);
		celoe.setSearchTreeFile("/tmp/searchtree-celoe.txt");
		celoe.setReplaceSearchTree(true);
		celoe.setMaxExecutionTimeInSeconds(10);
		celoe.setNoisePercentage(0);
		celoe.init();
		
		// run
		celoe.start();
		
//		Set<OWLClassExpression> refinements = op.refine(new OWLClassImpl(IRI.create("http://dbpedia.org/ontology/Work")), 5);
//		for (OWLClassExpression ref : refinements) {
//			System.out.println(ref + ":" + lp.getAccuracy(ref, 1.0));
//		}
		
		
		NavigableSet<? extends EvaluatedDescription> solutions = celoe.getCurrentlyBestEvaluatedDescriptions();
		
		System.out.println(solutions);
	}

}
