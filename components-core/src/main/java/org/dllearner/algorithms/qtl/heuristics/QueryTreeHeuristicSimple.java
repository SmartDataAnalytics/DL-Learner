/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
 * A simple heuristic based which just takes the accuracy into account.
 * @author Lorenz Buehmann
 *
 */
@ComponentAnn(name = "QueryTreeHeuristic", shortName = "qtree_heuristic_simple", version = 0.1)
public class QueryTreeHeuristicSimple extends QueryTreeHeuristic {
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() throws ComponentInitException {
		initialized = true;
	}
	
	@Override
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
			case MATTHEWS_CORRELATION  : // a measure between -1 and 1
				double denominator = Math.sqrt((tp + fp) * (tp + fn) * (tn + fp) * (tn + fn));
				if(denominator == 0) { // 0 means not better than random prediction
					return 0;
//					denominator = 1;
				}
				score = (tp * tn - fp * fn) / denominator;break;
			case YOUDEN_INDEX : score = tp / (tp + fn) + tn / (fp + tn) - 1;break;
		default:
			break;
			
		}
		
		return score;
	}
}
