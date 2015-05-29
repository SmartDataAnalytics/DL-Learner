/**
 * 
 */
package org.dllearner.test;

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
import org.dllearner.reasoning.SPARQLReasoner;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

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
		OWLClass cls = new OWLClassImpl(IRI.create("http://dbpedia.org/ontology/Book"));
		
		SparqlEndpointKS ks = new SparqlEndpointKS(endpoint);
		ks.init();
		
		// extract sample
		ClassBasedSampleGenerator sampleGen = new ClassBasedSampleGenerator(ks.getQueryExecutionFactory());
		OWLOntology sample = sampleGen.getSample(cls);
		
		AbstractKnowledgeSource sampleKS = new OWLAPIOntology(sample);
		ks.init();
		
//		SPARQLReasoner reasoner = new SPARQLReasoner(ks);
		ClosedWorldReasoner reasoner = new ClosedWorldReasoner(sampleKS);
		reasoner.init();
		
		ClassLearningProblem lp = new ClassLearningProblem(reasoner);
		lp.setClassToDescribe(cls);
		lp.init();
		
		CELOE celoe = new CELOE(lp, reasoner);
		celoe.init();
		
		celoe.start();
		
		
		TreeSet<? extends EvaluatedDescription> solutions = celoe.getCurrentlyBestEvaluatedDescriptions();
		
		System.out.println(solutions);
	}

}
