/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
 */
package org.dllearner.algorithms.qtl.experiments;

import com.google.common.base.*;
import com.google.common.collect.*;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.commons.collections15.ListUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SynchronizedDescriptiveStatistics;
import org.apache.commons.math3.util.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.ExprAggregator;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.aggregate.AggCountVarDistinct;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.vocabulary.RDF;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithms.qtl.QTL2Disjunctive;
import org.dllearner.algorithms.qtl.QTL2DisjunctiveMultiThreaded;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.EvaluatedRDFResourceTree;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.experiments.datasets.EvaluationDataset;
import org.dllearner.algorithms.qtl.experiments.datasets.QALD4BiomedicalChallengeEvaluationDataset;
import org.dllearner.algorithms.qtl.experiments.datasets.QALD6DBpediaEvaluationDataset;
import org.dllearner.algorithms.qtl.heuristics.QueryTreeHeuristic;
import org.dllearner.algorithms.qtl.heuristics.QueryTreeHeuristicSimple;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBaseInv;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGeneratorSimple;
import org.dllearner.algorithms.qtl.util.Entailment;
import org.dllearner.algorithms.qtl.util.filters.PredicateExistenceFilter;
import org.dllearner.algorithms.qtl.util.statistics.TimeMonitors;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.StringRenderer;
import org.dllearner.core.StringRenderer.Rendering;
import org.dllearner.kb.sparql.CBDStructureTree;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.TreeBasedConciseBoundedDescriptionGenerator;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.learningproblems.Heuristics.HeuristicType;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.utilities.QueryUtils;
import org.dllearner.utilities.owl.DLSyntaxObjectRendererExt;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Lorenz Buehmann
 *
 */
@SuppressWarnings("unchecked")
public class PRConvergenceExperiment {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PRConvergenceExperiment.class.getName());

	private static final ParameterizedSparqlString superClassesQueryTemplate2 = new ParameterizedSparqlString(
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX owl: <http://www.w3.org/2002/07/owl#> "
			+ "SELECT ?sup WHERE {"
			+ "?sub ((rdfs:subClassOf|owl:equivalentClass)|^owl:equivalentClass)+ ?sup .}");

	private static final ParameterizedSparqlString superClassesQueryTemplate = new ParameterizedSparqlString(
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX owl: <http://www.w3.org/2002/07/owl#> "
			+ "SELECT ?sup WHERE {"
			+ "?sub (rdfs:subClassOf|owl:equivalentClass)+ ?sup .}");

	private static final DecimalFormat dfPercent = new DecimalFormat("0.00%");
	private CBDStructureTree cbdStructureTree;


	enum Baseline {
		RANDOM, MOST_POPULAR_TYPE_IN_KB, MOST_FREQUENT_TYPE_IN_EXAMPLES, MOST_INFORMATIVE_EDGE_IN_EXAMPLES, LGG, MOST_FREQUENT_EDGE_IN_EXAMPLES
	}

	private QueryExecutionFactory qef;

	private org.dllearner.algorithms.qtl.impl.QueryTreeFactory queryTreeFactory;
	private TreeBasedConciseBoundedDescriptionGenerator cbdGen;

	private RandomDataGenerator rnd = new RandomDataGenerator();

	private EvaluationDataset dataset;

	private Map<String, List<String>> cache = new HashMap<>();

	private int kbSize;

	private boolean splitComplexQueries = true;

	private PredicateExistenceFilter filter;

	// the directory where all files, results etc. are maintained
	private File benchmarkDirectory;

	// whether to write eval results to a database
	private boolean write2DB;

	// DB related objects
	private Connection conn;
	private PreparedStatement psInsertOverallEval;
	private PreparedStatement psInsertDetailEval;

	// max. time for each QTL run
	private int maxExecutionTimeInSeconds = 600;

	private int minNrOfPositiveExamples = 9;

	private int maxTreeDepth = 2;

	private NoiseGenerator.NoiseMethod noiseMethod = NoiseGenerator.NoiseMethod.RANDOM;
	private NoiseGenerator noiseGenerator;

	// whether to override existing results
	private boolean override = false;

	// parameters
	private int[] nrOfExamplesIntervals = {
//					5,
//					10,
//					15,
					20,
//					25,
//					30
					};

	private double[] noiseIntervals = {
					0.0,
//					0.1,
//					0.2,
//					0.3,
//					0.4,
//					0.6
					};

	private QueryTreeHeuristic[] heuristics = {
					new QueryTreeHeuristicSimple(),
//					new QueryTreeHeuristicComplex(qef)
			};

	private HeuristicType[] measures = {
					HeuristicType.PRED_ACC,
//					HeuristicType.FMEASURE,
//					HeuristicType.MATTHEWS_CORRELATION
					};

	private final Map<String, ExampleCandidates> query2Examples = new HashMap<>();

	private File cacheDirectory;

	private boolean useEmailNotification = false;

	private int nrOfThreads;

	OWLObjectRenderer owlRenderer = new DLSyntaxObjectRendererExt();

	DescriptiveStatistics treeSizeStats = new DescriptiveStatistics();

	private long timeStamp;

	Set<String> queriesToProcessTokens = Sets.newHashSet(
//			"Natalie_Portman"
//			"Pakistan"
//			"Lou_Reed"
	);

	Set<String> queriesToOmitTokens = Sets.newHashSet(
//			"Lou_Reed"
//			"Pakistan"
	);

	String databaseName;

	public PRConvergenceExperiment(EvaluationDataset dataset, File benchmarkDirectory,
								   boolean write2DB, String databaseName, boolean override, int maxQTLRuntime,
								   boolean useEmailNotification, int nrOfThreads) {
		this.dataset = dataset;
		this.benchmarkDirectory = benchmarkDirectory;
		this.write2DB = write2DB;
		this.databaseName = databaseName;
		this.override = override;
		this.maxExecutionTimeInSeconds = maxQTLRuntime;
		this.useEmailNotification = useEmailNotification;
		this.nrOfThreads = nrOfThreads;

		queryTreeFactory = new QueryTreeFactoryBaseInv();
		queryTreeFactory.setMaxDepth(maxTreeDepth);
		
		// add some filters to avoid resources with namespaces like http://dbpedia.org/property/
		List<Predicate<Statement>> var = dataset.getQueryTreeFilters();
		queryTreeFactory.addDropFilters((Predicate<Statement>[]) var.toArray(new Predicate[var.size()]));
		
		qef = dataset.getKS().getQueryExecutionFactory();
		
		cbdGen = new TreeBasedConciseBoundedDescriptionGenerator(qef);

		rnd.reSeed(123);
		noiseGenerator = new NoiseGenerator(qef, rnd);
		
		kbSize = getKBSize();

		timeStamp = System.currentTimeMillis();
		
		cacheDirectory = new File(benchmarkDirectory, "cache");

		filter = dataset.getPredicateFilter();

		if(databaseName == null) {
			this.databaseName = "QTL_" + dataset.getName() + "_" + timeStamp;
		}

		if(write2DB) {
			setupDatabase();
		}
	}

	public void setWorkaroundEnabled(boolean enabled, SparqlEndpoint endpoint) {
		cbdGen.setWorkaround(enabled);
		cbdGen.setEndpoint(endpoint);
	}


	private void setupDatabase() {
		try {
			Properties config = new Properties();
			config.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/dllearner/algorithms/qtl/qtl-eval-config.properties"));

			String url = config.getProperty("url");
			String username = config.getProperty("username");
			String password = config.getProperty("password");
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(url, username, password);

			java.sql.Statement stmt = conn.createStatement();

			// create database
			logger.info("Creating database " + databaseName + "'");
			String sql = "CREATE DATABASE IF NOT EXISTS " + databaseName;
			stmt.executeUpdate(sql);
			logger.info("Database created successfully.");

			// switch to database
			conn.setCatalog(databaseName);
			stmt = conn.createStatement();

//			// empty tables if override
//			if(override) {
//				sql = "DROP TABLE IF EXISTS eval_overall,eval_detailed;";
//				sql = "ALTER TABLE IF EXISTS eval_overall DROP PRIMARY KEY;";
//				stmt.execute(sql);
//			}

			// create tables if not exist
			sql = "CREATE TABLE IF NOT EXISTS eval_overall (" +
					   "heuristic VARCHAR(100), " + 
					   "heuristic_measure VARCHAR(100), " +
					   "nrOfExamples TINYINT, " +
	                   "noise DOUBLE, " + 
	                   "avg_fscore_best_returned DOUBLE, " + 
	                   "avg_precision_best_returned DOUBLE, " + 
	                   "avg_recall_best_returned DOUBLE, " + 
	                   "avg_predacc_best_returned DOUBLE, " + 
	                   "avg_mathcorr_best_returned DOUBLE, " + 
	                   "avg_position_best DOUBLE, " +
	                   "avg_fscore_best DOUBLE, " + 
	                   "avg_precision_best DOUBLE, " + 
	                   "avg_recall_best DOUBLE, " + 
	                   "avg_predacc_best DOUBLE, " + 
	                   "avg_mathcorr_best DOUBLE, " + 
	                   "avg_fscore_baseline DOUBLE, " + 
	                   "avg_precision_baseline DOUBLE, " + 
	                   "avg_recall_baseline DOUBLE, " + 
	                   "avg_predacc_baseline DOUBLE, " + 
	                   "avg_mathcorr_baseline DOUBLE, " +
	                   "avg_runtime_best_returned DOUBLE, " +
	                   "PRIMARY KEY(heuristic, heuristic_measure, nrOfExamples, noise))";
			stmt.execute(sql);
			
			sql = "CREATE TABLE IF NOT EXISTS eval_detailed (" +
					   "target_query VARCHAR(700)," +
					   "nrOfExamples TINYINT, " +
	                   "noise DOUBLE, " + 
	                   "heuristic VARCHAR(50), " +
	                   "heuristic_measure VARCHAR(50), " +
	                   "query_top LONGTEXT, " +
	                   "fscore_top DOUBLE, " + 
	                   "precision_top DOUBLE, " + 
	                   "recall_top DOUBLE, " + 
	                   "best_query LONGTEXT," +
	                   "best_rank SMALLINT, " +
	                   "best_fscore DOUBLE, " + 
	                   "best_precision DOUBLE, " + 
	                   "best_recall DOUBLE, " + 
	                   "baseline_query TEXT," +
	                   "baseline_fscore DOUBLE, " + 
	                   "baseline_precision DOUBLE, " + 
	                   "baseline_recall DOUBLE, " + 
	                   "runtime_top INT, " +
	                   "PRIMARY KEY(target_query, nrOfExamples, noise, heuristic, heuristic_measure)) ENGINE=MyISAM";
			stmt.execute(sql);
			
			sql = "INSERT INTO eval_overall ("
					+ "heuristic, heuristic_measure, nrOfExamples, noise, "
					+ "avg_fscore_best_returned, avg_precision_best_returned, avg_recall_best_returned,"
					+ "avg_predacc_best_returned, avg_mathcorr_best_returned, "
					+ "avg_position_best, avg_fscore_best, avg_precision_best, avg_recall_best, avg_predacc_best, avg_mathcorr_best,"
					+ "avg_fscore_baseline, avg_precision_baseline, avg_recall_baseline, avg_predacc_baseline, avg_mathcorr_baseline,"
					+ "avg_runtime_best_returned"
					+ ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			if(override) {
				sql += " ON DUPLICATE KEY UPDATE ";
				sql += "avg_fscore_best_returned = VALUES(avg_fscore_best_returned),";
				sql += "avg_precision_best_returned = VALUES(avg_precision_best_returned),";
				sql += "avg_recall_best_returned = VALUES(avg_recall_best_returned),";
				sql += "avg_predacc_best_returned = VALUES(avg_predacc_best_returned),";
				sql += "avg_mathcorr_best_returned = VALUES(avg_mathcorr_best_returned),";
				sql += "avg_position_best = VALUES(avg_position_best),";
				sql += "avg_fscore_best = VALUES(avg_fscore_best),";
				sql += "avg_precision_best = VALUES(avg_precision_best),";
				sql += "avg_recall_best = VALUES(avg_recall_best),";
				sql += "avg_predacc_best = VALUES(avg_predacc_best),";
				sql += "avg_mathcorr_best = VALUES(avg_mathcorr_best),";
				sql += "avg_fscore_baseline = VALUES(avg_fscore_baseline),";
				sql += "avg_precision_baseline = VALUES(avg_precision_baseline),";
				sql += "avg_recall_baseline = VALUES(avg_recall_baseline),";
				sql += "avg_predacc_baseline = VALUES(avg_predacc_baseline),";
				sql += "avg_mathcorr_baseline = VALUES(avg_mathcorr_baseline),";
				sql += "avg_runtime_best_returned = VALUES(avg_runtime_best_returned)";
			}
			psInsertOverallEval = conn.prepareStatement(sql);
			
			sql = "INSERT INTO eval_detailed ("
					+ "target_query, nrOfExamples, noise, heuristic, heuristic_measure, "
					+ "query_top, fscore_top, precision_top, recall_top,"
					+ "best_query, best_rank, best_fscore, best_precision, best_recall, "
					+ "baseline_query,baseline_fscore, baseline_precision, baseline_recall,"
					+ "runtime_top"
					+ ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			if(override) {
				sql += " ON DUPLICATE KEY UPDATE ";
				sql += "query_top = VALUES(query_top),";
				sql += "fscore_top = VALUES(fscore_top),";
				sql += "precision_top = VALUES(precision_top),";
				sql += "recall_top = VALUES(recall_top),";
				sql += "best_query = VALUES(best_query),";
				sql += "best_rank = VALUES(best_rank),";
				sql += "best_fscore = VALUES(best_fscore),";
				sql += "best_precision = VALUES(best_precision),";
				sql += "best_recall = VALUES(best_recall),";
				sql += "baseline_query = VALUES(baseline_query),";
				sql += "baseline_fscore = VALUES(baseline_fscore),";
				sql += "baseline_precision = VALUES(baseline_precision),";
				sql += "baseline_recall = VALUES(baseline_recall),";
				sql += "runtime_top = VALUES(runtime_top)";
			}
			psInsertDetailEval = conn.prepareStatement(sql);
		} catch (Exception e) {
			throw new RuntimeException("Database setup failed", e);
		}
	}
	
	private int getKBSize() {
		String query = dataset.usesStrictOWLTypes() ?
				"SELECT (COUNT(DISTINCT ?s) AS ?cnt) WHERE {?s a ?type . ?type a <http://www.w3.org/2002/07/owl#Class> }" :
				"SELECT (COUNT(DISTINCT ?s) AS ?cnt) WHERE {?s a ?type }";
		
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		int size = rs.next().get("cnt").asLiteral().getInt();
		
		qe.close();
		
		return size;
	}

	public void setQueriesToOmitTokens(Collection<String> queriesToOmitTokens) {
		this.queriesToOmitTokens.addAll(queriesToOmitTokens);
	}

	public void setQueriesToOmitTokens(Set<String> queriesToOmitTokens) {
		this.queriesToOmitTokens = queriesToOmitTokens;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public void run(int maxNrOfProcessedQueries, int maxTreeDepth, int[] exampleInterval, double[] noiseInterval, HeuristicType[] measures) throws Exception{
		this.maxTreeDepth = maxTreeDepth;
		queryTreeFactory.setMaxDepth(maxTreeDepth);

		if(exampleInterval != null) {
			nrOfExamplesIntervals = exampleInterval;
		}
		if(noiseInterval != null) {
			this.noiseIntervals = noiseInterval;
		}
		if(measures != null) {
			this.measures = measures;
		}

		boolean noiseEnabled = noiseIntervals.length > 1 || noiseInterval[0] > 0;
		boolean posOnly = noiseEnabled ? false : true;

		logger.info("Started QTL evaluation...");
		long t1 = System.currentTimeMillis();
		
		List<String> queries = dataset.getSparqlQueries().values().stream().map(Query::toString).collect(Collectors.toList());
		logger.info("#loaded queries: " + queries.size());

		// filter for debugging purposes
		queries = queries.stream().filter(q -> queriesToProcessTokens.stream().noneMatch(t -> !q.contains(t))).collect(Collectors.toList());
		queries = queries.stream().filter(q -> queriesToOmitTokens.stream().noneMatch(q::contains)).collect(Collectors.toList());



		if(maxNrOfProcessedQueries == -1) {
			maxNrOfProcessedQueries = queries.size();
		}

//		queries = filter(queries, (int) Math.ceil((double) maxNrOfProcessedQueries / maxTreeDepth));
//		queries = queries.subList(0, Math.min(queries.size(), maxNrOfProcessedQueries));
		logger.info("#queries to process: " + queries.size());

		// generate examples for each query
		logger.info("precomputing pos. and neg. examples...");
		for (String query : queries) {//if(!(query.contains("Borough_(New_York_City)")))continue;
			query2Examples.put(query, generateExamples(query, posOnly, noiseEnabled));
		}
		logger.info("precomputing pos. and neg. examples finished.");

		// check for queries that do not return any result (should not happen, but we never know)
		Set<String> emptyQueries = query2Examples.entrySet().stream()
				.filter(e -> e.getValue().correctPosExampleCandidates.isEmpty())
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
		logger.info("got {} empty queries.", emptyQueries.size());
		queries.removeAll(emptyQueries);

		// min. pos examples
		int min = 3;
		Set<String> lowNrOfExamplesQueries = query2Examples.entrySet().stream()
				.filter(e -> e.getValue().correctPosExampleCandidates.size() < min)
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
		logger.info("got {} queries with < {} pos. examples.", emptyQueries.size(), min);
		queries.removeAll(lowNrOfExamplesQueries);
		queries = queries.subList(0, Math.min(80, queries.size()));

		final int totalNrOfQTLRuns = heuristics.length * this.measures.length * nrOfExamplesIntervals.length * noiseIntervals.length * queries.size();
		logger.info("#QTL runs: " + totalNrOfQTLRuns);

		final AtomicInteger currentNrOfFinishedRuns = new AtomicInteger(0);

		// loop over heuristics
		for(final QueryTreeHeuristic heuristic : heuristics) {
			final String heuristicName = heuristic.getClass().getAnnotation(ComponentAnn.class).shortName();
			
			// loop over heuristics measures
			for (HeuristicType measure : this.measures) {
				final String measureName = measure.toString();
				heuristic.setHeuristicType(measure);
			
				double[][] data = new double[nrOfExamplesIntervals.length][noiseIntervals.length];
				
				// loop over number of positive examples
				for (int i = 0; i < nrOfExamplesIntervals.length; i++) {
					 final int nrOfExamples = nrOfExamplesIntervals[i];
					 
					// loop over noise value
					for (int j = 0; j < noiseIntervals.length; j++) {
						final double noise = noiseIntervals[j];
						
						// check if not already processed
						File logFile = new File(benchmarkDirectory, "qtl2-" + nrOfExamples + "-" + noise + "-" + heuristicName + "-" + measureName + ".log");
						File statsFile = new File(benchmarkDirectory, "qtl2-" + nrOfExamples + "-" + noise + "-" + heuristicName + "-" + measureName + ".stats");
						
						if(!override && logFile.exists() && statsFile.exists()) {
							logger.info("Eval config already processed. For re-running please remove corresponding output files.");
							continue;
						}
						
						FileAppender appender = null;
						try {
							appender = new FileAppender(new SimpleLayout(), logFile.getPath(), false);
							Logger.getRootLogger().addAppender(appender);
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						
						logger.info("#examples: " + nrOfExamples + " noise: " + noise);
						
						final DescriptiveStatistics nrOfReturnedSolutionsStats = new SynchronizedDescriptiveStatistics();
						
						final DescriptiveStatistics baselinePrecisionStats = new SynchronizedDescriptiveStatistics();
						final DescriptiveStatistics baselineRecallStats = new SynchronizedDescriptiveStatistics();
						final DescriptiveStatistics baselineFMeasureStats = new SynchronizedDescriptiveStatistics();
						final DescriptiveStatistics baselinePredAccStats = new SynchronizedDescriptiveStatistics();
						final DescriptiveStatistics baselineMathCorrStats = new SynchronizedDescriptiveStatistics();
						
						final DescriptiveStatistics bestReturnedSolutionPrecisionStats = new SynchronizedDescriptiveStatistics();
						final DescriptiveStatistics bestReturnedSolutionRecallStats = new SynchronizedDescriptiveStatistics();
						final DescriptiveStatistics bestReturnedSolutionFMeasureStats = new SynchronizedDescriptiveStatistics();
						final DescriptiveStatistics bestReturnedSolutionPredAccStats = new SynchronizedDescriptiveStatistics();
						final DescriptiveStatistics bestReturnedSolutionMathCorrStats = new SynchronizedDescriptiveStatistics();
						
						final DescriptiveStatistics bestReturnedSolutionRuntimeStats = new SynchronizedDescriptiveStatistics();

						final DescriptiveStatistics bestSolutionPrecisionStats = new SynchronizedDescriptiveStatistics();
						final DescriptiveStatistics bestSolutionRecallStats = new SynchronizedDescriptiveStatistics();
						final DescriptiveStatistics bestSolutionFMeasureStats = new SynchronizedDescriptiveStatistics();
						final DescriptiveStatistics bestSolutionPredAccStats = new SynchronizedDescriptiveStatistics();
						final DescriptiveStatistics bestSolutionMathCorrStats = new SynchronizedDescriptiveStatistics();
						
						final DescriptiveStatistics bestSolutionPositionStats = new SynchronizedDescriptiveStatistics();
						

						MonitorFactory.getTimeMonitor(TimeMonitors.CBD_RETRIEVAL.name()).reset();
						MonitorFactory.getTimeMonitor(TimeMonitors.TREE_GENERATION.name()).reset();
						
						ExecutorService tp = Executors.newFixedThreadPool(nrOfThreads);
						
						// indicates if the execution for some of the queries failed
						final AtomicBoolean failed = new AtomicBoolean(false);

						Set<String> queriesToProcess = new TreeSet<>(queries);
						queriesToProcess.retainAll(
								query2Examples.entrySet().stream()
								.filter(e -> e.getValue().correctPosExampleCandidates.size() >= nrOfExamples)
								.map(Map.Entry::getKey)
								.collect(Collectors.toSet()));

						// loop over SPARQL queries
						for (final String sparqlQuery : queriesToProcess) {
							CBDStructureTree cbdStructure = cbdStructureTree != null ? cbdStructureTree : QueryUtils.getOptimalCBDStructure(QueryFactory.create(sparqlQuery));

								tp.submit(() -> {
									logger.info("CBD tree:" + cbdStructure.toStringVerbose());

									// update max tree depth
									this.maxTreeDepth = QueryTreeUtils.getDepth(cbdStructure);
									logger.info("##############################################################");
									logger.info("Processing query\n" + sparqlQuery);

									// we repeat it n times with different permutations of examples
									int nrOfPermutations = 1;

									if(nrOfExamples >= query2Examples.get(sparqlQuery).correctPosExampleCandidates.size()){
										nrOfPermutations = 1;
									}
									for(int perm = 1; perm <= nrOfPermutations; perm++) {
										logger.info("Run {}/{}", perm, nrOfPermutations);
										try {
											ExamplesWrapper examples = getExamples(sparqlQuery, nrOfExamples, nrOfExamples, noise, cbdStructure);
											logger.info("pos. examples:\n" + Joiner.on("\n").join(examples.correctPosExamples));
											logger.info("neg. examples:\n" + Joiner.on("\n").join(examples.correctNegExamples));

											// write examples to disk
											File dir = new File(benchmarkDirectory, "data/" + hash(sparqlQuery));
											dir.mkdirs();
											Files.write(Joiner.on("\n").join(examples.correctPosExamples),
													new File(dir, "examples" + perm + "_" + nrOfExamples + "_" + noise + ".tp"), Charsets.UTF_8);
											Files.write(Joiner.on("\n").join(examples.correctNegExamples),
													new File(dir, "examples" + perm + "_" + nrOfExamples + "_" + noise + ".tn"), Charsets.UTF_8);
											Files.write(Joiner.on("\n").join(examples.falsePosExamples),
													new File(dir, "examples" + perm + "_" + nrOfExamples + "_" + noise + ".fp"), Charsets.UTF_8);

											// compute baseline
											RDFResourceTree baselineSolution = applyBaseLine(examples, Baseline.MOST_INFORMATIVE_EDGE_IN_EXAMPLES);
											logger.info("Evaluating baseline...");
											Score baselineScore = computeScore(sparqlQuery, baselineSolution, noise);
											logger.info("Baseline score:\n" + baselineScore);
											String baseLineQuery = QueryTreeUtils.toSPARQLQueryString(
													baselineSolution, dataset.getBaseIRI(), dataset.getPrefixMapping());
											baselinePrecisionStats.addValue(baselineScore.precision);
											baselineRecallStats.addValue(baselineScore.recall);
											baselineFMeasureStats.addValue(baselineScore.fmeasure);
											baselinePredAccStats.addValue(baselineScore.predAcc);
											baselineMathCorrStats.addValue(baselineScore.mathCorr);

											// run QTL
											PosNegLPStandard lp = new PosNegLPStandard();
											lp.setPositiveExamples(examples.posExamplesMapping.keySet());
											lp.setNegativeExamples(examples.negExamplesMapping.keySet());
//											QTL2Disjunctive la = new QTL2Disjunctive(lp, qef);
											QTL2DisjunctiveMultiThreaded la = new QTL2DisjunctiveMultiThreaded(lp, qef);
											la.setRenderer(new DLSyntaxObjectRendererExt());
											la.setReasoner(dataset.getReasoner());
											la.setEntailment(Entailment.SIMPLE);
											la.setTreeFactory(queryTreeFactory);
											la.setPositiveExampleTrees(examples.posExamplesMapping);
											la.setNegativeExampleTrees(examples.negExamplesMapping);
											la.setNoise(noise);
											la.setHeuristic(heuristic);
											la.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
											la.setMaxTreeComputationTimeInSeconds(maxExecutionTimeInSeconds);
											la.init();
											la.start();
											List<EvaluatedRDFResourceTree> solutions = new ArrayList<>(la.getSolutions());

											//										List<EvaluatedRDFResourceTree> solutions = generateSolutions(examples, noise, heuristic);
											nrOfReturnedSolutionsStats.addValue(solutions.size());

											// the best returned solution by QTL
											EvaluatedRDFResourceTree bestSolution = solutions.get(0);
											logger.info("Got " + solutions.size() + " query trees.");
//											logger.info("Best computed solution:\n" + render(bestSolution.asEvaluatedDescription()));
											logger.info("QTL Score:\n" + bestSolution.getTreeScore());
											long runtimeBestSolution = la.getTimeBestSolutionFound();
											bestReturnedSolutionRuntimeStats.addValue(runtimeBestSolution);

											// convert to SPARQL query
											RDFResourceTree tree = bestSolution.getTree();
											tree = filter.apply(tree);
											String learnedSPARQLQuery = QueryTreeUtils.toSPARQLQueryString(
													tree, dataset.getBaseIRI(), dataset.getPrefixMapping());

											// compute score
											Score score = computeScore(sparqlQuery, tree, noise);
											bestReturnedSolutionPrecisionStats.addValue(score.precision);
											bestReturnedSolutionRecallStats.addValue(score.recall);
											bestReturnedSolutionFMeasureStats.addValue(score.fmeasure);
											bestReturnedSolutionPredAccStats.addValue(score.predAcc);
											bestReturnedSolutionMathCorrStats.addValue(score.mathCorr);
											logger.info(score.toString());

											// find the extensionally best matching tree in the list
											Pair<EvaluatedRDFResourceTree, Score> bestMatchingTreeWithScore = findBestMatchingTreeFast(solutions, sparqlQuery, noise, examples);
											EvaluatedRDFResourceTree bestMatchingTree = bestMatchingTreeWithScore.getFirst();
											Score bestMatchingScore = bestMatchingTreeWithScore.getSecond();

											// position of best tree in list of solutions
											int positionBestScore = solutions.indexOf(bestMatchingTree);
											bestSolutionPositionStats.addValue(positionBestScore);

											Score bestScore = score;
											if (positionBestScore > 0) {
												logger.info("Position of best covering tree in list: " + positionBestScore);
												logger.info("Best covering solution:\n" + render(bestMatchingTree.asEvaluatedDescription()));
												logger.info("Tree score: " + bestMatchingTree.getTreeScore());
												bestScore = bestMatchingScore;
												logger.info(bestMatchingScore.toString());
											} else {
												logger.info("Best returned solution was also the best covering solution.");
											}
											bestSolutionRecallStats.addValue(bestScore.recall);
											bestSolutionPrecisionStats.addValue(bestScore.precision);
											bestSolutionFMeasureStats.addValue(bestScore.fmeasure);
											bestSolutionPredAccStats.addValue(bestScore.predAcc);
											bestSolutionMathCorrStats.addValue(bestScore.mathCorr);

											for (RDFResourceTree negTree : examples.negExamplesMapping.values()) {
												if (QueryTreeUtils.isSubsumedBy(negTree, bestMatchingTree.getTree())) {
													Files.append(sparqlQuery + "\n", new File(System.getProperty("java.io.tmpdir") + File.separator + "negCovered.txt"), Charsets.UTF_8);
													break;
												}
											}

											String bestQuery = QueryFactory.create(QueryTreeUtils.toSPARQLQueryString(
													filter.apply(bestMatchingTree.getTree()),
													dataset.getBaseIRI(), dataset.getPrefixMapping())).toString();

											if (write2DB) {
												write2DB(sparqlQuery, nrOfExamples, examples, noise,
														baseLineQuery, baselineScore,
														heuristicName, measureName,
														QueryFactory.create(learnedSPARQLQuery).toString(), score, runtimeBestSolution,
														bestQuery, positionBestScore, bestScore);
											}

										} catch (Exception e) {
											failed.set(true);
											logger.error("Error occured for query\n" + sparqlQuery, e);
											try {
												StringWriter sw = new StringWriter();
												PrintWriter pw = new PrintWriter(sw);
												e.printStackTrace(pw);
												Files.append(sparqlQuery + "\n" + sw.toString(), new File(benchmarkDirectory, "failed-" + nrOfExamples + "-" + noise + "-" + heuristicName + "-" + measureName + ".txt"), Charsets.UTF_8);
											} catch (IOException e1) {
												e1.printStackTrace();
											}
										} finally {
											int cnt = currentNrOfFinishedRuns.incrementAndGet();
											logger.info("***********Evaluation Progress:"
													+ NumberFormat.getPercentInstance(Locale.ROOT).format((double) cnt / totalNrOfQTLRuns)
													+ "(" + cnt + "/" + totalNrOfQTLRuns + ")"
													+ "***********");
										}
									}
								});
						}
						
						tp.shutdown();
						tp.awaitTermination(12, TimeUnit.HOURS);

						Logger.getRootLogger().removeAppender(appender);
						
						if(!failed.get()) {
							String result = "";
							result += "\nBaseline Precision:\n" + baselinePrecisionStats;
							result += "\nBaseline Recall:\n" + baselineRecallStats;
							result += "\nBaseline F-measure:\n" + baselineFMeasureStats;
							result += "\nBaseline PredAcc:\n" + baselinePredAccStats;
							result += "\nBaseline MathCorr:\n" + baselineMathCorrStats;
							
							result += "#Returned solutions:\n" + nrOfReturnedSolutionsStats;
							
							result += "\nOverall Precision:\n" + bestReturnedSolutionPrecisionStats;
							result += "\nOverall Recall:\n" + bestReturnedSolutionRecallStats;
							result += "\nOverall F-measure:\n" + bestReturnedSolutionFMeasureStats;
							result += "\nOverall PredAcc:\n" + bestReturnedSolutionPredAccStats;
							result += "\nOverall MathCorr:\n" + bestReturnedSolutionMathCorrStats;
							
							result += "\nTime until best returned solution found:\n" + bestReturnedSolutionRuntimeStats;

							result += "\nPositions of best solution:\n" + Arrays.toString(bestSolutionPositionStats.getValues());
							result += "\nPosition of best solution stats:\n" + bestSolutionPositionStats;
							result += "\nOverall Precision of best solution:\n" + bestSolutionPrecisionStats;
							result += "\nOverall Recall of best solution:\n" + bestSolutionRecallStats;
							result += "\nOverall F-measure of best solution:\n" + bestSolutionFMeasureStats;
							
							result += "\nCBD generation time(total):\t" + MonitorFactory.getTimeMonitor(TimeMonitors.CBD_RETRIEVAL.name()).getTotal() + "\n";
							result += "CBD generation time(avg):\t" + MonitorFactory.getTimeMonitor(TimeMonitors.CBD_RETRIEVAL.name()).getAvg() + "\n";
							result += "Tree generation time(total):\t" + MonitorFactory.getTimeMonitor(TimeMonitors.TREE_GENERATION.name()).getTotal() + "\n";
							result += "Tree generation time(avg):\t" + MonitorFactory.getTimeMonitor(TimeMonitors.TREE_GENERATION.name()).getAvg() + "\n";
							result += "Tree size(avg):\t" + treeSizeStats.getMean() + "\n";
									
							logger.info(result);
							
							try {
								Files.write(result, statsFile, Charsets.UTF_8);
							} catch (IOException e) {
								e.printStackTrace();
							}
							
							data[i][j] = bestReturnedSolutionFMeasureStats.getMean();
							
							if(write2DB) {
								write2DB(heuristicName, measureName, nrOfExamples, noise, 
										bestReturnedSolutionFMeasureStats.getMean(), 
										bestReturnedSolutionPrecisionStats.getMean(), 
										bestReturnedSolutionRecallStats.getMean(),
										bestReturnedSolutionPredAccStats.getMean(),
										bestReturnedSolutionMathCorrStats.getMean(),
										bestSolutionPositionStats.getMean(),
										bestSolutionFMeasureStats.getMean(),
										bestSolutionPrecisionStats.getMean(),
										bestSolutionRecallStats.getMean(),
										bestSolutionPredAccStats.getMean(),
										bestSolutionMathCorrStats.getMean(),
										baselineFMeasureStats.getMean(), 
										baselinePrecisionStats.getMean(), 
										baselineRecallStats.getMean(),
										baselinePredAccStats.getMean(),
										baselineMathCorrStats.getMean(),
										bestReturnedSolutionRuntimeStats.getMean()
										);
							}
						}
					}
				}
				
				
				String content = "###";
				String separator = "\t";
				for (double noiseInterval1 : noiseIntervals) {
					content += separator + noiseInterval1;
				}
				content += "\n";
				for(int i = 0; i < nrOfExamplesIntervals.length; i++) {
					content += nrOfExamplesIntervals[i];
					for(int j = 0; j < noiseIntervals.length; j++) {
						content += separator + data[i][j];
					}
					content += "\n";
				}
				
				File examplesVsNoise = new File(benchmarkDirectory, "examplesVsNoise-" + heuristicName + "-" + measureName + ".tsv");
				try {
					Files.write(content, examplesVsNoise, Charsets.UTF_8);
				} catch (IOException e) {
					logger.error("failed to write stats to file", e);
				}
			}
		}

		if(write2DB) {
			conn.close();
		}

		if(useEmailNotification) {
			sendFinishedMail();
		}
		long t2 = System.currentTimeMillis();
		long duration = t2 - t1;
		logger.info("QTL evaluation finished in " + DurationFormatUtils.formatDurationHMS(duration) + "ms.");
	}

	private ExamplesWrapper getExamples(String query, int maxNrOfPosExamples, int maxNrOfNegExamples, double noise, CBDStructureTree cbdStructure) {
		return query2Examples
				.get(query)
				.get(maxNrOfPosExamples, maxNrOfNegExamples, noise, cbdStructure);
	}

	private String render(EvaluatedDescription ed) {
		return owlRenderer.render(ed.getDescription()) + dfPercent.format(ed.getAccuracy());
	}

	private void sendFinishedMail() throws EmailException, IOException {
		Properties config = new Properties();
		config.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/dllearner/algorithms/qtl/qtl-mail.properties"));

		Email email = new SimpleEmail();
		email.setHostName(config.getProperty("hostname"));
		email.setSmtpPort(465);
		email.setAuthenticator(new DefaultAuthenticator(config.getProperty("username"), config.getProperty("password")));
		email.setSSLOnConnect(true);
		email.setFrom(config.getProperty("from"));
		email.setSubject("QTL evaluation finished.");
		email.setMsg("QTL evaluation finished.");
		email.addTo(config.getProperty("to"));
		email.send();
	}

	/*
	 * Compute a baseline solution.
	 * 
	 * From simple to more complex:
	 * 
	 * 1. random type
	 * 2. most popular type in KB
	 * 3. most frequent type in pos. examples
	 * 4. most informative edge, e.g. based on information gain
	 * 5. LGG of all pos. examples
	 * 
	 */
	private RDFResourceTree applyBaseLine(ExamplesWrapper examples, Baseline baselineApproach) {
		logger.info("Computing baseline...");
		Collection<RDFResourceTree> posExamples = examples.posExamplesMapping.values();
		Collection<RDFResourceTree> negExamples = examples.negExamplesMapping.values();

		RDFResourceTree solution = null;

		switch (baselineApproach) {
		case RANDOM:// 1.
			String query = "SELECT ?cls WHERE {?cls a owl:Class .} ORDER BY RAND() LIMIT 1";
			QueryExecution qe = qef.createQueryExecution(query);
			ResultSet rs = qe.execSelect();
			if(rs.hasNext()) {
				QuerySolution qs = rs.next();
				Resource cls = qs.getResource("cls");
				solution = new RDFResourceTree();
				solution.addChild(new RDFResourceTree(cls.asNode()), RDF.type.asNode());
			}
			break;
		case MOST_POPULAR_TYPE_IN_KB:// 2.
			query = "SELECT ?cls WHERE {?cls a owl:Class . ?s a ?cls .} ORDER BY DESC(COUNT(?s)) LIMIT 1";
			qe = qef.createQueryExecution(query);
			rs = qe.execSelect();
			if(rs.hasNext()) {
				QuerySolution qs = rs.next();
				Resource cls = qs.getResource("cls");
				solution = new RDFResourceTree();
				solution.addChild(new RDFResourceTree(cls.asNode()), RDF.type.asNode());
			}
			break;
		case MOST_FREQUENT_TYPE_IN_EXAMPLES:// 3.
			Multiset<Node> types = HashMultiset.create();
			for (RDFResourceTree ex : posExamples) {
				List<RDFResourceTree> children = ex.getChildren(RDF.type.asNode());
				for (RDFResourceTree child : children) {
					types.add(child.getData());
				}
			}
			Node mostFrequentType = Ordering.natural().onResultOf(
					(Function<Multiset.Entry<Node>, Integer>) Multiset.Entry::getCount)
					.max(types.entrySet()).getElement();
			solution = new RDFResourceTree();
			solution.addChild(new RDFResourceTree(mostFrequentType), RDF.type.asNode());
			break;
		case MOST_FREQUENT_EDGE_IN_EXAMPLES:// 4.
			Multiset<Pair<Node, Node>> pairs = HashMultiset.create();
			for (RDFResourceTree ex : posExamples) {
				SortedSet<Node> edges = ex.getEdges();
				for (Node edge : edges) {
					List<RDFResourceTree> children = ex.getChildren(edge);
					for (RDFResourceTree child : children) {
						pairs.add(new Pair<>(edge, child.getData()));
					}
				}
			}
			Pair<Node, Node> mostFrequentPair = Ordering.natural().onResultOf(
					(Function<Multiset.Entry<Pair<Node, Node>>, Integer>) Multiset.Entry::getCount)
					.max(pairs.entrySet()).getElement();
			solution = new RDFResourceTree();
			solution.addChild(new RDFResourceTree(mostFrequentPair.getValue()), mostFrequentPair.getKey());
			break;
		case MOST_INFORMATIVE_EDGE_IN_EXAMPLES:
			// get all p-o in pos examples
			Multiset<Pair<Node, Node>> edgeObjectPairs = HashMultiset.create();
			for (RDFResourceTree ex : posExamples) {
				SortedSet<Node> edges = ex.getEdges();
				for (Node edge : edges) {
					List<RDFResourceTree> children = ex.getChildren(edge);
					for (RDFResourceTree child : children) {
						edgeObjectPairs.add(new Pair<>(edge, child.getData()));
					}
				}
			}
			
			double bestAccuracy = -1;
			solution = new RDFResourceTree();
			
			for (Pair<Node, Node> pair : edgeObjectPairs.elementSet()) {
				Node edge = pair.getKey();
				Node childValue = pair.getValue();
				
				// compute accuracy
				int tp = edgeObjectPairs.count(pair);
				int fn = posExamples.size() - tp;
				int fp = 0;
				for (RDFResourceTree ex : negExamples) { // compute false positives
					List<RDFResourceTree> children = ex.getChildren(edge);
					if(children != null) {
						for (RDFResourceTree child : children) {
							if(child.getData().equals(childValue)) {
								fp++;
								break;
							}
						}
					}
				}
				int tn = negExamples.size() - fp;
				
				double accuracy = Heuristics.getPredictiveAccuracy(
						posExamples.size(), 
						negExamples.size(), 
						tp, 
						tn, 
						1.0);
				// update best solution
				if(accuracy >= bestAccuracy) {
					solution = new RDFResourceTree();
					solution.addChild(new RDFResourceTree(childValue), edge);
					bestAccuracy = accuracy;
				}
			}
			break;
		case LGG:
			LGGGenerator lggGenerator = new LGGGeneratorSimple();
			solution = lggGenerator.getLGG(Lists.newArrayList(posExamples));
			break;
		default:
			break;
		}
		logger.info("Baseline solution:\n" + owlRenderer.render(QueryTreeUtils.toOWLClassExpression(solution)));

		return solution;
	}
	
	private List<EvaluatedRDFResourceTree> generateSolutions(ExamplesWrapper examples, double noise, QueryTreeHeuristic heuristic) throws ComponentInitException {
		// run QTL
		PosNegLPStandard lp = new PosNegLPStandard();
		lp.setPositiveExamples(examples.posExamplesMapping.keySet());
		lp.setNegativeExamples(examples.negExamplesMapping.keySet());
//		lp.init();

		QTL2Disjunctive la = new QTL2Disjunctive(lp, qef);
		la.setRenderer(new DLSyntaxObjectRendererExt());
		la.setReasoner(dataset.getReasoner());
		la.setEntailment(Entailment.RDFS);
		la.setTreeFactory(queryTreeFactory);
		la.setPositiveExampleTrees(examples.posExamplesMapping);
		la.setNegativeExampleTrees(examples.negExamplesMapping);
		la.setNoise(noise);
		la.setHeuristic(heuristic);
		la.setMaxExecutionTimeInSeconds(maxExecutionTimeInSeconds);
		la.setMaxTreeComputationTimeInSeconds(maxExecutionTimeInSeconds);
		la.init();
		la.start();

		List<EvaluatedRDFResourceTree> solutions = new ArrayList<>(la.getSolutions());
		
		return solutions;
	}
	
	private void solutionsFromCache(String sparqlQuery, int possibleNrOfExamples, double noise) {
		HashFunction hf = Hashing.md5();
		String hash = hf.newHasher()
				.putString(sparqlQuery, Charsets.UTF_8)
				.putInt(possibleNrOfExamples)
				.putDouble(noise)
				.hash().toString();
		File file = new File(cacheDirectory, hash + "-data.ttl");
		if(file.exists()) {
			
		}
	}
	
	private Pair<EvaluatedRDFResourceTree, Score> findBestMatchingTree(Collection<EvaluatedRDFResourceTree> trees,
			String targetSPARQLQuery, double noise) throws Exception {
	logger.info("Finding best matching query tree...");
		//get the tree with the highest fMeasure
		EvaluatedRDFResourceTree bestTree = null;
		Score bestScore = null;
		double bestFMeasure = -1;
		
		for (EvaluatedRDFResourceTree evalutedTree : trees) {
			RDFResourceTree tree = evalutedTree.getTree();
			
			// compute score
			Score score = computeScore(targetSPARQLQuery, tree, noise);
			double fMeasure = score.fmeasure;
			
			// we can stop if f-score is 1
			if(fMeasure == 1.0){
				return new Pair<>(evalutedTree, score);
			}
			
			if(fMeasure > bestFMeasure){
				bestFMeasure = fMeasure;
				bestTree = evalutedTree;
				bestScore = score;
			}
		}
		return new Pair<>(bestTree, bestScore);
	}
	
	/**
	 * find best query tree by searching for the tree which covers
	 * (1) most of the correct positive examples and
	 * (2) none of the noise positive examples
	 * @throws Exception 
	 */
	private Pair<EvaluatedRDFResourceTree, Score> findBestMatchingTreeFast(
			Collection<EvaluatedRDFResourceTree> trees, String targetSPARQLQuery, double noise,
			ExamplesWrapper examples) throws Exception{
		logger.info("Searching for best matching query tree...");
		
		Set<RDFResourceTree> correctPositiveExampleTrees = new HashSet<>();
		for (String ex  : examples.correctPosExamples) {
			correctPositiveExampleTrees.add(examples.posExamplesMapping.get(new OWLNamedIndividualImpl(IRI.create(ex))));
		}
		Set<RDFResourceTree> noisyPositiveExampleTrees = new HashSet<>();
		for (String ex  : examples.falsePosExamples) {
			noisyPositiveExampleTrees.add(examples.posExamplesMapping.get(new OWLNamedIndividualImpl(IRI.create(ex))));
		}
		
		
		EvaluatedRDFResourceTree bestTree = null;
		int coveredNoiseTreesBest = 0;
		int coveredCorrectTreesBest = 0;
		
		for (EvaluatedRDFResourceTree evaluatedTree : trees) {
			RDFResourceTree tree = evaluatedTree.getTree();
			
			int coveredNoiseTrees = 0;
			for (RDFResourceTree noiseTree : noisyPositiveExampleTrees) {
				if(QueryTreeUtils.isSubsumedBy(noiseTree, tree)) {
					coveredNoiseTrees++;
				}
			}
			
			int coveredCorrectTrees = 0;
			for (RDFResourceTree correctTree : correctPositiveExampleTrees) {
				if(QueryTreeUtils.isSubsumedBy(correctTree, tree)) {
					coveredCorrectTrees++;
				}
			}

//			System.err.println("+" + coveredCorrectTrees + "|-" + coveredNoiseTrees);
			// this is obviously the most perfect solution according to the input
			if(coveredNoiseTrees == 0 && coveredCorrectTrees == correctPositiveExampleTrees.size()) {
				bestTree = evaluatedTree;
				break;
			}
			
			if(coveredCorrectTrees > coveredCorrectTreesBest || coveredNoiseTrees < coveredNoiseTreesBest) {
				bestTree = evaluatedTree;
				coveredCorrectTreesBest = coveredCorrectTrees;
				coveredNoiseTreesBest = coveredNoiseTrees;
			} 
		}
		
		// compute score
		String learnedSPARQLQuery = QueryTreeUtils.toSPARQLQueryString(bestTree.getTree(), dataset.getBaseIRI(), dataset.getPrefixMapping());

		Score score = computeScore(targetSPARQLQuery, bestTree.getTree(), noise);
		return new Pair<>(bestTree, score);
	}

	private String hash(String query) {
		return Hashing.md5().newHasher().putString(query, Charsets.UTF_8).hash().toString();
	}
	
	private ExampleCandidates generateExamples(String sparqlQuery, boolean posOnly, boolean noiseEnabled) throws Exception {
		logger.info("Generating examples for query ..." + sparqlQuery);

		// generate hash for the query
		String queryHash = hash(sparqlQuery);

		// create examples folder
		File examplesDirectory = new File(cacheDirectory, "examples");
		examplesDirectory.mkdirs();

		// create sub-folder for the query
		examplesDirectory = new File(examplesDirectory, queryHash);
		examplesDirectory.mkdirs();

		// get all pos. examples, i.e. resources returned by the query 
		List<String> posExamples;
		File file = new File(examplesDirectory, "examples.tp");
		if(file.exists()) {
			posExamples = Files.readLines(file, Charsets.UTF_8);
		} else {
			posExamples = getResult(sparqlQuery, false);
			Files.write(Joiner.on("\n").join(posExamples), file, Charsets.UTF_8);
		}
		Collections.sort(posExamples);
		logger.info("#Pos. examples: " + posExamples.size());

		List<String> negExamples = new ArrayList<>();
		List<String> noiseCandidates = new ArrayList<>();
		if(!posOnly) {
			// get some neg. examples, i.e. resources not returned by the query
			int maxNrOfNegExamples = 100;
			file = new File(examplesDirectory, "examples-" + maxNrOfNegExamples + ".tn");
			if(file.exists()) {
				negExamples = Files.readLines(file, Charsets.UTF_8);
			} else {
				negExamples = new NegativeExampleSPARQLQueryGenerator(qef).getNegativeExamples(sparqlQuery, maxNrOfNegExamples);
				Files.write(Joiner.on("\n").join(negExamples), file, Charsets.UTF_8);
			}
			Collections.sort(negExamples);
			logger.info("#Neg. examples: " + negExamples.size());

			if(noiseEnabled) {
				// get some noise candidates, i.e. resources used as false pos. examples
				int maxNrOfNoiseCandidates = 100;
				file = new File(examplesDirectory, "examples-" + maxNrOfNoiseCandidates + ".fp");
				if(file.exists()) {
					noiseCandidates = Files.readLines(file, Charsets.UTF_8);
				} else {
					noiseCandidates = noiseGenerator.generateNoiseCandidates(sparqlQuery, noiseMethod,
							ListUtils.union(posExamples, negExamples), maxNrOfNoiseCandidates);
					Files.write(Joiner.on("\n").join(noiseCandidates), file, Charsets.UTF_8);
				}
				logger.info("#False pos. example candidates: " + noiseCandidates.size());
			}

		}

		return new ExampleCandidates(posExamples, negExamples, noiseCandidates);
	}
	
	private RDFResourceTree getSimilarTree(RDFResourceTree tree, String property, int maxTreeDepth){
		String query = "SELECT ?o WHERE {?s <" + property + "> ?o. FILTER(isURI(?o) && ?o != <" + tree.getData() + ">)} LIMIT 1";
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		if(rs.hasNext()){
			Resource object = rs.next().getResource("o");
			Model cbd = cbdGen.getConciseBoundedDescription(object.getURI(), maxTreeDepth);
			RDFResourceTree similarTree = queryTreeFactory.getQueryTree(object, cbd, maxTreeDepth);
			similarTree.setData(object.asNode());
			return similarTree;
		}
		return null;
	}
	


	private RDFResourceTree getQueryTree(String resource, CBDStructureTree cbdStructure) throws Exception {
		// get CBD
		logger.info("loading data for {} ...", resource);
		Monitor mon = MonitorFactory.getTimeMonitor(TimeMonitors.CBD_RETRIEVAL.name()).start();
		Model cbd = cbdGen.getConciseBoundedDescription(resource, cbdStructure);
		mon.stop();
		logger.info("got {} triples in {}ms.", cbd.size(), mon.getLastValue());

		// rewrite NAN to NaN to avoid parse exception
		try(ByteArrayOutputStream baos = new ByteArrayOutputStream()){
			cbd.write(baos, "N-TRIPLES", null);
			String modelAsString = new String(baos.toByteArray());
			modelAsString = modelAsString.replace("NAN", "NaN");
			Model newModel = ModelFactory.createDefaultModel();
			newModel.read(new StringReader(modelAsString), null, "TURTLE");
			cbd = newModel;
		} catch (IOException e) {
			e.printStackTrace();
		}

		// generate tree
		logger.info("generating query tree for {} ...", resource);
		mon = MonitorFactory.getTimeMonitor(TimeMonitors.TREE_GENERATION.name()).start();
		RDFResourceTree tree = queryTreeFactory.getQueryTree(resource, cbd, maxTreeDepth);
		mon.stop();
		logger.info("generating query tree for {} took {}ms.", resource, mon.getLastValue());

//		System.out.println(tree.getStringRepresentation());

		// keep track of tree size
		int size = QueryTreeUtils.getNrOfNodes(tree);
		treeSizeStats.addValue(size);

		return tree;
	}
	
	private List<String> getResult(String sparqlQuery){
		return getResult(sparqlQuery, true);
	}
	
	private List<String> getResult(String sparqlQuery, boolean useCache){
		logger.trace(sparqlQuery);
		List<String> resources = cache.get(sparqlQuery);
		if(resources == null || !useCache) {
			resources = new ArrayList<>();
//			sparqlQuery = getPrefixedQuery(sparqlQuery);
			
			// we assume a single projection var
			Query query = QueryFactory.create(sparqlQuery);
			String projectVar = query.getProjectVars().get(0).getName();
			System.out.println(query);
			ResultSet rs = qef.createQueryExecution(sparqlQuery).execSelect();
			QuerySolution qs;
			while(rs.hasNext()){
				qs = rs.next();
				
				if(qs.get(projectVar).isURIResource()){
					resources.add(qs.getResource(projectVar).getURI());
				} else if(qs.get(projectVar).isLiteral()){
					resources.add(qs.getLiteral(projectVar).toString());
				}
			}
			cache.put(sparqlQuery, resources);
		}
		
		return resources;
	}
	
	/**
	 * Split the SPARQL query and join the result set of each split. This
	 * allows for the execution of more complex queries.
	 * @param sparqlQuery
	 * @return
	 */
	private List<String> getResultSplitted(String sparqlQuery){
		Query query = QueryFactory.create(sparqlQuery);
		logger.trace("Getting result set splitted for\n{}", query);

		List<Query> queries = QueryRewriter.split(query);

		List<String> resources = getResult(queries.remove(0).toString());
		queries.stream().map(q -> getResult(q.toString())).forEach(resources::retainAll);

		return resources;
	}
	
	private Score computeScore(String referenceSparqlQuery, RDFResourceTree tree, double noise) throws Exception{
		logger.info("computing score...");
		// apply some filters
		QueryTreeUtils.removeVarLeafs(tree);
		QueryTreeUtils.prune(tree, null, Entailment.RDF);

		// remove redundant rdf:type triples
		QueryTreeUtils.keepMostSpecificTypes(tree, dataset.getReasoner());

		// predicates removed which do not contribute if the simply exists without a concrete value
		tree = filter.apply(tree);

		String learnedSPARQLQuery = QueryTreeUtils.toSPARQLQueryString(tree, dataset.getBaseIRI(), dataset.getPrefixMapping());
		logger.info("learned SPARQL query:\n{}", learnedSPARQLQuery);
		
		if(QueryUtils.getTriplePatterns(QueryFactory.create(learnedSPARQLQuery)).size() < 25) {
			return computeScoreBySparqlCount(referenceSparqlQuery, tree, noise);
		}
		
		// get the reference resources
		List<String> referenceResources = getResult(referenceSparqlQuery);
		if (referenceResources.isEmpty()) {
			logger.error("Reference SPARQL query returns no result.\n" + referenceSparqlQuery);
			return new Score();
		}

		// if query is most general one P=|TARGET|/|KB| R=1
		if (learnedSPARQLQuery.equals(QueryTreeUtils.EMPTY_QUERY_TREE_QUERY)) {
			
			int tp = referenceResources.size();
			int fp = kbSize - tp;
			int tn = 0;
			int fn = 0;
			
			return score(tp, fp, tn, fn);
		}

		// get the learned resources
		List<String> learnedResources = splitComplexQueries ? getResultSplitted(learnedSPARQLQuery) : getResult(learnedSPARQLQuery);
		Files.write(Joiner.on("\n").join(learnedResources), new File(System.getProperty("java.io.tmpdir") + File.separator + "result.txt"), Charsets.UTF_8);
		if (learnedResources.isEmpty()) {
			logger.error("Learned SPARQL query returns no result.\n{}", learnedSPARQLQuery);
			return new Score();
		}

		// get the overlapping resources
		int overlap = Sets.intersection(Sets.newHashSet(referenceResources), Sets.newHashSet(learnedResources)).size();

		int tp = overlap;
		int fp = Sets.difference(Sets.newHashSet(learnedResources), Sets.newHashSet(referenceResources)).size();
		int fn = Sets.difference(Sets.newHashSet(referenceResources), Sets.newHashSet(learnedResources)).size();
		int tn = kbSize - tp - fp - fn;
		
		return score(tp, fp, tn, fn);
	}
	
	private Score computeScoreBySparqlCount(String referenceSparqlQuery, RDFResourceTree tree, double noise) throws Exception{
		logger.debug("Computing score by COUNT query...");
		String learnedSPARQLQuery = QueryTreeUtils.toSPARQLQueryString(tree, dataset.getBaseIRI(), dataset.getPrefixMapping());

		Query referenceQuery = QueryFactory.create(referenceSparqlQuery);
		final ExprVar s = new ExprVar(referenceQuery.getProjectVars().get(0));
		Var cntVar = Var.alloc("cnt");
		
		// Q1
		Query q1 = QueryFactory.create(referenceSparqlQuery);
		Query q1Count = QueryFactory.create();
		q1Count.setQuerySelectType();
		q1Count.getProject().add(cntVar, new ExprAggregator(s.asVar(), new AggCountVarDistinct(s)));
		q1Count.setQueryPattern(q1.getQueryPattern());
		logger.debug("Reference COUNT query:\n" + q1Count);
		QueryExecution qe = qef.createQueryExecution(q1Count);
		ResultSet rs = qe.execSelect();
		QuerySolution qs = rs.next();
		int referenceCnt = qs.getLiteral(cntVar.getName()).getInt();
		qe.close();
				
		// if query is most general one P=|TARGET|/|KB| R=1
		if (learnedSPARQLQuery.equals(QueryTreeUtils.EMPTY_QUERY_TREE_QUERY)) {
			
			int tp = referenceCnt;
			int fp = kbSize - tp;
			int tn = 0;
			int fn = 0;
			
			return score(tp, fp, tn, fn);
		}
				
		// Q2
		Query q2 = QueryFactory.create(learnedSPARQLQuery);
		Var targetVar = q2.getProjectVars().get(0);
		Query q2Count = QueryFactory.create();
		q2Count.setQuerySelectType();
		q2Count.getProject().add(cntVar, new ExprAggregator(targetVar, new AggCountVarDistinct(new ExprVar(targetVar))));
		q2Count.setQueryPattern(q2.getQueryPattern());
		logger.debug("Learned COUNT query:\n" + q2Count);
		q2Count = VirtuosoUtils.rewriteForVirtuosoDateLiteralBug(q2Count);
		qe = qef.createQueryExecution(q2Count);
		rs = qe.execSelect();
		qs = rs.next();
		int learnedCnt = qs.getLiteral(cntVar.getName()).getInt();
		qe.close();
		
		
		// Q1 ∪ Q2
		// if noise = 0 then Q1 ∪ Q2 = Q2
		int overlap = Math.min(learnedCnt, referenceCnt);
		if(noise > 0) {
			Query q12 = QueryFactory.create();
			q12.setQuerySelectType();
			q12.getProject().add(cntVar, new ExprAggregator(s.asVar(), new AggCountVarDistinct(s)));
			ElementGroup whereClause = new ElementGroup();
			for (Element el : ((ElementGroup)q1.getQueryPattern()).getElements()) {
				whereClause.addElement(el);
			}
			for (Element el : ((ElementGroup)q2.getQueryPattern()).getElements()) {
				whereClause.addElement(el);
			}
			q12.setQueryPattern(whereClause);
			logger.debug("Combined COUNT query:\n" + q12);
			q12 = VirtuosoUtils.rewriteForVirtuosoDateLiteralBug(q12);
			qe = qef.createQueryExecution(q12);
			rs = qe.execSelect();
			qs = rs.next();
			overlap = qs.getLiteral(cntVar.getName()).getInt();
			qe.close();
		}

		int tp = overlap;
		int fp = learnedCnt - overlap;
		int fn = referenceCnt - overlap;
		int tn = kbSize - tp - fp - fn;

		logger.debug("finished computing score.");

		return score(tp, fp, tn, fn);
	}
	
	private Score score(int tp, int fp, int tn, int fn) throws Exception {
		System.err.println(String.format("tp:%d fp:%d tn:%s fn:%s", tp, fp, tn, fn));
		// P
		double precision = (tp == 0 && fp == 0) ? 1.0 : (double) tp / (tp + fp);
		System.err.println(precision);
		
		// R
		double recall = (tp == 0 && fn == 0) ? 1.0 : (double) tp / (tp + fn);
		System.err.println(recall);
		
		//F_1
		double fMeasure = Heuristics.getFScore(recall, precision);
		
		// pred. acc
		double predAcc = (tp + tn) / (double)((tp + fn) + (tn + fp));

		BigDecimal denominator = BigDecimal.valueOf(tp + fp).
				multiply(BigDecimal.valueOf(tp + fn)).
				multiply(BigDecimal.valueOf(tn + fp)).
				multiply(BigDecimal.valueOf(tn + fn));
		
		// Mathews CC
		double mathCorr = denominator.doubleValue() == 0 ? 0 : (tp * tn - fp * fn) / Math.sqrt(denominator.doubleValue());
		
		if(Double.isNaN(predAcc) || Double.isNaN(mathCorr)){
			throw new Exception(Double.isNaN(predAcc) ? ("PredAcc") : ("MC") + " not a number.");
		}
		
		return new Score(precision, recall, fMeasure, predAcc, mathCorr);
	}
	
	private void write2DB(String targetQuery, int nrOfExamples, ExamplesWrapper examples,
			double noise, String baseLineQuery, Score baselineScore, String heuristicName, String heuristicMeasure,
			String returnedQuery, Score returnedQueryScore, long returnedRuntime, String bestQuery, int bestQueryPosition,
			Score bestQueryScore) {
		logger.trace("Writing to DB...");
		try {
			psInsertDetailEval.setString(1, targetQuery);
			psInsertDetailEval.setInt(2, nrOfExamples);
			psInsertDetailEval.setDouble(3, noise);
			psInsertDetailEval.setString(4, heuristicName);
			psInsertDetailEval.setString(5, heuristicMeasure);
			
			psInsertDetailEval.setString(6, returnedQuery);
			psInsertDetailEval.setDouble(7, returnedQueryScore.fmeasure);
			psInsertDetailEval.setDouble(8, returnedQueryScore.precision);
			psInsertDetailEval.setDouble(9, returnedQueryScore.recall);
			
			psInsertDetailEval.setString(10, bestQuery);
			psInsertDetailEval.setInt(11, bestQueryPosition);
			psInsertDetailEval.setDouble(12, bestQueryScore.fmeasure);
			psInsertDetailEval.setDouble(13, bestQueryScore.precision);
			psInsertDetailEval.setDouble(14, bestQueryScore.recall);
			
			psInsertDetailEval.setString(15, baseLineQuery);
			psInsertDetailEval.setDouble(16, baselineScore.fmeasure);
			psInsertDetailEval.setDouble(17, baselineScore.precision);
			psInsertDetailEval.setDouble(18, baselineScore.recall);

			psInsertDetailEval.setLong(19, returnedRuntime);

//			logger.trace(psInsertDetailEval);
			psInsertDetailEval.executeUpdate();
			logger.trace("...finished writing to DB.");
		} catch (Exception e) {
			logger.error("Writing to DB failed with " + psInsertDetailEval, e);
		}
		
	}
	
	private synchronized void write2DB(
			String heuristic, String heuristicMeasure, int nrOfExamples, double noise, 
			double fmeasure, double precision, double recall, double predAcc, double mathCorr,
			double bestSolutionPosition, double bestSolutionFmeasure, double bestSolutionPrecision, double bestSolutionRecall, double bestSolutionPredAcc, double bestSolutionMathCorr,
			double baselineFmeasure, double baselinePrecision, double baselineRecall, double baselinePredAcc, double baselineMathCorr,
			double bestReturnedSolutionRuntime
			) {
		logger.trace("Writing to DB...");
		try {
			psInsertOverallEval.setString(1, heuristic);
			psInsertOverallEval.setString(2, heuristicMeasure);
			psInsertOverallEval.setInt(3, nrOfExamples);
			psInsertOverallEval.setDouble(4, noise);
			
			psInsertOverallEval.setDouble(5, fmeasure);
			psInsertOverallEval.setDouble(6, precision);
			psInsertOverallEval.setDouble(7, recall);
			psInsertOverallEval.setDouble(8, predAcc);
			psInsertOverallEval.setDouble(9, mathCorr);
			
			psInsertOverallEval.setDouble(10, bestSolutionPosition);
			psInsertOverallEval.setDouble(11, bestSolutionFmeasure);
			psInsertOverallEval.setDouble(12, bestSolutionPrecision);
			psInsertOverallEval.setDouble(13, bestSolutionRecall);
			psInsertOverallEval.setDouble(14, bestSolutionPredAcc);
			psInsertOverallEval.setDouble(15, bestSolutionMathCorr);
			
			psInsertOverallEval.setDouble(16, baselineFmeasure);
			psInsertOverallEval.setDouble(17, baselinePrecision);
			psInsertOverallEval.setDouble(18, baselineRecall);
			psInsertOverallEval.setDouble(19, baselinePredAcc);
			psInsertOverallEval.setDouble(20, baselineMathCorr);
			
			psInsertOverallEval.setDouble(21, bestReturnedSolutionRuntime);

//			logger.trace(psInsertOverallEval.toString());
			psInsertOverallEval.executeUpdate();
			logger.trace("...finished writing to DB.");
		} catch (Exception e) {
			logger.error("Writing to DB failed with " + psInsertOverallEval, e);
		}
	}
	
	public static void main(String[] args) throws Exception {
		StringRenderer.setRenderer(Rendering.DL_SYNTAX);
		Logger.getLogger(PRConvergenceExperiment.class).addAppender(
				new FileAppender(new SimpleLayout(), "log/qtl-qald.log", false));
		Logger.getRootLogger().setLevel(Level.INFO);
		Logger.getLogger(QTL2Disjunctive.class).setLevel(Level.INFO);
		Logger.getLogger(PRConvergenceExperiment.class).setLevel(Level.INFO);
		Logger.getLogger(QueryExecutionFactoryCacheEx.class).setLevel(Level.INFO);
		
		OptionParser parser = new OptionParser();
		OptionSpec<String> datasetSpec = parser.accepts("dataset", "possible datasets: QALD4-Bio or QALD6-DBpedia").withRequiredArg().ofType(String.class).required();
		OptionSpec<File> benchmarkDirectorySpec = parser.accepts("d", "base directory").withRequiredArg().ofType(File.class).required();
		OptionSpec<File> queriesFileSpec = parser.accepts("q", "processed queries file").withRequiredArg().ofType(File.class);
		OptionSpec<URL> endpointURLSpec = parser.accepts("e", "endpoint URL").withRequiredArg().ofType(URL.class).required();
		OptionSpec<String> defaultGraphSpec = parser.accepts("g", "default graph").withRequiredArg().ofType(String.class);
		OptionSpec<Boolean> overrideSpec = parser.accepts("o", "override previous results").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
		OptionSpec<Boolean> write2DBSpec = parser.accepts("db", "write to database").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
		OptionSpec<Boolean> emailNotificationSpec = parser.accepts("mail", "enable email notification").withOptionalArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);
		OptionSpec<Integer> maxNrOfQueriesSpec = parser.accepts("max-queries", "max. nr. of processed queries").withRequiredArg().ofType(Integer.class).defaultsTo(-1);
		OptionSpec<Integer> maxTreeDepthSpec = parser.accepts("max-tree-depth", "max. depth of processed queries and generated trees").withRequiredArg().ofType(Integer.class).defaultsTo(2);
		OptionSpec<Integer> maxQTLRuntimeSpec = parser.accepts("max-qtl-runtime", "max. runtime of each QTL run").withRequiredArg().ofType(Integer.class).defaultsTo(60);
		OptionSpec<Integer> nrOfThreadsSpec = parser.accepts("thread-count", "number of threads used for parallel evaluation").withRequiredArg().ofType(Integer.class).defaultsTo(1);

		OptionSpec<String> exampleIntervalsSpec = parser.accepts("examples", "comma-separated list of number of examples used in evaluation").withRequiredArg().ofType(String.class).defaultsTo("");
		OptionSpec<String> noiseIntervalsSpec = parser.accepts("noise", "comma-separated list of noise values used in evaluation").withRequiredArg().ofType(String.class).defaultsTo("");
		OptionSpec<String> measuresSpec = parser.accepts("measures", "comma-separated list of measures used in evaluation").withRequiredArg().ofType(String.class);

		OptionSpec<String> queriesToOmitTokensSpec = parser.accepts("omitTokens", "comma-separated list of tokens such that queries containing any of them will be omitted").withRequiredArg().ofType(String.class).defaultsTo("");
		OptionSpec<String> queriesToProcessTokensSpec = parser.accepts("processTokens", "comma-separated list of tokens such that queries containing any of them will be omitted").withRequiredArg().ofType(String.class).defaultsTo("");

		OptionSpec<String> databaseNameSpec = parser.accepts("dbName", "database name").withRequiredArg().ofType(String.class);

		OptionSpec<String> cbdSpec = parser.accepts("cbd", "CBD structure tree string").withRequiredArg().ofType(String.class);
		OptionSpec<Boolean> workaroundSpec = parser.accepts("workaround", "Virtuoso parse error workaround enabled").withRequiredArg().ofType(Boolean.class).defaultsTo(Boolean.FALSE);

		OptionSet options = null;
		try {
			options = parser.parse(args);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			parser.printHelpOn(System.out);
			System.exit(0);
		}


		File benchmarkDirectory = options.valueOf(benchmarkDirectorySpec);
		boolean write2DB = options.valueOf(write2DBSpec);
		boolean override = options.valueOf(overrideSpec);
		boolean useEmailNotification = options.valueOf(emailNotificationSpec);
		URL endpointURL = options.valueOf(endpointURLSpec);
		String defaultGraph = options.has(defaultGraphSpec) ? options.valueOf(defaultGraphSpec) : null;
		SparqlEndpoint endpoint = SparqlEndpoint.create(endpointURL.toString(), defaultGraph);
		int maxNrOfQueries = options.valueOf(maxNrOfQueriesSpec);
		int maxTreeDepth = options.valueOf(maxTreeDepthSpec);
		int maxQTLRuntime = options.valueOf(maxQTLRuntimeSpec);
		int nrOfThreads = options.valueOf(nrOfThreadsSpec);

		File queriesFile = null;
		if(options.has(queriesFileSpec)) {
			queriesFile = options.valueOf(queriesFileSpec);
		}

		int[] exampleInterval = null;
		if(options.has(exampleIntervalsSpec)) {
			exampleInterval = StreamSupport.stream(
					Splitter.on(',').omitEmptyStrings().trimResults().split(options.valueOf(exampleIntervalsSpec)).spliterator(), false)
					.map(Integer::valueOf).mapToInt(Integer::intValue).toArray();
		}

		double[] noiseInterval = null;
		if(options.has(noiseIntervalsSpec)) {
			noiseInterval = StreamSupport.stream(
					Splitter.on(',').omitEmptyStrings().trimResults().split(options.valueOf(noiseIntervalsSpec)).spliterator(), false)
					.map(Double::valueOf).mapToDouble(Double::doubleValue).toArray();
		}

		HeuristicType[] measures = null;
		if(options.has(measuresSpec)) {
			String s = options.valueOf(measuresSpec);
			String[] split = s.split(",");
			measures = new HeuristicType[split.length];
			for(int i = 0; i < split.length; i++) {
				if(split[i].equalsIgnoreCase("mcc")) {
					measures[i] = HeuristicType.MATTHEWS_CORRELATION;
				} else {
					measures[i] = HeuristicType.valueOf(split[i].toUpperCase());
				}
			}
		}

		List<String> omitTokens = Splitter
				.on(",")
				.omitEmptyStrings()
				.trimResults()
				.splitToList(options.valueOf(queriesToOmitTokensSpec));
		List<String> processTokens = Splitter
				.on(",")
				.omitEmptyStrings()
				.trimResults()
				.splitToList(options.valueOf(queriesToProcessTokensSpec));

//		EvaluationDataset dataset = new DBpediaEvaluationDataset(benchmarkDirectory, endpoint, queriesFile);
		String datasetName = options.valueOf(datasetSpec);
		EvaluationDataset dataset;
		if(datasetName.equals("QALD4-Bio")){
			dataset = new QALD4BiomedicalChallengeEvaluationDataset(benchmarkDirectory);
		} else if(datasetName.equals("QALD6-DBpedia")){
			dataset = new QALD6DBpediaEvaluationDataset(benchmarkDirectory);
		} else {
			throw new RuntimeException("Unsupported dataset:" + datasetName);
		}

		String databaseName = options.valueOf(databaseNameSpec);

		CBDStructureTree cbdStructureTree = options.has(options.valueOf(cbdSpec)) ? CBDStructureTree.fromTreeString(options.valueOf(cbdSpec).trim()) : null;


		PRConvergenceExperiment eval = new PRConvergenceExperiment(dataset, benchmarkDirectory,
				write2DB, databaseName, override, maxQTLRuntime, useEmailNotification, nrOfThreads);
		eval.setQueriesToOmitTokens(omitTokens);
		eval.setQueriesToProcessTokens(processTokens);
		eval.setDatabaseName(databaseName);
		eval.setDefaultCbdStructure(cbdStructureTree);
		eval.setWorkaroundEnabled(options.valueOf(workaroundSpec), endpoint);
		eval.run(maxNrOfQueries, maxTreeDepth, exampleInterval, noiseInterval, measures);

//		new QALDExperiment(Dataset.BIOMEDICAL).run();
	}

	public void setDefaultCbdStructure(CBDStructureTree cbdStructureTree) {
		this.cbdStructureTree = cbdStructureTree;
	}

	public void setQueriesToProcessTokens(Collection<String> queriesToProcessTokens) {
		this.queriesToProcessTokens.addAll(queriesToProcessTokens);
	}

	class Score {
		int tp, fp, tn, fn = 0;
		double precision, recall, fmeasure, predAcc, mathCorr = 0;

		public Score() {}
		
		public Score(double precision, double recall, double fmeasure, double predAcc, double mathCorr) {
			this.precision = precision;
			this.recall = recall;
			this.fmeasure = fmeasure;
			this.predAcc = predAcc;
			this.mathCorr = mathCorr;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("P=%f\nR=%f\nF-score=%f\nPredAcc=%f\nMC=%f", precision, recall, fmeasure, predAcc, mathCorr);
		}
		
	}
	

	class ExampleCandidates {

		List<String> correctPosExampleCandidates;
		List<String> falsePosExampleCandidates;
		List<String> correctNegExampleCandidates;

		Random rnd = new Random(123);

		public ExampleCandidates(
								 List<String> correctPosExampleCandidates,
								 List<String> correctNegExampleCandidates,
								 List<String> falsePosExampleCandidates) {
			this.correctPosExampleCandidates = correctPosExampleCandidates;
			this.falsePosExampleCandidates = falsePosExampleCandidates;
			this.correctNegExampleCandidates = correctNegExampleCandidates;
		}
		
		public ExamplesWrapper get(int nrOfPosExamples, int nrOfNegExamples, double noise, CBDStructureTree cbdStructure) {
			// random sublist of the pos. examplessak
			List<String> correctPosExamples = new ArrayList<>(correctPosExampleCandidates);
			Collections.sort(correctPosExamples);
			Collections.shuffle(correctPosExamples, rnd);
			correctPosExamples = new ArrayList<>(correctPosExamples.subList(0, Math.min(correctPosExamples.size(), nrOfPosExamples)));
			
			// random sublist of the neg. examples
			List<String> negExamples = new ArrayList<>(correctNegExampleCandidates);
			Collections.sort(negExamples);
			Collections.shuffle(negExamples, rnd);
			negExamples = new ArrayList<>(negExamples.subList(0, Math.min(negExamples.size(), nrOfNegExamples)));
			

			List<String> falsePosExamples = new ArrayList<>();
			if(noise > 0) {
				// randomly replace some of the pos. examples by false examples
				// 2 options
				// 1: iterate over pos. examples and if random number is below t_n, replace the example
				// 2: replace the (#posExamples * t_n) randomly chosen pos. examples by randomly chosen negative examples
				List<String> falsePosExampleCandidates = new ArrayList<>(this.falsePosExampleCandidates);
				Collections.sort(falsePosExampleCandidates);
				Collections.shuffle(falsePosExampleCandidates, rnd);

				boolean probabilityBased = false;

				if (probabilityBased) {
					// 1. way
					for (Iterator<String> iterator = correctPosExamples.iterator(); iterator.hasNext();) {
						String posExample = iterator.next();
						double rndVal = rnd.nextDouble();
						if (rndVal <= noise) {
							// remove the positive example
							iterator.remove();

							// add one of the negative examples
							String falsePosExample = falsePosExampleCandidates.remove(0);
							falsePosExamples.add(falsePosExample);
							logger.info("Replacing " + posExample + " by " + falsePosExample);
						}
					}
				} else {
					// 2. way
					// replace at least 1 but not more than half of the examples
					int upperBound = correctPosExamples.size() / 2;
					int nrOfPosExamples2Replace = Math.min((int) Math.ceil(noise * correctPosExamples.size()), upperBound);

					logger.info("replacing " + nrOfPosExamples2Replace + "/" + correctPosExamples.size() + " examples to introduce noise");
					List<String> posExamples2Replace = new ArrayList<>(correctPosExamples.subList(0, nrOfPosExamples2Replace));
					correctPosExamples.removeAll(posExamples2Replace);

					falsePosExamples = falsePosExampleCandidates.subList(0, nrOfPosExamples2Replace);
					logger.info("replaced " + posExamples2Replace + "\nby\n" + falsePosExamples);
				}
			}
			
			// ensure determinism
			Collections.sort(correctPosExamples);
			Collections.sort(negExamples);
			Collections.sort(falsePosExamples);
			
			// generate trees
			SortedMap<OWLIndividual, RDFResourceTree> posExamplesMapping = new TreeMap<>();
			for (String ex : ListUtils.union(correctPosExamples, falsePosExamples)) {
				try {
					posExamplesMapping.put(new OWLNamedIndividualImpl(IRI.create(ex)), getQueryTree(ex, cbdStructure));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			SortedMap<OWLIndividual, RDFResourceTree> negExamplesMapping = new TreeMap<>();
			for (String ex : negExamples) {
				try {
					negExamplesMapping.put(new OWLNamedIndividualImpl(IRI.create(ex)), getQueryTree(ex, cbdStructure));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			return new ExamplesWrapper(correctPosExamples, falsePosExamples, negExamples, posExamplesMapping, negExamplesMapping);
		}
	}
}
