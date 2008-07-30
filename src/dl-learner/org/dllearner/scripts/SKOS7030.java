/**
 * Copyright (C) 2007, Jens Lehmann
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
import java.util.ArrayList;
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
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.owl.Description;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.learningproblems.PosNegDefinitionLP;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL;
import org.dllearner.utilities.examples.AutomaticPositiveExampleFinderSPARQL;

public class SKOS7030 {
	
	public int test = 0;

	private static SPARQLTasks sparqlTasks;

	private static LearningAlgorithm la;

	private static final long wash = 1216800000000L;

	private boolean stable = true;

	// private static long wash = 1216901570168

	private static Logger logger = Logger.getRootLogger();

	static boolean local = true;

	static String url = "";

	// LEARNING
	static int recursiondepth = 1;

	static boolean closeAfterRecursion = true;

	static boolean randomizeCache = false;

	static double noise = 15;

	static int maxExecutionTimeInSeconds = 30;

	static int guaranteeXgoodDescriptions = 40;

	// examples
	static int sparqlResultSize = 2000;

	static double percentOfSKOSSet = 0.2;

	static double negfactor = 1.0;

	SortedSet<String> posExamples = new TreeSet<String>();

	SortedSet<String> fullPositiveSet = new TreeSet<String>();

	SortedSet<String> fullPosSetWithoutPosExamples = new TreeSet<String>();

	SortedSet<String> negExamples = new TreeSet<String>();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		initLogger();
		logger.info("Start");
		// String resultString="";
		// System.out.println(time());
		// System.out.println(System.currentTimeMillis());
		
		
		// parameters

		if (local) {
			url = "http://139.18.2.37:8890/sparql";
			sparqlTasks = new SPARQLTasks(Cache.getPersistentCache(),
					SparqlEndpoint.getEndpointLOCALDBpedia());
		} else {
			url = "http://dbpedia.openlinksw.com:8890/sparql";
			sparqlTasks = new SPARQLTasks(Cache.getPersistentCache(),
					SparqlEndpoint.getEndpointDBpedia());
		}

		//System.out.println(sparqlTasks.getDomain(
		//		"http://dbpedia.org/property/predecessor", 1000));
		
		String target = "http://dbpedia.org/resource/Category:Prime_Ministers_of_the_United_Kingdom";

		// String
		// award=("http://dbpedia.org/resource/Category:Best_Actor_Academy_Award_winners");

		SKOS7030 s = new SKOS7030();

		s.makeExamples(target, percentOfSKOSSet, negfactor, sparqlResultSize);

		// System.exit(0);
		List<Description> conceptresults = new ArrayList<Description>();
		List<EvaluatedDescription> conceptresults2 = new ArrayList<EvaluatedDescription>();
		s.learn();

		recordConceptClasses();

		System.exit(0);

		// EvaluatedDescription
		logger.debug("found nr of concepts: " + conceptresults.size());
		System.out.println(conceptresults);

		int x = 0;

		SortedSet<ResultMostCoveredInRest> res = new TreeSet<ResultMostCoveredInRest>();
		for (Description concept : conceptresults) {
			if (x++ == 100)
				break;
			res.add(s.evaluate(concept, 1000));

		}

		x = 0;
		for (ResultMostCoveredInRest resultMostCoveredInRest : res) {
			if (x++ == 10)
				break;
			System.out.println(resultMostCoveredInRest.concept);
			System.out.println(resultMostCoveredInRest.accuracy);
			System.out.println(resultMostCoveredInRest.retrievedInstancesSize);

		}

		s.print(res.first().concept, 1000);

		System.out.println("Finished");
		JamonMonitorLogger.printAllSortedByLabel();

	}

	void print(final Description concept, final int sparqlResultLimit) {
		logger.debug("evaluating concept: " + concept);
		// SortedSet<String> instances =
		// sparqlTasks.retrieveInstancesForConcept(oneConcept.toKBSyntaxString(),
		// sparqlResultLimit);
		SortedSet<String> instances = sparqlTasks
				.retrieveInstancesForConceptIncludingSubclasses(concept
						.toKBSyntaxString(), sparqlResultLimit);

		SortedSet<String> coveredInRest = new TreeSet<String>(
				fullPosSetWithoutPosExamples);
		coveredInRest.retainAll(instances);

		SortedSet<String> coveredTotal = new TreeSet<String>(fullPositiveSet);
		coveredTotal.retainAll(instances);

		SortedSet<String> notCoveredInRest = new TreeSet<String>(
				fullPosSetWithoutPosExamples);
		notCoveredInRest.retainAll(coveredInRest);
		System.out.println(notCoveredInRest);

		SortedSet<String> notCoveredTotal = new TreeSet<String>(fullPositiveSet);
		notCoveredTotal.retainAll(coveredTotal);
		System.out.println(notCoveredTotal);

	}

	ResultMostCoveredInRest evaluate(Description concept, int sparqlResultLimit) {
		logger.debug("evaluating concept: " + concept);
		// SortedSet<String> instances =
		// sparqlTasks.retrieveInstancesForConcept(oneConcept.toKBSyntaxString(),
		// sparqlResultLimit);
		SortedSet<String> instances = sparqlTasks
				.retrieveInstancesForConceptIncludingSubclasses(concept
						.toKBSyntaxString(), sparqlResultLimit);

		SortedSet<String> coveredInRest = new TreeSet<String>(
				fullPosSetWithoutPosExamples);
		coveredInRest.retainAll(instances);

		SortedSet<String> coveredTotal = new TreeSet<String>(fullPositiveSet);
		coveredTotal.retainAll(instances);

		SortedSet<String> notCoveredInRest = new TreeSet<String>(
				fullPosSetWithoutPosExamples);
		notCoveredInRest.retainAll(coveredInRest);

		SortedSet<String> notCoveredTotal = new TreeSet<String>(fullPositiveSet);
		notCoveredTotal.retainAll(coveredTotal);
		double acc = (double) (coveredInRest.size() / fullPosSetWithoutPosExamples
				.size());
		System.out.println("Accuracy: " + acc);
		return new ResultMostCoveredInRest(concept, acc, instances.size());

	}

	private static void initLogger() {

		SimpleLayout layout = new SimpleLayout();
		// create logger (a simple logger which outputs
		// its messages to the console)
		FileAppender fileAppender = null;
		try {
			fileAppender = new FileAppender(layout, "log/progress/skos"
					+ time() + ".txt", false);
		} catch (Exception e) {
			e.printStackTrace();
		}

		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.addAppender(fileAppender);
		logger.setLevel(Level.DEBUG);
		Logger.getLogger(KnowledgeSource.class).setLevel(Level.WARN);

	}

	/*
	 * public static SortedSet<String> selectDBpediaConcepts(int number){
	 * String query = "SELECT DISTINCT ?concept WHERE { \n" + "[] a ?concept
	 * .FILTER (regex(str(?concept),'yago'))" + " \n} \n"; //LIMIT "+number+"
	 * 
	 * String JSON = (c.executeSparqlQuery(new SparqlQuery(query, se)));
	 * ResultSet rs =SparqlQuery.JSONtoResultSet(JSON); JenaResultSetConvenience
	 * rsc = new JenaResultSetConvenience(rs); return
	 * SetManipulation.fuzzyShrink(rsc.getStringListForVariable("concept"),number); }
	 */

	public void makeExamples(String SKOSConcept, double percentOfSKOSSet,
			double negfactor, int sparqlResultSize) {

		// POSITIVES
		AutomaticPositiveExampleFinderSPARQL apos = new AutomaticPositiveExampleFinderSPARQL(
				sparqlTasks);
		apos.makePositiveExamplesFromSKOSConcept(SKOSConcept);
		fullPositiveSet = apos.getPosExamples();

		// System.exit(0);

		int poslimit = (int) Math.round(percentOfSKOSSet
				* fullPositiveSet.size());
		int neglimit = (int) Math.round(poslimit * negfactor);

		posExamples = SetManipulation.fuzzyShrink(fullPositiveSet, poslimit);

		// NEGATIVES

		AutomaticNegativeExampleFinderSPARQL aneg = new AutomaticNegativeExampleFinderSPARQL(
				fullPositiveSet, sparqlTasks);

		aneg.makeNegativeExamplesFromParallelClasses(posExamples,
				sparqlResultSize);
		negExamples = aneg.getNegativeExamples(neglimit, stable);

		logger.debug("POSITIVE EXAMPLES");
		for (String pos : posExamples) {
			logger.debug("+" + pos);
		}

		logger.debug("NEGATIVE EXAMPLES");
		for (String negs : this.negExamples) {
			logger.debug("-" + negs);
		}

		fullPosSetWithoutPosExamples = fullPositiveSet;
		fullPosSetWithoutPosExamples.removeAll(posExamples);

		logger.debug(fullPositiveSet);
		logger.debug(fullPosSetWithoutPosExamples);
	}

	public void learn() {

		SortedSet<String> instances = new TreeSet<String>();
		instances.addAll(this.posExamples);
		instances.addAll(this.negExamples);

		logger.info("Start Learning with");
		logger.info("positive examples: \t" + posExamples.size());
		logger.info("negative examples: \t" + negExamples.size());
		logger.info("instances \t" + instances.size());

		ComponentManager cm = ComponentManager.getInstance();
		// LearningAlgorithm la = null;
		ReasoningService rs = null;
		LearningProblem lp = null;
		SparqlKnowledgeSource ks = null;
		try {
			Set<KnowledgeSource> sources = new HashSet<KnowledgeSource>();
			ks = cm.knowledgeSource(SparqlKnowledgeSource.class);
			ReasonerComponent r = new FastInstanceChecker(sources);
			rs = new ReasoningService(r);
			// System.out.println("satisfy: "+rs.isSatisfiable());
			lp = new PosNegDefinitionLP(rs);
			((PosNegLP) lp).setPositiveExamples(SetManipulation
					.stringToInd(this.posExamples));
			((PosNegLP) lp).setNegativeExamples(SetManipulation
					.stringToInd(this.negExamples));

			la = cm.learningAlgorithm(ExampleBasedROLComponent.class, lp, rs);

			logger.debug("start learning");

			// KNOWLEDGESOURCE
			cm.applyConfigEntry(ks, "instances", instances);
			cm.applyConfigEntry(ks, "url", url);
			cm.applyConfigEntry(ks, "recursionDepth", recursiondepth);
			cm.applyConfigEntry(ks, "closeAfterRecursion", closeAfterRecursion);
			cm.applyConfigEntry(ks, "predefinedFilter", "YAGO");
			if (local)
				cm.applyConfigEntry(ks, "predefinedEndpoint", "LOCALDBPEDIA");
			else {
				cm.applyConfigEntry(ks, "predefinedEndpoint", "DBPEDIA");
			}
			if (randomizeCache)
				cm.applyConfigEntry(ks, "cacheDir", "cache/"
						+ System.currentTimeMillis() + "");
			else {
				cm.applyConfigEntry(ks, "cacheDir", Cache.getDefaultCacheDir());
			}

			// LEARNINGALGORITHM
			cm.applyConfigEntry(la, "useAllConstructor", false);
			cm.applyConfigEntry(la, "useExistsConstructor", true);
			cm.applyConfigEntry(la, "useCardinalityRestrictions", false);
			cm.applyConfigEntry(la, "useNegation", false);
			cm.applyConfigEntry(la, "minExecutionTimeInSeconds", 0);
			cm.applyConfigEntry(la, "maxExecutionTimeInSeconds",
					maxExecutionTimeInSeconds);
			cm.applyConfigEntry(la, "guaranteeXgoodDescriptions",
					guaranteeXgoodDescriptions);
			cm.applyConfigEntry(la, "writeSearchTree", false);
			cm.applyConfigEntry(la, "searchTreeFile", "log/SKOS.txt");
			cm.applyConfigEntry(la, "replaceSearchTree", true);
			cm.applyConfigEntry(la, "noisePercentage", noise);
			// cm.applyConfigEntry(la,"guaranteeXgoodDescriptions",999999);
			cm.applyConfigEntry(la, "logLevel", "TRACE");
			/*
			 * if(ignoredConcepts.size()>0)
			 * cm.applyConfigEntry(la,"ignoredConcepts",ignoredConcepts);
			 */

			ks.init();
			sources.add(ks);
			r.init();
			lp.init();
			la.init();

			la.start();
			// Statistics.addTimeCollecting(sc.getTime());
			// Statistics.addTimeLearning(sc.getTime());

			// return la.getCurrentlyBestDescriptions();

		} catch (Exception e) {
			e.printStackTrace();
		}
		// return null;

	}

	// String t="\"http://dbpedia.org/class/yago/Fiction106367107\"";
	// t="(\"http://dbpedia.org/class/yago/HeadOfState110164747\" AND
	// (\"http://dbpedia.org/class/yago/Negotiator110351874\" AND
	// \"http://dbpedia.org/class/yago/Representative110522035\"))";
	// //System.out.println(t);
	// //t="\"http://www.w3.org/2004/02/skos/core#subject\"";
	// //conceptRewrite(t);
	// //getSubClasses(t);
	//	
	// AutomaticExampleFinderSKOSSPARQL ae= new
	// AutomaticExampleFinderSKOSSPARQL( se);
	// try{
	// System.out.println("oneconcept: "+t);
	// SortedSet<String> instances =
	// ae.queryConceptAsStringSet(conceptRewrite(t), 200);
	// if(instances.size()>=0)System.out.println("size of instances
	// "+instances.size());
	// if(instances.size()>=0 && instances.size()<100)
	// System.out.println("instances"+instances);
	// }catch (Exception e) {
	// e.printStackTrace();
	// }
	// SortedSet<String> concepts = new TreeSet<String>();

	// System.out.println(DBpediaSKOS(prim));
	// double acc1=0.0;
	// for (int i = 0; i < 5; i++) {
	// acc1+=DBpediaSKOS(prim);
	// }
	// System.out.println("accprim"+(acc1/5));
	//	
	// double acc2=0.0;
	// for (int i = 0; i < 5; i++) {
	// acc2+=DBpediaSKOS(award);
	// }
	// System.out.println("accprim"+(acc2/5));

	// DBpediaSKOS(concepts.first());
	// DBpediaSKOS(concepts.first());
	// concepts.remove(concepts.first());
	// DBpediaSKOS(concepts.first());
	// DBpediaSKOS(concepts.first());
	// concepts.remove(concepts.first());
	// DBpediaSKOS(concepts.first());
	// DBpediaSKOS(concepts.first());
	// algorithm="refinement";
	// roles();

	/*
	 * System.out.println(Level.DEBUG.getClass());
	 * System.out.println(Level.toLevel("INFO"));
	 * System.out.println(Level.INFO);
	 */
	// System.exit(0);
	private class ResultCompare implements Comparable<ResultCompare> {
		Description concept;

		double accuracy = 0.0;

		int retrievedInstancesSize = 0;

		public int compareTo(ResultCompare o2) {
			return 0;
		}

		
		public boolean equals(ResultCompare o2) {
			return this.concept.equals(o2.concept);
		}

		public ResultCompare(Description conceptKBSyntax, double accuracy,
				int retrievedInstancesSize) {
			super();
			this.concept = conceptKBSyntax;
			this.accuracy = accuracy;
			this.retrievedInstancesSize = retrievedInstancesSize;
		}

	}

	private class ResultMostCoveredInRest extends ResultCompare {

		public ResultMostCoveredInRest(Description concept, double accuracy,
				int retrievedInstancesSize) {
			super(concept, accuracy, retrievedInstancesSize);

		}

		public int compareTo(ResultMostCoveredInRest o2) {
			if (this.equals(o2))
				return 0;

			if (this.accuracy > o2.accuracy) {
				return 1;
			} else if (this.accuracy == o2.accuracy) {
				if (this.retrievedInstancesSize < o2.retrievedInstancesSize)
					return 1;
				else if (this.retrievedInstancesSize > o2.retrievedInstancesSize) {
					return -1;
				} else
					return this.concept.toKBSyntaxString().compareTo(
							o2.concept.toKBSyntaxString());
			} else {
				return -1;
			}

		}

	}

	public static String time() {
		return ("" + (System.currentTimeMillis() - wash)).substring(0, 7);

	}

	/**
	 * 
	 */
	public static void recordConceptClasses() {
		StringBuffer result =new StringBuffer();
		StringBuffer result1 =new StringBuffer("\n\n ***********Entity*****\n");
		StringBuffer result2 =new StringBuffer("\n\n ***********OR*****\n");
		int result1count = 1;
		int result2count = 1;
		List<EvaluatedDescription> conceptresults = la
				.getCurrentlyBestEvaluatedDescriptions(5000, .70, true);

		int x = 0;
		for (EvaluatedDescription description : conceptresults) {
			if (x < 50) {
				x++;
				result.append(description + "\n");
			}

			if (!description.toString().contains("Entity")) {
				result1.append(description + "\n");
				result1count++;
			}
			if (!description.toString().contains("OR")) {
				result2.append(description + "\n");
				result2count++;
			}
		}
		result.append("full size: " + conceptresults.size());
		result.append(result1.toString() + " size: " + result1count + "\n");
		result.append(result2.toString() + " size: " + result2count + "\n");

		Files.createFile(new File("results/descriptions/concepts" + time()
				+ ".txt"), result.toString());
	}

}
