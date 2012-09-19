/**
 * Copyright (C) 2007-2011, Jens Lehmann
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.**/
package org.dllearner.scripts;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.Files;

import com.jamonapi.MonitorFactory;

/**
 * Sample script showing how to use DL-Learner. Provides an entry point for tool
 * developers.
 * 
 * @author Sebastian Hellmann
 * @author Jens Lehmann
 * 
 */
public class Sample {

	private static Logger logger = Logger.getRootLogger();
	private static DecimalFormat df = new DecimalFormat();

	public static void main(String[] args) throws IOException, ComponentInitException,
			LearningProblemUnsupportedException {

		// create logger (configure this to your needs)
		SimpleLayout layout = new SimpleLayout();
		FileAppender fileAppender = new FileAppender(layout, "log/sample_log.txt", false);

		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.addAppender(fileAppender);
		logger.setLevel(Level.DEBUG);

		// OWL file containing background knowledge
		String owlFile = "../examples/trains/trains.owl";

		// examples
		SortedSet<Individual> posExamples = new TreeSet<Individual>();
		posExamples.add(new Individual("http://example.com/foo#east1"));
		posExamples.add(new Individual("http://example.com/foo#east2"));
		posExamples.add(new Individual("http://example.com/foo#east3"));
		posExamples.add(new Individual("http://example.com/foo#east4"));
		posExamples.add(new Individual("http://example.com/foo#east5"));

		SortedSet<Individual> negExamples = new TreeSet<Individual>();
		negExamples.add(new Individual("http://example.com/foo#west6"));
		negExamples.add(new Individual("http://example.com/foo#west7"));
		negExamples.add(new Individual("http://example.com/foo#west8"));
		negExamples.add(new Individual("http://example.com/foo#west9"));
		negExamples.add(new Individual("http://example.com/foo#west10"));

		List<? extends EvaluatedDescription> results = learn(owlFile, posExamples, negExamples, 5);
		int x = 0;
		for (EvaluatedDescription ed : results) {
			System.out.println("solution: " + x);
			System.out.println("  description: \t"
					+ ed.getDescription().toManchesterSyntaxString(null, null));
			System.out.println("  accuracy: \t" + df.format(((EvaluatedDescriptionPosNeg)ed).getAccuracy() * 100) + "%");
			System.out.println();
			x++;
		}

		Files.createFile(new File("log/jamon_sample.html"), MonitorFactory.getReport());
	}

	public static List<? extends EvaluatedDescription> learn(String owlFile, SortedSet<Individual> posExamples,
			SortedSet<Individual> negExamples, int maxNrOfResults) throws ComponentInitException,
			LearningProblemUnsupportedException {

		logger.info("Start Learning with");
		logger.info("positive examples: \t" + posExamples.size());
		logger.info("negative examples: \t" + negExamples.size());

		// use the specified OWL file
		OWLFile ks = new OWLFile(owlFile);
		ks.init();
		
		// load file into reasoner
		FastInstanceChecker r = new FastInstanceChecker();
		r.setSources(ks);
		r.init();
		
		// configure learning problem
		PosNegLPStandard lp = new PosNegLPStandard();
		lp.setReasoner(r);
		lp.setPositiveExamples(posExamples);
		lp.setNegativeExamples(negExamples);
		lp.init();
		
		// configure target language
		RhoDRDown rho = new RhoDRDown();
		rho.setReasoner(r);
		rho.setUseAllConstructor(false);
		rho.setUseExistsConstructor(true);
		rho.setUseNegation(false);
		rho.setUseCardinalityRestrictions(false);
		rho.init();
		
		// configure learning algorithm
		CELOE la = new CELOE();
		la.setReasoner(r);
		la.setLearningProblem(lp);
		la.setWriteSearchTree(false);
		la.setSearchTreeFile("log/searchTree.txt");
		la.setReplaceSearchTree(true);
		la.setNoisePercentage(20.0);
		la.setMaxExecutionTimeInSeconds(2);
		la.init();
		
		// start learning algorithm
		logger.debug("start learning");
		la.start();
		
		return la.getCurrentlyBestEvaluatedDescriptions(maxNrOfResults);
	}

}
