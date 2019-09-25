/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.test.junit;

import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.dllearner.algorithms.properties.*;
import org.dllearner.core.EvaluatedAxiom;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.reasoning.SPARQLReasoner.PopularityType;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class PropertyAxiomLearningTest {
	
	private static SparqlEndpointKS ks;
	private int maxExecutionTimeInSeconds = 3;
	private int nrOfAxioms = 3;
	
	private static final String NS = "http://dllearner.org/test/";
	
	static PrefixManager pm = new DefaultPrefixManager();
	static {
		pm.setDefaultPrefix(NS);
	}
	OWLDataFactory df = new OWLDataFactoryImpl();
	
	private OWLObjectProperty op1 = df.getOWLObjectProperty("op1", pm);
	private OWLObjectProperty op2 = df.getOWLObjectProperty("op2", pm);
	
	private OWLObjectProperty functional = df.getOWLObjectProperty(IRI.create( "http://dbpedia.org/ontology/league"));
	private OWLObjectProperty reflexive = df.getOWLObjectProperty(IRI.create( "http://dbpedia.org/ontology/influencedBy"));
	private OWLObjectProperty symmetric = df.getOWLObjectProperty(IRI.create( "http://dbpedia.org/ontology/influencedBy"));
	
	private OWLObjectProperty range = df.getOWLObjectProperty(IRI.create( "http://dbpedia.org/ontology/writer"));
	private OWLObjectProperty subProperty = df.getOWLObjectProperty(IRI.create( "http://dbpedia.org/ontology/author"));
	private OWLObjectProperty equivProperty = df.getOWLObjectProperty(IRI.create( "http://dbpedia.org/ontology/academyAward"));
	
	private OWLDataProperty disDataProperty = df.getOWLDataProperty(IRI.create( "http://dbpedia.org/ontology/height"));
	private OWLDataProperty equivDataProperty = df.getOWLDataProperty(IRI.create( "http://dbpedia.org/ontology/height"));

	@BeforeClass
	public static void setUp() throws Exception {
//		ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpedia());
		
		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefix("", NS);
		
		// object properties
		Property op1 = model.createProperty(NS, "op1");
		model.add(op1, RDF.type, OWL.ObjectProperty);
		Property op2 = model.createProperty(NS, "op2");
		model.add(op2, RDF.type, OWL.ObjectProperty);
		
		// classes
		Resource clsA = model.createResource(NS + "A", OWL.Class);
		Resource clsB = model.createResource(NS + "B", OWL.Class);
		Resource clsC = model.createResource(NS + "C", OWL.Class);
		
		// add instances to A
		for(int i = 1; i <= 100; i++){
			model.add(ResourceFactory.createResource(NS + "a" + i), RDF.type, clsA);
		}
		// add instances to B
		for (int i = 1; i <= 100; i++) {
			model.add(ResourceFactory.createResource(NS + "b" + i), RDF.type, clsB);
		}
		// add instances to C
		for (int i = 1; i <= 100; i++) {
			model.add(ResourceFactory.createResource(NS + "c" + i), RDF.type, clsC);
		}
		// add triples <a_i, op1, b_i>
		for (int i = 1; i <= 70; i++) {
			model.add(ResourceFactory.createResource(NS + "a" + i), op1, ResourceFactory.createResource(NS + "b" + i));
		}
		// add triples <a_i, op2, b_i>
		for (int i = 1; i <= 50; i++) {
			model.add(ResourceFactory.createResource(NS + "a" + i), op2, ResourceFactory.createResource(NS + "b" + i));
		}
		// add triples <b_i, op1, a_i>
		for (int i = 1; i <= 30; i++) {
			model.add(ResourceFactory.createResource(NS + "b" + i), op1, ResourceFactory.createResource(NS + "a" + i));
		}
		// add triples <a_i, op1, a_i>
		for (int i = 1; i <= 30; i++) {
			model.add(ResourceFactory.createResource(NS + "a" + i), op1, ResourceFactory.createResource(NS + "a" + i));
		}
		// add triples <a_i, op2, a_i>
		for (int i = 1; i <= 10; i++) {
			model.add(ResourceFactory.createResource(NS + "a" + i), op2, ResourceFactory.createResource(NS + "a" + i));
		}
		// add triples <c_i, op2, b_i>
		for (int i = 1; i <= 10; i++) {
			model.add(ResourceFactory.createResource(NS + "c" + i), op2, ResourceFactory.createResource(NS + "b" + i));
		}
		
		ks = new LocalModelBasedSparqlEndpointKS(model);
	}
	
	/*
	 * object property axioms
	 */
	@Test
	public void testObjectPropertyDomainAxiomLearning() throws Exception {
		ObjectPropertyDomainAxiomLearner l = new ObjectPropertyDomainAxiomLearner(ks);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.setUsePrecisionOnly(false);
		l.setEntityToDescribe(op1);
		l.init();
		l.start();

		EvaluatedAxiom<OWLObjectPropertyDomainAxiom> evAxiom = l.getCurrentlyBestEvaluatedAxiom();
		System.out.println(evAxiom);
		double actualScore = evAxiom.getScore().getAccuracy();
		
		int cntA = 100;
		int cntB = 70;
		int cntAB = 70;
		double beta = 3.0;
		double precision = Heuristics.getConfidenceInterval95WaldAverage(cntB, cntAB);
		double recall = Heuristics.getConfidenceInterval95WaldAverage(cntA, cntAB);
		double expectedScore = Heuristics.getFScore(recall, precision, beta);
		
		 assertEquals("", expectedScore, actualScore, 0d);
	}
	
	@Test
	public void testSubPropertyOfAxiomLearning() throws Exception {
		SubObjectPropertyOfAxiomLearner l = new SubObjectPropertyOfAxiomLearner(ks);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.setEntityToDescribe(op1);
		l.init();
		l.start();
		
		EvaluatedAxiom<OWLSubObjectPropertyOfAxiom> evAxiom = l.getCurrentlyBestEvaluatedAxiom();
		System.out.println(evAxiom);
		double actualScore = evAxiom.getScore().getAccuracy();
		
		int cntOp1 = 130;
		int cntOp2 = 70;
		int cntOp1_Op2 = 60;
		double beta = 3.0;
		double precision = Heuristics.getConfidenceInterval95WaldAverage(cntOp2, cntOp1_Op2);
		double recall = Heuristics.getConfidenceInterval95WaldAverage(cntOp1, cntOp1_Op2);
		double expectedScore = Heuristics.getFScore(recall, precision, beta);
		
		// update Feb 2015: seems to be incorrect to require those values to be the same as one is exact and the other an approximation
		// assertEquals("", expectedScore, actualScore, 0d);
	}
	
	@Test
	public void testEquivalentObjectPropertiesAxiomLearning() throws Exception {
		EquivalentObjectPropertyAxiomLearner l = new EquivalentObjectPropertyAxiomLearner(ks);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.setEntityToDescribe(op1);
		l.init();
		l.start();
		
		EvaluatedAxiom<OWLEquivalentObjectPropertiesAxiom> evAxiom = l.getCurrentlyBestEvaluatedAxiom();
		System.out.println(evAxiom);
		double actualScore = evAxiom.getScore().getAccuracy();
		
		int cntOp1 = 130;
		int cntOp2 = 70;
		int cntOp1_Op2 = 60;
		double beta = 1.0;
		double precision = Heuristics.getConfidenceInterval95WaldAverage(cntOp2, cntOp1_Op2);
		double recall = Heuristics.getConfidenceInterval95WaldAverage(cntOp1, cntOp1_Op2);
		double expectedScore = Heuristics.getFScore(recall, precision, beta);

		// update Feb 2015: seems to be incorrect to require those values to be the same as one is exact and the other an approximation
		// assertEquals("", expectedScore, actualScore, 0d);
		
		// set strict mode, i.e. if for the property explicit domain and range is given
		// we only consider properties with same range and domain
	}
	
	@Test
	public void testPropertyRangeAxiomLearning() throws Exception {
		ObjectPropertyRangeAxiomLearner l = new ObjectPropertyRangeAxiomLearner(ks);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.setEntityToDescribe(range);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(nrOfAxioms));
	}
	
	@Test
	public void testReflexivePropertyAxiomLearning() throws Exception {
		ReflexiveObjectPropertyAxiomLearner l = new ReflexiveObjectPropertyAxiomLearner(ks);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.setEntityToDescribe(reflexive);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(nrOfAxioms));
	}
	
	@Test
	public void testFunctionalPropertyAxiomLearnining() throws Exception {
		FunctionalObjectPropertyAxiomLearner l = new FunctionalObjectPropertyAxiomLearner(ks);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.setEntityToDescribe(functional);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(nrOfAxioms));
	}
	
	@Test
	public void testSymmetricPropertyAxiomLearning() throws Exception {
		SymmetricObjectPropertyAxiomLearner l = new SymmetricObjectPropertyAxiomLearner(ks);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.setEntityToDescribe(symmetric);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(nrOfAxioms));
	}
	
	/*
	 * data property axioms
	 */
	@Test
	public void testEquivalentDataPropertiesAxiomLearning() throws Exception {
		EquivalentDataPropertyAxiomLearner l = new EquivalentDataPropertyAxiomLearner(ks);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.setEntityToDescribe(equivDataProperty);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(nrOfAxioms));
	}
	
	@Test
	public void testDisjointDataPropertiesAxiomLearning() throws Exception {
		DisjointDataPropertyAxiomLearner l = new DisjointDataPropertyAxiomLearner(ks);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.setEntityToDescribe(disDataProperty);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(nrOfAxioms));
	}
	
	@Ignore
	public void testRunDBpedia() throws Exception {
		OWLObjectProperty op = df.getOWLObjectProperty(IRI.create("http://dbpedia.org/ontology/birthPlace"));
		
		SparqlEndpointKS ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpedia());
		ks.setCache(CacheUtilsH2.createCacheFrontend("cache", true, TimeUnit.DAYS.toMillis(1)));
		
		SPARQLReasoner reasoner = new SPARQLReasoner(ks);
		reasoner.init();
		reasoner.precomputePopularities(PopularityType.OBJECT_PROPERTY);
		
		List<Class<? extends ObjectPropertyAxiomLearner<? extends OWLObjectPropertyAxiom>>> la = new ArrayList<>();
		la.add(DisjointObjectPropertyAxiomLearner.class);
		la.add(SubObjectPropertyOfAxiomLearner.class);
		la.add(EquivalentObjectPropertyAxiomLearner.class);
		la.add(FunctionalObjectPropertyAxiomLearner.class);
		la.add(InverseFunctionalObjectPropertyAxiomLearner.class);
		la.add(ReflexiveObjectPropertyAxiomLearner.class);
		la.add(IrreflexiveObjectPropertyAxiomLearner.class);
		
		for (Class<? extends ObjectPropertyAxiomLearner<? extends OWLObjectPropertyAxiom>> cls : la) {
			try {
				Constructor<? extends ObjectPropertyAxiomLearner<? extends OWLObjectPropertyAxiom>> constructor = cls.getConstructor(SparqlEndpointKS.class);
				ObjectPropertyAxiomLearner<? extends OWLObjectPropertyAxiom> learner = constructor.newInstance(ks);
				learner.setEntityToDescribe(op);
				learner.init();
				learner.start();
				List<?> axioms = learner.getCurrentlyBestEvaluatedAxioms(10);
				System.out.println(axioms);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public static void main(String[] args) {
		 String query = "SELECT  ?dt (count(distinct ?o) AS ?cnt)\n" + 
		 		"			WHERE\n" + 
		 		"			  { ?s <http://dbpedia.org/ontology/birthDate> ?o }\n" + 
		 		"			GROUP BY (datatype(?o) AS ?dt)";
		 QueryEngineHTTP qe = new QueryEngineHTTP("http://dbpedia.org/sparql", query);
		 qe.setDefaultGraphURIs(Collections.singletonList("http://dbpedia.org"));
		 ResultSet rs = qe.execSelect();
		 System.out.println(rs.next());
	}

	
}
