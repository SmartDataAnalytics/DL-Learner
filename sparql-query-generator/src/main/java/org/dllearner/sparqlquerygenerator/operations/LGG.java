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

import java.util.HashSet;
import java.util.Set;

import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class LGG {
	
	public static <N> QueryTree<N> computeLGG(QueryTreeImpl<N> tree1, QueryTreeImpl<N> tree2){
		QueryTree<N> lgg = compute(tree1, tree2);
		
		return lgg;
	}
	
	private static <N> QueryTreeImpl<N> compute(QueryTreeImpl<N> tree1, QueryTreeImpl<N> tree2){
		QueryTreeImpl<N> lgg = new QueryTreeImpl<N>(tree1.getUserObject());
		
		if(!lgg.getUserObject().equals(tree2.getUserObject())){
			lgg.setUserObject((N)"?");
		}
		
		Set<QueryTreeImpl<N>> addedChildren;
		QueryTreeImpl<N> lggChild;
		for(Object edge : tree1.getEdges()){
			addedChildren = new HashSet<QueryTreeImpl<N>>();
			for(QueryTree<N> child1 : tree1.getChildren(edge)){
				for(QueryTree<N> child2 : tree2.getChildren(edge)){
					lggChild = compute((QueryTreeImpl)child1, (QueryTreeImpl)child2);
					boolean add = true;
					for(QueryTreeImpl<N> addedChild : addedChildren){
						if(addedChild.isSubsumedBy(lggChild)){
							add = false;
							break;
						} else if(lggChild.isSubsumedBy(addedChild)){
							lgg.removeChild(addedChild);
						} 
					}
					if(add){
						lgg.addChild(lggChild, edge);
						addedChildren.add(lggChild);
					}
				}
			}
		}
		return lgg;
	}
	
}
