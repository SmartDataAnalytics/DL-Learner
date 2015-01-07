/**
 * 
 */
package org.dllearner.algorithms.qtl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
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
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryImpl;
import org.dllearner.algorithms.qtl.operations.lgg.EvaluatedQueryTree;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.QueryExecutionFactoryHttp;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.utilities.QueryUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.google.common.base.Charsets;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
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
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.E_NotEquals;
import com.hp.hpl.jena.sparql.expr.E_NotExists;
import com.hp.hpl.jena.sparql.expr.E_Str;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementUnion;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author Lorenz Buehmann
 *
 */
public class QALDExperiment {
	
	private static final Logger logger = Logger.getLogger(QALDExperiment.class.getName());
	
	private static final ParameterizedSparqlString superClassesQueryTemplate = new ParameterizedSparqlString(
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> SELECT ?sup WHERE {?sub rdfs:subClassOf+ ?sup .}");
	
	enum Dataset {
		DBPEDIA, BIOMEDICAL
	}
	
	static Map<String, String> prefixes = new HashMap<String, String>();
	static {
		prefixes.put("sider", "http://www4.wiwiss.fu-berlin.de/sider/resource/sider/");
		prefixes.put("side_effects", "http://www4.wiwiss.fu-berlin.de/sider/resource/side_effects/");
		prefixes.put("drug", "http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/");
		prefixes.put("diseasome", "http://www4.wiwiss.fu-berlin.de/diseasome/resource/diseasome/");
		
		prefixes.put("dbo", "http://dbpedia.org/ontology/");
		prefixes.put("dbpedia", "http://dbpedia.org/resource/");
	}
	
	// DBpedia dataset related content
	static SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
	static {
		try {
			endpoint = new SparqlEndpoint(
					new URL("http://akswnc3.informatik.uni-leipzig.de:8860/sparql"), 
					"http://dbpedia.org");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	List<String> dbpediaQuestionFiles = Lists.newArrayList(
			"org/dllearner/algorithms/qtl/qald-4_multilingual_train.xml",
			"org/dllearner/algorithms/qtl/qald-4_multilingual_test.xml"
			);
	Set<String> allowedNamespaces = Sets.newHashSet("http://dbpedia.org/ontology/", "http://dbpedia.org/resource/");
	Set<String> ignoredProperties = Sets.newHashSet("http://dbpedia.org/ontology/wikiPageID","http://dbpedia.org/ontology/wikiPageRevisionID",
			"http://dbpedia.org/ontology/wikiPageExtracted", "http://dbpedia.org/ontology/wikiPageModified");
	
	// Biomedical dataset related content
	static SparqlEndpoint biomedicalEndpoint;
	static {
		try {
			biomedicalEndpoint = new SparqlEndpoint(
					new URL("http://akswnc3.informatik.uni-leipzig.de:8860/sparql"), 
					"http://biomedical.org");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	File localBiomedicalDataDir = new File(
			"/home/me/work/datasets/qald4/biomedical"
//			"/home/user/work/datasets/qald4/biomedical"
			);
	//http://greententacle.techfak.uni-bielefeld.de/~cunger/qald/4/
	List<String> biomedicalQuestionFiles = Lists.newArrayList(
			"org/dllearner/algorithms/qtl/qald-4_biomedical_train.xml",
			"org/dllearner/algorithms/qtl/qald-4_biomedical_test.xml"
			);
	
	
	List<String> questionFiles;
	QueryExecutionFactory qef;
	String cacheDirectory = "cache/qtl";
	
	int minNrOfPositiveExamples = 5;
	int maxDepth = 2;
	
	private QueryTreeFactory<String> queryTreeFactory;
	private ConciseBoundedDescriptionGenerator cbdGen;
	private QTL2 la;
	
	private Random rnd = new Random(123);

	private Dataset dataset;
	
	private Map<String, List<String>> cache = new HashMap<String, List<String>>();
	
	public QALDExperiment(Dataset dataset) {
		this.dataset = dataset;
		queryTreeFactory = new QueryTreeFactoryImpl();
		queryTreeFactory.setMaxDepth(maxDepth);
		
		if(dataset == Dataset.DBPEDIA){
			questionFiles = dbpediaQuestionFiles;
			
			qef = new QueryExecutionFactoryHttp(endpoint.getURL().toString(), endpoint.getDefaultGraphURIs());
			long timeToLive = TimeUnit.DAYS.toMillis(60);
			CacheFrontend cacheFrontend = CacheUtilsH2.createCacheFrontend(cacheDirectory, false, timeToLive);
			qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);
			
			// add some filters to avoid resources with namespaces like http://dbpedia.org/property/
			queryTreeFactory.addAllowedNamespaces(allowedNamespaces);
			queryTreeFactory.addIgnoredPropperties(ignoredProperties);
//			queryTreeFactory.setStatementFilter(new KeywordBasedStatementFilter(
//					Sets.newHashSet("bandleader", "play", "trumpet")));
		} else if(dataset == Dataset.BIOMEDICAL){
			questionFiles = biomedicalQuestionFiles;
			
			Model model = loadBiomedicalData();
			qef = new QueryExecutionFactoryModel(model);
		}
		
		cbdGen = new ConciseBoundedDescriptionGeneratorImpl(qef);
		cbdGen.setRecursionDepth(maxDepth);
		
		la = new QTL2();
	}
	
	private Model loadBiomedicalData(){
		logger.info("Loading QALD biomedical data from local directory " + localBiomedicalDataDir + " ...");
		Model model = ModelFactory.createDefaultModel();
		
		for (File file : localBiomedicalDataDir.listFiles()) {
			if(file.isFile() && file.getName().endsWith(".nt")){
				try(FileInputStream is = new FileInputStream(file)){
					model.read(is, null, "N-TRIPLES");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		logger.info("...done.");
		return model;
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
				0.4,
//				0.6
				};
			
		// loop of number of positive examples
		for (int nrOfExamples = minNrOfExamples; nrOfExamples < maxNrOfExamples; nrOfExamples = Math.min(nrOfExamples + stepSize, maxNrOfExamples)) {
			
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
				
				DescriptiveStatistics bestReturnedSolutionPrecisionStats = new DescriptiveStatistics();
				DescriptiveStatistics bestReturnedSolutionRecallStats = new DescriptiveStatistics();
				DescriptiveStatistics bestReturnedSolutionFMeasureStats = new DescriptiveStatistics();
				
				DescriptiveStatistics bestSolutionPrecisionStats = new DescriptiveStatistics();
				DescriptiveStatistics bestSolutionRecallStats = new DescriptiveStatistics();
				DescriptiveStatistics bestSolutionFMeasureStats = new DescriptiveStatistics();
				
				DescriptiveStatistics bestSolutionPositionStats = new DescriptiveStatistics();
				
				// loop over SPARQL queries
				for (String sparqlQuery : sparqlQueries) {
//					if(!sparqlQuery.contains("currency"))continue;
					logger.info("##############################################################");
					logger.info("Processing query\n" + sparqlQuery);
					// some queries can return less examples
					int possibleNrOfExamples = Math.min(getResultCount(sparqlQuery), nrOfExamples);
					
					try {
						// generate some random examples and add some noise if necessary
						Map<OWLIndividual, QueryTree<String>> generatedExamples = generateExamples(sparqlQuery, possibleNrOfExamples, noise);

						// run QTL
						PosNegLPStandard lp = new PosNegLPStandard();
						lp.setPositiveExamples(generatedExamples.keySet());
						
						//						lp.init();
//						la = new QTL2(lp, qef);
						QTL2Disjunctive la = new QTL2Disjunctive(lp, qef);
						la.setAllowedNamespaces(allowedNamespaces);
						la.setIgnoredPropperties(ignoredProperties);
						la.setTreeFactory(queryTreeFactory);
						la.setPositiveExampleTrees(generatedExamples);
						la.init();
						la.start();

						List<EvaluatedQueryTree<String>> solutions = new ArrayList<EvaluatedQueryTree<String>>(
								la.getSolutions());

						// the best solution by QTL
						EvaluatedQueryTree<String> bestSolution = solutions.get(0);
						logger.info("Got " + solutions.size() + " query trees.");
						logger.info("Best computed solution:\n" + bestSolution.asEvaluatedDescription());
						logger.info("Score:\n" + bestSolution.getTreeScore());

						// convert to SPARQL query
						String learnedSPARQLQuery = bestSolution.getTree().toSPARQLQueryString(true, false);

						double precision = precision(sparqlQuery, learnedSPARQLQuery);
						bestReturnedSolutionPrecisionStats.addValue(precision);

						double recall = recall(sparqlQuery, learnedSPARQLQuery);
						bestReturnedSolutionRecallStats.addValue(recall);

						double fmeasure = fMeasure(sparqlQuery, learnedSPARQLQuery);
						bestReturnedSolutionFMeasureStats.addValue(fmeasure);

						logger.info(String.format("P=%f\nR=%f\nF-score=%f", precision, recall, fmeasure));

						// find the extensionally best matching tree in the list
						EvaluatedQueryTree<String> bestMatchingTree = findBestMatchingTree(solutions, sparqlQuery);

						// position of best tree in list of solutions
						int position = solutions.indexOf(bestMatchingTree);
						bestSolutionPositionStats.addValue(position);

						if (position > 0) {
							logger.info("Position of best covering tree in list: " + position);
							logger.info("Best covering solution:\n" + bestMatchingTree.asEvaluatedDescription());
							logger.info("Tree score: " + bestMatchingTree.getTreeScore());
							String bestLearnedSPARQLQuery = bestMatchingTree.getTree().toSPARQLQueryString(true, false);
							precision = precision(sparqlQuery, bestLearnedSPARQLQuery);
							recall = recall(sparqlQuery, bestLearnedSPARQLQuery);
							fmeasure = fMeasure(sparqlQuery, bestLearnedSPARQLQuery);
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
		
	
	private EvaluatedQueryTree<String> findBestMatchingTree(Collection<EvaluatedQueryTree<String>> trees, String targetSPARQLQuery){
		logger.info("Finding best matching query tree...");
		//get the tree with the highest fmeasure
		EvaluatedQueryTree<String> bestTree = null;
		double bestFMeasure = -1;
		for (EvaluatedQueryTree<String> tree : trees) {
			String learnedSPARQLQuery = tree.getTree().toSPARQLQueryString();
//			logger.info(getPrefixedQuery(learnedSPARQLQuery));
//			logger.info("Tree Score: " + tree.getTreeScore());
			double fMeasure = fMeasure(targetSPARQLQuery, learnedSPARQLQuery);
			if(fMeasure == 1.0){
				return tree;
			}
			if(fMeasure > bestFMeasure){
				bestFMeasure = fMeasure;
				bestTree = tree;
			}
//			logger.info("Extensional fMeasure: " + fMeasure);
		}
		return bestTree;
	}
	
	private Map<OWLIndividual, QueryTree<String>> generateExamples(String sparqlQuery, int maxNrOfExamples, double noise){
		Random randomGen = new Random(123);
		
		// get all resources returned by the query
		List<String> resources = getResult(sparqlQuery);
		
		// pick some random positive examples from the list
		Collections.shuffle(resources, randomGen);
		List<String> examples = resources.subList(0, Math.min(maxNrOfExamples, resources.size()));
		logger.info("Pos. examples: " + examples);
		
		// if noise as not zero
		if(noise > 0) {
			// generate negative examples
			List<String> negativeExamples = new ArrayList<>(generateNegativeExamples(sparqlQuery));
			Collections.shuffle(negativeExamples, randomGen);
			
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
						String negExample = negativeExamples.remove(0);
						newExamples.add(negExample);
						logger.info("Replacing " + posExample + " by " + negExample);
					}
				}
				examples.addAll(newExamples);
			} else {
				// 2. way
				int nrOfPosExamples2Replace = (int) (noise * examples.size());
				logger.info("replacing " + nrOfPosExamples2Replace + "/" + examples.size() + " examples to introduce noise");
				List<String> posExamples2Replace = new ArrayList<>(examples.subList(0, nrOfPosExamples2Replace));
				examples.removeAll(posExamples2Replace);
				List<String> negExamples4Replacement = negativeExamples.subList(0, nrOfPosExamples2Replace);
				examples.addAll(negExamples4Replacement);
				logger.info("replaced " + posExamples2Replace + " by " + negExamples4Replacement);
			}
		}
		
		// build query trees
		Map<OWLIndividual, QueryTree<String>> queryTrees = new HashMap<>();
		for (String ex : examples) {
			QueryTree<String> queryTree = getQueryTree(ex);
			queryTrees.put(new OWLNamedIndividualImpl(IRI.create(ex)), queryTree);
		}
		
		// add noise by modifying the query trees
//		generateNoiseAttributeLevel(sparqlQuery, queryTrees, noise);
		
		return queryTrees;
	}
	
	private QueryTree<String> getSimilarTree(QueryTree<String> tree, String property, int maxTreeDepth){
		String query = "SELECT ?o WHERE {?s <" + property + "> ?o. FILTER(isURI(?o) && ?o != <" + tree.getUserObject() + ">)} LIMIT 1";
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		if(rs.hasNext()){
			String uri = rs.next().getResource("o").getURI();
			Model cbd = cbdGen.getConciseBoundedDescription(uri, maxTreeDepth);
			QueryTree<String> similarTree = queryTreeFactory.getQueryTree(uri, cbd);
			similarTree.setUserObject(uri);
			return similarTree;
		}
		return null;
	}
	
	private Set<String> generateNegativeExamples(String queryString){
		Query query = QueryFactory.create(queryString);
		
		// get the positive examples
		List<String> posExamples = getResult(queryString);
		
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
					result.removeAll(posExamples);
					
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
						result.removeAll(posExamples);
					}
					negExamples.addAll(result);
				}
			}
		}
		
		negExamples.removeAll(posExamples);
		if(negExamples.isEmpty()){
			logger.error("Found no negative example.");
			System.exit(0);
		}
		return negExamples;
	}
	
	private List<QueryTree<String>> getQueryTrees(List<String> resources){
		List<QueryTree<String>> trees = new ArrayList<QueryTree<String>>();
		
		for (String resource : resources) {
			trees.add(getQueryTree(resource));
		}
		
		return trees;
	}
	
	private QueryTree<String> getQueryTree(String resource){
		Model cbd = cbdGen.getConciseBoundedDescription(resource);
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
		
		QueryTree<String> tree = queryTreeFactory.getQueryTree(resource, cbd);
		return tree;
	}
	
	private List<String> getResult(String sparqlQuery){
//		logger.trace(sparqlQuery);
		List<String> resources = cache.get(sparqlQuery);
		if(resources == null) {
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
		
		// remove triple patterns with unbound object vars
		query = removeUnboundObjectVarTriples(query);
		
		Var targetVar = query.getProjectVars().get(0); // should be ?x0
		
		QueryUtils queryUtils = new QueryUtils();
		Set<Triple> triplePatterns = queryUtils.extractTriplePattern(query);
		
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
				
				//  Virtuoso bug workaround with literals of type xsd:float and xsd:double
				if(object.isLiteral() && object.getLiteralDatatype() != null 
						&& (object.getLiteralDatatype().equals(XSDDatatype.XSDfloat) || object.getLiteralDatatype().equals(XSDDatatype.XSDdouble))){
					continue;
				}
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
		int maxNrOfTriplePatternsPerQuery = 10;// number of outgoing triple patterns form the target var in each executed query
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
			Set<Triple> additionalTriples = new HashSet<Triple>();
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
			logger.trace(q);
			
//			sparqlQuery = getPrefixedQuery(sparqlQuery);
			Set<String> resourcesTmp = new HashSet<String>(getResult(q.toString()));
			
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
	
	private void filterOutGeneralTypes(Multimap<Var, Triple> var2Triples){
		for (Var var : var2Triples.keySet()) {
			Collection<Triple> triples = var2Triples.get(var);
			Set<Node> types2Remove = new HashSet<>();
			for (Triple triple : triples) {
				if(triple.getPredicate().matches(RDF.type.asNode()) && triple.getObject().isURI()){
					types2Remove.addAll(getSuperClasses(triple.getObject()));
				}
			}
			for (Iterator<Triple> iterator = triples.iterator(); iterator.hasNext();) {
				Triple triple = iterator.next();
				if(triple.getPredicate().matches(RDF.type.asNode()) && types2Remove.contains(triple.getObject())){
					iterator.remove();
				}
			}
		}
	}
	
	private Set<Node> getSuperClasses(Node cls){
		Set<Node> superClasses = new HashSet<Node>();
		
		superClassesQueryTemplate.setIri("sub", cls.getURI());
		
		String query = superClassesQueryTemplate.toString();
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		while(rs.hasNext()){
			QuerySolution qs = rs.next();
			superClasses.add(qs.getResource("sup").asNode());
		}
		qe.close();
		
		return superClasses;
	}
	
	private int getResultCount(String sparqlQuery){
		sparqlQuery = "PREFIX owl: <http://www.w3.org/2002/07/owl#> " + sparqlQuery;
		int cnt = 0;
		ResultSet rs = qef.createQueryExecution(sparqlQuery).execSelect();
		while(rs.hasNext()){
			rs.next();
			cnt++;
		}
		return cnt;
	}
	
	private List<String> loadSPARQLQueries(){
		List<String> queries = new ArrayList<String>();
		try {
			int cnt = 0;
			for(String file : questionFiles){
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
	            	String sparqlQuery = ((org.w3c.dom.Element)questionNode.getElementsByTagName("query").item(0)).getChildNodes().item(0).getNodeValue().trim();
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
	                	Set<Triple> ingoingTriplePatterns = triplePatternExtractor.extractIngoingTriplePatterns(q, projectVars.get(0).asNode());
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
		catch (DOMException e) {
	            e.printStackTrace();
	    }
		catch (ParserConfigurationException e) {
	            e.printStackTrace();
	    }
		catch (SAXException e) {
	            e.printStackTrace();
	    } 
		catch (IOException e) {
	            e.printStackTrace();
	    }
//		queries.clear();
//		queries.add("select ?uri where {?uri a <http://dbpedia.org/ontology/Book>. ?uri <http://dbpedia.org/ontology/author> <http://dbpedia.org/resource/Dan_Brown>.}");
		return queries;
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
	
	private String getPrefixedQuery(String sparqlQuery){
		for (Entry<String, String> entry : prefixes.entrySet()) {
			String prefix = entry.getKey();
			String namespace = entry.getValue();
			Pattern p = Pattern.compile("(<" + Pattern.quote(namespace) + "[.\\S]*>)");
		    Matcher m = p.matcher(sparqlQuery);
		    boolean matched = false;
		    while (m.find()){
		    	matched = true;
		    	String resource = m.group(1);
		    	String prefixedResource = resource.replace("<", "").replace(">", "");
		    	prefixedResource = prefixedResource.replace(namespace, prefix + ":");
		        sparqlQuery = sparqlQuery.replace(resource, prefixedResource);
		    }
		    if(matched){
		    	sparqlQuery = "PREFIX " + prefix + ": <" + namespace + "> \n" + sparqlQuery;
		    }
		}
		System.out.println(sparqlQuery); 
		return QueryFactory.create(sparqlQuery).toString(Syntax.syntaxSPARQL_11);
	}
	
	private double precision(String referenceSparqlQuery, String learnedSPARQLQuery) {
		// get the reference resources
		List<String> referenceResources = getResult(referenceSparqlQuery);
		if(referenceResources.isEmpty()){
			logger.error("Reference SPARQL query returns no result.\n" + referenceSparqlQuery);
			System.exit(0);
		}

		// get the learned resources
		List<String> learnedResources = getResultSplitted(learnedSPARQLQuery);
		if(learnedResources.isEmpty()){
			logger.error("Learned SPARQL query returns no result.\n" + learnedSPARQLQuery);
			System.exit(0);
		}

		// get the overlapping resources
		int overlap = Sets.intersection(
				Sets.newHashSet(referenceResources),
				Sets.newHashSet(learnedResources)).size();

		double precision = overlap / (double) learnedResources.size();

		return precision;
	}
	
	private double recall(String referenceSparqlQuery, String learnedSPARQLQuery) {
		// get the reference resources
		List<String> referenceResources = getResult(referenceSparqlQuery);

		// get the learned resources
		List<String> learnedResources = getResultSplitted(learnedSPARQLQuery);

		int overlap = Sets.intersection(
				Sets.newHashSet(referenceResources),
				Sets.newHashSet(learnedResources)).size();

		double recall = overlap / (double) referenceResources.size();

		return recall;
	}
	
	private double fMeasure(String referenceSparqlQuery, String learnedSPARQLQuery){
		double precision = precision(referenceSparqlQuery, learnedSPARQLQuery);
		
		double recall = recall(referenceSparqlQuery, learnedSPARQLQuery);
		
		double fMeasure = 0;
		if(precision + recall > 0){
			fMeasure = 2 * precision * recall / (precision + recall);
		}
		
		return fMeasure;
	}
	
	public static void main(String[] args) throws Exception {
//		ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
//		String ontologyURL = "file:/tmp/property_inference.owl";
//		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
//		OWLDataFactory dataFactory = man.getOWLDataFactory();
//		OWLOntology ontology = man.loadOntology(IRI.create(ontologyURL));
//		OWLReasonerFactory reasonerFactory = PelletReasonerFactory.getInstance();
//		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
//		for (OWLObjectProperty op : ontology.getObjectPropertiesInSignature()) {
//			System.out.println(op + ":" + reasoner.getEquivalentObjectProperties(op).getEntities());
//		}
//		
//		
		
		ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
		Logger.getLogger(QALDExperiment.class).addAppender(
				new FileAppender(new SimpleLayout(), "log/qtl-qald.log", false));
		Logger.getRootLogger().setLevel(Level.INFO);
		Logger.getLogger(QTL2.class).setLevel(Level.INFO);
		Logger.getLogger(QALDExperiment.class).setLevel(Level.INFO);
		Logger.getLogger(QueryExecutionFactoryCacheEx.class).setLevel(Level.INFO);
		
//		new QALDExperiment(Dataset.DBPEDIA).run();

		new QALDExperiment(Dataset.BIOMEDICAL).run();
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
