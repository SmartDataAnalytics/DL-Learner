package org.dllearner.test;

import java.io.File;
import java.net.MalformedURLException;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.semanticweb.owlapi.model.IRI;

public class ClassCastExceptionTest {

	/**
	 * @param args
	 * @throws ComponentInitException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws ComponentInitException, MalformedURLException {
				
		// create knowledge source
		String example = "examples/swore/swore.rdf";
		AbstractKnowledgeSource source = new OWLFile(example);
		
		// create OWL API reasoning service with standard settings
		AbstractReasonerComponent reasoner = new ClosedWorldReasoner(source);
		reasoner.init();
		
		// create a learning problem and set positive and negative examples

		ClassLearningProblem lp = new ClassLearningProblem(reasoner);
		lp.setClassToDescribe(IRI.create("http://ns.softwiki.de/req/PerformanceRequirement"));
		lp.setEquivalence(true);
		lp.init();
		
		// create the learning algorithm
		AbstractCELA la = null;
		CELOE cla = new CELOE(lp, reasoner);
		cla.setMaxExecutionTimeInSeconds(2);
		la = cla;
		la.init();
	
		// start the algorithm and print the best concept found
		la.start();
		System.out.println(la.getCurrentlyBestEvaluatedDescriptions(10, 0.8, true));
		EvaluatedDescription desc = la.getCurrentlyBestEvaluatedDescription();
		System.out.println("test: " + ((EvaluatedDescriptionPosNeg)desc).getNotCoveredPositives());
	}
}
