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
package org.dllearner.algorithms.qtl.operations;

import java.util.ArrayList;

import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;

import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDF;

public class Generalisation {
	
	public RDFResourceTree generalise(RDFResourceTree queryTree){
		RDFResourceTree copy = new RDFResourceTree(queryTree);
		
		copy.setData(RDFResourceTree.DEFAULT_VAR_NODE);
		
		pruneTree(copy, 0.5);
		retainTypeEdges(copy);
		
		return copy;
	}
	
	private void replaceAllLeafs(RDFResourceTree queryTree){
		for(RDFResourceTree leaf : QueryTreeUtils.getLeafs(queryTree)){
			leaf.setData(RDFResourceTree.DEFAULT_VAR_NODE);
		}
	}
	
	private void pruneTree(RDFResourceTree tree, double limit){
		int childCountBefore = tree.getNumberOfChildren();
		for(RDFResourceTree child : tree.getChildren()){
			tree.removeChild(child);
			if((double)tree.getNumberOfChildren()/childCountBefore <= 0.5){
				break;
			}
		}
	}
	
	private void retainTypeEdges(RDFResourceTree tree){
		for(Node edge : tree.getEdges()) {
			if(!edge.equals(RDF.type.asNode())) {
				for(RDFResourceTree child : new ArrayList<>(tree.getChildren(edge))){
					tree.removeChild(child, edge);
				}
			}
		}
	}
}
