package org.dllearner.scripts.tiger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.refinement2.ROLComponent2;
import org.dllearner.algorithms.refinement2.ROLearner2;
import org.dllearner.core.ComponentPool;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.configurators.ComponentFactory;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.kb.sparql.SparqlQueryDescriptionConvertVisitor;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.examples.ExMakerFixedSize;
import org.dllearner.utilities.examples.ExampleDataCollector;
import org.dllearner.utilities.examples.Examples;

import com.jamonapi.Monitor;

public class GlobalTest {
	private static final Logger logger = Logger.getLogger(GlobalTest.class);

	static DecimalFormat df = new DecimalFormat("00.###%");

	static String backgroundXML = "files/tiger.noSchema.noImports.rdf";
	static String propertiesXML = "files/propertiesOnly.rdf";
	static String sentenceXMLFolder = "files/tiger/";
	static String sentenceprefix = "http://nlp2rdf.org/ontology/s";
	static String prefix = "http://nlp2rdf.org/ontology/";

	static String active = "files/active_all_sentenceNumbers.txt";
	static String passiveNoZU = "files/passive_noZuInf_sentenceNumbers.txt";
	static String passiveWithZu = "files/passive_zuInf_sentenceNumbers.txt";
	static String test_has_pos = "files/test_has_pos.txt";
	static String test_has_neg = "files/test_has_neg.txt";

	static SparqlEndpoint sparqlEndpoint;
	static SPARQLTasks sparqlTasks;

	static String sparqlEndpointURL = "http://db0.aksw.org:8893/sparql";
	static String graph = "http://nlp2rdf.org/tiger";
	static String rulegraph = "http://nlp2rdf.org/schema/rules1";
	
	
	
	
	
	final static boolean  debug = false;
	//no randomization in examples
	final static boolean  randomizedebug = !debug;

	public static void main(String[] args) {
		LogHelper.initLoggers();
		Logger.getLogger(Cache.class).setLevel(Level.INFO);
		Logger.getLogger(ComponentPool.class).setLevel(Level.INFO);
		Logger.getLogger(ROLearner2.class).setLevel(Level.TRACE);
		Logger.getLogger(RhoDRDown.class).setLevel(Level.TRACE);
		Logger.getLogger(SparqlQuery.class).setLevel(Level.INFO);

		try {
			sparqlEndpoint = new SparqlEndpoint(new URL(sparqlEndpointURL), new ArrayList<String>(Arrays
					.asList(new String[] { graph })), new ArrayList<String>());
			sparqlTasks = new SPARQLTasks(Cache.getDefaultCache(), sparqlEndpoint);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Examples allExamples = new Examples();
		SortedSet<String> positives;
		SortedSet<String> negatives;

//		positives = read(passiveWithZu);
		positives =  read(passiveNoZU);
		negatives = read(active);
		
		//removing overlap
		positives.removeAll(negatives);
		negatives.removeAll(positives);

//		System.out.println(Helper.intersection(passiveZuInfSentences, activeSentences));
//		System.out.println(Helper.intersection(passiveZuInfSentences, passiveNoZuSentences));
//		System.out.println(Helper.intersection(activeSentences, passiveNoZuSentences));
		allExamples.addPosTrain(positives);
		allExamples.addNegTrain(negatives);

		logger.debug("All examples \n"+allExamples);
		
		ExperimentConfig config = new ExperimentConfig();
		firstContact( allExamples, config);
		JamonMonitorLogger.writeHTMLReport("log/tiger.html");
		//retrieved wird neues Example, als schnittmenge mit all 
		//und den bisher gewaehlten
		//dann splits auswählen und
		//pos und neg wieder hinzufuegen

	}
	
	public static void firstContact(Examples allExamples, ExperimentConfig config){
		ExMakerFixedSize fs = new ExMakerFixedSize(allExamples, randomizedebug);
		Examples learn = fs.select(config.initialsplits, config.initialsplits);
		logger.debug("Intial training set \n"+learn);
//		System.out.println(learn.getPosTrain());
//		System.out.println(learn.getNegTrain());
//		if (true) {
//			System.exit(0);
//		}
//		int size = 0;
		for(int i = 0 ; config.stopCondition(i, learn) ;i++ ) {
			/*LEARNING*/
			EvaluatedDescription ed = learn(learn, config);
			
			/*RETRIEVING*/
			SortedSet<String> retrieved = getSentences(ed, config.resultLimit);
			logger.debug("Retrieved "+retrieved.size()+" sentences");
			
			
			/*MASHING*/
			//Menge aller positiven geschn. mit den gefundenen
			SortedSet<String> posAsPos = Helper.intersection(retrieved, allExamples.getPosTrain());
			logger.debug("Number of retrieved positives: "+posAsPos.size());
			logger.debug("Number of total positives: "+allExamples.getPosTrain().size());
			results(posAsPos, retrieved, allExamples);
			
			//Menge aller positiven geschn. mit den gefundenen
			SortedSet<String> negAsPos = Helper.intersection(retrieved, allExamples.getNegTrain());
			logger.debug("Number of retrieved negatives: "+negAsPos.size());
			logger.debug("Total: "+posAsPos.size()+" + "+negAsPos.size() +" = "+retrieved.size());
			
//			if(retrieved.size()!=(posAsPos.size()+negAsPos.size())){
//				logger.warn("sets are  wrong");
//				System.exit(0);
//			}
			
			Examples newlyFound = new Examples();
			newlyFound.addPosTrain(Helper.intersection(retrieved, learn.getPosTest()));
			newlyFound.addNegTrain(Helper.intersection(retrieved, learn.getNegTest()));
			//validate here
			
			fs = new ExMakerFixedSize(newlyFound, randomizedebug);
			newlyFound = fs.select(config.splits, config.splits);
			
			learn.addPosTrain(newlyFound.getPosTrain());
			learn.addNegTrain(newlyFound.getNegTrain());
			logger.debug("Next training set \n"+learn);
//			size =  learn.getPosTrain().size() + learn.getNegTrain().size();
			
		}
		
		
		
		
		
	}
	
	private static void results(SortedSet<String> posAsPos, SortedSet<String> retrieved, Examples allExamples) {
		double precision = precision( posAsPos.size(), retrieved.size());
		double recall = recall( posAsPos.size(),allExamples.getPosTrain().size());
		logger.info("F-Measure: "+df.format(   (2*precision*recall)/(precision+recall))  );
		
	}

	public static double precision( int posAsPos, int retrieved){
		double precision = ((double)posAsPos)/((double)retrieved);
		logger.info("Precision: "+df.format(precision));
		return precision;
	}
	public static double recall( int posAsPos, int allPositives){
		double recall = ((double)posAsPos)/((double)allPositives);
		
		logger.info("Recall: "+df.format(recall));
		return recall;
		
	}

	private static Set<KnowledgeSource> _getOWL(Examples ex) throws Exception{
		Set<KnowledgeSource> tmp = new HashSet<KnowledgeSource>();
		List<URL> urls = new ArrayList<URL>();
		urls.addAll(ExampleDataCollector.convert(sentenceXMLFolder, ex.getPosTrain()));
		urls.addAll(ExampleDataCollector.convert(sentenceXMLFolder, ex.getNegTrain()));
		urls.add(new File(backgroundXML).toURI().toURL());

		for (URL u : urls) {
			OWLFile ks = ComponentFactory.getOWLFile(u);
			tmp.add(ks);
		}
		return tmp;
	}
	@SuppressWarnings("unused")
	private static Set<KnowledgeSource> _getSPARQL(Examples ex) throws Exception{
		Set<KnowledgeSource> tmp = new HashSet<KnowledgeSource>();

		Set<String> examples = new TreeSet<String>();
		examples.addAll(ex.getPosTrain());
		examples.addAll(ex.getNegTrain());
		SparqlKnowledgeSource ks = ComponentFactory.getSparqlKnowledgeSource(new URL(sparqlEndpointURL), examples);
		ks.getConfigurator().setUrl(new URL(sparqlEndpointURL));
		ks.getConfigurator().setDefaultGraphURIs(new HashSet<String>(Arrays.asList(new String[]{graph})));
		ks.getConfigurator().setInstances(examples);
		ks.getConfigurator().setDissolveBlankNodes(false);
		ks.getConfigurator().setRecursionDepth(2);
		ks.getConfigurator().setDissolveBlankNodes(false);
		ks.getConfigurator().setCloseAfterRecursion(true);
		ks.getConfigurator().setGetAllSuperClasses(true);
		ks.getConfigurator().setGetPropertyInformation(false);
		ks.getConfigurator().setUseLits(true);
//		ks.getConfigurator().
		OWLFile ks2 = ComponentFactory.getOWLFile(new File(propertiesXML).toURI().toURL());
		tmp.add(ks);
		tmp.add(ks2);
		
		return tmp;
	}
	
	//test if virtuoso is correct
	public static void validate(Description d, Examples newlyFound){
		try {
		ExMakerFixedSize fs = new ExMakerFixedSize(newlyFound);
		Examples tmp = fs.select(100, 100);
		FastInstanceChecker fc = _getFastInstanceChecker(tmp);
		SortedSet<Individual> inds = fc.getIndividuals(d);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static FastInstanceChecker _getFastInstanceChecker(Examples ex)throws Exception{
			Set<KnowledgeSource> tmp = _getOWL(ex);
//			Set<KnowledgeSource> tmp = _getSPARQL(ex);
			

			FastInstanceChecker rc = ComponentFactory.getFastInstanceChecker(tmp);
			for (KnowledgeSource ks : tmp) {
				ks.init();
			}
			rc.init();
			return rc;
	}
	
	public static EvaluatedDescription learn(Examples ex, ExperimentConfig config) {
		Monitor init = JamonMonitorLogger.getTimeMonitor(GlobalTest.class, "init").start();

		EvaluatedDescription result = null;
		
		try {
			FastInstanceChecker rc = _getFastInstanceChecker(ex);
			PosNegLPStandard lp = ComponentFactory
					.getPosNegLPStandard(rc, ex.getPosTrain(), ex.getNegTrain());
			LearningAlgorithm la = _getROLLearner(lp, rc, config, ex);
			lp.init();
			la.init();
			init.stop();
			Monitor learning = JamonMonitorLogger.getTimeMonitor(GlobalTest.class, "learning")
					.start();
			la.start();
			learning.stop();

			result = la.getCurrentlyBestEvaluatedDescription();
			logger.debug(PrefixMap.toKBSyntaxString(result.getDescription()));
			logger.debug(PrefixMap.toManchesterSyntaxString(result.getDescription()));
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return result;
	}

	public static SortedSet<String> getSentences(EvaluatedDescription ed, int resultLimit) {
		SortedSet<String> result = new TreeSet<String>();
		SparqlQueryDescriptionConvertVisitor visit = new SparqlQueryDescriptionConvertVisitor();
		visit.setDistinct(true);
		visit.setLabels(false);
		visit.setLimit(resultLimit);
		String sparqlQuery = "";
		try {
			sparqlQuery = visit.getSparqlQuery(ed.getDescription());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		logger.debug(PrefixMap.toKBSyntaxString(ed.getDescription()));
		sparqlQuery = " \n define input:inference \"" + rulegraph + "\" \n" + "" + sparqlQuery;
		logger.debug(sparqlQuery);

		Monitor m = JamonMonitorLogger.getTimeMonitor(GlobalTest.class, "sparqlquery").start();
		result.addAll(sparqlTasks.queryAsSet(sparqlQuery, "subject"));
		m.stop();
		logger.debug("query avg: " + ((double)m.getAvg() / (double)1000)+ " seconds (last: "+((double)m.getLastValue() / (double)1000)+")");
		if(result.isEmpty()){
			
			logger.error("sparql query returned no results ");
			logger.error(sparqlQuery);
			System.exit(0);
		}
		return result;
	}

	private static LearningAlgorithm _getROLLearner(LearningProblem lp, ReasonerComponent rc, ExperimentConfig config, Examples ex)
			throws Exception {
		
		int maxExecutionTime = config.maxExecutionTime;
		int valueFrequencyThreshold = config.valueFrequencyThreshold;
		if(config.adaptive){
			maxExecutionTime = 2 * ex.sizeOfTrainingSets();
			valueFrequencyThreshold = ex.getPosTrain().size();
//			valueFrequencyThreshold = (int) Math.floor(0.8d*((double)ex.getPosTrain().size()));
			
		}
		
		ROLComponent2 la = ComponentFactory.getROLComponent2(lp, rc);
		la.getConfigurator().setUseExistsConstructor(true);

		la.getConfigurator().setUseAllConstructor(false);
		la.getConfigurator().setUseCardinalityRestrictions(false);
		la.getConfigurator().setUseNegation(false);
		la.getConfigurator().setUseHasValueConstructor(false);
		la.getConfigurator().setUseDataHasValueConstructor(true);
		la.getConfigurator().setValueFrequencyThreshold(valueFrequencyThreshold);
		
		la.getConfigurator().setIgnoredConcepts(new HashSet<String>(Arrays.asList(new String[]{
				"http://nlp2rdf.org/ontology/sentencefinalpunctuation_tag",
				"http://nlp2rdf.org/ontology/comma_tag",
				"http://nachhalt.sfb632.uni-potsdam.de/owl/stts.owl#SentenceFinalPunctuation"
		})));
		

		la.getConfigurator().setNoisePercentage(config.noise);
		la.getConfigurator().setTerminateOnNoiseReached(true);
		la.getConfigurator().setMaxExecutionTimeInSeconds(maxExecutionTime);
		
		if(config.useStartClass){
			la.getConfigurator().setStartClass(prefix + "Sentence");
		}
		
		 la.getConfigurator().setWriteSearchTree(config.searchTree);
		 la.getConfigurator().setSearchTreeFile("log/searchTreeTiger.txt");
		 la.getConfigurator().setReplaceSearchTree(true);
		return la;
	}

	public static SortedSet<String> read(String f) {
		SortedSet<String> result = new TreeSet<String>();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(f)));

			String line;
			while ((line = in.readLine()) != null) {
				try {
					line = line.trim();
					Integer.parseInt(line);
					if (!result.add(sentenceprefix + line)) {
						logger.error("reading failed");
						System.exit(0);
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Could not read examples from: " + f);
			System.exit(0);

		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		logger.info("read " + result.size() + " lines from " + f);

		return result;
	}

}
