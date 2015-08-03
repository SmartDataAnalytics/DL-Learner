/**
 * 
 */
package org.dllearner.scripts;

import java.io.File;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.celoe.PCELOE;
import org.dllearner.cli.CLI;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.utilities.owl.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.IRI;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

/**
 * @author Lorenz Buehmann
 *
 */
public class Concurrent {
	
	public static void main(String[] args) throws Exception {
		ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
		Logger.getRootLogger().setLevel(Level.INFO);
		Logger.getLogger(PCELOE.class).setLevel(Level.DEBUG);
		Logger.getLogger(PCELOE.class).addAppender(new FileAppender(new PatternLayout( "[%t] %c: %m%n" ), "log/parallel_run.txt", false));

		CLI cli = new CLI(new File("../examples/carcinogenesis/train.conf"));
		cli.init();
		
		KnowledgeSource ks = cli.getKnowledgeSource();
		ks.init();
		
		AbstractReasonerComponent rc = new ClosedWorldReasoner(ks);
		rc.init();

		AbstractClassExpressionLearningProblem lp = cli.getLearningProblem();
		lp.init();
		
		int maxExecutionTimeInSeconds = 10;
//
		CELOE alg1 = new CELOE(lp, rc);
		alg1.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		alg1.init();
		alg1.start();
		
//		PCELOE alg = new PCELOE(lp, rc);
//		alg.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
//		alg.setNrOfThreads(1);
////		alg.setMaxClassDescriptionTests(200);
//		alg.init();
//
//		alg.start();
	}

}
