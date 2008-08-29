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

import java.util.ArrayList;
import java.util.List;
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
import org.dllearner.core.ReasoningService;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.extraction.ExtractionAlgorithm;
import org.dllearner.kb.extraction.Manager;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderOWL;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL;
import org.dllearner.utilities.examples.AutomaticPositiveExampleFinderOWL;
import org.dllearner.utilities.examples.AutomaticPositiveExampleFinderSPARQL;
import org.dllearner.utilities.learn.ConfWriter;
import org.dllearner.utilities.learn.LearnOWLFile;
import org.dllearner.utilities.learn.LearnOWLFileConfiguration;
import org.dllearner.utilities.learn.LearnSPARQLConfiguration;
import org.dllearner.utilities.owl.ReasoningServiceFactory;
import org.dllearner.utilities.owl.ReasoningServiceFactory.AvailableReasoners;
import org.dllearner.utilities.statistics.SimpleClock;

public class SemanticBible {

	private static ReasoningService reasoningService;

	private static Logger logger = Logger.getRootLogger();

	// size of randomly choosen negative examples compared to positives
	public static double NEGFACTOR = 3.0;

	// different negative Ex (randomizes) each run, if set to false
	private static final boolean DEVELOP = true;
	
	public static String ontologyPath = "examples/semantic_bible/NTNcombined.owl";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimpleClock sc = new SimpleClock();
		initLogger();
		logger.info("Start");
		
		//String fileURL = new File(ontologyFile).toURI().toString();
		
		reasoningService = ReasoningServiceFactory.getReasoningService(
				ontologyPath, AvailableReasoners.OWLAPIREASONERPELLET);
		// SortedSet<NamedClass> classesToRelearn = new
		// TreeSet<NamedClass>(rs.getAtomicConceptsList(true));
		// for (NamedClass target : classesToRelearn) {
		// System.out.println("classesToRelearn.add(new
		// NamedClass(\""+target.toString()+"\"));");

		// }

		SortedSet<NamedClass> classesToRelearn = getClassesToRelearn(true);
		SortedSet<Individual> positiveEx = new TreeSet<Individual>();
		SortedSet<Individual> negativeEx = new TreeSet<Individual>();

		for (NamedClass target : classesToRelearn) {
			positiveEx.clear();
			negativeEx.clear();

			AutomaticPositiveExampleFinderOWL ape = new AutomaticPositiveExampleFinderOWL(
					reasoningService);
			ape.makePositiveExamplesFromConcept(target);
			positiveEx = ape.getPosExamples();

			AutomaticNegativeExampleFinderOWL ane = new AutomaticNegativeExampleFinderOWL(
					positiveEx, reasoningService);
			ane.makeNegativeExamplesFromSuperClasses(target);
			ane.makeNegativeExamplesFromAllOtherInstances();
			//double correct = ()
			// System.out.println((positiveEx.size()*NEGFACTOR));
			negativeEx = ane.getNegativeExamples(
					(int) (positiveEx.size() * NEGFACTOR), DEVELOP);

			// reasoningService.prepareSubsumptionHierarchy();
			// System.out.println(reasoningService.getMoreGeneralConcepts(target));

			// for every class execute the learning algorithm
			learnOriginal(target, positiveEx, negativeEx);

		}

		sc.printAndSet("Finished");
		// JamonMonitorLogger.printAllSortedByLabel();

	}

	private static void learnOriginal(NamedClass target, SortedSet<Individual> posExamples, SortedSet<Individual> negExamples) {
		List<EvaluatedDescription> conceptresults = new ArrayList<EvaluatedDescription>();
		System.out.println("Starting to learn original");
		//System.out.println(ConfWriter.listExamples(true, posExamples));
		//System.out.println(ConfWriter.listExamples(false, negExamples));
		//System.exit(0);
		LearnOWLFile learner = new LearnOWLFile(getConfForOriginal(target));
		LearningAlgorithm la = null;
		try{
		la = learner.learn(
				SetManipulation.indToString(posExamples), 
				SetManipulation.indToString(negExamples), 
				FastInstanceChecker.class);
		}catch (Exception e) {
			// TODO: handle exception
		}
		la.start();
		System.out.println(la.getCurrentlyBestDescription());
	}

	private static LearnSPARQLConfiguration getConfForSparql(NamedClass c) {
		LearnSPARQLConfiguration lsc = new LearnSPARQLConfiguration();
		// lsc.sparqlEndpoint = sparqlTasks.getSparqlEndpoint();

		
		lsc.recursiondepth = 2;
		lsc.closeAfterRecursion = true;
		lsc.ignoredConcepts.add(c.toString());
		
		lsc.noisePercentage = 20;
		lsc.guaranteeXgoodDescriptions = 100;
		lsc.maxExecutionTimeInSeconds = 50;

		// lsc.searchTreeFile = "log/WikipediaCleaner.txt";

		return lsc;

	}

	private static LearnOWLFileConfiguration getConfForOriginal(NamedClass c) {
		LearnOWLFileConfiguration loc = new LearnOWLFileConfiguration();
		
		
		loc.setOWLFileURL(ontologyPath);
		
		
		//loc.ignoredConcepts.add(c.toString());
		

		loc.noisePercentage = 0;
		// loc.guaranteeXgoodDescriptions = 100;
		loc.maxExecutionTimeInSeconds = 20;
		loc.writeSearchTree = true;
		loc.replaceSearchTree = true;
		loc.searchTreeFile = "log/treeSemanticBible.txt";

		return loc;

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

		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.addAppender(fileAppender);
		logger.setLevel(Level.DEBUG);
		Logger.getLogger(Manager.class).setLevel(Level.INFO);
		Level lwarn = Level.DEBUG;
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
		Logger.getLogger(AutomaticPositiveExampleFinderOWL.class).setLevel(
				lwarn);

	}

	public static SortedSet<NamedClass> getClassesToRelearn(boolean firstOnly) {

		SortedSet<NamedClass> classesToRelearn = new TreeSet<NamedClass>();
		if (firstOnly) {
			//classesToRelearn.add(new NamedClass(
				//	"http://semanticbible.org/ns/2006/NTNames#Angel"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#Woman"));

		} else {
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#Agent"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#Angel"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#BeliefGroup"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#BeliefSystem"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#Character"));
			classesToRelearn
					.add(new NamedClass(
							"http://semanticbible.org/ns/2006/NTNames#CitizenshipAttribute"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#City"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#CognitiveAgent"));
			classesToRelearn
					.add(new NamedClass(
							"http://semanticbible.org/ns/2006/NTNames#ContentBearingObject"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#EthnicGroup"));
			classesToRelearn
					.add(new NamedClass(
							"http://semanticbible.org/ns/2006/NTNames#EthnicityAttribute"));
			classesToRelearn
					.add(new NamedClass(
							"http://semanticbible.org/ns/2006/NTNames#EvilSupernaturalBeing"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#FixedHoliday"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#FreshWaterArea"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#GeographicArea"));
			classesToRelearn
					.add(new NamedClass(
							"http://semanticbible.org/ns/2006/NTNames#GeographicLocation"));
			classesToRelearn
					.add(new NamedClass(
							"http://semanticbible.org/ns/2006/NTNames#GeopoliticalArea"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#God"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#Group"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#GroupOfPeople"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#Human"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#HumanAttribute"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#Island"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#LandArea"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#Man"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#Mountain"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#Nation"));
			classesToRelearn
					.add(new NamedClass(
							"http://semanticbible.org/ns/2006/NTNames#NaturalLanguage"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#Object"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#Organization"));
			classesToRelearn
					.add(new NamedClass(
							"http://semanticbible.org/ns/2006/NTNames#PoliticalAttribute"));
			classesToRelearn
					.add(new NamedClass(
							"http://semanticbible.org/ns/2006/NTNames#PoliticalBeliefSystem"));
			classesToRelearn
					.add(new NamedClass(
							"http://semanticbible.org/ns/2006/NTNames#PoliticalOrganization"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#Region"));
			classesToRelearn
					.add(new NamedClass(
							"http://semanticbible.org/ns/2006/NTNames#ReligiousBelief"));
			classesToRelearn
					.add(new NamedClass(
							"http://semanticbible.org/ns/2006/NTNames#ReligiousBeliefSystem"));
			classesToRelearn
					.add(new NamedClass(
							"http://semanticbible.org/ns/2006/NTNames#ReligiousOrganization"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#ResidenceGroup"));
			classesToRelearn
					.add(new NamedClass(
							"http://semanticbible.org/ns/2006/NTNames#ResidencyAttribute"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#SaltWaterArea"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#Series"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#SonOfGod"));
			classesToRelearn
					.add(new NamedClass(
							"http://semanticbible.org/ns/2006/NTNames#StateOrProvince"));
			classesToRelearn
					.add(new NamedClass(
							"http://semanticbible.org/ns/2006/NTNames#SupernaturalBeing"));
			classesToRelearn
					.add(new NamedClass(
							"http://semanticbible.org/ns/2006/NTNames#SupernaturalRegion"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#Tribe"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#WaterArea"));
			classesToRelearn.add(new NamedClass(
					"http://semanticbible.org/ns/2006/NTNames#Woman"));
		}
		return classesToRelearn;
	}

}
