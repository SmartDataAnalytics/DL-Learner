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
package org.dllearner.algorithms.qtl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import com.jamonapi.MonitorFactory;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.dllearner.algorithms.qtl.datastructures.impl.EvaluatedRDFResourceTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.LiteralNodeConversionStrategy;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.LiteralNodeSubsumptionStrategy;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.heuristics.QueryTreeHeuristic;
import org.dllearner.algorithms.qtl.heuristics.QueryTreeHeuristicSimple;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBase;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGeneratorRDFS;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGeneratorSimple;
import org.dllearner.algorithms.qtl.util.Entailment;
import org.dllearner.core.*;
import org.dllearner.core.StringRenderer.Rendering;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.SparqlEndpointKS;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.QueryTreeScore;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

@ComponentAnn(name="query tree learner with noise (disjunctive)", shortName="qtl2dis", version=0.8)
public class QTL2Disjunctive extends AbstractCELA implements Cloneable{
	
	private static final Logger logger = LoggerFactory.getLogger(QTL2Disjunctive.class);
	private final DecimalFormat dFormat = new DecimalFormat("0.00");
	
	private SparqlEndpointKS ks;
	
//	private LGGGenerator2 lggGenerator = new LGGGeneratorSimple();
	private LGGGenerator lggGenerator;
	
	private QueryTreeFactory treeFactory;
	private ConciseBoundedDescriptionGenerator cbdGen;
	
	private Queue<EvaluatedRDFResourceTree> todoList;
	private SortedSet<EvaluatedRDFResourceTree> currentPartialSolutions;
	
	private double bestCurrentScore = 0d;
	
	private List<RDFResourceTree> currentPosExampleTrees = new ArrayList<>();
	private List<RDFResourceTree> currentNegExampleTrees = new ArrayList<>();
	private Set<OWLIndividual> currentPosExamples = new HashSet<>();
	private Set<OWLIndividual> currentNegExamples = new HashSet<>();
	
	private BiMap<RDFResourceTree, OWLIndividual> tree2Individual = HashBiMap.create();

	private PosNegLP lp;

	private Model model;

	private volatile boolean stop;
	private boolean isRunning;

	private List<EvaluatedRDFResourceTree> partialSolutions;
	
	private EvaluatedDescription<? extends Score> currentBestSolution;
	
	private QueryTreeHeuristic heuristic;
	
	//Parameters
	@ConfigOption(defaultValue="0.0", description="the (approximated) percentage of noise within the examples")
	private double noisePercentage = 0.0;
	
	private double coverageWeight = 0.8;
	private double specifityWeight = 0.1;
	
	private double minCoveredPosExamplesFraction = 0.2;
	
	// maximum execution time to compute a part of the solution
	private double maxTreeComputationTimeInSeconds = 10;
	
	@ConfigOption(defaultValue = "1", description = "how important it is not to cover negatives")
	private double beta = 1;
	
	// minimum score a query tree must have to be part of the solution
	private double minimumTreeScore = 0.3;
	
	// If TRUE the algorithm tries to cover all positive examples. Note that
	// while this improves accuracy on the testing set,
	// it may lead to overfitting
	private boolean tryFullCoverage;

	// algorithm will terminate immediately when a correct definition is found
	private boolean stopOnFirstDefinition;
	
	// the (approximated) value of noise within the examples
	private double noise = 0.0;
	
	private long partialSolutionStartTime;
	
	private double startPosExamplesSize;
	private int expressionTests = 0;
	
	// the time needed until the best solution was found
	private long timeBestSolutionFound = -1;
	
	LiteralNodeConversionStrategy[] strategies = new LiteralNodeConversionStrategy[]{
			LiteralNodeConversionStrategy.MIN,
			LiteralNodeConversionStrategy.MAX,
			LiteralNodeConversionStrategy.MIN_MAX,
			LiteralNodeConversionStrategy.DATATYPE
	};
	private QueryExecutionFactory qef;
	
	private Entailment entailment = Entailment.SIMPLE;
	
	private int maxTreeDepth = 2;
	
	private boolean useDisjunction = false;
	
	public QTL2Disjunctive() {}
	
	public QTL2Disjunctive(PosNegLP learningProblem, AbstractReasonerComponent reasoner) {
		super(learningProblem, reasoner);
		loadModel();
	}
	
	public QTL2Disjunctive(PosNegLP lp, QueryExecutionFactory qef) {
		super.learningProblem = lp;
		this.lp = lp;
		this.qef = qef;
	}
	
	public QTL2Disjunctive(PosNegLP lp, SparqlEndpointKS ks) {
		this(lp, ks.getQueryExecutionFactory());
	}
	
//	public QTL2Disjunctive(PosNegLP lp, Model model) {
//		this.learningProblem = lp;
//		this.model = model;
//	}
	
	/**
	 * Copy constructor.
	 * @param qtl the QTL2Disjunctive instance
	 */
	public QTL2Disjunctive(QTL2Disjunctive qtl) {
		super(qtl.getLearningProblem(), qtl.getReasoner());
		this.model = ModelFactory.createDefaultModel();
		this.model.add(qtl.model);
		this.beta = qtl.beta;
		this.maxExecutionTimeInSeconds = qtl.maxExecutionTimeInSeconds;
		this.maxTreeComputationTimeInSeconds = qtl.maxTreeComputationTimeInSeconds;
		this.tryFullCoverage = qtl.tryFullCoverage;
		this.stopOnFirstDefinition = qtl.stopOnFirstDefinition;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() throws ComponentInitException {
		logger.info("Initializing...");
		if(!(learningProblem instanceof PosNegLP)){
			throw new IllegalArgumentException("Only PosNeg learning problems are supported");
		}
		lp = (PosNegLP) learningProblem;
		
		// get query execution factory from KS
		if(qef == null) {
			qef = ks.getQueryExecutionFactory();
		}
		
		if(treeFactory == null) {
			treeFactory = new QueryTreeFactoryBase();
		}
		cbdGen = new ConciseBoundedDescriptionGeneratorImpl(qef);

		// set the used heuristic
		if(heuristic == null){
			heuristic = new QueryTreeHeuristicSimple();
			heuristic.setPosExamplesWeight(beta);
		}
		
		if(entailment == Entailment.SIMPLE) {
			lggGenerator = new LGGGeneratorSimple();
		} else if(entailment == Entailment.RDFS){
			lggGenerator = new LGGGeneratorRDFS(reasoner);
		}
		
		// generate the query trees
		generateQueryTrees();
		
		startPosExamplesSize = currentPosExampleTrees.size();
		
		//console rendering of class expressions
		StringRenderer.setRenderer(Rendering.MANCHESTER_SYNTAX);
		StringRenderer.setShortFormProvider(new SimpleShortFormProvider());
		
		//compute the LGG for all examples
		//this allows us to prune all other trees because we can omit paths in trees which are contained in all positive
		//as well as negative examples
//		List<RDFResourceTree> allExamplesTrees = new ArrayList<RDFResourceTree>();
//		allExamplesTrees.addAll(currentPosExampleTrees);
//		allExamplesTrees.addAll(currentNegExampleTrees);
//		RDFResourceTree lgg = lggGenerator.getLGG(allExamplesTrees);
//		lgg.dump();
		
		initialized = true;
		logger.info("...initialization finished.");
	}
	
	/**
	 * @param entailment the entailment to set
	 */
	public void setEntailment(Entailment entailment) {
		this.entailment = entailment;
	}
	
	private void generateQueryTrees(){
		logger.info("Generating trees...");
		RDFResourceTree queryTree;
		
		// positive examples
		if(currentPosExampleTrees.isEmpty()){
			for (OWLIndividual ind : lp.getPositiveExamples()) {
				try {
					Model cbd = cbdGen.getConciseBoundedDescription(ind.toStringID(), maxTreeDepth);
//					cbd.write(new FileOutputStream("/tmp/dbpedia-" + ind.toStringID().substring(ind.toStringID().lastIndexOf('/') + 1) + ".ttl"), "TURTLE", null);
					queryTree = treeFactory.getQueryTree(ind.toStringID(), cbd, maxTreeDepth);
					tree2Individual.put(queryTree, ind);
					currentPosExampleTrees.add(queryTree);
					currentPosExamples.add(ind);
					logger.debug(ind.toStringID());
					logger.debug(queryTree.getStringRepresentation());
				} catch (Exception e) {
					logger.error("Failed to generate tree for resource " + ind, e);
					throw new RuntimeException();
				}
			}
		}
		
		// negative examples
		if(currentNegExampleTrees.isEmpty()){
			for (OWLIndividual ind : lp.getNegativeExamples()) {
				try {
					Model cbd = cbdGen.getConciseBoundedDescription(ind.toStringID(), maxTreeDepth);
					queryTree = treeFactory.getQueryTree(ind.toStringID(), cbd, maxTreeDepth);
					tree2Individual.put(queryTree, ind);
					currentNegExampleTrees.add(queryTree);
					currentNegExamples.add(ind);
					logger.debug(ind.toStringID());
					logger.debug(queryTree.getStringRepresentation());
				} catch (Exception e) {
					logger.error("Failed to generate tree for resource " + ind, e);
					throw new RuntimeException();
				}
			}
		}
		logger.info("...done.");
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningAlgorithm#start()
	 */
	@Override
	public void start() {

		if(currentPosExampleTrees.isEmpty()) {
			logger.info("No positive examples given!");
			return;
		}

		printSetup();
		logger.info("Running...");
		
		reset();
		
		int i = 1;
		while(!terminationCriteriaSatisfied() && (useDisjunction || i == 1)){
			logger.info(i++ + ". iteration...");
			logger.info("#Remaining pos. examples:" + currentPosExampleTrees.size());
			logger.info("#Remaining neg. examples:" + currentNegExampleTrees.size());
			
			// compute best (partial) solution computed so far
			EvaluatedRDFResourceTree bestPartialSolution = computeBestPartialSolution();
			
			// add to set of partial solutions if criteria are satisfied
			if(bestPartialSolution.getScore() >= minimumTreeScore){
				
				partialSolutions.add(bestPartialSolution);
				
				// remove all examples covered by current partial solution
				RDFResourceTree tree;
				for (Iterator<RDFResourceTree> iterator = currentPosExampleTrees.iterator(); iterator.hasNext();) {
					tree = iterator.next();
					if(!bestPartialSolution.getFalseNegatives().contains(tree)){//a pos tree that is not covered
						iterator.remove();
						currentPosExamples.remove(tree2Individual.get(tree));
					}
				}
				for (Iterator<RDFResourceTree> iterator = currentNegExampleTrees.iterator(); iterator.hasNext();) {
					tree = iterator.next();
					if(bestPartialSolution.getFalsePositives().contains(tree)){//a neg example that is covered
						iterator.remove();
						currentNegExamples.remove(tree2Individual.get(tree));
					}
				}
				
				// (re)build the current combined solution from all partial solutions
				currentBestSolution = buildCombinedSolution();
				
				logger.info("combined accuracy: " + dFormat.format(currentBestSolution.getAccuracy()));
			} else {
				String message = "No partial tree found which satisfies the minimal criteria.";
				if(currentBestSolution != null) {
					message += "- the best was: "
							+ currentBestSolution.getDescription()
							+ " with score " + currentBestSolution.getScore();
				}
				logger.info(message);
			}
			
		}

		isRunning = false;
		
		postProcess();
		
		long nanoEndTime = System.nanoTime();
		logger.info("Finished in " + TimeUnit.NANOSECONDS.toMillis(nanoEndTime - nanoStartTime) + "ms.");
		logger.info(expressionTests +" descriptions tested");
		if(currentBestSolution != null) {
			logger.info("Combined solution:" + currentBestSolution.getDescription().toString().replace("\n", ""));
			logger.info(currentBestSolution.getScore().toString());
		} else {
			logger.info("Could not find a solution in the given time.");
		}
	}
	
	/**
	 * This method can be called for clean up of the solutions and re-ranking.
	 */
	private void postProcess() {
		logger.trace("Post processing ...");
		// pick solutions with same accuracy, i.e. in the pos only case
		// covering the same number of positive examples
		SortedSet<EvaluatedRDFResourceTree> solutions = getSolutions();
		// pick solutions with accuracy above
		// mas(maximum achievable score) - noise
		List<EvaluatedRDFResourceTree> solutionsForPostProcessing = new ArrayList<>();
		for (EvaluatedRDFResourceTree solution : solutions) {
			
			double accuracy = solution.getTreeScore().getAccuracy();
			
			double mas = heuristic.getMaximumAchievableScore(solution);
			
			double epsilon = 0.01;
			
			if(accuracy != mas && accuracy >= (mas - noise - epsilon)) {
				solutionsForPostProcessing.add(solution);
			}
		}
		
		logger.trace("Finished post processing.");
	}
	
	/**
	 * Compute a (partial) solution that covers as much positive examples as possible.
	 * @return a (partial) solution
	 */
	private EvaluatedRDFResourceTree computeBestPartialSolution(){
		logger.info("Computing best partial solution...");
		bestCurrentScore = Double.NEGATIVE_INFINITY;
		partialSolutionStartTime = System.currentTimeMillis();
		initTodoList(currentPosExampleTrees, currentNegExampleTrees);
		
		EvaluatedRDFResourceTree bestPartialSolutionTree = null;
		EvaluatedRDFResourceTree currentElement;
		RDFResourceTree currentTree;
		
		// generate id for each pos and neg example tree
		TObjectIntMap<RDFResourceTree> index = new TObjectIntHashMap<>(this.currentPosExampleTrees.size() + this.currentNegExampleTrees.size());
		int id = 1;
		for (RDFResourceTree posTree : currentPosExampleTrees) {
			index.put(posTree, id++);
		}
		Set<Set<RDFResourceTree>> processedCombinations = new HashSet<>();
		
		while(!partialSolutionTerminationCriteriaSatisfied()){
			logger.trace("ToDo list size: " + todoList.size());
			// pick best element from todo list
			currentElement = todoList.poll();
			currentTree = currentElement.getTree();
			
			logger.trace("Next tree: {} ({})", currentElement.getBaseQueryTrees(), currentElement.getTreeScore());
			
			// generate the LGG between the chosen tree and each false negative resp. uncovered positive example
			Collection<RDFResourceTree> falseNegatives = currentElement.getFalseNegatives();
			
			if(falseNegatives.isEmpty()) { // if the current solution covers already all pos examples
//				addToSolutions(bestPartialSolutionTree);
//				bestPartialSolutionTree = currentElement;
			}
			
			Iterator<RDFResourceTree> it = falseNegatives.iterator();
			while (it.hasNext() && !(useDisjunction && isPartialSolutionTimeExpired()) && !isTimeExpired()) {
				RDFResourceTree uncoveredTree = it.next();
				logger.trace("Uncovered tree: "  + uncoveredTree);
				
				// we should avoid the computation of lgg(t2,t1) if we already did lgg(t1,t2)
				Set<RDFResourceTree> baseQueryTrees = Sets.newTreeSet(currentElement.getBaseQueryTrees());
				baseQueryTrees.add(uncoveredTree);
//				String s = "";
//				for (RDFResourceTree queryTree : baseQueryTrees) {
//					s += index.get(queryTree) + ",";
//				}
//				System.err.println(s);
				if(!processedCombinations.add(baseQueryTrees)) {
//					System.err.println("skipping");
//					continue;
				}
				
				// compute the LGG
				MonitorFactory.getTimeMonitor("lgg").start();
				((LGGGeneratorSimple)lggGenerator).setTimeout(getRemainingPartialSolutionTime(), TimeUnit.SECONDS);
				RDFResourceTree lgg = lggGenerator.getLGG(currentTree, uncoveredTree);
				MonitorFactory.getTimeMonitor("lgg").stop();
//				System.out.println("COMPLETE:" + ((LGGGeneratorSimple)lggGenerator).isComplete());
//				logger.info("LGG: "  + lgg.getStringRepresentation());
				
				// redundancy check
				boolean redundant = isRedundant(lgg);
				if(redundant) {
					logger.trace("redundant");
					continue;
				}
				
				// evaluate the LGG
				Set<EvaluatedRDFResourceTree> solutions = evaluate(lgg, true);
				for (EvaluatedRDFResourceTree solution : solutions) {
					solution.setBaseQueryTrees(baseQueryTrees);
					logger.trace("solution: {} ({})", solution.getBaseQueryTrees(), solution.getTreeScore());
					expressionTests++;
					double score = solution.getScore();
					double mas = heuristic.getMaximumAchievableScore(solution);
					
					if(score >= bestCurrentScore){
						if(score > bestCurrentScore){
							timeBestSolutionFound = getCurrentRuntimeInMilliSeconds();
							logger.info("\tGot better solution after {}ms:" + solution.getTreeScore(), timeBestSolutionFound);
							logger.info("\t" + solutionAsString(solution.asEvaluatedDescription()));
							bestCurrentScore = score;
							bestPartialSolutionTree = solution;
						}
						// add to ToDo list, if not already contained in ToDo list or solution list
						if(bestCurrentScore == 1.0 || mas > score){
//							todo(solution);
						}
					} else if(bestCurrentScore == 1.0 || mas >= bestCurrentScore){ // add to ToDo list if max. achievable score is higher
//						todo(solution);
					} else {
						logger.trace("Too weak: {}", solution.getTreeScore());
//						System.err.println(solution.getEvaluatedDescription());
//						System.out.println("Too general");
//						System.out.println("MAS=" + mas + "\nBest=" + bestCurrentScore);
//						todo(solution);
					}
					todo(solution);
					addToSolutions(solution);
				}
			}
//			addToSolutions(currentElement);
		}
		
		long endTime = System.currentTimeMillis();
		logger.info("...finished computing best partial solution in " + (endTime-partialSolutionStartTime) + "ms.");
		EvaluatedDescription bestPartialSolution = bestPartialSolutionTree.asEvaluatedDescription();
		
		logger.info("Best partial solution: " + solutionAsString(bestPartialSolution) + "\n(" + bestPartialSolution.getScore() + ")");
		
		logger.trace("LGG time: " + MonitorFactory.getTimeMonitor("lgg").getTotal() + "ms");
		logger.trace("Avg. LGG time: " + MonitorFactory.getTimeMonitor("lgg").getAvg() + "ms");
		logger.info("#LGG computations: " + MonitorFactory.getTimeMonitor("lgg").getHits());
		
		logger.trace("Subsumption test time: " + MonitorFactory.getTimeMonitor("subsumption").getTotal() + "ms");
		logger.trace("Avg. subsumption test time: " + MonitorFactory.getTimeMonitor("subsumption").getAvg() + "ms");
		logger.trace("#Subsumption tests: " + MonitorFactory.getTimeMonitor("subsumption").getHits());
		
		return bestPartialSolutionTree;
	}
	
	private String solutionAsString(EvaluatedDescription ed) {
		return renderer.render(ed.getDescription()).replace("\n", "").replaceAll("\\\\s{2,}", " ");
	}
	
	private boolean addToSolutions(EvaluatedRDFResourceTree solution) {
		for (EvaluatedRDFResourceTree partialSolution : currentPartialSolutions) {
			if(QueryTreeUtils.sameTrees(partialSolution.getTree(), solution.getTree())) {
				return false;
			}
		}
		return currentPartialSolutions.add(solution);
	}
	
	/**
	 * Initializes the ToDo list with all distinct trees contained in the given list of positive
	 * example trees {@code posExamples} and negative example trees {@code negExamples}.
	 * First, distinct trees are computed and afterwards, for each tree an initial score will be
	 *  computed.
	 * @param posExamples the positive example trees
	 * @param negExamples the negative example trees
	 */
	private void initTodoList(List<RDFResourceTree> posExamples, List<RDFResourceTree> negExamples){
		todoList = new PriorityQueue<>();
		currentPartialSolutions = new TreeSet<>();
//		EvaluatedRDFResourceTree dummy = new EvaluatedRDFResourceTree(new QueryTreeImpl<String>((N)"TOP"), trees, 0d);
//		todoList.add(dummy);
		
		// compute distinct trees, i.e. check if some of the trees already cover others
		Collection<RDFResourceTree> distinctTrees = new ArrayList<>();
		for (RDFResourceTree queryTree : posExamples) {
			boolean distinct = true;
			for (RDFResourceTree otherTree : distinctTrees) {
				if(!queryTree.equals(otherTree)){
					if(QueryTreeUtils.sameTrees(queryTree, otherTree)){
						distinct = false;
						break;
					}
				}
			}
			if(distinct){
				distinctTrees.add(queryTree);
			}
		}
		
		// compute an initial score
		for (RDFResourceTree queryTree : distinctTrees) {
			EvaluatedRDFResourceTree evaluatedQueryTree = evaluateSimple(queryTree, false);
			evaluatedQueryTree.setBaseQueryTrees(Collections.singleton(queryTree));
			todoList.add(evaluatedQueryTree);
		}
	}
	
	/**
	 * @return TRUE if the query tree is already contained in the solutions or
	 * todo list, otherwise FALSE
	 */
	private boolean isRedundant(RDFResourceTree tree) {
		//check if not already contained in todo list
		for (EvaluatedRDFResourceTree evTree : todoList) {
			if(QueryTreeUtils.sameTrees(tree, evTree.getTree())){
				logger.trace("Not added to TODO list: Already contained in.");
//				logger.trace(evTree.getBaseQueryTrees().toString());
				return true;
			}
		}
		
		//check if not already contained in solutions
		for (EvaluatedRDFResourceTree evTree : currentPartialSolutions) {
			if(QueryTreeUtils.sameTrees(tree, evTree.getTree())){
				logger.trace("Not added to partial solutions list: Already contained in.");
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Add tree to ToDo list if not already contained in that list or the solutions.
	 * @param solution the solution
	 */
	private void todo(EvaluatedRDFResourceTree solution){
		logger.trace("Added to TODO list.");
		todoList.add(solution);
	}
	
	private EvaluatedRDFResourceTree evaluateSimple(RDFResourceTree tree, boolean useSpecifity){
		//1. get a score for the coverage = recall oriented
		//compute positive examples which are not covered by LGG
		List<RDFResourceTree> uncoveredPositiveExampleTrees = getUncoveredTrees(tree, currentPosExampleTrees);
		Set<OWLIndividual> uncoveredPosExamples = new TreeSet<>();
		for (RDFResourceTree queryTree : uncoveredPositiveExampleTrees) {
			uncoveredPosExamples.add(tree2Individual.get(queryTree));
		}
		//compute negative examples which are covered by LGG
		Collection<RDFResourceTree> coveredNegativeExampleTrees = getCoveredTrees(tree, currentNegExampleTrees);
		Set<OWLIndividual> coveredNegExamples = new TreeSet<>();
		for (RDFResourceTree queryTree : coveredNegativeExampleTrees) {
			coveredNegExamples.add(tree2Individual.get(queryTree));
		}
		//compute score
		int coveredPositiveExamples = currentPosExampleTrees.size() - uncoveredPositiveExampleTrees.size();
		double recall = coveredPositiveExamples / (double)currentPosExampleTrees.size();
		double precision = (coveredNegativeExampleTrees.size() + coveredPositiveExamples == 0)
						? 0
						: coveredPositiveExamples / (double)(coveredPositiveExamples + coveredNegativeExampleTrees.size());
		
		double coverageScore = Heuristics.getFScore(recall, precision, beta);
		
		//2. get a score for the specifity of the query, i.e. how many edges/nodes = precision oriented
		int nrOfSpecificNodes = 0;
		for (RDFResourceTree childNode : QueryTreeUtils.getNodes(tree)) {
			if(!childNode.isVarNode()){
				nrOfSpecificNodes++;
			}
		}
		double specifityScore = 0d;
		if(useSpecifity){
			specifityScore = Math.log(nrOfSpecificNodes);
		} else {
			specifityScore = 1 / (double) nrOfSpecificNodes;
		}

		//3.compute the total score
		double score = coverageWeight * coverageScore + specifityWeight * specifityScore;

		QueryTreeScore queryTreeScore = new QueryTreeScore(score, coverageScore,
				new TreeSet<>(Sets.difference(currentPosExamples, uncoveredPosExamples)), uncoveredPosExamples,
				coveredNegExamples, new TreeSet<>(Sets.difference(currentNegExamples, coveredNegExamples)),
				specifityScore, nrOfSpecificNodes);
		
//		QueryTreeScore queryTreeScore = new QueryTreeScore(score, coverageScore,
//				null,null,null,null,
//				specifityScore, nrOfSpecificNodes);
		
		EvaluatedRDFResourceTree evaluatedTree = new EvaluatedRDFResourceTree(tree, uncoveredPositiveExampleTrees, coveredNegativeExampleTrees, queryTreeScore);
		
		//TODO use only the heuristic to compute the score
		score = heuristic.getScore(evaluatedTree);
		queryTreeScore.setScore(score);
		queryTreeScore.setAccuracy(score);
		
		return evaluatedTree;
	}
	
	/**
	 * Evaluated a query tree such that it returns a set of evaluated query trees.
	 * A set is returned because there are several ways how to convert literal nodes.
	 * @param tree the query tree
	 * @param useSpecifity whether to use SPECIFITY as measure
	 * @return a set of evaluated query trees
	 */
	private Set<EvaluatedRDFResourceTree> evaluate(RDFResourceTree tree, boolean useSpecifity){
		Set<EvaluatedRDFResourceTree> evaluatedTrees = new TreeSet<>();
		
		LiteralNodeSubsumptionStrategy[] strategies = LiteralNodeSubsumptionStrategy.values();
		strategies = new LiteralNodeSubsumptionStrategy[]{
				LiteralNodeSubsumptionStrategy.DATATYPE,
//				LiteralNodeSubsumptionStrategy.INTERVAL,
//				LiteralNodeSubsumptionStrategy.MIN,
//				LiteralNodeSubsumptionStrategy.MAX,
				};
		for (LiteralNodeSubsumptionStrategy strategy : strategies) {
			// 1. get a score for the coverage = recall oriented
			List<RDFResourceTree> uncoveredPositiveExampleTrees = new ArrayList<>();
			List<RDFResourceTree> coveredNegativeExampleTrees = new ArrayList<>();
			
			// compute positive examples which are not covered by LGG
			for (RDFResourceTree posTree : currentPosExampleTrees) {
//				System.out.print(currentPosExampleTrees.indexOf(posTree) + ":");
				if(!QueryTreeUtils.isSubsumedBy(posTree, tree, entailment, reasoner)){
//					System.err.println(posTree.getStringRepresentation(true));System.err.println(tree.getStringRepresentation(true));
//					System.out.println("FALSE");
					uncoveredPositiveExampleTrees.add(posTree);
				} else {
//					System.out.println("TRUE");
				}
			}
			
			// compute negative examples which are covered by LGG
			for (RDFResourceTree negTree : currentNegExampleTrees) {
				if(QueryTreeUtils.isSubsumedBy(negTree, tree, entailment, reasoner)){
					coveredNegativeExampleTrees.add(negTree);
				}
			}
			
			// convert to individuals
			Set<OWLIndividual> uncoveredPosExamples = asIndividuals(uncoveredPositiveExampleTrees);
			Set<OWLIndividual> coveredNegExamples = asIndividuals(coveredNegativeExampleTrees);
			
			// compute score
			int coveredPositiveExamples = currentPosExampleTrees.size() - uncoveredPositiveExampleTrees.size();
			double recall = coveredPositiveExamples / (double)currentPosExampleTrees.size();
			double precision = (coveredNegativeExampleTrees.size() + coveredPositiveExamples == 0)
							? 0
							: coveredPositiveExamples / (double)(coveredPositiveExamples + coveredNegativeExampleTrees.size());
			
			double coverageScore = Heuristics.getFScore(recall, precision, beta);
			
			// 2. get a score for the specifity of the query, i.e. how many edges/nodes = precision oriented
			int nrOfSpecificNodes = 0;
			for (RDFResourceTree childNode : QueryTreeUtils.getNodes(tree)) {
				if(!childNode.isVarNode()){
					nrOfSpecificNodes++;
				}
			}
			double specifityScore = 0d;
			if(useSpecifity){
				specifityScore = Math.log(nrOfSpecificNodes);
			}
			
			// 3.compute the total score
			double score = coverageWeight * coverageScore + specifityWeight * specifityScore;
			
			QueryTreeScore queryTreeScore = new QueryTreeScore(score, coverageScore,
					new TreeSet<>(Sets.difference(currentPosExamples, uncoveredPosExamples)), uncoveredPosExamples,
					coveredNegExamples, new TreeSet<>(Sets.difference(currentNegExamples, coveredNegExamples)),
					specifityScore, nrOfSpecificNodes);
			
			EvaluatedRDFResourceTree evaluatedTree = new EvaluatedRDFResourceTree(tree, uncoveredPositiveExampleTrees, coveredNegativeExampleTrees, queryTreeScore);
			
			//TODO use only the heuristic to compute the score
			score = heuristic.getScore(evaluatedTree);
			queryTreeScore.setScore(score);
			queryTreeScore.setAccuracy(score);
			
			evaluatedTrees.add(evaluatedTree);
		}
		
		return evaluatedTrees;
	}

	/**
	 * Evaluated a query tree such that it returns a set of evaluated query trees.
	 * A set is returned because there are several ways how to convert literal nodes.
	 * @param tree the query tree
	 * @param useSpecifity whether to use SPECIFITY as measure
	 * @return a set of evaluated query trees
	 */
	private Set<EvaluatedRDFResourceTree> evaluate2(RDFResourceTree tree, boolean useSpecifity){
		Set<EvaluatedRDFResourceTree> evaluatedTrees = new TreeSet<>();
		
		//test different strategies on the conversion of literal nodes
		Set<OWLClassExpression> combinations = new HashSet<>();
		
		for (LiteralNodeConversionStrategy strategy : strategies) {
			OWLClassExpression ce = QueryTreeUtils.toOWLClassExpression(tree);
			combinations.add(ce);
		}
		//compute all combinations of different types of facets
//		OWLClassExpression ce = tree.asOWLClassExpression(LiteralNodeConversionStrategy.FACET_RESTRICTION);
//		combinations = ce.accept(new ClassExpressionLiteralCombination());
		for (OWLClassExpression c : combinations) {
			//convert to individuals
			SortedSet<OWLIndividual> coveredExamples = reasoner.getIndividuals(c);
			Set<OWLIndividual> coveredPosExamples = new TreeSet<>(Sets.intersection(currentPosExamples, coveredExamples));
			Set<OWLIndividual> uncoveredPosExamples = new TreeSet<>(Sets.difference(currentPosExamples, coveredExamples));
			Set<OWLIndividual> coveredNegExamples = new TreeSet<>(Sets.intersection(currentNegExamples, coveredExamples));
			Set<OWLIndividual> uncoveredNegExamples = new TreeSet<>(Sets.difference(currentNegExamples, coveredExamples));
			
			//compute score
			double recall = coveredPosExamples.size() / (double)currentPosExamples.size();
			double precision = (coveredNegExamples.size() + coveredPosExamples.size() == 0)
							? 0
							: coveredPosExamples.size() / (double)(coveredPosExamples.size() + coveredNegExamples.size());
			
			double coverageScore = Heuristics.getFScore(recall, precision, beta);
			
			//2. get a score for the specificity of the query, i.e. how many edges/nodes = precision oriented
			int nrOfSpecificNodes = 0;
			for (RDFResourceTree childNode : QueryTreeUtils.getNodes(tree)){
				if(!childNode.isVarNode()){
					nrOfSpecificNodes++;
				}
			}
			double specifityScore = 0d;
			if(useSpecifity){
				specifityScore = Math.log(nrOfSpecificNodes);
			}
			
			//3.compute the total score
			double score = coverageWeight * coverageScore + specifityWeight * specifityScore;
			
			QueryTreeScore queryTreeScore = new QueryTreeScore(
					score, coverageScore,
					coveredPosExamples, uncoveredPosExamples,
					coveredNegExamples, uncoveredNegExamples,
					specifityScore, nrOfSpecificNodes);
			
			//TODO use only the heuristic to compute the score
			EvaluatedRDFResourceTree evaluatedTree = new EvaluatedRDFResourceTree(tree,
					asQueryTrees(uncoveredPosExamples), asQueryTrees(coveredNegExamples), queryTreeScore);
			score = heuristic.getScore(evaluatedTree);
			queryTreeScore.setScore(score);
			queryTreeScore.setAccuracy(score);
			
			
			EvaluatedDescription evaluatedDescription = new EvaluatedDescription(c, queryTreeScore);
			
			evaluatedTree.setDescription(evaluatedDescription);
			
			evaluatedTrees.add(evaluatedTree);
		}
		return evaluatedTrees;
	}
	
	private EvaluatedDescription<? extends Score> buildCombinedSolution(){
		EvaluatedDescription<? extends Score> bestCombinedSolution = null;
		double bestScore = Double.NEGATIVE_INFINITY;
		LiteralNodeConversionStrategy[] strategies = LiteralNodeConversionStrategy.values();
		strategies = new LiteralNodeConversionStrategy[]{LiteralNodeConversionStrategy.DATATYPE};
		for (LiteralNodeConversionStrategy strategy : strategies) {
			EvaluatedDescription<? extends Score> combinedSolution;
			if(partialSolutions.size() == 1){
				combinedSolution = partialSolutions.get(0).asEvaluatedDescription();
			} else {
				Set<OWLClassExpression> disjuncts = new TreeSet<>();
				
				Set<OWLIndividual> posCovered = new HashSet<>();
				Set<OWLIndividual> negCovered = new HashSet<>();
				
				//build the union of all class expressions
				OWLClassExpression partialDescription;
				for (EvaluatedRDFResourceTree partialSolution : partialSolutions) {
					partialDescription = partialSolution.asEvaluatedDescription().getDescription();
					disjuncts.add(partialDescription);
					posCovered.addAll(partialSolution.getTreeScore().getCoveredPositives());
					negCovered.addAll(partialSolution.getTreeScore().getCoveredNegatives());
				}
				OWLClassExpression unionDescription = dataFactory.getOWLObjectUnionOf(disjuncts);
				
				Set<OWLIndividual> posNotCovered = Sets.difference(lp.getPositiveExamples(), posCovered);
				Set<OWLIndividual> negNotCovered = Sets.difference(lp.getNegativeExamples(), negCovered);
				
				//compute the coverage
				double recall = posCovered.size() / (double)lp.getPositiveExamples().size();
				double precision = (posCovered.size() + negCovered.size() == 0)
								? 0
								: posCovered.size() / (double)(posCovered.size() + negCovered.size());
				
				double coverageScore = Heuristics.getFScore(recall, precision, beta);
				
//				ScoreTwoValued score = new ScoreTwoValued(posCovered, posNotCovered, negCovered, negNotCovered);
//				score.setAccuracy(coverageScore);
				QueryTreeScore score = new QueryTreeScore(coverageScore, coverageScore, posCovered, posNotCovered, negCovered, negNotCovered, -1, -1);
				
				combinedSolution = new EvaluatedDescription(unionDescription, score);
			}
			if(combinedSolution.getAccuracy() > bestScore){
				bestCombinedSolution = combinedSolution;
				bestCurrentScore = combinedSolution.getAccuracy();
			}
		}
		return bestCombinedSolution;
	}
	
	private void reset(){
		stop = false;
		isRunning = true;
		
		currentBestSolution = null;
		partialSolutions = new ArrayList<>();
		
		bestCurrentScore = minimumTreeScore;
		
		MonitorFactory.getTimeMonitor("lgg").reset();
		nanoStartTime = System.nanoTime();
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.StoppableLearningAlgorithm#stop()
	 */
	@Override
	public void stop() {
		stop = true;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractCELA#getCurrentlyBestDescription()
	 */
	@Override
	public OWLClassExpression getCurrentlyBestDescription() {
		return currentBestSolution.getDescription();
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractCELA#getCurrentlyBestEvaluatedDescription()
	 */
	@Override
	public EvaluatedDescription getCurrentlyBestEvaluatedDescription() {
		return currentBestSolution;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.StoppableLearningAlgorithm#isRunning()
	 */
	@Override
	public boolean isRunning() {
		return isRunning;
	}
	
//	@Autowired
//	public void setLearningProblem(PosNegLP learningProblem) {
//		this.lp = learningProblem;
//	}
	
//	@Autowired
	@Override
	public void setReasoner(AbstractReasonerComponent reasoner){
		super.setReasoner(reasoner);
//		loadModel();
	}

	private void loadModel(){
		model = ModelFactory.createDefaultModel();
		for (KnowledgeSource ks : reasoner.getSources()) {
			if(ks instanceof OWLFile){
				try {
					model.read(((OWLFile) ks).getURL().openStream(), null);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if(ks instanceof OWLAPIOntology){
				ByteArrayInputStream bais = new ByteArrayInputStream(((OWLAPIOntology) ks).getConverter().convert(((OWLAPIOntology) ks).getOntology()));
				model.read(bais, null);
				try {
					bais.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private Set<OWLIndividual> asIndividuals(Collection<RDFResourceTree> trees){
		Set<OWLIndividual> individuals = new HashSet<>(trees.size());
		for (RDFResourceTree queryTree : trees) {
			individuals.add(tree2Individual.get(queryTree));
		}
		return individuals;
	}
	
	private Set<RDFResourceTree> asQueryTrees(Collection<OWLIndividual> individuals){
		Set<RDFResourceTree> trees = new HashSet<>(individuals.size());
		for (OWLIndividual ind : individuals) {
			trees.add(tree2Individual.inverse().get(ind));
		}
		return trees;
	}

	/**
	 * Computes all trees from the given list {@code allTrees} which are subsumed by {@code tree}.
	 * @param tree the tree
	 * @param trees all trees
	 * @return all trees from the given list {@code allTrees} which are subsumed by {@code tree}
	 */
	private List<RDFResourceTree> getCoveredTrees(RDFResourceTree tree, List<RDFResourceTree> trees){
		List<RDFResourceTree> coveredTrees = new ArrayList<>();
		for (RDFResourceTree queryTree : trees) {
			if(QueryTreeUtils.isSubsumedBy(queryTree, tree)){
				coveredTrees.add(queryTree);
			}
		}
		return coveredTrees;
	}

	/**
	 * Computes all trees from the given list {@code trees} which are not subsumed by {@code tree}.
	 * @param tree the tree
	 * @param trees the trees
	 * @return all trees from the given list {@code trees} which are not subsumed by {@code tree}.
	 */
	private List<RDFResourceTree> getUncoveredTrees(RDFResourceTree tree, List<RDFResourceTree> trees){
		List<RDFResourceTree> uncoveredTrees = new ArrayList<>();
		for (RDFResourceTree queryTree : trees) {
			if(!QueryTreeUtils.isSubsumedBy(queryTree, tree)){
				uncoveredTrees.add(queryTree);
			}
		}
		return uncoveredTrees;
	}

	private boolean terminationCriteriaSatisfied() {
		//stop was called or time expired
		if(stop || isTimeExpired()){
			return true;
		}
		
		// stop if there are no more positive examples to cover
		if (stopOnFirstDefinition && currentPosExamples.isEmpty()) {
			return true;
		}

		// we stop when the score of the last tree added is too low
		// (indicating that the algorithm could not find anything appropriate
		// in the timeframe set)
		if (bestCurrentScore < minimumTreeScore) {
			return true;
		}

		// stop when almost all positive examples have been covered
		if (tryFullCoverage) {
			return false;
		} else {
			int maxPosRemaining = (int) Math.ceil(startPosExamplesSize * 0.05d);
			return (currentPosExamples.size() <= maxPosRemaining);
		}
	}
	
	private boolean partialSolutionTerminationCriteriaSatisfied(){
		return stop || todoList.isEmpty() || currentPosExampleTrees.isEmpty() || (useDisjunction && isPartialSolutionTimeExpired()) || isTimeExpired();
	}
	
	private boolean isPartialSolutionTimeExpired(){
		return maxTreeComputationTimeInSeconds > 0 && getRemainingPartialSolutionTime() > 0;
	}

	private long getRemainingPartialSolutionTime() {
		return (long) (maxTreeComputationTimeInSeconds  - (System.currentTimeMillis() - partialSolutionStartTime) / 1000);
	}
	
	/**
	 * Shows the current setup of the algorithm.
	 */
	private void printSetup(){
		String setup = "Setup:";
		setup += "\n#Pos. examples:" + currentPosExampleTrees.size();
		setup += "\n#Neg. examples:" + currentNegExampleTrees.size();
		setup += "\nHeuristic:" + heuristic.getHeuristicType().name();
		setup += "\nNoise value=" + noise;
		setup += "\nbeta=" + beta;
		logger.info(setup);
	}
	
	/**
	 * @param noisePercentage the noisePercentage to set
	 */
	public void setNoisePercentage(double noisePercentage) {
		this.noisePercentage = noisePercentage;
	}
	
	/**
	 * @param noise the noise to set
	 */
	public void setNoise(double noise) {
		this.noise = noise;
	}
	
	/**
	 * Default value is 1. Lower values force importance of covering positive examples.
	 * @param beta the beta to set
	 */
	public void setBeta(double beta) {
		this.beta = beta;
	}
	
	/**
	 * Set the max. execution time for the computation of a partial solution. If this value isn't set, the
	 * max. algorithm runtime will be used, thus, in worst case only one partial solution was computed.
	 *
	 * @param maxTreeComputationTimeInSeconds the max. computation for a partial solution tree
	 */
	public void setMaxTreeComputationTimeInSeconds(double maxTreeComputationTimeInSeconds) {
		this.maxTreeComputationTimeInSeconds = maxTreeComputationTimeInSeconds;
	}
	
	/**
	 * @return the heuristic
	 */
	public QueryTreeHeuristic getHeuristic() {
		return heuristic;
	}
	
	/**
	 * @param heuristic the heuristic to set
	 */
	public void setHeuristic(QueryTreeHeuristic heuristic) {
		this.heuristic = heuristic;
	}
	
	/**
	 * @param treeFactory the treeFactory to set
	 */
	public void setTreeFactory(org.dllearner.algorithms.qtl.impl.QueryTreeFactory treeFactory) {
		this.treeFactory = treeFactory;
	}
	
	public EvaluatedRDFResourceTree getBestSolution(){
		return currentPartialSolutions.last();
	}
	
	public SortedSet<EvaluatedRDFResourceTree> getSolutions(){
		return currentPartialSolutions;
	}
	
	public List<EvaluatedRDFResourceTree> getSolutionsAsList(){
		//		Collections.sort(list, Collections.reverseOrder());
		return new ArrayList<>(currentPartialSolutions);
	}
	
	/**
	 * @param positiveExampleTrees the positive example trees to set
	 */
	public void setPositiveExampleTrees(Map<OWLIndividual,RDFResourceTree> positiveExampleTrees) {
		this.currentPosExampleTrees = new ArrayList<>(positiveExampleTrees.values());
		this.currentPosExamples = new HashSet<>(positiveExampleTrees.keySet());
		
		for (Entry<OWLIndividual, RDFResourceTree> entry : positiveExampleTrees.entrySet()) {
			OWLIndividual ind = entry.getKey();
			RDFResourceTree tree = entry.getValue();
			tree2Individual.put(tree, ind);
		}
	}
	
	/**
	 * @param negativeExampleTrees the negative example trees to set
	 */
	public void setNegativeExampleTrees(Map<OWLIndividual,RDFResourceTree> negativeExampleTrees) {
		this.currentNegExampleTrees = new ArrayList<>(negativeExampleTrees.values());
		this.currentNegExamples = new HashSet<>(negativeExampleTrees.keySet());
		
		for (Entry<OWLIndividual, RDFResourceTree> entry : negativeExampleTrees.entrySet()) {
			OWLIndividual ind = entry.getKey();
			RDFResourceTree tree = entry.getValue();
			tree2Individual.put(tree, ind);
		}
	}
	
	/**
	 * @param ks the ks to set
	 */
	@Autowired
	public void setKs(SparqlEndpointKS ks) {
		this.ks = ks;
	}
	
	/**
	 * @param maxTreeDepth the maximum depth of the trees, if those have to be generated
	 * first. The default depth is 2.
	 */
	public void setMaxTreeDepth(int maxTreeDepth) {
		this.maxTreeDepth = maxTreeDepth;
	}
	
	/**
	 * @return the runtime in ms until the best solution was found
	 */
	public long getTimeBestSolutionFound() {
		return timeBestSolutionFound;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		super.clone();
		return new QTL2Disjunctive(this);
	}
}
