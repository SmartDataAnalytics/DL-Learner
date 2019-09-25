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
package org.dllearner.algorithms.qtl.qald;

import com.google.common.base.Charsets;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.datatypes.xsd.impl.XSDAbstractDateTimeType;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.*;
import org.apache.jena.sparql.syntax.*;
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
import org.dllearner.algorithms.qtl.util.*;
import org.dllearner.algorithms.qtl.util.filters.*;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.StringRenderer;
import org.dllearner.core.StringRenderer.Rendering;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.utilities.QueryUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Lorenz Buehmann
 *
 */
public class QALDExperiment {
	
	private static final Logger logger = Logger.getLogger(QALDExperiment.class.getName());
	
	private static final ParameterizedSparqlString superClassesQueryTemplate2 = new ParameterizedSparqlString(
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX owl: <http://www.w3.org/2002/07/owl#> "
			+ "SELECT ?sup WHERE {"
			+ "?sub ((rdfs:subClassOf|owl:equivalentClass)|^owl:equivalentClass)+ ?sup .}");
	
	private static final ParameterizedSparqlString superClassesQueryTemplate = new ParameterizedSparqlString(
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> PREFIX owl: <http://www.w3.org/2002/07/owl#> "
			+ "SELECT ?sup WHERE {"
			+ "?sub (rdfs:subClassOf|owl:equivalentClass)+ ?sup .}");
	
	enum Dataset {
		DBPEDIA, BIOMEDICAL
	}
	
	enum NoiseMethod {
		RANDOM, SIMILAR, SIMILARITY_PARAMETERIZED
	}
	
	NoiseMethod noiseMethod = NoiseMethod.RANDOM;
	
	static Map<String, String> prefixes = new HashMap<>();
	static {
		prefixes.put("sider", "http://www4.wiwiss.fu-berlin.de/sider/resource/sider/");
		prefixes.put("side_effects", "http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/");
		prefixes.put("drug", "http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/");
		prefixes.put("diseasome", "http://www4.wiwiss.fu-berlin.de/diseasome/resource/diseasome/");
		
		prefixes.put("dbo", "http://dbpedia.org/ontology/");
		prefixes.put("dbpedia", "http://dbpedia.org/resource/");
	}
	
	KB kb;
	
	List<String> questionFiles;
	QueryExecutionFactory qef;
	String cacheDirectory = "./cache/qtl";
	
	int minNrOfPositiveExamples = 5;
	int maxDepth = 2;
	
	private org.dllearner.algorithms.qtl.impl.QueryTreeFactory queryTreeFactory;
	private ConciseBoundedDescriptionGenerator cbdGen;
	
	RandomDataGenerator rnd = new RandomDataGenerator();

	private Dataset dataset;
	
	private Map<String, List<String>> cache = new HashMap<>();
	
	private int kbSize;

	private boolean splitComplexQueries = true;
	
	PredicateExistenceFilter filter = new PredicateExistenceFilterDBpedia(null);
	
	public QALDExperiment(KB dataset) {
		this.kb = dataset;
		
		queryTreeFactory = new QueryTreeFactoryBase();
		queryTreeFactory.setMaxDepth(maxDepth);
		
		if(KB.id.equals(DBpediaKB.id)){
			
			// add some filters to avoid resources with namespaces like http://dbpedia.org/property/
			queryTreeFactory.addDropFilters(
					new PredicateDropStatementFilter(StopURIsDBpedia.get()),
					new ObjectDropStatementFilter(StopURIsDBpedia.get()),
					new PredicateDropStatementFilter(StopURIsRDFS.get()),
					new PredicateDropStatementFilter(StopURIsOWL.get()),
					new ObjectDropStatementFilter(StopURIsOWL.get()),
					new PredicateDropStatementFilter(StopURIsSKOS.get()),
					new ObjectDropStatementFilter(StopURIsSKOS.get()),
					new NamespaceDropStatementFilter(
					Sets.newHashSet(
							"http://dbpedia.org/property/", 
							"http://purl.org/dc/terms/",
							"http://dbpedia.org/class/yago/"
//							,FOAF.getURI()
							)
							),
							new PredicateDropStatementFilter(
									Sets.newHashSet(
											"http://www.w3.org/2002/07/owl#equivalentClass", 
											"http://www.w3.org/2002/07/owl#disjointWith"))
			);
//			queryTreeFactory.setStatementFilter(new KeywordBasedStatementFilter(
//					Sets.newHashSet("bandleader", "play", "trumpet")));
		}
		qef = kb.ks.getQueryExecutionFactory();
		
		cbdGen = new ConciseBoundedDescriptionGeneratorImpl(qef);

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
	
	public void run(){
		
		List<String> sparqlQueries = loadSPARQLQueries();
		logger.info("Total number of queries: " + sparqlQueries.size());
		
		// parameters
		int minNrOfExamples = 3;
		int maxNrOfExamples = 10;
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
			for (double noise : noiseIntervals) {

				FileAppender appender = null;
				try {
					appender = new FileAppender(new SimpleLayout(), "log/qtl/qtl2-" + nrOfExamples + "-" + noise + ".log", false);
					Logger.getRootLogger().addAppender(appender);
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				logger.info("#examples: " + nrOfExamples + " noise: " + noise);

				DescriptiveStatistics bestReturnedSolutionPrecisionStats = new DescriptiveStatistics();
				DescriptiveStatistics bestReturnedSolutionRecallStats = new DescriptiveStatistics();
				DescriptiveStatistics bestReturnedSolutionFMeasureStats = new DescriptiveStatistics();

				DescriptiveStatistics bestSolutionPrecisionStats = new DescriptiveStatistics();
				DescriptiveStatistics bestSolutionRecallStats = new DescriptiveStatistics();
				DescriptiveStatistics bestSolutionFMeasureStats = new DescriptiveStatistics();

				DescriptiveStatistics bestSolutionPositionStats = new DescriptiveStatistics();

//				if(nrOfExamples != 7) continue;
				// loop over SPARQL queries
				for (String sparqlQuery : sparqlQueries) {
//					if(!sparqlQuery.contains("Nobel_Prize_in_Literature"))continue;
					logger.info("##############################################################");
					logger.info("Processing query\n" + sparqlQuery);
					// some queries can return less examples
					int possibleNrOfExamples = Math.min(getResultCount(sparqlQuery), nrOfExamples);

					try {
						// compute or load cached solutions
						List<EvaluatedRDFResourceTree> solutions = generateSolutions(sparqlQuery, possibleNrOfExamples, noise);

						// the best solution by QTL
						EvaluatedRDFResourceTree bestSolution = solutions.get(0);
						logger.info("Got " + solutions.size() + " query trees.");
						logger.info("Best computed solution:\n" + bestSolution.asEvaluatedDescription());
						logger.info("Score:\n" + bestSolution.getTreeScore());

						// convert to SPARQL query
						String learnedSPARQLQuery = QueryTreeUtils.toSPARQLQueryString(filter.apply(bestSolution.getTree()), kb.baseIRI, kb.prefixMapping);

						Score score = computeScore(sparqlQuery, learnedSPARQLQuery);

						// compute precision
						double precision = score.getPrecision();
						bestReturnedSolutionPrecisionStats.addValue(precision);

						// compute recall
						double recall = score.getRecall();
						bestReturnedSolutionRecallStats.addValue(recall);

						// compute F1-score
						double fmeasure = score.getFmeasure();
						bestReturnedSolutionFMeasureStats.addValue(fmeasure);

						logger.info(String.format("P=%f\nR=%f\nF-score=%f", precision, recall, fmeasure));

						// find the extensionally best matching tree in the list
						Pair<EvaluatedRDFResourceTree, Score> bestMatchingTreeWithScore = findBestMatchingTree(solutions, sparqlQuery);
						EvaluatedRDFResourceTree bestMatchingTree = bestMatchingTreeWithScore.getFirst();
						Score bestMatchingScore = bestMatchingTreeWithScore.getSecond();

						// position of best tree in list of solutions
						int position = solutions.indexOf(bestMatchingTree);
						bestSolutionPositionStats.addValue(position);

						if (position > 0) {
							logger.info("Position of best covering tree in list: " + position);
							logger.info("Best covering solution:\n" + bestMatchingTree.asEvaluatedDescription());
							logger.info("Tree score: " + bestMatchingTree.getTreeScore());
							String bestLearnedSPARQLQuery = QueryTreeUtils.toSPARQLQueryString(filter.apply(bestMatchingTree.getTree()), kb.baseIRI, kb.prefixMapping);
							precision = bestMatchingScore.getPrecision();
							recall = bestMatchingScore.getRecall();
							fmeasure = bestMatchingScore.getFmeasure();
							logger.info(String.format("P=%f\nR=%f\nF-score=%f", precision, recall, fmeasure));
						} else {
							logger.info("Best returned solution was also the best covering solution.");
						}
						bestSolutionRecallStats.addValue(recall);
						bestSolutionPrecisionStats.addValue(precision);
						bestSolutionFMeasureStats.addValue(fmeasure);

					} catch (Exception e) {
						logger.error("Error occured.", e);
						System.exit(0);
					}
				}

				Logger.getRootLogger().removeAppender(appender);

				String result = "";
				result += "\nOverall Precision:\n" + bestReturnedSolutionPrecisionStats;
				result += "\nOverall Recall:\n" + bestReturnedSolutionRecallStats;
				result += "\nOverall FMeasure:\n" + bestReturnedSolutionFMeasureStats;
				result += "\nPositions of best solution:\n" + Arrays.toString(bestSolutionPositionStats.getValues());
				result += "\nPosition of best solution stats:\n" + bestSolutionPositionStats;

				result += "\nOverall Precision of best solution:\n" + bestSolutionPrecisionStats;
				result += "\nOverall Recall of best solution:\n" + bestSolutionRecallStats;
				result += "\nOverall FMeasure of best solution:\n" + bestSolutionFMeasureStats;

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
		Map<OWLIndividual, RDFResourceTree> generatedExamples = generateExamples(sparqlQuery, possibleNrOfExamples, noise);

		
		
		// run QTL
		PosNegLPStandard lp = new PosNegLPStandard();
		lp.setPositiveExamples(generatedExamples.keySet());
//		lp.init();

		QTL2Disjunctive la = new QTL2Disjunctive(lp, qef);
		la.setReasoner(kb.reasoner);
		la.setEntailment(Entailment.RDFS);
		la.setTreeFactory(queryTreeFactory);
		la.setPositiveExampleTrees(generatedExamples);
		la.setNoise(noise);
		la.init();
		la.start();

		List<EvaluatedRDFResourceTree> solutions = new ArrayList<>(la.getSolutions());
		
		return solutions;
	}
	
	private Pair<EvaluatedRDFResourceTree, Score> findBestMatchingTree(Collection<EvaluatedRDFResourceTree> trees, String targetSPARQLQuery){
		logger.info("Finding best matching query tree...");
		//get the tree with the highest fmeasure
		EvaluatedRDFResourceTree bestTree = null;
		Score bestScore = null;
		double bestFMeasure = -1;
		
		for (EvaluatedRDFResourceTree evalutedTree : trees) {
			RDFResourceTree tree = evalutedTree.getTree();
			
			// apply predicate existence filter
			tree = filter.apply(tree);
			String learnedSPARQLQuery = QueryTreeUtils.toSPARQLQueryString(tree, kb.baseIRI, kb.prefixMapping);
			System.out.println(learnedSPARQLQuery);
			// compute score
			Score score = computeScore(targetSPARQLQuery, learnedSPARQLQuery);
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
	
	private Map<OWLIndividual, RDFResourceTree> generateExamples(String sparqlQuery, int maxNrOfExamples, double noise){
		Random randomGen = new Random(123);
		
		// get all resources returned by the query
		List<String> resources = getResult(sparqlQuery);
		
		// pick some random positive examples from the list
		Collections.shuffle(resources, randomGen);
		List<String> examples = resources.subList(0, Math.min(maxNrOfExamples, resources.size()));
		logger.info("Pos. examples: " + examples);
		
		// add noise if enabled
		if(noise > 0) {
			generateNoise(examples, sparqlQuery, noise, randomGen);
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
			RDFResourceTree similarTree = queryTreeFactory.getQueryTree(object, cbd, maxTreeDepth);
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
			List<String> newExamples = new ArrayList<>();
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
		
		Set<String> negExamples = new HashSet<>();
		
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
			Set<Set<Triple>> powerSet = new TreeSet<>((o1, o2) -> ComparisonChain.start()
                    .compare(o1.size(), o2.size())
                    .compare(o1.hashCode(), o2.hashCode())
                    .result());
			powerSet.addAll(Sets.powerSet(triplePatterns));
			
			for (Set<Triple> set : powerSet) {
				if(!set.isEmpty() && set.size() != triplePatterns.size()){
					List<Triple> existingTriplePatterns = new ArrayList<>(triplePatterns);
					List<Triple> newTriplePatterns = new ArrayList<>();
					List<ElementFilter> filters = new ArrayList<>();
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
					List<Triple> allTriplePatterns = new ArrayList<>(existingTriplePatterns);
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
		List<RDFResourceTree> trees = new ArrayList<>();
		
		for (String resource : resources) {
			trees.add(getQueryTree(resource));
		}
		
		return trees;
	}
	
	private RDFResourceTree getQueryTree(String resource){
		Model cbd = cbdGen.getConciseBoundedDescription(resource, maxDepth);
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
		
		RDFResourceTree tree = queryTreeFactory.getQueryTree(resource, cbd);
		return tree;
	}
	
	private List<String> getResult(String sparqlQuery){
		logger.trace(sparqlQuery);
		List<String> resources = cache.get(sparqlQuery);
		if(resources == null) {
			resources = new ArrayList<>();
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
		Set<Triple> fixedTriplePatterns = new HashSet<>();
		Set<Set<Triple>> clusters = new HashSet<>();
		Collection<Triple> targetVarTriplePatterns = var2TriplePatterns.get(targetVar);
		boolean useSplitting = false;
		for (Triple tp : targetVarTriplePatterns) {
			Node object = tp.getObject();
			if(object.isConcrete() || !var2TriplePatterns.containsKey(Var.alloc(object))){
				fixedTriplePatterns.add(tp);
			} else {
				Set<Triple> cluster = new TreeSet<>((o1, o2) -> ComparisonChain.start().
                compare(o1.getSubject().toString(), o2.getSubject().toString()).
                compare(o1.getPredicate().toString(), o2.getPredicate().toString()).
                compare(o1.getObject().toString(), o2.getObject().toString()).
                result());
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
		Set<Set<Triple>> newClusters = new HashSet<>();
		for (Set<Triple> cluster : clusters) {
			int cnt = 0;
			for (Triple triple : cluster) {
				if(triple.getSubject().matches(targetVar)) {
					cnt++;
				}
			}
			
			if(cnt > maxNrOfTriplePatternsPerQuery) {
				Set<Triple> newCluster = new HashSet<>();
				for (Triple triple : cluster) {
					if(triple.getSubject().matches(targetVar)) {
						newCluster.add(triple);
					}
					if(newCluster.size() == maxNrOfTriplePatternsPerQuery) {
						newClusters.add(newCluster);
						newCluster = new HashSet<>();
					}
				}
				if(!newCluster.isEmpty()) {
					newClusters.add(newCluster);
				}
			}
		}
		for (Set<Triple> cluster : newClusters) {
			Set<Triple> additionalTriples = new HashSet<>();
			for (Triple triple : cluster) {
				if(triple.getObject().isVariable()){
					additionalTriples.addAll(var2TriplePatterns.get(Var.alloc(triple.getObject())));
				}
			}
			cluster.addAll(additionalTriples);
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
			Set<String> resourcesTmp = new HashSet<>(partialResult);
			
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
		
		return new ArrayList<>(resources);
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
			Collection<Triple> triplesPatterns2Remove = new HashSet<>();

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
		Set<Node> superClasses = new HashSet<>();
		
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
	
	private List<String> loadSPARQLQueries(){
		List<String> queries = new ArrayList<>();
		try {
			int cnt = 0;
			for(String file : kb.questionFiles){
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	            DocumentBuilder db = dbf.newDocumentBuilder();
	            Document doc = db.parse(this.getClass().getClassLoader().getResourceAsStream(file));
	            doc.getDocumentElement().normalize();
	            NodeList questionNodes = doc.getElementsByTagName("question");
	            
	            QueryUtils triplePatternExtractor = new QueryUtils();
	            
	            for( int i = 0; i < questionNodes.getLength(); i++){
	                
	            	org.w3c.dom.Element questionNode = (org.w3c.dom.Element) questionNodes.item(i);
	                
	            	int id = Integer.valueOf(questionNode.getAttribute("id")); //if(id != 44)continue;
//	            	String answerType = questionNode.getAttribute("answerType");
	            	boolean aggregation = false;//Boolean.valueOf(questionNode.getAttribute("aggregation"));
	            	boolean onlydbo = Boolean.valueOf(questionNode.getAttribute("onlydbo"));
	            	
	            	
	            	
	                // Read SPARQL query
	            	String sparqlQuery = questionNode.getElementsByTagName("query").item(0).getChildNodes().item(0).getNodeValue().trim();
	            	sparqlQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + sparqlQuery;
	            	if(sparqlQuery.contains("OPTIONAL {?uri rdfs:label ?string . FILTER (lang(?string) = 'en') }")){
	            		sparqlQuery = sparqlQuery.replace("OPTIONAL {?uri rdfs:label ?string . FILTER (lang(?string) = 'en') }", "");
	            		sparqlQuery = sparqlQuery.replace("FILTER (lang(?string) = 'en')", "");
	            		sparqlQuery = sparqlQuery.replace("?string", "");
	            	}
	            	if(sparqlQuery.contains("OPTIONAL {?uri rdfs:label ?string. FILTER (lang(?string) = 'en') }")){
	            		sparqlQuery = sparqlQuery.replace("OPTIONAL {?uri rdfs:label ?string. FILTER (lang(?string) = 'en') }", "");
	            		sparqlQuery = sparqlQuery.replace("?string", "");
	            	}
	            
//	            	System.out.println(sparqlQuery);
	            	// check if OUT OF SCOPE marked
	            	boolean outOfScope = sparqlQuery.toUpperCase().contains("OUT OF SCOPE");
	            	
	            	//check if ASK query
	            	boolean askQuery = sparqlQuery.toUpperCase().contains("ASK");
	            	
	            	boolean containsLimit = sparqlQuery.toUpperCase().contains("LIMIT");
	            	
	            	boolean containsCount = sparqlQuery.toUpperCase().contains("COUNT");
	            	
	            	boolean containsFilter = sparqlQuery.toUpperCase().contains("FILTER");
	            	
	            	boolean containsUNION = sparqlQuery.toUpperCase().contains("UNION");
	            	
	            	boolean needsSPARQL11 = sparqlQuery.toUpperCase().contains("MINUS") || 
	            			sparqlQuery.toUpperCase().contains("EXISTS");
	            	
	            	boolean singleProjectionVariable = true;
	            	
	            	//check if projection variable is somewhere in object position
	            	boolean hasIngoingLinks = false;
	            	if(!outOfScope && !askQuery && !containsLimit && !containsFilter){
	            		Query q = QueryFactory.create("prefix owl:<http://www.w3.org/2002/07/owl#> " + sparqlQuery, Syntax.syntaxARQ);
	                	
	            		singleProjectionVariable = q.getProjectVars().size() == 1;
	                	List<Var> projectVars = q.getProjectVars();
	                	Set<Triple> ingoingTriplePatterns = triplePatternExtractor.extractIncomingTriplePatterns(q, projectVars.get(0).asNode());
	                	hasIngoingLinks = !ingoingTriplePatterns.isEmpty();
	            	}
//	            	if(!hasIngoingLinks){
//	            		System.out.println(sparqlQuery);
//	            		cnt++;
//	            	}
	            	if(true
	            			&& !needsSPARQL11
	            			&& !aggregation 
	            			&& !outOfScope 
	            			&& !containsCount 
	            			&& !askQuery 
	            			&& singleProjectionVariable
	            			&& !hasIngoingLinks 
	            			&& !containsFilter
//	            			&& !containsUNION
	            			&& (dataset != Dataset.DBPEDIA || onlydbo)
	            			
	            			&& (getResultCount(sparqlQuery) >= minNrOfPositiveExamples)
	            			){
	            		queries.add(sparqlQuery);
	            	}
	            }
			}
            
		} 
		catch (DOMException | IOException | SAXException | ParserConfigurationException e) {
	            e.printStackTrace();
	    }
//		queries.clear();
//		queries.add("select ?uri where {?uri a <http://dbpedia.org/ontology/Book>. ?uri <http://dbpedia.org/ontology/author> <http://dbpedia.org/resource/Dan_Brown>.}");
		return queries;
	}
	
	private Query rewriteForVirtuosoFloatingPointIssue(Query query){
		QueryUtils queryUtils = new QueryUtils();
		Set<Triple> triplePatterns = queryUtils.extractTriplePattern(query);
		
		Set<Triple> newTriplePatterns = new TreeSet<>((o1, o2) -> ComparisonChain.start().
        compare(o1.getSubject().toString(), o2.getSubject().toString()).
        compare(o1.getPredicate().toString(), o2.getPredicate().toString()).
        compare(o1.getObject().toString(), o2.getObject().toString()).
        result());
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
		
		Set<Triple> newTriplePatterns = new TreeSet<>((o1, o2) -> ComparisonChain.start().
        compare(o1.getSubject().toString(), o2.getSubject().toString()).
        compare(o1.getPredicate().toString(), o2.getPredicate().toString()).
        compare(o1.getObject().toString(), o2.getObject().toString()).
        result());
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
	
	private Score computeScore(String referenceSparqlQuery, String learnedSPARQLQuery) {
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
	
	public static void main(String[] args) throws Exception {
		StringRenderer.setRenderer(Rendering.DL_SYNTAX);
		Logger.getLogger(QALDExperiment.class).addAppender(
				new FileAppender(new SimpleLayout(), "log/qtl-qald.log", false));
		Logger.getRootLogger().setLevel(Level.INFO);
		Logger.getLogger(QTL2Disjunctive.class).setLevel(Level.INFO);
		Logger.getLogger(QALDExperiment.class).setLevel(Level.INFO);
		Logger.getLogger(QueryExecutionFactoryCacheEx.class).setLevel(Level.INFO);
		
		new QALDExperiment(new DBpediaKB()).run();

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
			List<Triple> triplePatterns = new ArrayList<>(triplePatternExtractor.extractTriplePattern(query));
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
			for (Element e : new ArrayList<>(el.getElements())) {
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
			for (Element e : el.getElements()) {
				e.visit(this);
			}
		}
		
		@Override
		public void visit(ElementFilter el) {
		}

	}

}
