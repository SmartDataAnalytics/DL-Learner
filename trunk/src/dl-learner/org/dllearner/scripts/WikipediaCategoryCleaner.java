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
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithms.refexamples.ExampleBasedROLComponent;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.extraction.ExtractionAlgorithm;
import org.dllearner.kb.extraction.Manager;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.scripts.improveWikipedia.ConceptSPARQLReEvaluator;
import org.dllearner.scripts.improveWikipedia.ConceptSelector;
import org.dllearner.scripts.improveWikipedia.WikipediaCategoryTasks;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL;
import org.dllearner.utilities.examples.AutomaticPositiveExampleFinderSPARQL;
import org.dllearner.utilities.learn.LearnSPARQLConfiguration;
import org.dllearner.utilities.learn.LearnSparql;
import org.dllearner.utilities.statistics.SimpleClock;

public class WikipediaCategoryCleaner {

	private static SPARQLTasks sparqlTasks;

	private static Cache cache;

	private static Logger logger = Logger.getRootLogger();

	// localEndpoint switch
	private static final boolean LOCAL = false;

	// parameters
	// used for developing,
	private static final boolean DEVELOP = true;

	public static final int SPARQL_RESULTSET_LIMIT = 500;
	
	private static final int DEPTH_OF_RDFS = 0;

	// the 70/30 strategy was abandoned
	public static double PERCENT_OF_SKOSSET = 1.0;

	// size of randomly choosen negative examples compared to positives
	public static double NEGFACTOR = 1.0;

	public static int MAX_NR_CONCEPTS_TO_BE_EVALUATED = Integer.MAX_VALUE;

	public static double ACCURACY_THRESHOLD = 0.0;

	public static String FILTER_CONCEPTS_BY = "Entity";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimpleClock sc = new SimpleClock();
		initLogger();
		setup();
		logger.info("Start");
		SortedSet<String> wikipediaCategories = new TreeSet<String>();
		
		
		String test = "http://dbpedia.org/resource/Category:Prime_Ministers_of_the_United_Kingdom";
		wikipediaCategories.add(test);
		test = "http://dbpedia.org/resource/Category:Best_Actor_Academy_Award_winners";
		wikipediaCategories.add(test);

		for (String target : wikipediaCategories) {

			doit(target);

		}

		sc.printAndSet("Finished");
		// JamonMonitorLogger.printAllSortedByLabel();

	}

	private static void doit(String target) {
		List<EvaluatedDescription> conceptresults;
		SortedSet<String> currentPOSITIVEex = new TreeSet<String>();
		SortedSet<String> currentNEGATIVEex = new TreeSet<String>();
		SortedSet<String> wrongIndividuals;

		WikipediaCategoryTasks wikiTasks;
		ConceptSPARQLReEvaluator csparql;
		System.out.println("test");
		wikiTasks = new WikipediaCategoryTasks(sparqlTasks);
		csparql = new ConceptSPARQLReEvaluator(sparqlTasks, DEPTH_OF_RDFS, SPARQL_RESULTSET_LIMIT);

		// PHASE 1 *************

		wikiTasks.makeInitialExamples(target, PERCENT_OF_SKOSSET, NEGFACTOR,
				SPARQL_RESULTSET_LIMIT, DEVELOP);
		currentPOSITIVEex.addAll(wikiTasks.getPosExamples());
		currentNEGATIVEex.addAll(wikiTasks.getNegExamples());
		// get wrong individuals and reevaluate concepts
		conceptresults = learn(getConfToFindWrongIndividuals(),
				currentPOSITIVEex, currentNEGATIVEex);
		// TODO select concepts
		conceptresults = selectConcepts(conceptresults);
		wrongIndividuals = wikiTasks.calculateWrongIndividualsAndNewPosEx(
				conceptresults, currentPOSITIVEex);
		currentPOSITIVEex.clear();
		currentPOSITIVEex.addAll(wikiTasks.getCleanedPositiveSet());

		// reevaluate versus the Endpoint
		conceptresults = csparql.reevaluateConceptsByLowestRecall(
				conceptresults, currentPOSITIVEex);

		WikipediaCategoryCleaner.printEvaluatedDescriptionCollection(2,
				conceptresults);

		printIntermediateResults(wikiTasks.getFullPositiveSet(),
				wikiTasks.getCleanedPositiveSet(),
				wrongIndividuals, conceptresults.size());
		
		// PHASE 2 ***********************
		logger.info("PHASE 2 ***********************");
		logger.info("making new Negative Examples");
		currentNEGATIVEex = wikiTasks.makeNewNegativeExamples(conceptresults,
				currentPOSITIVEex, NEGFACTOR);

		logger.info("learning");
		conceptresults = learn(getConfToRelearn(), currentPOSITIVEex,
				currentNEGATIVEex);
		//		 TODO select concepts
		logger.info("reducing concept size before evaluating from "+conceptresults.size());
		conceptresults = selectConcepts(conceptresults);
			// reevaluate versus the Endpoint
		conceptresults = csparql.reevaluateConceptsByLowestRecall(
				conceptresults, currentPOSITIVEex);

		printEvaluatedDescriptionCollection(2, conceptresults);
		collectResults(wikiTasks);

	}

	private static void collectResults(WikipediaCategoryTasks wikiTasks) {
		//logger.setLevel(Level.DEBUG);
		SetManipulation.printSet("fullpos", wikiTasks.getFullPositiveSet(), logger);
		
		SetManipulation.printSet("cleanedpos", wikiTasks.getCleanedPositiveSet(), logger);
		
		SetManipulation.printSet("wrongindividuals", wikiTasks.getDefinitelyWrongIndividuals(), logger);
		
	}

	private static List<EvaluatedDescription> selectConcepts(
			List<EvaluatedDescription> concepts) {
		// TODO maybe not smart here
		ConceptSelector cs = new ConceptSelector();
		concepts = cs.getConceptsNotContainingString(concepts,
				FILTER_CONCEPTS_BY, MAX_NR_CONCEPTS_TO_BE_EVALUATED);
		if (concepts.size() == 0) {
			logger.warn("NO GOOD CONCEPTS FOUND");
			// TODO if this happens there has to be a fallback
		}
		return concepts;
	}

	/**
	 * All Concepts are returned, filtering these are done separately
	 * 
	 * @param conf
	 * @param posExamples
	 * @param negExamples
	 * @return
	 */
	private static List<EvaluatedDescription> learn(
			LearnSPARQLConfiguration conf, SortedSet<String> posExamples,
			SortedSet<String> negExamples) {
		LearnSparql learner = new LearnSparql(getConfToRelearn());
		LearningAlgorithm la = null;
		try {
			la = learner.learn(posExamples, negExamples);
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<EvaluatedDescription> conceptresults = la
				.getCurrentlyBestEvaluatedDescriptions(Integer.MAX_VALUE, 0.0,
						true);
		return conceptresults;
	}

	private static LearnSPARQLConfiguration getConfToFindWrongIndividuals() {
		LearnSPARQLConfiguration lsc = new LearnSPARQLConfiguration();
		lsc.sparqlEndpoint = sparqlTasks.getSparqlEndpoint();

		lsc.recursiondepth = 1;
		lsc.noisePercentage = 15;
		lsc.guaranteeXgoodDescriptions = 200;
		lsc.maxExecutionTimeInSeconds = 50;
		lsc.logLevel = "TRACE";
		// lsc.searchTreeFile = "log/WikipediaCleaner.txt";

		return lsc;

	}

	private static LearnSPARQLConfiguration getConfToRelearn() {
		return getConfToFindWrongIndividuals();

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
				
				break;
			}
			x++;
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

	
	
	private static void printIntermediateResults(
			SortedSet fullSet, 
			SortedSet correctIndividuals, 
			SortedSet wrongIndividuals,
			int numberOfConcepts) {
		SetManipulation.printSet("full  Individual set: ", fullSet, logger);
		
		SetManipulation.printSet("correct Individuals: ", correctIndividuals, logger);
		SetManipulation.printSet("incorrect Individuals: ", wrongIndividuals, logger);
		logger.info("reevaluated " + numberOfConcepts + " found Concepts");
		logger.info("END OF PHASE 1 **********************");
	}
	
	

	private static void setup() {
		// SETUP cache and sparqltasks
		cache = Cache.getPersistentCache();

		if (LOCAL) {
			// url = "http://139.18.2.37:8890/sparql";
			sparqlTasks = new SPARQLTasks(cache, SparqlEndpoint
					.getEndpointLOCALDBpedia());
		} else {
			// url = "http://dbpedia.openlinksw.com:8890/sparql";
			sparqlTasks = new SPARQLTasks(cache, SparqlEndpoint
					.getEndpointDBpedia());
			
		}
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
		Logger.getLogger(Manager.class).setLevel(Level.INFO);
		Level lwarn = Level.WARN;
		Logger.getLogger(KnowledgeSource.class).setLevel(lwarn);
		Logger.getLogger(SparqlKnowledgeSource.class).setLevel(lwarn);
		
		Logger.getLogger(ExtractionAlgorithm.class).setLevel(lwarn);
		Logger.getLogger(AutomaticNegativeExampleFinderSPARQL.class).setLevel(
				lwarn);
		Logger.getLogger(AutomaticPositiveExampleFinderSPARQL.class).setLevel(
				lwarn);
		Logger.getLogger(ExampleBasedROLComponent.class).setLevel(lwarn);
		Logger.getLogger(SparqlQuery.class).setLevel(lwarn);
		Logger.getLogger(Cache.class).setLevel(lwarn);

	}

}
