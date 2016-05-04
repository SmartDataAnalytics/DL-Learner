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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.dllearner.algorithms.el.ELLearningAlgorithm;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ClassBasedSampleGenerator;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerImplementation;
import org.dllearner.refinementoperators.RhoDRDown;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.dllearner.core.StringRenderer;
import org.dllearner.core.StringRenderer.Rendering;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.collect.Sets;

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
		StringRenderer.setRenderer(Rendering.DL_SYNTAX);
		
		SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
//		endpoint = new SparqlEndpoint(new URL("http://sake.informatik.uni-leipzig.de:8890/sparql"), "http://dbpedia.org");
		
		Set<String> ignoredProperties = Sets.newHashSet(
				"http://dbpedia.org/ontology/wikiPageRevisionID",
				"http://dbpedia.org/ontology/wikiPageID",
				"http://dbpedia.org/ontology/abstract",
				"http://dbpedia.org/ontology/alias"
				,"http://dbpedia.org/ontology/number"
				,"http://dbpedia.org/ontology/endowment"
				);
		
		// the class to describe
		OWLClass cls = new OWLClassImpl(IRI.create("http://dbpedia.org/ontology/SoccerPlayer"));
		
		// (optionally) provide the schema
		String ontologyURL = "http://mappings.dbpedia.org/server/ontology/dbpedia.owl";
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLDataFactory df = man.getOWLDataFactory();
		OWLOntology schemaOntology = man.loadOntology(IRI.create(ontologyURL));
		// OWL API does not support rdf:langString so far
		Set<OWLDataPropertyRangeAxiom> rangeAxioms = schemaOntology.getAxioms(AxiomType.DATA_PROPERTY_RANGE);
		Set<OWLAxiom> toRemove = new HashSet<>();
		Set<OWLAxiom> toAdd = new HashSet<>();
		for (OWLDataPropertyRangeAxiom ax : rangeAxioms) {
			OWLDatatype datatype = ax.getRange().asOWLDatatype();
			if(datatype.equals(df.getOWLDatatype(IRI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString")))) {
				toRemove.add(ax);
//				toAdd.add(df.getOWLDataPropertyRangeAxiom(ax.getProperty(), df.getOWLDatatype(IRI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#PlainLiteral"))));
			}
			toRemove.add(ax);
		}
		man.removeAxioms(schemaOntology, toRemove);
		man.addAxioms(schemaOntology, toAdd);
		// remove functionality axioms because otherwise inconsistency can occur
		man.removeAxioms(schemaOntology, schemaOntology.getAxioms(AxiomType.FUNCTIONAL_DATA_PROPERTY));
		man.removeAxioms(schemaOntology, schemaOntology.getAxioms(AxiomType.DISJOINT_CLASSES));
		
		KnowledgeSource schemaKS = new OWLAPIOntology(schemaOntology);
		schemaKS.init();
		
		// setup the dataset
		SparqlEndpointKS ks = new SparqlEndpointKS(endpoint, schemaKS);
		ks.init();
		
		// extract sample of the knowledge base
		ClassBasedSampleGenerator sampleGen = new ClassBasedSampleGenerator(ks);
		sampleGen.addAllowedPropertyNamespaces(Sets.newHashSet("http://dbpedia.org/ontology/"));
		sampleGen.addIgnoredProperties(ignoredProperties);
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
		baseReasoner.setReasonerImplementation(ReasonerImplementation.PELLET);
		baseReasoner.init();
		ClosedWorldReasoner reasoner = new ClosedWorldReasoner(baseReasoner);
		reasoner.init();
		
		// setup learning problem
		ClassLearningProblem lp = new ClassLearningProblem(reasoner);
		lp.setClassToDescribe(cls);
		lp.init();
		
		// setup refinement operator
		RhoDRDown op = new RhoDRDown();
		op.setUseTimeDatatypes(false);
		op.setReasoner(reasoner);
		op.setUseNegation(false);
		op.init();
		
		// setup learning algorithm
//		final AbstractCELA alg;
//		
//		CELOE celoe = new CELOE(lp, reasoner);
//		celoe.setOperator(op);
//		celoe.setWriteSearchTree(true);
//		celoe.setSearchTreeFile("/tmp/searchtree-celoe.txt");
//		celoe.setReplaceSearchTree(true);
//		celoe.setMaxExecutionTimeInSeconds(10);
//		celoe.setNoisePercentage(60);
//		celoe.init();
//		alg = celoe;
		
		PosNegLP lp2 = new PosNegLPStandard(reasoner);
		lp2.setPositiveExamples(sampleGen.getPositiveExamples());
		lp2.setNegativeExamples(sampleGen.getNegativeExamples());
		lp2.init();
		ELLearningAlgorithm el = new ELLearningAlgorithm(lp2, reasoner);
		el.setMaxExecutionTimeInSeconds(10);
//		el.setStartClass(cls);
		el.setClassToDescribe(cls);
		el.setNoisePercentage(70);
		el.init();
		final AbstractCELA alg = el;
		
		TimerTask t = new TimerTask() {
			
			@Override
			public void run() {
				System.out.println("T:" + alg.getCurrentlyBestEvaluatedDescriptions(10, 0.5, true));
				
			}
		};
		Timer timer = new Timer();
		timer.schedule(t, 1000, 1000);
		
		// run
		alg.start();
		
//		Set<OWLClassExpression> refinements = op.refine(new OWLClassImpl(IRI.create("http://dbpedia.org/ontology/Work")), 5);
//		for (OWLClassExpression ref : refinements) {
//			System.out.println(ref + ":" + lp.getAccuracy(ref, 1.0));
//		}
		
		timer.cancel();
		List<? extends EvaluatedDescription> solutions = alg.getCurrentlyBestEvaluatedDescriptions(10, 0.5, true);
		
		System.out.println(solutions);
	}

}
