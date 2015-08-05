/**
 * 
 */
package org.dllearner.scripts;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

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
import org.semanticweb.owlapi.model.OWLIndividual;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

/**
 * @author Lorenz Buehmann
 *
 */
public class Concurrent {
	
	public static void main(String[] args) throws Exception {
//		int i1 = 30000;
//		int i2 = 100;
//		Set<OWLIndividual> ind1 = new TreeSet<>();
//		for(int i = 1; i < i1; i++) {
//			ind1.add(new OWLNamedIndividualImpl(IRI.create("http://example.org#" + i)));
//		}
//		Set<OWLIndividual> ind2 = new TreeSet<>();
//		for(int i = 1; i < i2; i++) {
//			ind2.add(new OWLNamedIndividualImpl(IRI.create("http://example.org#" + i)));
//		}
//		long start = System.currentTimeMillis();
//		SetView<OWLIndividual> diff = Sets.difference(ind1, ind2);
//		Set<OWLIndividual> result = new TreeSet<OWLIndividual>(diff);
//		long end = System.currentTimeMillis();
//		System.out.println("Operation took " + (end - start) + "ms");
//		ind1 = new HashSet<OWLIndividual>(ind1);
//		ind2 = new HashSet<OWLIndividual>(ind2);
//		start = System.currentTimeMillis();
//		diff = Sets.difference(ind1, ind2);
//		
//		end = System.currentTimeMillis();
//		System.out.println("Operation took " + (end - start) + "ms");
		
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
//		CELOE alg1 = new CELOE(lp, rc);
//		alg1.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
//		alg1.init();
//		alg1.start();
		
		PCELOE alg = new PCELOE(lp, rc);
		alg.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		alg.setNrOfThreads(1);
//		alg.setMaxClassDescriptionTests(200);
		alg.init();

		alg.start();
	}

}
