/**
 * 
 */
package org.dllearner.test;

import java.io.File;
import java.util.List;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.FastInstanceChecker;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

/**
 * Simple test class that learns a description of a given class.
 * @author Lorenz Buehmann
 *
 */
public class ClassLearningProblemExample {
	
	public static void main(String[] args) throws Exception {
		// load a knowledge base
		String ontologyPath = "../examples/swore/swore.rdf";
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = man.loadOntologyFromOntologyDocument(new File(ontologyPath));
		AbstractKnowledgeSource source = new OWLAPIOntology(ontology);
		source.init();
		
		// set up a closed-world reasoner
		AbstractReasonerComponent reasoner = new FastInstanceChecker(source);
		reasoner.init();
		
		// create a learning problem and set the class to describe
		ClassLearningProblem lp = new ClassLearningProblem(reasoner);
		lp.setClassToDescribe(new OWLClassImpl(IRI.create("http://ns.softwiki.de/req/CustomerRequirement")));
		lp.init();
		
		// create the learning algorithm
		AbstractCELA la = new CELOE(lp, reasoner);
		la.init();
	
		// start the algorithm and print the best concept found
		la.start();
		List<? extends EvaluatedDescription> currentlyBestEvaluatedDescriptions = la.getCurrentlyBestEvaluatedDescriptions(0.8);
		System.out.println(currentlyBestEvaluatedDescriptions);
	}

}
