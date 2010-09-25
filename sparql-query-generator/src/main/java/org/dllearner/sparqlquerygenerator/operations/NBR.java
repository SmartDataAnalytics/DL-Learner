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
package org.dllearner.sparqlquerygenerator.operations;

import java.util.Set;

import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class NBR {
	
	
	public static <N> QueryTree<N> computeNBR(QueryTreeImpl<N> posExampleTree, Set<QueryTreeImpl<N>> negExampleTrees){
		QueryTreeImpl<N> nbr = new QueryTreeImpl<N>(posExampleTree.getUserObject());
		
		return nbr;
	}
	
	public static <N> QueryTree<N> computeNBR(QueryTreeImpl<N> posTree, QueryTreeImpl<N> negTree){
		negTree.isSubsumedBy(posTree, true);
		QueryTreeImpl<N> nbr = buildNBR(posTree);
		
		return nbr;
	}
	
	private static <N> QueryTreeImpl<N> buildNBR(QueryTree<N> tree){
		QueryTreeImpl<N> nbr = new QueryTreeImpl<N>(tree.getUserObject());
		
		for(QueryTree<N> child : tree.getChildren()){
			if(child.isTagged()){
				nbr.addChild(buildNBR(child), tree.getEdge(child));
			}
		}
		
		return nbr;
	}

}
