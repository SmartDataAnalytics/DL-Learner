/**
 * 
 */
package org.dllearner.test;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.semanticweb.elk.owlapi.ElkReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Check different reasoner implementations.
 * @author Lorenz Buehmann
 *
 */
public class ReasonerTests {
	
	public static void main(String[] args) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		Logger.getLogger(ElkReasoner.class).setLevel(Level.WARN);
		
		String[] reasonerImplementations = new String[]{"elk"};//, "trowl", "fact", "hermit", "pellet"};
		
		String ontologyURL = "../examples/swore/swore.rdf";
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = man.loadOntologyFromOntologyDocument(new File(ontologyURL));
		
		KnowledgeSource ks = new OWLAPIOntology(ontology);
		ks.init();
		
		NamedClass classToDescribe = new NamedClass("http://ns.softwiki.de/req/CustomerRequirement");
		
		for (String reasonerImpl : reasonerImplementations) {
			System.out.println("Testing " + reasonerImpl);
			
			try {
				OWLAPIReasoner reasoner = new OWLAPIReasoner(ks);
				reasoner.setReasonerTypeString(reasonerImpl);
				reasoner.setUseFallbackReasoner(true);
				reasoner.init();
				
				ClassLearningProblem lp = new ClassLearningProblem(reasoner);
				lp.setClassToDescribe(classToDescribe);
				lp.init();
				
				CELOE la = new CELOE(lp, reasoner);
				la.setMaxExecutionTimeInSeconds(10);
				la.init();
				
				la.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

}
