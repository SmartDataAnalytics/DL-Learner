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
import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.algorithms.ocel.ROLearner2;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.ComponentPool;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.kb.sparql.SparqlQueryDescriptionConvertVisitor;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.examples.ExampleDataCollector;
import org.dllearner.utilities.experiments.ExMakerCrossFolds;
import org.dllearner.utilities.experiments.ExMakerFixedSize;
import org.dllearner.utilities.experiments.ExMakerRandomizer;
import org.dllearner.utilities.experiments.Examples;
import org.dllearner.utilities.experiments.Table;

import com.jamonapi.MonKeyImp;
import com.jamonapi.Monitor;

public class TestIterativeLearning {
	private static final Logger logger = Logger.getLogger(TestIterativeLearning.class);

	static DecimalFormat df = new DecimalFormat("00.###%");
	public static DecimalFormat dftime = new DecimalFormat("#####.#");

	public static boolean newTiger = false;
	
	// static String backgroundXML = "files/tiger.noSchema.noImports.rdf";
	static String backgroundXML = "files/VirtuosoSyntaxSchema.rdf";
	static String sparqlEndpointURL ="http://db0.aksw.org:8893/sparql";
	static String graph = (newTiger)?"http://nlp2rdf.org/tigerFull":"http://nlp2rdf.org/tiger";
	static String rulegraph = (newTiger)?"http://nlp2rdf.org/schemaFull/rules1":"http://nlp2rdf.org/schema/rules1";
//	static String propertiesXML = "files/propertiesOnly.rdf";
	static String sentenceXMLFolder = "files/tiger/";
	static String resultFolder = "tigerResults/";

	static String sentenceprefix = "http://nlp2rdf.org/ontology/s";
	static String prefix = "http://nlp2rdf.org/ontology/";

	static String active = "files/active_all_sentenceNumbers.txt";
	static String passiveNoZU = "files/passive_noZuInf_sentenceNumbers.txt";
	static String passiveWithZu = "files/passive_zuInf_sentenceNumbers.txt";
	static String test_has_pos = "files/test_has_pos.txt";
	static String test_has_neg = "files/test_has_neg.txt";

	static SparqlEndpoint sparqlEndpoint;
	static SPARQLTasks sparqlTasks;

	

	static MonKeyImp logFMeasure = new MonKeyImp("F-Measure", JamonMonitorLogger.PERCENTAGE);
	static MonKeyImp logPrecision = new MonKeyImp("Precision", JamonMonitorLogger.PERCENTAGE);
	static MonKeyImp logRecall = new MonKeyImp("Recall", JamonMonitorLogger.PERCENTAGE);
	static MonKeyImp logAccuracy = new MonKeyImp("Accuracy", JamonMonitorLogger.PERCENTAGE);

	static MonKeyImp logLearningTime = new MonKeyImp("Learning Time", JamonMonitorLogger.MS);
	static MonKeyImp logIterationTime = new MonKeyImp("Iteration Time", JamonMonitorLogger.MS);
	static MonKeyImp nrOfRetrievedInstances = new MonKeyImp("Nr of retrieved Instances", JamonMonitorLogger.COUNT);

	static List<MonKeyImp> mks = new ArrayList<MonKeyImp>(Arrays.asList(new MonKeyImp[] { logPrecision,
			logRecall, logFMeasure, logAccuracy, logLearningTime, logIterationTime, nrOfRetrievedInstances}));

	static int iterations = 4;
	static int folds = 10;
	static int printSentences = 3;
	
	static SortedSet<String> concepts = new TreeSet<String>();

	// no randomization in examples

	public static void main(String[] args) {
		LogHelper.initLoggers();
		Logger.getLogger(Cache.class).setLevel(Level.INFO);
		Logger.getLogger(ComponentPool.class).setLevel(Level.INFO);
		Logger.getLogger(ROLearner2.class).setLevel(Level.INFO);
		Logger.getLogger(RhoDRDown.class).setLevel(Level.INFO);
		Logger.getLogger(SparqlQuery.class).setLevel(Level.INFO);

		try {
			sparqlEndpoint = new SparqlEndpoint(new URL(sparqlEndpointURL), new ArrayList<String>(Arrays
					.asList(new String[] { graph })), new ArrayList<String>());
			sparqlTasks = new SPARQLTasks(Cache.getDefaultCache(), sparqlEndpoint);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		long n = System.currentTimeMillis();
//			passiveNoZU();
			passiveWithZu();

		 String a="\n";
		 for(String s: concepts){
			 a+=s+"\n";
		 }
		 logger.info(a);
		 
		logger.info("finished, needed: " + (System.currentTimeMillis() - n));
		JamonMonitorLogger.writeHTMLReport("log/tiger.html");

	}

	public static void passiveNoZU() {

		SortedSet<String> positives = read(passiveNoZU);
		SortedSet<String> negatives = read(active);

		
		
		// removing overlap
		positives.removeAll(negatives);
		negatives.removeAll(positives);

		Examples allExamples = new Examples();
		allExamples.addPosTrain(positives);
		allExamples.addNegTrain(negatives);

		logger.debug("All examples \n" + allExamples);

		List<Examples> folds = new ExMakerCrossFolds(allExamples)
				.splitLeaveOneOut(TestIterativeLearning.folds);
		
		/*CLEANUP*/
		positives = null;
		negatives = null;
		allExamples = null;
		
		List<IteratedConfig> configs = getConfigsNo();
		Table masterTable = new Table();
		for (IteratedConfig experimentConfig : configs) {
			experimentConfig.init(mks);
			logger.info("next: passiveNoZU." + experimentConfig.experimentName);
			int i = 1;
			for (Examples examples : folds) {
				logger.info("beginning fold: " + (i++));
				conductExperiment(examples, experimentConfig);
			}
			Table expTable = new Table();
			expTable.addTableRowColumns(experimentConfig.getTableRows());
			expTable.write(resultFolder, "passiveNoZu_"+experimentConfig.experimentName);
			masterTable.addTable(expTable);
			masterTable.write(resultFolder, "passiveNoZu_master");
			
			JamonMonitorLogger.writeHTMLReport("/tmp/tiger.html");
			logger.info(experimentConfig);

		}

	}

	public static void passiveWithZu() {
		SortedSet<String> positives = read(passiveWithZu);
		SortedSet<String> negatives = read(active);
		negatives.addAll(read(passiveNoZU));

		SortedSet<String> positivesTMP = new TreeSet<String>();
		positivesTMP.addAll(positives);
		
		// removing overlap
		positives.removeAll(negatives);
		negatives.removeAll(positivesTMP);

		Examples allExamples = new Examples();
		allExamples.addPosTrain(positives);
		allExamples.addNegTrain(negatives);

		logger.debug("All examples \n" + allExamples);

		List<Examples> runs = new ArrayList<Examples>();
		runs.add(new ExMakerRandomizer(allExamples).split(0.7d));
		runs.add(new ExMakerRandomizer(allExamples).split(0.7d));
		runs.add(new ExMakerRandomizer(allExamples).split(0.7d));
		runs.add(new ExMakerRandomizer(allExamples).split(0.7d));
		runs.add(new ExMakerRandomizer(allExamples).split(0.7d));
		runs.add(new ExMakerRandomizer(allExamples).split(0.7d));
		runs.add(new ExMakerRandomizer(allExamples).split(0.7d));
		runs.add(new ExMakerRandomizer(allExamples).split(0.7d));
		runs.add(new ExMakerRandomizer(allExamples).split(0.7d));
		runs.add(new ExMakerRandomizer(allExamples).split(0.7d));
//		
		/*CLEANUP*/
		positives = null;
		positivesTMP = null;
		negatives = null;
		allExamples = null;

		List<IteratedConfig> configs = getConfigsZU();
		Table masterTable = new Table();
		for (IteratedConfig experimentConfig : configs) {
			experimentConfig.init(mks);
			logger.info("next: passiveWithZu." + experimentConfig.experimentName);
			int i = 1;
			for (Examples examples : runs) {
				logger.info("beginning run: " + (i++));
				conductExperiment(examples, experimentConfig);

			}
			Table expTable = new Table();
			expTable.addTableRowColumns(experimentConfig.getTableRows());
			expTable.write(resultFolder, "passiveWithZu_"+experimentConfig.experimentName);
			masterTable.addTable(expTable);
			masterTable.write(resultFolder, "passiveWithZu_master");

			JamonMonitorLogger.writeHTMLReport("/tmp/tiger.html");
			logger.info(experimentConfig);
		}
		

	}

	public static List<IteratedConfig> getConfigsNo() {

		List<IteratedConfig> l = new ArrayList<IteratedConfig>();
		IteratedConfig baseline = new IteratedConfig("baseline", iterations);

		IteratedConfig reducedExamples = new IteratedConfig("reducedExamples", iterations);
		reducedExamples.initialsplits = 2;
		reducedExamples.splits = 2;
		

		IteratedConfig adaptRuntime = new IteratedConfig("adaptRuntime3t", iterations);
		adaptRuntime.adaptMaxRuntime = true;
		adaptRuntime.maxExecutionTimeFactor = 3.0d;

		IteratedConfig useLemma = new IteratedConfig("noLemma", iterations);
		useLemma.useDataHasValue = false;

		l.add(baseline);
		l.add(reducedExamples);
		l.add(adaptRuntime);
		l.add(useLemma);

		return l;
	}
	public static List<IteratedConfig> getConfigsZU() {
		
		List<IteratedConfig> l = new ArrayList<IteratedConfig>();
		IteratedConfig baseline = new IteratedConfig("baseline", iterations);

		
		IteratedConfig increasedNegativeExamples = new IteratedConfig("increasedNegativeExamples", iterations);
		increasedNegativeExamples.negativeSplitAdd = 10;
		
		
		IteratedConfig increasedNegativeExamples3t = new IteratedConfig("increasedNegativeExamples3t", iterations);
		increasedNegativeExamples3t.negativeSplitAdd = 10;
		increasedNegativeExamples3t.adaptMaxRuntime = true;
		increasedNegativeExamples3t.maxExecutionTimeFactor = 3.0d;

		
		IteratedConfig useLemma = new IteratedConfig("noLemma", iterations);
		useLemma.useDataHasValue = false;
		
		l.add(baseline);
		l.add(increasedNegativeExamples);
		l.add(increasedNegativeExamples3t);
		l.add(useLemma);
		
		return l;
	}

	public static void conductExperiment(Examples allExamples, IteratedConfig config) {
		Examples tmp = new Examples();
		tmp.addPosTrain(allExamples.getPosTrain());
		tmp.addNegTrain(allExamples.getNegTrain());

		Examples learn = new ExMakerFixedSize(tmp).select(config.initialsplits, config.initialsplits+config.negativeSplitAdd);
		tmp = null;
		logger.debug("Total set \n" + allExamples);
		logger.debug("Initial training set \n" + learn);

		Set<String> posAsPos = new TreeSet<String>();
		Set<String> posAsNeg = new TreeSet<String>();
		Set<String> negAsNeg = new TreeSet<String>();
		Set<String> negAsPos = new TreeSet<String>();
		
		
		Set<String> retrieved = new TreeSet<String>();
		Set<String> newTestRetrieved = new TreeSet<String>();
		Set<String> newTrainRetrieved = new TreeSet<String>();

		String lastConcept = "";
		double precision = 0.0;
		double recall = 0.0;
		double fmeasure = 0.0;
		double accuracy = 0.0;

		for (int i = 0; config.stopCondition(i, precision, recall, fmeasure, lastConcept); i++) {
			Monitor iterationTime = JamonMonitorLogger.getTimeMonitor(TestIterativeLearning.class,
					"iterationTime").start();
			Monitor literationTime = config.start(logIterationTime, i);

			/* LEARNING */
			Monitor lLearningTime = config.start(logLearningTime, i);
			EvaluatedDescription ed = learn(learn, config, i);
			lLearningTime.stop();
			lastConcept = PrefixMap.toKBSyntaxString(ed.getDescription());
			concepts.add(ed.getDescription().toKBSyntaxString(null,null));
			logger.debug("USING CONCEPT: " + lastConcept);
			logger.debug(PrefixMap.toManchesterSyntaxString(ed.getDescription()));

			/* RETRIEVING */
			Monitor queryTime = JamonMonitorLogger.getTimeMonitor(TestIterativeLearning.class, "queryTime")
					.start();
			
			retrieved = getSentences(ed, config.resultLimit);
			config.add(nrOfRetrievedInstances, i, retrieved.size());
			logger.debug("retrieved: "+retrieved.size());
			
			queryTime.stop();
			// remove all that are not to be tested
			newTestRetrieved = Helper.intersection(allExamples.getTestExamples(), retrieved);
			newTrainRetrieved = Helper.intersection(allExamples.getTrainExamples(), retrieved);
			logger.debug("intersection with testset: "+newTestRetrieved.size());
			
			
			
			// logger.debug("Retrieved "+retrieved.size()+" sentences");

			/* MASHING */
			// Menge aller positiven geschn. mit den gefundenen
			
			
			
			posAsPos = Helper.intersection(newTestRetrieved, allExamples.getPosTest());
			negAsPos = Helper.intersection(newTestRetrieved, allExamples.getNegTest());
			posAsNeg = Helper.difference(allExamples.getPosTest(), newTestRetrieved);
			negAsNeg = Helper.difference(allExamples.getNegTest(), newTestRetrieved );
//			logger.debug("" + posAsPos.size()+"|"+negAsPos.size()+"|"+posAsNeg.size()+"|"+negAsPos.size());
			logger.debug("Number of retrieved positives: " + posAsPos.size());
			logger.debug("Number of total positives: " + allExamples.getPosTest().size());

			precision = precision(posAsPos.size(), newTestRetrieved.size());
			config.add(logPrecision, i, precision);
			recall = recall(posAsPos.size(), allExamples.getPosTest().size());
			config.add(logRecall, i, recall);
			fmeasure = fmeasure(precision, recall);
			config.add(logFMeasure, i, fmeasure);
			accuracy = accuracy(posAsPos.size(), negAsNeg.size(), posAsNeg.size(), negAsPos.size());
			config.add(logAccuracy, i, accuracy);

			// Menge aller positiven geschn. mit den gefundenen
			
			logger.debug("Number of retrieved negatives: " + negAsPos.size());
			logger.debug("Number of total negatives in test set: " + allExamples.getNegTest().size());
			logger.debug("Total: " + posAsPos.size() + " + " + negAsPos.size() + " = "
					+ newTestRetrieved.size());

			Examples newlyFound = new Examples();
			Set<String> discoveredPosInStore = Helper.intersection(newTrainRetrieved, allExamples
					.getPosTrain());
			Set<String> misclassifiedNegInStore = Helper.intersection(newTrainRetrieved, allExamples
					.getNegTrain());
			newlyFound.addPosTrain(discoveredPosInStore);
			newlyFound.addNegTrain(misclassifiedNegInStore);

			

			logger.debug("Discovered: " + discoveredPosInStore.size()
					+ " positive sentences in store (printing " + printSentences + "):");
			_getLabels(discoveredPosInStore, printSentences);
			logger.debug("Misclassified: " + misclassifiedNegInStore.size()
					+ " negative sentences in store (printing " + printSentences + "):");
			_getLabels(misclassifiedNegInStore, printSentences);
			logger.debug("Not found positives: " + posAsNeg.size()
					+ " positive sentences in store (printing " + printSentences + "):");
			_getLabels(posAsNeg, printSentences);

			newlyFound = new ExMakerFixedSize(newlyFound).select(config.splits, config.splits+config.negativeSplitAdd);

			learn.addPosTrain(newlyFound.getPosTrain());
			learn.addNegTrain(newlyFound.getNegTrain());
			logger.debug("Next training set \n" + learn);
			iterationTime.stop();
			literationTime.stop();
			logger.info("finished iteration " + (i + 1) + " needed on  avg: "
					+ dftime.format(iterationTime.getAvg()));
			logger.info("learning: " + dftime.format(lLearningTime.getLastValue()) + "  Acc: "
					+ ed.getAccuracy());
			logger.info("learning: " + PrefixMap.toManchesterSyntaxString(ed));
			logger.info("query: " + dftime.format(queryTime.getLastValue()));
			logger.info("F-Measure on Store = " + df.format(fmeasure));
			logger.info("******************");

		}

	}

	public static double accuracy(int posAsPos, int negAsNeg, int posAsNeg, int negAsPos) {
		int upper = posAsPos+negAsNeg;
		int lower = posAsPos+negAsNeg + posAsNeg + negAsPos;
		double accuracy = ((double)upper)/((double)lower);
		logger.debug("Accuracy: " + df.format(accuracy));
		return accuracy;
	}

	public static double fmeasure(double precision, double recall) {
		double fmeasure = (precision + recall == 0) ? 0.0d : (2 * precision * recall) / (precision + recall);
		logger.debug("F-Measure: " + df.format(fmeasure));
		return fmeasure;
	}

	public static double precision(int posAsPos, int retrieved) {
		double precision = (retrieved == 0) ? 0.0d : ((double) posAsPos) / ((double) retrieved);
		logger.debug("Precision: " + df.format(precision));
		return precision;
	}

	public static double recall(int posAsPos, int allPositives) {
		double recall = ((double) posAsPos) / ((double) allPositives);
		logger.debug("Recall: " + df.format(recall));
		return recall;

	}

	private static Set<KnowledgeSource> _getOWL(Examples ex) throws Exception {
		Set <KnowledgeSource> tmp = new HashSet<KnowledgeSource>();
		List<URL> urls = new ArrayList<URL>();
		urls.add(new File(backgroundXML).toURI().toURL());
		urls.addAll(ExampleDataCollector.convert(sentenceXMLFolder, ex.getPosTrain()));
		urls.addAll(ExampleDataCollector.convert(sentenceXMLFolder, ex.getNegTrain()));

		for (URL u : urls) {
			OWLFile ks = new OWLFile(u);
			tmp.add(ks);
		}
		return tmp;
	}

//	@SuppressWarnings("unused")
//	private static Set<KnowledgeSource> _getSPARQL(Examples ex) throws Exception {
//		Set<KnowledgeSource> tmp = new HashSet<KnowledgeSource>();
//
//		Set<String> examples = new TreeSet<String>();
//		examples.addAll(ex.getPosTrain());
//		examples.addAll(ex.getNegTrain());
//		SparqlKnowledgeSource ks = ComponentFactory.getSparqlKnowledgeSource(new URL(sparqlEndpointURL),
//				examples);
//		ks.getConfigurator().setUrl(new URL(sparqlEndpointURL));
//		ks.getConfigurator().setDefaultGraphURIs(new HashSet<String>(Arrays.asList(new String[] { graph })));
//		ks.getConfigurator().setInstances(examples);
//		ks.getConfigurator().setDissolveBlankNodes(false);
//		ks.getConfigurator().setRecursionDepth(2);
//		ks.getConfigurator().setDissolveBlankNodes(false);
//		ks.getConfigurator().setCloseAfterRecursion(true);
//		ks.getConfigurator().setGetAllSuperClasses(true);
//		ks.getConfigurator().setGetPropertyInformation(false);
//		ks.getConfigurator().setUseLits(true);
//		// ks.getConfigurator().
//		OWLFile ks2 = ComponentFactory.getOWLFile(new File(propertiesXML).toURI().toURL());
//		tmp.add(ks);
//		tmp.add(ks2);
//
//		return tmp;
//	}

	// test if virtuoso is correct
	// public static void validate(Description d, Examples newlyFound){
	// try {
	// ExMakerFixedSize fs = new ExMakerFixedSize(newlyFound);
	// Examples tmp = fs.select(100, 100);
	// FastInstanceChecker fc = _getFastInstanceChecker(tmp);
	// @SuppressWarnings("unused")
	// SortedSet<Individual> inds = fc.getIndividuals(d);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	public static FastInstanceChecker _getFastInstanceChecker(Examples ex) throws Exception {
		Set<KnowledgeSource> tmp = _getOWL(ex);
		// Set<KnowledgeSource> tmp = _getSPARQL(ex);

		FastInstanceChecker rc = new FastInstanceChecker(tmp);
		for (KnowledgeSource ks : tmp) {
			ks.init();
		}
		rc.init();
		return rc;
	}

	public static EvaluatedDescription learn(Examples ex, IteratedConfig config, int iteration) {
		Monitor initTimeKBandReasoner = JamonMonitorLogger.getTimeMonitor(TestIterativeLearning.class,
				"initTimeKBandReasoner").start();

		EvaluatedDescription result = null;

		try {
			FastInstanceChecker rc = _getFastInstanceChecker(ex);
			PosNegLPStandard lp = new PosNegLPStandard(rc, Helper.getIndividualSet(ex.getPosTrain()), Helper.getIndividualSet(ex.getNegTrain()));
			AbstractCELA la = _getROLLearner(lp, rc, config, ex, iteration);
			lp.init();
			la.init();
			initTimeKBandReasoner.stop();
			Monitor learningTime = JamonMonitorLogger.getTimeMonitor(TestIterativeLearning.class,
					"learningTime").start();
			la.start();
			learningTime.stop();
			result = la.getCurrentlyBestEvaluatedDescription();
			
//			System.out.println(result = la.getCurrentlyBestEvaluatedDescription());
//			for (EvaluatedDescription edd : la.getCurrentlyBestMostGeneralEvaluatedDescriptions()) {
//				System.out.println(edd);
//			}
			
			
			
			
			ComponentManager.getInstance().freeAllComponents();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return result;
	}

	public static SortedSet<String> getSentences(EvaluatedDescription ed, int resultLimit) {// Examples
																							// justforFindingTheBug
		Monitor m = JamonMonitorLogger.getTimeMonitor(TestIterativeLearning.class, "getSentences").start();
		SortedSet<String> result = new TreeSet<String>();
		SparqlQueryDescriptionConvertVisitor visit = new SparqlQueryDescriptionConvertVisitor();
		visit.setDistinct(true);
		visit.setLabels(false);
		visit.setLimit(resultLimit);
		String sparqlQueryGood = "";
		// String sparqlQueryBad = "";
		try {
			sparqlQueryGood = visit.getSparqlQuery(ed.getDescription().toKBSyntaxString());
			// sparqlQueryBad = visit.getSparqlQuery(ed.getDescription());
			// if(!sparqlQueryGood.equals(sparqlQueryBad)){
			// String file = "errorDescription/"+System.currentTimeMillis();
			// justforFindingTheBug.writeExamples(file);
			// Files.appendFile(new File(file),
			// "\n\n/**\nGood:\n"+sparqlQueryGood+"\nBad:\n"+sparqlQueryBad+"**/");
			// }

		} catch (Exception e1) {
			e1.printStackTrace();
		}

		sparqlQueryGood = " \n define input:inference \"" + rulegraph + "\" \n" + "" + sparqlQueryGood;
		logger.debug(sparqlQueryGood);
		try{
			result.addAll(sparqlTasks.queryAsSet(sparqlQueryGood, "subject"));
		}catch (Exception e) {
			e.printStackTrace();
			logger.error("Virtuoso failed");
//			SortedSet<String> s = new TreeSet<String> (Arrays.asList(new String[]{"http://nlp2rdf.org/ontology/nextToken", "http://nlp2rdf.org/ontology/previousToken"}));
//			visit.setTransitiveProperties(s);
//			try {
//				sparqlQueryGood = " \n define input:inference \"" + rulegraph + "\" \n" + "" + visit.getSparqlQuery(ed.getDescription().toKBSyntaxString());
//				logger.warn(sparqlQueryGood);
//				result.addAll(sparqlTasks.queryAsSet(sparqlQueryGood, "subject"));
//			
//			} catch (ParseException e1) {
//				e1.printStackTrace();
//			}
		}
		
		m.stop();
		logger.debug("query avg: " + ((double) m.getAvg() / (double) 1000) + " seconds (last: "
				+ ((double) m.getLastValue() / (double) 1000) + ")");
		if (result.isEmpty()) {

			logger.error("sparql query returned no results ");
			logger.error(sparqlQueryGood);
//			System.exit(0);
		}
		return result;
	}

	private static void _getLabels(Set<String> sentenceURIs, int limit) {
		Monitor m = JamonMonitorLogger.getTimeMonitor(TestIterativeLearning.class, "_getLabels").start();
		int i = 0;
		for (String sentenceURI : sentenceURIs) {
			if (i >= limit) {
				break;
			}
			i++;
			_getLabel(sentenceURI);
		}
		m.stop();
	}

	private static void _getLabel(String sentenceURI) {
		String query = "SELECT * FROM <" + graph + "> " + "{ <" + sentenceURI + "> rdfs:label ?label . }";
		SortedSet<String> s = sparqlTasks.queryAsSet(query, "label");
		if (s.isEmpty()) {
			logger.warn("no label for " + sentenceURI);
		} else {
			logger.debug(sentenceURI.replace(prefix, "") + " " + s.first());
		}
	}

	private static AbstractCELA _getROLLearner(AbstractLearningProblem lp, AbstractReasonerComponent rc,
			IteratedConfig config, Examples ex, int iteration) throws Exception {

		int maxExecutionTime = config.maxExecutionTime;
		int valueFrequencyThreshold = ex.getPosTrain().size();
		int noise = config.noise + (config.noiseIterationFactor * iteration);
		if (config.adaptMaxRuntime) {
			maxExecutionTime = (int) Math.floor(config.maxExecutionTimeFactor * (double) ex.sizeOfTrainingSets());
			// valueFrequencyThreshold = (int)
			// Math.floor(0.8d*((double)ex.getPosTrain().size()));
		}

		OCEL la = ComponentManager.getInstance().learningAlgorithm(OCEL.class, lp, rc);
		RhoDRDown op = (RhoDRDown) la.getOperator();
//		CELOE la = ComponentFactory.getCELOE(lp, rc);
		op.setUseExistsConstructor(true);

		op.setUseAllConstructor(false);
		op.setUseCardinalityRestrictions(false);
		op.setUseNegation(false);
		op.setUseHasValueConstructor(false);
		op.setUseDataHasValueConstructor(config.useDataHasValue);
		op.setFrequencyThreshold(valueFrequencyThreshold);
//		la.getConfigurator().setInstanceBasedDisjoints(true);
		
//		la.getConfigurator().setIgnoredConcepts(
//				new HashSet<String>(Arrays.asList(new String[] {
//						"http://nlp2rdf.org/ontology/sentencefinalpunctuation_tag",
//						"http://nlp2rdf.org/ontology/comma_tag",
//						"http://nachhalt.sfb632.uni-potsdam.de/owl/stts.owl#SentenceFinalPunctuation",
//						"http://nlp2rdf.org/ontology/generalsentenceinternalpunctuation_tag" })));

		la.setNoisePercentage(noise);
		la.setTerminateOnNoiseReached(true);
		la.setMaxExecutionTimeInSeconds(maxExecutionTime);

		if (config.useStartClass) {
			la.setStartClass(new NamedClass(prefix + "Sentence"));
		}

		la.setWriteSearchTree(config.searchTree);
		la.setSearchTreeFile(new File("log/searchTreeTiger.txt"));
		la.setReplaceSearchTree(true);
		return la;
	}
	
//	if(config.ignorePOSFeatures){
//	la.getConfigurator().setIgnoredConcepts(VocabFilter.posClasses);
//	la.getConfigurator().setIgnoredRoles(VocabFilter.posProperties);
//}

//SortedSet<String> inv = new TreeSet<String>();
//for(String s : VocabFilter.syntaxProperies){
//	if(s.toLowerCase().endsWith("inv")){
//		inv.add(s);
//	}
//}
//inv.add("http://nlp2rdf.org/ontology/hasToken");
//inv.add("http://nlp2rdf.org/ontology/firstToken");
//System.out.println(inv);
//if (true) {
//	System.exit(0);
//}
//SortedSet<String> all = new TreeSet<String>(Helper.difference(VocabFilter.syntaxProperies, inv));
//all.addAll(VocabFilter.posProperties);
//la.getConfigurator().setAllowedRoles(all);
//if(config.ignoreSyntaxFeatures){
//	la.getConfigurator().setIgnoredConcepts(VocabFilter.syntaxClasses);
//	la.getConfigurator().setIgnoredRoles(VocabFilter.syntaxProperies);
//}else{
//	la.getConfigurator().setAllowedRoles(all);
//}
//la.getConfigurator().setInstanceBasedDisjoints(false);



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
