/**
 * 
 */
package org.dllearner.algorithms.qtl.experiments;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.Pair;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithms.qtl.QTL2Disjunctive;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.EvaluatedRDFResourceTree;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBase;
import org.dllearner.algorithms.qtl.util.Entailment;
import org.dllearner.algorithms.qtl.util.filters.PredicateExistenceFilter;
import org.dllearner.algorithms.qtl.util.filters.PredicateExistenceFilterDBpedia;
import org.dllearner.algorithms.qtl.util.statistics.TimeMonitors;
import org.dllearner.core.ComponentInitException;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.utilities.QueryUtils;
import org.dllearner.utilities.experiments.Jamon;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;

import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;
import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

import com.google.common.base.Charsets;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.datatypes.xsd.impl.XSDAbstractDateTimeType;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_LessThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.E_NotEquals;
import com.hp.hpl.jena.sparql.expr.E_NotExists;
import com.hp.hpl.jena.sparql.expr.E_NumAbs;
import com.hp.hpl.jena.sparql.expr.E_Str;
import com.hp.hpl.jena.sparql.expr.E_Subtract;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.aggregate.AggCountVarDistinct;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.util.iterator.Filter;
import com.jamonapi.MonKeyBase;
import com.jamonapi.MonitorFactory;

/**
 * @author Lorenz Buehmann
 *
 */
public class QTLEvaluation {
	
	private static final Logger logger = Logger.getLogger(QTLEvaluation.class.getName());
	
	private static final ParameterizedSparqlString superClassesQueryTemplate2 = new ParameterizedSparqlString(
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX owl: <http://www.w3.org/2002/07/owl#> "
			+ "SELECT ?sup WHERE {"
			+ "?sub ((rdfs:subClassOf|owl:equivalentClass)|^owl:equivalentClass)+ ?sup .}");
	
	private static final ParameterizedSparqlString superClassesQueryTemplate = new ParameterizedSparqlString(
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX owl: <http://www.w3.org/2002/07/owl#> "
			+ "SELECT ?sup WHERE {"
			+ "?sub (rdfs:subClassOf|owl:equivalentClass)+ ?sup .}");
	
	enum NoiseMethod {
		RANDOM, SIMILAR, SIMILARITY_PARAMETERIZED
	}
	
	NoiseMethod noiseMethod = NoiseMethod.RANDOM;
	
	static Map<String, String> prefixes = new HashMap<String, String>();
	static {
		prefixes.put("sider", "http://www4.wiwiss.fu-berlin.de/sider/resource/sider/");
		prefixes.put("side_effects", "http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/");
		prefixes.put("drug", "http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/");
		prefixes.put("diseasome", "http://www4.wiwiss.fu-berlin.de/diseasome/resource/diseasome/");
		
		prefixes.put("dbo", "http://dbpedia.org/ontology/");
		prefixes.put("dbpedia", "http://dbpedia.org/resource/");
	}
	
	
	List<String> questionFiles;
	QueryExecutionFactory qef;
	String cacheDirectory = "./cache/qtl";
	
	int minNrOfPositiveExamples = 9;
	int maxDepth = 2;
	
	private org.dllearner.algorithms.qtl.impl.QueryTreeFactory queryTreeFactory;
	private ConciseBoundedDescriptionGenerator cbdGen;
	
	RandomDataGenerator rnd = new RandomDataGenerator();

	private EvaluationDataset dataset;
	
	private Map<String, List<String>> cache = new HashMap<String, List<String>>();
	
	private int kbSize;

	private boolean splitComplexQueries = true;
	
	PredicateExistenceFilter filter = new PredicateExistenceFilterDBpedia(null);
	
	List<String> noiseExamples = new ArrayList<String>();

	private Map<OWLIndividual, RDFResourceTree> generatedExamples;

	private List<String> correctExamples;
	
	public QTLEvaluation(EvaluationDataset dataset) throws ComponentInitException {
		this.dataset = dataset;

		queryTreeFactory = new QueryTreeFactoryBase();
		queryTreeFactory.setMaxDepth(maxDepth);
		
		// add some filters to avoid resources with namespaces like http://dbpedia.org/property/
		queryTreeFactory.addDropFilters((Filter<Statement>[]) dataset.getQueryTreeFilters().toArray(new Filter[]{}));
		
		qef = dataset.getKS().getQueryExecutionFactory();
		
		cbdGen = new ConciseBoundedDescriptionGeneratorImpl(qef);
		cbdGen.setRecursionDepth(maxDepth);
		
		rnd.reSeed(123);
		
		kbSize = getKBSize();
	}
	
	
	
	private int getKBSize() {
		String query = "SELECT (COUNT(*) AS ?cnt) WHERE {[] a ?type . ?type a <http://www.w3.org/2002/07/owl#Class> .}";
		
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		int size = rs.next().get("cnt").asLiteral().getInt();
		
		qe.close();
		
		return size;
	}
	
	private List<String> getSparqlQueries() {
		List<String> sparqlQueries = new ArrayList<String>();
		
		for (String query : dataset.getSparqlQueries()) {
			Query q = QueryFactory.create(query);
			int subjectObjectJoinDepth = QueryUtils.getSubjectObjectJoinDepth(q, q.getProjectVars().get(0));
			if(subjectObjectJoinDepth < maxDepth) {
				sparqlQueries.add(query);
			}
		}
		return sparqlQueries;
	}
	
	public void run(){
		
		List<String> sparqlQueries = getSparqlQueries();
		logger.info("Total number of queries: " + sparqlQueries.size());
		
		// parameters
		int minNrOfExamples = 3;
		int maxNrOfExamples = 30;
		int stepSize = 2;
		
		double[] noiseIntervals = {
				0.0,
				0.2,
//				0.4,
//				0.6
				};
			
		// loop over number of positive examples
		for (int nrOfExamples = minNrOfExamples; nrOfExamples <= maxNrOfExamples; nrOfExamples = nrOfExamples + stepSize) {
			
			// loop over noise value
			for (int i = 0; i < noiseIntervals.length; i++) {
				
				double noise = noiseIntervals[i];
				
				FileAppender appender = null;
				try {
					appender = new FileAppender(new SimpleLayout(), "log/qtl/qtl2-" + nrOfExamples + "-" + noise + ".log", false);
					Logger.getRootLogger().addAppender(appender);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				
				logger.info("#examples: " + nrOfExamples + " noise: " + noise);
				
				DescriptiveStatistics nrOfReturnedSolutionsStats = new DescriptiveStatistics();
				
				DescriptiveStatistics bestReturnedSolutionPrecisionStats = new DescriptiveStatistics();
				DescriptiveStatistics bestReturnedSolutionRecallStats = new DescriptiveStatistics();
				DescriptiveStatistics bestReturnedSolutionFMeasureStats = new DescriptiveStatistics();
				
				DescriptiveStatistics bestSolutionPrecisionStats = new DescriptiveStatistics();
				DescriptiveStatistics bestSolutionRecallStats = new DescriptiveStatistics();
				DescriptiveStatistics bestSolutionFMeasureStats = new DescriptiveStatistics();
				
				DescriptiveStatistics bestSolutionPositionStats = new DescriptiveStatistics();
				
				MonitorFactory.getTimeMonitor(TimeMonitors.CBD_RETRIEVAL.name()).reset();
				MonitorFactory.getTimeMonitor(TimeMonitors.TREE_GENERATION.name()).reset();
				
//				if(nrOfExamples != 7) continue;
				// loop over SPARQL queries
				for (String sparqlQuery : sparqlQueries) {
//					if(!sparqlQuery.contains("Alternative_rock"))continue;
					logger.info("##############################################################");
					logger.info("Processing query\n" + sparqlQuery);
					// some queries can return less examples
					int possibleNrOfExamples = Math.min(getResultCount(sparqlQuery), nrOfExamples);
					
					try {
						// compute or load cached solutions
						List<EvaluatedRDFResourceTree> solutions = generateSolutions(sparqlQuery, possibleNrOfExamples, noise);
						nrOfReturnedSolutionsStats.addValue(solutions.size());
						
						// the best returned solution by QTL
						EvaluatedRDFResourceTree bestSolution = solutions.get(0);
						logger.info("Got " + solutions.size() + " query trees.");
						logger.info("Best computed solution:\n" + bestSolution.asEvaluatedDescription());
						logger.info("QTL Score:\n" + bestSolution.getTreeScore());

						// convert to SPARQL query
						RDFResourceTree tree = bestSolution.getTree();
//						filter.filter(tree);
						String learnedSPARQLQuery = QueryTreeUtils.toSPARQLQueryString(
								tree, dataset.getBaseIRI(), dataset.getPrefixMapping());

						// compute score
						Score score = computeScore(sparqlQuery, tree);
						bestReturnedSolutionPrecisionStats.addValue(score.getPrecision());
						bestReturnedSolutionRecallStats.addValue(score.getRecall());
						bestReturnedSolutionFMeasureStats.addValue(score.getFmeasure());
						logger.info(score);

						// find the extensionally best matching tree in the list
						Pair<EvaluatedRDFResourceTree, Score> bestMatchingTreeWithScore = findBestMatchingTreeFast(solutions, sparqlQuery);
						EvaluatedRDFResourceTree bestMatchingTree = bestMatchingTreeWithScore.getFirst();
						Score bestMatchingScore = bestMatchingTreeWithScore.getSecond();
						
						// position of best tree in list of solutions
						int position = solutions.indexOf(bestMatchingTree);
						bestSolutionPositionStats.addValue(position);

						Score bestScore = score;
						if (position > 0) {
							logger.info("Position of best covering tree in list: " + position);
							logger.info("Best covering solution:\n" + bestMatchingTree.asEvaluatedDescription());
							logger.info("Tree score: " + bestMatchingTree.getTreeScore());
							String bestLearnedSPARQLQuery = QueryTreeUtils.toSPARQLQueryString(
									filter.filter(bestMatchingTree.getTree()), 
									dataset.getBaseIRI(), dataset.getPrefixMapping());
							bestScore = bestMatchingScore;
							logger.info(bestMatchingScore);
						} else {
							logger.info("Best returned solution was also the best covering solution.");
						}
						bestSolutionRecallStats.addValue(bestScore.getRecall());
						bestSolutionPrecisionStats.addValue(bestScore.getPrecision());
						bestSolutionFMeasureStats.addValue(bestScore.getFmeasure());

					} catch (Exception e) {
						logger.error("Error occured.", e);
						System.exit(0);
					}
				}
				
				Logger.getRootLogger().removeAppender(appender);
				
				String result = "";
				result += "\n#Returned solutions:" + nrOfReturnedSolutionsStats + "\n";
				
				result += "\nOverall Precision:\n" + bestReturnedSolutionPrecisionStats;
				result += "\nOverall Recall:\n" + bestReturnedSolutionRecallStats;
				result += "\nOverall FMeasure:\n" + bestReturnedSolutionFMeasureStats;
				result += "\nPositions of best solution:\n" + Arrays.toString(bestSolutionPositionStats.getValues());
				result += "\nPosition of best solution stats:\n" + bestSolutionPositionStats;
				
				result += "\nOverall Precision of best solution:\n" + bestSolutionPrecisionStats;
				result += "\nOverall Recall of best solution:\n" + bestSolutionRecallStats;
				result += "\nOverall FMeasure of best solution:\n" + bestSolutionFMeasureStats;
				
				result += "\nCBD generation time(total):\t" + MonitorFactory.getTimeMonitor(TimeMonitors.CBD_RETRIEVAL.name()).getTotal() + "\n";
				result += "CBD generation time(avg):\t" + MonitorFactory.getTimeMonitor(TimeMonitors.CBD_RETRIEVAL.name()).getAvg() + "\n";
				result += "Tree generation time(total):\t" + MonitorFactory.getTimeMonitor(TimeMonitors.TREE_GENERATION.name()).getTotal() + "\n";
				result += "Tree generation time(avg):\t" + MonitorFactory.getTimeMonitor(TimeMonitors.TREE_GENERATION.name()).getAvg() + "\n";
						
				logger.info(result);
				
				try {
					Files.write(result, new File("log/qtl/qtl2-" + nrOfExamples + "-" + noise + ".stats"), Charsets.UTF_8);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private List<EvaluatedRDFResourceTree> generateSolutions(String sparqlQuery, int possibleNrOfExamples, double noise) throws ComponentInitException {
		generatedExamples = generateExamples(sparqlQuery, possibleNrOfExamples, noise);

		// run QTL
		PosNegLPStandard lp = new PosNegLPStandard();
		lp.setPositiveExamples(generatedExamples.keySet());
//		lp.init();

		QTL2Disjunctive la = new QTL2Disjunctive(lp, qef);
		la.setReasoner(dataset.getReasoner());
		la.setEntailment(Entailment.RDFS);
		la.setTreeFactory(queryTreeFactory);
		la.setPositiveExampleTrees(generatedExamples);
		la.setNoise(noise);
		la.init();
		la.start();

		List<EvaluatedRDFResourceTree> solutions = new ArrayList<EvaluatedRDFResourceTree>(la.getSolutions());
		
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
	
	private Pair<EvaluatedRDFResourceTree, Score> findBestMatchingTree(Collection<EvaluatedRDFResourceTree> trees, String targetSPARQLQuery){
		logger.info("Finding best matching query tree...");
		//get the tree with the highest fmeasure
		EvaluatedRDFResourceTree bestTree = null;
		Score bestScore = null;
		double bestFMeasure = -1;
		
		for (EvaluatedRDFResourceTree evalutedTree : trees) {
			RDFResourceTree tree = evalutedTree.getTree();
			
			// compute score
			Score score = computeScore(targetSPARQLQuery, tree);
			double fMeasure = score.getFmeasure();
			
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
	 */
	private Pair<EvaluatedRDFResourceTree, Score> findBestMatchingTreeFast(
			Collection<EvaluatedRDFResourceTree> trees, String targetSPARQLQuery
			){
		logger.info("Finding best matching query tree...");
		
		Set<RDFResourceTree> correctPositiveExampleTrees = new HashSet<RDFResourceTree>();
		for (String ex  : correctExamples) {
			correctPositiveExampleTrees.add(generatedExamples.get(new OWLNamedIndividualImpl(IRI.create(ex))));
		}
		Set<RDFResourceTree> noisyPositiveExampleTrees = new HashSet<RDFResourceTree>();
		for (String ex  : noiseExamples) {
			noisyPositiveExampleTrees.add(generatedExamples.get(new OWLNamedIndividualImpl(IRI.create(ex))));
		}
		
		
		EvaluatedRDFResourceTree bestTree = null;
		int coveredNoiseTreesBest = 0;
		int coveredCorrectTreesBest = 0;
		
		for (EvaluatedRDFResourceTree evalutedTree : trees) {
			RDFResourceTree tree = evalutedTree.getTree();
			
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
				bestTree = evalutedTree;
				break;
			}
			
			if(coveredCorrectTrees > coveredCorrectTreesBest || coveredNoiseTrees < coveredNoiseTreesBest) {
				bestTree = evalutedTree;
				coveredCorrectTreesBest = coveredCorrectTrees;
				coveredNoiseTreesBest = coveredNoiseTrees;
			} 
		}
		
		// compute score
		String learnedSPARQLQuery = QueryTreeUtils.toSPARQLQueryString(bestTree.getTree(), dataset.getBaseIRI(), dataset.getPrefixMapping());
		System.out.println(learnedSPARQLQuery);
		
		Score score = computeScore(targetSPARQLQuery, bestTree.getTree());
		return new Pair<>(bestTree, score);
	}
	
	private Map<OWLIndividual, RDFResourceTree> generateExamples(String sparqlQuery, int maxNrOfExamples, double noise){
		Random randomGen = new Random(123);
		
		// get all resources returned by the query
		List<String> resources = getResult(sparqlQuery, false);
		
		// pick some random positive examples from the list
		Collections.shuffle(resources, randomGen);
		List<String> examples = resources.subList(0, Math.min(maxNrOfExamples, resources.size()));
		logger.info("Pos. examples: " + examples);
		
		// add noise if enabled
		if(noise > 0) {
			generateNoise(examples, sparqlQuery, noise, randomGen);
		} else {
			correctExamples = new ArrayList<String>(examples);
			noiseExamples = new ArrayList<String>();
		}
		
		// build query trees
		Map<OWLIndividual, RDFResourceTree> queryTrees = new HashMap<>();
		for (String ex : examples) {
			RDFResourceTree queryTree = getQueryTree(ex);
			queryTrees.put(new OWLNamedIndividualImpl(IRI.create(ex)), queryTree);
		}
		
		// add noise by modifying the query trees
//		generateNoiseAttributeLevel(sparqlQuery, queryTrees, noise);
		
		return queryTrees;
	}
	
	private RDFResourceTree getSimilarTree(RDFResourceTree tree, String property, int maxTreeDepth){
		String query = "SELECT ?o WHERE {?s <" + property + "> ?o. FILTER(isURI(?o) && ?o != <" + tree.getData() + ">)} LIMIT 1";
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		if(rs.hasNext()){
			Resource object = rs.next().getResource("o");
			Model cbd = cbdGen.getConciseBoundedDescription(object.getURI(), maxTreeDepth);
			RDFResourceTree similarTree = queryTreeFactory.getQueryTree(object, cbd);
			similarTree.setData(object.asNode());
			return similarTree;
		}
		return null;
	}
	
	private void generateNoise(List<String> examples, String sparqlQuery, double noise, Random randomGen) {
		// generate noise example candidates
		List<String> noiseCandidateExamples = null;
		switch(noiseMethod) {
		case RANDOM: noiseCandidateExamples = generateNoiseCandidatesRandom(examples, 10);
			break;
		case SIMILAR:noiseCandidateExamples = generateNoiseCandidatesSimilar(examples, sparqlQuery);
			break;
		case SIMILARITY_PARAMETERIZED://TODO implement configurable noise method
			break;
		default:noiseCandidateExamples = generateNoiseCandidatesRandom(examples, 10);
			break;
		}
		Collections.shuffle(noiseCandidateExamples, randomGen);

		// add some noise by using instances close to the positive examples
		// we have two ways of adding noise t_n
		// 1: iterate over pos. examples and if random number is below t_n, replace the example
		// 2: replace the (#posExamples * t_n) randomly chosen pos. examples by randomly chosen negative examples
		boolean probabilityBased = false;

		if (probabilityBased) {
			// 1. way
			List<String> newExamples = new ArrayList<String>();
			for (Iterator<String> iterator = examples.iterator(); iterator.hasNext();) {
				String posExample = iterator.next();
				double rnd = randomGen.nextDouble();
				if (rnd <= noise) {
					// remove the positive example
					iterator.remove();
					// add one of the negative examples
					String negExample = noiseCandidateExamples.remove(0);
					newExamples.add(negExample);
					logger.info("Replacing " + posExample + " by " + negExample);
				}
			}
			examples.addAll(newExamples);
		} else {
			// 2. way
			// replace at least 1 but not more than half of the examples
			int upperBound = examples.size() / 2;
			int nrOfPosExamples2Replace = (int) Math.ceil(noise * examples.size());
			nrOfPosExamples2Replace = Math.min(nrOfPosExamples2Replace, upperBound);
			logger.info("replacing " + nrOfPosExamples2Replace + "/" + examples.size() + " examples to introduce noise");
			List<String> posExamples2Replace = new ArrayList<>(examples.subList(0, nrOfPosExamples2Replace));
			examples.removeAll(posExamples2Replace);
			List<String> negExamples4Replacement = noiseCandidateExamples.subList(0, nrOfPosExamples2Replace);
			noiseExamples = new ArrayList<String>(negExamples4Replacement);
			correctExamples = new ArrayList<String>(examples);
			examples.addAll(negExamples4Replacement);
			logger.info("replaced " + posExamples2Replace + " by " + negExamples4Replacement);
		}
	}
	
	/**
	 * Randomly pick {@code n} instances from KB that do not belong to given examples {@code examples}.
	 * @param examples the examples that must not be contained in the returned list
	 * @param n the number of random examples
	 * @return
	 */
	private List<String> generateNoiseCandidatesRandom(List<String> examples, int n) {
		List<String> noiseExamples = new ArrayList<>();
		
		// get max number of instances in KB
		String query = "SELECT (COUNT(*) AS ?cnt) WHERE {[] a ?type . ?type a <http://www.w3.org/2002/07/owl#Class> .}";
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		int max = rs.next().get("cnt").asLiteral().getInt();
		
		// generate random instances
		while(noiseExamples.size() < n) {
			int offset = rnd.nextInt(0, max);
			query = "SELECT ?s WHERE {?s a [] .} LIMIT 1 OFFSET " + offset;
			
			qe = qef.createQueryExecution(query);
			rs = qe.execSelect();
			
			String resource = rs.next().getResource("s").getURI();
			
			if(!examples.contains(resource) && !resource.contains("__")) {
				noiseExamples.add(resource);
			}
			qe.close();
		}
		
		return noiseExamples;
	}
	
	private List<String> generateNoiseCandidatesSimilar(List<String> examples, String queryString){
		Query query = QueryFactory.create(queryString);
		
		QueryUtils queryUtils = new QueryUtils();
		
		Set<Triple> triplePatterns = queryUtils.extractTriplePattern(query);
		
		Set<String> negExamples = new HashSet<String>();
		
		if(triplePatterns.size() == 1){
			Triple tp = triplePatterns.iterator().next();
			Node var = NodeFactory.createVariable("var");
			Triple newTp = Triple.create(tp.getSubject(), tp.getPredicate(), var);
			
			ElementTriplesBlock triplesBlock = new ElementTriplesBlock();
			triplesBlock.addTriple(newTp);
			
			ElementFilter filter = new ElementFilter(new E_NotEquals(new ExprVar(var), NodeValue.makeNode(tp.getObject())));
			
			ElementGroup eg = new ElementGroup();
			eg.addElement(triplesBlock);
			eg.addElementFilter(filter);
			
			Query q = new Query();
			q.setQuerySelectType();
			q.setDistinct(true);
			q.addProjectVars(query.getProjectVars());
			
			q.setQueryPattern(eg);
//			System.out.println(q);
			
			List<String> result = getResult(q.toString());
			negExamples.addAll(result);
		} else {
			// we modify each triple pattern <s p o> by <s p ?var> . ?var != o
			Set<Set<Triple>> powerSet = new TreeSet<>(new Comparator<Set<Triple>>() {

				@Override
				public int compare(Set<Triple> o1, Set<Triple> o2) {
					return ComparisonChain.start().compare(o1.size(), o2.size()).compare(o1.hashCode(), o2.hashCode()).result();
				}
			});
			powerSet.addAll(Sets.powerSet(triplePatterns));
			
			for (Set<Triple> set : powerSet) {
				if(!set.isEmpty() && set.size() != triplePatterns.size()){
					List<Triple> existingTriplePatterns = new ArrayList<>(triplePatterns);
					List<Triple> newTriplePatterns = new ArrayList<>();
					List<ElementFilter> filters = new ArrayList<ElementFilter>();
					int cnt = 0;
					for (Triple tp : set) {
						if(tp.getObject().isURI() || tp.getObject().isLiteral()){
							Node var = NodeFactory.createVariable("var" + cnt++);
							Triple newTp = Triple.create(tp.getSubject(), tp.getPredicate(), var);
							
							existingTriplePatterns.remove(tp);
							newTriplePatterns.add(newTp);
							
							ElementTriplesBlock triplesBlock = new ElementTriplesBlock();
							triplesBlock.addTriple(tp);
							
							ElementGroup eg = new ElementGroup();
							eg.addElement(triplesBlock);
							
							ElementFilter filter = new ElementFilter(new E_NotExists(eg));
							filters.add(filter);
						}
					}
					Query q = new Query();
					q.setQuerySelectType();
					q.setDistinct(true);
					q.addProjectVars(query.getProjectVars());
					List<Triple> allTriplePatterns = new ArrayList<Triple>(existingTriplePatterns);
					allTriplePatterns.addAll(newTriplePatterns);
					ElementTriplesBlock tripleBlock = new ElementTriplesBlock(BasicPattern.wrap(allTriplePatterns));
					ElementGroup eg = new ElementGroup();
					eg.addElement(tripleBlock);
					
					for (ElementFilter filter : filters) {
						eg.addElementFilter(filter);
					}
					
					q.setQueryPattern(eg);
//					System.out.println(q);
					
					List<String> result = getResult(q.toString());
					result.removeAll(examples);
					
					if(result.isEmpty()){
						q = new Query();
						q.setQuerySelectType();
						q.setDistinct(true);
						q.addProjectVars(query.getProjectVars());
						tripleBlock = new ElementTriplesBlock(BasicPattern.wrap(existingTriplePatterns));
						eg = new ElementGroup();
						eg.addElement(tripleBlock);
						
						for (ElementFilter filter : filters) {
							eg.addElementFilter(filter);
						}
						
						q.setQueryPattern(eg);
//						System.out.println(q);
						
						result = getResult(q.toString());
						result.removeAll(examples);
					}
					negExamples.addAll(result);
				}
			}
		}
		
		negExamples.removeAll(examples);
		if(negExamples.isEmpty()){
			logger.error("Found no negative example.");
			System.exit(0);
		}
		return new ArrayList<>(negExamples);
	}
	
	private List<RDFResourceTree> getQueryTrees(List<String> resources){
		List<RDFResourceTree> trees = new ArrayList<RDFResourceTree>();
		
		for (String resource : resources) {
			trees.add(getQueryTree(resource));
		}
		
		return trees;
	}
	
	private RDFResourceTree getQueryTree(String resource){
		// get CBD
		MonitorFactory.getTimeMonitor(TimeMonitors.CBD_RETRIEVAL.name()).start();
		Model cbd = cbdGen.getConciseBoundedDescription(resource);
		MonitorFactory.getTimeMonitor(TimeMonitors.CBD_RETRIEVAL.name()).stop();
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
		MonitorFactory.getTimeMonitor(TimeMonitors.TREE_GENERATION.name()).start();
		RDFResourceTree tree = queryTreeFactory.getQueryTree(resource, cbd);
		MonitorFactory.getTimeMonitor(TimeMonitors.TREE_GENERATION.name()).stop();
		return tree;
	}
	
	private List<String> getResult(String sparqlQuery){
		return getResult(sparqlQuery, true);
	}
	
	private List<String> getResult(String sparqlQuery, boolean useCache){
		logger.trace(sparqlQuery);
		List<String> resources = cache.get(sparqlQuery);
		if(resources == null || !useCache) {
			resources = new ArrayList<String>();
//			sparqlQuery = getPrefixedQuery(sparqlQuery);
			
			// we assume a single projection var
			Query query = QueryFactory.create(sparqlQuery);
			String projectVar = query.getProjectVars().get(0).getName();
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
		logger.trace("Getting result set for\n" + query);
		
		QueryUtils queryUtils = new QueryUtils();
		Set<Triple> triplePatterns = queryUtils.extractTriplePattern(query);
		
		// remove triple patterns with unbound object vars
		if(triplePatterns.size() > 10) {
			query = removeUnboundObjectVarTriples(query);
			triplePatterns = queryUtils.extractTriplePattern(query);
		} 
		
	//  Virtuoso bug workaround with literals of type xsd:float and xsd:double
		for (Iterator<Triple> iterator = triplePatterns.iterator(); iterator.hasNext();) {
			Node object = iterator.next().getObject();
			if(object.isLiteral() && object.getLiteralDatatype() != null 
					&& (object.getLiteralDatatype().equals(XSDDatatype.XSDfloat) || object.getLiteralDatatype().equals(XSDDatatype.XSDdouble))){
				iterator.remove();
			}
		}
					
		
		Var targetVar = query.getProjectVars().get(0); // should be ?x0
		
		Multimap<Var, Triple> var2TriplePatterns = HashMultimap.create();
		for (Triple tp : triplePatterns) {
			var2TriplePatterns.put(Var.alloc(tp.getSubject()), tp);
		}
		
		// we keep only the most specific types for each var
		filterOutGeneralTypes(var2TriplePatterns);
		
		// 1. get the outgoing triple patterns of the target var that do not have
		// outgoing triple patterns
		Set<Triple> fixedTriplePatterns = new HashSet<Triple>();
		Set<Set<Triple>> clusters = new HashSet<Set<Triple>>();
		Collection<Triple> targetVarTriplePatterns = var2TriplePatterns.get(targetVar);
		boolean useSplitting = false;
		for (Triple tp : targetVarTriplePatterns) {
			Node object = tp.getObject();
			if(object.isConcrete() || !var2TriplePatterns.containsKey(Var.alloc(object))){
				fixedTriplePatterns.add(tp);
			} else {
				Set<Triple> cluster = new TreeSet<>(new Comparator<Triple>() {
					@Override
					public int compare(Triple o1, Triple o2) {
						return ComparisonChain.start().
						compare(o1.getSubject().toString(), o2.getSubject().toString()).
						compare(o1.getPredicate().toString(), o2.getPredicate().toString()).
						compare(o1.getObject().toString(), o2.getObject().toString()).
						result();
					}
				});
				cluster.add(tp);
				clusters.add(cluster);
				useSplitting = true;
			}
		}
		
		if(!useSplitting){
			clusters.add(Sets.newHashSet(fixedTriplePatterns));
		} else {
			logger.trace("Query too complex. Splitting...");
			// 2. build clusters for other
			for (Set<Triple> cluster : clusters) {
				Triple representative = cluster.iterator().next();
				cluster.addAll(var2TriplePatterns.get(Var.alloc(representative.getObject())));
				cluster.addAll(fixedTriplePatterns);
			}
		}
		
		// again split clusters to have only a maximum number of triple patterns
		int maxNrOfTriplePatternsPerQuery = 20;// number of outgoing triple patterns form the target var in each executed query
		Set<Set<Triple>> newClusters = new HashSet<Set<Triple>>();
		for (Set<Triple> cluster : clusters) {
			int cnt = 0;
			for (Triple triple : cluster) {
				if(triple.getSubject().matches(targetVar)) {
					cnt++;
				}
			}
			
			if(cnt > maxNrOfTriplePatternsPerQuery) {
				Set<Triple> newCluster = new HashSet<Triple>();
				for (Triple triple : cluster) {
					if(triple.getSubject().matches(targetVar)) {
						newCluster.add(triple);
					}
					if(newCluster.size() == maxNrOfTriplePatternsPerQuery) {
						newClusters.add(newCluster);
						newCluster = new HashSet<Triple>();
					}
				}
				if(!newCluster.isEmpty()) {
					newClusters.add(newCluster);
				}
			}
		}
		for (Set<Triple> cluster : newClusters) {
			
			for(int i = 1; i < maxDepth; i++) {
				Set<Triple> additionalTriples = new HashSet<Triple>();
				for (Triple triple : cluster) {
					if(triple.getObject().isVariable()){
						additionalTriples.addAll(var2TriplePatterns.get(Var.alloc(triple.getObject())));
					}
				}
				cluster.addAll(additionalTriples);
			}
			
		}
//		clusters = newClusters;
		
		
		
		Set<String> resources = null;
		// 3. run query for each cluster
		for (Set<Triple> cluster : clusters) {
			Query q = new Query();
			q.addProjectVars(Collections.singleton(targetVar));
			ElementTriplesBlock el = new ElementTriplesBlock();
			for (Triple triple : cluster) {
				el.addTriple(triple);
			}
			q.setQuerySelectType();
			q.setDistinct(true);
			q.setQueryPattern(el);
			
			q = rewriteForVirtuosoDateLiteralBug(q);
//			q = rewriteForVirtuosoFloatingPointIssue(q);
			logger.trace(q);
//			sparqlQuery = getPrefixedQuery(sparqlQuery);
			System.out.println(q);
			List<String> partialResult = getResult(q.toString());
			Set<String> resourcesTmp = new HashSet<String>(partialResult);
			
			if(resourcesTmp.isEmpty()) {
				System.err.println("Empty query result");
				System.err.println(q);
//				System.exit(0);
				return Collections.EMPTY_LIST;
			}
			
			if(resources == null){
				resources = resourcesTmp;
			} else {
				resources.retainAll(resourcesTmp);
			}
		}
		
		return new ArrayList<String>(resources);
	}
	
	private Query removeUnboundObjectVarTriples(Query query) {
		QueryUtils queryUtils = new QueryUtils();
		Set<Triple> triplePatterns = queryUtils.extractTriplePattern(query);
		
		Multimap<Var, Triple> var2TriplePatterns = HashMultimap.create();
		for (Triple tp : triplePatterns) {
			var2TriplePatterns.put(Var.alloc(tp.getSubject()), tp);
		}
		
		Iterator<Triple> iterator = triplePatterns.iterator();
		while (iterator.hasNext()) {
			Triple triple = iterator.next();
			Node object = triple.getObject();
			if(object.isVariable() && !var2TriplePatterns.containsKey(Var.alloc(object))) {
				iterator.remove();
			}
		}
		
		Query newQuery = new Query();
		newQuery.addProjectVars(query.getProjectVars());
		ElementTriplesBlock el = new ElementTriplesBlock();
		for (Triple triple : triplePatterns) {
			el.addTriple(triple);
		}
		newQuery.setQuerySelectType();
		newQuery.setDistinct(true);
		newQuery.setQueryPattern(el);
		
		return newQuery;
	}
	
	private void filterOutGeneralTypes(Multimap<Var, Triple> var2Triples) {
		// keep the most specific types for each subject
		for (Var subject : var2Triples.keySet()) {
			Collection<Triple> triplePatterns = var2Triples.get(subject);
			Collection<Triple> triplesPatterns2Remove = new HashSet<Triple>();

			for (Triple tp : triplePatterns) {
				if (tp.getObject().isURI() && !triplesPatterns2Remove.contains(tp)) {
					// get all super classes for the triple object
					Set<Node> superClasses = getSuperClasses(tp.getObject());

					// remove triple patterns that have one of the super classes as object
					for (Triple tp2 : triplePatterns) {
						if(tp2 != tp && superClasses.contains(tp2.getObject())) {
							triplesPatterns2Remove.add(tp2);
						}
					}
				}
			}
			
			// remove triple patterns
			triplePatterns.removeAll(triplesPatterns2Remove);
		}
	}
	
	private Set<Node> getSuperClasses(Node cls){
		Set<Node> superClasses = new HashSet<Node>();
		
		superClassesQueryTemplate.setIri("sub", cls.getURI());
		
		String query = superClassesQueryTemplate.toString();
		
		try {
			QueryExecution qe = qef.createQueryExecution(query);
			ResultSet rs = qe.execSelect();
			while(rs.hasNext()){
				QuerySolution qs = rs.next();
				superClasses.add(qs.getResource("sup").asNode());
			}
			qe.close();
		} catch (Exception e) {
			System.out.println(query);
			throw e;
		}
		
		return superClasses;
	}
	
	private int getResultCount(String sparqlQuery){
		sparqlQuery = "PREFIX owl: <http://www.w3.org/2002/07/owl#> " + sparqlQuery;
		int cnt = 0;
		QueryExecution qe = qef.createQueryExecution(sparqlQuery);
		ResultSet rs = qe.execSelect();
		while(rs.hasNext()){
			rs.next();
			cnt++;
		}
		qe.close();
		return cnt;
	}
	
	
	private Query rewriteForVirtuosoFloatingPointIssue(Query query){
		QueryUtils queryUtils = new QueryUtils();
		Set<Triple> triplePatterns = queryUtils.extractTriplePattern(query);
		
		Set<Triple> newTriplePatterns = new TreeSet<>(new Comparator<Triple>() {
			@Override
			public int compare(Triple o1, Triple o2) {
				return ComparisonChain.start().
				compare(o1.getSubject().toString(), o2.getSubject().toString()).
				compare(o1.getPredicate().toString(), o2.getPredicate().toString()).
				compare(o1.getObject().toString(), o2.getObject().toString()).
				result();
			}
		});
		List<ElementFilter> filters = new ArrayList<>();
		int cnt = 0;
		// <s p o>
		for (Iterator<Triple> iter = triplePatterns.iterator(); iter.hasNext();) {
			Triple tp = iter.next();
			if(tp.getObject().isLiteral()){
				RDFDatatype dt = tp.getObject().getLiteralDatatype();
				if(dt != null && (dt.equals(XSDDatatype.XSDfloat) || dt.equals(XSDDatatype.XSDdouble))){
					iter.remove();
					// new triple pattern <s p ?var> 
					Node objectVar = NodeFactory.createVariable("floatVal" + cnt++);
					newTriplePatterns.add(Triple.create(
							tp.getSubject(), 
							tp.getPredicate(),
							objectVar)
							);
					// add FILTER(STR(?var) = lexicalform(o))
					System.out.println(tp.getObject());
					double epsilon = 0.00001;
					Expr filterExpr = new E_LessThanOrEqual(
							new E_NumAbs(
									new E_Subtract(
											new ExprVar(objectVar), 
											NodeValue.makeNode(tp.getObject())
											)
									),
									NodeValue.makeDouble(epsilon)
							);
					ElementFilter filter = new ElementFilter(filterExpr);
					filters.add(filter);
				}
			}
		}
		
		newTriplePatterns.addAll(triplePatterns);
		
		Query q = new Query();
		q.addProjectVars(query.getProjectVars());
		ElementTriplesBlock tripleBlock = new ElementTriplesBlock();
		for (Triple triple : newTriplePatterns) {
			tripleBlock.addTriple(triple);
		}
		ElementGroup eg = new ElementGroup();
		eg.addElement(tripleBlock);
		for (ElementFilter filter : filters) {
			eg.addElementFilter(filter);
		}
		q.setQuerySelectType();
		q.setDistinct(true);
		q.setQueryPattern(eg);
		
		return q;
	}
	
	private Query rewriteForVirtuosoDateLiteralBug(Query query){
		QueryUtils queryUtils = new QueryUtils();
		Set<Triple> triplePatterns = queryUtils.extractTriplePattern(query);
		
		Set<Triple> newTriplePatterns = new TreeSet<>(new Comparator<Triple>() {
			@Override
			public int compare(Triple o1, Triple o2) {
				return ComparisonChain.start().
				compare(o1.getSubject().toString(), o2.getSubject().toString()).
				compare(o1.getPredicate().toString(), o2.getPredicate().toString()).
				compare(o1.getObject().toString(), o2.getObject().toString()).
				result();
			}
		});
		List<ElementFilter> filters = new ArrayList<>();
		int cnt = 0;
		// <s p o>
		for (Iterator<Triple> iter = triplePatterns.iterator(); iter.hasNext();) {
			Triple tp = iter.next();
			if(tp.getObject().isLiteral()){
				RDFDatatype dt = tp.getObject().getLiteralDatatype();
				if(dt != null && dt instanceof XSDAbstractDateTimeType){
					iter.remove();
					// new triple pattern <s p ?var> 
					Node objectVar = NodeFactory.createVariable("date" + cnt++);
					newTriplePatterns.add(Triple.create(
							tp.getSubject(), 
							tp.getPredicate(),
							objectVar)
							);
					// add FILTER(STR(?var) = lexicalform(o))
					String lit = tp.getObject().getLiteralLexicalForm();
					Object literalValue = tp.getObject().getLiteralValue();
					Expr filterExpr = new E_Equals(
							new E_Str(new ExprVar(objectVar)), 
							NodeValue.makeString(lit));
					if(literalValue instanceof XSDDateTime){
						Calendar calendar = ((XSDDateTime) literalValue).asCalendar();
						Date date = new Date(calendar.getTimeInMillis() + TimeUnit.HOURS.toMillis(2));   
						SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");          
						String inActiveDate = format1.format(date);
						filterExpr = new E_LogicalOr(filterExpr, new E_Equals(
							new E_Str(new ExprVar(objectVar)), 
							NodeValue.makeString(inActiveDate)));
					}
					ElementFilter filter = new ElementFilter(filterExpr);
					filters.add(filter);
				}
			}
		}
		
		newTriplePatterns.addAll(triplePatterns);
		
		Query q = new Query();
		q.addProjectVars(query.getProjectVars());
		ElementTriplesBlock tripleBlock = new ElementTriplesBlock();
		for (Triple triple : newTriplePatterns) {
			tripleBlock.addTriple(triple);
		}
		ElementGroup eg = new ElementGroup();
		eg.addElement(tripleBlock);
		for (ElementFilter filter : filters) {
			eg.addElementFilter(filter);
		}
		q.setQuerySelectType();
		q.setDistinct(true);
		q.setQueryPattern(eg);
		
		return q;
	}
	
	private Score computeScore(String referenceSparqlQuery, RDFResourceTree tree) {
		// apply some filters
		QueryTreeUtils.removeVarLeafs(tree);
		QueryTreeUtils.prune(tree, null, Entailment.RDF);
		
		String learnedSPARQLQuery = QueryTreeUtils.toSPARQLQueryString(tree, dataset.getBaseIRI(), dataset.getPrefixMapping());
		
		if(QueryUtils.getTriplePatterns(QueryFactory.create(learnedSPARQLQuery)).size() < 30) {
			return computeScoreBySparqlCount(referenceSparqlQuery, learnedSPARQLQuery);
		}
		
		// get the reference resources
		List<String> referenceResources = getResult(referenceSparqlQuery);
		if (referenceResources.isEmpty()) {
			logger.error("Reference SPARQL query returns no result.\n" + referenceSparqlQuery);
			return new Score();
		}

		// if query is most general one P=|TARGET|/|KB| R=1
		if (learnedSPARQLQuery.equals(QueryTreeUtils.EMPTY_QUERY_TREE_QUERY)) {
			double precision = referenceResources.size() / (double) kbSize;
			double recall = 1.0;
			double fMeasure = Heuristics.getFScore(recall, precision);
			return new Score(precision, recall, fMeasure);
		}

		// get the learned resources
		List<String> learnedResources = splitComplexQueries ? getResultSplitted(learnedSPARQLQuery) : getResult(learnedSPARQLQuery);
		if (learnedResources.isEmpty()) {
			logger.error("Learned SPARQL query returns no result.\n" + learnedSPARQLQuery);
			System.err.println(learnedSPARQLQuery);
			return new Score();
		}

		// get the overlapping resources
		int overlap = Sets.intersection(Sets.newHashSet(referenceResources), Sets.newHashSet(learnedResources)).size();

		double precision = overlap / (double) learnedResources.size();
		double recall = overlap / (double) referenceResources.size();
		double fMeasure = Heuristics.getFScore(recall, precision);

		return new Score(precision, recall, fMeasure);
	}
	
	private Score computeScoreBySparqlCount(String referenceSparqlQuery, String learnedSPARQLQuery) {
		final ExprVar s = new ExprVar("s");
		Var cntVar = Var.alloc("cnt");
		
		// Q1
		Query q1 = QueryFactory.create(referenceSparqlQuery);
		Query q1Count = QueryFactory.create();
		q1Count.setQuerySelectType();
		q1Count.getProject().add(cntVar, new ExprAggregator(s.asVar(), new AggCountVarDistinct(s)));
		q1Count.setQueryPattern(q1.getQueryPattern());
		QueryExecution qe = qef.createQueryExecution(q1Count);
		ResultSet rs = qe.execSelect();
		QuerySolution qs = rs.next();
		int referenceCnt = qs.getLiteral(cntVar.getName()).getInt();
		qe.close();
				
		// if query is most general one P=|TARGET|/|KB| R=1
		if (learnedSPARQLQuery.equals(QueryTreeUtils.EMPTY_QUERY_TREE_QUERY)) {
			double precision = referenceCnt / (double) kbSize;
			double recall = 1.0;
			double fMeasure = Heuristics.getFScore(recall, precision);
			return new Score(precision, recall, fMeasure);
		}
				
		// Q2
		Query q2 = QueryFactory.create(learnedSPARQLQuery);
		Query q2Count = QueryFactory.create();
		q2Count.setQuerySelectType();
		q2Count.getProject().add(cntVar, new ExprAggregator(s.asVar(), new AggCountVarDistinct(s)));
		q2Count.setQueryPattern(q2.getQueryPattern());
		System.err.println(q2Count);
		qe = qef.createQueryExecution(q2Count);
		rs = qe.execSelect();
		qs = rs.next();
		int learnedCnt = qs.getLiteral(cntVar.getName()).getInt();
		qe.close();
		
		
		// Q1 ∪ Q2
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
		System.err.println(q12);
		qe = qef.createQueryExecution(q12);
		rs = qe.execSelect();
		qs = rs.next();
		int overlap = qs.getLiteral(cntVar.getName()).getInt();
		qe.close();
		
		double precision = overlap / (double) learnedCnt;
		double recall = overlap / (double) referenceCnt;
		double fMeasure = Heuristics.getFScore(recall, precision);

		return new Score(precision, recall, fMeasure);
	}
	
	public static void main(String[] args) throws Exception {
		ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
		Logger.getLogger(QTLEvaluation.class).addAppender(
				new FileAppender(new SimpleLayout(), "log/qtl-qald.log", false));
		Logger.getRootLogger().setLevel(Level.INFO);
		Logger.getLogger(QTL2Disjunctive.class).setLevel(Level.INFO);
		Logger.getLogger(QTLEvaluation.class).setLevel(Level.INFO);
		Logger.getLogger(QueryExecutionFactoryCacheEx.class).setLevel(Level.INFO);
		
		new QTLEvaluation(new DBpediaEvaluationDataset()).run();

//		new QALDExperiment(Dataset.BIOMEDICAL).run();
	}
	
	class Score {
		double precision, recall, fmeasure = 0;

		public Score() {}
		
		public Score(double precision, double recall, double fmeasure) {
			this.precision = precision;
			this.recall = recall;
			this.fmeasure = fmeasure;
		}
		
		public double getPrecision() {
			return precision;
		}
		
		public double getRecall() {
			return recall;
		}
		
		public double getFmeasure() {
			return fmeasure;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("P=%f\nR=%f\nF-score=%f", precision, recall, fmeasure);
		}
		
	}
	
	class NegativeExampleSPARQLQueryGenerator extends ElementVisitorBase{
		
		private boolean inOptionalClause;
		private Stack<ElementGroup> parentGroup = new Stack<>();
		private QueryUtils triplePatternExtractor = new QueryUtils();
		private Triple triple;
		Random randomGen = new Random(123);

		/**
		 * Returns a modified SPARQL query such that it is similar but different by choosing one of the triple patterns and use
		 * the negation of its existence.
		 * @param query
		 */
		public Query generateSPARQLQuery(Query query){
			//choose a random triple for the modification
			List<Triple> triplePatterns = new ArrayList<Triple>(triplePatternExtractor.extractTriplePattern(query));
			Collections.shuffle(triplePatterns, randomGen);
			triple = triplePatterns.get(0);
			
			Query modifiedQuery = query.cloneQuery();
			modifiedQuery.getQueryPattern().visit(this);
			logger.info("Negative examples query:\n" + modifiedQuery.toString());
			return modifiedQuery;
		}
		
		@Override
		public void visit(ElementGroup el) {
			parentGroup.push(el);
			for (Iterator<Element> iterator = new ArrayList<Element>(el.getElements()).iterator(); iterator.hasNext();) {
				Element e = iterator.next();
				e.visit(this);
			}
			parentGroup.pop();
		}

		@Override
		public void visit(ElementOptional el) {
			inOptionalClause = true;
			el.getOptionalElement().visit(this);
			inOptionalClause = false;
		}

		@Override
		public void visit(ElementTriplesBlock el) {
			for (Iterator<Triple> iterator = el.patternElts(); iterator.hasNext();) {
				Triple t = iterator.next();
				if(inOptionalClause){
					
				} else {
					if(t.equals(triple)){
						ElementGroup parent = parentGroup.peek();
						ElementTriplesBlock elementTriplesBlock = new ElementTriplesBlock();
						elementTriplesBlock.addTriple(t);
						ElementGroup eg = new ElementGroup();
						eg.addElement(elementTriplesBlock);
						parent.addElement(new ElementFilter(new E_NotExists(eg)));
						iterator.remove();
					}
				}
			}
		}

		@Override
		public void visit(ElementPathBlock el) {
			for (Iterator<TriplePath> iterator = el.patternElts(); iterator.hasNext();) {
				TriplePath tp = iterator.next();
				if(inOptionalClause){
					
				} else {
					if(tp.asTriple().equals(triple)){
						ElementGroup parent = parentGroup.peek();
						ElementPathBlock elementTriplesBlock = new ElementPathBlock();
						elementTriplesBlock.addTriple(tp);
						ElementGroup eg = new ElementGroup();
						eg.addElement(elementTriplesBlock);
						parent.addElement(new ElementFilter(new E_NotExists(eg)));
						iterator.remove();
					}
				}
			}
		}

		@Override
		public void visit(ElementUnion el) {
			for (Iterator<Element> iterator = el.getElements().iterator(); iterator.hasNext();) {
				Element e = iterator.next();
				e.visit(this);
			}
		}
		
		@Override
		public void visit(ElementFilter el) {
		}

	}

}
