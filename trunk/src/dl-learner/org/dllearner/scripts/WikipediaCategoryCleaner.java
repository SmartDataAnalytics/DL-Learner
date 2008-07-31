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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.scripts.improveWikipedia.ConceptSelector;
import org.dllearner.scripts.improveWikipedia.WikipediaCategoryTasks;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL;
import org.dllearner.utilities.examples.AutomaticPositiveExampleFinderSPARQL;

public class WikipediaCategoryCleaner {

	private static SPARQLTasks sparqlTasks;

	private static Cache cache;

	private static Logger logger = Logger.getRootLogger();

	private static boolean local = true; // localEndpoint switch

	// parameters
	public static final int SPARQL_RESULTSET_LIMIT = 1000;

	public static double PERCENT_OF_SKOSSET = 1.0; // the 70/30 strategy was

	// abandoned

	public static double NEGFACTOR = 1.0; // size of randomly choosen negative

	// examples compared to positives

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		initLogger();
		logger.info("Start");

		// SETUP cache and sparqltasks
		cache = Cache.getPersistentCache();

		if (local) {
			// url = "http://139.18.2.37:8890/sparql";
			sparqlTasks = new SPARQLTasks(cache, SparqlEndpoint
					.getEndpointLOCALDBpedia());
		} else {
			// url = "http://dbpedia.openlinksw.com:8890/sparql";
			sparqlTasks = new SPARQLTasks(cache, SparqlEndpoint
					.getEndpointDBpedia());
		}

		String target = "http://dbpedia.org/resource/Category:Prime_Ministers_of_the_United_Kingdom";
		// target =
		// "http://dbpedia.org/resource/Category:Best_Actor_Academy_Award_winners";

		WikipediaCategoryTasks s = new WikipediaCategoryTasks(sparqlTasks);
		// TODO Optimize
		s.calculateDefinitelyWrongIndividuals(target, PERCENT_OF_SKOSSET,
				NEGFACTOR, SPARQL_RESULTSET_LIMIT);

		logger.info("Found " + s.getDefinitelyWrongIndividuals().size()
				+ " incorrect individuals");
		logger.debug("incorrect Individuals: "
				+ s.getDefinitelyWrongIndividuals());
		logger.info("reevaluating " + s.getConceptresults().size()
				+ " found Concepts");
		logger
				.info("END OF PHASE 1 **********************************************");

		s.reevaluateAndRelearn();
		List<EvaluatedDescription> newEval = s.getConceptresults();
		printEvaluatedDescriptionCollection(5, newEval);

		System.out.println("Finished");
		JamonMonitorLogger.printAllSortedByLabel();

	}

	private static void initLogger() {

		SimpleLayout layout = new SimpleLayout();
		// create logger (a simple logger which outputs
		// its messages to the console)
		FileAppender fileAppender = null;
		try {
			fileAppender = new FileAppender(layout, "log/progress/skos"
					+ ConceptSelector.time() + ".txt", false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.addAppender(fileAppender);
		logger.setLevel(Level.DEBUG);
		Logger.getLogger(KnowledgeSource.class).setLevel(Level.WARN);

		Logger.getLogger(SparqlQuery.class).setLevel(Level.INFO);
		Logger.getLogger(Cache.class).setLevel(Level.INFO);
		Logger.getLogger(AutomaticNegativeExampleFinderSPARQL.class).setLevel(
				Level.INFO);
		Logger.getLogger(AutomaticPositiveExampleFinderSPARQL.class).setLevel(
				Level.INFO);
	}

	public static void printEvaluatedDescriptionCollection(int howMany,
			Collection<EvaluatedDescription> c) {
		int x = 0;
		Set<Individual> first = null;
		Set<Individual> tmp = new HashSet<Individual>();
		for (EvaluatedDescription ed : c) {
			if (x == 0) {
				first = ed.getNotCoveredPositives();
			}
			if (x >= howMany) {
				x++;
				break;
			}

			tmp.addAll(ed.getNotCoveredPositives());
			tmp.removeAll(first);
			logger.debug("*************************");
			logger.debug("Concept: " + ed);
			logger.debug("accuracy: " + ed.getAccuracy());
			logger.debug("Not Covered compared to First: " + tmp);
			logger.debug(ed.getScore());
			tmp.clear();

		}
	}

}
