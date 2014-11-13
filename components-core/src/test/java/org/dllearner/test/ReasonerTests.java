/**
 * 
 */
package org.dllearner.test;

import java.io.File;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.FastInstanceChecker;
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
		Logger.getRootLogger().setLevel(Level.OFF);
		Logger.getLogger(ElkReasoner.class).setLevel(Level.OFF);
		
		String[] reasonerImplementations = new String[]{"elk", "trowl", "fact", "hermit", "pellet"};
		
		int maxExecutionTimeInSeconds = 10;
		
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
				Logger.getLogger(ElkReasoner.class).setLevel(Level.OFF);
				
				FastInstanceChecker closedWorldReasoner = new FastInstanceChecker(ks);
				closedWorldReasoner.setReasonerComponent(reasoner);
				closedWorldReasoner.init();
				
				ClassLearningProblem lp = new ClassLearningProblem(closedWorldReasoner);
				lp.setClassToDescribe(classToDescribe);
				lp.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
				lp.init();
				
				CELOE la = new CELOE(lp, closedWorldReasoner);
				la.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
				la.init();
				
				la.start();
			} catch (Exception e) {
				e.printStackTrace();
			} catch (Error e) {
				e.printStackTrace();
			}
		}
		
	}

}
