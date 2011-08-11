package org.dllearner.test.junit;

import org.dllearner.algorithms.properties.EquivalentObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.FunctionalObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.ObjectPropertyDomainAxiomLearner;
import org.dllearner.algorithms.properties.ObjectPropertyRangeAxiomLearner;
import org.dllearner.algorithms.properties.ReflexiveObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.SubObjectPropertyOfAxiomLearner;
import org.dllearner.algorithms.properties.SymmetricObjectPropertyAxiomLearner;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;

import junit.framework.TestCase;

public class PropertyLearningTest extends TestCase{
	
	private SparqlEndpointKS ks;
	private int maxExecutionTimeInSeconds = 3;
	private int nrOfAxioms = 3;
	
	private ObjectProperty functional = new ObjectProperty("http://dbpedia.org/ontology/league");
	private ObjectProperty reflexive = new ObjectProperty("http://dbpedia.org/ontology/influencedBy");
	private ObjectProperty symmetric = new ObjectProperty("http://dbpedia.org/ontology/influencedBy");
	private ObjectProperty domain = new ObjectProperty("http://dbpedia.org/ontology/writer");
	private ObjectProperty range = new ObjectProperty("http://dbpedia.org/ontology/writer");
	private ObjectProperty subProperty = new ObjectProperty("http://dbpedia.org/ontology/author");
	private ObjectProperty equivProperty = new ObjectProperty("http://dbpedia.org/ontology/academyAward");
	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpedia());
	}
	
	public void testSubPropertyOfAxiomLearning() throws Exception {
		SubObjectPropertyOfAxiomLearner l = new SubObjectPropertyOfAxiomLearner(ks);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.setPropertyToDescribe(subProperty);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(nrOfAxioms));
	}
	
	public void testEquivalentPropertyOfAxiomLearning() throws Exception {
		EquivalentObjectPropertyAxiomLearner l = new EquivalentObjectPropertyAxiomLearner(ks);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.setPropertyToDescribe(equivProperty);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(nrOfAxioms));
	}
	
	public void testPropertyDomainAxiomLearning() throws Exception {
		ObjectPropertyDomainAxiomLearner l = new ObjectPropertyDomainAxiomLearner(ks);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.setPropertyToDescribe(domain);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(nrOfAxioms));
	}
	
	public void testPropertyRangeAxiomLearning() throws Exception {
		ObjectPropertyRangeAxiomLearner l = new ObjectPropertyRangeAxiomLearner(ks);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.setPropertyToDescribe(range);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(nrOfAxioms));
	}
	
	public void testReflexivePropertyAxiomLearning() throws Exception {
		ReflexiveObjectPropertyAxiomLearner l = new ReflexiveObjectPropertyAxiomLearner(ks);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.setPropertyToDescribe(reflexive);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(nrOfAxioms));
	}
	
	public void testFunctionalPropertyAxiomLearnining() throws Exception {
		FunctionalObjectPropertyAxiomLearner l = new FunctionalObjectPropertyAxiomLearner(ks);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.setPropertyToDescribe(functional);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(nrOfAxioms));
	}
	
	public void testSymmetricPropertyAxiomLearning() throws Exception {
		SymmetricObjectPropertyAxiomLearner l = new SymmetricObjectPropertyAxiomLearner(ks);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.setPropertyToDescribe(symmetric);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(nrOfAxioms));
	}

}
