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
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.extraction.ExtractionAlgorithm;
import org.dllearner.kb.extraction.Manager;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.reasoning.ReasonerType;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.components.ReasonerComponentFactory;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderOWL;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL;
import org.dllearner.utilities.examples.AutomaticPositiveExampleFinderOWL;
import org.dllearner.utilities.examples.AutomaticPositiveExampleFinderSPARQL;
import org.dllearner.utilities.learn.LearnOWLFileConfiguration;
import org.dllearner.utilities.learn.LearnSPARQLConfiguration;
import org.dllearner.utilities.statistics.SimpleClock;

public class SemanticBible {

	private static AbstractReasonerComponent reasoningService;

	private static Logger logger = Logger.getRootLogger();

	// size of randomly choosen negative examples compared to positives
	public static double NEGFACTOR = 1.0;
	public static int POSLIMIT = 10;

	// different negative Ex (randomizes) each run, if set to false
	private static final boolean DEVELOP = true;
	private static final boolean WAITFORINPUT = false;
	private static final boolean RANDOMNEGATIVES = false;
	private static final boolean FORCESIZEOFNEG = true;
	static File file = new File("sembib.txt");
	
	public static String ontologyPath = "examples/semantic_bible/NTNcombined.owl";
	
//	private static Class<? extends ReasonerComponent> usedReasoner = FastInstanceChecker.class;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimpleClock sc = new SimpleClock();
		initLogger();
		logger.info("Start");
		Files.clearFile(file);
		Files.appendFile(file, "neg Ex random: "+RANDOMNEGATIVES+"\n");
		Files.appendFile(file, "negfactor : "+NEGFACTOR+"\n");
			
		
		//String fileURL = new File(ontologyFile).toURI().toString();
		
		reasoningService = ReasonerComponentFactory.getReasonerComponent(
				ontologyPath, ReasonerType.OWLAPI_PELLET);
		// SortedSet<NamedClass> classesToRelearn = new
		// TreeSet<NamedClass>(rs.getAtomicConceptsList(true));
		// for (NamedClass target : classesToRelearn) {
		// System.out.println("classesToRelearn.add(new
		// NamedClass(\""+target.toString()+"\"));");

		// }

		SortedSet<NamedClass> classesToRelearn = getClassesToRelearn(false);
		SortedSet<Individual> positiveEx = new TreeSet<Individual>();
		SortedSet<Individual> negativeEx = new TreeSet<Individual>();

		positiveEx.add(new Individual("http://semanticbible.org/ns/2006/NTNames#Archelaus"));
		positiveEx.add(new Individual("http://semanticbible.org/ns/2006/NTNames#HerodAntipas"));
				
		
		negativeEx.add(new Individual("http://semanticbible.org/ns/2006/NTNames#Almighty"));
		negativeEx.add(new Individual("http://semanticbible.org/ns/2006/NTNames#Gabriel"));
		negativeEx.add(new Individual("http://semanticbible.org/ns/2006/NTNames#Michael"));
		negativeEx.add(new Individual("http://semanticbible.org/ns/2006/NTNames#Satan"));
		negativeEx.add(new Individual("http://semanticbible.org/ns/2006/NTNames#Jesus"));
		learnOriginal(null, positiveEx, negativeEx);
		
		
		
		for (NamedClass target : classesToRelearn) {
			Files.appendFile(file,"now learning: "+target+"\n");
			waitForInput();
			
			positiveEx.clear();
			negativeEx.clear();

			AutomaticPositiveExampleFinderOWL ape = new AutomaticPositiveExampleFinderOWL(
					reasoningService);
			ape.makePositiveExamplesFromConcept(target);
			positiveEx.addAll(ape.getPosExamples());
			positiveEx = SetManipulation.stableShrinkInd(positiveEx, POSLIMIT);

			AutomaticNegativeExampleFinderOWL ane = new AutomaticNegativeExampleFinderOWL(
					positiveEx, reasoningService);
			//ane.makeNegativeExamplesFromSuperClasses(target);
			if(RANDOMNEGATIVES){ane.makeNegativeExamplesFromAllOtherInstances();}
			else{ ane.makeNegativeExamplesFromSuperClasses(target);}
			//double correct = ()
			// System.out.println((positiveEx.size()*NEGFACTOR));
			negativeEx.addAll(ane.getNegativeExamples(
					(int) (positiveEx.size() * NEGFACTOR), DEVELOP, FORCESIZEOFNEG));

			if(negativeEx.size()<0) {
				System.out.println(target);
				waitForInput();
				Files.appendFile(file, "\tSKIPPED negEX "+negativeEx+"\n");
				continue;
			}
			// reasoningService.prepareSubsumptionHierarchy();
			// System.out.println(reasoningService.getMoreGeneralConcepts(target));

			// for every class execute the learning algorithm
			try{
			learnOriginal(target, positiveEx, negativeEx);
			}catch (Exception e) {
				e.printStackTrace();
			}
			waitForInput();
			Files.appendFile(file, "*************\n");
			
		}

		sc.printAndSet("Finished");
		// JamonMonitorLogger.printAllSortedByLabel();

	}

	private static void learnOriginal(NamedClass target, SortedSet<Individual> posExamples, SortedSet<Individual> negExamples) {
		/*
		List<? extends EvaluatedDescription> conceptresults = new ArrayList<EvaluatedDescriptionPosNeg>();
		System.out.println("Starting to learn original");
		//System.out.println(ConfWriter.listExamples(true, posExamples));
		//System.out.println(ConfWriter.listExamples(false, negExamples));
		//System.exit(0);
//		LearnOWLFile learner = new LearnOWLFile(getConfForOriginal(target));
		
		LearningAlgorithm la = null;
		try{
//		la = learner.learn(
//				SetManipulation.indToString(posExamples), 
//				SetManipulation.indToString(negExamples), 
//				usedReasoner);
		la.start();
		}catch (Exception e) {
			System.out.println("ignoring the error "+e.toString());
			// TODO: handle exception
		}
		
		
		conceptresults = la.getCurrentlyBestEvaluatedDescriptions(5);
		for (EvaluatedDescription description : conceptresults) {
			Files.appendFile(file,"\t"+ description+"\n" );
		}
		*/
		
	}

	public static LearnSPARQLConfiguration getConfForSparql(NamedClass c) {
		LearnSPARQLConfiguration lc = new LearnSPARQLConfiguration();
		// lsc.sparqlEndpoint = sparqlTasks.getSparqlEndpoint();

		
		lc.recursiondepth = 2;
		lc.closeAfterRecursion = true;
		
		if(c!=null) lc.ignoredConcepts.add(c.toString());
		
		lc.noisePercentage = 0;
		lc.guaranteeXgoodDescriptions = 20;
		lc.maxExecutionTimeInSeconds = 0;
		
		boolean extended = false;
		lc.useAllConstructor = extended;
		lc.useCardinalityRestrictions = extended;
		lc.useNegation = extended;

		lc.useExistsConstructor = true;
		
		// lsc.searchTreeFile = "log/WikipediaCleaner.txt";

		return lc;

	}

	@SuppressWarnings("unused")
	private static LearnOWLFileConfiguration getConfForOriginal(NamedClass c) {
		LearnOWLFileConfiguration lc = new LearnOWLFileConfiguration();
		
		
		lc.setOWLFileURL(ontologyPath);
		
		
		if(c!=null) lc.ignoredConcepts.add(c.toString());
		

		lc.noisePercentage = 0;
		lc.guaranteeXgoodDescriptions = 20;
		lc.maxExecutionTimeInSeconds = 20;
		lc.writeSearchTree = false;
		lc.replaceSearchTree = true;
		lc.searchTreeFile = "log/treeSemanticBible.txt";

		return lc;

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
		Logger.getLogger(AbstractKnowledgeSource.class).setLevel(lwarn);
		Logger.getLogger(SparqlKnowledgeSource.class).setLevel(lwarn);

		Logger.getLogger(ExtractionAlgorithm.class).setLevel(lwarn);
		Logger.getLogger(AutomaticNegativeExampleFinderSPARQL.class).setLevel(
				lwarn);
		Logger.getLogger(AutomaticPositiveExampleFinderSPARQL.class).setLevel(
				lwarn);
		Logger.getLogger(OCEL.class).setLevel(lwarn);
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

	public static void waitForInput(){
		
		byte[] b = new byte[100];
		try{
			if(WAITFORINPUT){
				System.out.println("PRESS ENTER TO CONTINUE");
				System.in.read(b);
			}
		
		}catch (Exception e) {
			
		}
	}
}
