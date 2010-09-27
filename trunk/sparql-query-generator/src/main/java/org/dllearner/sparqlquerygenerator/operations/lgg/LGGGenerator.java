/**
 * Copyright (C) 2007-2010, Jens Lehmann
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
 *
 */
package org.dllearner.sparqlquerygenerator.operations.lgg;

import java.util.Set;

import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public interface LGGGenerator<N> {
	
	QueryTree<N> getLGG(QueryTree<N> tree1, QueryTree<N> tree2);
	
	QueryTree<N> getLGG(QueryTree<N> tree1, QueryTree<N> tree2, boolean learnFilters);
	
	QueryTree<N> getLGG(Set<QueryTree<N>> trees);
	
	QueryTree<N> getLGG(Set<QueryTree<N>> trees, boolean learnFilters);

}
