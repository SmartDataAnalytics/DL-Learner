/**
 * 
 */
package org.dllearner.core;

import java.io.File;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

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
		ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
		
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
		alg.setSearchTreeFile("/tmp/dllearner/search-tree.log");
		alg.setReplaceSearchTree(true);
		alg.init();
		
		// run the learning algorithm
		alg.start();
	}

}
