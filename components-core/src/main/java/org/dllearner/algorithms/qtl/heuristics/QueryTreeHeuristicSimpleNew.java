/**
 * 
 */
package org.dllearner.algorithms.qtl.heuristics;

import java.util.Set;

import org.dllearner.algorithms.qtl.datastructures.impl.EvaluatedRDFResourceTree;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.learningproblems.Heuristics;
import org.dllearner.learningproblems.QueryTreeScore;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * @author Lorenz Buehmann
 *
 */
@ComponentAnn(name = "QueryTreeHeuristic", shortName = "qtree_heuristic", version = 0.1)
public class QueryTreeHeuristicSimpleNew extends QueryTreeHeuristicNew {
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() throws ComponentInitException {
	}
	
	public double getScore(EvaluatedRDFResourceTree tree){
		QueryTreeScore treeScore = tree.getTreeScore();
		
		Set<OWLIndividual> truePositives = treeScore.getCoveredPositives();
		Set<OWLIndividual> trueNegatives = treeScore.getNotCoveredNegatives();
		Set<OWLIndividual> falsePositives = treeScore.getNotCoveredPositives();
		Set<OWLIndividual> falseNegatives = treeScore.getCoveredNegatives();
		
		double tp = truePositives.size();
		double tn = trueNegatives.size();
		double fp = falsePositives.size();
		double fn = falseNegatives.size();
		
		double score = 0;
		switch(heuristicType){
			case FMEASURE : 
				score = Heuristics.getFScore(tp/(tp+fn), tp/(tp+fp), posExamplesWeight);break;
			case PRED_ACC : 
				score = (1/posExamplesWeight * tp + tn) / (1/posExamplesWeight * (tp + fn) + (tn + fp));break;
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
}
