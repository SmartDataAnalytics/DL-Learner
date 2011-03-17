package org.dllearner.test;

import java.io.File;
import java.net.MalformedURLException;

import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.reasoning.FastInstanceChecker;

public class ClassCastExceptionTest {

	/**
	 * @param args
	 * @throws ComponentInitException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws ComponentInitException, MalformedURLException {
		
		// get singleton instance of component manager
		ComponentManager cm = ComponentManager.getInstance();
		
		// create knowledge source
		KnowledgeSource source = cm.knowledgeSource(OWLFile.class);
		String example = "examples/swore/swore.rdf";
		cm.applyConfigEntry(source, "url", new File(example).toURI().toURL());
		source.init();
		
		// create OWL API reasoning service with standard settings
		ReasonerComponent reasoner = cm.reasoner(FastInstanceChecker.class, source);
		reasoner.init();
		
		// create a learning problem and set positive and negative examples

		LearningProblem lp = cm.learningProblem(ClassLearningProblem.class, reasoner);
		cm.applyConfigEntry(lp, "classToDescribe", "http://ns.softwiki.de/req/PerformanceRequirement");
		cm.applyConfigEntry(lp, "type", "equivalence");
		lp.init();
		
		// create the learning algorithm
		AbstractCELA la = null;
		try {
			la = cm.learningAlgorithm(CELOE.class, lp, reasoner);
			cm.applyConfigEntry(la, "maxExecutionTimeInSeconds", 2);
			la.init();
		} catch (LearningProblemUnsupportedException e) {
			e.printStackTrace();
		}
	
		// start the algorithm and print the best concept found
		la.start();
		System.out.println(la.getCurrentlyBestEvaluatedDescriptions(10, 0.8, true));
		EvaluatedDescription desc = la.getCurrentlyBestEvaluatedDescription();
		System.out.println("test: " + ((EvaluatedDescriptionPosNeg)desc).getNotCoveredPositives());
	}
}
