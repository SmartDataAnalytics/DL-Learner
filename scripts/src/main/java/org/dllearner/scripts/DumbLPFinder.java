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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.dllearner.scripts;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.configurators.ComponentFactory;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.reasoning.ReasonerType;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.components.ReasonerComponentFactory;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.dllearner.utilities.learn.ConfWriter;
import org.dllearner.utilities.statistics.Stat;

// TODO: Write something about the use of this class here.
public class DumbLPFinder {

	private static Logger logger = Logger.getRootLogger();

	private static AbstractReasonerComponent reasoningService;

	private static String ontologyPath = "examples/semantic_bible/NTNcombined.owl";

	private static int numberOfLearningProblems = 100;

	private static String baseDir = "sembib/";

	private static String exampleBaseDir = baseDir + "examples/";

	private static boolean allOrExists = true;

	private static boolean tenORthirty = true;

	private static boolean DEBUG = false;

	// private static boolean allOrExists = true;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		initLogger();
		logger.info("started");
		Stat acc = new Stat();
		// String fileURL = new File(ontologyFile).toURI().toString();

		reasoningService = ReasonerComponentFactory.getReasonerComponent(
				ontologyPath, ReasonerType.OWLAPI_PELLET);

		Files.mkdir(baseDir);
		
		Files.mkdir(exampleBaseDir);

		SortedSet<Individual> allIndividuals = new TreeSet<Individual>();
		allIndividuals.addAll(reasoningService.getIndividuals());

		//reasoningService = null;
		ComponentManager.getInstance().freeAllComponents();

		int count = 0;
		while (count < numberOfLearningProblems) {

			int exampleSize = (tenORthirty) ? 10 : 30;
			int half = (tenORthirty) ? 5 : 15;

			String filename = "";
			filename += (allOrExists) ? "all_" : "exists_";
			filename += (tenORthirty) ? "ten_" : "thirty_";

			try {

				SortedSet<Individual> tmp = SetManipulation.fuzzyShrinkInd(
						allIndividuals, exampleSize);

				SortedSet<Individual> positiveEx = new TreeSet<Individual>();
				SortedSet<Individual> negativeEx = new TreeSet<Individual>();

				for (Individual individual : tmp) {
					if (positiveEx.size() < half) {
						positiveEx.add(individual);
					} else {
						negativeEx.add(individual);
					}

				}

				EvaluatedDescriptionPosNeg d;

				d = learnSPARQL(positiveEx, negativeEx);
				acc.addNumber(d.getAccuracy());
				logger.warn("onFragment"+d.getAccuracy());
				
				//logger.warn();

				writeFiles(filename, d, positiveEx, negativeEx);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				ComponentManager.getInstance().freeAllComponents();
			}
			// System.out.println(count);
			count++;
		}
		
		logger.warn("AVERAGE ACCURACY "+acc.getMeanAsPercentage());

	}

	private static void writeFiles(String filename, EvaluatedDescriptionPosNeg d,
			SortedSet<Individual> positiveEx, SortedSet<Individual> negativeEx) {
		String div = (System.currentTimeMillis() % 10000) + "";
		if (d.getAccuracy() >= 0.99) {
			filename += "99+";
		} else if (d.getAccuracy() >= 0.90) {
			filename += "90+";
		} else if (d.getAccuracy() >= 0.80) {
			filename += "80+";
		} else if (d.getAccuracy() >= 0.70) {
			filename += "70+";
		} else if (d.getAccuracy() > 0.50) {
			filename += "50+";
		} else {
			filename += "50-";
		}
		filename += "_";
		filename += (d.getDescriptionLength() < 10) ? "0"
				+ d.getDescriptionLength() : d.getDescriptionLength() + "";
		filename += "_" + div + ".conf";

		//String content = fileString(true, d, positiveEx, negativeEx);
		//Files.createFile(new File(baseDirSparql + filename), content);
		String content = fileString( d, positiveEx, negativeEx);
		Files.createFile(new File(exampleBaseDir + filename), content);
	}

	@SuppressWarnings("unused")
	private static String accString(EvaluatedDescriptionPosNeg d) {

		String acc = (d.getAccuracy()) + "";
		try {
			acc = acc.substring(2, 6);
			acc = acc.substring(0, 2) + "." + acc.substring(3) + "%";
		} catch (Exception e) {
		}

		return acc;
	}

	private static String fileString( EvaluatedDescriptionPosNeg d,
			SortedSet<Individual> p, SortedSet<Individual> n) {

		String str = "/**\n" + d.getDescription().toKBSyntaxString() + "\n" + d
				+ "\n" + "\n" + "**/\n" + "\n\n";
		/*if (sparql) {
			str += "sparql.instances = {\n";
			for (Individual individual : p) {
				str += "\"" + individual + "\",\n";
			}
			for (Individual individual : n) {
				str += "\"" + individual + "\",\n";
			}
			str = str.substring(0, str.length() - 2);
			str += "};\n";

		}*/

		str += "\n" + "/**EXAMPLES**/\n" + ConfWriter.listExamples(true, p)
				+ "\n" + ConfWriter.listExamples(false, n) + "\n";

		return str;
	}

	private static EvaluatedDescriptionPosNeg learnSPARQL(
			SortedSet<Individual> posExamples, SortedSet<Individual> negExamples) {

		OCEL la = null;

		try {

			SortedSet<Individual> instances = new TreeSet<Individual>();
			instances.addAll(posExamples);
			instances.addAll(negExamples);

			SparqlKnowledgeSource ks = ComponentFactory
					.getSparqlKnowledgeSource(URI.create(
							"http://www.blabla.com").toURL(), SetManipulation
							.indToString(instances));

			ks.setCloseAfterRecursion(true);
			ks.setRecursionDepth(2);
			ks.setPredefinedEndpoint("LOCALJOSEKIBIBLE");
			ks.setUseLits(true);

			Set<AbstractKnowledgeSource> tmp = new HashSet<AbstractKnowledgeSource>();
			tmp.add(ks);
			// reasoner
			OWLAPIReasoner f = ComponentFactory
					.getOWLAPIReasoner(tmp);

			// learning problem
			PosNegLPStandard lp = ComponentFactory.getPosNegLPStandard(f,
					SetManipulation.indToString(posExamples), SetManipulation
							.indToString(negExamples));

			// learning algorithm
			la = ComponentManager.getInstance().learningAlgorithm(OCEL.class, lp, f);;
			la.setNoisePercentage(0.0);
			la.setGuaranteeXgoodDescriptions(1);
			la.setMaxExecutionTimeInSeconds(30);
			
			ks.init();
			f.init();	
			lp.init();
			la.init();

			// start learning algorithm
			logger.debug("start learning");
			la.start();
		} catch (Exception e) {
			// System.out.println("ignoring the error "+e.toString());

		}

		return la.getCurrentlyBestEvaluatedDescription();

	}

	private static void initLogger() {

		SimpleLayout layout = new SimpleLayout();
		// create logger (a simple logger which outputs
		// its messages to the console)
		FileAppender fileAppender = null;
		try {
			fileAppender = new FileAppender(layout, "log/semBibleLog.txt",
					false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		logger.removeAllAppenders();
		if (DEBUG) {
			logger.setLevel(Level.DEBUG);
			ConsoleAppender consoleAppender = new ConsoleAppender(layout);
			logger.addAppender(consoleAppender);
		} else {
			logger.setLevel(Level.INFO);
			
		}
		logger.addAppender(fileAppender);

	}

}
