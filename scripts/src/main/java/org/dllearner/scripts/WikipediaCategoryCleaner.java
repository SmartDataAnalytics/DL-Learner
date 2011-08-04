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
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.configurators.ComponentFactory;
import org.dllearner.core.configurators.OCELConfigurator;
import org.dllearner.core.configurators.SparqlKnowledgeSourceConfigurator;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.extraction.ExtractionAlgorithm;
import org.dllearner.kb.extraction.Manager;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.scripts.improveWikipedia.ConceptSPARQLReEvaluator;
import org.dllearner.scripts.improveWikipedia.ConceptSelector;
import org.dllearner.scripts.improveWikipedia.WikipediaCategoryTasks;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.datastructures.SetManipulation;
import org.dllearner.utilities.examples.AutomaticNegativeExampleFinderSPARQL;
import org.dllearner.utilities.examples.AutomaticPositiveExampleFinderSPARQL;
import org.dllearner.utilities.statistics.SimpleClock;

public class WikipediaCategoryCleaner {

	private static SPARQLTasks sparqlTasks;

	private static Cache cache;

	private static Logger logger = Logger.getRootLogger();

	// localEndpoint switch
	private static final boolean LOCAL = true;

	// parameters
	// used for developing,
	private static final boolean DEVELOPSTABLESETS = true;

	public static final int SPARQL_RESULTSET_LIMITa = 500;
	public static final int SPARQL_RESULTSET_LIMIT_NEGATIVES = 20;
	public static final int SPARQL_RESULTSET_LIMIT_CONCEPT_REEVALUATE = 500;
	
	private static final int DEPTH_OF_RDFS = 0;

	// the 70/30 strategy was abandoned
	public static double PERCENT_OF_SKOSSET = 1.0;

	// size of randomly choosen negative examples compared to positives
	public static double NEGFACTOR = 1.0;

	public static int MAX_NR_CONCEPTS_TO_BE_EVALUATED = 20;

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
		//System.out.println(returnCat().size());
		//System.exit(0);
		//String test = "http://dbpedia.org/resource/Category:Prime_Ministers_of_the_United_Kingdom";
		
		//test = "http://dbpedia.org/resource/Category:Best_Actor_Academy_Award_winners";
		wikipediaCategories.addAll(returnCat());
		
		wikipediaCategories.add("http://dbpedia.org/resource/Category:Prime_Ministers_of_the_United_Kingdom");
		wikipediaCategories.add("http://dbpedia.org/resource/Category:Best_Actor_Academy_Award_winners");
	//	<http://dbpedia.org/resource/Category:Assassinated_monarchs>
	//		 <http://dbpedia.org/resource/Category:Alabama_musicians> 
	//	wikipediaCategories.add(test);
		
		int i = 0;
		for (String target : wikipediaCategories) {
			
		
				System.out.println(target);
			
				doit(target);
			ComponentManager.getInstance().freeAllComponents();
			System.out.println(i);
			i++;
		}

		sc.printAndSet("Finished");
		// JamonMonitorLogger.printAllSortedByLabel();

	}
	
	
	@SuppressWarnings("unchecked")
	private static void doit(String target) {
		try{
		String dir="";
		try{
			dir = "wiki/"+URLEncoder.encode(target,"UTF-8")+"/";
			Files.mkdir(dir);
		}catch (Exception e) {
			 e.printStackTrace();
		}
		List<EvaluatedDescriptionPosNeg> conceptresults;
		SortedSet<String> currentPOSITIVEex = new TreeSet<String>();
		SortedSet<String> currentNEGATIVEex = new TreeSet<String>();
		SortedSet<String> wrongIndividuals;

		WikipediaCategoryTasks wikiTasks;
		ConceptSPARQLReEvaluator csparql;
		System.out.println("test");
		wikiTasks = new WikipediaCategoryTasks(sparqlTasks);
		csparql = new ConceptSPARQLReEvaluator(sparqlTasks, DEPTH_OF_RDFS, SPARQL_RESULTSET_LIMIT_CONCEPT_REEVALUATE);

		// PHASE 1 *************

		wikiTasks.makeInitialExamples(target, PERCENT_OF_SKOSSET, NEGFACTOR,
				SPARQL_RESULTSET_LIMIT_NEGATIVES, DEVELOPSTABLESETS);
		currentPOSITIVEex.addAll(wikiTasks.getPosExamples());
		currentNEGATIVEex.addAll(wikiTasks.getNegExamples());
		OCEL la = learn(currentPOSITIVEex, currentNEGATIVEex);
		la.start();
		// get wrong individuals and reevaluate concepts
		conceptresults = (List<EvaluatedDescriptionPosNeg>) la.getCurrentlyBestEvaluatedDescriptions(Integer.MAX_VALUE, 0.5, true);
		// TODO select concepts
		conceptresults = selectConcepts(conceptresults);
		wrongIndividuals = wikiTasks.calculateWrongIndividualsAndNewPosEx(
				conceptresults, currentPOSITIVEex);
		
		writeList(dir+"wrongIndividuals.html",wrongIndividuals);
		currentPOSITIVEex.clear();
		currentPOSITIVEex.addAll(wikiTasks.getCleanedPositiveSet());

		writeList(dir+"correctIndividuals.html",currentPOSITIVEex);
		
		String content = "";
		for (EvaluatedDescription string : conceptresults) {
			content+=string+"\n";
			
		}
		content+=conceptresults.size()+"\n";
		Files.createFile(new File(dir+"concepts.html"), content);
		
		
		// reevaluate versus the Endpoint
		conceptresults = csparql.reevaluateConceptsByLowestRecall(
				conceptresults, currentPOSITIVEex);

		try{
			
			SortedSet<Individual> found = new TreeSet<Individual>( conceptresults.get(0).getNotCoveredPositives());
			writeList(dir+"foundIndividuals1.html",SetManipulation.indToString(found));
			
			found = new TreeSet<Individual>( conceptresults.get(1).getNotCoveredPositives());
			writeList(dir+"foundIndividuals2.html",SetManipulation.indToString(found));
			
			
		}catch (Exception e) {
			 e.printStackTrace();
		}
		
		WikipediaCategoryCleaner.printEvaluatedDescriptionCollection(5,
				conceptresults);
		
		}catch (Exception e) {
			 e.printStackTrace();
		}
		return;
		/*WikipediaCategoryCleaner.printEvaluatedDescriptionCollection(2,
				conceptresults);
*/
		/*printIntermediateResults(wikiTasks.getFullPositiveSet(),
				wikiTasks.getCleanedPositiveSet(),
				wrongIndividuals, conceptresults.size());*/
		
		//System.exit(0);
		// PHASE 2 ***********************
		/*logger.info("PHASE 2 ***********************");
		logger.info("making new Negative Examples");
		currentNEGATIVEex = wikiTasks.makeNewNegativeExamples(conceptresults,
				currentPOSITIVEex, NEGFACTOR);

		logger.info("learning");
		la = learn( currentPOSITIVEex, currentNEGATIVEex);
		conceptresults = la.getCurrentlyBestEvaluatedDescriptions(Integer.MAX_VALUE, 0.5, true);
		//		 TODO select concepts
		logger.info("reducing concept size before evaluating from "+conceptresults.size());
		conceptresults = selectConcepts(conceptresults);
			// reevaluate versus the Endpoint
		conceptresults = csparql.reevaluateConceptsByLowestRecall(
				conceptresults, currentPOSITIVEex);

		printEvaluatedDescriptionCollection(2, conceptresults);
		collectResults(wikiTasks);*/

	}
	
	
	private static void writeList(String file, Collection<String> c){
		String content = "";
		for (String string : c) {
			content+="<a href='"+string+"'>"+string+"</a><br>\n";
			
		}
		content+=c.size()+"\n";
		Files.createFile(new File(file), content);
	}

	@SuppressWarnings("unused")
	private static void collectResults(WikipediaCategoryTasks wikiTasks) {
		//logger.setLevel(Level.DEBUG);
		SetManipulation.printSet("fullpos", wikiTasks.getFullPositiveSet(), logger);
		
		SetManipulation.printSet("cleanedpos", wikiTasks.getCleanedPositiveSet(), logger);
		
		SetManipulation.printSet("wrongindividuals", wikiTasks.getDefinitelyWrongIndividuals(), logger);
		
	}

	private static List<EvaluatedDescriptionPosNeg> selectConcepts(
			List<EvaluatedDescriptionPosNeg> concepts) {
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

	
	private static OCEL learn( SortedSet<String> posExamples, SortedSet<String> negExamples) {
		
		
		OCEL la = null;
		try{
			SortedSet<Individual> instances = new TreeSet<Individual>();
			instances.addAll(SetManipulation.stringToInd(posExamples));
			instances.addAll(SetManipulation.stringToInd(negExamples));
	
			SparqlKnowledgeSource ks = ComponentFactory
					.getSparqlKnowledgeSource(URI.create(
							"http://dbpedia.org").toURL(), SetManipulation
							.indToString(instances));
	
			SparqlKnowledgeSourceConfigurator c = ks.getConfigurator();
			
			c.setCloseAfterRecursion(true);
			c.setRecursionDepth(1);
			if(LOCAL){
			c.setPredefinedEndpoint("LOCALDBPEDIA");
			}else{
				c.setPredefinedEndpoint("DBPEDIA");
			}
			c.setUseLits(false);
			c.setGetAllSuperClasses(true);
			c.setGetPropertyInformation(false);
			c.setVerbosity("warning");
			c.setCacheDir(Cache.getPersistantCacheDir());
			c.setPredefinedFilter("YAGOONLY");
			
			
			
			Set<AbstractKnowledgeSource> tmp = new HashSet<AbstractKnowledgeSource>();
			tmp.add(ks);
			// reasoner
			FastInstanceChecker f = ComponentFactory.getFastInstanceChecker(tmp);
			f.getConfigurator().setDefaultNegation(false);
			//OWLAPIReasoner f = ComponentFactory.getOWLAPIReasoner(tmp);
	
			// learning problem
			PosNegLPStandard lp = ComponentFactory.getPosNegLPStandard(f,
					posExamples, negExamples);
	
			// learning algorithm
			la = ComponentFactory.getOCEL(lp, f);
			OCELConfigurator lc = la.getConfigurator();
			la.getConfigurator().setNoisePercentage(20);
			la.getConfigurator().setGuaranteeXgoodDescriptions(100);
			la.getConfigurator().setMaxExecutionTimeInSeconds(50);
			
			lc.setUseAllConstructor(false);
			lc.setUseBooleanDatatypes(false);
			lc.setUseCardinalityRestrictions(false);
			lc.setUseNegation(false);
			lc.setUseHasValueConstructor(false);
			lc.setUseDoubleDatatypes(false);
			lc.setWriteSearchTree(true);
			lc.setSearchTreeFile("log/dbpedia.txt");
			lc.setReplaceSearchTree(true);
			
			ks.init();
			f.init();
			lp.init();
			la.init();
			
			
		}catch (Exception e) {
			 e.printStackTrace();
			 logger.warn(e);
			 logger.warn("error in sparqlprepare");
			 
		}
		
		return la;
		
	}

	
	

	public static void printEvaluatedDescriptionCollection(int howMany,
			Collection<EvaluatedDescriptionPosNeg> c) {
		int x = 0;
		Set<Individual> first = null;
		Set<Individual> tmp = new HashSet<Individual>();
		for (EvaluatedDescriptionPosNeg ed : c) {
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

	
	@SuppressWarnings("unused")
	private static void printIntermediateResults(
			SortedSet<String> fullSet, 
			SortedSet<String> correctIndividuals, 
			SortedSet<String> wrongIndividuals,
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

//		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		logger.removeAllAppenders();
		//logger.addAppender(consoleAppender);
		logger.addAppender(fileAppender);
		logger.setLevel(Level.DEBUG);
		Logger.getLogger(Manager.class).setLevel(Level.INFO);
		Level lwarn = Level.WARN;
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

	}

	@SuppressWarnings("unused")
	private static void findCat(){
		String q = "SELECT DISTINCT ?cat WHERE { ?a <http://www.w3.org/2004/02/skos/core#subject> ?cat  }";
		//System.out.println(q);
		SortedSet<String> s = sparqlTasks.queryAsSet(q, "cat");
		//System.out.println(s.size());
		//System.exit(0);
		
		SortedSet<String> results = new TreeSet<String>();
		int i = 0;
		for (String category : s) {
			System.out.println(""+(i++)+" "+results.size());
			
			String q2 = "SELECT DISTINCT ?subject WHERE { ?subject <http://www.w3.org/2004/02/skos/core#subject> <"+category+">  }";
			SortedSet<String> subj = sparqlTasks.queryAsSet(q2, "subject");
			if(40<subj.size() && subj.size()<80){
				results.add(category);
				
			}
			
			if(results.size()>200 || i>970){
				for (String cat : results) {
					System.out.println("cat.add(\""+cat+"\");");
				}
				System.exit(0);
			}
			//System.out.println(subj.size() +" "+ string);
		}
		System.exit(0);
	}
	
	private static SortedSet<String> returnCat (){
		SortedSet<String> cat = new TreeSet<String>();
		
		//cat.add("http://dbpedia.org/resource/Category:.NET_framework");
		/*cat.add("http://dbpedia.org/resource/Category:1948_songs");
		cat.add("http://dbpedia.org/resource/Category:1949_songs");
		cat.add("http://dbpedia.org/resource/Category:1951_songs");
		cat.add("http://dbpedia.org/resource/Category:1953_songs");
		cat.add("http://dbpedia.org/resource/Category:1961_songs");
		cat.add("http://dbpedia.org/resource/Category:1970s_pop_songs");
		cat.add("http://dbpedia.org/resource/Category:1991_introductions");
		cat.add("http://dbpedia.org/resource/Category:1993_introductions");
		cat.add("http://dbpedia.org/resource/Category:1995_introductions");
		cat.add("http://dbpedia.org/resource/Category:2001_television_films");
		cat.add("http://dbpedia.org/resource/Category:2008_establishments");
		cat.add("http://dbpedia.org/resource/Category:3-manifolds");*/
		cat.add("http://dbpedia.org/resource/Category:Al-Qaeda_activities");
		/*cat.add("http://dbpedia.org/resource/Category:Albums_produced_by_Teo_Macero");
		cat.add("http://dbpedia.org/resource/Category:American_accordionists");
		cat.add("http://dbpedia.org/resource/Category:American_comedy_musicians");
		cat.add("http://dbpedia.org/resource/Category:American_entertainers");
		cat.add("http://dbpedia.org/resource/Category:Apollo_asteroids");*/
		cat.add("http://dbpedia.org/resource/Category:Assassinated_monarchs");
		/*cat.add("http://dbpedia.org/resource/Category:Ayumi_Hamasaki_songs");
		cat.add("http://dbpedia.org/resource/Category:Best_Song_Academy_Award_winning_songs");
		cat.add("http://dbpedia.org/resource/Category:Books_about_film");
		cat.add("http://dbpedia.org/resource/Category:Brian_Eno_albums");
		cat.add("http://dbpedia.org/resource/Category:British_military_personnel_killed_in_action");
		cat.add("http://dbpedia.org/resource/Category:British_military_personnel_of_the_Falklands_War");
		cat.add("http://dbpedia.org/resource/Category:CENTR_members");*/
		cat.add("http://dbpedia.org/resource/Category:Companies_of_Finland");
		/*cat.add("http://dbpedia.org/resource/Category:Computing_platforms");
		cat.add("http://dbpedia.org/resource/Category:Coordination_compounds");
		cat.add("http://dbpedia.org/resource/Category:Dance-punk_musical_groups");
		cat.add("http://dbpedia.org/resource/Category:Deathgrind_musical_groups");
		cat.add("http://dbpedia.org/resource/Category:Disney_Channel_original_films");
		cat.add("http://dbpedia.org/resource/Category:Dutch_Eurovision_songs");
		cat.add("http://dbpedia.org/resource/Category:Executed_royalty");
		cat.add("http://dbpedia.org/resource/Category:Films_based_on_Stephen_King%27s_works");
		cat.add("http://dbpedia.org/resource/Category:First_Nations_history");*/
		cat.add("http://dbpedia.org/resource/Category:Fluorescent_dyes");
	/*	cat.add("http://dbpedia.org/resource/Category:ForeFront_Records_albums");
		cat.add("http://dbpedia.org/resource/Category:Former_municipalities_of_Utrecht_%28province%29");
		cat.add("http://dbpedia.org/resource/Category:Fred_Astaire_songs");
		cat.add("http://dbpedia.org/resource/Category:Home_computer_magazines");
		cat.add("http://dbpedia.org/resource/Category:Honolulu_County%2C_Hawaii");
		cat.add("http://dbpedia.org/resource/Category:House_of_Hashim");
		cat.add("http://dbpedia.org/resource/Category:Hugo_Award_Winner_for_Best_Short_Story");*/
		cat.add("http://dbpedia.org/resource/Category:Irish_folk_songs");
		cat.add("http://dbpedia.org/resource/Category:Islands_of_Tonga");
		/*cat.add("http://dbpedia.org/resource/Category:James_Bond");
		cat.add("http://dbpedia.org/resource/Category:Jason_Nevins_remixes");
		cat.add("http://dbpedia.org/resource/Category:Jay-Z_songs");
		cat.add("http://dbpedia.org/resource/Category:Jo_Stafford_songs");
		cat.add("http://dbpedia.org/resource/Category:.NET_framework");
		cat.add("http://dbpedia.org/resource/Category:1930_songs");
		cat.add("http://dbpedia.org/resource/Category:1945_songs");
		cat.add("http://dbpedia.org/resource/Category:1948_songs");
		cat.add("http://dbpedia.org/resource/Category:1949_songs");
		cat.add("http://dbpedia.org/resource/Category:1951_songs");
		cat.add("http://dbpedia.org/resource/Category:1953_songs");
		cat.add("http://dbpedia.org/resource/Category:1955_songs");
		cat.add("http://dbpedia.org/resource/Category:1956_singles");
		cat.add("http://dbpedia.org/resource/Category:1961_songs");
		cat.add("http://dbpedia.org/resource/Category:1970s_pop_songs");
		cat.add("http://dbpedia.org/resource/Category:1980s_pop_songs");
		cat.add("http://dbpedia.org/resource/Category:1991_introductions");
		cat.add("http://dbpedia.org/resource/Category:1993_introductions");
		cat.add("http://dbpedia.org/resource/Category:1995_introductions");
		cat.add("http://dbpedia.org/resource/Category:2001_television_films");
		cat.add("http://dbpedia.org/resource/Category:2008_establishments");
		cat.add("http://dbpedia.org/resource/Category:3-manifolds");
		cat.add("http://dbpedia.org/resource/Category:Agriculture_in_California");
		cat.add("http://dbpedia.org/resource/Category:Al-Qaeda_activities");
		cat.add("http://dbpedia.org/resource/Category:Albums_produced_by_Brendan_O%27Brien");
		cat.add("http://dbpedia.org/resource/Category:Albums_produced_by_Teo_Macero");
		cat.add("http://dbpedia.org/resource/Category:American_accordionists");
		cat.add("http://dbpedia.org/resource/Category:American_children%27s_television_series");
		cat.add("http://dbpedia.org/resource/Category:American_comedy_musicians");
		cat.add("http://dbpedia.org/resource/Category:American_entertainers");
		cat.add("http://dbpedia.org/resource/Category:Apollo_asteroids");
		cat.add("http://dbpedia.org/resource/Category:Aromatic_amines");
		cat.add("http://dbpedia.org/resource/Category:Assassinated_monarchs");
		cat.add("http://dbpedia.org/resource/Category:Ayumi_Hamasaki_songs");
		cat.add("http://dbpedia.org/resource/Category:Baden-W%C3%BCrttemberg_football_clubs");
		cat.add("http://dbpedia.org/resource/Category:Bavarian_football_clubs");
		cat.add("http://dbpedia.org/resource/Category:Beastie_Boys_songs");
		cat.add("http://dbpedia.org/resource/Category:Best_Song_Academy_Award_winning_songs");
		cat.add("http://dbpedia.org/resource/Category:Books_about_film");
		cat.add("http://dbpedia.org/resource/Category:Brian_Eno_albums");
		cat.add("http://dbpedia.org/resource/Category:British_military_personnel_killed_in_action");
		cat.add("http://dbpedia.org/resource/Category:British_military_personnel_of_the_Falklands_War");
		cat.add("http://dbpedia.org/resource/Category:CENTR_members");
		cat.add("http://dbpedia.org/resource/Category:Chemical_nomenclature");
		cat.add("http://dbpedia.org/resource/Category:Climatology");
		cat.add("http://dbpedia.org/resource/Category:Common_Lisp_software");
		cat.add("http://dbpedia.org/resource/Category:Companies_based_in_Utah");
		cat.add("http://dbpedia.org/resource/Category:Companies_based_on_Long_Island");
		cat.add("http://dbpedia.org/resource/Category:Companies_of_Finland");
		cat.add("http://dbpedia.org/resource/Category:Computing_platforms");*/
		cat.add("http://dbpedia.org/resource/Category:Concurrent_programming_languages");
		/*cat.add("http://dbpedia.org/resource/Category:Coordination_compounds");
		cat.add("http://dbpedia.org/resource/Category:Dance-punk_musical_groups");
		cat.add("http://dbpedia.org/resource/Category:Deathgrind_musical_groups");
		cat.add("http://dbpedia.org/resource/Category:Defunct_German_football_clubs");
		cat.add("http://dbpedia.org/resource/Category:Digital_media");
		cat.add("http://dbpedia.org/resource/Category:Disney_Channel_original_films");
		cat.add("http://dbpedia.org/resource/Category:Dutch_Eurovision_songs");
		cat.add("http://dbpedia.org/resource/Category:Dynamically-typed_programming_languages");
		cat.add("http://dbpedia.org/resource/Category:EC_1.3.1");
		cat.add("http://dbpedia.org/resource/Category:EC_3.1.1");
		cat.add("http://dbpedia.org/resource/Category:EC_3.1.3");
		cat.add("http://dbpedia.org/resource/Category:EC_3.2.1");
		cat.add("http://dbpedia.org/resource/Category:Executed_royalty");
		cat.add("http://dbpedia.org/resource/Category:Explosive_chemicals");
		cat.add("http://dbpedia.org/resource/Category:Failed_pilots");
		cat.add("http://dbpedia.org/resource/Category:Film_sound_production");
		cat.add("http://dbpedia.org/resource/Category:Films_based_on_Stephen_King%27s_works");
		cat.add("http://dbpedia.org/resource/Category:First_Nations_history");
		cat.add("http://dbpedia.org/resource/Category:Fluorescent_dyes");
		cat.add("http://dbpedia.org/resource/Category:Football_%28soccer%29_clubs_established_in_1896");
		cat.add("http://dbpedia.org/resource/Category:Football_%28soccer%29_clubs_established_in_1899");
		cat.add("http://dbpedia.org/resource/Category:Football_%28soccer%29_clubs_established_in_1905");
		cat.add("http://dbpedia.org/resource/Category:ForeFront_Records_albums");
		cat.add("http://dbpedia.org/resource/Category:Former_municipalities_of_Utrecht_%28province%29");
		cat.add("http://dbpedia.org/resource/Category:Fred_Astaire_songs");
		cat.add("http://dbpedia.org/resource/Category:GMA_News_and_Public_Affairs");
		cat.add("http://dbpedia.org/resource/Category:Genetic_genealogy");
		cat.add("http://dbpedia.org/resource/Category:Hazardous_air_pollutants");
		cat.add("http://dbpedia.org/resource/Category:Hessian_football_clubs");
		cat.add("http://dbpedia.org/resource/Category:Home_computer_magazines");
		cat.add("http://dbpedia.org/resource/Category:Honolulu_County%2C_Hawaii");
		cat.add("http://dbpedia.org/resource/Category:House_of_Hashim");
		cat.add("http://dbpedia.org/resource/Category:Hugo_Award_Winner_for_Best_Short_Story");
		cat.add("http://dbpedia.org/resource/Category:Hungarian_football_clubs");
		cat.add("http://dbpedia.org/resource/Category:Hydra_Head_Records_albums");
		cat.add("http://dbpedia.org/resource/Category:Irish_folk_songs");
		cat.add("http://dbpedia.org/resource/Category:Iron_compounds");
		cat.add("http://dbpedia.org/resource/Category:Islands_of_Tonga");
		cat.add("http://dbpedia.org/resource/Category:James_Bond");
		cat.add("http://dbpedia.org/resource/Category:James_Bond_books");
		cat.add("http://dbpedia.org/resource/Category:Jason_Nevins_remixes");
		cat.add("http://dbpedia.org/resource/Category:Jay-Z_songs");
		cat.add("http://dbpedia.org/resource/Category:Jo_Stafford_songs");
		cat.add("http://dbpedia.org/resource/Category:Lie_algebras");
		cat.add("http://dbpedia.org/resource/Category:Light_novels");
		cat.add("http://dbpedia.org/resource/Category:Lisp_programming_language_family");
		cat.add("http://dbpedia.org/resource/Category:Live_Music_Archive_artists");
		cat.add("http://dbpedia.org/resource/Category:Mary_J._Blige_songs");
		cat.add("http://dbpedia.org/resource/Category:Maze_games");
		cat.add("http://dbpedia.org/resource/Category:Monomers");
		cat.add("http://dbpedia.org/resource/Category:Muppets_songs");
		cat.add("http://dbpedia.org/resource/Category:Music_videos_directed_by_Joseph_Kahn");
		cat.add("http://dbpedia.org/resource/Category:Musical_groups_disestablished_in_2002");
		cat.add("http://dbpedia.org/resource/Category:Musical_groups_disestablished_in_2005");
		cat.add("http://dbpedia.org/resource/Category:Nebula_Award_winning_works");
		cat.add("http://dbpedia.org/resource/Category:Neighborhoods_in_Honolulu");
		cat.add("http://dbpedia.org/resource/Category:Nitro_compounds");
		cat.add("http://dbpedia.org/resource/Category:Number-one_singles_in_Finland");*/
		cat.add("http://dbpedia.org/resource/Category:Nuremberg");
		/*cat.add("http://dbpedia.org/resource/Category:Organobromides");
		cat.add("http://dbpedia.org/resource/Category:Organometallic_compounds");
		cat.add("http://dbpedia.org/resource/Category:Oricon_International_Singles_Chart_number-one_singles");
		cat.add("http://dbpedia.org/resource/Category:Oxygen_heterocycles");
		cat.add("http://dbpedia.org/resource/Category:Parody_musicians");
		cat.add("http://dbpedia.org/resource/Category:Pearl_Jam_songs");
		cat.add("http://dbpedia.org/resource/Category:Perry_Como_songs");
		cat.add("http://dbpedia.org/resource/Category:Pesticides");
		cat.add("http://dbpedia.org/resource/Category:Philadelphia_in_film_and_television");
		cat.add("http://dbpedia.org/resource/Category:Piperazines");
		cat.add("http://dbpedia.org/resource/Category:Placename_etymologies");
		cat.add("http://dbpedia.org/resource/Category:R.E.M._songs");
		cat.add("http://dbpedia.org/resource/Category:Ramallah_and_al-Bireh_Governorate");
		cat.add("http://dbpedia.org/resource/Category:Reagents_for_organic_chemistry");
		cat.add("http://dbpedia.org/resource/Category:Rearrangement_reactions");
		cat.add("http://dbpedia.org/resource/Category:Relapse_Records_albums");
		cat.add("http://dbpedia.org/resource/Category:Richard_Cheese_and_Lounge_Against_the_Machine_songs");
		cat.add("http://dbpedia.org/resource/Category:Rod_Stewart_songs");
		cat.add("http://dbpedia.org/resource/Category:SRC_network_shows");*/
		cat.add("http://dbpedia.org/resource/Category:Satirical_magazines");
		/*cat.add("http://dbpedia.org/resource/Category:Short_stories_by_Robert_A._Heinlein");
		cat.add("http://dbpedia.org/resource/Category:Simple_aromatic_rings");
		cat.add("http://dbpedia.org/resource/Category:Software_companies_of_Canada");
		cat.add("http://dbpedia.org/resource/Category:Songs_about_California");
		cat.add("http://dbpedia.org/resource/Category:Songs_with_lyrics_by_Ira_Gershwin");
		cat.add("http://dbpedia.org/resource/Category:Songs_with_music_by_George_Gershwin");
		cat.add("http://dbpedia.org/resource/Category:Sony_BMG_artists");
		cat.add("http://dbpedia.org/resource/Category:Sound_production_technology");
		cat.add("http://dbpedia.org/resource/Category:Speakers");
		cat.add("http://dbpedia.org/resource/Category:Sport_in_North_Rhine-Westphalia");
		cat.add("http://dbpedia.org/resource/Category:Supersymmetry");
		cat.add("http://dbpedia.org/resource/Category:Synthpop_songs");
		cat.add("http://dbpedia.org/resource/Category:Taliban");
		cat.add("http://dbpedia.org/resource/Category:Techno_dance_songs");
		cat.add("http://dbpedia.org/resource/Category:The_Temptations_songs");
		cat.add("http://dbpedia.org/resource/Category:Thiols");
		cat.add("http://dbpedia.org/resource/Category:Toponymy");
		cat.add("http://dbpedia.org/resource/Category:Toronto_television_series");*/
		
		return cat;

	}
	
}
