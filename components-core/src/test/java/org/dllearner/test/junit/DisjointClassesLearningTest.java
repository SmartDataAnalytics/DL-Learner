package org.dllearner.test.junit;

import java.net.MalformedURLException;

import org.dllearner.algorithms.DisjointClassesLearner;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.reasoning.SPARQLReasoner;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

public class DisjointClassesLearningTest { //extends TestCase{

	private SparqlEndpointKS ks;
	private SPARQLReasoner reasoner;

	private static final int maxExecutionTimeInSeconds = 10;

//	@Override
//	protected void setUp() throws Exception {
//		super.setUp();
//		ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpediaLiveAKSW());
//
//		reasoner = new SPARQLReasoner(ks);
//		reasoner.prepareSubsumptionHierarchy();
//	}

//	@Test
	public void testLearnSingleClass() throws MalformedURLException, ComponentInitException{
		ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpediaLiveAKSW());
		ks.init();
		reasoner = new SPARQLReasoner(ks);
		reasoner.init();
		DisjointClassesLearner l = new DisjointClassesLearner(ks);
		l.setReasoner(reasoner);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.setEntityToDescribe(new OWLClassImpl(IRI.create("http://dbpedia.org/ontology/Book")));
		l.init();
		l.start();

		System.out.println(l.getCurrentlyBestAxioms(5));
	}

//	@Test
	public void testLearnForMostGeneralClasses() throws ComponentInitException, MalformedURLException{
		ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpediaLiveAKSW());
		ks.init();
		reasoner = new SPARQLReasoner(ks);
		reasoner.init();
		DisjointClassesLearner l = new DisjointClassesLearner(ks);
		l.setReasoner(reasoner);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.init();

		for(OWLClassExpression cls : reasoner.getClassHierarchy().getMostGeneralClasses()){
			l.setEntityToDescribe(cls.asOWLClass());

			l.start();

			System.out.println(l.getCurrentlyBestAxioms(5));
		}
	}



}
