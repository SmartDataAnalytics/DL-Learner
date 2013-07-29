package org.dllearner.algorithms.qtl.operations.lgg;

import java.util.ArrayList;
import java.util.Collection;
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
	
	public NoiseSensitiveLGG() {
	}

	public List<EvaluatedQueryTree<N>> computeLGG(List<QueryTree<N>> trees){
		Monitor subMon = MonitorFactory.getTimeMonitor("subsumption-mon");
		Monitor lggMon = MonitorFactory.getTimeMonitor("lgg-mon");
		init(trees);
		EvaluatedQueryTree<N> currentElement;
		do{
			logger.trace("TODO list size: " + todoList.size());
			//pick best element from todo list
			currentElement = todoList.poll();
			for (QueryTree<N> example : currentElement.getUncoveredExamples()) {
				QueryTree<N> tree = currentElement.getTree();
				//compute the LGG
				lggMon.start();
				QueryTree<N> lgg = lggGenerator.getLGG(tree, example);
				lggMon.stop();
				//compute examples which are not covered by LGG
				Collection<QueryTree<N>> uncoveredExamples = getUncoveredTrees(lgg, trees);
				//compute score
				double score = Heuristics.getConfidenceInterval95WaldAverage(trees.size(), trees.size() - uncoveredExamples.size());
				//add to todo list, if not already contained in todo list or solution list
				EvaluatedQueryTree<N> solution = new EvaluatedQueryTree<N>(lgg, uncoveredExamples, score);
				todo(solution);
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
	
	private void init(List<QueryTree<N>> trees){
		todoList = new PriorityQueue<EvaluatedQueryTree<N>>();
		solutions = new TreeSet<EvaluatedQueryTree<N>>();
//		EvaluatedQueryTree<N> dummy = new EvaluatedQueryTree<N>(new QueryTreeImpl<N>((N)"TOP"), trees, 0d);
//		todoList.add(dummy);
		//compute distinct trees
		Collection<QueryTree<N>> distinctTrees = new ArrayList<QueryTree<N>>();
		for (QueryTree<N> queryTree : trees) {//System.out.println(queryTree.getStringRepresentation());
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
			Collection<QueryTree<N>> uncoveredExamples = getUncoveredTrees(queryTree, trees);
			double score = Heuristics.getConfidenceInterval95WaldAverage(trees.size(), trees.size() - uncoveredExamples.size());
			todoList.add(new EvaluatedQueryTree<N>(queryTree, uncoveredExamples, score));
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
