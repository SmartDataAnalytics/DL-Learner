/**
 * 
 */
package org.dllearner.algorithms.qtl;

import java.util.Comparator;
import java.util.Set;

import org.dllearner.algorithms.qtl.operations.lgg.EvaluatedQueryTree;
import org.dllearner.core.AbstractComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.Heuristic;
import org.dllearner.core.owl.Individual;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.learningproblems.QueryTreeScore;
import org.dllearner.utilities.owl.ConceptComparator;

/**
 * @author Lorenz Buehmann
 *
 */
@ComponentAnn(name = "QueryTreeHeuristic", shortName = "qtree_heuristic", version = 0.1)
public class QueryTreeHeuristic extends AbstractComponent implements Heuristic, Comparator<EvaluatedQueryTree<String>>{
	
	// F score beta value
	private double coverageBeta = 1;
	
	private double coverageWeight = 0.8;
	
	private double specifityWeight = 0.1;
	
	// syntactic comparison as final comparison criterion
	private ConceptComparator conceptComparator = new ConceptComparator();

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() throws ComponentInitException {
	}
	
	public double getScore(EvaluatedQueryTree<String> tree){
		QueryTreeScore treeScore = tree.getTreeScore();
		
		//TODO
		double score = treeScore.getScore();
		
		return score;
	}
	
	private double getPredictedAccuracy(EvaluatedQueryTree<String> tree){
		QueryTreeScore treeScore = tree.getTreeScore();
		
		Set<Individual> truePositives = treeScore.getCoveredPositives();
		Set<Individual> trueNegatives = treeScore.getNotCoveredNegatives();
		Set<Individual> falsePositives = treeScore.getNotCoveredPositives();
		Set<Individual> falseNegatives = treeScore.getCoveredNegatives();
		return 0;
		
	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(EvaluatedQueryTree<String> tree1, EvaluatedQueryTree<String> tree2) {
		double diff = getScore(tree1) - getScore(tree2);
		
		if (diff > 0) {
			return 1;
		} else if (diff < 0) {
			return -1;
		} else {
			return conceptComparator.compare(tree1.asEvaluatedDescription().getDescription(), tree2.asEvaluatedDescription().getDescription());
		}
	}

}
