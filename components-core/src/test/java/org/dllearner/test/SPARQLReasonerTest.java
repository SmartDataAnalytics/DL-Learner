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

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.StringRenderer;
import org.dllearner.core.StringRenderer.Rendering;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.SPARQLReasoner;
import org.dllearner.refinementoperators.RhoDRDown;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLIndividual;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import java.util.SortedSet;

/**
 * @author Lorenz Buehmann
 *
 */
public class SPARQLReasonerTest {

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
