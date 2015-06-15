/**
 * 
 */
package org.dllearner.test;

import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ClassBasedSampleGenerator;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.refinementoperators.RhoDRDown;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

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
		endpoint = new SparqlEndpoint(new URL("http://sake.informatik.uni-leipzig.de:8890/sparql"), "http://dbpedia.org");
		OWLClass cls = new OWLClassImpl(IRI.create("http://dbpedia.org/ontology/Astronaut"));
		
		SparqlEndpointKS ks = new SparqlEndpointKS(endpoint);
		ks.init();
		
		// extract sample
		ClassBasedSampleGenerator sampleGen = new ClassBasedSampleGenerator(ks.getQueryExecutionFactory());
		sampleGen.addAllowedPropertyNamespaces(Sets.newHashSet("http://dbpedia.org/ontology/"));
		sampleGen.addAllowedObjectNamespaces(Sets.newHashSet("http://dbpedia.org/ontology/", "http://dbpedia.org/resource/"));
		OWLOntology sample = sampleGen.getSample(cls);
		
		AbstractKnowledgeSource sampleKS = new OWLAPIOntology(sample);
		ks.init();
		
//		SPARQLReasoner reasoner = new SPARQLReasoner(ks);
		ClosedWorldReasoner reasoner = new ClosedWorldReasoner(sampleKS);
		reasoner.init();
		
		ClassLearningProblem lp = new ClassLearningProblem(reasoner);
		lp.setClassToDescribe(cls);
		lp.init();
		
		RhoDRDown op = new RhoDRDown();
		op.setReasoner(reasoner);
		op.setUseNegation(false);
		op.init();
		
		CELOE celoe = new CELOE(lp, reasoner);
		celoe.setOperator(op);
		celoe.setWriteSearchTree(true);
		celoe.setSearchTreeFile("/tmp/searchtree-celoe.txt");
		celoe.setReplaceSearchTree(true);
		celoe.setMaxExecutionTimeInSeconds(300);
		celoe.setNoisePercentage(0);
		celoe.init();
		
		celoe.start();
		
//		Set<OWLClassExpression> refinements = op.refine(new OWLClassImpl(IRI.create("http://dbpedia.org/ontology/Work")), 5);
//		for (OWLClassExpression ref : refinements) {
//			System.out.println(ref + ":" + lp.getAccuracy(ref, 1.0));
//		}
		
		
		TreeSet<? extends EvaluatedDescription> solutions = celoe.getCurrentlyBestEvaluatedDescriptions();
		
		System.out.println(solutions);
	}

}
