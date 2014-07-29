package org.dllearner.algorithms.qtl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.qtl.cache.QueryTreeCache;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl.LiteralNodeConversionStrategy;
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
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.AxiomScore;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.QueryTreeScore;
import org.dllearner.utilities.owl.DLLearnerDescriptionConvertVisitor;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

@ComponentAnn(name="query tree learner with noise", shortName="qtl2", version=0.8)
public class QTL2 extends AbstractCELA {
	
	
	private static final Logger logger = Logger.getLogger(QTL2.class.getName());
	
	private LGGGenerator<String> lggGenerator = new LGGGeneratorImpl<String>();
	
	private Queue<EvaluatedQueryTree<String>> todoList;
	private SortedSet<EvaluatedQueryTree<String>> solutions;
	
	private double currentlyBestScore = 0d;

	private List<QueryTree<String>> currentPosExampleTrees;
	private List<QueryTree<String>> currentNegExampleTrees;
	
	private Map<QueryTree<String>, Individual> tree2Indivual;

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
	
	public QTL2() {}
	
	public QTL2(PosNegLP learningProblem, AbstractReasonerComponent reasoner) throws LearningProblemUnsupportedException{
		this.lp = learningProblem;
		this.reasoner = reasoner;
		
	}
	
	public QTL2(PosNegLP lp, Model model) {
		this.lp = lp;
		this.model = model;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() throws ComponentInitException {
		logger.info("Initializing...");
		treeCache = new QueryTreeCache(model);
		tree2Indivual = new HashMap<QueryTree<String>, Individual>(lp.getPositiveExamples().size()+lp.getNegativeExamples().size());
		
		currentPosExampleTrees = new ArrayList<QueryTree<String>>(lp.getPositiveExamples().size());
		currentNegExampleTrees = new ArrayList<QueryTree<String>>(lp.getNegativeExamples().size());
		
		//get the query trees
		QueryTree<String> queryTree;
		for (Individual ind : lp.getPositiveExamples()) {
			queryTree = treeCache.getQueryTree(ind.getName());
			tree2Indivual.put(queryTree, ind);
			currentPosExampleTrees.add(queryTree);
		}
		for (Individual ind : lp.getNegativeExamples()) {
			queryTree = treeCache.getQueryTree(ind.getName());
			tree2Indivual.put(queryTree, ind);
			currentNegExampleTrees.add(treeCache.getQueryTree(ind.getName()));
		}
		
		//some logging
		subMon = MonitorFactory.getTimeMonitor("subsumption-mon");
		lggMon = MonitorFactory.getTimeMonitor("lgg-mon");
		
		//console rendering of class expressions
		ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
		ToStringRenderer.getInstance().setShortFormProvider(new SimpleShortFormProvider());
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
			//generate the LGG between the chosen tree and each uncovered positive example
			for (QueryTree<String> example : currentElement.getFalseNegatives()) {
				QueryTree<String> tree = currentElement.getTree();
				//compute the LGG
				lggMon.start();
				QueryTree<String> lgg = lggGenerator.getLGG(tree, example);
				lggMon.stop();
//				tree.dump();System.out.println("++++++++++++++++++++++++++++++++");
//				example.dump();System.out.println("############################");
//				lgg.dump();System.out.println("******************************");
				
				
				//evaluate the LGG
				EvaluatedQueryTree<String> solution = evaluate(lgg, true);
				
				if(solution.getScore() >= currentlyBestScore){
					//add to todo list, if not already contained in todo list or solution list
					todo(solution);
					currentlyBestScore = solution.getScore();
				}
				
			}
			solutions.add(currentElement);
//			todoList.remove(currentElement);
		} while(!terminationCriteriaSatisfied());
		long endTime = System.currentTimeMillis();
		logger.info("Finished in " + (endTime-startTime) + "ms.");
		logger.info("Best solution:\n" + getCurrentlyBestEvaluatedDescription());
		
		logger.trace("LGG time: " + lggMon.getTotal() + "ms");
		logger.trace("Avg. LGG time: " + lggMon.getAvg() + "ms");
		logger.trace("#LGG computations: " + lggMon.getHits());
		logger.trace("Subsumption test time: " + subMon.getTotal() + "ms");
		logger.trace("Avg. subsumption test time: " + subMon.getAvg() + "ms");
		logger.trace("#Subsumption tests: " + subMon.getHits());
	}
	
	public EvaluatedQueryTree<String> getBestSolution(){
		return solutions.first();
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractCELA#getCurrentlyBestDescription()
	 */
	@Override
	public Description getCurrentlyBestDescription() {
		return getCurrentlyBestEvaluatedDescription().getDescription();
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractCELA#getCurrentlyBestEvaluatedDescription()
	 */
	@Override
	public EvaluatedDescription getCurrentlyBestEvaluatedDescription() {
		EvaluatedQueryTree<String> bestSolution = solutions.first();
		Description description = DLLearnerDescriptionConvertVisitor.getDLLearnerDescription(
				bestSolution.getTree().asOWLClassExpression(LiteralNodeConversionStrategy.MIN_MAX));
		return new EvaluatedDescription(description, new AxiomScore(bestSolution.getScore()));
	}
	
	/**
	 * @return the treeCache
	 */
	public QueryTreeCache getTreeCache() {
		return treeCache;
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

	private EvaluatedQueryTree<String> evaluate(QueryTree<String> tree, boolean useSpecifity){
		//1. get a score for the coverage = recall oriented
		//compute positive examples which are not covered by LGG
		Collection<QueryTree<String>> uncoveredPositiveExampleTrees = getUncoveredTrees(tree, currentPosExampleTrees);
		Set<Individual> uncoveredPosExamples = new HashSet<Individual>();
		for (QueryTree<String> queryTree : uncoveredPositiveExampleTrees) {
			uncoveredPosExamples.add(tree2Indivual.get(queryTree));
		}
		//compute negative examples which are covered by LGG
		Collection<QueryTree<String>> coveredNegativeExampleTrees = getCoveredTrees(tree, currentNegExampleTrees);
		Set<Individual> coveredNegExamples = new HashSet<Individual>();
		for (QueryTree<String> queryTree : coveredNegativeExampleTrees) {
			coveredNegExamples.add(tree2Indivual.get(queryTree));
		}
		//compute score
		int coveredPositiveExamples = currentPosExampleTrees.size() - uncoveredPositiveExampleTrees.size();
		double recall = coveredPositiveExamples / (double)currentPosExampleTrees.size();
		double precision = (coveredNegativeExampleTrees.size() + coveredPositiveExamples == 0) 
						? 0 
						: coveredPositiveExamples / (double)(coveredPositiveExamples + coveredNegativeExampleTrees.size());
		
		double coverageScore = recall;//Heuristics.getFScore(recall, precision);
		
		//2. get a score for the specifity of the query, i.e. how many edges/nodes = precision oriented
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
				uncoveredPosExamples, Sets.difference(lp.getPositiveExamples(), uncoveredPosExamples),
				coveredNegExamples, Sets.difference(lp.getNegativeExamples(), coveredNegExamples),
				specifityScore, nrOfSpecificNodes);
		
		EvaluatedQueryTree<String> evaluatedTree = new EvaluatedQueryTree<String>(tree, uncoveredPositiveExampleTrees, coveredNegativeExampleTrees, queryTreeScore);
		
		return evaluatedTree;
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
	private Collection<QueryTree<String>> getUncoveredTrees(QueryTree<String> tree, List<QueryTree<String>> allTrees){
		Collection<QueryTree<String>> uncoveredTrees = new ArrayList<QueryTree<String>>();
		for (QueryTree<String> queryTree : allTrees) {
			boolean subsumed = queryTree.isSubsumedBy(tree);
			if(!subsumed){
				uncoveredTrees.add(queryTree);
			}
		}
		return uncoveredTrees;
	}

	/**
	 * Initializes the todo list with all distinct trees contained in the given list {@code trees}.
	 * Firstly, distinct trees are computed and afterwards, for each tree a score is computed.
	 * @param trees
	 */
	private void initTodoList(List<QueryTree<String>> posExamples, List<QueryTree<String>> negExamples){
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
	}

	private boolean sameTrees(QueryTree<String> tree1, QueryTree<String> tree2){
		return tree1.isSubsumedBy(tree2) && tree2.isSubsumedBy(tree1);
	}
	
	private boolean terminationCriteriaSatisfied(){
		return stop || todoList.isEmpty();
	}
	
	/**
	 * Add tree to todo list if not already contained in that list or the solutions.
	 * @param solution
	 */
	private void todo(EvaluatedQueryTree<String> solution){
		//check if not already contained in todo list
		for (EvaluatedQueryTree<String> evTree : todoList) {
			if(sameTrees(solution.getTree(), evTree.getTree())){
				return;
			}
		}
		//check if not already contained in solutions
		for (EvaluatedQueryTree<String> evTree : solutions) {
			if(sameTrees(solution.getTree(), evTree.getTree())){
				return;
			}
		}
		todoList.add(solution);
	}
	
	

}
