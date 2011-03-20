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
package org.dllearner.algorithm.qtl.operations.nbr;

import java.util.Collections;
import java.util.List;

import org.dllearner.algorithm.qtl.datastructures.QueryTree;
import org.dllearner.algorithm.qtl.operations.nbr.strategy.BruteForceNBRStrategy;
import org.dllearner.algorithm.qtl.operations.nbr.strategy.NBRStrategy;

/**
 * 
 * @author Lorenz Bühmann
 * @param <N>
 *
 */
public class NBRGeneratorImpl<N> implements NBRGenerator<N>{
	
	NBRStrategy<N> strategy;
	
	public NBRGeneratorImpl(){
		this.strategy = new BruteForceNBRStrategy<N>();
	}
	
	public NBRGeneratorImpl(NBRStrategy<N> strategy){
		this.strategy = strategy;
	}

	@Override
	public QueryTree<N> getNBR(QueryTree<N> posExampleTree,
			QueryTree<N> negExampleTree) {
		return strategy.computeNBR(posExampleTree, Collections.singletonList(negExampleTree));
	}

	@Override
	public QueryTree<N> getNBR(QueryTree<N> posExampleTree,
			List<QueryTree<N>> negExampleTrees) {
		return strategy.computeNBR(posExampleTree, negExampleTrees);
	}

	@Override
	public List<QueryTree<N>> getNBRs(QueryTree<N> posExampleTree,
			QueryTree<N> negExampleTree) {
		return strategy.computeNBRs(posExampleTree, Collections.singletonList(negExampleTree));
	}

	@Override
	public List<QueryTree<N>> getNBRs(QueryTree<N> posExampleTree,
			List<QueryTree<N>> negExampleTrees) {
		return strategy.computeNBRs(posExampleTree, negExampleTrees);
	}

	@Override
	public List<QueryTree<N>> getNBRs(QueryTree<N> posExampleTree,
			QueryTree<N> negExampleTree, int limit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<QueryTree<N>> getNBRs(QueryTree<N> posExampleTree,
			List<QueryTree<N>> negExampleTrees, int limit) {
		// TODO Auto-generated method stub
		return null;
	}
	

}
