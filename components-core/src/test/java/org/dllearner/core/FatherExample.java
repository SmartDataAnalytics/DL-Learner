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
package org.dllearner.core;

import java.io.File;

import com.google.common.base.StandardSystemProperty;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.dllearner.core.StringRenderer;
import org.dllearner.core.StringRenderer.Rendering;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

/**
 * A basic example how to use DL-Learner.
 * 
 * Knowledge base: a family ontology
 * Target Concept: father
 * 
 * @author Lorenz Buehmann
 *
 */
public class FatherExample {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		StringRenderer.setRenderer(Rendering.MANCHESTER_SYNTAX);
		
		// setup the knowledge base
		File file = new File("../examples/father.owl");
		OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(file);
		AbstractKnowledgeSource ks = new OWLAPIOntology(ontology);
		ks.init();
		
		// setup the reasoner
		ClosedWorldReasoner rc = new ClosedWorldReasoner(ks);
		rc.init();
		
		// setup the learning problem
		OWLClass classToDescribe = new OWLClassImpl(IRI.create("http://example.com/father#male"));
		ClassLearningProblem lp = new ClassLearningProblem(rc);
		lp.setClassToDescribe(classToDescribe);
		lp.init();
		
		// setup the learning algorithm
		CELOE alg = new CELOE(lp, rc);
		alg.setMaxExecutionTimeInSeconds(10);
		alg.setWriteSearchTree(true);
		alg.setSearchTreeFile(System.getProperty("java.io.tmpdir") + File.separator + "dllearner" + File.separator + "search-tree.log");
		alg.setReplaceSearchTree(true);
		alg.init();
		
		// run the learning algorithm
		alg.start();
	}

}
