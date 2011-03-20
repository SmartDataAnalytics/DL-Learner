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
package org.dllearner.algorithm.qtl.operations.nbr.strategy;

import java.util.List;

import org.dllearner.algorithm.qtl.datastructures.QueryTree;
import org.dllearner.algorithm.qtl.datastructures.impl.QueryTreeImpl;


/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class TagNonSubsumingPartsNBRStrategy<N> implements NBRStrategy<N>{

	@Override
	public QueryTree<N> computeNBR(QueryTree<N> posExampleTree,
			List<QueryTree<N>> negExampleTrees) {
		
		for(QueryTree<N> negExampleTree : negExampleTrees){
			negExampleTree.isSubsumedBy(posExampleTree, true);
		}
		
		QueryTree<N> nbr = buildNBR(posExampleTree);
		
		return nbr;
	}

	@Override
	public List<QueryTree<N>> computeNBRs(QueryTree<N> posExampleTree,
			List<QueryTree<N>> negExampleTrees) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private QueryTreeImpl<N> buildNBR(QueryTree<N> tree){
		QueryTreeImpl<N> nbr = new QueryTreeImpl<N>(tree.getUserObject());
		
		for(QueryTree<N> child : tree.getChildren()){
			if(child.isTagged()){
				nbr.addChild(buildNBR(child), tree.getEdge(child));
			}
		}
		
		return nbr;
	}

}
