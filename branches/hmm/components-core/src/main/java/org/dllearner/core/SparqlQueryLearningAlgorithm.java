/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

package org.dllearner.core;

import java.util.List;

/**
 * Basic interface for algorithms learning SPARQL queries.
 * 
 * TODO: Check whether it is necessary to have a "real" SPARQL query class instead of 
 * only a string.
 * 
 * @author Jens Lehmann
 *
 */
public interface SparqlQueryLearningAlgorithm extends LearningAlgorithm {

	/**
	 * @param nrOfSPARQLQueries Limit for the number or returned SPARQL queries.
	 * @return The best SPARQL queries found by the learning algorithm so far.
	 */
	public List<String> getCurrentlyBestSPARQLQueries(int nrOfSPARQLQueries);
	
	public String getBestSPARQLQuery();
	
	
	
}
