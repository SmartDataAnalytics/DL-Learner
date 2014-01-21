package org.dllearner.algorithms.qtl.operations.lgg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.learningproblems.Heuristics;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class NoiseSensitiveLGG<N> {
	
	
	private static final Logger logger = Logger.getLogger(NoiseSensitiveLGG.class.getName());
	
	private LGGGenerator<N> lggGenerator = new LGGGeneratorImpl<N>();
	
	private Queue<EvaluatedQueryTree<N>> todoList;
	private SortedSet<EvaluatedQueryTree<N>> solutions;
	
	private double currentlyBestScore = 0d;

	private List<QueryTree<N>> posExamples;

	private List<QueryTree<N>> negExamples;

	private double coverageWeight = 0.6;
	private double specifityWeight = 0.4;
	
	public NoiseSensitiveLGG() {
	}

	public List<EvaluatedQueryTree<N>> computeLGG(List<QueryTree<N>> posExampleTrees){
		return computeLGG(posExampleTrees, Collections.<QueryTree<N>>emptyList());
	}
	
	public List<EvaluatedQueryTree<N>> computeLGG(List<QueryTree<N>> posExamples, List<QueryTree<N>> negExamples){
		this.posExamples = posExamples;
		this.negExamples = negExamples;
		
		currentlyBestScore = 0d;
		
		Monitor subMon = MonitorFactory.getTimeMonitor("subsumption-mon");
		Monitor lggMon = MonitorFactory.getTimeMonitor("lgg-mon");
		init(posExamples, negExamples);
		EvaluatedQueryTree<N> currentElement;
		do{
			logger.trace("TODO list size: " + todoList.size());
			//pick best element from todo list
			currentElement = todoList.poll();
			//generate the LGG between the chosen tree and each uncovered positive example
			for (QueryTree<N> example : currentElement.getFalseNegatives()) {
				QueryTree<N> tree = currentElement.getTree();
				//compute the LGG
				lggMon.start();
				QueryTree<N> lgg = lggGenerator.getLGG(tree, example);
				lggMon.stop();
				
				//evaluate the LGG
				EvaluatedQueryTree<N> solution = evaluate(lgg);
				
				if(solution.getScore() >= currentlyBestScore){
					//add to todo list, if not already contained in todo list or solution list
					todo(solution);
					currentlyBestScore = solution.getScore();
				}
				
			}
			solutions.add(currentElement);
//			todoList.remove(currentElement);
		} while(!terminationCriteriaSatisfied());
		logger.trace("LGG time: " + lggMon.getTotal() + "ms");
		logger.trace("Avg. LGG time: " + lggMon.getAvg() + "ms");
		logger.trace("#LGG computations: " + lggMon.getHits());
		logger.trace("Subsumption test time: " + subMon.getTotal() + "ms");
		logger.trace("Avg. subsumption test time: " + subMon.getAvg() + "ms");
		logger.trace("#Subsumption tests: " + subMon.getHits());
		return new ArrayList<EvaluatedQueryTree<N>>(solutions);
	}
	
	private EvaluatedQueryTree<N> evaluate(QueryTree<N> lgg){
		//1. get a score for the coverage = recall oriented
		//compute positive examples which are not covered by LGG
		Collection<QueryTree<N>> uncoveredPositiveExamples = getUncoveredTrees(lgg, posExamples);
		//compute negative examples which are covered by LGG
		Collection<QueryTree<N>> coveredNegativeExamples = getCoveredTrees(lgg, negExamples);
		//compute score
		int coveredPositiveExamples = posExamples.size() - uncoveredPositiveExamples.size();
		double recall = coveredPositiveExamples / (double)posExamples.size();
		double precision = (coveredNegativeExamples.size() + coveredPositiveExamples == 0) 
						? 0 
						: coveredPositiveExamples / (double)(coveredPositiveExamples + coveredNegativeExamples.size());
		
		double coverageScore = recall;//Heuristics.getFScore(recall, precision);
		
		//2. get a score for the specifity of the query, i.e. how many edges/nodes = precision oriented
		int numberOfSpecificNodes = 0;
		for (QueryTree<N> childNode : lgg.getChildrenClosure()) {
			if(!childNode.getUserObject().equals("?")){
				numberOfSpecificNodes++;
			}
		}
		double specifityScore = Math.log(numberOfSpecificNodes);
		
		//3.compute the total score
		double score = coverageWeight * coverageScore + specifityWeight * specifityScore;
		
		EvaluatedQueryTree<N> solution = new EvaluatedQueryTree<N>(lgg, uncoveredPositiveExamples, coveredNegativeExamples, score);
		
		return solution;
	}
	
	/**
	 * Return all trees from the given list {@code allTrees} which are not already subsumed by {@code tree}.
	 * @param tree
	 * @param allTrees
	 * @return
	 */
	private Collection<QueryTree<N>> getUncoveredTrees(QueryTree<N> tree, List<QueryTree<N>> allTrees){
		Collection<QueryTree<N>> uncoveredTrees = new ArrayList<QueryTree<N>>();
		for (QueryTree<N> queryTree : allTrees) {
			boolean subsumed = queryTree.isSubsumedBy(tree);
			if(!subsumed){
				uncoveredTrees.add(queryTree);
			}
		}
		return uncoveredTrees;
	}
	
	/**
	 * Return all trees from the given list {@code allTrees} which are not already subsumed by {@code tree}.
	 * @param tree
	 * @param allTrees
	 * @return
	 */
	private Collection<QueryTree<N>> getCoveredTrees(QueryTree<N> tree, List<QueryTree<N>> trees){
		Collection<QueryTree<N>> coveredTrees = new ArrayList<QueryTree<N>>();
		for (QueryTree<N> queryTree : trees) {
			boolean subsumed = queryTree.isSubsumedBy(tree);
			if(subsumed){
				coveredTrees.add(queryTree);
			}
		}
		return coveredTrees;
	}
	
	/**
	 * Initializes the todo list with all distinct trees contained in the given list {@code trees}.
	 * Firstly, distinct trees are computed and afterwards, for each tree a score is computed.
	 * @param trees
	 */
	private void init(List<QueryTree<N>> posExamples, List<QueryTree<N>> negExamples){
		todoList = new PriorityQueue<EvaluatedQueryTree<N>>();
		solutions = new TreeSet<EvaluatedQueryTree<N>>();
//		EvaluatedQueryTree<N> dummy = new EvaluatedQueryTree<N>(new QueryTreeImpl<N>((N)"TOP"), trees, 0d);
//		todoList.add(dummy);
		//compute distinct trees
		Collection<QueryTree<N>> distinctTrees = new ArrayList<QueryTree<N>>();
		for (QueryTree<N> queryTree : posExamples) {//System.out.println(queryTree.getStringRepresentation());
			boolean distinct = true;
			for (QueryTree<N> otherTree : distinctTrees) {
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
		for (QueryTree<N> queryTree : distinctTrees) {
			//compute positive examples which are not covered by LGG
			Collection<QueryTree<N>> uncoveredPositiveExamples = getUncoveredTrees(queryTree, posExamples);
			//compute negative examples which are covered by LGG
			Collection<QueryTree<N>> coveredNegativeExamples = getCoveredTrees(queryTree, negExamples);
			//compute score
			int coveredPositiveExamples = posExamples.size() - uncoveredPositiveExamples.size();
			double recall = coveredPositiveExamples / (double)posExamples.size();
			double precision = (coveredNegativeExamples.size() + coveredPositiveExamples == 0) 
							? 0 
							: coveredPositiveExamples / (double)(coveredPositiveExamples + coveredNegativeExamples.size());
			
			double score = Heuristics.getFScore(recall, precision);
			todoList.add(new EvaluatedQueryTree<N>(queryTree, uncoveredPositiveExamples, coveredNegativeExamples, score));
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
	
	

}
