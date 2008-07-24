/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithms.refexamples.ExampleBasedROLComponent;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.owl.Description;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.datastructures.SetManipulation;

// TODO COMMENT !!! added a sample java call class for the dl-learner (as a
// possible entry point for tool developers)
public class Sample {

	private static Logger logger = Logger.getRootLogger();

	String owlfile = "";

	// examples
	SortedSet<String> posExamples = new TreeSet<String>();

	SortedSet<String> negExamples = new TreeSet<String>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Start");

		// create logger (a simple logger which outputs
		// its messages to the console)
		SimpleLayout layout = new SimpleLayout();
		FileAppender fileAppender = null;
		;
		try {
			fileAppender = new FileAppender(layout, "the_log.txt", false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.addAppender(fileAppender);
		logger.setLevel(Level.DEBUG);

		Sample s = new Sample();

		s.owlfile = "examples/trains/trains.owl";

		s.posExamples = new TreeSet<String>();
		s.negExamples = new TreeSet<String>();

		/* Examples */
		s.posExamples.add("http://example.com/foo#east1");
		s.posExamples.add("http://example.com/foo#east2");
		s.posExamples.add("http://example.com/foo#east3");
		s.posExamples.add("http://example.com/foo#east4");
		s.posExamples.add("http://example.com/foo#east5");

		s.negExamples.add("http://example.com/foo#west6");
		s.negExamples.add("http://example.com/foo#west7");
		s.negExamples.add("http://example.com/foo#west8");
		s.negExamples.add("http://example.com/foo#west9");
		s.negExamples.add("http://example.com/foo#west10");

		List<Description> conceptresults = s.learn();
		int x = 0;
		for (Description description : conceptresults) {
			if (x >= 5)
				break;
			System.out
					.println(description.toManchesterSyntaxString(null, null));
			x++;
		}

		System.out.println("Finished");
		JamonMonitorLogger.printAllSortedByLabel();

	}

	public List<Description> learn() {

		logger.info("Start Learning with");
		logger.info("positive examples: \t" + posExamples.size());
		logger.info("negative examples: \t" + negExamples.size());

		// Components
		ComponentManager cm = ComponentManager.getInstance();
		LearningAlgorithm la = null;
		ReasoningService rs = null;
		LearningProblem lp = null;
		KnowledgeSource ks = null;

		try {
			Set<KnowledgeSource> sources = new HashSet<KnowledgeSource>();
			ks = cm.knowledgeSource(OWLFile.class);

			// there are probably better ways, but this works
			File f = new File(this.owlfile);
			URL url = null;
			try {
				url = new URL("file://" + f.getAbsolutePath());

			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			cm.applyConfigEntry(ks, "url", url.toString());

			ReasonerComponent r = new FastInstanceChecker(sources);
			rs = new ReasoningService(r);
			// System.out.println("satisfy: "+rs.isSatisfiable());

			lp = new PosNegDefinitionLP(rs);
			lp = new PosNegDefinitionLP(rs);

			// This method is a workaround, it should be like the two commented
			// lines below
			((PosNegLP) lp).setPositiveExamples(SetManipulation
					.stringToInd(this.posExamples));
			((PosNegLP) lp).setNegativeExamples(SetManipulation
					.stringToInd(this.negExamples));
			// cm.applyConfigEntry(lp,"positiveExamples",this.posExamples);
			// cm.applyConfigEntry(lp,"negativeExamples",this.negExamples);

			la = cm.learningAlgorithm(ExampleBasedROLComponent.class, lp, rs);

			logger.debug("start learning");

			// KNOWLEDGESOURCE
			//

			// LEARNINGALGORITHM
			cm.applyConfigEntry(la, "useAllConstructor", false);
			cm.applyConfigEntry(la, "useExistsConstructor", true);
			cm.applyConfigEntry(la, "useCardinalityRestrictions", false);
			cm.applyConfigEntry(la, "useNegation", false);

			cm.applyConfigEntry(la, "writeSearchTree", false);
			cm.applyConfigEntry(la, "searchTreeFile", "log/searchtree.txt");
			cm.applyConfigEntry(la, "replaceSearchTree", true);
			// cm.applyConfigEntry(la,"noisePercentage",noise);

			/*
			 * if(ignoredConcepts.size()>0)
			 * cm.applyConfigEntry(la,"ignoredConcepts",ignoredConcepts);
			 */

			// initialization
			ks.init();
			sources.add(ks);
			r.init();
			lp.init();
			la.init();

			la.start();
			// Statistics.addTimeCollecting(sc.getTime());
			// Statistics.addTimeLearning(sc.getTime());

			return la.getCurrentlyBestDescriptions();
			// return la.getCurrentlyBestEvaluatedDescriptions();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

}
