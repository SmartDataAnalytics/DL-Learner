/**
 * 
 */
package org.dllearner.algorithms.qtl.heuristics;

import java.util.Comparator;
import java.util.Set;

import org.dllearner.algorithms.qtl.datastructures.impl.EvaluatedRDFResourceTree;
import org.dllearner.core.AbstractComponent;
import org.dllearner.core.Heuristic;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.learningproblems.Heuristics.HeuristicType;
import org.dllearner.learningproblems.QueryTreeScore;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * @author Lorenz Buehmann
 *
 */
public abstract class QueryTreeHeuristicNew extends AbstractComponent implements Heuristic,
		Comparator<EvaluatedRDFResourceTree> {

	protected double posExamplesWeight = 1.0;
	protected HeuristicType heuristicType = HeuristicType.PRED_ACC;

	public QueryTreeHeuristicNew() {
		super();
	}

	public abstract double getScore(EvaluatedRDFResourceTree tree);

	/**
	 * @param posExamplesWeight the posExamplesWeight to set
	 */
	public void setPosExamplesWeight(double posExamplesWeight) {
		this.posExamplesWeight = posExamplesWeight;
	}
	
	protected double getAccuracy(EvaluatedRDFResourceTree tree) {
		QueryTreeScore treeScore = tree.getTreeScore();

		Set<OWLIndividual> truePositives = treeScore.getCoveredPositives();
		Set<OWLIndividual> trueNegatives = treeScore.getNotCoveredNegatives();
		Set<OWLIndividual> falsePositives = treeScore.getNotCoveredPositives();
		Set<OWLIndividual> falseNegatives = treeScore.getCoveredNegatives();

		double tp = truePositives.size();
		double tn = trueNegatives.size();
		double fp = falsePositives.size();
		double fn = falseNegatives.size();

		double accuracy = 0;
		switch (heuristicType) {
		case FMEASURE:
			accuracy = Heuristics.getFScore(tp / (tp + fn), tp / (tp + fp), posExamplesWeight);
			break;
		case PRED_ACC:
			accuracy = (1 / posExamplesWeight * tp + tn) / (1 / posExamplesWeight * (tp + fn) + (tn + fp));
			break;
		case ENTROPY: {
			double total = tp + fn;
			double pp = tp / total;
			double pn = fn / total;
			accuracy = pp * Math.log(pp) + pn * Math.log(pn);
			break;
		}
		case MATTHEWS_CORRELATION:
			accuracy = (tp * tn - fp * fn) / Math.sqrt((tp + fp) * (tp + fn) * (tn + fp) * (tn + fn));
			break;
		case YOUDEN_INDEX:
			accuracy = tp / (tp + fn) + tn / (fp + tn) - 1;
			break;
		default:
			break;

		}
		return accuracy;
	}

	/**
	 * Returns the maximum achievable score according to the used score
	 * function.
	 * 
	 * @return
	 */
	public double getMaximumAchievableScore(EvaluatedRDFResourceTree tree) {
		QueryTreeScore treeScore = tree.getTreeScore();

		Set<OWLIndividual> truePositives = treeScore.getCoveredPositives();
		Set<OWLIndividual> trueNegatives = treeScore.getNotCoveredNegatives();
		Set<OWLIndividual> falsePositives = treeScore.getCoveredNegatives();
		Set<OWLIndividual> falseNegatives = treeScore.getNotCoveredPositives();

		double tp = truePositives.size();
		double tn = trueNegatives.size();
		double fp = falsePositives.size();
		double fn = falseNegatives.size();

		return getMaximumAchievableScore(tp, tn, fp, fn);
	}

	/**
	 * Returns the maximum achievable accuracy score according to the used score
	 * function.
	 * The idea is as follows:
	 * For algorithms which make the current solution more general, we know that
	 * 1. all already covered positive examples remain covered
	 * 2. all already covered negative examples remain covered
	 * 3. uncovered positive examples might be covered by more general solutions
	 * 4. uncovered negative examples might be covered by more general solutions
	 * That means, in the optimal case we get a solution which covers all
	 * uncovered positive examples, but non of the uncovered negative examples.
	 * 
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
			mas = Double.POSITIVE_INFINITY;
			break;
		case PRED_ACC: // (tn + tp) / (tn + fp + fn + tp) -> (tn + (tp + fn)) / (tn + fp + fn + tp)
			//			mas = (posExamplesWeight * tp + tn - fp) / (posExamplesWeight * (tp + fn) + tn + fp);
			mas = (posExamplesWeight * (tp + fn) + tn) / (posExamplesWeight * (tp + fn) + tn + fp);
			break;
		case ENTROPY:
			mas = Double.POSITIVE_INFINITY;
			break;
		case MATTHEWS_CORRELATION:
			mas = Double.POSITIVE_INFINITY;
			break;
		case YOUDEN_INDEX:
			mas = Double.POSITIVE_INFINITY;
			break;
		default:
			break;

		}
		return mas;
	}

	@Override
	public int compare(EvaluatedRDFResourceTree tree1, EvaluatedRDFResourceTree tree2) {
		double diff = getScore(tree1) - getScore(tree2);

		if (diff > 0) {
			return 1;
		} else if (diff < 0) {
			return -1;
		} else {
			return tree1.asEvaluatedDescription().getDescription()
					.compareTo(tree2.asEvaluatedDescription().getDescription());
		}
	}

	/**
	 * @param heuristicType the type of accuracy measure to set
	 */
	public void setHeuristicType(HeuristicType heuristicType) {
		this.heuristicType = heuristicType;
	}

	/**
	 * @return the heuristicType
	 */
	public HeuristicType getHeuristicType() {
		return heuristicType;
	}

}