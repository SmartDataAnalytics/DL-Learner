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
package org.dllearner.core;

import com.google.common.collect.ComparisonChain;
import org.dllearner.algorithms.celoe.OENode;

/**
 * Search algorithm heuristic for the ontology engineering algorithm. The heuristic
 * has a strong bias towards short descriptions (i.e. the algorithm is likely to be
 * less suitable for learning complex descriptions).
 * 
 * @author Jens Lehmann
 *
 */
public abstract class AbstractHeuristic extends AbstractComponent implements Heuristic<OENode>{
	
	public AbstractHeuristic() {}
	
	@Override
	public void init() throws ComponentInitException {

		initialized = true;
	}
	
	@Override
	public int compare(OENode node1, OENode node2) {
		return ComparisonChain.start()
				.compare(getNodeScore(node1), getNodeScore(node2))
				.compare(node1.getDescription(), node2.getDescription())
				.result();
	}

	public abstract double getNodeScore(OENode node);

}
