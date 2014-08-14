package org.dllearner.test.junit;

import junit.framework.TestCase;

import org.dllearner.algorithms.DisjointClassesLearner;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

public class DisjointClassesLearningTest extends TestCase{
	
	private SparqlEndpointKS ks;
	private SPARQLReasoner reasoner;
	
	private static final int maxExecutionTimeInSeconds = 10;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpediaLiveAKSW());
		
		reasoner = new SPARQLReasoner(ks);
		reasoner.prepareSubsumptionHierarchy();
	}
	
	public void testLearnSingleClass(){
		DisjointClassesLearner l = new DisjointClassesLearner(ks);
		l.setReasoner(reasoner);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.setClassToDescribe(new OWLClassImpl(IRI.create("http://dbpedia.org/ontology/Book")));
		
		l.start();
		
		System.out.println(l.getCurrentlyBestAxioms(5));
	}
	
	public void testLearnForMostGeneralClasses(){
		DisjointClassesLearner l = new DisjointClassesLearner(ks);
		l.setReasoner(reasoner);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		
		for(OWLClassExpression cls : reasoner.getClassHierarchy().getMostGeneralClasses()){
			l.setClassToDescribe(cls.asOWLClass());
			
			l.start();
			
			System.out.println(l.getCurrentlyBestAxioms(5));
		}
	}
	
	

}
