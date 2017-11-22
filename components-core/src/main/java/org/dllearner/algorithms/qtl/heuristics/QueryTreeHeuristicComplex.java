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
/**
 *
 */
package org.dllearner.algorithms.qtl.heuristics;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.EvaluatedRDFResourceTree;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.learningproblems.QueryTreeScore;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;

/**
 * @author Lorenz Buehmann
 *
 */
@ComponentAnn(name = "QueryTreeHeuristicC", shortName = "qtree_heuristic_complex", version = 0.1)
public class QueryTreeHeuristicComplex extends QueryTreeHeuristic {
	
	
	private double resultSetSizePenalty = 0.0001;

	private QueryExecutionFactory qef;

	public QueryTreeHeuristicComplex(QueryExecutionFactory qef) {
		this.qef = qef;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() throws ComponentInitException {
		initialized = true;
	}

	@Override
	public double getScore(EvaluatedRDFResourceTree tree) {
		QueryTreeScore treeScore = tree.getTreeScore();
		
		// accuracy as baseline
		double score = getAccuracy(tree);

		// distance penalty
		score -= treeScore.getDistancePenalty();
		
		// result set weight
		int resultCount = getResultCount(tree);

		return score;
	}

	private int getResultCount(EvaluatedRDFResourceTree evaluatedQueryTree) {
		int cnt = 0;
		String query = QueryTreeUtils.toSPARQLQueryString(evaluatedQueryTree.getTree());
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		
		while (rs.hasNext()) {
			rs.next();
			cnt++;
		}
		qe.close();
		return cnt;
	}

}
