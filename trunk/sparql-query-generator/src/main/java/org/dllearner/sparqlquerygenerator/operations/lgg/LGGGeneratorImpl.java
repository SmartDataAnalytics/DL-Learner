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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dllearner.sparqlquerygenerator.datastructures.QueryTree;
import org.dllearner.sparqlquerygenerator.datastructures.impl.QueryTreeImpl;

/**
 * 
 * @author Lorenz Bühmann
 *
 */
public class LGGGeneratorImpl<N> implements LGGGenerator<N>{

	@Override
	public QueryTree<N> getLGG(QueryTree<N> tree1, QueryTree<N> tree2) {
		return computeLGG(tree1, tree2);
	}

	@Override
	public QueryTree<N> getLGG(Set<QueryTree<N>> trees) {
		if(trees.size() == 1){
			return trees.iterator().next();
		}
		
		List<QueryTree<N>> treeList = new ArrayList<QueryTree<N>>(trees);
		QueryTree<N> lgg = computeLGG(treeList.get(0), treeList.get(1));
		for(int i = 2; i < treeList.size(); i++){
			lgg = computeLGG(lgg, treeList.get(i));
		}
		
		return lgg;
	}
	
	private QueryTree<N> computeLGG(QueryTree<N> tree1, QueryTree<N> tree2){
		QueryTree<N> lgg = new QueryTreeImpl<N>(tree1.getUserObject());
		
		if(!lgg.getUserObject().equals(tree2.getUserObject())){
			lgg.setUserObject((N)"?");
		}
		
		Set<QueryTreeImpl<N>> addedChildren;
		QueryTreeImpl<N> lggChild;
		for(Object edge : tree1.getEdges()){
			addedChildren = new HashSet<QueryTreeImpl<N>>();
			for(QueryTree<N> child1 : tree1.getChildren(edge)){
				for(QueryTree<N> child2 : tree2.getChildren(edge)){
					lggChild = (QueryTreeImpl<N>) computeLGG(child1, child2);
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
