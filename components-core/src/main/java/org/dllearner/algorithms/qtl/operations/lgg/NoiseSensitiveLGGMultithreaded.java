package org.dllearner.algorithms.qtl.operations.lgg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

import org.dllearner.algorithms.qtl.cache.QueryTreeCache;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.learningproblems.Heuristics;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.rdf.model.Model;

public class NoiseSensitiveLGGMultithreaded<N> {
	
	private LGGGenerator<N> lggGenerator = new LGGGeneratorImpl<N>();
	
	private BlockingQueue<EvaluatedQueryTree<N>> todoList;
	private SortedSet<EvaluatedQueryTree<N>> solutions;

	private List<QueryTree<N>> trees;
	
	public NoiseSensitiveLGGMultithreaded() {
	}

	public List<EvaluatedQueryTree<N>> computeLGG(List<QueryTree<N>> trees) {
		this.trees = trees;
		ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
		init(trees);
		EvaluatedQueryTree<N> currentElement;
		do {
			// pick best element from todo list
			try {
				currentElement = todoList.take();
				threadPool.execute(new QueryTreeProcessor(currentElement));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (!terminationCriteriaSatisfied());
		threadPool.shutdown();
		while (!threadPool.isTerminated()) {

		}
		return new ArrayList<EvaluatedQueryTree<N>>(solutions);
	}
	
	class QueryTreeProcessor implements Runnable{
		private EvaluatedQueryTree<N> evaluatedQueryTree;

		public QueryTreeProcessor(EvaluatedQueryTree<N> evaluatedQueryTree) {
			this.evaluatedQueryTree = evaluatedQueryTree;
		}

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			for (QueryTree<N> example : evaluatedQueryTree.getFalseNegatives()) {
				QueryTree<N> tree = evaluatedQueryTree.getTree();
				//compute the LGG
				QueryTree<N> lgg = lggGenerator.getLGG(tree, example);
				//compute examples which are not covered by LGG
				Collection<QueryTree<N>> uncoveredExamples = new ArrayList<QueryTree<N>>();
				for (QueryTree<N> queryTree : trees) {
					boolean subsumed = queryTree.isSubsumedBy(lgg);
					if(!subsumed){
						uncoveredExamples.add(queryTree);
					}
				}
				//compute score
				double score = Heuristics.getConfidenceInterval95WaldAverage(trees.size(), trees.size() - uncoveredExamples.size());
				//add to todo list, if not already contained in todo list or solution list
				EvaluatedQueryTree<N> solution = new EvaluatedQueryTree<N>(lgg, uncoveredExamples, null, score);
				todo(solution);
			}
			solutions.add(evaluatedQueryTree);
		}
	}
	
	private void init(List<QueryTree<N>> trees){
		todoList = new PriorityBlockingQueue<EvaluatedQueryTree<N>>();
		solutions = new TreeSet<EvaluatedQueryTree<N>>();
//		EvaluatedQueryTree<N> dummy = new EvaluatedQueryTree<N>(new QueryTreeImpl<N>((N)"TOP"), trees, 0d);
//		todoList.add(dummy);
		//compute distinct trees
		Collection<QueryTree<N>> distinctTrees = new ArrayList<QueryTree<N>>();
		for (QueryTree<N> queryTree : trees) {//System.out.println(queryTree.getStringRepresentation());
			boolean distinct = true;
			for (QueryTree<N> otherTree : distinctTrees) {
				if(queryTree.isSubsumedBy(otherTree)){
					distinct = false;
					break;
				}
			}
			if(distinct){
				distinctTrees.add(queryTree);
			}
		}
		for (QueryTree<N> queryTree : distinctTrees) {
			Collection<QueryTree<N>> uncoveredExamples = new ArrayList<QueryTree<N>>(distinctTrees);
			uncoveredExamples.remove(queryTree);
			double score = (trees.size() - uncoveredExamples.size()) / (double)trees.size();
			todoList.add(new EvaluatedQueryTree<N>(queryTree, uncoveredExamples, null, score));
		}
	}
	
	/**
	 * Add tree to todo list if not already contained in that list or the solutions.
	 * @param solution
	 */
	private void todo(EvaluatedQueryTree<N> solution){
		//check if not already contained in todo list
		for (EvaluatedQueryTree<N> evTree : todoList) {
			if(sameTrees(solution.getTree(), evTree.getTree())){
				return;
			}
		}
		//check if not already contained in solutions
		for (EvaluatedQueryTree<N> evTree : solutions) {
			if(sameTrees(solution.getTree(), evTree.getTree())){
				return;
			}
		}
		todoList.add(solution);
	}
	
	private boolean sameTrees(QueryTree<N> tree1, QueryTree<N> tree2){
		return tree1.isSubsumedBy(tree2) && tree2.isSubsumedBy(tree1);
	}
	
	private boolean terminationCriteriaSatisfied(){
		return todoList.isEmpty();
	}
	
	
	public static void main(String[] args) throws Exception {
		NoiseSensitiveLGGMultithreaded<String> lggGen = new NoiseSensitiveLGGMultithreaded<String>();
		
		List<QueryTree<String>> trees = new ArrayList<QueryTree<String>>();
		QueryTree<String> tree;
		Model model;
		ConciseBoundedDescriptionGenerator cbdGenerator = new ConciseBoundedDescriptionGeneratorImpl(SparqlEndpoint.getEndpointDBpedia(), "cache");
		cbdGenerator.setRecursionDepth(3);
		QueryTreeCache treeCache = new QueryTreeCache();
		List<String> resources = Lists.newArrayList("http://dbpedia.org/resource/Leipzig", "http://dbpedia.org/resource/Dresden");
		for(String resource : resources){
			try {
				System.out.println(resource);
				model = cbdGenerator.getConciseBoundedDescription(resource);
				tree = treeCache.getQueryTree(resource, model);
				System.out.println(tree.getStringRepresentation());
				trees.add(tree);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		lggGen.computeLGG(trees);
	}
	

}
