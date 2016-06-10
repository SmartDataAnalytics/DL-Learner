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
package org.dllearner.test;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import org.apache.jena.riot.RDFDataMgr;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.StringRenderer;
import org.dllearner.core.StringRenderer.Rendering;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.OWLAPIUtils;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.Set;
import java.util.SortedSet;

/**
 * @author Lorenz Buehmann
 *
 */
public class SPARQLReasonerTest {

	@Test
	public void testPropertyPath2() throws ComponentInitException {
		boolean tryPropertyPaths=true;
		Model m = ModelFactory.createDefaultModel();
		final String NS = "http://dllearner.test/";
		Resource cl1 = ResourceFactory.createResource(NS+"Cl1");
		Resource cl2 = ResourceFactory.createResource(NS+"Cl2");
		Resource cl3 = ResourceFactory.createResource(NS+"Cl3");
		Resource ind1 = ResourceFactory.createResource(NS+"ind1");
		Resource ind2 = ResourceFactory.createResource(NS+"ind2");
		Resource ind3 = ResourceFactory.createResource(NS+"ind3");
		m.add(ind1, RDF.type, OWL2.NamedIndividual);
		m.add(ind1, RDF.type, cl1);
		m.add(ind2, RDF.type, cl2);
		m.add(ind3, RDF.type, cl3);
		m.add(cl1, RDF.type, OWL.Class);
		m.add(cl2, RDFS.subClassOf, cl1);
		m.add(cl3, RDFS.subClassOf, cl1);
		if (!tryPropertyPaths) {
			m.add(cl2, RDF.type, OWL.Class);
			m.add(cl3, RDF.type, OWL.Class);
		}
		SparqlEndpointKS ks = new LocalModelBasedSparqlEndpointKS(m,false);
		ks.init();
		SPARQLReasoner sparqlReasoner = new SPARQLReasoner(ks);
		sparqlReasoner.tryPropertyPath=tryPropertyPaths;
		sparqlReasoner.init();
		AbstractReasonerComponent rc = sparqlReasoner;

		OWLDataFactory owlDataFactory = new OWLDataFactoryImpl();
		Set<OWLClass> classes = rc.getClasses();
		System.out.println("classes = " + classes);
		SortedSet<OWLIndividual> individuals1 = rc.getIndividuals(OWLAPIUtils.fromManchester("Cl1", rc, owlDataFactory, true));
		System.out.println("Cl1="+individuals1);
		SortedSet<OWLIndividual> individuals2 = rc.getIndividuals(OWLAPIUtils.fromManchester("Cl2", rc, owlDataFactory, true));
		System.out.println("Cl2="+individuals2);
		SortedSet<OWLIndividual> individuals3 = rc.getIndividuals(OWLAPIUtils.fromManchester("Cl3", rc, owlDataFactory, true));
		System.out.println("Cl3="+individuals3);
		SortedSet<OWLIndividual> individuals4 = rc.getIndividuals(OWLAPIUtils.fromManchester("Cl1 and not Cl2", rc, owlDataFactory, true));
		System.out.println("Cl1 and not Cl2="+individuals4);
	}

	@Test
	public void testPropertyPathOP() throws ComponentInitException {
		boolean tryPropertyPaths=true;
		Model m = ModelFactory.createDefaultModel();
		final String NS = "http://dllearner.test/";
		final Resource cl1 = ResourceFactory.createResource(NS+"Cl1");
		final Resource cl2 = ResourceFactory.createResource(NS+"Cl2");
		final Resource ind1 = ResourceFactory.createResource(NS+"ind1");
		final Resource ind2 = ResourceFactory.createResource(NS+"ind2");
		final Resource o1 = ResourceFactory.createResource(NS+"obj1");
		final Resource o2 = ResourceFactory.createResource(NS+"obj2");
		final Property p1 = m.getProperty(NS + "p1");
		final Property p2 = m.getProperty(NS + "p2");


		m.add(ind1, RDF.type, OWL2.NamedIndividual);
		m.add(ind2, RDF.type, OWL2.NamedIndividual);
		m.add(ind1, RDF.type, cl1);
		m.add(ind2, RDF.type, cl2);
		m.add(cl1, RDF.type, OWL.Class);
		m.add(cl2, RDF.type, OWL.Class);
		m.add(cl2, RDFS.subClassOf, cl1);
		m.add(ind1, p1, o1);
		m.add(ind2, p2, o2);

		SparqlEndpointKS ks = new LocalModelBasedSparqlEndpointKS(m,false);
		ks.init();
		SPARQLReasoner sparqlReasoner = new SPARQLReasoner(ks);
		sparqlReasoner.tryPropertyPath=tryPropertyPaths;
		sparqlReasoner.init();
		AbstractReasonerComponent rc = sparqlReasoner;

		OWLDataFactory df = new OWLDataFactoryImpl();
		Set<OWLClass> classes = rc.getClasses();
		System.out.println("classes = " + classes);
		SortedSet<OWLIndividual> individuals1 = rc.getIndividuals(OWLAPIUtils.fromManchester("Cl1", rc, df, true));
		System.out.println("Cl1="+individuals1);
		SortedSet<OWLIndividual> individuals2 = rc.getIndividuals(OWLAPIUtils.fromManchester("Cl2", rc, df, true));
		System.out.println("Cl2="+individuals2);
		System.out.println("Ops1" + sparqlReasoner.getObjectProperties(df.getOWLClass(IRI.create(cl1.getURI()))));
		System.out.println("Ops2" + sparqlReasoner.getObjectProperties(df.getOWLClass(IRI.create(cl2.getURI()))));
	}
/*
	@Test
	public void testPropertyPath() throws MalformedURLException, ComponentInitException {
		StringRenderer.setRenderer(Rendering.DL_SYNTAX);

		SparqlEndpointKS ks = new SparqlEndpointKS();
		ks.setUrl(new URL("http://sake.informatik.uni-leipzig.de:8850/sparql"));
		ks.setDefaultGraphURIs(Collections.singletonList("http://sake-projekt.de"));
		ks.setUseCache(false);
		ks.init();
		SPARQLReasoner sparqlReasoner = new SPARQLReasoner(ks);
		sparqlReasoner.init();
		AbstractReasonerComponent rc = sparqlReasoner;

		OWLDataFactory owlDataFactory = new OWLDataFactoryImpl();
		Set<OWLClass> classes = rc.getClasses();
		//System.out.println("classes = " + classes);
		SortedSet<OWLIndividual> individuals = rc.getIndividuals(OWLAPIUtils.fromManchester("FailureData", rc, owlDataFactory, true));
		System.out.println(individuals);
		SortedSet<OWLIndividual> failures = rc.getIndividuals(OWLAPIUtils.fromManchester("containsFailureData some FailureData_999999", rc, owlDataFactory, true));
		System.out.println(failures);
		//rc.getTypes()
	}
*/
	public static void main(String[] args) throws Exception{
		StringRenderer.setRenderer(Rendering.DL_SYNTAX);
		
		Model model = RDFDataMgr.loadModel("../examples/swore/swore.rdf");
		LocalModelBasedSparqlEndpointKS ks = new LocalModelBasedSparqlEndpointKS(model, true);
//		ks.setEnableReasoning(true);
		ks.init();
		
		AbstractReasonerComponent rc = new SPARQLReasoner(ks);
//		AbstractReasonerComponent rc = new SPARQLReasoner(new SparqlEndpointKS(new SparqlEndpoint(
//				new URL("http://localhost:8890/sparql"), "http://family-benchmark.owl")));
		rc.setUseInstanceChecks(false);
        rc.init();
        
        OWLClass classToDescribe = new OWLClassImpl(IRI.create("http://ns.softwiki.de/req/CustomerRequirement"));
//        OWLClass classToDescribe = new OWLClassImpl(IRI.create("http://www.benchmark.org/family#Female"));
        SortedSet<OWLIndividual> posExamples = rc.getIndividuals(classToDescribe);
        SortedSet<OWLIndividual> negExamples = rc.getIndividuals();
        negExamples.removeAll(posExamples);
		
        RhoDRDown op = new RhoDRDown();
        op.setReasoner(rc);
        op.setUseAllConstructor(false);
        op.setUseHasValueConstructor(false);
        op.setUseNegation(false);
        op.init();
		
//		PosNegLP lp = new PosNegLPStandard(rc);
//		lp.setPositiveExamples(posExamples);
//		lp.setNegativeExamples(negExamples);
//		lp.init();
		
		ClassLearningProblem lp = new ClassLearningProblem(rc);
		lp.setClassToDescribe(classToDescribe);
		lp.init();
		
		CELOE alg = new CELOE(lp, rc);
		alg.setOperator(op);
		alg.setMaxExecutionTimeInSeconds(60);
		alg.setWriteSearchTree(true);
		alg.setSearchTreeFile("log/search-tree.log");
		alg.setReplaceSearchTree(true);
		alg.init();
		
		alg.start();
	}

}
