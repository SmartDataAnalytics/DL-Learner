package org.dllearner.algorithms.qtl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.qtl.cache.QueryTreeCache;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.LiteralNodeConversionStrategy;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.LiteralNodeSubsumptionStrategy;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryImpl;
import org.dllearner.algorithms.qtl.operations.lgg.EvaluatedQueryTree;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGeneratorImpl;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.EvaluatedDescription;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.QueryTreeScore;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

@ComponentAnn(name="query tree learner with noise (disjunctive)", shortName="qtl2dis", version=0.8)
public class QTL2Disjunctive extends AbstractCELA implements Cloneable{
	
	private static final Logger logger = Logger.getLogger(QTL2Disjunctive.class);
	private final DecimalFormat dFormat = new DecimalFormat("0.00"); 
	
	private LGGGenerator<String> lggGenerator = new LGGGeneratorImpl<String>();
	
	private QueryTreeFactory<String> treeFactory;
	private ConciseBoundedDescriptionGenerator cbdGen;
	
	private Queue<EvaluatedQueryTree<String>> todoList;
	private SortedSet<EvaluatedQueryTree<String>> currentPartialSolutions;
	
	private double bestCurrentScore = 0d;
	
	private Set<String> allowedNamespaces = new HashSet<String>();
	private Set<String> ignoredProperties = new HashSet<String>();

	private List<QueryTree<String>> currentPosExampleTrees = new ArrayList<QueryTree<String>>();
	private List<QueryTree<String>> currentNegExampleTrees = new ArrayList<QueryTree<String>>();
	private Set<OWLIndividual> currentPosExamples = new HashSet<OWLIndividual>();
	private Set<OWLIndividual> currentNegExamples = new HashSet<OWLIndividual>();
	
	private Map<QueryTree<String>, OWLIndividual> tree2Individual = new HashMap<QueryTree<String>, OWLIndividual>();
	private Map<OWLIndividual, QueryTree<String>> individual2Tree = new HashMap<OWLIndividual, QueryTree<String>>();
	
	private QueryTreeCache treeCache;

	private PosNegLP lp;

	private Model model;

	private volatile boolean stop;
	private boolean isRunning;

	private Monitor subMon;
	private Monitor lggMon;

	private List<EvaluatedQueryTree<String>> partialSolutions;
	
	private EvaluatedDescription currentBestSolution;
	
	private QueryTreeHeuristic heuristic;
	
	
	//Parameters
	@ConfigOption(name = "noisePercentage", defaultValue="0.0", description="the (approximated) percentage of noise within the examples")
	private double noisePercentage = 0.0;
	@ConfigOption(defaultValue = "10", name = "maxExecutionTimeInSeconds", description = "maximum execution of the algorithm in seconds")
	private int maxExecutionTimeInSeconds = 60;
	
	private double coverageWeight = 0.8;
	private double specifityWeight = 0.1;
	
	private double minCoveredPosExamplesFraction = 0.2;
	// maximum execution time to compute a part of the solution
	private double maxTreeComputationTimeInSeconds = 10;
	// how important not to cover negatives
	private double beta = 1;
	// minimum score a query tree must have to be part of the solution
	private double minimumTreeScore = 0.3;
	//If yes, then the algorithm tries to cover all positive examples. Note that while this improves accuracy on the testing set, 
	//it may lead to overfitting
	private boolean tryFullCoverage;
	//algorithm will terminate immediately when a correct definition is found
	private boolean stopOnFirstDefinition;
	
	private long startTime;
	private long partialSolutionStartTime;
	
	private double startPosExamplesSize;
	private int expressionTests = 0;
	
	LiteralNodeConversionStrategy[] strategies = new LiteralNodeConversionStrategy[]{
			LiteralNodeConversionStrategy.MIN,
			LiteralNodeConversionStrategy.MAX,
			LiteralNodeConversionStrategy.MIN_MAX,
			LiteralNodeConversionStrategy.DATATYPE
	};
	private QueryExecutionFactory qef;
	
	public QTL2Disjunctive() {}
	
	public QTL2Disjunctive(PosNegLP learningProblem, AbstractReasonerComponent reasoner) throws LearningProblemUnsupportedException{
		super(learningProblem, reasoner);
		loadModel();
	}
	
	public QTL2Disjunctive(PosNegLP lp, QueryExecutionFactory qef) {
		super.learningProblem = lp;
		this.lp = lp;
		this.qef = qef;
	}
	
//	public QTL2Disjunctive(PosNegLP lp, Model model) {
//		this.learningProblem = lp;
//		this.model = model;
//	}
	
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
		
		if(treeFactory == null){
			treeFactory = new QueryTreeFactoryImpl();
			treeFactory.addAllowedNamespaces(allowedNamespaces);
			treeFactory.addIgnoredPropperties(ignoredProperties);
		}
		cbdGen = new ConciseBoundedDescriptionGeneratorImpl(qef);
		
		//get the query trees
		generateQueryTrees();
		
		if(heuristic == null){
			heuristic = new QueryTreeHeuristic();
			heuristic.setPosExamplesWeight(beta);
		}
		
		startPosExamplesSize = currentPosExampleTrees.size();
		
		//some logging
		subMon = MonitorFactory.getTimeMonitor("subsumption-mon");
		lggMon = MonitorFactory.getTimeMonitor("lgg-mon");
		
		//console rendering of class expressions
		ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
		ToStringRenderer.getInstance().setShortFormProvider(new SimpleShortFormProvider());
		
		//compute the LGG for all examples
		//this allows us to prune all other trees because we can omit paths in trees which are contained in all positive
		//as well as negative examples
//		List<QueryTree<String>> allExamplesTrees = new ArrayList<QueryTree<String>>();
//		allExamplesTrees.addAll(currentPosExampleTrees);
//		allExamplesTrees.addAll(currentNegExampleTrees);
//		QueryTree<String> lgg = lggGenerator.getLGG(allExamplesTrees);
//		lgg.dump();
		logger.info("Initialization finished.");
	}
	
	private void generateQueryTrees(){
		logger.info("Generating trees...");
		QueryTree<String> queryTree;
		
		// positive examples
		if(currentPosExampleTrees.isEmpty()){
			for (OWLIndividual ind : lp.getPositiveExamples()) {
				Model cbd = cbdGen.getConciseBoundedDescription(ind.toStringID(), 2);
				queryTree = treeFactory.getQueryTree(ind.toStringID(), cbd);
				tree2Individual.put(queryTree, ind);
				currentPosExampleTrees.add(queryTree);
			}
		}
		
		// negative examples
		if(currentNegExampleTrees.isEmpty()){
			for (OWLIndividual ind : lp.getNegativeExamples()) {
				Model cbd = cbdGen.getConciseBoundedDescription(ind.toStringID(), 2);
				queryTree = treeFactory.getQueryTree(ind.toStringID(), cbd);
				tree2Individual.put(queryTree, ind);
				currentNegExampleTrees.add(treeCache.getQueryTree(ind.toStringID()));
			}
		}
		logger.info("...done.");
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningAlgorithm#start()
	 */
	@Override
	public void start() {
		showSetup();
		logger.info("Running...");
		startTime = System.currentTimeMillis();
		
		reset();
		
		int i = 1;
		while(!terminationCriteriaSatisfied()){
			logger.info(i++ + ". iteration...");
			logger.info("#Remaining pos. examples:" + currentPosExampleTrees.size());
			logger.info("#Remaining neg. examples:" + currentNegExampleTrees.size());
			
			//compute best (partial) solution computed so far
			EvaluatedQueryTree<String> bestPartialSolution = computeBestPartialSolution();
			
			//add to partial solutions if criteria are satisfied
			if(bestPartialSolution.getScore() >= minimumTreeScore){
				
				partialSolutions.add(bestPartialSolution);
				
				//remove all covered examples
				QueryTree<String> tree;
				for (Iterator<QueryTree<String>> iterator = currentPosExampleTrees.iterator(); iterator.hasNext();) {
					tree = iterator.next();
					if(!bestPartialSolution.getFalseNegatives().contains(tree)){//a pos tree that is not covered
						iterator.remove();
						currentPosExamples.remove(tree2Individual.get(tree));
					}
				}
				for (Iterator<QueryTree<String>> iterator = currentNegExampleTrees.iterator(); iterator.hasNext();) {
					tree = iterator.next();
					if(bestPartialSolution.getFalsePositives().contains(tree)){//a neg example that is covered
						iterator.remove();
						currentNegExamples.remove(tree2Individual.get(tree));
					}
				}
				
				//build the current combined solution
				currentBestSolution = buildCombinedSolution();
				
				logger.info("combined accuracy: " + dFormat.format(currentBestSolution.getAccuracy()));
			} else {
				logger.info("no tree found, which satisfies the minimum criteria - the best was: "
						+ currentBestSolution.getDescription()
						+ " with score " + currentBestSolution.getScore());
			}
			
		};
		
		isRunning = false;
		
		long endTime = System.currentTimeMillis();
		logger.info("Finished in " + (endTime-startTime) + "ms.");
		logger.info(expressionTests +" descriptions tested");
		logger.info("Combined solution:" + currentBestSolution.getDescription().toString().replace("\n", ""));
		
		logger.info(currentBestSolution.getScore());
		
	}
	
	/**
	 * Compute a (partial) solution that covers as much positive examples as possible.
	 * @return
	 */
	private EvaluatedQueryTree<String> computeBestPartialSolution(){
		logger.info("Computing best partial solution...");
		bestCurrentScore = Double.NEGATIVE_INFINITY;
		partialSolutionStartTime = System.currentTimeMillis();
		initTodoList(currentPosExampleTrees, currentNegExampleTrees);
		
		EvaluatedQueryTree<String> bestPartialSolutionTree = null;
		EvaluatedQueryTree<String> currentElement;
		QueryTree<String> currentTree;
		
		while(!partialSolutionTerminationCriteriaSatisfied()){
			logger.trace("TODO list size: " + todoList.size());
			//pick best element from todo list
			currentElement = todoList.poll();
			
			currentTree = currentElement.getTree();
			logger.trace("Next tree: "  + currentElement.getTreeScore() + "\n" + solutionAsString(currentElement.getEvaluatedDescription()));
			//generate the LGG between the chosen tree and each uncovered positive example
			Iterator<QueryTree<String>> it = currentElement.getFalseNegatives().iterator();
			while (it.hasNext() && !isPartialSolutionTimeExpired() && !isTimeExpired()) {
				QueryTree<String> uncoveredTree = it.next();
				
				//compute the LGG
				lggMon.start();
				QueryTree<String> lgg = lggGenerator.getLGG(currentTree, uncoveredTree);
				lggMon.stop();
				
				//evaluate the LGG
				Set<EvaluatedQueryTree<String>> solutions = evaluate(lgg, true);
				for (EvaluatedQueryTree<String> solution : solutions) {
					expressionTests++;
					double score = solution.getScore();
					double mas = heuristic.getMaximumAchievableScore(solution);
					
					if(score >= bestCurrentScore){
						if(score > bestCurrentScore){
							logger.info("\tGot better solution:" + solution.getTreeScore());
							logger.info("\t" + solutionAsString(solution.getEvaluatedDescription()));
							bestCurrentScore = score;
							bestPartialSolutionTree = solution;
						}
						//add to todo list, if not already contained in todo list or solution list
						if(mas > score){
							todo(solution);
						}
					} else if(mas >= bestCurrentScore){ // add to todo list if max. achievable score is higher
						todo(solution);
					} else {
						logger.trace("Too weak:" + solution.getTreeScore());
//						System.err.println(solution.getEvaluatedDescription());
//						System.out.println("Too general");
//						System.out.println("MAS=" + mas + "\nBest=" + bestCurrentScore);
					}
					
					currentPartialSolutions.add(solution);
				}
			}
			currentPartialSolutions.add(currentElement);
		}
		long endTime = System.currentTimeMillis();
		logger.info("...finished computing best partial solution in " + (endTime-partialSolutionStartTime) + "ms.");
		EvaluatedDescription bestPartialSolution = bestPartialSolutionTree.getEvaluatedDescription();
		
		logger.info("Best partial solution: " + solutionAsString(bestPartialSolution) + "\n(" + bestPartialSolution.getScore() + ")");
		
		logger.trace("LGG time: " + lggMon.getTotal() + "ms");
		logger.trace("Avg. LGG time: " + lggMon.getAvg() + "ms");
		logger.info("#LGG computations: " + lggMon.getHits());
		logger.trace("Subsumption test time: " + subMon.getTotal() + "ms");
		logger.trace("Avg. subsumption test time: " + subMon.getAvg() + "ms");
		logger.trace("#Subsumption tests: " + subMon.getHits());
		
		return bestPartialSolutionTree;
	}
	
	private String solutionAsString(EvaluatedDescription ed) {
		return ed.getDescription().toString().replace("\n", "").replace("/\\s{2,}/g", " ");
	}
	
	/**
	 * Initializes the todo list with all distinct trees contained in the given list {@code trees}.
	 * Firstly, distinct trees are computed and afterwards, for each tree a score is computed.
	 * @param trees
	 */
	private void initTodoList(List<QueryTree<String>> posExamples, List<QueryTree<String>> negExamples){
		todoList = new PriorityQueue<EvaluatedQueryTree<String>>();
		currentPartialSolutions = new TreeSet<EvaluatedQueryTree<String>>();
//		EvaluatedQueryTree<String> dummy = new EvaluatedQueryTree<String>(new QueryTreeImpl<String>((N)"TOP"), trees, 0d);
//		todoList.add(dummy);
		//compute distinct trees
		Collection<QueryTree<String>> distinctTrees = new ArrayList<QueryTree<String>>();
		for (QueryTree<String> queryTree : posExamples) {
			boolean distinct = true;
			for (QueryTree<String> otherTree : distinctTrees) {
				if(!queryTree.equals(otherTree)){
					if(queryTree.isSameTreeAs(otherTree)){
						distinct = false;
						break;
					}
				}
			}
			if(distinct){
				distinctTrees.add(queryTree);
			}
		}
		for (QueryTree<String> queryTree : distinctTrees) {//System.out.println(queryTree.getStringRepresentation());
			EvaluatedQueryTree<String> evaluatedQueryTree = evaluateSimple(queryTree, false);
			todoList.add(evaluatedQueryTree);
		}
	}
	
	/**
	 * Add tree to todo list if not already contained in that list or the solutions.
	 * @param solution
	 */
	private void todo(EvaluatedQueryTree<String> solution){
		//check if not already contained in todo list
		for (EvaluatedQueryTree<String> evTree : todoList) {
			//this is a workaround as we have currently no equals method for trees based on the literal conversion strategy
//			boolean sameTree = sameTrees(solution.getTree(), evTree.getTree());
			boolean sameTree = evTree.getEvaluatedDescription().getDescription().toString()
			.equals(solution.getEvaluatedDescription().getDescription().toString());
			if(sameTree){
				logger.trace("Not added to TODO list: Already contained in.");
				return;
			}
		}
		//check if not already contained in solutions
		for (EvaluatedQueryTree<String> evTree : currentPartialSolutions) {
			if(sameTrees(solution.getTree(), evTree.getTree())){
				logger.trace("Not added to partial solutions list: Already contained in.");
				return;
			}
		}
		logger.trace("Added to TODO list.");
		todoList.add(solution);
	}
	
	private EvaluatedQueryTree<String> evaluateSimple(QueryTree<String> tree, boolean useSpecifity){
		//1. get a score for the coverage = recall oriented
		//compute positive examples which are not covered by LGG
		List<QueryTree<String>> uncoveredPositiveExampleTrees = getUncoveredTrees(tree, currentPosExampleTrees);
		Set<OWLIndividual> uncoveredPosExamples = new TreeSet<OWLIndividual>();
		for (QueryTree<String> queryTree : uncoveredPositiveExampleTrees) {
			uncoveredPosExamples.add(tree2Individual.get(queryTree));
		}
		//compute negative examples which are covered by LGG
		Collection<QueryTree<String>> coveredNegativeExampleTrees = getCoveredTrees(tree, currentNegExampleTrees);
		Set<OWLIndividual> coveredNegExamples = new TreeSet<OWLIndividual>();
		for (QueryTree<String> queryTree : coveredNegativeExampleTrees) {
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
		for (QueryTree<String> childNode : tree.getChildrenClosure()) {
			if(!childNode.getUserObject().equals("?")){
				nrOfSpecificNodes++;
			}
		}
		double specifityScore = 0d;
		if(useSpecifity){
			specifityScore = Math.log(nrOfSpecificNodes);
		}
		
		//3.compute the total score
		double score = coverageWeight * coverageScore + specifityWeight * specifityScore;
		
		QueryTreeScore queryTreeScore = new QueryTreeScore(score, coverageScore, 
				new TreeSet<OWLIndividual>(Sets.difference(currentPosExamples, uncoveredPosExamples)), uncoveredPosExamples,
				coveredNegExamples, new TreeSet<OWLIndividual>(Sets.difference(currentNegExamples, coveredNegExamples)),
				specifityScore, nrOfSpecificNodes);
		
//		QueryTreeScore queryTreeScore = new QueryTreeScore(score, coverageScore, 
//				null,null,null,null,
//				specifityScore, nrOfSpecificNodes);
		
		EvaluatedQueryTree<String> evaluatedTree = new EvaluatedQueryTree<String>(tree, uncoveredPositiveExampleTrees, coveredNegativeExampleTrees, queryTreeScore);
		
		//TODO use only the heuristic to compute the score
		score = heuristic.getScore(evaluatedTree);
		queryTreeScore.setScore(score);
		queryTreeScore.setAccuracy(score);
		
		return evaluatedTree;
	}
	
	/**
	 * Returns a set of evaluated query trees. A set is returned because there are several ways how to convert literal nodes.
	 * @param tree
	 * @param useSpecifity
	 * @return
	 */
	private Set<EvaluatedQueryTree<String>> evaluate(QueryTree<String> tree, boolean useSpecifity){
		Set<EvaluatedQueryTree<String>> evaluatedTrees = new TreeSet<EvaluatedQueryTree<String>>();
		
		LiteralNodeSubsumptionStrategy[] strategies = LiteralNodeSubsumptionStrategy.values();
		strategies = new LiteralNodeSubsumptionStrategy[]{
				LiteralNodeSubsumptionStrategy.DATATYPE, 
//				LiteralNodeSubsumptionStrategy.INTERVAL, 
//				LiteralNodeSubsumptionStrategy.MIN,
//				LiteralNodeSubsumptionStrategy.MAX,
				};
		for (LiteralNodeSubsumptionStrategy strategy : strategies) {
			// 1. get a score for the coverage = recall oriented
			List<QueryTree<String>> uncoveredPositiveExampleTrees = new ArrayList<QueryTree<String>>();
			List<QueryTree<String>> coveredNegativeExampleTrees = new ArrayList<QueryTree<String>>();
			
			// compute positive examples which are not covered by LGG
			for (QueryTree<String> posTree : currentPosExampleTrees) {
				if(!posTree.isSubsumedBy(tree, strategy)){
					uncoveredPositiveExampleTrees.add(posTree);
				}
			}
			// compute negative examples which are covered by LGG
			for (QueryTree<String> negTree : currentNegExampleTrees) {
				if(negTree.isSubsumedBy(tree, strategy)){
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
			for (QueryTree<String> childNode : tree.getChildrenClosure()) {
				if(!childNode.getUserObject().equals("?")){
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
					new TreeSet<OWLIndividual>(Sets.difference(currentPosExamples, uncoveredPosExamples)), uncoveredPosExamples,
					coveredNegExamples, new TreeSet<OWLIndividual>(Sets.difference(currentNegExamples, coveredNegExamples)),
					specifityScore, nrOfSpecificNodes);
			
			EvaluatedQueryTree<String> evaluatedTree = new EvaluatedQueryTree<String>(tree, uncoveredPositiveExampleTrees, coveredNegativeExampleTrees, queryTreeScore);
			
			//TODO use only the heuristic to compute the score
			score = heuristic.getScore(evaluatedTree);
			queryTreeScore.setScore(score);
			queryTreeScore.setAccuracy(score);
			
			evaluatedTrees.add(evaluatedTree);
		}
		
		return evaluatedTrees;
	}
	
	/**
	 * Returns a set of evaluated query trees. A set is returned because there are several ways how to convert literal nodes.
	 * @param tree
	 * @param useSpecifity
	 * @return
	 */
	private Set<EvaluatedQueryTree<String>> evaluate2(QueryTree<String> tree, boolean useSpecifity){
		Set<EvaluatedQueryTree<String>> evaluatedTrees = new TreeSet<EvaluatedQueryTree<String>>();
		
		//test different strategies on the conversion of literal nodes
		Set<OWLClassExpression> combinations = new HashSet<OWLClassExpression>();
		
		for (LiteralNodeConversionStrategy strategy : strategies) {
			OWLClassExpression ce = tree.asOWLClassExpression(strategy);
			combinations.add(ce);
		}
		//compute all combinations of different types of facets
//		OWLClassExpression ce = tree.asOWLClassExpression(LiteralNodeConversionStrategy.FACET_RESTRICTION);
//		combinations = ce.accept(new ClassExpressionLiteralCombination());
		for (OWLClassExpression c : combinations) {
			//convert to individuals
			SortedSet<OWLIndividual> coveredExamples = reasoner.getIndividuals(c);
			Set<OWLIndividual> coveredPosExamples = new TreeSet<OWLIndividual>(Sets.intersection(currentPosExamples, coveredExamples));
			Set<OWLIndividual> uncoveredPosExamples = new TreeSet<OWLIndividual>(Sets.difference(currentPosExamples, coveredExamples));
			Set<OWLIndividual> coveredNegExamples = new TreeSet<OWLIndividual>(Sets.intersection(currentNegExamples, coveredExamples));
			Set<OWLIndividual> uncoveredNegExamples = new TreeSet<OWLIndividual>(Sets.difference(currentNegExamples, coveredExamples));
			
			//compute score
			double recall = coveredPosExamples.size() / (double)currentPosExamples.size();
			double precision = (coveredNegExamples.size() + coveredPosExamples.size() == 0) 
							? 0 
							: coveredPosExamples.size() / (double)(coveredPosExamples.size() + coveredNegExamples.size());
			
			double coverageScore = Heuristics.getFScore(recall, precision, beta);
			
			//2. get a score for the specificity of the query, i.e. how many edges/nodes = precision oriented
			int nrOfSpecificNodes = 0;
			for (QueryTree<String> childNode : tree.getChildrenClosure()) {
				if(!childNode.getUserObject().equals("?")){
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
			EvaluatedQueryTree<String> evaluatedTree = new EvaluatedQueryTree<String>(tree, 
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
	
	private EvaluatedDescription buildCombinedSolution(){
		EvaluatedDescription bestCombinedSolution = null;
		double bestScore = Double.NEGATIVE_INFINITY;
		LiteralNodeConversionStrategy[] strategies = LiteralNodeConversionStrategy.values();
		strategies = new LiteralNodeConversionStrategy[]{LiteralNodeConversionStrategy.DATATYPE};
		for (LiteralNodeConversionStrategy strategy : strategies) {
			EvaluatedDescription combinedSolution;
			if(partialSolutions.size() == 1){
				combinedSolution = partialSolutions.get(0).getEvaluatedDescription();
			} else {
				Set<OWLClassExpression> disjuncts = new TreeSet<OWLClassExpression>();
				
				Set<OWLIndividual> posCovered = new HashSet<OWLIndividual>();
				Set<OWLIndividual> negCovered = new HashSet<OWLIndividual>();
				
				//build the union of all class expressions
				OWLClassExpression partialDescription;
				for (EvaluatedQueryTree<String> partialSolution : partialSolutions) {
					partialDescription = partialSolution.getEvaluatedDescription().getDescription();
					disjuncts.add(partialDescription);
					posCovered.addAll(partialSolution.getTreeScore().getCoveredPositives());
					negCovered.addAll(partialSolution.getTreeScore().getCoveredNegatives());
				}
				OWLClassExpression unionDescription = df.getOWLObjectUnionOf(disjuncts);
				
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
		currentBestSolution = null;
		partialSolutions = new ArrayList<EvaluatedQueryTree<String>>();
		
		stop = false;
		isRunning = true;
		
		subMon.reset();
		lggMon.reset();
		
		bestCurrentScore = minimumTreeScore;
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
	
	@Autowired
	public void setReasoner(AbstractReasonerComponent reasoner){
		super.setReasoner(reasoner);
		loadModel();
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
	
	/**
	 * @return the treeCache
	 */
	public QueryTreeCache getTreeCache() {
		return treeCache;
	}

	private Set<OWLIndividual> asIndividuals(Collection<QueryTree<String>> trees){
		Set<OWLIndividual> individuals = new HashSet<OWLIndividual>(trees.size());
		for (QueryTree<String> queryTree : trees) {
			individuals.add(tree2Individual.get(queryTree));
		}
		return individuals;
	}
	
	private Set<QueryTree<String>> asQueryTrees(Collection<OWLIndividual> individuals){
		Set<QueryTree<String>> trees = new HashSet<QueryTree<String>>(individuals.size());
		for (OWLIndividual ind : individuals) {
			trees.add(individual2Tree.get(ind));
		}
		return trees;
	}

	/**
	 * Return all trees from the given list {@code allTrees} which are not already subsumed by {@code tree}.
	 * @param tree
	 * @param allTrees
	 * @return
	 */
	private List<QueryTree<String>> getCoveredTrees(QueryTree<String> tree, List<QueryTree<String>> trees){
		List<QueryTree<String>> coveredTrees = new ArrayList<QueryTree<String>>();
		for (QueryTree<String> queryTree : trees) {
			boolean subsumed = queryTree.isSubsumedBy(tree);
			if(subsumed){
				coveredTrees.add(queryTree);
			}
		}
		return coveredTrees;
	}

	/**
	 * Return all trees from the given list {@code allTrees} which are not already subsumed by {@code tree}.
	 * @param tree
	 * @param allTrees
	 * @return
	 */
	private List<QueryTree<String>> getUncoveredTrees(QueryTree<String> tree, List<QueryTree<String>> allTrees){
		List<QueryTree<String>> uncoveredTrees = new ArrayList<QueryTree<String>>();
		for (QueryTree<String> queryTree : allTrees) {
			boolean subsumed = queryTree.isSubsumedBy(tree);
			if(!subsumed){
				uncoveredTrees.add(queryTree);
			}
		}
		return uncoveredTrees;
	}

	private boolean sameTrees(QueryTree<String> tree1, QueryTree<String> tree2){
		return tree1.isSubsumedBy(tree2) && tree2.isSubsumedBy(tree1);
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
		return stop || todoList.isEmpty() || currentPosExampleTrees.isEmpty() || isPartialSolutionTimeExpired() || isTimeExpired();
	}
	
	private boolean isTimeExpired(){
		return maxExecutionTimeInSeconds > 0 && (System.currentTimeMillis() - startTime) / 1000d >= maxExecutionTimeInSeconds;
	}
	
	private boolean isPartialSolutionTimeExpired(){
		return maxTreeComputationTimeInSeconds <= 0 ? false : (System.currentTimeMillis() - partialSolutionStartTime)/1000d >= maxTreeComputationTimeInSeconds;
	}
	
	
	
	/**
	 * Shows the current setup of the algorithm.
	 */
	private void showSetup(){
		String setup = "Setup:";
		setup += "\n#Pos. examples:" + currentPosExamples.size();
		setup += "\n#Neg. examples:" + currentNegExamples.size();
		setup += "\nHeuristic:" + heuristic.getHeuristicType().name();
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
	 * @param maxExecutionTimeInSeconds the maxExecutionTimeInSeconds to set
	 */
	public void setMaxExecutionTimeInSeconds(int maxExecutionTimeInSeconds) {
		this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;
	}
	
	/**
	 * Default value is 1. Lower values force importance of covering positive examples.
	 * @param beta the beta to set
	 */
	public void setBeta(double beta) {
		this.beta = beta;
	}
	
	/**
	 * @param maxTreeComputationTimeInSeconds the maxTreeComputationTimeInSeconds to set
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
	public void setTreeFactory(QueryTreeFactory<String> treeFactory) {
		this.treeFactory = treeFactory;
	}
	
	public EvaluatedQueryTree<String> getBestSolution(){
		return currentPartialSolutions.last();
	}
	
	public SortedSet<EvaluatedQueryTree<String>> getSolutions(){
		return currentPartialSolutions;
	}
	
	public List<EvaluatedQueryTree<String>> getSolutionsAsList(){
		ArrayList<EvaluatedQueryTree<String>> list = new ArrayList<>(currentPartialSolutions);
		Collections.sort(list, Collections.reverseOrder());
		return list;
	}
	
	public void setAllowedNamespaces(Set<String> allowedNamespaces){
		this.allowedNamespaces = allowedNamespaces;
	}
	
	public void setIgnoredPropperties(Set<String> ignoredProperties){
		this.ignoredProperties = ignoredProperties;
	}
	
	/**
	 * @param positiveExampleTrees the positive example trees to set
	 */
	public void setPositiveExampleTrees(Map<OWLIndividual,QueryTree<String>> positiveExampleTrees) {
		this.currentPosExampleTrees = new ArrayList<>(positiveExampleTrees.values());
		this.currentPosExamples = new HashSet<OWLIndividual>(positiveExampleTrees.keySet());
		
		for (Entry<OWLIndividual, QueryTree<String>> entry : positiveExampleTrees.entrySet()) {
			OWLIndividual ind = entry.getKey();
			QueryTree<String> tree = entry.getValue();
			tree2Individual.put(tree, ind);
		}
	}
	
	/**
	 * @param negativeExampleTrees the negative example trees to set
	 */
	public void setNegativeExampleTrees(Map<OWLIndividual,QueryTree<String>> negativeExampleTrees) {
		this.currentNegExampleTrees = new ArrayList<>(negativeExampleTrees.values());
		this.currentNegExamples = new HashSet<OWLIndividual>(negativeExampleTrees.keySet());
		
		for (Entry<OWLIndividual, QueryTree<String>> entry : negativeExampleTrees.entrySet()) {
			OWLIndividual ind = entry.getKey();
			QueryTree<String> tree = entry.getValue();
			tree2Individual.put(tree, ind);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		return new QTL2Disjunctive(this);
	}
}
