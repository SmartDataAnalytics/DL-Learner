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
import org.dllearner.learningproblems.Heuristics.HeuristicType;
import org.dllearner.learningproblems.QueryTreeScore;
import org.dllearner.utilities.owl.ConceptComparator;

/**
 * @author Lorenz Buehmann
 *
 */
@ComponentAnn(name = "QueryTreeHeuristic", shortName = "qtree_heuristic", version = 0.1)
public class QueryTreeHeuristic extends AbstractComponent implements Heuristic, Comparator<EvaluatedQueryTree<String>>{
	
	private HeuristicType heuristicType = HeuristicType.PRED_ACC;
	
	// F score beta value
	private double coverageBeta = 1;
	
	private double coverageWeight = 0.8;
	
	private double specifityWeight = 0.1;
	
	private double posExamplesWeight = 1;
	
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
		
		Set<Individual> truePositives = treeScore.getCoveredPositives();
		Set<Individual> trueNegatives = treeScore.getNotCoveredNegatives();
		Set<Individual> falsePositives = treeScore.getNotCoveredPositives();
		Set<Individual> falseNegatives = treeScore.getCoveredNegatives();
		
		double tp = truePositives.size();
		double tn = trueNegatives.size();
		double fp = falsePositives.size();
		double fn = falseNegatives.size();
		
		double score = 0;
		switch(heuristicType){
			case FMEASURE : 
				score = Heuristics.getFScore(tp/(tp+fn), tp/(tp+fp), posExamplesWeight);break;
			case PRED_ACC : 
				score = (posExamplesWeight * tp + tn) / (posExamplesWeight * (tp + fn) + tn + fp);break;
			case ENTROPY  :{
				double total = tp + fn;
				double pp = tp / total;
				double pn = fn / total;
				score = pp * Math.log(pp) + pn * Math.log(pn);
				break;}
			case MATTHEWS_CORRELATION  :
				score = (tp * tn - fp * fn) / Math.sqrt((tp + fp) * (tp + fn) * (tn + fp) * (tn + fn));break;
			case YOUDEN_INDEX : score = tp / (tp + fn) + tn / (fp + tn) - 1;break;
		default:
			break;
			
		}
		
		return score;
	}
	
	/**
	 * Returns the maximum achievable score according to the used score function.
	 * @return
	 */
	public double getMaximumAchievableScore(EvaluatedQueryTree<String> tree) {
		QueryTreeScore treeScore = tree.getTreeScore();
		
		Set<Individual> truePositives = treeScore.getCoveredPositives();
		Set<Individual> trueNegatives = treeScore.getNotCoveredNegatives();
		Set<Individual> falsePositives = treeScore.getNotCoveredPositives();
		Set<Individual> falseNegatives = treeScore.getCoveredNegatives();
		
		double tp = truePositives.size();
		double tn = trueNegatives.size();
		double fp = falsePositives.size();
		double fn = falseNegatives.size();
		
		return getMaximumAchievableScore(tp, tn, fp, fn);
	}
	
	/**
	 * Returns the maximum achievable score according to the used score function.
	 * @param tp
	 * @param tn
	 * @param fp
	 * @param fn
	 * @return
	 */
	private double getMaximumAchievableScore(double tp, double tn, double fp, double fn) {
		double mas = 0d;
		switch (heuristicType) {
		case FMEASURE:
			break;
		case PRED_ACC:
			mas = (posExamplesWeight * tp + tn - fp) / (posExamplesWeight * (tp + fn) + tn + fp);
			break;
		case ENTROPY:
			break;
		case MATTHEWS_CORRELATION:
			break;
		case YOUDEN_INDEX:
			break;
		default:
			break;

		}
		return mas;
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
	
	/**
	 * @param heuristicType the heuristicType to set
	 */
	public void setHeuristicType(HeuristicType heuristicType) {
		this.heuristicType = heuristicType;
	}
	
	/**
	 * @param posExamplesWeight the posExamplesWeight to set
	 */
	public void setPosExamplesWeight(double posExamplesWeight) {
		this.posExamplesWeight = posExamplesWeight;
	}

}
