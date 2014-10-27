package org.dllearner.algorithms.qtl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
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
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.QueryTreeScore;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

@ComponentAnn(name="query tree learner with noise", shortName="qtl2", version=0.8)
public class QTL2 extends AbstractCELA {
	
	private static final Logger logger = LoggerFactory.getLogger(QTL2.class);
	
	private LGGGenerator<String> lggGenerator = new LGGGeneratorImpl<String>();
	
	private Queue<EvaluatedQueryTree<String>> todoList;
	private SortedSet<EvaluatedQueryTree<String>> solutions;
	
	private double currentlyBestScore = 0d;

	private List<QueryTree<String>> currentPosExampleTrees = new ArrayList<QueryTree<String>>();
	private List<QueryTree<String>> currentNegExampleTrees = new ArrayList<QueryTree<String>>();
	
	private Map<QueryTree<String>, OWLIndividual> tree2Individual = new HashMap<QueryTree<String>, OWLIndividual>();

	private double coverageWeight = 1;
	private double specifityWeight = 0;
	
	private QueryTreeCache treeCache;

	private PosNegLP lp;

	private Model model;

	private AbstractReasonerComponent reasoner;

	private volatile boolean stop;
	private boolean isRunning;
	
	private Monitor subMon;
	private Monitor lggMon;

	private QueryExecutionFactory qef;

	private QueryTreeFactory<String> treeFactory;

	private ConciseBoundedDescriptionGenerator cbdGen;

	private Set<String> allowedNamespaces;

	private Set<String> ignoredProperties;
	
	public QTL2() {}
	
	public QTL2(PosNegLP learningProblem, AbstractReasonerComponent reasoner) throws LearningProblemUnsupportedException{
		this.lp = learningProblem;
		this.reasoner = reasoner;
	}
	
	public QTL2(PosNegLP lp, Model model) {
		this.lp = lp;
		this.model = model;
	}
	
	public QTL2(PosNegLP lp, QueryExecutionFactory qef) {
		this.lp = lp;
		this.qef = qef;
	}
	
	public void setAllowedNamespaces(Set<String> allowedNamespaces){
		this.allowedNamespaces = allowedNamespaces;
	}
	
	public void setIgnoredPropperties(Set<String> ignoredProperties){
		this.ignoredProperties = ignoredProperties;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() throws ComponentInitException {
		logger.info("Initializing...");
		if(treeFactory == null){
			treeFactory = new QueryTreeFactoryImpl();
			treeFactory.addAllowedNamespaces(allowedNamespaces);
			treeFactory.addIgnoredPropperties(ignoredProperties);
		}
		cbdGen = new ConciseBoundedDescriptionGeneratorImpl(qef);
		
		//get the query trees
		generateQueryTrees();
		
		//some logging
		subMon = MonitorFactory.getTimeMonitor("subsumption-mon");
		lggMon = MonitorFactory.getTimeMonitor("lgg-mon");
		
		//console rendering of class expressions
		ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
		ToStringRenderer.getInstance().setShortFormProvider(new SimpleShortFormProvider());
		logger.info("Initialization finished.");
	}
	
	private void generateQueryTrees(){
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
	}
	
	/**
	 * @param positiveExampleTrees the positive example trees to set
	 */
	public void setPositiveExampleTrees(Map<OWLIndividual,QueryTree<String>> positiveExampleTrees) {
		this.currentPosExampleTrees = new ArrayList<>(positiveExampleTrees.values());
		
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
		
		for (Entry<OWLIndividual, QueryTree<String>> entry : negativeExampleTrees.entrySet()) {
			OWLIndividual ind = entry.getKey();
			QueryTree<String> tree = entry.getValue();
			tree2Individual.put(tree, ind);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningAlgorithm#start()
	 */
	@Override
	public void start() {
		logger.info("Running...");
		stop = false;
		isRunning = true;
		long startTime = System.currentTimeMillis();
		currentlyBestScore = 0d;
		
		initTodoList(currentPosExampleTrees, currentNegExampleTrees);
		
		EvaluatedQueryTree<String> currentElement;
		do{
			logger.trace("TODO list size: " + todoList.size());
			
			//pick best element from todo list
			currentElement = todoList.poll();
			
			// generate the LGG between the chosen tree and each uncovered positive example
			for (QueryTree<String> example : currentElement.getFalseNegatives()) {
				QueryTree<String> tree = currentElement.getTree();
				
				// compute the LGG
				lggMon.start();
				QueryTree<String> lgg = lggGenerator.getLGG(tree, example);
				lggMon.stop();
//				tree.dump();System.out.println("++++++++++++++++++++++++++++++++");
//				example.dump();System.out.println("############################");
//				lgg.dump();System.out.println("******************************");
				
				
				// evaluate the LGG
				EvaluatedQueryTree<String> solution = evaluate(lgg, true);
				
				if(solution.getScore() >= currentlyBestScore){
					// add to todo list, if not already contained in todo list or solution list
					todo(solution);
					currentlyBestScore = solution.getScore();
				}
			}
			// once we have used the tree with each uncovered tree, we add it to the solutions list
			logger.trace("Add to solutions: " + currentElement.getEvaluatedDescription());
			solutions.add(currentElement);
			
		} while(!terminationCriteriaSatisfied());
		
		long endTime = System.currentTimeMillis();
		logger.info("Finished in " + (endTime-startTime) + "ms.");
		logger.debug("Best solution:\n" + getCurrentlyBestEvaluatedDescription());
		
		logger.trace("LGG time: " + lggMon.getTotal() + "ms");
		logger.trace("Avg. LGG time: " + lggMon.getAvg() + "ms");
		logger.trace("#LGG computations: " + lggMon.getHits());
		logger.trace("Subsumption test time: " + subMon.getTotal() + "ms");
		logger.trace("Avg. subsumption test time: " + subMon.getAvg() + "ms");
		logger.trace("#Subsumption tests: " + subMon.getHits());
	}
	
	/**
	 * Add tree to todo list if not already contained in that list or the solutions.
	 * @param solution
	 */
	private void todo(EvaluatedQueryTree<String> solution){
		//check if not already contained in todo list
		for (EvaluatedQueryTree<String> evTree : todoList) {
			// this is a workaround as we have currently no equals method for
			// trees based on the literal conversion strategy
			// boolean sameTree = sameTrees(solution.getTree(),
			// evTree.getTree());
			boolean sameTree = evTree
					.getEvaluatedDescription()
					.getDescription()
					.toString()
					.equals(solution.getEvaluatedDescription().getDescription()
							.toString());
			if (sameTree) {
				logger.warn("Not added to TODO list: Already contained in.");
				return;
			}
		}
//		//check if not already contained in todo list
//		for (EvaluatedQueryTree<String> evTree : todoList) {
//			if(sameTrees(solution.getTree(), evTree.getTree())){
//				return;
//			}
//		}
		//check if not already contained in solutions
		for (EvaluatedQueryTree<String> evTree : solutions) {
			if(sameTrees(solution.getTree(), evTree.getTree())){
				return;
			}
		}
		logger.trace("Add to TODO list:" + solution.asEvaluatedDescription());
		todoList.add(solution);
	}
	
	/**
	 * Initializes the todo list with all distinct trees contained in the given list {@code trees}.
	 * Firstly, distinct trees are computed and afterwards, for each tree a score is computed.
	 * @param trees
	 */
	private void initTodoList(List<QueryTree<String>> posExamples, List<QueryTree<String>> negExamples){
		logger.trace("Initializing TODO list ...");
		todoList = new PriorityQueue<EvaluatedQueryTree<String>>();
		solutions = new TreeSet<EvaluatedQueryTree<String>>();
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
			EvaluatedQueryTree<String> evaluatedQueryTree = evaluate(queryTree, false);
			todoList.add(evaluatedQueryTree);
		}
		logger.trace("Done. TODO list size: " + todoList.size());
	}
	
	private EvaluatedQueryTree<String> evaluate(QueryTree<String> tree, boolean useSpecifity){
		// 1. get a score for the coverage = recall oriented
		
		// compute positive examples which are not covered by LGG
		Collection<QueryTree<String>> uncoveredPositiveExampleTrees = getUncoveredTrees(tree, currentPosExampleTrees);
		Set<OWLIndividual> uncoveredPosExamples = new HashSet<OWLIndividual>();
		for (QueryTree<String> queryTree : uncoveredPositiveExampleTrees) {
			uncoveredPosExamples.add(tree2Individual.get(queryTree));
		}
		
		// compute negative examples which are covered by LGG
		Collection<QueryTree<String>> coveredNegativeExampleTrees = getCoveredTrees(tree, currentNegExampleTrees);
		Set<OWLIndividual> coveredNegExamples = new HashSet<OWLIndividual>();
		for (QueryTree<String> queryTree : coveredNegativeExampleTrees) {
			coveredNegExamples.add(tree2Individual.get(queryTree));
		}
		
		// compute score
		int coveredPositiveExamples = currentPosExampleTrees.size() - uncoveredPositiveExampleTrees.size();
		double recall = coveredPositiveExamples / (double)currentPosExampleTrees.size();
		double precision = (coveredNegativeExampleTrees.size() + coveredPositiveExamples == 0) 
						? 0 
						: coveredPositiveExamples / (double)(coveredPositiveExamples + coveredNegativeExampleTrees.size());
		
		double coverageScore = recall;//Heuristics.getFScore(recall, precision);
		
		// 2. get a score for the specifity of the query, i.e. how many edges/nodes = precision oriented
		int nrOfSpecificNodes = 0;
		for (QueryTree<String> childNode : tree.getChildrenClosure()) {
			if(!childNode.getUserObject().equals("?")){
				nrOfSpecificNodes++;
			}
		}
		double specifityScore = Math.log(nrOfSpecificNodes);
		
		//3.compute the total score
		double score = coverageWeight * coverageScore + specifityWeight * specifityScore;
		
		QueryTreeScore queryTreeScore = new QueryTreeScore(score, coverageScore, 
				Sets.difference(lp.getPositiveExamples(), uncoveredPosExamples), uncoveredPosExamples,
				coveredNegExamples, Sets.difference(lp.getNegativeExamples(), coveredNegExamples),
				specifityScore, nrOfSpecificNodes);
		
		EvaluatedQueryTree<String> evaluatedTree = new EvaluatedQueryTree<String>(tree, uncoveredPositiveExampleTrees, coveredNegativeExampleTrees, queryTreeScore);
		
		return evaluatedTree;
	}
	
	public EvaluatedQueryTree<String> getBestSolution(){
		return solutions.first();
	}
	
	public SortedSet<EvaluatedQueryTree<String>> getSolutions(){
		return solutions;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractCELA#getCurrentlyBestDescription()
	 */
	@Override
	public OWLClassExpression getCurrentlyBestDescription() {
		return getCurrentlyBestEvaluatedDescription().getDescription();
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractCELA#getCurrentlyBestEvaluatedDescription()
	 */
	@Override
	public EvaluatedDescription getCurrentlyBestEvaluatedDescription() {
		EvaluatedQueryTree<String> bestSolution = solutions.first();
		return new EvaluatedDescription(bestSolution.getTree().asOWLClassExpression(LiteralNodeConversionStrategy.MIN_MAX), new AxiomScore(bestSolution.getScore()));
	}
	
	/**
	 * @return the treeCache
	 */
	public QueryTreeCache getTreeCache() {
		return treeCache;
	}
	
	/**
	 * @param treeCache the treeCache to set
	 */
	public void setTreeCache(QueryTreeCache treeCache) {
		this.treeCache = treeCache;
	}
	
	/**
	 * @param treeFactory the treeFactory to set
	 */
	public void setTreeFactory(QueryTreeFactory<String> treeFactory) {
		this.treeFactory = treeFactory;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.StoppableLearningAlgorithm#isRunning()
	 */
	@Override
	public boolean isRunning() {
		return isRunning;
	}
	
	@Autowired
	public void setLearningProblem(PosNegLP learningProblem) {
		this.lp = learningProblem;
	}
	
	@Autowired
	public void setReasoner(AbstractReasonerComponent reasoner){
		this.reasoner = reasoner;
		model = ModelFactory.createDefaultModel();
		for (KnowledgeSource ks : reasoner.getSources()) {
			if(ks instanceof OWLFile){
				try {
					model.read(((OWLFile) ks).getURL().openStream(), null);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.StoppableLearningAlgorithm#stop()
	 */
	@Override
	public void stop() {
		stop = true;
	}

	/**
	 * Return all trees from the given list {@code allTrees} which are not already subsumed by {@code tree}.
	 * @param tree
	 * @param allTrees
	 * @return
	 */
	private Collection<QueryTree<String>> getCoveredTrees(QueryTree<String> tree, List<QueryTree<String>> trees){
		Collection<QueryTree<String>> coveredTrees = new ArrayList<QueryTree<String>>();
		for (QueryTree<String> queryTree : trees) {
			boolean subsumed = queryTree.isSubsumedBy(tree, LiteralNodeSubsumptionStrategy.DATATYPE);
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
	private Collection<QueryTree<String>> getUncoveredTrees(QueryTree<String> tree, List<QueryTree<String>> allTrees){
//		System.out.println(tree.getStringRepresentation(true));
		Collection<QueryTree<String>> uncoveredTrees = new ArrayList<QueryTree<String>>();
		for (QueryTree<String> queryTree : allTrees) {
//			System.out.println(queryTree.getStringRepresentation(false));
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
	
	private boolean terminationCriteriaSatisfied(){
		return stop || todoList.isEmpty();
	}
}
