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
package org.dllearner.algorithms.qtl.operations.nbr.strategy;

import java.util.List;

import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;

/**
 * A strategy used to apply negative-based reduction on query trees.
 * @author Lorenz Bühmann
 *
 */
public interface NBRStrategy {
	
	RDFResourceTree computeNBR(RDFResourceTree posExampleTree, List<RDFResourceTree> negExampleTrees);
	
	List<RDFResourceTree> computeNBRs(RDFResourceTree posExampleTree, List<RDFResourceTree> negExampleTrees);

}
