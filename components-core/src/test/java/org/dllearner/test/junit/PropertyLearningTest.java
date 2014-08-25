/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

import junit.framework.TestCase;

import org.dllearner.algorithms.properties.DisjointDataPropertyAxiomLearner;
import org.dllearner.algorithms.properties.EquivalentDataPropertyAxiomLearner;
import org.dllearner.algorithms.properties.EquivalentObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.FunctionalObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.ObjectPropertyDomainAxiomLearner2;
import org.dllearner.algorithms.properties.ObjectPropertyRangeAxiomLearner;
import org.dllearner.algorithms.properties.ReflexiveObjectPropertyAxiomLearner;
import org.dllearner.algorithms.properties.SubObjectPropertyOfAxiomLearner;
import org.dllearner.algorithms.properties.SymmetricObjectPropertyAxiomLearner;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class PropertyLearningTest extends TestCase{
	
	private SparqlEndpointKS ks;
	private int maxExecutionTimeInSeconds = 3;
	private int nrOfAxioms = 3;
	
	OWLDataFactory df = new OWLDataFactoryImpl();
	private OWLObjectProperty functional = df.getOWLObjectProperty(IRI.create( "http://dbpedia.org/ontology/league"));
	private OWLObjectProperty reflexive = df.getOWLObjectProperty(IRI.create( "http://dbpedia.org/ontology/influencedBy"));
	private OWLObjectProperty symmetric = df.getOWLObjectProperty(IRI.create( "http://dbpedia.org/ontology/influencedBy"));
	private OWLObjectProperty domain = df.getOWLObjectProperty(IRI.create( "http://dbpedia.org/ontology/writer"));
	private OWLObjectProperty range = df.getOWLObjectProperty(IRI.create( "http://dbpedia.org/ontology/writer"));
	private OWLObjectProperty subProperty = df.getOWLObjectProperty(IRI.create( "http://dbpedia.org/ontology/author"));
	private OWLObjectProperty equivProperty = df.getOWLObjectProperty(IRI.create( "http://dbpedia.org/ontology/academyAward"));
	
	private OWLDataProperty disDataProperty = df.getOWLDataProperty(IRI.create( "http://dbpedia.org/ontology/height"));
	private OWLDataProperty equivDataProperty = df.getOWLDataProperty(IRI.create( "http://dbpedia.org/ontology/height"));
	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ks = new SparqlEndpointKS(SparqlEndpoint.getEndpointDBpedia());
	}
	
	/*
	 * object property axioms
	 */
	
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
		ObjectPropertyDomainAxiomLearner2 l = new ObjectPropertyDomainAxiomLearner2(ks);
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
	
	/*
	 * data property axioms
	 */
	
	public void testEquivalentDataPropertiesAxiomLearning() throws Exception {
		EquivalentDataPropertyAxiomLearner l = new EquivalentDataPropertyAxiomLearner(ks);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.setPropertyToDescribe(equivDataProperty);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(nrOfAxioms));
	}
	
	public void testDisjointDataPropertiesAxiomLearning() throws Exception {
		DisjointDataPropertyAxiomLearner l = new DisjointDataPropertyAxiomLearner(ks);
		l.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		l.setPropertyToDescribe(disDataProperty);
		l.init();
		l.start();
		System.out.println(l.getCurrentlyBestEvaluatedAxioms(nrOfAxioms));
	}

}
