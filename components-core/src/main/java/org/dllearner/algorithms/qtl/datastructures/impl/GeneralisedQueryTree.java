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
package org.dllearner.algorithms.qtl.datastructures.impl;

import java.util.ArrayList;
import java.util.List;

import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeChange.ChangeType;

public class GeneralisedQueryTree<N> {
	
	private QueryTree<N> tree;
	private List<QueryTreeChange> changes;
	
	public GeneralisedQueryTree(QueryTree<N> tree){
		this.tree = tree;
		changes = new ArrayList<>();
	}
	
	public GeneralisedQueryTree(QueryTree<N> tree, List<QueryTreeChange> changes){
		this.tree = tree;
		this.changes = changes;
	}
	
	public void setQueryTree(QueryTree<N> tree){
		this.tree = tree;
	}
	
	public QueryTree<N> getQueryTree(){
		return tree;
	}
	
	public void addChange(QueryTreeChange change){
		changes.add(change);
	}
	
	public void addChanges(List<QueryTreeChange> changes){
		this.changes.addAll(changes);
	}
	
	public List<QueryTreeChange> getChanges(){
		return changes;
	}
	
	public QueryTreeChange getLastChange(){
		if(changes.isEmpty()){
			return new QueryTreeChange(0, ChangeType.REPLACE_LABEL);
		}
		return changes.get(changes.size()-1);
	}

}
