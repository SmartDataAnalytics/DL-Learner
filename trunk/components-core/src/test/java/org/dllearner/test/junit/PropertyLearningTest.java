package org.dllearner.test.junit;

import org.dllearner.algorithms.properties.FunctionalPropertyAxiomLearner;
import org.dllearner.algorithms.properties.PropertyDomainAxiomLearner;
import org.dllearner.algorithms.properties.PropertyRangeAxiomLearner;
import org.dllearner.algorithms.properties.ReflexivePropertyAxiomLearner;
import org.dllearner.algorithms.properties.SymmetricPropertyAxiomLearner;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;

import junit.framework.TestCase;

public class PropertyLearningTest extends TestCase{
	
	private SparqlEndpointKS ks;
	private int maxExecutionTimeInSeconds = 5;
	private int nrOfAxioms = 3;
	
	private ObjectProperty functional = new ObjectProperty("http://dbpedia.org/ontology/league");
	private ObjectProperty reflexive = new ObjectProperty("http://dbpedia.org/ontology/influencedBy");
	private ObjectProperty symmetric = new ObjectProperty("http://dbpedia.org/ontology/influencedBy");
	private ObjectProperty domain = new ObjectProperty("http://dbpedia.org/ontology/writer");
	private ObjectProperty range = new ObjectProperty("http://dbpedia.org/ontology/writer");
	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpedia());
	}
	
	public void testPropertyDomainAxiomLearning() throws Exception {
		PropertyDomainAxiomLearner l = new PropertyDomainAxiomLearner(ks);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.setPropertyToDescribe(domain);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(nrOfAxioms));
	}
	
	public void testPropertyRangeAxiomLearning() throws Exception {
		PropertyRangeAxiomLearner l = new PropertyRangeAxiomLearner(ks);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.setPropertyToDescribe(range);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(nrOfAxioms));
	}
	
	public void testReflexivePropertyAxiomLearning() throws Exception {
		ReflexivePropertyAxiomLearner l = new ReflexivePropertyAxiomLearner(ks);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.setPropertyToDescribe(reflexive);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(nrOfAxioms));
	}
	
	public void testFunctionalPropertyAxiomLearnining() throws Exception {
		FunctionalPropertyAxiomLearner l = new FunctionalPropertyAxiomLearner(ks);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.setPropertyToDescribe(functional);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(nrOfAxioms));
	}
	
	public void testSymmetricPropertyAxiomLearning() throws Exception {
		SymmetricPropertyAxiomLearner l = new SymmetricPropertyAxiomLearner(ks);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.setPropertyToDescribe(symmetric);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(nrOfAxioms));
	}

}
