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

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * @author Lorenz Buehmann
 *
 */
@ComponentAnn(name = "QueryTreeHeuristic", shortName = "qtree_heuristic_complex", version = 0.1)
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
