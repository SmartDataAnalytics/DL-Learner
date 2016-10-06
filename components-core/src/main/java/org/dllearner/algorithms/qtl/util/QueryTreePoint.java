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
package org.dllearner.algorithms.qtl.util;

import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.heuristics.QueryTreeEditDistance;

import java.util.Collection;

@SuppressWarnings("deprecation") // deprecated
interface Clusterable<T> extends org.apache.commons.math3.stat.clustering.Clusterable<T> {}

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
