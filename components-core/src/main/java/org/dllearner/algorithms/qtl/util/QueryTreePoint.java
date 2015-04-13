/**
 * 
 */
package org.dllearner.algorithms.qtl.util;

import java.util.Collection;

import org.apache.commons.math3.stat.clustering.Clusterable;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.heuristics.QueryTreeEditDistance;

/**
 * A wrapper class that allows for using DBSCANClusterer class of Apache Commons
 * Math with query trees.
 * @author Lorenz Buehmann
 *
 */
public class QueryTreePoint implements Clusterable<QueryTreePoint>{
	
	private RDFResourceTree queryTree;

	public QueryTreePoint(RDFResourceTree queryTree) {
		this.queryTree = queryTree;
	}
	
	/**
	 * @return the queryTree
	 */
	public RDFResourceTree getQueryTree() {
		return queryTree;
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.math3.stat.clustering.Clusterable#distanceFrom(java.lang.Object)
	 */
	@Override
	public double distanceFrom(QueryTreePoint p) {
		return QueryTreeEditDistance.getDistanceApprox(queryTree, p.getQueryTree());
	}

	/* (non-Javadoc)
	 * @see org.apache.commons.math3.stat.clustering.Clusterable#centroidOf(java.util.Collection)
	 */
	@Override
	public QueryTreePoint centroidOf(Collection<QueryTreePoint> p) {
		return null;
	}


}
